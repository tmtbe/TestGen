package com.thoughtworks.work;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ProjectComponent;
import org.jetbrains.annotations.NotNull;


/**
 * ApplicationComponent implementation
 *
 * @author Alex Nazimok (SCI)
 * @author Jon Osborn
 * @since <pre>Aug 29, 2003</pre>
 */
public class JUnitGenerator implements ApplicationComponent, ProjectComponent {
    /**
     * Method is called after plugin is already created and configured. Plugin can start to communicate with
     * other plugins only in this method.
     */
    public void initComponent() {
    }

    /**
     * This method is called on plugin disposal.
     */
    public void disposeComponent() {
    }

    /**
     * Returns the name of component
     *
     * @return String representing component name. Use plugin_name.component_name notation.
     */
    @NotNull
    public String getComponentName() {
        return "JUnitGenerator.JUnitGenerator";
    }

    @Override
    public void projectOpened() {
    }

    @Override
    public void projectClosed() {
    }

}
