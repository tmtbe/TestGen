package com.thoughtworks.work;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiJavaFile;
import com.thoughtworks.work.diff.DiffFileAction;
import com.thoughtworks.work.util.JUnitGeneratorUtil;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;


/**
 * Responsible for writing test case out to the file and bringing
 * new editor window up.
 * Must implement runnable since we are using Application.runWriteAction in
 * JUnitGeneratorAction to refresh the content of the VirtualFileSystem.
 *
 * @author Alex Nazimok (SCI)
 * @author Jon Osborn
 * @since <pre>Sep 1, 2003</pre>
 */
public class JUnitGeneratorFileCreator implements Runnable {

    private static final Logger log = JUnitGeneratorUtil.getLogger(JUnitGeneratorFileCreator.class);

    private String outputFile;
    private StringWriter writer;
    private DataContext ctx;
    private PsiJavaFile file;
    private JUnitGeneratorContext genCtx;

    /**
     * Default constructor
     *
     * @param outputFile output file name
     * @param writer     holds the content of the file
     * @param genCtx     generator context
     */
    public JUnitGeneratorFileCreator(String outputFile, StringWriter writer, JUnitGeneratorContext genCtx) {
        this.outputFile = outputFile;
        this.writer = writer;
        this.ctx = genCtx.getDataContext();
        this.file = genCtx.getFile();
        this.genCtx = genCtx;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p/>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    public void run() {
        try {
            final VirtualFileManager manager = VirtualFileManager.getInstance();
            VirtualFile virtualFile = manager.refreshAndFindFileByUrl(VfsUtil.pathToUrl(this.outputFile));
            int overwriteInd = JOptionPane.NO_OPTION;

            if (virtualFile != null && virtualFile.exists()) {
                log.debug("file exists so prompt the user for merge request");
                overwriteInd =
                        JOptionPane.showOptionDialog(null,
                                JUnitGeneratorUtil.getProperty("junit.generator.file.exists"),
                                "View the difference?",
                                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (JOptionPane.NO_OPTION == overwriteInd) {
                    //user chose to overwrite the file, so replace the virtual file contents
                    virtualFile.setBinaryContent(writer.toString().getBytes());
                }
            } else {
                //we need to create the file
                final File newFile = new File(this.outputFile);
                //create directories if required
                if (!newFile.getParentFile().exists()) {
                    if (newFile.getParentFile().mkdirs()) {
                        log.debug("created directories");
                    }
                }
                //user chose to overwrite
                FileWriter w = null;
                try {
                    w = new FileWriter(newFile);
                    w.write(this.writer.toString());
                } catch (IOException e) {
                    log.warn("Exception while logging the error", e);
                } finally {
                    if (w != null) {
                        try {
                            w.close();
                        } catch (IOException e) {
                            //ignore
                        }
                    }
                }
                log.info("Created File");
                virtualFile = manager.refreshAndFindFileByUrl(VfsUtil.pathToUrl(this.outputFile));
            }

            final VirtualFile fileToOpen = virtualFile;
            if (JOptionPane.NO_OPTION == overwriteInd && virtualFile != null) {
                //now open the file
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        FileEditorManager.getInstance(genCtx.getProject()).openFile(fileToOpen, true, true);
                    }
                });
            } else if (JOptionPane.YES_OPTION == overwriteInd) {
                //user wants to merge, so create the files and we get them together
                new DiffFileAction().showDiff(writer.toString(), fileToOpen, this.genCtx);
            } else {
                log.warn("Couldn't create the virtual file for some reason");
            }
        } catch (Exception e) {
            log.error("Exception while attempting to create the JUnit file", e);
        }
    }
}
