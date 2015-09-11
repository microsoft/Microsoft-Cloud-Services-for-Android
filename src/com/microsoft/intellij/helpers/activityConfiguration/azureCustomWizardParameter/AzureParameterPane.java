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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.ide.DataManager;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiElement;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.model.ms.MobileService;
import org.jetbrains.android.facet.AndroidFacet;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class AzureParameterPane extends JPanel {
    private JCheckBox mobileServicesCheckBox;
    private JPanel mainPanel;
    private JCheckBox notificationHubCheckBox;
    private JButton notificationHubConfigureButton;
    private JButton mobileServicesConfigureButton;
    private PlainDocument document;
    private MobileService selectedMobileService;
    private String connectionString;
    private String hubName;
    private String senderID;

    public AzureParameterPane() {
        super(new BorderLayout());

        this.add(mainPanel, BorderLayout.CENTER);

        document = new PlainDocument();

        mobileServicesCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                mobileServicesConfigureButton.setEnabled(mobileServicesCheckBox.isSelected());
                updateDocument();
            }
        });
        notificationHubCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                notificationHubConfigureButton.setEnabled(notificationHubCheckBox.isSelected());
                updateDocument();
            }
        });

        mobileServicesConfigureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                DataContext dataContext = DataManager.getInstance().getDataContext(mainPanel);
                final Project project = DataKeys.PROJECT.getData(dataContext);

                final MobileServiceConfigForm form = new MobileServiceConfigForm(project);

                if (selectedMobileService != null) {
                    form.setSelectedMobileService(selectedMobileService);
                }

                form.show();

                if (form.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                    selectedMobileService = form.getSelectedMobileService();

                }

                updateDocument();

            }
        });


        notificationHubConfigureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    DataContext dataContext = DataManager.getInstance().getDataContext(mainPanel);
                    Project project = DataKeys.PROJECT.getData(dataContext);

                    Module module = null;
                    Object selectedElement = ProjectView.getInstance(project).getCurrentProjectViewPane().getSelectedElement();

                    if (selectedElement instanceof PsiElement) {
                        PsiElement psiSelectedElement = (PsiElement) selectedElement;
                        module = ModuleUtil.findModuleForPsiElement(psiSelectedElement);
                    } else if (selectedElement instanceof AndroidFacet) {
                        module = ((AndroidFacet) selectedElement).getModule();
                    } else if (selectedElement instanceof Module) {
                        module = (Module) selectedElement;
                    }

                    if (module != null) {
                        final NotificationHubConfigForm form = new NotificationHubConfigForm(module);

                        if (connectionString != null) {
                            form.setConnectionString(connectionString);
                        }
                        if (senderID != null) {
                            form.setSenderID(senderID);
                        }
                        if (hubName != null) {
                            form.setHubName(hubName);
                        }

                        form.show();

                        if (form.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                            connectionString = form.getConnectionString();
                            hubName = form.getHubName();
                            senderID = form.getSenderID();
                        }

                        updateDocument();
                    }
                } catch (Throwable e) {
                    DefaultLoader.getUIHelper().showException("Error loading notification hubs configuration", e);
                }
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
        if ((mobileServicesCheckBox.isSelected()
                && !notificationHubCheckBox.isSelected()
                && selectedMobileService != null)
                || (!mobileServicesCheckBox.isSelected()
                && notificationHubCheckBox.isSelected()
                && senderID != null
                && connectionString != null
                && hubName != null)
                || (mobileServicesCheckBox.isSelected()
                && notificationHubCheckBox.isSelected()
                && selectedMobileService != null
                && senderID != null
                && connectionString != null
                && hubName != null)) {

            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.disableHtmlEscaping();
            Gson gson = gsonBuilder.create();

            AzureParameters azureParameters = new AzureParameters(
                    mobileServicesCheckBox.isSelected(),
                    notificationHubCheckBox.isSelected(),
                    mobileServicesCheckBox.isSelected() ? selectedMobileService.getAppUrl() : null,
                    mobileServicesCheckBox.isSelected() ? selectedMobileService.getAppKey() : null,
                    notificationHubCheckBox.isSelected() ? senderID : null,
                    notificationHubCheckBox.isSelected() ? connectionString : null,
                    notificationHubCheckBox.isSelected() ? hubName : null);

            String stringVal = gson.toJson(azureParameters);

            setValue(stringVal);
        } else {
            setValue("");
        }
    }

    private class AzureParameters {
        public AzureParameters(boolean hasMobileService, boolean hasNotificationHub, String appUrl, String appKey, String sender, String connStr, String hub) {
            this.hasMobileService = hasMobileService;
            this.hasNotificationHub = hasNotificationHub;
            this.appUrl = appUrl;
            this.appKey = appKey;
            this.sender = sender;
            this.connStr = connStr;
            this.hub = hub;
        }

        private boolean hasMobileService;
        private boolean hasNotificationHub;
        private String appUrl;
        private String appKey;
        private String sender;
        private String connStr;
        private String hub;
    }
}
