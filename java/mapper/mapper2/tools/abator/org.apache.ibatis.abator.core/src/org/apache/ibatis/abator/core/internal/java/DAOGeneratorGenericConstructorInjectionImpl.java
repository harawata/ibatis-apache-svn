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
package org.apache.ibatis.abator.core.internal.java;

/**
 * This class generates DAO classes that are generic and utilize sql maps
 * directly. The pattern is constructor injection.
 * 
 * @author Jeff Butler
 */
public class DAOGeneratorGenericConstructorInjectionImpl extends
		DAOGeneratorBaseImpl {

	private static final DAOGeneratorTemplate template;

	static {
		template = new DAOGeneratorTemplate();
		template
				.setConstructorTemplate("public {0}(SqlMapClient sqlMapClient) '{' super(); this.sqlMapClient = sqlMapClient;}"); //$NON-NLS-1$
		template.addField("private SqlMapClient sqlMapClient;"); //$NON-NLS-1$

		template
				.addImplementationImport("com.ibatis.sqlmap.client.SqlMapClient"); //$NON-NLS-1$
		template.addImplementationImport("java.sql.SQLException"); //$NON-NLS-1$

		template.addInterfaceImport("java.sql.SQLException"); //$NON-NLS-1$

		template.setCheckedExceptions("SQLException"); //$NON-NLS-1$
		template.setDeleteMethod("sqlMapClient.delete"); //$NON-NLS-1$
		template.setInsertMethod("sqlMapClient.insert"); //$NON-NLS-1$
		template.setQueryForObjectMethod("sqlMapClient.queryForObject"); //$NON-NLS-1$
		template.setQueryForListMethod("sqlMapClient.queryForList"); //$NON-NLS-1$
		template.setUpdateMethod("sqlMapClient.update"); //$NON-NLS-1$
	}

	/**
	 *  
	 */
	public DAOGeneratorGenericConstructorInjectionImpl() {
		super(template);
	}
}
