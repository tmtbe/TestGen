package com.thoughtworks.work.bean;

import java.util.ArrayList;
import java.util.List;

public class RestInfo {
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

    private String restMethod;
    private String path;

    public ConstructorParam getBody() {
        return body;
    }

    public void setBody(ConstructorParam body) {
        this.body = body;
    }

    private ConstructorParam body;
    private List<String> heads = new ArrayList<>();

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
