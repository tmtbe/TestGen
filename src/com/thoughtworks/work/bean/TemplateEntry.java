package com.thoughtworks.work.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * DataHolder class. Needs to be public since velocity is using it in the
 * template.
 *
 * @author Jon Osborn
 */
public class TemplateEntry {

    private final List<MethodComposite> methodList;
    private final List<MethodComposite> privateMethodList;
    private final List<String> fieldList;
    private final List<ConstructorParam> deepConstructorParamList;
    private final List<ConstructorParam> constructorParamList;

    public List<String> getImportList() {
        return importList;
    }

    public void setImportList(List<String> importList) {
        this.importList = importList;
    }

    private List<String> importList = new ArrayList<String>();

    public String getBaseControllerTest() {
        return baseControllerTest;
    }

    public void setBaseControllerTest(String baseControllerTest) {
        this.baseControllerTest = baseControllerTest;
    }

    public String getBaseServiceTest() {
        return baseServiceTest;
    }

    public void setBaseServiceTest(String baseServiceTest) {
        this.baseServiceTest = baseServiceTest;
    }

    public String getBaseRepositoryTest() {
        return baseRepositoryTest;
    }

    public void setBaseRepositoryTest(String baseRepositoryTest) {
        this.baseRepositoryTest = baseRepositoryTest;
    }

    private String baseControllerTest;
    private String baseServiceTest;
    private String baseRepositoryTest;
    private String className;
    private String packageName;

    public List<MethodComposite> getMethodList() {
        return methodList;
    }

    public List<MethodComposite> getPrivateMethodList() {
        return privateMethodList;
    }

    public List<String> getFieldList() {
        return fieldList;
    }

    public List<ConstructorParam> getDeepConstructorParamList() {
        return deepConstructorParamList;
    }

    public List<ConstructorParam> getConstructorParamList() {
        return constructorParamList;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public TemplateEntry(String className,
                         String packageName,
                         List<MethodComposite> methodList,
                         List<MethodComposite> privateMethodList,
                         List<String> fieldList,
                         List<ConstructorParam> constructorParamList,
                         List<ConstructorParam> deepConstructorParamList,
                         List<String> importList,
                         String baseControllerTest,
                         String baseServiceTest,
                         String baseRepositoryTest
    ) {
        this.className = className;
        this.packageName = packageName;
        this.methodList = methodList;
        this.privateMethodList = privateMethodList;
        this.fieldList = fieldList;
        this.deepConstructorParamList = deepConstructorParamList;
        this.constructorParamList = constructorParamList;
        this.importList = importList;
        this.baseControllerTest = baseControllerTest;
        this.baseServiceTest = baseServiceTest;
        this.baseRepositoryTest = baseRepositoryTest;
    }
}
