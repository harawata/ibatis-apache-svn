/*
 *  Copyright 2008 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.ibatis.ibator.build.test;

import java.util.List;

import org.apache.ibatis.ibator.api.IbatorPluginAdapter;
import org.apache.ibatis.ibator.api.IntrospectedTable;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;

/**
 * This class is used to test certain plugin features during the Ibator build
 * 
 * @author Jeff Butler
 *
 */
public class TestPlugin extends IbatorPluginAdapter {

    /**
     * 
     */
    public TestPlugin() {
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.ibator.api.IbatorPlugin#validate(java.util.List)
     */
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void attributesCalculated(IntrospectedTable introspectedTable) {
        StringBuilder sb = new StringBuilder();
        sb.append(introspectedTable.getJavaModelPackage());
        sb.append('.');
        sb.append(introspectedTable.getFullyQualifiedTable().getDomainObjectName());
        sb.append("Criteria"); //$NON-NLS-1$

        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(sb.toString());

        introspectedTable.setAttribute(IntrospectedTable.ATTR_EXAMPLE_TYPE, fqjt);
    }
}
