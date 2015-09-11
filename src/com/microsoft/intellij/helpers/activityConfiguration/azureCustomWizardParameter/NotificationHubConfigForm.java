/**
 * Copyright (c) Microsoft Corporation
 * <p/>
 * All rights reserved.
 * <p/>
 * MIT License
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.intellij.helpers.activityConfiguration.azureCustomWizardParameter;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.ServiceCodeReferenceHelper;
import com.microsoft.tooling.msservices.helpers.StringHelper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class NotificationHubConfigForm extends DialogWrapper {

    private JPanel rootPanel;
    private JTextField textSenderID;
    private JTextField textConnectionString;
    private JTextField textHubName;
    private JTextPane summaryTextPane;
    private Module module;

    public NotificationHubConfigForm(Module module) {
        super(module.getProject(), true);

        this.module = module;

        setTitle("Notification Hub Connection Configuration");
        init();

        DocumentListener documentListener = new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                updateSummary();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                updateSummary();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                updateSummary();
            }
        };

        textHubName.getDocument().addDocumentListener(documentListener);
        textConnectionString.getDocument().addDocumentListener(documentListener);
        textHubName.getDocument().addDocumentListener(documentListener);

        summaryTextPane.setContentType("text/html");
    }

    private void updateSummary() {
        summaryTextPane.setText("<html> <head> </head> <body style=\"font-family: sans serif;\"> <p style=\"margin-top: 0\">"
                + "<b>Summary:</b></p> <ol> "
                + "<li>Will add <a href=\"https://go.microsoft.com/fwLink/?LinkID=280126&clcid=0x409\">"
                + "Notification Hub</a> library to project <b>"
                + module.getProject()
                + "</b>.</li> "
                + "<li>Will add a helper class extending NotificationsHandler, using Notification Hub <b>"
                + getHubName()
                + "</b>.</li> "
                + "<li>Will add a static method to handle notifications using NotificationHubsHelper.</li> "
                + "<li>Will configure the Azure Services Activity referencing the mentioned static methods.</li> "
                + "</ol> <p style=\"margin-top: 0\">After clicking Finish, it might take a few seconds to "
                + "complete set up.</p> </body> </html>");
    }


    @Nullable
    @Override
    protected ValidationInfo doValidate() {

        String senderId = this.textSenderID.getText().trim();
        String connectionString = this.textConnectionString.getText().trim();
        String hubName = this.textHubName.getText().trim();

        if (StringHelper.isNullOrWhiteSpace(senderId)) {
            return new ValidationInfo("The Sender ID must not be empty", textSenderID);
        } else if (!senderId.matches("^[0-9]+$")) {
            return new ValidationInfo("Invalid Sender ID. The Sender Id must contain only numbers.", textSenderID);
        }

        if (StringHelper.isNullOrWhiteSpace(connectionString)) {
            return new ValidationInfo("The Connection String must not be empty.", textConnectionString);
        }

        if (StringHelper.isNullOrWhiteSpace(hubName)) {
            return new ValidationInfo("The Notification Hub Name must not be empty", textHubName);
        } else {
            if (hubName.length() < 6 || hubName.length() > 50) {
                return new ValidationInfo("The Notification Hub Name must be between 6 and 50 characters long", textHubName);
            }

            if (!hubName.matches("^[A-Za-z][A-Za-z0-9-]+[A-Za-z0-9]$")) {
                return new ValidationInfo("Invalid Notification Hub name.  The Notification Hub name must start with a letter, contain only letters, numbers, and hyphens, and end with a letter or number", textHubName);
            }
        }

        return super.doValidate();
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();

        ProgressManager.getInstance().run(new Task.Backgroundable(module.getProject(), "Downloading Notification Hubs library", false) {
            @Override
            public void run(ProgressIndicator progressIndicator) {
                progressIndicator.setIndeterminate(true);

                ServiceCodeReferenceHelper scrh = new ServiceCodeReferenceHelper();
                try {
                    scrh.addNotificationHubsLibs(module);
                } catch (Throwable throwable) {
                    DefaultLoader.getUIHelper().showException("Error downloading Notification Hubs library", throwable);
                }
            }
        });
    }

    public String getSenderID() {
        return textSenderID.getText();
    }

    public String getConnectionString() {
        return textConnectionString.getText();
    }

    public String getHubName() {
        return textHubName.getText();
    }

    public void setSenderID(String senderID) {
        this.textSenderID.setText(senderID);
    }

    public void setConnectionString(String connectionString) {
        this.textConnectionString.setText(connectionString);
    }

    public void setHubName(String hubName) {
        this.textHubName.setText(hubName);
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return rootPanel;
    }

}