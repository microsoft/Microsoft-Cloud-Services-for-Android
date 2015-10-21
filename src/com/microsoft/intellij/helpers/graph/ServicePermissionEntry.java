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

package com.microsoft.intellij.helpers.graph;

import com.microsoft.tooling.msservices.model.Office365PermissionList;
import com.microsoft.tooling.msservices.model.Office365Service;

import java.util.Map;

public class ServicePermissionEntry implements Map.Entry<Office365Service, Office365PermissionList> {
    private Office365Service service;
    private Office365PermissionList permissionSet;

    public ServicePermissionEntry(Office365Service service, Office365PermissionList permissionSet) {
        this.service = service;
        this.permissionSet = permissionSet;
    }

    @Override
    public Office365Service getKey() {
        return service;
    }

    @Override
    public Office365PermissionList getValue() {
        return permissionSet;
    }

    @Override
    public Office365PermissionList setValue(Office365PermissionList value) {
        Office365PermissionList old = permissionSet;
        permissionSet = value;
        return old;
    }
}