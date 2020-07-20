package com.thoughtworks.work.bean;

import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * A holder for the dissection of the methods
 *
 * @author Jon Osborn
 * @since 1/3/12 4:37 PM
 */
public class MethodComposite {

    private PsiMethod method;
    private String name;
    private String signature;
    private List<String> paramClasses;
    private List<String> paramNames;
    private List<String> reflectionCode;
    private MethodComposite base;
    private List<MethodComposite> overloadedMethods = new ArrayList<MethodComposite>();
    private RestInfo restInfo = new RestInfo();
    private List<String> restInfoHeads = new ArrayList<>();
    private List<String> restInfoParams = new ArrayList<>();

    public String getRestInfoMethod() {
        return restInfoMethod;
    }

    public void setRestInfoMethod(String restInfoMethod) {
        this.restInfoMethod = restInfoMethod;
    }

    public String getRestInfoPath() {
        return restInfoPath;
    }

    public void setRestInfoPath(String restInfoPath) {
        this.restInfoPath = restInfoPath;
    }

    private String restInfoMethod;
    private String restInfoPath;

    public ConstructorParam getRestInfoBody() {
        return restInfoBody;
    }

    public void setRestInfoBody(ConstructorParam restInfoBody) {
        this.restInfoBody = restInfoBody;
    }

    private ConstructorParam restInfoBody;
    public RestInfo getRestInfo() {
        return restInfo;
    }

    public PsiMethod getMethod() {
        return method;
    }

    public void setMethod(PsiMethod method) {
        this.method = method;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public List<String> getParamClasses() {
        return paramClasses;
    }

    public void setParamClasses(List<String> paramClasses) {
        this.paramClasses = paramClasses;
    }

    public List<String> getParamNames() {
        return paramNames;
    }

    public void setParamNames(List<String> paramNames) {
        this.paramNames = paramNames;
    }

    public List<String> getReflectionCode() {
        return reflectionCode;
    }

    public void setReflectionCode(List<String> reflectionCode) {
        this.reflectionCode = reflectionCode;
    }

    public MethodComposite getBase() {
        return base;
    }

    public void setBase(MethodComposite base) {
        this.base = base;
    }

    public List<MethodComposite> getOverloadedMethods() {
        return overloadedMethods;
    }

    public void setOverloadedMethods(List<MethodComposite> overloadedMethods) {
        this.overloadedMethods = overloadedMethods;
    }

    public void setRestInfo(RestInfo restInfo) {
        restInfo = restInfo;
        restInfoMethod = restInfo.getRestMethod();
        restInfoPath = restInfo.getPath();
        restInfoBody = restInfo.getBody();
        restInfoHeads = restInfo.getHeads();
        restInfoParams = restInfo.getParams();
    }
    @Override
    public String toString() {
        return "MethodComposite{" +
                "base=" + base +
                ", method=" + method +
                ", name='" + name + '\'' +
                ", signature='" + signature + '\'' +
                ", paramClasses=" + paramClasses +
                ", paramNames=" + paramNames +
                ", reflectionCode=" + reflectionCode +
                ", overloadedMethods=" + overloadedMethods +
                '}';
    }

    public List<String> getRestInfoHeads() {
        return restInfoHeads;
    }

    public void setRestInfoHeads(List<String> restInfoHeads) {
        this.restInfoHeads = restInfoHeads;
    }

    public List<String> getRestInfoParams() {
        return restInfoParams;
    }

    public void setRestInfoParams(List<String> restInfoParams) {
        this.restInfoParams = restInfoParams;
    }
}
