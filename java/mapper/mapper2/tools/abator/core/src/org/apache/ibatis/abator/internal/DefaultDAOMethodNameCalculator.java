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
public class DefaultDAOMethodNameCalculator implements DAOMethodNameCalculator {

    /**
     * 
     */
    public DefaultDAOMethodNameCalculator() {
        super();
    }

    public String getInsertMethodName(IntrospectedTable introspectedTable) {
        return "insert"; //$NON-NLS-1$
    }

    public String getUpdateByPrimaryKeyWithoutBLOBsMethodName(IntrospectedTable introspectedTable) {
        if (introspectedTable.getRules().generateUpdateByPrimaryKeyWithBLOBs()
                && !introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            // can't use the same method name for both methods...
            return "updateByPrimaryKeyWithoutBLOBs"; //$NON-NLS-1$
        } else {
            return "updateByPrimaryKey"; //$NON-NLS-1$
        }
    }

    public String getUpdateByPrimaryKeyWithBLOBsMethodName(IntrospectedTable introspectedTable) {
        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            return "updateByPrimaryKey"; //$NON-NLS-1$
        } else {
            return "updateByPrimaryKeyWithBLOBs"; //$NON-NLS-1$
        }
    }
    
    public String getDeleteByExampleMethodName(IntrospectedTable introspectedTable) {
        return "deleteByExample"; //$NON-NLS-1$
    }

    public String getDeleteByPrimaryKeyMethodName(IntrospectedTable introspectedTable) {
        return "deleteByPrimaryKey"; //$NON-NLS-1$
    }

    public String getSelectByExampleWithoutBLOBsMethodName(IntrospectedTable introspectedTable) {
        return "selectByExample"; //$NON-NLS-1$
    }

    public String getSelectByExampleWithBLOBsMethodName(IntrospectedTable introspectedTable) {
        return "selectByExampleWithBLOBs"; //$NON-NLS-1$
    }

    public String getSelectByPrimaryKeyMethodName(IntrospectedTable introspectedTable) {
        return "selectByPrimaryKey"; //$NON-NLS-1$
    }
}
