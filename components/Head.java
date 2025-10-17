package components;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Head {
    
    private File HEAD;
    
    public Head(String pathname) throws IOException {
        initializePath(pathname);
    }
    
    // GETTERS
    
    public boolean exists() {
        return HEAD.exists();
    }

    // METHODS

    /*
     * Since the private File HEAD is initialized without a pathname, this
     * method assigns a pathname to HEAD so that it can point somewhere
     */
    private void initializePath(String pathname){
        HEAD = new File(pathname);
    }

     /*
     * Once the pathname is assigned, this makes the HEAD file
     */
    public void initialize() throws IOException {
        HEAD.createNewFile();
    }

    public boolean delete() {
        return HEAD.delete();
    }

    public void update(String commitHash) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("git/HEAD", false));
        bw.write(commitHash);
        bw.close();
    }

}
