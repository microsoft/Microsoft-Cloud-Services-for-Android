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

package com.microsoft.intellij.helpers;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.microsoft.tooling.msservices.components.DefaultLoader;
import com.microsoft.tooling.msservices.helpers.ServiceCodeReferenceHelper;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;
import sun.misc.IOUtils;

import javax.swing.*;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;


public class AndroidStudioHelper {
    private static final String mobileServicesTemplateName = "AzureServicesActivity";
    private static final String officeTemplateName = "Office365Activity";

    public static void newActivityTemplateManager(boolean deleteTemplates, Object caller) throws IOException, InterruptedException {
        String templatePath = URLDecoder.decode(ApplicationComponent.class.getResource("").getPath().replace("file:/", ""), "UTF-8");
        templatePath = templatePath.replace("/", File.separator);
        templatePath = templatePath.substring(0, templatePath.indexOf(File.separator + "lib"));
        templatePath = templatePath + File.separator + "plugins" + File.separator + "android" + File.separator;
        templatePath = templatePath + "lib" + File.separator + "templates" + File.separator + "activities" + File.separator;

        if (System.getProperty("os.name").toLowerCase().startsWith("mac") && !templatePath.startsWith(File.separator)) {
            templatePath = File.separator + templatePath;
        }

        if (deleteTemplates || !new File(templatePath + mobileServicesTemplateName).exists()) {
            String tmpDir = getTempLocation();
            copyResourcesRecursively(new File(tmpDir));

            tmpDir = tmpDir + "MobileServiceTemplate" + File.separator;

            if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
                try {

                    if (deleteTemplates) {
                        VirtualFile mobileTemplate = LocalFileSystem.getInstance().findFileByIoFile(new File(templatePath + mobileServicesTemplateName));
                        VirtualFile officeTemplate = LocalFileSystem.getInstance().findFileByIoFile(new File(templatePath + officeTemplateName));

                        if (mobileTemplate != null)
                            mobileTemplate.delete(caller);

                        if (officeTemplate != null)
                            officeTemplate.delete(caller);

                    }

                    copyFolder(new File(tmpDir + mobileServicesTemplateName), new File(templatePath + mobileServicesTemplateName));
                    copyFolder(new File(tmpDir + officeTemplateName), new File(templatePath + officeTemplateName));

                } catch (IOException ex) {

                    String parameterFormat = "<Copy originPath='%s' targetPath='%s' deleteTarget='%s'/>";

                    StringBuilder sb = new StringBuilder();
                    sb.append("<WindowsTemplateCopyParameters>");
                    sb.append(String.format(parameterFormat, tmpDir + mobileServicesTemplateName, templatePath + mobileServicesTemplateName, deleteTemplates ? "true" : "false"));
                    sb.append(String.format(parameterFormat, tmpDir + officeTemplateName, templatePath + officeTemplateName, deleteTemplates ? "true" : "false"));
                    sb.append("</WindowsTemplateCopyParameters>");
                    String param = DatatypeConverter.printBase64Binary(sb.toString().getBytes("UTF-8"));


                    String[] tmpCmd = {
                            "cmd",
                            "/c",
                            tmpDir + "WindowsTemplateCopy.exe",
                            param,
                    };

                    ArrayList<String> tempenvlist = new ArrayList<String>();
                    for (String envval : System.getenv().keySet())
                        tempenvlist.add(String.format("%s=%s", envval, System.getenv().get(envval)));

                    tempenvlist.add("PRECOMPILE_STREAMLINE_FILES=1");
                    String[] env = new String[tempenvlist.size()];
                    tempenvlist.toArray(env);

                    Runtime rt = Runtime.getRuntime();
                    Process proc = rt.exec(tmpCmd, env, new File(tmpDir));
                    int errorCode = proc.waitFor();

                    //wait for elevate command to finish
                    Thread.sleep(3000);

                    if (!new File(templatePath + mobileServicesTemplateName).exists() || errorCode != 0)
                        DefaultLoader.getUIHelper().showException("Error copying template files. Please refer to documentation to copy manually.", new Exception());
                }
            } else if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {

                String[] deleteAndCopy = {
                        "osascript",
                        "-e",
                        "do shell script \"rm -r \\\"/" + templatePath + mobileServicesTemplateName + "\\\"\" with administrator privileges",
                        "-e",
                        "do shell script \"rm -r \\\"/" + templatePath + officeTemplateName + "\\\"\" with administrator privileges",
                        "-e",
                        "do shell script \"cp -Rp \\\"" + tmpDir + mobileServicesTemplateName + "\\\" \\\"/" + templatePath + "\\\"\" with administrator privileges",
                        "-e",
                        "do shell script \"cp -Rp \\\"" + tmpDir + officeTemplateName + "\\\" \\\"/" + templatePath + "\\\"\" with administrator privileges"
                };

                String[] copy = {
                        "osascript",
                        "-e",
                        "do shell script \"cp -Rp \\\"" + tmpDir + mobileServicesTemplateName + "\\\" \\\"/" + templatePath + "\\\"\" with administrator privileges",
                        "-e",
                        "do shell script \"cp -Rp \\\"" + tmpDir + officeTemplateName + "\\\" \\\"/" + templatePath + "\\\"\" with administrator privileges"
                };

                exec(deleteTemplates ? deleteAndCopy : copy, tmpDir);
            } else {
                try {

                    if (deleteTemplates) {
                        VirtualFile mobileTemplate = LocalFileSystem.getInstance().findFileByIoFile(new File(templatePath + mobileServicesTemplateName));
                        VirtualFile officeTemplate = LocalFileSystem.getInstance().findFileByIoFile(new File(templatePath + officeTemplateName));

                        if (mobileTemplate != null)
                            mobileTemplate.delete(caller);

                        if (officeTemplate != null)
                            officeTemplate.delete(caller);

                    }

                    copyFolder(new File(tmpDir + mobileServicesTemplateName), new File(templatePath + mobileServicesTemplateName));
                    copyFolder(new File(tmpDir + officeTemplateName), new File(templatePath + officeTemplateName));


                } catch (IOException ex) {

                    JPasswordField pf = new JPasswordField();
                    int okCxl = JOptionPane.showConfirmDialog(null, pf, "To copy Microsoft Services templates, the plugin needs your password:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                    if (okCxl == JOptionPane.OK_OPTION) {
                        String password = new String(pf.getPassword());


                        exec(new String[]{
                                "echo",
                                password,
                                "|",
                                "sudo",
                                "-S",
                                "rm",
                                "-r",
                                tmpDir + mobileServicesTemplateName,
                                templatePath + mobileServicesTemplateName
                        }, tmpDir);


                        exec(new String[]{
                                "echo",
                                password,
                                "|",
                                "sudo",
                                "-S",
                                "rm",
                                "-r",
                                tmpDir + officeTemplateName,
                                templatePath + officeTemplateName
                        }, tmpDir);


                        exec(new String[]{
                                "echo",
                                password,
                                "|",
                                "sudo",
                                "-S",
                                "cp",
                                "-Rp",
                                tmpDir + mobileServicesTemplateName,
                                templatePath + mobileServicesTemplateName
                        }, tmpDir);


                        exec(new String[]{
                                "echo",
                                password,
                                "|",
                                "sudo",
                                "-S",
                                "cp",
                                "-Rp",
                                tmpDir + officeTemplateName,
                                templatePath + officeTemplateName
                        }, tmpDir);
                    }
                }
            }
        }
    }

    private static void copyResourcesRecursively(File targetDir) throws IOException {
        InputStream fileList = ServiceCodeReferenceHelper.getTemplateResource("MobileServiceTemplate/fileList.txt");
        BufferedReader in = new BufferedReader(new InputStreamReader(fileList));

        String line;
        while ((line = in.readLine()) != null) {

            String[] pathParts = line.split("/");
            String fileName = pathParts[pathParts.length - 1];
            String path = line.replace(fileName, "");

            String targetPath = targetDir.getAbsolutePath() + File.separator
                    + "MobileServiceTemplate" + File.separator
                    + path.replace("/", File.separator);

            File targetFolder = new File(targetPath);
            targetFolder.mkdirs();

            File targetFile = new File(targetFolder, fileName);
            targetFile.createNewFile();
            targetFile.setWritable(true);

            InputStream inputStream = ServiceCodeReferenceHelper.getTemplateResource("MobileServiceTemplate/" + line);

            byte[] content = IOUtils.readFully(inputStream, -1, true);

            FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
            fileOutputStream.write(content);
            fileOutputStream.flush();
            fileOutputStream.close();

        }
    }


    private static void exec(String[] cmd, String tmpdir) throws IOException, InterruptedException {
        String[] env = new String[]{"PRECOMPILE_STREAMLINE_FILES=1"};

        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(cmd, env, new File(tmpdir));

        // any error message?
        StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), true);


        // kick them off
        errorGobbler.start();

        proc.waitFor();
    }

    private static String getTempLocation() {

        String tmpdir = System.getProperty("java.io.tmpdir");
        StringBuilder sb = new StringBuilder();
        sb.append(tmpdir);

        if (!tmpdir.endsWith(File.separator))
            sb.append(File.separator);

        sb.append("TempAzure");
        sb.append(File.separator);

        return sb.toString();
    }

    public static boolean isAndroidGradleModule(VirtualFile virtualFileDir) throws IOException {
        for (VirtualFile file : virtualFileDir.getChildren()) {
            if (file.getName().contains("build.gradle")) {
                if (isAndroidGradleBuildFile(file))
                    return true;
            }
        }
        return false;
    }

    public static boolean isAndroidGradleBuildFile(VirtualFile buildGradleFile) throws IOException {
        return ServiceCodeReferenceHelper.getStringAndCloseStream(buildGradleFile.getInputStream()).contains("com.android.tools.build");
    }

    private static class StreamGobbler extends Thread {
        InputStream is;
        boolean isError;

        public StreamGobbler(InputStream is, boolean isError) {
            this.is = is;
            this.isError = isError;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }

                is.close();

                String streamContent = sb.toString();

                if (isError && !streamContent.isEmpty())
                    DefaultLoader.getUIHelper().showException("Error copying Microsoft Services templates", new AzureCmdException("Error copying Microsoft Services templates", "Error: " + streamContent));

            } catch (IOException ioe) {
                DefaultLoader.getUIHelper().showException("Error copying Microsoft Services templates", ioe);
            }
        }
    }

    public static void copyFolder(File src, File dest)
            throws IOException {

        if (src.isDirectory()) {

            //if directory not exists, create it
            if (!dest.exists()) {
                dest.mkdir();
            }

            //list all the directory contents
            String files[] = src.list();

            for (String file : files) {
                //construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                //recursive copy
                copyFolder(srcFile, destFile);
            }

        } else {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
        }
    }
}
