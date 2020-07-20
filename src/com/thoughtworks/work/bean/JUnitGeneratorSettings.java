package com.thoughtworks.work.bean;

import com.intellij.util.xmlb.annotations.Transient;
import com.thoughtworks.work.util.JUnitGeneratorUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This bean represents the de-serialized settings from the project and application configurations
 *
 * @author JOsborne
 * @since 2/28/13 8:31 AM
 */
public class JUnitGeneratorSettings {


    private String outputFilePattern;
    private boolean generateForOverloadedMethods;
    private String listOverloadedMethodsBy;
    private boolean combineGetterAndSetter;
    private Map<String, String> vmTemplates = new HashMap<String, String>();
    private String selectedTemplateKey;
    private boolean useProjectSettings;

    public JUnitGeneratorSettings() {
        loadDefaultState(this);
    }

    public boolean isCombineGetterAndSetter() {
        return combineGetterAndSetter;
    }

    public void setCombineGetterAndSetter(boolean combineGetterAndSetter) {
        this.combineGetterAndSetter = combineGetterAndSetter;
    }

    public boolean isGenerateForOverloadedMethods() {
        return generateForOverloadedMethods;
    }

    public void setGenerateForOverloadedMethods(boolean generateForOverloadedMethods) {
        this.generateForOverloadedMethods = generateForOverloadedMethods;
    }

    public String getListOverloadedMethodsBy() {
        return listOverloadedMethodsBy;
    }

    public void setListOverloadedMethodsBy(String listOverloadedMethodsBy) {
        this.listOverloadedMethodsBy = listOverloadedMethodsBy;
    }

    public String getOutputFilePattern() {
        return outputFilePattern;
    }

    public void setOutputFilePattern(String outputFilePattern) {
        this.outputFilePattern = outputFilePattern;
    }

    public String getSelectedTemplateKey() {
        return selectedTemplateKey;
    }

    public void setSelectedTemplateKey(String selectedTemplateKey) {
        this.selectedTemplateKey = selectedTemplateKey;
    }

    public boolean isUseProjectSettings() {
        return useProjectSettings;
    }

    public void setUseProjectSettings(boolean useProjectSettings) {
        this.useProjectSettings = useProjectSettings;
    }

    public Map<String, String> getVmTemplates() {
        return vmTemplates;
    }

    public void setVmTemplates(Map<String, String> vmTemplates) {
        this.vmTemplates = vmTemplates;
    }

    @Transient
    public String getTemplate(String key) {
        if (key != null) {
            return this.vmTemplates.get(key);
        }
        return null;
    }


    /**
     * Load the default state
     *
     * @param settings the settings to load into
     */
    public static JUnitGeneratorSettings loadDefaultState(JUnitGeneratorSettings settings) {
        settings.setOutputFilePattern(JUnitGeneratorUtil.getProperty("junit.generator.outputPath"));
        settings.setGenerateForOverloadedMethods(true);
        settings.setListOverloadedMethodsBy("paramName");
        settings.setCombineGetterAndSetter(false);
        final List<String> names = Arrays.asList(JUnitGeneratorUtil.getDelimitedProperty("junit.generator.vm.names", ","));
        final List<String> templates = JUnitGeneratorUtil.getPropertyList("junit.generator.vm");
        if (names.size() != templates.size()) {
            throw new IllegalArgumentException("template names and definitions must be equal");
        }
        final Map<String, String> map = new HashMap<String, String>(names.size());
        for (int i = 0; i < names.size(); i++) {
            map.put(names.get(i), templates.get(i));
        }
        settings.setVmTemplates(map);
        settings.setUseProjectSettings(false);
        settings.setSelectedTemplateKey(names.get(0));
        return settings;
    }

    @Override
    public String toString() {
        return "JUnitGeneratorSettings{" +
                "combineGetterAndSetter=" + combineGetterAndSetter +
                ", outputFilePattern='" + outputFilePattern + '\'' +
                ", generateForOverloadedMethods=" + generateForOverloadedMethods +
                ", listOverloadedMethodsBy='" + listOverloadedMethodsBy + '\'' +
                ", vmTemplates=" + vmTemplates +
                ", selectedTemplateKey='" + selectedTemplateKey + '\'' +
                ", useProjectSettings=" + useProjectSettings +
                '}';
    }
}
