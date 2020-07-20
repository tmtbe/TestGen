/*
 * Copyright (c) United Parcel Service of America, Inc.
 * All Rights Reserved.
 *                                                                                    
 * The use, disclosure, reproduction, modification, transfer, or transmittal          
 * of this work for any purpose in any form or by any means without the               
 * written permission of United Parcel Service is strictly prohibited.                
 * Confidential, Unpublished Property of United Parcel Service.                       
 * Use and Distribution Limited Solely to Authorized Personnel.                       
 */
package com.thoughtworks.work.ui;

import com.intellij.openapi.components.*;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.thoughtworks.work.bean.JUnitGeneratorSettings;
import com.thoughtworks.work.util.JUnitGeneratorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * JUnitGenerator configuration UI.
 * Could be accessed via File->Settings->(IDE Settings) JUnit Generator.
 * <p>This class serves a few tasks. It acts as the entry point for both the configuration
 * and the persistent state for both project and application level configurations.</p>
 *
 * @author Alex Nazimok (SCI)
 * @author Jon Osborn
 * @since <pre>Sep 7, 2004</pre>
 */
public abstract class JUnitGeneratorConfigurable implements SearchableConfigurable,
        PersistentStateComponent<JUnitGeneratorSettings> {

    private JUnitGeneratorSettings settings;
    private JUnitGeneratorConfigurationPanel configuration;
    private final Project project;

    public JUnitGeneratorConfigurable(@NotNull JUnitGeneratorSettings settings, @Nullable Project project) {
        this.settings = settings;
        this.project = project;
    }

    /**
     * the global configuration instance
     */
    @State(
            name = "JUnitGeneratorAppSettings",
            storages = {
                    @Storage("工序-app-settings.xml")})
    public static class AppSettings extends JUnitGeneratorConfigurable {
        public AppSettings() {
            super(new JUnitGeneratorSettings(), null);
        }

        @NotNull
        @Override
        public String getId() {
            return "plugins.junitgenerator.application";
        }
    }

    public static class AppConfigurable extends JUnitGeneratorConfigurable {
        public AppConfigurable() {
            super(JUnitGeneratorUtil.getInstance(), null);
        }

        @NotNull
        @Override
        public String getId() {
            return "plugins.junitgenerator.application";
        }
    }

    /**
     * Ask for the project specific instance, regardless of the configuration
     */
    @State(
            name = "JUnitGeneratorProjectSettings",
            storages = {
                    @Storage("工序-prj-settings.xml")})
    public static class PrjSettings extends JUnitGeneratorConfigurable {
        public PrjSettings(Project project) {
            super(new JUnitGeneratorSettings(), project);
        }

        @NotNull
        @Override
        public String getId() {
            return "plugins.junitgenerator.project";
        }
    }

    public static class PrjConfigurable extends JUnitGeneratorConfigurable {
        public PrjConfigurable(Project project) {
            super(JUnitGeneratorUtil.getInstance(project), project);
        }

        @NotNull
        @Override
        public String getId() {
            return "plugins.junitgenerator.project";
        }
    }

    public String getDisplayName() {
        return "工序测试生成";
    }

    public Icon getIcon() {
        return JUnitGeneratorUtil.ICON;
    }

    @NotNull
    public String getHelpTopic() {
        return "help.junitgen.configuration";
    }


    public JComponent createComponent() {
        return getConfigurationPanel().getPanel();
    }

    private JUnitGeneratorConfigurationPanel getConfigurationPanel() {
        if (this.configuration == null) {
            this.configuration = new JUnitGeneratorConfigurationPanel(this.settings, this.project);
        }
        return this.configuration;
    }

    /**
     * Compare the data to see if we are modified
     * we are modified if we are the project settings instance and are using project settings
     * OR we are the global instance and have a delta with the form
     *
     * @return true if the settings should be 'applied'
     */
    public boolean isModified() {
        return getConfigurationPanel().isModified();
    }

    /**
     * Apply the settings. Since this class holds the settings, we are setting them on ourself
     *
     * @throws ConfigurationException
     */
    public void apply() throws ConfigurationException {
        getConfigurationPanel().apply();
    }

    /**
     * Need to release stuff that the UI created
     */
    public void disposeUIResources() {
        getConfigurationPanel().release();
    }

    /**
     * Undo the settings by copying from internal state out to the panel
     */
    public void reset() {
        getConfigurationPanel().reset();
    }

    @Override
    public Runnable enableSearch(String s) {
        return null;
    }

    @Override
    public JUnitGeneratorSettings getState() {
        return this.settings;
    }

    @Override
    public void loadState(JUnitGeneratorSettings jUnitGeneratorSettings) {
        if (this.getState() != null) {
            XmlSerializerUtil.copyBean(jUnitGeneratorSettings, this.getState());
        }
    }
}
