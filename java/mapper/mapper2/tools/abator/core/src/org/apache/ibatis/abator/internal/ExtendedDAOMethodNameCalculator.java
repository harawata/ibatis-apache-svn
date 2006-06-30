/*
 *  Copyright 2006 The Apache Software Foundation
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

package org.apache.ibatis.abator.internal;

import org.apache.ibatis.abator.api.DAOMethodNameCalculator;
import org.apache.ibatis.abator.api.IntrospectedTable;

/**
 * @author Jeff Butler
 *
 */
public class ExtendedDAOMethodNameCalculator implements DAOMethodNameCalculator {

    /**
     * 
     */
    public ExtendedDAOMethodNameCalculator() {
        super();
    }

    public String getInsertMethodName(IntrospectedTable introspectedTable) {
        StringBuffer sb = new StringBuffer();
        sb.append("insert"); //$NON-NLS-1$
        sb.append(introspectedTable.getTable().getDomainObjectName());
        
        return sb.toString();
    }

    public String getUpdateByPrimaryKeyMethodName(IntrospectedTable introspectedTable) {
        StringBuffer sb = new StringBuffer();
        sb.append("update"); //$NON-NLS-1$
        sb.append(introspectedTable.getTable().getDomainObjectName());
        sb.append("ByPrimaryKey"); //$NON-NLS-1$
        
        return sb.toString();
    }

    public String getDeleteByExampleMethodName(IntrospectedTable introspectedTable) {
        StringBuffer sb = new StringBuffer();
        sb.append("delete"); //$NON-NLS-1$
        sb.append(introspectedTable.getTable().getDomainObjectName());
        sb.append("ByExample"); //$NON-NLS-1$
        
        return sb.toString();
    }

    public String getDeleteByPrimaryKeyMethodName(IntrospectedTable introspectedTable) {
        StringBuffer sb = new StringBuffer();
        sb.append("delete"); //$NON-NLS-1$
        sb.append(introspectedTable.getTable().getDomainObjectName());
        sb.append("ByPrimaryKey"); //$NON-NLS-1$
        
        return sb.toString();
    }

    public String getSelectByExampleMethodName(IntrospectedTable introspectedTable) {
        StringBuffer sb = new StringBuffer();
        sb.append("select"); //$NON-NLS-1$
        sb.append(introspectedTable.getTable().getDomainObjectName());
        sb.append("ByExample"); //$NON-NLS-1$
        
        return sb.toString();
    }

    public String getSelectByExampleWithBLOBsMethodName(IntrospectedTable introspectedTable) {
        StringBuffer sb = new StringBuffer();
        sb.append("select"); //$NON-NLS-1$
        sb.append(introspectedTable.getTable().getDomainObjectName());
        sb.append("ByExampleWithBLOBs"); //$NON-NLS-1$
        
        return sb.toString();
    }

    public String getSelectByPrimaryKeyMethodName(IntrospectedTable introspectedTable) {
        StringBuffer sb = new StringBuffer();
        sb.append("select"); //$NON-NLS-1$
        sb.append(introspectedTable.getTable().getDomainObjectName());
        sb.append("ByPrimaryKey"); //$NON-NLS-1$
        
        return sb.toString();
    }
}
