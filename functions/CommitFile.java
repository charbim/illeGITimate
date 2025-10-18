package functions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.Scanner;

public class CommitFile {
    private String tree;
    private String parent;
    private String author;
    private String date;
    private String message;
    private String hash;

    private IlleGITimate git;


    // creates a "CommitFile" without actually COMMITTING the file (counter intuitive, ik -- kinda works like the File java class.)
    // User inputs the author and message
    public CommitFile(String author, String message, IlleGITimate git) throws IOException {
        this.git = git;
        this.tree = git.createTreeObjectsFromStagedFiles();
        parent = git.getHEAD().getContents();
        this.author = author;
        date = (new Date()).toString();
        this.message = message;
    }

    // like the previous, except it takes the user input.
    public CommitFile(String tree) throws IOException {
        String[] userInfo = inputCommitInfo();
        this.tree = tree;
        parent = git.getHEAD().getContents();
        this.author = userInfo[0];
        date = (new Date()).toString();
        this.message = userInfo[1];
    }

    // actually stores the new commitfile in objects
    public void commitFile() throws IOException {
        String contents = buildCommitInfoString();
        String hash = git.getObjects().generateSha1Hex(createInfoFile("commit", contents));
        new File("commit").delete();

        File f = createInfoFile(hash, contents);
        git.getObjects().addFile(f);
        f.delete();

        git.getHEAD().update(hash);
    }

    // inconvience. wow. very annoying. darn you previous people.
    public File createInfoFile(String name, String contents) throws IOException {
        File f = new File(name);
        BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
        bw.write(contents);
        bw.close();
        return f;
    }

    // creates a string containing the contents of our commit file
    public String buildCommitInfoString() {

        // TITLE FILL OUT
        // 2D array w/commit info (bc hashmap has no guarentee info stored in correct order)
        // Columns --> each data type
        // Rows --> [0]: Titles; [1]: Info;
        String[][] commitInfo = new String[2][5];
        String[] infoType = new String[]{"tree", "parent", "author", "date", "message"};
        for (int i = 0; i < 5; i++) {
            commitInfo[0][i] = infoType[i];
        }

        // INFO ADD
        commitInfo[1][0] = tree;
        commitInfo[1][1] = parent;
        commitInfo[1][2] = author;
        commitInfo[1][3] = date;
        commitInfo[1][4] = message;

        // Write into SB
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(commitInfo[0][i] + ": " + commitInfo[1][i]);
            if (i != 4) {
                sb.append("\n");
            }
        }

        return sb.toString();

    }

    // Takes user typing inputs and creates a new commit based on that information.
    public String[] inputCommitInfo() throws IOException {
        // return string with data formatted as so: [Author, Message]
        String[] userInfo = new String[2];

        // Username
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter author name: ");
        userInfo[0] = sc.nextLine();

        // Message
        System.out.println("Enter Commit Summary: ");
        userInfo[1] = sc.nextLine();
        while (sc.hasNext()) {
            userInfo[1] += sc.nextLine();
        }

        sc.close();
        return userInfo;
    }

    // returns the hash of this given commit.
    public String getHash() {
        return hash;
    }

}