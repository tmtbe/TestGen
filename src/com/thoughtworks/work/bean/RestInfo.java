package com.thoughtworks.work.bean;

import java.util.ArrayList;
import java.util.List;

public class RestInfo {
    private String restMethod;
    private String path;
    private ConstructorParam body;
    private List<String> heads = new ArrayList<>();

    private List<String> pathSegments = new ArrayList<>();

    public List<String> getPathSegments() {
        return pathSegments;
    }

    public void setPathSegments(List<String> pathSegments) {
        this.pathSegments = pathSegments;
    }

    public String getRestMethod() {
        return restMethod;
    }

    public void setRestMethod(String restMethod) {
        this.restMethod = restMethod;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ConstructorParam getBody() {
        return body;
    }

    public void setBody(ConstructorParam body) {
        this.body = body;
    }

    public List<String> getHeads() {
        return heads;
    }

    public void setHeads(List<String> heads) {
        this.heads = heads;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    private List<String> params = new ArrayList<>();
}
