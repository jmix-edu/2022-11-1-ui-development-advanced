package com.company.jmixpm.screen.user;

import com.company.jmixpm.entity.User;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.app.inputdialog.DialogActions;
import io.jmix.ui.app.inputdialog.DialogOutcome;
import io.jmix.ui.app.inputdialog.InputParameter;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.TextArea;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.TaskLifeCycle;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@UiController("User.browse")
@UiDescriptor("user-browse.xml")
@LookupComponent("usersTable")
@Route("users")
public class UserBrowse extends StandardLookup<User> {
    @Autowired
    private GroupTable<User> usersTable;

    @Autowired
    private CollectionContainer<User> usersDc;

    @Autowired
    private Dialogs dialogs;
    @Autowired
    private MessageBundle messageBundle;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Notifications notifications;

    @Subscribe("usersTable.sendEmail")
    public void onUsersTableSendEmail(Action.ActionPerformedEvent event) {
        dialogs.createInputDialog(this)
                .withCaption(messageBundle.getMessage("sendEmailDialog.caption"))
                .withParameters(
                        InputParameter.stringParameter("title")
                                .withCaption(messageBundle.getMessage("titleParam.caption"))
                                .withRequired(true),
                        InputParameter.stringParameter("body")
                                .withField(() -> {
                                    TextArea<String> textArea = uiComponents.create(TextArea.TYPE_STRING);
                                    textArea.setCaption(messageBundle.getMessage("bodyParam.caption"));
                                    textArea.setRequired(true);
                                    textArea.setWidthFull();

                                    return textArea;
                                })
                )
                .withActions(DialogActions.OK_CANCEL)
                .withCloseListener(closeEvent -> {
                    if (closeEvent.closedWith(DialogOutcome.OK)) {
                        String title = closeEvent.getValue("title");
                        String body = closeEvent.getValue("body");

                        Set<User> selected = usersTable.getSelected();
                        Collection<User> users = selected.isEmpty()
                                ? usersDc.getItems()
                                : selected;

                        doSendEmail(title, body, users);
                    }
                })
                .show();
    }

    private void doSendEmail(String title, String body, Collection<User> users) {
        EmailTask emailTask = new EmailTask(title, body, users);
        dialogs.createBackgroundWorkDialog(this, emailTask)
                .withCaption("Sending remainder emails")
                .withMessage("please wait while emails are being sent")
                .withTotal(users.size())
                .withShowProgressInPercentage(true)
                .withCancelAllowed(true)
                .show();
    }

    private class EmailTask extends BackgroundTask<Integer, Void> {
        private final String title;
        private final String body;
        private final Collection<User> users;

        public EmailTask(String title, String body, Collection<User> users) {
            super(10, TimeUnit.MINUTES, UserBrowse.this);

            this.title = title;
            this.body = body;
            this.users = users;
        }

        @Override
        public Void run(TaskLifeCycle<Integer> taskLifeCycle) throws Exception {
            int i = 0;
            for (User user : users) {
                if (taskLifeCycle.isCancelled()) {
                    break;
                }

                TimeUnit.SECONDS.sleep(2);

                i++;
                taskLifeCycle.publish(i);
            }

            return null;
        }

        @Override
        public void done(Void result) {
            notifications.create()
                    .withCaption("Email has been sent")
                    .withType(Notifications.NotificationType.TRAY)
                    .show();
        }

        @Override
        public void canceled() {
            notifications.create()
                    .withCaption("Canceled")
                    .withType(Notifications.NotificationType.TRAY)
                    .show();
        }

        @Override
        public void progress(List<Integer> changes) {
            notifications.create()
                    .withCaption("Progress")
                    .show();
        }
    }
}