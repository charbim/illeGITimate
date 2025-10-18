package components;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.codec.digest.DigestUtils;

public class Index {

    /*
     * This is an HashMap of all the files that are in the objects directory.
     * Specifically, it contains the paths of all the added Files. I want this to
     * have an easy way of looking up whether a file exists (with good efficiency)
     * and it helps me not have to repeatedly iterate over the index when I can just
     * initialize this and iterate through it for all my needs.
     * 
     * TLDR: storedFiles is a HashMap that represents index that will be tinkered
     * with at run-time and the result will be written back to index
     * 
     * REMEMBER: <path, unique hash> since an index can only hold a path once,
     * whereas an index can hold the same hash many times
     * 
     * could always refactor the hash to be an IndexEntry or some other custom
     * object...
     */
    private HashMap<String, String> storedFiles = new HashMap<String, String>();
    private HashSet<String> allFilePaths = new HashSet<>();
    private File index;
    private int numberOfEntries;

    public Index(String pathname) throws IOException {
        initializePath(pathname);
    }

    // GETTERS

    public HashMap<String, String> getStoredFiles() {
        return storedFiles;
    }

    public boolean exists() {
        return index.exists();
    }

    public int getNumberOfEntries() {
        return numberOfEntries;
    }

    // METHODS

    /*
     * Since the private File index is initialized without a pathname, this
     * method assigns a pathname to index so that it can point somewhere
     */
    private void initializePath(String pathname) {
        index = new File(pathname);
    }

    /*
     * Once the pathname is assigned, this makes the index file
     */
    public void initialize() throws IOException {
        index.createNewFile();
    }

    /*
     * Using apache library, which is gitignored. If this is not working for
     * someone, download the jar files from Google
     */
    private String generateSha1Hex(File file) throws IOException {
        return DigestUtils.sha1Hex(Files.readAllBytes(file.toPath()));
    }

    /*
     * Remember that storedFiles is the run-time representation of the index file.
     * This method is called when running the program when an index already exists.
     * This allows the HashMap to be filled with Files using the data within index.
     * 
     * 41 is the length of the hash.
     */
    public void sync() throws IOException {
        numberOfEntries = 0;

        BufferedReader br = new BufferedReader(new FileReader(index));
        while (br.ready()) {
            String line = br.readLine();
            String hash = line.substring(0, 40); // apoloigies for the magic number
            String pathname = line.substring(41, line.length());
            storedFiles.put(pathname, hash);
            hashPaths(pathname);
            numberOfEntries += 1;
        }

        br.close();
    }

    /*
     * The same as above, but for adding a single file to storedFiles
     */
    public void addFile(File file) throws IOException {
        storedFiles.put(file.getPath(), generateSha1Hex(file));
        hashPaths(file.getPath());
        rewrite();
    }

    /*
     * Rebuilds the index by deleting the index file and then rewriting it from
     * what's stored in storedFiles. This needs to be done every time a commit is
     * made. I understand that there is probably a better way to do this.
     */
    private void rewrite() throws IOException {
        index.delete();
        index.createNewFile();
        numberOfEntries = 0;

        for (String pathname : storedFiles.keySet()) {
            numberOfEntries += 1;
            appendFile(new File(pathname));
        }
    }

    /*
     * writes a new line to the index. this only ever happens after storedFiles has
     * been modified
     */
    private void appendFile(File file) throws IOException {
        // Checking if index exists
        if (!this.exists()) {
            throw new FileNotFoundException("appendFileToIndex(File file): Index file does not exist");
        }

        String hash = generateSha1Hex(file);
        String pathname = file.getPath();

        // True means the FileWriter is appending the text
        BufferedWriter bw = new BufferedWriter(new FileWriter(index, true));

        // First line doesn't need a new line, subsequent edits do
        if (!Files.readString(index.toPath()).isEmpty()) {
            bw.newLine();
        }

        bw.write(hash + " " + pathname);
        bw.close();
    }

    public boolean delete() {
        return index.delete();
    }

    public void clear() throws IOException {
        index.delete();
        index.createNewFile();
        storedFiles.clear();
        numberOfEntries = 0;
    }

    public boolean containsPath(String pathname) {
        return storedFiles.containsKey(pathname);
    }

    public boolean containsHash(String pathname, String hash) {
        return storedFiles.get(pathname).equals(hash);
    }

    // is this inefficient?? yes, absolutely. Do I care???? no. no I do not.
    // basically makes sure all necessary folder paths are recognized so you can stage what is needed. I could make this more efficient but that would require me to rewrite a whole lot of tree functionality which is not my job. sorry miles :(
    // Hashes all used Files/Folders in "allFilePaths" to be referenced later.
    public void hashPaths(String pathname) {
        int nextDir = pathname.lastIndexOf("/");
        if (nextDir != -1) {
            String name = pathname.substring(nextDir + 1);
            if (!allFilePaths.contains(name)) {
                allFilePaths.add(name);
            }
            hashPaths(pathname.substring(0, nextDir));
        }
    }

    //checks if the index has the file/folder staged
    // there was def a better way to do this w/mile's code but bc the tree person didn't really use the hashmap functionality I felt bad deleting miles' stuff (in fear of wrecking the code) & just created my own thing. (adding shas would be too much effort I was not ready to *COMMIT* to -- haha, get my joke?)
    public boolean contains(String singlePath) {
        return allFilePaths.contains(singlePath);
    }

    //ignore this, i needed to test if it was storing stuff in the correct format.
    public String toString(){
        return allFilePaths.toString();
    }

}
