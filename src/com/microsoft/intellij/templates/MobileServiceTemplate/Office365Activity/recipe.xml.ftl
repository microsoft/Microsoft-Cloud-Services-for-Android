<#assign parameters = customParameters?eval><?xml version="1.0"?>
<!--
Copyright (c) Microsoft Corporation

All rights reserved.

MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of
the Software.

THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
-->
<recipe>
<#if parameters.isOutlookServices>
	<dependency mavenUrl="com.microsoft.services:outlook-services:0.13.0" />
	
</#if>
<#if parameters.isFileServices>
	<dependency mavenUrl="com.microsoft.services:file-services:0.13.0" />
	
</#if>
<#if parameters.isOutlookServices || parameters.isFileServices || parameters.isOneNote>
	<dependency mavenUrl="com.microsoft.services:odata-engine-android-impl:0.13.0@aar" />
	
</#if>
<#if parameters.isSharepointLists>
	<dependency mavenUrl="com.microsoft.services:sharepoint-services:0.13.0@aar" />
	
</#if>
<#if parameters.isOneNote>
	<dependency mavenUrl="com.microsoft.services:onenote-services:0.13.0" />
    <dependency mavenUrl="com.microsoft.services:live-auth:0.13.0@aar" />
</#if>
    <merge from="AndroidManifest.xml.ftl"
            to="${escapeXmlAttribute(manifestOut)}/AndroidManifest.xml" />

    <merge from="res/values/strings.xml.ftl"
            to="${escapeXmlAttribute(resOut)}/values/strings.xml" />

    <instantiate from="src/app_package/Office365Activity.java.ftl"
            to="${escapeXmlAttribute(srcOut)}/${activityClass}.java" />

    <open file="${escapeXmlAttribute(srcOut)}/${activityClass}.java" />
</recipe>
