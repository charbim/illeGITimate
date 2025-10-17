import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import org.apache.commons.codec.digest.DigestUtils;

public class IlleGITimateTester {

    public static final String RB = "\033[1;31m"; // RED
    public static final String GB = "\033[1;32m"; // GREEN
    public static final String RESET_COLOR = "\u001B[0m"; // RESET

    public static void main(String[] args) throws IOException {
        // git/objects/ needs to be clear before doing any tests
        IlleGITimate test = new IlleGITimate();
        test.clearRepository();

        /*
         * Normal Behavior Tests
         */
        testRepositoryCreation(test);

        testCommittingNewTextFiles(test);

        testClearingRepository(test);

        testCommittingDuplicateTextFiles(test);

        testCommittingNewImages(test);

        testRepeatedlyCreatingAndClearingRepository(test);

        System.out.println();

        /*
         * Edge Case Testing
         */

        testCommittingNonexistentFile(test);

        testClearingUnhealthyRepository(test);

        testDeletingAlreadyDeletedRepository(test);

        // test.deleteRepository();
    }

    public static void testRepositoryCreation(IlleGITimate test) {

        // (1) Tests if all the folders are in the right locations and created
        if (test.isRepositoryHealthy()) {
            System.out.println(GB + "Passed || (1) testRepositoryCreation" + RESET_COLOR);
        } else {
            System.out.println(RB + "Failed || (1) testRepositoryCreation" + RESET_COLOR);
        }

    }

    public static void testCommittingNewTextFiles(IlleGITimate test) throws IOException {
        File a = new File("testTextFiles" + File.separator + "a.txt");
        File b = new File("testTextFiles" + File.separator + "b.txt");
        File c = new File("testTextFiles" + File.separator + "c.txt");
        File d = new File("testTextFiles" + File.separator + "d.txt");
        File sampleText = new File("testTextFiles" + File.separator + "sampleText.txt");
        File[] files = { a, b, c, d, sampleText };
        String[] contents = new String[files.length];
        String[] hashes = new String[files.length];

        for (int i = 0; i < files.length; i++) {
            contents[i] = Files.readString(files[i].toPath());
            hashes[i] = DigestUtils.sha1Hex(Files.readString(files[i].toPath()));
        }

        for (File file : files) {
            test.stageFile(file);
        }

        // (1) Checks if the right number of files were put in objects directory
        if (test.getObjects().listFiles().length == files.length) {
            System.out.println(GB + "Passed || (1) testCommittingNewFiles" + RESET_COLOR);
        } else {
            System.out.println(RB + "Failed || (1) testCommittingNewFiles" + RESET_COLOR);
        }

        // (2) Checks to see if the created files have the right hashed names
        for (int i = 0; i < hashes.length; i++) {
            if (!Arrays.asList(test.getObjects().list()).contains(hashes[i])) {
                System.out.println(RB + "Failed || (2) testCommittingNewFiles" + RESET_COLOR);
                break;
            }
            if (i == hashes.length - 1) {
                System.out.println(GB + "Passed || (2) testCommittingNewFiles" + RESET_COLOR);
            }
        }

        // (3) Checks to see if the created files have the right contents
        String[] objectsDirContents = new String[test.getObjects().listFiles().length];
        for (int i = 0; i < objectsDirContents.length; i++) {
            objectsDirContents[i] = Files.readString(test.getObjects().listFiles()[i].toPath());
        }

        for (int i = 0; i < contents.length; i++) {
            if (!Arrays.asList(objectsDirContents).contains(contents[i])) {
                System.out.println(RB + "Failed || (3) testCommittingNewFiles" + RESET_COLOR);
                break;
            }
            if (i == contents.length - 1) {
                System.out.println(GB + "Passed || (3) testCommittingNewFiles" + RESET_COLOR);
            }
        }

        // (4) Checks to see if the index has the right number of entries
        if (test.getIndex().getNumberOfEntries() == files.length) {
            System.out.println(GB + "Passed || (4) testCommittingNewFiles" + RESET_COLOR);
        } else {
            System.out.println(RB + "Failed || (4) testCommittingNewFiles" + RESET_COLOR);
        }

        // (5) Checks to see if the index has all the path names in it
        for (int i = 0; i < files.length; i++) {
            if (!test.getIndex().containsPath(files[i].getPath())) {
                System.out.println(RB + "Failed || (5) testCommittingNewFiles" + RESET_COLOR);
                break;
            }
            if (i == files.length - 1) {
                System.out.println(GB + "Passed || (5) testCommittingNewFiles" + RESET_COLOR);
            }
        }

        // (6) Checks to see if the index has all the hashes in it
        for (int i = 0; i < files.length; i++) {
            if (!test.getIndex().containsHash(files[i].getPath(), hashes[i])) {
                System.out.println(RB + "Failed || (6) testCommittingNewFiles" + RESET_COLOR);
                break;
            }
            if (i == files.length - 1) {
                System.out.println(GB + "Passed || (6) testCommittingNewFiles" + RESET_COLOR);
            }
        }

        // Cleans the repository so that the next test can use it
        test.clearRepository();
    }

    public static void testClearingRepository(IlleGITimate test) throws IOException {
        File a = new File("testTextFiles" + File.separator + "a.txt");
        test.stageFile(a);
        test.clearRepository();
        if (test.getIndex().getNumberOfEntries() == 0 && test.getObjects().listFiles().length == 0) {
            System.out.println(GB + "Passed || (1) testClearingRepository" + RESET_COLOR);
        } else {
            System.out.println(RB + "Failed || (1) testClearingRepository" + RESET_COLOR);
        }
    }

    public static void testCommittingDuplicateTextFiles(IlleGITimate test) throws IOException {
        File a = new File("testTextFiles" + File.separator + "a.txt");
        File b = new File("testTextFiles" + File.separator + "b.txt");
        File c = new File("testTextFiles" + File.separator + "c.txt");
        File d = new File("testTextFiles" + File.separator + "d.txt");
        File sampleText = new File("testTextFiles" + File.separator + "sampleText.txt");

        // Remember that these are duplicates (in contents) of the other files
        File a2 = new File("testTextFiles" + File.separator + "a2.txt");
        File b2 = new File("testTextFiles" + File.separator + "b2.txt");
        File c2 = new File("testTextFiles" + File.separator + "c2.txt");
        File d2 = new File("testTextFiles" + File.separator + "d2.txt");
        File[] files = { a, b, c, d, sampleText, a2, b2, c2, d2 };
        File[] originals = { a, b, c, d, sampleText };
        String[] contents = new String[files.length];
        String[] hashes = new String[files.length];

        for (int i = 0; i < files.length; i++) {
            contents[i] = Files.readString(files[i].toPath());
            hashes[i] = DigestUtils.sha1Hex(Files.readString(files[i].toPath()));
        }

        for (File file : files) {
            test.stageFile(file);
        }

        // (1) Checks if the right number of files were put in objects directory
        if (test.getObjects().listFiles().length == originals.length) {
            System.out.println(GB + "Passed || (1) testCommittingDuplicateFiles" + RESET_COLOR);
        } else {
            System.out.println(RB + "Failed || (1) testCommittingDuplicateFiles" + RESET_COLOR);
        }

        // (2) Checks to see if the created files have the right hashed names
        for (int i = 0; i < hashes.length; i++) {
            if (!Arrays.asList(test.getObjects().list()).contains(hashes[i])) {
                System.out.println(RB + "Failed || (2) testCommittingDuplicateFiles" + RESET_COLOR);
                break;
            }
            if (i == hashes.length - 1) {
                System.out.println(GB + "Passed || (2) testCommittingDuplicateFiles" + RESET_COLOR);
            }
        }

        // (3) Checks to see if the created files have the right contents
        String[] objectsDirContents = new String[test.getObjects().listFiles().length];
        for (int i = 0; i < objectsDirContents.length; i++) {
            objectsDirContents[i] = Files.readString(test.getObjects().listFiles()[i].toPath());
        }

        for (int i = 0; i < contents.length; i++) {
            if (!Arrays.asList(objectsDirContents).contains(contents[i])) {
                System.out.println(RB + "Failed || (3) testCommittingDuplicateFiles" + RESET_COLOR);
                break;
            }
            if (i == contents.length - 1) {
                System.out.println(GB + "Passed || (3) testCommittingDuplicateFiles" + RESET_COLOR);
            }
        }

        // (4) Checks to see if the index has the right number of entries
        if (test.getIndex().getNumberOfEntries() == files.length) {
            System.out.println(GB + "Passed || (4) testCommittingDuplicateFiles" + RESET_COLOR);
        } else {
            System.out.println(RB + "Failed || (4) testCommittingDuplicateFiles" + RESET_COLOR);
        }

        // (5) Checks to see if the index has all the path names in it
        for (int i = 0; i < files.length; i++) {
            if (!test.getIndex().containsPath(files[i].getPath())) {
                System.out.println(RB + "Failed || (5) testCommittingDuplicateFiles" + RESET_COLOR);
                break;
            }
            if (i == files.length - 1) {
                System.out.println(GB + "Passed || (5) testCommittingDuplicateFiles" + RESET_COLOR);
            }
        }

        // (6) Checks to see if the index has all the hashes in it
        for (int i = 0; i < files.length; i++) {
            if (!test.getIndex().containsHash(files[i].getPath(), hashes[i])) {
                System.out.println(RB + "Failed || (6) testCommittingDuplicateFiles" + RESET_COLOR);
                break;
            }
            if (i == files.length - 1) {
                System.out.println(GB + "Passed || (6) testCommittingDuplicateFiles" + RESET_COLOR);
            }
        }

        // Cleans the repository so that the next test can use it
        test.clearRepository();
    }

    // Checking if the contents match is too hard for this test cus its a byte array
    public static void testCommittingNewImages(IlleGITimate test) throws IOException {
        File a = new File("testImages" + File.separator + "a.jpg");
        File b = new File("testImages" + File.separator + "b.jpg");
        File c = new File("testImages" + File.separator + "c.jpg");
        File d = new File("testImages" + File.separator + "d.jpg");

        File[] files = { a, b, c, d };
        String[] hashes = new String[files.length];

        for (int i = 0; i < files.length; i++) {
            hashes[i] = DigestUtils.sha1Hex(Files.readAllBytes(files[i].toPath()));
        }

        for (File file : files) {
            test.stageFile(file);
        }

        // (1) Checks if the right number of files were put in objects directory
        if (test.getObjects().listFiles().length == files.length) {
            System.out.println(GB + "Passed || (1) testCommittingImages" + RESET_COLOR);
        } else {
            System.out.println(RB + "Failed || (1) testCommittingImages" + RESET_COLOR);
        }

        // (2) Checks to see if the created files have the right hashed names
        for (int i = 0; i < hashes.length; i++) {
            if (!Arrays.asList(test.getObjects().list()).contains(hashes[i])) {
                System.out.println(RB + "Failed || (2) testCommittingImages" + RESET_COLOR);
                break;
            }
            if (i == hashes.length - 1) {
                System.out.println(GB + "Passed || (2) testCommittingImages" + RESET_COLOR);
            }
        }

        // (3) Checks to see if the index has the right number of entries
        if (test.getIndex().getNumberOfEntries() == files.length) {
            System.out.println(GB + "Passed || (4) testCommittingImages" + RESET_COLOR);
        } else {
            System.out.println(RB + "Failed || (4) testCommittingImages" + RESET_COLOR);
        }

        // (4) Checks to see if the index has all the path names in it
        for (int i = 0; i < files.length; i++) {
            if (!test.getIndex().containsPath(files[i].getPath())) {
                System.out.println(RB + "Failed || (5) testCommittingImages" + RESET_COLOR);
                break;
            }
            if (i == files.length - 1) {
                System.out.println(GB + "Passed || (5) testCommittingImages" + RESET_COLOR);
            }
        }

        // (5) Checks to see if the index has all the hashes in it
        for (int i = 0; i < files.length; i++) {
            if (!test.getIndex().containsHash(files[i].getPath(), hashes[i])) {
                System.out.println(RB + "Failed || (6) testCommittingImages" + RESET_COLOR);
                break;
            }
            if (i == files.length - 1) {
                System.out.println(GB + "Passed || (6) testCommittingImages" + RESET_COLOR);
            }
        }

        // Cleans the repository so that the next test can use it
        test.clearRepository();
    }

    public static void testRepeatedlyCreatingAndClearingRepository(IlleGITimate test) {
        try {
            File a = new File("testTextFiles" + File.separator + "a.txt");
            File b = new File("testTextFiles" + File.separator + "b.txt");
            File c = new File("testTextFiles" + File.separator + "c.txt");
            File d = new File("testTextFiles" + File.separator + "d.txt");
            File sampleText = new File("testTextFiles" + File.separator + "sampleText.txt");
            File[] files = { a, b, c, d, sampleText };

            // creates and removes files 100 times lol
            for (int i = 0; i < 100; i++) {
                test.clearRepository();

                for (File file : files) {
                    test.stageFile(file);
                }
            }

            test.clearRepository();
        } catch (Exception e) {
            System.out.println(RB + "Failed || (1) testRepeatedlyCreatingAndClearingRepository" + RESET_COLOR);
        }

        System.out.println(GB + "Passed || (1) testRepeatedlyCreatingAndClearingRepository" + RESET_COLOR);
    }

    public static void testCommittingNonexistentFile(IlleGITimate test) throws IOException {
        File nonexistentFile = new File("thisisnotafile");

        try {
            test.stageFile(nonexistentFile);
            System.out.println(GB + "Passed || (1) testCommittingNonexistentFile" + RESET_COLOR);
        } catch (Exception e) {
            System.out.println(RB + "Failed || (1) testCommittingNonexistentFile" + RESET_COLOR);
        }
    }

    public static void testClearingUnhealthyRepository(IlleGITimate test) throws IOException {
        test.deleteRepository();
        try {
            test.clearRepository();
            System.out.println(GB + "Passed || (1) testClearingUnhealthyRepository" + RESET_COLOR);
        } catch (Exception e) {
            System.out.println(RB + "Failed || (1) testClearingUnhealthyRepository" + RESET_COLOR);
        }
        test.createRepository();
    }

    public static void testDeletingAlreadyDeletedRepository(IlleGITimate test) throws IOException {

        try {
            test.deleteRepository();
            test.deleteRepository();
            System.out.println(GB + "Passed || (1) testDeletingAlreadyDeletedRepository" + RESET_COLOR);
        } catch (Exception e) {
            System.out.println(RB + "Failed || (1) testDeletingAlreadyDeletedRepository" + RESET_COLOR);
        }

        test.createRepository();
    }

    /*
     * File Structure as followed (ALL FILES STORED IN *testFilesForCommit*):
     * a/
     *  -aa/
     *  -a1.txt --> this
     *  - aa1/
     *      - a2.txt --> assignment
     * b/
     *  -bb/
     *      -bbb/
     *          -b1.txt --> sucks
     * c.txt --> boo
     */
    public static void testFileCreationForCommit(IlleGITimate test) throws IOException {
        test.clearRepository();
        String parentName = "testFileCreationForCommit/";
        String[] filePaths = {"a/a1.txt", "a/aa1/a2.txt", "b/bb/bbb/b1.txt", "c.txt"};

        for (String path : filePaths) {
            File f = new File(parentName + path);
            test.stageFile(f);
        }

        TreeBuilder tb = new TreeBuilder(new File("."), test.getIndex());

    }

}
