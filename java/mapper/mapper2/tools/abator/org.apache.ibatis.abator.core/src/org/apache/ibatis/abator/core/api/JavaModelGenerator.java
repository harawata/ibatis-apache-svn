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

import org.apache.ibatis.abator.core.config.FullyQualifiedTable;
import org.apache.ibatis.abator.core.config.TableConfiguration;
import org.apache.ibatis.abator.core.internal.db.ColumnDefinitions;

public interface JavaModelGenerator {
	/**
	 * 
	 * @param properties
	 */
	void setProperties(Map properties);

	/**
	 * 
	 * @param targetPackage
	 */
	void setTargetPackage(String targetPackage);

	/**
	 * 
	 * @param targetProject
	 */
	void setTargetProject(String targetProject);
	
	/**
	 * 
	 * @param table - the table for which the name should be generated
	 * @return - the short (unqualified) class name for the primary key.  Note that
	 *  the value will be calculated regardless of whether the table has a primary key or not.
	 */
	String getPrimaryKeyShortClassName(FullyQualifiedTable table);

	/**
	 * 
	 * @param table - the table for which the name should be generated
	 * @return - the short (unqualified) class name for the rcord (the class that holds non-primary
	 *  key and non-BLOB fields.  Note that
	 *  the value will be calculated regardless of whether the table has these columns or not.
	 */
	String getRecordShortClassName(FullyQualifiedTable table);

	/**
	 * 
	 * @param table - the table for which the name should be generated
	 * @return - the fully qualified class name for the primary key.  Note that
	 *  the value will be calculated regardless of whether the table has a primary key or not.
	 */
	String getPrimaryKeyFullyQualifiedClassName(FullyQualifiedTable table);

	/**
	 * 
	 * @param table - the table for which the name should be generated
	 * @return - the fully qualified class name for the rcord (the class that holds non-primary
	 *  key and non-BLOB fields.  Note that
	 *  the value will be calculated regardless of whether the table has these columns or not.
	 */
	String getRecordFullyQualifiedClassName(FullyQualifiedTable table);

	/**
	 * 
	 * @param table - the table for which the name should be generated
	 * @return - the short (unqualified) class name for the example class.
	 */
	String getExampleShortClassName(FullyQualifiedTable table);

	/**
	 * 
	 * @param table - the table for which the name should be generated
	 * @return - the fully qualified class name for the example class.
	 */
	String getExampleFullyQualifiedClassName(FullyQualifiedTable table);
	
	/**
	 * 
	 * @param table - the table for which the name should be generated
	 * @return - the short (unqualified) class name for the record with BLOBs class.  Note that
	 *  the value will be calculated regardless of whether the table has BLOB columns or not.
	 */
	String getRecordWithBLOBsShortClassName(FullyQualifiedTable table);
	
	/**
	 * 
	 * @param table - the table for which the name should be generated
	 * @return - the fully qualified class name for the record with BLOBs class.  Note that
	 *  the value will be calculated regardless of whether the table has BLOB columns or not.
	 */
	String getRecordWithBLOBsFullyQualifiedClassName(FullyQualifiedTable table);
	
	GeneratedJavaFile getPrimaryKey(ColumnDefinitions columnDefinitions,
			TableConfiguration tableConfiguration);
	GeneratedJavaFile getRecord(ColumnDefinitions columnDefinitions,
			TableConfiguration tableConfiguration);
	GeneratedJavaFile getExample(ColumnDefinitions columnDefinitions,
			TableConfiguration tableConfiguration);
	GeneratedJavaFile getRecordWithBLOBs(ColumnDefinitions columnDefinitions,
			TableConfiguration tableConfiguration);
}
