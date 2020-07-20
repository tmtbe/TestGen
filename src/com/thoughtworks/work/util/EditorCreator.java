package com.thoughtworks.work.util;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.ex.temp.TempFileSystem;

import java.io.IOException;
import java.util.UUID;

/**
 * Create a file on the fly with the file creator
 *
 * @author JOsborne
 * @since 3/4/13 2:42 PM
 */
public class EditorCreator implements Computable<VirtualFile> {

    private final Document document;

    public EditorCreator(Document document) {
        this.document = document;
    }

    @Override
    public VirtualFile compute() {
        if (document == null || document.getText() == null) {
            return null;
        }

        try {
            final TempFileSystem tempFileSystem = TempFileSystem.getInstance();

            VirtualFile rootFile = tempFileSystem.findFileByPath("/junitGenerator");
            if (rootFile == null || !rootFile.exists()) {
                rootFile = tempFileSystem.findFileByPath("/").createChildDirectory(this, "junitGenerator");
            }
            VirtualFile theFile = rootFile.createChildData(this, UUID.randomUUID().toString() + ".vm");
            theFile.setBinaryContent(this.document.getText().getBytes());
            theFile = tempFileSystem.refreshAndFindFileByPath(theFile.getPath());
            return theFile;
        } catch (IOException e) {
            Logger.getInstance(EditorCreator.class).warn(e);
        }
        return null;
    }
}
