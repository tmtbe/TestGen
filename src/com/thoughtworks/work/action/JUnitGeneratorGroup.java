package com.thoughtworks.work.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.thoughtworks.work.util.JUnitGeneratorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This group allows us to calculate the templates we have available so the user can select
 * an action corresponding to a particular template
 *
 * @author Jon Osborn
 * @since <pre>1/8/12 1:02 PM</pre>
 */
public class JUnitGeneratorGroup extends ActionGroup implements DumbAware {

    private static final Logger log = JUnitGeneratorUtil.getLogger(JUnitGeneratorGroup.class);

    @Override
    public void update(AnActionEvent e) {
        super.update(e);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * use the settings to determine what groups we have, and which one is the 'default' group. Once we
     * have the list, fire up the action handlers
     *
     * @param anActionEvent the event
     * @return the list of children
     */
    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
        if (anActionEvent == null) {
            return AnAction.EMPTY_ARRAY;
        }
        Project project = PlatformDataKeys.PROJECT.getData(anActionEvent.getDataContext());
        if (project == null) {
            return AnAction.EMPTY_ARRAY;
        }
        final List<AnAction> children = new ArrayList<AnAction>();
        if (log.isDebugEnabled()) {
            log.debug("adding the menu items");
        }

        //DefaultActionGroup subgroup = new DefaultActionGroup();
        //subgroup.getTemplatePresentation().setText("Templates", false);
        //subgroup.setPopup(true);
        for (String templateKey : JUnitGeneratorUtil.getInstance(anActionEvent.getProject()).getVmTemplates().keySet()) {
            final AnAction action = getOrCreateAction(templateKey);
            //subgroup.add(action);
            children.add(action);
        }
        Collections.sort(children, (a, b) -> {
            JUnitGeneratorAction aa = (JUnitGeneratorAction) a;
            JUnitGeneratorAction bb = (JUnitGeneratorAction) b;
            if (aa == null) return 1;
            if (bb == null) return -1;
            return aa.getName().compareTo(bb.getName());
        });
        return children.toArray(new AnAction[children.size()]);
    }

    AnAction getOrCreateAction(String name) {
        final String actionId = "JUnitGenerator.Menu.Action." + name;
        AnAction action = ActionManager.getInstance().getAction(actionId);
        if (action == null) {
            action = new JUnitGeneratorAction(name);
            ActionManager.getInstance().registerAction(actionId, action);
        }
        return action;
    }
}
