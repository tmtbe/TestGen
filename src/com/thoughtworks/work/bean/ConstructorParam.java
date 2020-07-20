package com.thoughtworks.work.bean;

import java.util.ArrayList;
import java.util.List;

public class ConstructorParam {
    public ConstructorParam(String className, String sortClassNameT, String paramName) {
        this.className = getSubText(className);
        this.paramName = paramName;
        this.shortClassNameT = sortClassNameT;
        this.shortClassName = getSubText(sortClassNameT);
        className = className.replaceAll("<",",");
        className = className.replaceAll(">",",");
        String[] split = className.trim().split(",");
        for (String s : split) {
            if(!importNames.contains(s)) importNames.add(s);
        }
    }
    private String getSubText(String text) {
        return text.split("<")[0];
    }
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    private String className;

    public List<String> getImportNames() {
        return importNames;
    }

    public void setImportNames(List<String> importNames) {
        this.importNames = importNames;
    }

    private List<String> importNames = new ArrayList();
    private String shortClassName;

    public String getShortClassNameT() {
        return shortClassNameT;
    }

    public void setShortClassNameT(String shortClassNameT) {
        this.shortClassNameT = shortClassNameT;
    }

    private String shortClassNameT;
    private String paramName;

    public Boolean getIfInterface() {
        return ifInterface;
    }

    public void setIfInterface(Boolean ifInterface) {
        this.ifInterface = ifInterface;
    }

    public ArrayList<ConstructorParam> getConstructorNeedParamList() {
        return constructorNeedParamList;
    }

    public void setConstructorNeedParamList(ArrayList<ConstructorParam> constructorNeedParamList) {
        this.constructorNeedParamList = constructorNeedParamList;
    }

    private Boolean ifInterface = false;
    private ArrayList<ConstructorParam> constructorNeedParamList = new ArrayList<>();

    public String getShortClassName() {
        return shortClassName;
    }

    public void setShortClassName(String shortClassName) {
        this.shortClassName = shortClassName;
    }
}
