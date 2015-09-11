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
package com.microsoft.intellij.helpers.o365;

import com.google.common.util.concurrent.ListenableFuture;
import com.intellij.openapi.project.Project;
import com.microsoft.directoryservices.Application;
import com.microsoft.directoryservices.OAuth2PermissionGrant;
import com.microsoft.directoryservices.ServicePrincipal;
import com.microsoft.intellij.helpers.graph.ServicePermissionEntry;
import com.microsoft.tooling.msservices.helpers.NotNull;
import com.microsoft.tooling.msservices.helpers.azure.AzureCmdException;

import java.util.List;

public interface Office365Manager {
    void authenticate() throws AzureCmdException;

    boolean authenticated();

    void clearAuthentication();

    @NotNull
    ListenableFuture<List<Application>> getApplicationList();

    @NotNull
    ListenableFuture<Application> getApplicationByObjectId(@NotNull String objectId);

    @NotNull
    ListenableFuture<List<ServicePermissionEntry>> getO365PermissionsForApp(@NotNull String objectId);

    @NotNull
    ListenableFuture<Application> setO365PermissionsForApp(@NotNull Application application,
                                                           @NotNull List<ServicePermissionEntry> permissionEntryList);

    @NotNull
    ListenableFuture<Application> updateApplication(@NotNull Application application);

    @NotNull
    ListenableFuture<List<ServicePrincipal>> getServicePrincipalsForO365();

    @NotNull
    ListenableFuture<List<ServicePrincipal>> getServicePrincipals();

    @NotNull
    ListenableFuture<List<OAuth2PermissionGrant>> getPermissionGrants();

    @NotNull
    ListenableFuture<Application> registerApplication(@NotNull Application application);

    void setApplicationForProject(@NotNull Project project, @NotNull Application application);

    @NotNull
    ListenableFuture<Application> getApplicationForProject(@NotNull Project project);

    @NotNull
    ListenableFuture<List<ServicePrincipal>> getServicePrincipalsForApp(@NotNull Application application);

    @NotNull
    ListenableFuture<List<ServicePrincipal>> getO365ServicePrincipalsForApp(@NotNull Application application);

    @NotNull
    ListenableFuture<List<ServicePrincipal>> addServicePrincipals(@NotNull List<ServicePrincipal> servicePrincipals);
}