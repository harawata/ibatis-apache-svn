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
import org.apache.ibatis.abator.internal.util.StringUtility;

/**
 * @author Jeff Butler
 */
public class SqlMapGeneratorConfiguration extends TypedPropertyHolder {
	private String targetPackage;

	private String targetProject;

    private AbatorContext abatorContext;

	/**
	 *  
	 */
	public SqlMapGeneratorConfiguration(AbatorContext abatorContext) {
		super();
        this.abatorContext = abatorContext;
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

    public String getImplementationType() {
        String answer;
        String value = (String) abatorContext.getProperties().get(
                "defaultGeneratorConfiguration"); //$NON-NLS-1$

        if (StringUtility.stringHasValue(getConfigurationType())) {
            answer = getConfigurationType();
        } else {
            if ("Java5Iterator".equalsIgnoreCase(value)) { //$NON-NLS-1$
                answer = SqlMapGeneratorIterateImpl.class.getName();
            } else if ("Java2Iterator".equalsIgnoreCase(value)) { //$NON-NLS-1$
                answer = SqlMapGeneratorIterateImpl.class.getName();
            } else if ("Java2NonIterator".equalsIgnoreCase(value)) { //$NON-NLS-1$
                answer = SqlMapGeneratorDefaultImpl.class.getName();
            } else {
                answer = SqlMapGeneratorDefaultImpl.class.getName();
            }
        }

        return answer;
    }
}
