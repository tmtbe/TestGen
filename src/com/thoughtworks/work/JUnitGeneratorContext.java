package com.thoughtworks.work;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;


/**
 * Data holder/distributor object.
 *
 * @author Alex Nazimok (SCI)
 * @since <pre>Sep 3, 2003</pre>
 */
public class JUnitGeneratorContext {

    private final DataContext dataContext;
    private final PsiJavaFile file;
    private final PsiClass psiClass;

    public JUnitGeneratorContext(DataContext ctx, PsiJavaFile file, PsiClass psiClass) {
        this.dataContext = ctx;
        this.file = file;
        this.psiClass = psiClass;
    }

    public DataContext getDataContext() {
        return dataContext;
    }

    public PsiJavaFile getFile() {
        return file;
    }

    public PsiClass getPsiClass() {
        return psiClass;
    }

    public String getPackageName() {
        return this.file.getPackageName();
    }

    public String getClassName(boolean qualified) {
        if (!qualified) {
            return this.psiClass.getName();
        } else {
            return this.psiClass.getQualifiedName();
        }
    }

    /**
     * Return the project for the base file
     *
     * @return the project
     */
    public Project getProject() {
        return DataKeys.PROJECT.getData(this.dataContext);
    }
}
