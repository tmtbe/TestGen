package com.thoughtworks.work.bean;

import com.thoughtworks.work.util.Tool;

import java.util.ArrayList;
import java.util.List;

public class CallNode {
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<String> getMethodParamNameList() {
        return methodParamNameList;
    }

    public void setMethodParamNameList(List<String> methodParamNameList) {
        this.methodParamNameList = methodParamNameList;
    }

    public List<String> getMethodParamClassList() {
        return methodParamClassList;
    }

    public void setMethodParamClassList(List<String> methodParamClassList) {
        this.methodParamClassList = methodParamClassList;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    private int index;
    private String className;

    public Boolean getInterface() {
        return isInterface;
    }

    public void setInterface(Boolean anInterface) {
        isInterface = anInterface;
    }

    private Boolean isInterface;
    private String methodName;
    private List<String> methodParamNameList = new ArrayList<>();
    private List<String> methodParamClassList = new ArrayList<>();
    private List<CallNode> children = new ArrayList<>();


    public void addChild(CallNode callNode) {
        children.add(callNode);
        callNode.setIndex(this.index + 1);
    }

    public String buildParams() {
        StringBuilder a = new StringBuilder("(");
        for (int i = 0; i < methodParamClassList.size(); i++) {
            a.append(methodParamClassList.get(i));
            a.append(" ");
            a.append(methodParamNameList.get(i));
            if (i != methodParamClassList.size() - 1) {
                a.append(", ");
            }
        }
        a.append(")");
        return a.toString();
    }

    public String buildMockParams() {
        StringBuilder a = new StringBuilder("(");
        for (int i = 0; i < methodParamClassList.size(); i++) {
            a.append("Mockito.any(" + methodParamClassList.get(i) + ".class)");
            if (i != methodParamClassList.size() - 1) {
                a.append(", ");
            }
        }
        a.append(")");
        return a.toString();
    }

    public String toString() {
        StringBuilder desc = new StringBuilder(this.getClassName() + "::" + this.getMethodName() + buildParams() + "\n");
        for (CallNode callNode : this.children) {
            desc.append("*  ");
            for (int i = 0; i <= index; i++) {
                desc.append("-");
            }
            desc.append(callNode.toString());
        }
        if (index == 0) {
            return "/**\n*  " + desc.toString() + "*/";
        } else {
            return desc.toString();
        }
    }

    public ArrayList<CallNode> getMock() {
        ArrayList<CallNode> calls = new ArrayList<>();
        mock(calls, this);
        return calls;
    }

    private void mock(ArrayList<CallNode> callNodes, CallNode callNode) {
        if (callNode.isInterface) {
            callNodes.add(callNode);
        } else {
            for (CallNode child : callNode.children) {
                mock(callNodes, child);
            }
        }
    }
}
