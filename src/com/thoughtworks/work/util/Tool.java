package com.thoughtworks.work.util;

import com.thoughtworks.work.bean.ConstructorParam;

import java.util.List;

/**
 * A simple date formatting tool for more flexibility in the template. This class could be expanded
 * if users have more sophisticated requirements
 *
 * @author Jon Osborn
 * @since 1/6/12 10:48 AM
 */
public class Tool {
    public String cap(String s) {
        return s.valueOf(s.charAt(0)).toUpperCase() + s.substring(1);
    }

    public String sap(String s) {
        return s.valueOf(s.charAt(0)).toLowerCase() + s.substring(1);
    }

    public String buildParam(List<ConstructorParam> constructorParams) {
        String s = "";
        if(constructorParams.size()==0) return "";
        for (ConstructorParam constructorParam : constructorParams) {
            s += sap(constructorParam.getShortClassName()) + ",";
        }
        return s.substring(0, s.length() - 1);
    }

    public String buildHttpParam(List<String> params) {
        String result = "?";
        if (params.size() == 0) return "";
        for (String param : params) {
            param = param.substring(1,param.length()-1);
            result = result + param + "={" + param + "}&";
        }
        return result.substring(0, result.length() - 1);
    }
}
