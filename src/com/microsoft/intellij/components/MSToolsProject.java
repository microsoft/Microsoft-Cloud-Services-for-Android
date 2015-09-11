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
package com.microsoft.intellij.components;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.microsoft.tooling.msservices.helpers.XmlHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MSToolsProject extends AbstractProjectComponent {
    private static final String TEMPLATES_ROOT_DIR = "build/intermediates/exploded-aar/Microsoft/templates/%s";
    private static final String TEMPLATES_RESOURCE_PATH = "/com/microsoft/intellij/templates/MobileServiceTemplate/";
    private static final String TEMPLATE_ZIP_NAME = "templates.zip";
    private static final String CACHED_TEMPLATE_ZIP_NAME = "templates-%s.zip";


    protected MSToolsProject(Project project) {
        super(project);
    }

    @Override
    public void projectOpened() {
        StartupManager.getInstance(myProject).registerPostStartupActivity(new Runnable() {
            @Override
            public void run() {
                try {
                    // get project root dir and check if this is an Android project
                    //if (AndroidStudioHelper.isAndroidGradleModule(myProject.getBaseDir())) {
                    if (isAndroidProject()) {
                        createActivityTemplates();
                    }

                } catch (IOException ignored) {
                }

            }
        });
    }


    private void createActivityTemplates() throws IOException {
        // create the root dir to contain our templates zip if the
        // dir doesn't exist already
        File rootDir = new File(myProject.getBaseDir().getPath(),
                String.format(
                        TEMPLATES_ROOT_DIR,
                        MSToolsApplication.getCurrent().getSettings().getPluginVersion()));
        rootDir.mkdirs();

        // we proceed only if "templates.zip" doesn't already exist in the path
        File templatesZip = new File(rootDir, TEMPLATE_ZIP_NAME);
        if (!templatesZip.exists()) {
            File cachedZip = getTemplatesZip();
            Files.copy(cachedZip, templatesZip);
        }
    }

    private File getTemplatesZip() throws IOException {
        // we cache the templates zip for the current version of the plugin
        File cachedZip = new File(System.getProperty("java.io.tmpdir"),
                String.format(
                        CACHED_TEMPLATE_ZIP_NAME,
                        MSToolsApplication.getCurrent().getSettings().getPluginVersion()));

        BufferedReader reader = null;
        InputStream inputStream = null;
        ZipOutputStream outputStream = null;

        try {
            if (!cachedZip.exists()) {
                // read list of files to copy to zip and create the zip file
                outputStream = new ZipOutputStream(
                        new BufferedOutputStream(new FileOutputStream(cachedZip)));

                reader = new BufferedReader(
                        new InputStreamReader(
                                MSToolsProject.class.getResourceAsStream(
                                        TEMPLATES_RESOURCE_PATH + "fileList.txt")));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    inputStream = MSToolsProject.class.getResourceAsStream(
                            TEMPLATES_RESOURCE_PATH + line
                    );

                    ZipEntry entry = new ZipEntry(line);
                    outputStream.putNextEntry(entry);

                    ByteStreams.copy(inputStream, outputStream);
                    inputStream.close();
                    inputStream = null;
                }
                reader.close();
                reader = null;
                outputStream.close();
                outputStream = null;
            }
        } catch (IOException e) {
            // if creation of the zip file fails and leaves a partially
            // created zip file in the file system then we delete it so
            // that we attempt creating it again the next time around
            // which we wouldn't if we discover that the file exists
            // already
            if (cachedZip.exists()) {
                cachedZip.delete();
            }

            throw e;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException ignored) {
            }
        }

        return cachedZip;
    }

    private boolean isAndroidProject() {
        try {
            String projectContent = new String(myProject.getProjectFile().contentsToByteArray());

            Node node = ((NodeList) XmlHelper.getXMLValue(projectContent,
                    "project/component[@name='ProjectType']/option[@name='id']",
                    XPathConstants.NODESET)).item(0);

            return (node != null && XmlHelper.getAttributeValue(node, "value").equals("Android"));

        } catch (Throwable e) {
            return false;
        }
    }
}
