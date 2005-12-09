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
 * This class generates DAO classes based on the Spring Framework DAO support.
 * 
 * @author Jeff Butler
 */
public class DAOGeneratorSpringImpl extends DAOGeneratorBaseImpl {
	private static final DAOGeneratorTemplate template;

	static {
		template = new DAOGeneratorTemplate();
		template.setConstructorTemplate("public {0}() '{' super();}"); //$NON-NLS-1$
		template.setSuperClass("SqlMapClientDaoSupport"); //$NON-NLS-1$

		template
				.addImplementationImport("org.springframework.orm.ibatis.support.SqlMapClientDaoSupport"); //$NON-NLS-1$

		template.setDeleteMethod("getSqlMapClientTemplate().delete"); //$NON-NLS-1$
		template.setInsertMethod("getSqlMapClientTemplate().insert"); //$NON-NLS-1$
		template
				.setQueryForObjectMethod("getSqlMapClientTemplate().queryForObject"); //$NON-NLS-1$
		template
				.setQueryForListMethod("getSqlMapClientTemplate().queryForList"); //$NON-NLS-1$
		template.setUpdateMethod("getSqlMapClientTemplate().update"); //$NON-NLS-1$
	}

	/**
	 *  
	 */
	public DAOGeneratorSpringImpl() {
		super(template);
	}
}
