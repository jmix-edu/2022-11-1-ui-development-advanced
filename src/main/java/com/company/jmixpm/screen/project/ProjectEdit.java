package com.company.jmixpm.screen.project;

import com.company.jmixpm.entity.Project;
import com.company.jmixpm.entity.Task;
import io.jmix.core.DataManager;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.BaseAction;
import io.jmix.ui.component.TabSheet;
import io.jmix.ui.component.Table;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("Project.edit")
@UiDescriptor("project-edit.xml")
@EditedEntityContainer("projectDc")
public class ProjectEdit extends StandardEditor<Project> {
    @Autowired
    private CollectionLoader<Task> tasksDl;

    @Autowired
    private ScreenBuilders screenBuilders;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Notifications notifications;

    private Table<Task> tasksTable;

    @Subscribe("tabSheet")
    public void onTabSheetSelectedTabChange(TabSheet.SelectedTabChangeEvent event) {
        if ("tasksTab".equals(event.getSelectedTab().getName())) {
            initTable();
        }
    }

    private void initTable() {
        if (tasksTable != null) {
            // It means that we've already opened this tab and initialized table
            return;
        }

        tasksDl.setParameter("project", getEditedEntity());
        tasksDl.load();

        //noinspection unchecked
        tasksTable = ((Table<Task>) getWindow().getComponentNN("tasksTable"));
        ((BaseAction) tasksTable.getActionNN("create"))
                .addActionPerformedListener(this::onTasksTableCreate);
        ((BaseAction) tasksTable.getActionNN("edit"))
                .addActionPerformedListener(this::onTasksTableEdit);
    }

    //    @Subscribe("tasksTable.create")
    public void onTasksTableCreate(Action.ActionPerformedEvent event) {
        Task task = dataManager.create(Task.class);
        task.setProject(getEditedEntity());

        screenBuilders.editor(tasksTable)
                .newEntity(task)
                .withParentDataContext(getScreenData().getDataContext())
                .show();
    }

    //    @Subscribe("tasksTable.edit")
    public void onTasksTableEdit(Action.ActionPerformedEvent event) {
        Task selected = tasksTable.getSingleSelected();
        if (selected == null) {
            return;
        }

        screenBuilders.editor(tasksTable)
                .editEntity(selected)
                .withParentDataContext(getScreenData().getDataContext())
                .show();
    }

    @Subscribe(id = "tasksDc", target = Target.DATA_CONTAINER)
    public void onTasksDcCollectionChange(CollectionContainer.CollectionChangeEvent<Task> event) {
        notifications.create()
                .withCaption("[tasksDc] CollectionChangeEvent")
                .withDescription(event.getChangeType() + "")
                .withType(Notifications.NotificationType.TRAY)
                .show();
    }

    /*@Subscribe
    public void onInitEntity(InitEntityEvent<Project> event) {
        tasksTable.setEnabled(false);
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    public void onPostCommit(DataContext.PostCommitEvent event) {
        tasksTable.setEnabled(true);
    }*/
}