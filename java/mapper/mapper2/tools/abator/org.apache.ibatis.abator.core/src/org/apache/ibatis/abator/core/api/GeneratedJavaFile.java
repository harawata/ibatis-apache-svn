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
package org.apache.ibatis.abator.core.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * @author Jeff Butler
 */
public class GeneratedJavaFile extends GeneratedFile {
	private String packageStatement;

	private Set importedTypes;

	private List fields;

	private List methods;

	private boolean javaInterface;

	private String javaName;

	private String superClass;

	private Set superInterfaces;

	/**
	 *  Default constructor
	 */
	public GeneratedJavaFile() {
		super();
		importedTypes = new HashSet();
		fields = new ArrayList();
		methods = new ArrayList();
		superInterfaces = new HashSet();
	}

	public String getPackageStatement() {
		return packageStatement;
	}

	public void setPackageStatement(String packageStatement) {
		this.packageStatement = packageStatement;
	}

	public List getFields() {
		return fields;
	}

	public Set getImportedTypes() {
		return importedTypes;
	}

	public List getMethods() {
		return methods;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.apache.ibatis.abator.core.api.GeneratedFile#getContent()
	 */
	public String getContent() {
		StringBuffer content = new StringBuffer();

		content.append(packageStatement);
		content.append(";\n\n");

		Iterator iter = importedTypes.iterator();
		while (iter.hasNext()) {
			content.append("import ");
			content.append(iter.next());
			content.append(";\n");
		}

		content.append('\n');

		content.append("public ");
		content.append(javaInterface ? "interface " : "class ");

		content.append(javaName);
		
		if (superClass != null) {
			content.append(" extends ");
			content.append(superClass);
		}
		
		if (superInterfaces.size() > 0) {
			content.append(" implements ");
			iter = superInterfaces.iterator();
			boolean comma = false; 
			while (iter.hasNext()) {
				if (comma) {
					content.append(", ");
				}
				
				content.append(iter.next());
				
				comma = true;
			}
		}

		content.append(" {\n");
		
		iter = fields.iterator();
		while (iter.hasNext()) {
			content.append(iter.next());
		}

		iter = methods.iterator();
		while (iter.hasNext()) {
			content.append(iter.next());
		}

		content.append("}\n");

		return content.toString();
	}

	public boolean isJavaInterface() {
		return javaInterface;
	}

	public void setJavaInterface(boolean javaInterface) {
		this.javaInterface = javaInterface;
	}

	public String getJavaName() {
		return javaName;
	}

	public void setJavaName(String javaName) {
		this.javaName = javaName;
	}

	public Set getSuperInterfaces() {
		return superInterfaces;
	}

	public void addSuperInterface(String superInterface) {
		superInterfaces.add(superInterface);
	}

	public String getSuperClass() {
		return superClass;
	}

	public void setSuperClass(String superClass) {
		this.superClass = superClass;
	}
	
	/**
	 * The import should not include the final semicolon.  Any
	 * import in the java.lang package will be ignored.
	 * 
	 * The underlying Set does not allow duplicates, so clients do
	 * not need to be concerned with duplicate resolution.
	 * 
	 * Example addImport("java.math.BigDecimal"); 
	 * 
	 * @param importedType - the type to import.
	 */
	public void addImportedType(String importedType) {
		int i = importedType.lastIndexOf('.');
		if (i > 0) {
			String pack = importedType.substring(0, i);
			if (!"java.lang".equals(pack)) {
				importedTypes.add(importedType);
			}
		}
	}
	
	public void addField(String field) {
		fields.add(field);
	}
	
	public void addMethod(String method) {
		methods.add(method);
	}
}
