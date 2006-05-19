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
import org.apache.ibatis.abator.internal.java.dao.GenericCIJava5DAOGenerator;
import org.apache.ibatis.abator.internal.java.dao.GenericSIJava2DAOGenerator;
import org.apache.ibatis.abator.internal.java.dao.GenericSIJava5DAOGenerator;
import org.apache.ibatis.abator.internal.java.dao.IbatisJava2DAOGenerator;
import org.apache.ibatis.abator.internal.java.dao.IbatisJava5DAOGenerator;
import org.apache.ibatis.abator.internal.java.dao.SpringJava2DAOGenerator;
import org.apache.ibatis.abator.internal.java.dao.SpringJava5DAOGenerator;

/**
 * @author Jeff Butler
 */
public class DAOGeneratorConfiguration extends TypedPropertyHolder {
	private String targetPackage;

	private String targetProject;

	private boolean enabled;
    
    private AbatorContext abatorContext;

	/**
	 *  
	 */
	public DAOGeneratorConfiguration(AbatorContext abatorContext) {
		super();
		enabled = false;
        this.abatorContext = abatorContext;
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
	
    public String getImplementationType() {
        String answer;
        String value = (String) abatorContext.getProperties().get("defaultGeneratorConfiguration"); //$NON-NLS-1$
        
        if ("Java5Iterator".equalsIgnoreCase(value)) { //$NON-NLS-1$
            if ("IBATIS".equalsIgnoreCase(getConfigurationType())) { //$NON-NLS-1$
                answer = IbatisJava5DAOGenerator.class.getName();
            } else if ("SPRING".equalsIgnoreCase(getConfigurationType())) { //$NON-NLS-1$
                answer =SpringJava5DAOGenerator.class.getName();
            } else if ("GENERIC-CI".equalsIgnoreCase(getConfigurationType())) { //$NON-NLS-1$
                answer =GenericCIJava5DAOGenerator.class.getName();
            } else if ("GENERIC-SI".equalsIgnoreCase(getConfigurationType())) { //$NON-NLS-1$
                answer = GenericSIJava5DAOGenerator.class.getName();
            } else {
                answer = getConfigurationType();
            }
        } else if ("Java2Iterator".equalsIgnoreCase(value)) { //$NON-NLS-1$
            if ("IBATIS".equalsIgnoreCase(getConfigurationType())) { //$NON-NLS-1$
                answer = IbatisJava2DAOGenerator.class.getName();
            } else if ("SPRING".equalsIgnoreCase(getConfigurationType())) { //$NON-NLS-1$
                answer =SpringJava2DAOGenerator.class.getName();
            } else if ("GENERIC-CI".equalsIgnoreCase(getConfigurationType())) { //$NON-NLS-1$
                answer =GenericCIJava2DAOGenerator.class.getName();
            } else if ("GENERIC-SI".equalsIgnoreCase(getConfigurationType())) { //$NON-NLS-1$
                answer = GenericSIJava2DAOGenerator.class.getName();
            } else {
                answer = getConfigurationType();
            }
        } else if ("Java2NonIterator".equalsIgnoreCase(value)) { //$NON-NLS-1$
            answer = getDefaultGeneratorType();
        } else {
            answer = getDefaultGeneratorType();
        }
        
        return answer;
    }
    
    private String getDefaultGeneratorType() {
        String answer;
        
        if ("IBATIS".equalsIgnoreCase(getConfigurationType())) { //$NON-NLS-1$
            answer = DAOGeneratorIbatisImpl.class.getName();
        } else if ("SPRING".equalsIgnoreCase(getConfigurationType())) { //$NON-NLS-1$
            answer = DAOGeneratorSpringImpl.class.getName();
        } else if ("GENERIC-CI".equalsIgnoreCase(getConfigurationType())) { //$NON-NLS-1$
            answer = DAOGeneratorGenericConstructorInjectionImpl.class.getName();
        } else if ("GENERIC-SI".equalsIgnoreCase(getConfigurationType())) { //$NON-NLS-1$
            answer = DAOGeneratorGenericSetterInjectionImpl.class.getName();
        } else {
            answer = getConfigurationType();
        }
        
        return answer;
    }
}
