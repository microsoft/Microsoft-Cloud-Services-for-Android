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

import com.android.tools.idea.templates.Parameter;
import com.android.tools.idea.wizard.ScopedDataBinder;
import com.android.tools.idea.wizard.WizardParameterFactory;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.Document;

public class Office365WizardParameterFactory implements WizardParameterFactory {
    @Override
    public String[] getSupportedTypes() {
        return new String[]{"office365CustomParameter"};
    }

    @Override
    public JComponent createComponent(String s, Parameter parameter) {
        return new Office365ParameterPane();
    }

    @Override
    public ScopedDataBinder.ComponentBinding<String, JComponent> createBinding(JComponent jComponent, Parameter parameter) {
        return new ScopedDataBinder.ComponentBinding<String, JComponent>() {
            @Nullable
            @Override
            public String getValue(JComponent component) {
                return ((Office365ParameterPane) component).getValue();
            }

            @Override
            public void setValue(@Nullable String newValue, JComponent component) {
                ((Office365ParameterPane) component).setValue(newValue);
            }

            @Nullable
            @Override
            public Document getDocument(JComponent component) {
                return ((Office365ParameterPane) component).getDocument();
            }
        };

    }
}
