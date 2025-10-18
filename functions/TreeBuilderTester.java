package functions;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;

public class TreeBuilderTester {
    public static void main(String[] args) {
        try {
            // Make a temp working directory
            Path tempRoot = Files.createTempDirectory("git-treebuilder-demo-");
            System.out.println("Workspace: " + tempRoot.toAbsolutePath());

            // Add some files to the temp directory
            Path myProgram = tempRoot.resolve("myProgram");
            Path scripts = myProgram.resolve("scripts");
            Files.createDirectories(scripts);

            writeUtf8(myProgram.resolve("README.md"), "# My Program\nJust a demo.\n");
            writeUtf8(myProgram.resolve("Hello.txt"), "I am a fan of taylor swift but i don't tell anyone because it is self-incriminating\n");
            writeUtf8(scripts.resolve("Cat.java"),
                    "public class Cat { public String meow(){ return \"meow\"; } }\n");

            // IlleGITimate writes git/, git/objects, git/index, etc.
            IlleGITimate repo = new IlleGITimate(tempRoot.toString());

            // Make trees and stage blobs
            TreeBuilder tb = new TreeBuilder(tempRoot.toFile(), repo.getIndex());
            String rootTreeSha = tb.addDirectory(myProgram.toString());

            System.out.println("\n--- First run ---");
            System.out.println("Top-level tree SHA: " + rootTreeSha);

            // PRint working list
            Path workingList = tempRoot.resolve("git").resolve("working_list");
            System.out.println("\n-- git/working_list --");
            printFile(workingList.toFile());

            // Show the saved tree object (top-level tree)
            Path objectsDir = tempRoot.resolve("git").resolve("objects");
            Path treeObjectPath = objectsDir.resolve(rootTreeSha);
            System.out.println("\n-- git/objects/" + rootTreeSha + " (top-level tree object) --");
            printFile(treeObjectPath.toFile());

            // Add another file to scripts, rebuild, and show that hashes update
            writeUtf8(scripts.resolve("Dog.java"), "public class Dog { public String woof(){ return \"woof\"; } }\n");

            String newRootTreeSha = tb.addDirectory(myProgram.toString());

            System.out.println("\n--- Second run (after adding scripts/Dog.java) ---");
            System.out.println("New top-level tree SHA: " + newRootTreeSha);
            System.out.println("(Changed: " + !newRootTreeSha.equals(rootTreeSha) + ")");

            // Show updated working list and updated top-level tree object
            System.out.println("\n-- git/working_list (after second run) --");
            printFile(workingList.toFile());

            Path newTreeObjectPath = objectsDir.resolve(newRootTreeSha);
            System.out.println("\n-- git/objects/" + newRootTreeSha + " (updated top-level tree object) --");
            printFile(newTreeObjectPath.toFile());

            System.out.println("\nDone. Temp workspace left at: " + tempRoot);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper methods
     * @param path
     * @param content
     * @throws IOException
     */

    public static void writeUtf8(Path path, String content) throws IOException {
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    public static void printFile(File f) throws IOException {
        if (!f.exists()) {
            System.out.println("(missing) " + f.getAbsolutePath());
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) System.out.println(line);
        }
    }
}
