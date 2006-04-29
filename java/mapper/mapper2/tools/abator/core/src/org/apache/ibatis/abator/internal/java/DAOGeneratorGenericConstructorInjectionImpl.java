/*
 *  Copyright 2005 The Apache Software Foundation
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
package org.apache.ibatis.abator.internal.java;

import org.apache.ibatis.abator.api.FullyQualifiedJavaType;

/**
 * This class generates DAO classes that are generic and utilize sql maps
 * directly. The pattern is constructor injection.
 * 
 * @author Jeff Butler
 */
public class DAOGeneratorGenericConstructorInjectionImpl extends
        DAOGeneratorBaseImpl {

    /**
     *  
     */
    public DAOGeneratorGenericConstructorInjectionImpl() {
        super();
    }
    
    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.internal.java.DAOGeneratorBaseImpl#getDAOGeneratorTemplate()
     */
    public DAOGeneratorTemplate getDAOGeneratorTemplate() {
        DAOGeneratorTemplate template = new DAOGeneratorTemplate();
        
        StringBuffer sb = new StringBuffer();
        indent(sb, 1);
        sb.append("public {0}(SqlMapClient sqlMapClient) '{'"); //$NON-NLS-1$
        newLine(sb);
        indent(sb, 2);
        sb.append("super();"); //$NON-NLS-1$
        newLine(sb);
        indent(sb, 2);
        sb.append("this.sqlMapClient = sqlMapClient;"); //$NON-NLS-1$
        newLine(sb);
        indent(sb, 1);
        sb.append('}');
        template.setConstructorTemplate(sb.toString());

        template.addField("private SqlMapClient sqlMapClient;"); //$NON-NLS-1$

        template.addImplementationImport(new FullyQualifiedJavaType(
                "com.ibatis.sqlmap.client.SqlMapClient")); //$NON-NLS-1$

        template.addCheckedException(new FullyQualifiedJavaType("java.sql.SQLException")); //$NON-NLS-1$
        template.setDeleteMethod("sqlMapClient.delete"); //$NON-NLS-1$
        template.setInsertMethod("sqlMapClient.insert"); //$NON-NLS-1$
        template.setQueryForObjectMethod("sqlMapClient.queryForObject"); //$NON-NLS-1$
        template.setQueryForListMethod("sqlMapClient.queryForList"); //$NON-NLS-1$
        template.setUpdateMethod("sqlMapClient.update"); //$NON-NLS-1$

        return template;
    }
}
