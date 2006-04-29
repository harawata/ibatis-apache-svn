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
package org.apache.ibatis.abator.internal.java.dao;

import org.apache.ibatis.abator.api.FullyQualifiedJavaType;

/**
 * @author Jeff Butler
 */
public class GenericSIDAOTemplate extends AbstractDAOTemplate {

    /**
     *  
     */
    public GenericSIDAOTemplate() {
        super();

        StringBuffer sb = new StringBuffer();
        indent(sb, 1);
        sb.append("public {0}() '{'"); //$NON-NLS-1$
        newLine(sb);
        indent(sb, 2);
        sb.append("super();"); //$NON-NLS-1$
        newLine(sb);
        indent(sb, 1);
        sb.append('}');
        setConstructorTemplate(sb.toString());

        addField("private SqlMapClient sqlMapClient;"); //$NON-NLS-1$

        sb.setLength(0);
        indent(sb, 1);
        sb.append("public void setSqlMapClient(SqlMapClient sqlMapClient) {"); //$NON-NLS-1$
        newLine(sb);
        indent(sb, 2);
        sb.append("this.sqlMapClient = sqlMapClient;"); //$NON-NLS-1$
        newLine(sb);
        indent(sb, 1);
        sb.append('}');
        addMethod(sb.toString());

        sb.setLength(0);
        indent(sb, 1);
        sb.append("public SqlMapClient getSqlMapClient() {"); //$NON-NLS-1$
        newLine(sb);
        indent(sb, 2);
        sb.append("return sqlMapClient;"); //$NON-NLS-1$
        newLine(sb);
        indent(sb, 1);
        sb.append('}');
        addMethod(sb.toString());

        addImplementationImport(new FullyQualifiedJavaType(
                "com.ibatis.sqlmap.client.SqlMapClient")); //$NON-NLS-1$

        addCheckedException(new FullyQualifiedJavaType("java.sql.SQLException")); //$NON-NLS-1$
        setDeleteMethod("sqlMapClient.delete"); //$NON-NLS-1$
        setInsertMethod("sqlMapClient.insert"); //$NON-NLS-1$
        setQueryForObjectMethod("sqlMapClient.queryForObject"); //$NON-NLS-1$
        setQueryForListMethod("sqlMapClient.queryForList"); //$NON-NLS-1$
        setUpdateMethod("sqlMapClient.update"); //$NON-NLS-1$
    }
}
