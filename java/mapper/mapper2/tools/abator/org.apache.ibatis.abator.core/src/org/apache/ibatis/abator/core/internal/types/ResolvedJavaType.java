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
package org.apache.ibatis.abator.core.internal.types;

public class ResolvedJavaType {
	private String fullyQualifiedName;

	private String jdbcTypeName;

	public ResolvedJavaType() {
		super();
	}

	public String getFullyQualifiedName() {
		return fullyQualifiedName;
	}

	public void setFullyQualifiedName(String fullyQualifiedName) {
		this.fullyQualifiedName = fullyQualifiedName;
	}

	public String getShortName() {
		int lastIndex = fullyQualifiedName.lastIndexOf('.');
		if (lastIndex == -1) {
			// something is wrong - no package in the name
			return fullyQualifiedName;
		} else {
			String typeName = fullyQualifiedName.substring(lastIndex + 1);
			return typeName;
		}
	}

	/**
	 * @return Returns the explicitlyImported.
	 */
	public boolean isExplicitlyImported() {
		int lastIndex = fullyQualifiedName.lastIndexOf('.');
		if (lastIndex == -1) {
			// something is wrong - do not try to import this type
			return false;
		} else {
			String packageName = fullyQualifiedName.substring(0, lastIndex);
			return !"java.lang".equals(packageName); //$NON-NLS-1$
		}
	}

	public String getJdbcTypeName() {
		return jdbcTypeName;
	}

	public void setJdbcTypeName(String jdbcTypeName) {
		this.jdbcTypeName = jdbcTypeName;
	}
}
