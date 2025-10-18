package components;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Head {
    
    private File HEAD;
    private String contents;
    
    public Head(String pathname) throws IOException {
        initializePath(pathname);
        contents = "";
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
        BufferedWriter bw = new BufferedWriter(new FileWriter(HEAD, false));
        bw.write(commitHash);
        bw.close();
        setContents(commitHash);
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    // public String getContents() {
    //     if (contents == null) {
    //         return "";
    //     }
    //     return contents;
    // }

    public void clear() throws IOException {
        HEAD.delete();
        HEAD.createNewFile();
    }


    public String getContents() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(HEAD));
        String contents = br.readLine();
        br.close();
        if (contents == null) {
            return "";
        }
        return contents;
    }

}
