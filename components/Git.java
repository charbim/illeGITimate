package components;

import java.io.File;
import java.util.HashMap;

import functions.CommitFile;

public class Git {

    private File git;

    // Stores all commits.
    private HashMap<String, CommitFile> commitHistory;

    public Git(String pathname) {
        initializePath(pathname);
    }

    // GETTERS

    public boolean exists() {
        return git.exists();
    }

    // METHODS

    /*
     * Since the private File git is initialized without a pathname, this
     * method assigns a pathname to git so that it can point somewhere
     */
    private void initializePath(String pathname) {
        git = new File(pathname);
    }

    /*
     * Once the pathname is assigned, this makes the directory
     */
    public void initialize() {
        git.mkdir();
    }

    public boolean delete() {
        return git.delete();
    }

    public void updateCommitHistory(CommitFile commit) {
        commitHistory.put(commit.getHash(), commit);
    }

    public CommitFile findCommit(String commitHash) {
        return commitHistory.get(commitHash);
    }

}
