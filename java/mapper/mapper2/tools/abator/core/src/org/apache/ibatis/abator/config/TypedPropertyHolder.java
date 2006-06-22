/*
 *  Copyright 2005, 2006 The Apache Software Foundation
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

/**
 * @author Jeff Butler
 */
public abstract class TypedPropertyHolder extends PropertyHolder {

	private String configurationType;

	/**
	 *  
	 */
	public TypedPropertyHolder() {
		super();
	}

	public String getConfigurationType() {
		return configurationType;
	}

	/**
	 * This method is protected because subclasses should override it and allow
	 * using special values (like "DEFAULT", or the alias names for the different
	 * DAO generator types).
	 * 
	 * @param type
	 */
	public void setConfigurationType(String configurationType) {
        if (!"DEFAULT".equalsIgnoreCase(configurationType)) { //$NON-NLS-1$
            this.configurationType = configurationType;
        }
	}
}
