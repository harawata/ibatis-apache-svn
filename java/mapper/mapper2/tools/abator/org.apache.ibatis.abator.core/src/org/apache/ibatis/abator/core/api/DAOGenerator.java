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

import java.util.Map;

import org.apache.ibatis.abator.core.config.TableConfiguration;
import org.apache.ibatis.abator.core.internal.db.ColumnDefinitions;

/**
 * The DAOGenerator interface describes the methods needed to generate DAO
 * objects for a table.
 * 
 * @author Jeff Butler
 */
public interface DAOGenerator {
	/**
	 * Sets unique properties for this instance. The properties consist of the
	 * default properties for the DAOGenerator, overridden and added to by any
	 * DAOGenerator properties that are unique to the current table.
	 * 
	 * This method will be called before any of the get methods.
	 * 
	 * @param properties
	 *            All properties from the configuration
	 */
	void setProperties(Map properties);

	/**
	 * Sets the target package for this instance. This value should be used to
	 * calculate the package for the DAO interface and implementation
	 * classes.
	 * 
	 * This method will be called before any of the get methods.
	 * 
	 * @param targetPackage
	 *            The target package from the configuration
	 */
	void setTargetPackage(String targetPackage);

	/**
	 * 
	 * @param targetProject
	 */
	void setTargetProject(String targetProject);
	
	/**
	 * Sets the instance of JavaModelGenerator associated with this instance.
	 * 
	 * This method will be called before any of the get methods.
	 * 
	 * @param javaModelGenerator
	 *            The JavaModelGenerator associated with this instance
	 */
	void setJavaModelGenerator(JavaModelGenerator javaModelGenerator);

	/**
	 * Sets the instance of SqlMapGenerator associated with this instance.
	 * 
	 * This method will be called before any of the get methods.
	 * 
	 * @param sqlMapGenerator
	 *            The SqlMapGenerator associated with this instance
	 */
	void setSqlMapGenerator(SqlMapGenerator sqlMapGenerator);

	/**
	 * Generate and return the DAOInterface for this table.
	 * 
	 * @param columnDefinitions introspected database information
	 * @param tableConfiguration the configuration associated with the current table
	 * @return a GeneratedJavaFile object holding the results of code generation
	 */
	GeneratedJavaFile getDAOInterface(ColumnDefinitions columnDefinitions,
			TableConfiguration tableConfiguration);

	/**
	 * Generate and return the DAOImplementation for this table.
	 * 
	 * @param columnDefinitions introspected database information
	 * @param tableConfiguration the configuration associated with the current table
	 * @return a GeneratedJavaFile object holding the results of code generation
	 */
	GeneratedJavaFile getDAOImplementation(ColumnDefinitions columnDefinitions,
			TableConfiguration tableConfiguration);
}
