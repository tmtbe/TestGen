package com.thoughtworks.work.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * DataHolder class. Needs to be public since velocity is using it in the
 * template.
 *
 * @author Jon Osborn
 */
public class TemplateEntry {

    private final List<MethodComposite> methodList;
    private final List<String> fieldList;
    private final List<ConstructorParam> deepConstructorParamList;
    private final List<ConstructorParam> constructorParamList;
    private String baseApiTest;
    private String baseBusinessTest;
    private String baseRepositoryTest;
    private String baseClientTest;
    private String className;
    private String packageName;

    public TemplateEntry(String className,
                         String packageName,
                         List<MethodComposite> methodList,
                         List<String> fieldList,
                         List<ConstructorParam> constructorParamList,
                         List<ConstructorParam> deepConstructorParamList,
                         List<String> importList,
                         String baseApiTest,
                         String baseBusinessTest,
                         String baseRepositoryTest,
                         String baseClientTest
    ) {
        this.className = className;
        this.packageName = packageName;
        this.methodList = methodList;
        this.fieldList = fieldList;
        this.deepConstructorParamList = deepConstructorParamList;
        this.constructorParamList = constructorParamList;
        this.importList = importList;
        this.baseApiTest = baseApiTest;
        this.baseBusinessTest = baseBusinessTest;
        this.baseRepositoryTest = baseRepositoryTest;
        this.baseClientTest = baseClientTest;
    }

    public List<MethodComposite> getMethodList() {
        return methodList;
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

    public List<String> getImportList() {
        return importList;
    }

    public void setImportList(List<String> importList) {
        this.importList = importList;
    }

    private List<String> importList = new ArrayList<String>();

    public String getBaseApiTest() {
        return baseApiTest;
    }

    public void setBaseApiTest(String baseApiTest) {
        this.baseApiTest = baseApiTest;
    }

    public String getBaseBusinessTest() {
        return baseBusinessTest;
    }

    public void setBaseBusinessTest(String baseBusinessTest) {
        this.baseBusinessTest = baseBusinessTest;
    }

    public String getBaseRepositoryTest() {
        return baseRepositoryTest;
    }

    public void setBaseRepositoryTest(String baseRepositoryTest) {
        this.baseRepositoryTest = baseRepositoryTest;
    }

    public String getBaseClientTest() {
        return baseClientTest;
    }

    public void setBaseClientTest(String baseClientTest) {
        this.baseClientTest = baseClientTest;
    }
}
