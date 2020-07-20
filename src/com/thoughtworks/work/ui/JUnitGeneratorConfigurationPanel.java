package com.thoughtworks.work.ui;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPopupMenu;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFileFactory;
import com.intellij.ui.UserActivityListener;
import com.intellij.ui.UserActivityWatcher;
import com.thoughtworks.work.bean.JUnitGeneratorSettings;
import com.thoughtworks.work.util.JUnitGeneratorUtil;

import javax.swing.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author JOsborne
 * @since 1/4/12 2:44 PM
 */
public class JUnitGeneratorConfigurationPanel {

    private static final int SETTINGS_INDEX_APP = 0;
    private static final int SETTINGS_INDEX_PRJ = 1;

    private boolean modified;
    private JPanel panel;
    private JTabbedPane tabbedPane1;
    private JCheckBox combineGetterAndSetterCheckBox;
    private JCheckBox generateForOverloadedMethodsCheckBox;
    private JComboBox methodGenerationComboBox;
    private JTextField outputTextBox;
    private JComboBox settingsTypeComboBox;
    private JLabel settingsTypeLabel;
    private JButton copyGobalSettingsToButton;
    private JComboBox selectedTemplateComboBox;
    private JLabel loadDefaultsLabel;
    private final Map<String, Editor> velocityEditorMap = new HashMap<String, Editor>();
    private final Project project;
    private JUnitGeneratorSettings settings;

    public JUnitGeneratorConfigurationPanel(JUnitGeneratorSettings settings, Project project) {
        this.project = project;
        this.settings = settings;
        updateSettingsVisibility();

        //only show these if the project is included as we are in 'project' mode instead of app mode
        this.settingsTypeComboBox.setVisible(this.project != null);
        this.settingsTypeLabel.setVisible(this.project != null);
        //if we use global settings, then hide the project settings
        this.settingsTypeComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                updateSettingsVisibility();
            }
        });
        setupSelectedTemplate(settings.getVmTemplates(), settings.getSelectedTemplateKey());
        this.copyGobalSettingsToButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("copy".equals(e.getActionCommand())) {
                    copyApplicationSettingsToProject();
                }
            }
        });
        //setup an activity watcher
        UserActivityWatcher watcher = new UserActivityWatcher();
        watcher.addUserActivityListener(new UserActivityListener() {
            public void stateChanged() {
                modified = true;
            }
        });
        watcher.register(panel);
        //capture the click event on the label
        this.loadDefaultsLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                reset(new JUnitGeneratorSettings());
            }
        });
        createEditors(settings);
    }

    public JComponent getPanel() {
        return panel;
    }

    public void apply() {
        if (this.settings == null) {
            this.settings = new JUnitGeneratorSettings();
        }
        this.settings.setUseProjectSettings(this.settingsTypeComboBox.getSelectedIndex() > 0);
        this.settings.setOutputFilePattern(this.outputTextBox.getText());
        this.settings.setSelectedTemplateKey((String) this.selectedTemplateComboBox.getSelectedItem());
        this.settings.setCombineGetterAndSetter(this.combineGetterAndSetterCheckBox.isSelected());
        this.settings.setListOverloadedMethodsBy((String) this.methodGenerationComboBox.getSelectedItem());
        this.settings.setGenerateForOverloadedMethods(this.generateForOverloadedMethodsCheckBox.isSelected());
        this.settings.getVmTemplates().clear();
        this.settings.getVmTemplates().putAll(getVmTemplates());
        this.modified = false;
        this.tabbedPane1.setVisible(this.project == null || this.settings.isUseProjectSettings());
    }

    /**
     * Reset the UI back to the default and commit the change
     */
    public void reset() {
        reset(this.settings);
        this.modified = false;
    }

    /**
     * Call this method with new settings to apply that we don't want to commit
     *
     * @param uiSettings the settings to apply
     */
    public void reset(JUnitGeneratorSettings uiSettings) {
        if (uiSettings == null) {
            uiSettings = new JUnitGeneratorSettings();
        }
        this.settingsTypeComboBox.setSelectedIndex(
                this.project != null && uiSettings.isUseProjectSettings() ? SETTINGS_INDEX_PRJ : SETTINGS_INDEX_APP);
        this.outputTextBox.setText(uiSettings.getOutputFilePattern());
        this.selectedTemplateComboBox.setSelectedItem(uiSettings.getSelectedTemplateKey());
        this.combineGetterAndSetterCheckBox.setSelected(uiSettings.isCombineGetterAndSetter());
        this.generateForOverloadedMethodsCheckBox.setSelected(uiSettings.isGenerateForOverloadedMethods());
        this.methodGenerationComboBox.setSelectedItem(uiSettings.getListOverloadedMethodsBy());
        updateTemplateEditors(uiSettings.getVmTemplates());
    }

    /**
     * Return a map of all of the templates in their current state
     *
     * @return the map of all of th templates
     */
    private Map<String, String> getVmTemplates() {
        final Map<String, String> contents = new HashMap<String, String>();
        //find all of our editors and read them back
        for (Map.Entry<String, Editor> editorEntry : this.velocityEditorMap.entrySet()) {
            contents.put(editorEntry.getKey(), editorEntry.getValue().getDocument().getText());
        }
        return contents;
    }

    private Editor findEditorForTab(String tabTitle) {
        return this.velocityEditorMap.get(tabTitle);
    }

    private void createEditors(JUnitGeneratorSettings settings) {
        EditorFactory factory = EditorFactory.getInstance();
        //create the editors
        for (Map.Entry<String, String> entry : settings.getVmTemplates().entrySet()) {
            final Document velocityTemplate = factory.createDocument(entry.getValue() != null ? entry.getValue() : "");
            try {
                final Editor editor =
                        factory.createEditor(velocityTemplate, this.project, FileTypeManager.getInstance().getFileTypeByExtension("vm"), false);
                editor.getContentComponent().addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(KeyEvent e) {
                        modified = true;
                    }
                });
                editor.getContentComponent().addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            // Single right click unless configured differently
                            ActionManager actionManager = ActionManager.getInstance();
                            DefaultActionGroup defaultActionGroup = (DefaultActionGroup) actionManager.getAction("org.intellij.plugins.junitgen.action.JUnitGeneratorEditorMenu");
                            ActionPopupMenu menu = actionManager.createActionPopupMenu("junitgenerator.editor.popup", defaultActionGroup);
                            menu.getComponent().show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                });
                this.velocityEditorMap.put(entry.getKey(), editor);
                this.tabbedPane1.addTab(entry.getKey(), editor.getComponent());
            } catch (Exception e) {
                Logger.getInstance(JUnitGeneratorConfigurationPanel.class).warn(e);
            }
        }

    }

    private void updateTemplateEditors(Map<String, String> templates) {
        //just update them
        for (Map.Entry<String, String> entry : templates.entrySet()) {
            //grab the components and replace the content since the editor was already built
            final Editor editor = findEditorForTab(entry.getKey());
            if (editor != null) {
                final String text = entry.getValue();
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ApplicationManager.getApplication()
                                .runWriteAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        editor.getDocument().setText(text);
                                    }
                                });
                    }
                });
            }
        }
    }

    public static PsiFileFactory getPsiInstance() {
        return ServiceManager.getService(PsiFileFactory.class);
    }

    private void setupSelectedTemplate(Map<String, String> templates, String selectedTemplateKey) {
        //also update the selected template combo box
        this.selectedTemplateComboBox.removeAllItems();
        for (String title : templates.keySet()) {
            this.selectedTemplateComboBox.addItem(title);
            if (title.equals(selectedTemplateKey))
                this.selectedTemplateComboBox.setSelectedItem(title);
        }
    }


    private void updateSettingsVisibility() {
        this.tabbedPane1.setVisible(this.project == null || this.settingsTypeComboBox.getSelectedIndex() > 0);
        this.copyGobalSettingsToButton.setVisible(this.project != null);
    }

    /**
     * copy the global settings in but maintain the 'project settings' type
     */
    private void copyApplicationSettingsToProject() {
        JUnitGeneratorSettings appSettings = JUnitGeneratorUtil.getInstance();
        appSettings.setUseProjectSettings(this.settings.isUseProjectSettings());
        reset(appSettings);
        //also mark us as modified
        this.modified = true;
    }

    /**
     * Return true if we are modified
     *
     * @return true if modified
     */
    public boolean isModified() {
        return this.modified;
    }

    /**
     * Clean out the editors and other components that need to be released
     */
    public void release() {
        EditorFactory factory = EditorFactory.getInstance();
        for (Editor editor : this.velocityEditorMap.values()) {
            this.tabbedPane1.remove(editor.getComponent());
            factory.releaseEditor(editor);
        }
        this.velocityEditorMap.clear();
    }
}
