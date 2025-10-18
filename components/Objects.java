package components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.codec.digest.DigestUtils;

public class Objects {

    private File objects;

    public Objects(String pathname) throws IOException {
        initializePath(pathname);
    }

    // GETTERS

    public boolean exists() {
        return objects.exists();
    }


    // METHODS

    /*
     * Since the private File objects is initialized without a pathname, this
     * method assigns a pathname to objects so that it can point somewhere
     */
    private void initializePath(String pathname) {
        objects = new File(pathname);
    }

    /*
     * Once the pathname is assigned, this makes the objects directory
     */
    public void initialize() {
        objects.mkdir();
    }

    /*
     * Using apache library, which is gitignored. If this is not working for
     * someone, download the jar files from Google
     */
    public String generateSha1Hex(File file) throws IOException {
        return DigestUtils.sha1Hex(Files.readAllBytes(file.toPath()));
    }

    /*
     * This generates the BLOB and puts it in git/objects/
     * returns hash
     */
    public String addFile(File file) throws IOException {
        String hash = generateSha1Hex(file);
        File objectsFile = new File(objects.getPath() + File.separator + hash);

        //TODO: compression would go here

        // logic to copy stuff from file to objectsFile
        // sleek!
        FileOutputStream fos = new FileOutputStream(objectsFile);
        Files.copy(file.toPath(), fos);
        fos.close();
        return hash;
    }

    /*
     * Objects is a directory containing many files. Java can only delete the
     * directory if it is empty, thus this method deletes everything within objects
     * first before finally deleting the directory objects.
     */
    public boolean delete() {
        deleteContents();
        return objects.delete();
    }

    public void deleteContents(){
        for (File file : objects.listFiles()) {
            file.delete();
        }
    }

    public File[] listFiles() {
        return objects.listFiles();
    }

    public String[] list() {
        return objects.list();
    }
}
