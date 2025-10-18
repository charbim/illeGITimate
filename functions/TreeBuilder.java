package functions;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import org.apache.commons.codec.digest.*;
import components.*;

/**
 * Milestones 3.0 + 3.1:
 * - Build each tree object that rerpresent directories
 * - Each tree entry is one line, ie: "blob <SHA1> <name>" or "tree <SHA1> <name>"
 * - Recursvely generate trees bottom-up so that parent hashes update on adds
 * - Maintain working list file that mirrors prefixed-index format
 * - Stage all found files into Index (index should have BLOBS ONLY)
 */

public class TreeBuilder {
    public File repoRoot;
    public File objectsDir;
    public File workingListFile;
    public Index index;

    public TreeBuilder(File repoRoot, Index index) throws IOException {
        // use the parameter (correct name) to initialize the field
        this.repoRoot = repoRoot.getCanonicalFile();
        this.objectsDir = new File(this.repoRoot, "git" + File.separator + "objects");
        this.workingListFile = new File(this.repoRoot, "git" + File.separator + "working_list");
        this.index = index;

        if (!objectsDir.isDirectory()) {
            throw new IOException("Objects directory not found at " + objectsDir.getAbsolutePath());
        }

        // Make sure that the list file actually exists (3.1 req)
        if (!workingListFile.exists()) workingListFile.createNewFile();

        // Clear the working list file
        try (BufferedWriter w = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(workingListFile, false), StandardCharsets.UTF_8))) {
            // start empty
        }
    }


    /**
     * Milestone 3.0/3.1:
     * - Adds directory, stages its files (BLOBS) into the Index, and then writes TREE objects for
     * all subdirectories up to this dictionary. Returns just teh SHA1 of the top-level tree.
     */
    public String addDirectory(String directoryPath) throws IOException {
        Path dir = Paths.get(directoryPath).normalize().toAbsolutePath();
        if (!Files.isDirectory(dir)) throw new IOException("Not a directory: " + dir);

        // Build trees bottom-up and return the hash for the root dir
        String topHash = buildTreeForDirectory(dir, dir);
        return topHash;
    }

    /**
     * Recursively build a tree object for currentDir.
     */
    public String buildTreeForDirectory(Path currentDir, Path rootDir) throws IOException {
        // Collect immediate children (files + subdirs)
        List<Path> files = new ArrayList<>();
        List<Path> dirs  = new ArrayList<>();

        Files.list(currentDir).forEach(p -> {
            // Skip our repository metadata if pointing at the repo root
            String name = p.getFileName().toString();
            if (name.equals("git")) return;   // never walk the repo metadata
            if (name.equals(".git")) return;  // (safety if naming differs)
            if (Files.isDirectory(p) && index.contains(name)) {
                dirs.add(p);
            } else if (Files.isRegularFile(p) && index.contains(name)) {
                files.add(p);
            }
        });

        // For files: ensure blob objects exist + stage them in the index.
        // Also produce the tree entries ("blob <sha> <basename>")
        List<Entry> entries = new ArrayList<>();
        for (Path f : files) {
            String sha = hashBytes(Files.readAllBytes(f));
            // Keep blob in objects if need be (named by SHA1)
            // writeObjectIfMissing(sha, Files.readAllBytes(f));
            // Stage in index (BLOBS ONLY). Use Miles code
            // index.addFile(f.toFile());

            String baseName = f.getFileName().toString();
            entries.add(Entry.blob(sha, baseName));

            // Working list wants relative path from the originally added directory
            // Git stores paths inside tree obejcts with forward slashes NO MATTER WHAT OS
            String relPath = unixify(rootDir.relativize(f).toString());
            appendWorkingListLine("blob", sha, relPath);
        }

        // For directories: recurse, then we have a tree hash for each child
        for (Path d : dirs) {
            String childTreeSha = buildTreeForDirectory(d, rootDir);
            String baseName = d.getFileName().toString();
            entries.add(Entry.tree(childTreeSha, baseName));

            // Working list wants relative path from the originally added directory
            String relPath = unixify(rootDir.relativize(d).toString());
            appendWorkingListLine("tree", childTreeSha, relPath);
        }

        // Sort based on:
        // 1) blobs before trees (to match many examples),
        // 2) then lexicographically by displayed name.
        Collections.sort(entries, Comparator.comparing(Entry::typeOrder).thenComparing(Entry::name));

        // Build the tree file content (NO trailing newline)
        StringBuilder treeContent = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            Entry e = entries.get(i);
            treeContent.append(e.type).append(' ').append(e.sha).append(' ').append(e.name); // Kinda cursed
            if (i < entries.size() - 1) treeContent.append('\n');
        }

        byte[] bytes = treeContent.toString().getBytes(StandardCharsets.UTF_8);
        String treeSha = hashBytes(bytes);

        // Write the tree object into objects/ named by its SHA1 (idempotent)
        writeObjectIfMissing(treeSha, bytes);

        return treeSha;
    }

    /**
     * From here are random helpers
     */
    // --- helpers (paste these three methods exactly) ---

    public static String unixify(String p) {
        return p.replace(File.separatorChar, '/');
    }

    // Works with Miles' code dw
    public String hashBytes(byte[] data) throws IOException {
        return DigestUtils.sha1Hex(data);
    }

    public void writeObjectIfMissing(String sha, byte[] data) throws IOException {
        File out = new File(objectsDir, sha);
        if (out.exists()) return;

        // Correct try-with-resources: one closing ')' and a block with { ... }
        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(data);
        }
    }

    public void appendWorkingListLine(String type, String sha, String relPath) throws IOException {
        // Append one line per spec. keep UNIX-style separators and NO trailing spaces.
        try (BufferedWriter w = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(workingListFile, true), StandardCharsets.UTF_8))) {

            // If file is empty we just write the line. Else always add a newline first
            long len = workingListFile.length();
            if (len > 0) w.newLine();

            w.write(type);
            w.write(' ');
            w.write(sha);
            w.write(' ');
            w.write(relPath);
        }
    }

    // Milestone 3.0/3.1:
    // Tree entries are "blob <SHA1> <name>" or "tree <SHA1> <name>"
    public static class Entry {
        String type; // "blob" or "tree"
        String sha;
        String name; // display name in this directory (no path separators)

        public Entry(String type, String sha, String name) {
            this.type = type;
            this.sha = sha;
            this.name = name;
        }

        static Entry blob(String sha, String name) { return new Entry("blob", sha, name); }
        static Entry tree(String sha, String name) { return new Entry("tree", sha, name); }

        int typeOrder() { return "blob".equals(type) ? 0 : 1; }
        String name() { return name; }
    }
}

// PLEASE work