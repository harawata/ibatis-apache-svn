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

import org.apache.ibatis.abator.internal.java.DAOGeneratorGenericConstructorInjectionImpl;
import org.apache.ibatis.abator.internal.java.DAOGeneratorGenericSetterInjectionImpl;
import org.apache.ibatis.abator.internal.java.DAOGeneratorIbatisImpl;
import org.apache.ibatis.abator.internal.java.DAOGeneratorSpringImpl;
import org.apache.ibatis.abator.internal.java.dao.GenericCIJava2DAOGenerator;
import org.apache.ibatis.abator.internal.java.dao.GenericSIJava2DAOGenerator;
import org.apache.ibatis.abator.internal.java.dao.IbatisJava2DAOGenerator;
import org.apache.ibatis.abator.internal.java.dao.SpringJava2DAOGenerator;

/**
 * @author Jeff Butler
 */
public class DAOGeneratorConfiguration extends TypedPropertyHolder {
	private String targetPackage;

	private String targetProject;

	private boolean enabled;

	/**
	 *  
	 */
	public DAOGeneratorConfiguration() {
		super();
		enabled = false;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
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
        if (JavaModelGeneratorConfiguration.USE_NEW_GENERATORS) {
            if ("IBATIS".equalsIgnoreCase(type)) { //$NON-NLS-1$
                super.setType(IbatisJava2DAOGenerator.class.getName());
            } else if ("SPRING".equalsIgnoreCase(type)) { //$NON-NLS-1$
                super.setType(SpringJava2DAOGenerator.class.getName());
            } else if ("GENERIC-CI".equalsIgnoreCase(type)) { //$NON-NLS-1$
                super.setType(GenericCIJava2DAOGenerator.class.getName());
            } else if ("GENERIC-SI".equalsIgnoreCase(type)) { //$NON-NLS-1$
                super.setType(GenericSIJava2DAOGenerator.class.getName());
            } else {
                super.setType(type);
            }
        } else {
            if ("IBATIS".equalsIgnoreCase(type)) { //$NON-NLS-1$
                super.setType(DAOGeneratorIbatisImpl.class.getName());
            } else if ("SPRING".equalsIgnoreCase(type)) { //$NON-NLS-1$
                super.setType(DAOGeneratorSpringImpl.class.getName());
            } else if ("GENERIC-CI".equalsIgnoreCase(type)) { //$NON-NLS-1$
                super.setType(DAOGeneratorGenericConstructorInjectionImpl.class.getName());
            } else if ("GENERIC-SI".equalsIgnoreCase(type)) { //$NON-NLS-1$
                super.setType(DAOGeneratorGenericSetterInjectionImpl.class.getName());
            } else {
                super.setType(type);
            }
        }
    }
}
