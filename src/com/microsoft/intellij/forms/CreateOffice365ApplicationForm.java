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
package com.microsoft.intellij.forms;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.microsoft.directoryservices.Application;
import com.microsoft.intellij.helpers.LinkListener;
import com.microsoft.intellij.helpers.o365.Office365ManagerImpl;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.StringHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URISyntaxException;

public class CreateOffice365ApplicationForm extends JDialog {
    private JPanel mainPanel;
    private JTextField nameTextField;
    private JButton btnCloseButton;
    private JLabel lblPrivacy;
    private JButton btnCreate;
    private JCheckBox multiTenantCheckBox;
    private JTextField redirectURITextField;
    private DialogResult dialogResult;
    private Application application;

    public DialogResult getDialogResult() {
        return dialogResult;
    }

    public Application getApplication() {
        return application;
    }

    private void setApplication(Application application) {
        this.application = application;
    }

    public enum DialogResult {
        OK,
        CANCEL
    }

    public CreateOffice365ApplicationForm() {
        final JDialog form = this;

        this.setContentPane(mainPanel);
        this.setResizable(false);
        this.setModal(true);
        this.setTitle("Create Office 365 Application");

        // we disable the multi-tenant checkbox for now since VS
        // does it
        // TODO: Find out why VS does it. It's probably because mobile apps *have* to be multi-tenant.
        // In which case why have this option on the UI at all?
        multiTenantCheckBox.setSelected(true);
        multiTenantCheckBox.setEnabled(false);

        lblPrivacy.addMouseListener(new LinkListener("http://msdn.microsoft.com/en-us/vstudio/dn425032.aspx"));

        btnCloseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dialogResult = DialogResult.CANCEL;
                form.setVisible(false);
                form.dispose();
            }
        });

        btnCreate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final String name = nameTextField.getText();
                            final String replyURL = redirectURITextField.getText();

                            String error = "";

                            if (StringHelper.isNullOrWhiteSpace(name)) {
                                error += "The application name must not be empty.\n";
                            } else if (name.length() > 64) {
                                error += "The application name cannot be more than 64 characters long.\n";
                            }

                            if (StringHelper.isNullOrWhiteSpace(replyURL)) {
                                error += "The redirect URI must not be empty.\n";
                            } else {
                                try {
                                    new URI(replyURL);
                                } catch (URISyntaxException e) {
                                    error += "The redirect URI must be a valid URI.\n";
                                }
                            }

                            if (!error.isEmpty()) {
                                JOptionPane.showMessageDialog(form, error, "Error creating the application",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            form.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                            Application application = new Application();
                            application.setdisplayName(name);
                            application.setreplyUrls(Lists.newArrayList(replyURL));
                            application.sethomepage(replyURL);
                            application.setavailableToOtherTenants(multiTenantCheckBox.isSelected());
                            application.setpublicClient(true);

                            Futures.addCallback(Office365ManagerImpl.getManager().registerApplication(application),
                                    new FutureCallback<Application>() {
                                        @Override
                                        public void onSuccess(final Application application) {
                                            ApplicationManager.getApplication().invokeAndWait(new Runnable() {
                                                @Override
                                                public void run() {
                                                    setApplication(application);
                                                    dialogResult = DialogResult.OK;
                                                    form.setCursor(Cursor.getDefaultCursor());
                                                    form.dispose();
                                                }
                                            }, ModalityState.any());
                                        }

                                        @Override
                                        public void onFailure(final Throwable throwable) {
                                            ApplicationManager.getApplication().invokeAndWait(new Runnable() {
                                                @Override
                                                public void run() {
                                                    form.setCursor(Cursor.getDefaultCursor());
                                                    DefaultLoader.getUIHelper().showException("An error occurred while trying to register the Office 365 application.",
                                                            throwable,
                                                            "Error Registering Office 365 Application",
                                                            false,
                                                            true);
                                                }
                                            }, ModalityState.any());
                                        }
                                    });
                        } catch (Throwable e) {
                            form.setCursor(Cursor.getDefaultCursor());
                            DefaultLoader.getUIHelper().showException("An error occurred while trying to register the Office 365 application.",
                                    e,
                                    "Error Registering Office 365 Application",
                                    false,
                                    true);
                        }
                    }
                });
            }
        });
    }
}