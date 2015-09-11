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

package com.microsoft.intellij.helpers.activityConfiguration.office365CustomWizardParameter;

import com.google.gson.Gson;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.microsoft.directoryservices.Application;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Office365ParameterPane extends JPanel {
    private JCheckBox outlookServicesCheckBox;
    private JPanel mainPanel;
    private JCheckBox sharepointListsCheckBox;
    private JCheckBox fileServicesCheckBox;
    private JCheckBox oneNoteCheckBox;
    private JButton configureOneNoteButton;
    private JButton configureOffice365Button;
    private PlainDocument document;
    private Application selectedApplication;
    private String selectedClientID;

    public Office365ParameterPane() {
        super(new BorderLayout());

        add(mainPanel, BorderLayout.CENTER);

        document = new PlainDocument();


        oneNoteCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                configureOneNoteButton.setEnabled(oneNoteCheckBox.isSelected());

                updateDocument();
            }


        });

        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                configureOffice365Button.setEnabled(
                        outlookServicesCheckBox.isSelected()
                                || sharepointListsCheckBox.isSelected()
                                || fileServicesCheckBox.isSelected()
                );

                updateDocument();
            }
        };
        outlookServicesCheckBox.addActionListener(actionListener);
        sharepointListsCheckBox.addActionListener(actionListener);
        fileServicesCheckBox.addActionListener(actionListener);


        configureOffice365Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                DataContext dataContext = DataManager.getInstance().getDataContext(mainPanel);
                final Project project = DataKeys.PROJECT.getData(dataContext);

                Office365ConfigForm form = new Office365ConfigForm(project,
                        sharepointListsCheckBox.isSelected(),
                        fileServicesCheckBox.isSelected(),
                        outlookServicesCheckBox.isSelected());

                form.show();

                if (form.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                    selectedApplication = form.getApplication();
                }

                updateDocument();
            }
        });

        configureOneNoteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                DataContext dataContext = DataManager.getInstance().getDataContext(mainPanel);
                final Project project = DataKeys.PROJECT.getData(dataContext);

                OneNoteConfigForm form = new OneNoteConfigForm(project);
                form.show();

                if (form.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                    selectedClientID = form.getClientId();
                }

                updateDocument();
            }
        });
    }


    public String getValue() {
        try {
            return document.getText(0, document.getLength());
        } catch (BadLocationException ignored) {
            return null;
        }
    }

    public void setValue(String newValue) {
        try {
            document.replace(0, document.getLength(), newValue, null);
        } catch (BadLocationException ignored) {
        }
    }

    public PlainDocument getDocument() {
        return document;
    }

    private void updateDocument() {
        boolean isOffice365Selected =
                sharepointListsCheckBox.isSelected()
                        || fileServicesCheckBox.isSelected()
                        || outlookServicesCheckBox.isSelected();

        if ((isOffice365Selected
                && oneNoteCheckBox.isSelected()
                && selectedApplication != null
                && selectedClientID != null)
                || (isOffice365Selected
                && !oneNoteCheckBox.isSelected()
                && selectedApplication != null)
                || (!isOffice365Selected
                && oneNoteCheckBox.isSelected()
                && selectedClientID != null)) {

            Gson gson = new Gson();

            Office365Parameters office365Parameters = new Office365Parameters(
                    sharepointListsCheckBox.isSelected(),
                    fileServicesCheckBox.isSelected(),
                    outlookServicesCheckBox.isSelected(),
                    oneNoteCheckBox.isSelected(),
                    isOffice365Selected ? selectedApplication.getappId() : null,
                    isOffice365Selected ? selectedApplication.getdisplayName() : null,
                    oneNoteCheckBox.isSelected() ? selectedClientID : null);

            String stringVal = gson.toJson(office365Parameters);

            setValue(stringVal);
        } else {
            setValue("");
        }

    }

    private class Office365Parameters {

        private boolean isSharepointLists;
        private boolean isFileServices;
        private boolean isOutlookServices;
        private boolean isOneNote;
        private String appId;
        private String appName;
        private String clientId;

        public Office365Parameters(boolean sharepointListsCheckBoxSelected, boolean fileServicesCheckBoxSelected, boolean outlookServicesCheckBoxSelected, boolean oneNoteCheckBoxSelected, String appId, String appName, String clientId) {

            this.isSharepointLists = sharepointListsCheckBoxSelected;
            this.isFileServices = fileServicesCheckBoxSelected;
            this.isOutlookServices = outlookServicesCheckBoxSelected;
            this.isOneNote = oneNoteCheckBoxSelected;
            this.appId = appId;
            this.appName = appName;
            this.clientId = clientId;
        }
    }
}