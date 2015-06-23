package org.eclipse.jgit.lib;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.junit.LocalDiskRepositoryTestCase;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;

public class DirectoryFlagsTest extends LocalDiskRepositoryTestCase {
    @Test
    public void testAddFileWithNoGitLinks() throws Exception {
        DirectoryFlags flags = new DirectoryFlags();
        flags.setNoGitLinks(true);

        File gitdir = createUniqueTestGitDir(false);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.setGitDir(gitdir);
        builder.setDirectoryFlags(flags);
        Repository db = builder.build();
        db.create();
        // TODO: add cleanup logic?  This is based on `createRepository` in
        //  the parent class
        //toClose.add(db);


        File nestedRepoPath = new File(db.getWorkTree(), "sub/nested");

        FileRepositoryBuilder nestedBuilder = new FileRepositoryBuilder();
        nestedBuilder.setWorkTree(nestedRepoPath);
        nestedBuilder.build().create();


        File file = new File(nestedRepoPath, "a.txt");
        FileUtils.createNewFile(file);
        PrintWriter writer = new PrintWriter(file);
        writer.print("content");
        writer.close();

        System.out.println("Calling add command");
        Git git = new Git(db);
        git.add().addFilepattern("sub/nested/a.txt").call();
        System.out.println("Back from calling add command");

        assertEquals(
                "[sub/nested/a.txt, mode:100644, content:content]",
                indexState(db, CONTENT));
    }

    // TODO: add a test for calling status after (and before?) doing
    //  an add on a nested repo
}
