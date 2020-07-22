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

    public String getBaseFakeTest() {
        return baseFakeTest;
    }

    public void setBaseFakeTest(String baseFakeTest) {
        this.baseFakeTest = baseFakeTest;
    }

    private String baseApiTest;
    private String baseBusinessTest;
    private String baseFakeTest;
    private String baseClientTest;
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
                         String baseApiTest,
                         String baseBusinessTest,
                         String baseFakeTest,
                         String baseClientTest
    ) {
        this.className = className;
        this.packageName = packageName;
        this.methodList = methodList;
        this.privateMethodList = privateMethodList;
        this.fieldList = fieldList;
        this.deepConstructorParamList = deepConstructorParamList;
        this.constructorParamList = constructorParamList;
        this.importList = importList;
        this.baseApiTest = baseApiTest;
        this.baseBusinessTest = baseBusinessTest;
        this.baseFakeTest = baseFakeTest;
        this.baseClientTest = baseClientTest;
    }

    public String getBaseClientTest() {
        return baseClientTest;
    }

    public void setBaseClientTest(String baseClientTest) {
        this.baseClientTest = baseClientTest;
    }
}
