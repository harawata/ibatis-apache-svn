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
package org.apache.ibatis.abator.config;

import org.apache.ibatis.abator.internal.sqlmap.SqlMapGeneratorDefaultImpl;
import org.apache.ibatis.abator.internal.sqlmap.SqlMapGeneratorIterateImpl;

/**
 * @author Jeff Butler
 */
public class SqlMapGeneratorConfiguration extends TypedPropertyHolder {
	private String targetPackage;

	private String targetProject;

	/**
	 *  
	 */
	public SqlMapGeneratorConfiguration() {
		super();
		if (JavaModelGeneratorConfiguration.USE_NEW_GENERATORS) {
		    super.setType(SqlMapGeneratorIterateImpl.class.getName());
		} else {
		    super.setType(SqlMapGeneratorDefaultImpl.class.getName());
		}
	}

	public String getTargetProject() {
		return targetProject;
	}

	public void setTargetProject(String targetProject) {
		this.targetProject = targetProject;
	}
	
	public String getTargetPackage() {
		return targetPackage;
	}
	
	public void setTargetPackage(String targetPackage) {
		this.targetPackage = targetPackage;
	}
	
    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.config.TypedPropertyHolder#setType(java.lang.String)
     */
    public void setType(String type) {
		if (!"DEFAULT".equalsIgnoreCase(type)) { //$NON-NLS-1$
		    super.setType(type);
		}
    }
}
