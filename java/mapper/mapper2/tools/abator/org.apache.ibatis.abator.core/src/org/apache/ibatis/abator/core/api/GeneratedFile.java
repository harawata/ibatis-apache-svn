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

/**
 * Abstract class that holds information common to all generated files.
 * 
 * @author Jeff Butler
 */
public abstract class GeneratedFile {
	private String fileName;
	private String targetPackage;
	private String targetProject;

	/**
	 *  
	 */
	public GeneratedFile() {
		super();
	}

	/**
	 * This method returns the entire contents of the generated file.  Clients
	 * can simply save the value returned from this method as the file contents.
	 * Subclasses such as @see org.apache.ibatis.abator.core.api.GeneratedJavaFile
	 * offer more fine grained access to file parts, but still implement this method
	 * in the event that the entire contents are desired.
	 * 
	 * @return Returns the content.
	 */
	public abstract String getContent();
	
	/**
	 * Get the file name (without any path).  Clients should use this method to determine how to save
	 * the results.
	 * 
	 * @return Returns the file name.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the file name (without any path).  Clients should not call this method.
	 * 
	 * @param fileName
	 *            The file name to set.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Gets the target project.  This is useful for the eclipse plugin - it designates
	 * which project the file sould be placed in.  This property is set from the configuration,
	 * but is otherwise ignored in the Abator core. Clients can call this method to determine
	 * how to save the results.
	 * 
	 * @return the target project
	 */
	public String getTargetProject() {
		return targetProject;
	}

	/**
	 * Sets the target project.  This is useful for the eclipse plugin - it designates
	 * which project the file sould be placed in.  This property is set from the configuration,
	 * but is otherwise ignored in the Abator core. Clients should not call this method.
	 * 
	 * @param targetProject the target project
	 */
	public void setTargetProject(String targetProject) {
		this.targetProject = targetProject;
	}
	
	/**
	 * Get the target package for the file.  Clients should use this method to determine how to save
	 * the results.
	 * 
	 * @return Returns the target project.
	 */
	public String getTargetPackage() {
		return targetPackage;
	}

	/**
	 * Sets the target package for the file.  Clients should not call this method.
	 * 
	 * @param targetPackage the target package
	 */
	public void setTargetPackage(String targetPackage) {
		this.targetPackage = targetPackage;
	}
}