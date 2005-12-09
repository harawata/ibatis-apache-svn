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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.abator.core.config.AbatorConfiguration;
import org.apache.ibatis.abator.core.config.AbatorContext;
import org.apache.ibatis.abator.core.exception.InvalidConfigurationException;
import org.apache.ibatis.abator.core.internal.NullProgressCallback;

/**
 * This class is the main interface to the Abator for iBATIS code generator.
 * A typical execution of the tool involves these steps:
 * 
 * <ol>
 * <li>Create a GeneratorConfiguration object. The GeneratorConfiguration can
 * be the result of a parsing the XML configuration file, or it can be created
 * solely in Java.</li>
 * <li>Create an Abator object</li>
 * <li>Call the generateFiles() method</li>
 * <li>Iterate through the list of GeneratedJavaFiles and GeneratedXmlFiles and save the
 *     contents to the files system.</li>
 * </ol>
 * 
 * @see org.apache.ibatis.abator.core.config.xml.AbatorConfigurationParser
 * 
 * @author Jeff Butler
 */
public class Abator {

	private AbatorConfiguration abatorConfiguration;

	private List generatedJavaFiles;
	private List generatedXmlFiles;
	private List warnings;

	/**
	 * Constructs an Abator object.
	 * 
	 * @param abatorConfiguration
	 *            The configuration for this run of Abator
	 * 
	 * @param warnings
	 *            Any warnings generated during execution will be added to this
	 *            list. Warnings do not affect the running of the tool, but they
	 *            may affect the results. A typical warning is an unsupported
	 *            data type. In that case, the column will be ignored and
	 *            generation will continue. Abator will only add Strings to the
	 *            list.
	 */
	public Abator(AbatorConfiguration abatorConfiguration, List warnings) {
		super();
		this.abatorConfiguration = abatorConfiguration;
		this.warnings = warnings;
		generatedJavaFiles = new ArrayList();
		generatedXmlFiles = new ArrayList();
	}

	/**
	 * Get the list of GeneratedJavaFile objects from the most recent
	 * run of the generator.  The list will be empty if the
	 * <code>generateFiles()</code> method has not been called. 
	 * 
	 * @return the list of GeneratedJavaFile objects.
	 */
	public List getGeneratedJavaFiles() {
		return generatedJavaFiles;
	}

	/**
	 * Get the list of GeneratedXmlFile objects from the most recent
	 * run of the generator.  The list will be empty if the
	 * <code>generateFiles()</code> method has not been called. 
	 * 
	 * @return the list of GeneratedXmlFile objects.
	 */
	public List getGeneratedXmlFiles() {
		return generatedXmlFiles;
	}
	
	public void generateFiles(ProgressCallback callback) throws InvalidConfigurationException,
		SQLException, InterruptedException {
	    
	    if (callback == null) {
	        callback = new NullProgressCallback();
	    }

	    generatedJavaFiles.clear();
		generatedXmlFiles.clear();
	    
		int totalSteps = 0;
		totalSteps++; // validation
		
		Iterator iter = abatorConfiguration.getAbatorContexts().iterator();
		while (iter.hasNext()) {
		    AbatorContext abatorContext = (AbatorContext) iter.next();
		    
		    totalSteps += abatorContext.getTotalSteps();
		}

		callback.setTotalSteps(totalSteps);

		callback.setTaskName("Validating Configuration");
		abatorConfiguration.validate();
		
		iter = abatorConfiguration.getAbatorContexts().iterator();
		while (iter.hasNext()) {
		    AbatorContext abatorContext = (AbatorContext) iter.next();
		    
		    abatorContext.generateFiles(callback, generatedJavaFiles, generatedXmlFiles, warnings);
		}
	}
}
