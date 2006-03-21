/*
 *  Copyright 2006 The Apache Software Foundation
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
package org.apache.ibatis.abator.internal.rules;

import org.apache.ibatis.abator.internal.db.ColumnDefinitions;

/**
 * Typesafe enum (from "Effective Java" #21) of table types supported by Abator.
 * See package JavaDoc for more information.
 * 
 * @author Jeff Butler
 */
public class TableType {
    private final String name;

    /**
     * 
     */
    private TableType(String name) {
        super();
        this.name = name;
    }
    
    public String toString() {
        return name;
    }
    
	/**
	 * A table that has no primary key and all columns are not BLOBs.
	 * Generate these objects/methods:
	 * <ul>
	 *   <li>Base Result Map (if either select method enabled)</li>
	 *   <li>Select By Example Method (if enabled)</li>
	 *   <li>Insert Method (if enabled)</li>
	 *   <li>Delete By Example Method (if enabled)</li>
	 *   <li>Record with No Superclass</li>
	 *   <li>Example class extending Record (if either "by example" method enabled)</li>
	 * </ul>
	 */
    public static final TableType NO_PK_FIELDS_NO_BLOBS = new TableType("No primary key, no BLOBs");
    
	/**
	 * A table that has no primary key and columns that are both BLOBs and non BLOBs.
	 * Generate these objects/methods:
	 * <ul>
	 *   <li>Base Result Map (if either select method enabled)</li>
	 *   <li>Result Map with BLOBs (if either select method enabled)</li>
	 *   <li>Select By Example Method (if enabled)</li>
	 *   <li>Select By Example with BLOBs Method (if enabled)</li>
	 *   <li>Insert Method (if enabled)</li>
	 *   <li>Delete By Example Method (if enabled)</li>
	 *   <li>Record with No Superclass</li>
	 *   <li>Record with BLOBS extending Record</li>
	 *   <li>Example class extending Record (if either "by example" method enabled)</li>
	 * </ul>
	 */
    public static final TableType NO_PK_FIELDS_BLOBS = new TableType("No primary key, contains BLOBs and non-BLOBs");
    
	/**
	 * A table with a primary key only.
	 * Generate these objects/methods:
	 * <ul>
	 *   <li>Base Result Map (if either select method enabled)</li>
	 *   <li>Select By Example Method (if enabled)</li>
	 *   <li>Insert Method (if enabled)</li>
	 *   <li>Delete By Primary Key Method (if enabled)</li>
	 *   <li>Delete By Example Method (if enabled)</li>
	 *   <li>Primary Key</li>
	 *   <li>Example class extending Primary Key (if either "by example" method enabled)</li>
	 * </ul>
	 */
    public static final TableType PK_NO_FIELDS_NO_BLOBS = new TableType("Primary key only");
    
	/**
	 * A table with a primary key, and all other columns are not BLOBs.
	 * Generate these objects/methods:
	 * <ul>
	 *   <li>Base Result Map (if either select method enabled)</li>
	 *   <li>Select By Example Method (if enabled)</li>
	 *   <li>Select By Primary Key Method (if enabled)</li>
	 *   <li>Insert Method (if enabled)</li>
	 *   <li>Update By Primary Key (No BLOBs) Method (if enabled)</li>
	 *   <li>Delete By Primary Key Method (if enabled)</li>
	 *   <li>Delete By Example Method (if enabled)</li>
	 *   <li>Primary Key</li>
	 *   <li>Record class extending Primary Key</li>
	 *   <li>Example class extending Record class (if either "by example" method enabled)</li>
	 * </ul>
	 */
    public static final TableType PK_FIELDS_NO_BLOBS = new TableType("Primary key, all other fields are not BLOBs");
    
	/**
	 * A table with a primary key, and all other columns are BLOBs.
	 * Generate these objects/methods:
	 * <ul>
	 *   <li>Base Result Map (if either select method enabled)</li>
	 *   <li>Result Map with BLOBs (if either select method enabled)</li>
	 *   <li>Select By Example Method (if enabled)</li>
	 *   <li>Select By Example with BLOBs Method (if enabled)</li>
	 *   <li>Select By Primary Key Method (if enabled)</li>
	 *   <li>Insert Method (if enabled)</li>
	 *   <li>Update By Primary Key (BLOBs) Method (if enabled)</li>
	 *   <li>Delete By Primary Key Method (if enabled)</li>
	 *   <li>Delete By Example Method (if enabled)</li>
	 *   <li>Primary Key</li>
	 *   <li>Record with BLOBs class extending Primary Key</li>
	 *   <li>Example class extending primary key class (if either "by example" method enabled)</li>
	 * </ul>
	 */
    public static final TableType PK_NO_FIELDS_BLOBS = new TableType("Primary key, all other fields are BLOBs");
    
	/**
	 * A table with a primary key and other columns that are both BLOBs and
	 * non BLOBs.
	 * Generate these objects/methods:
	 * <ul>
	 *   <li>Base Result Map (if either select method enabled)</li>
	 *   <li>Result Map with BLOBs (if either select method enabled)</li>
	 *   <li>Select By Example Method (if enabled)</li>
	 *   <li>Select By Example with BLOBs Method (if enabled)</li>
	 *   <li>Select By Primary Key Method (if enabled)</li>
	 *   <li>Insert Method (if enabled)</li>
	 *   <li>Update By Primary Key (BLOBs) Method (if enabled)</li>
	 *   <li>Update By Primary Key (no BLOBs) Method (if enabled)</li>
	 *   <li>Delete By Primary Key Method (if enabled)</li>
	 *   <li>Delete By Example Method (if enabled)</li>
	 *   <li>Primary Key</li>
	 *   <li>Record class extending Primary Key</li>
	 *   <li>Record with BLOBs class extending record class</li>
	 *   <li>Example class extending record class (if either "by example" method enabled)</li>
	 * </ul>
	 */
    public static final TableType PK_FIELDS_BLOBS = new TableType("Primary key, BLOB and non BLOB fields");
    
    public static TableType calculateTableType(ColumnDefinitions columnDefinitions) {
        TableType tableType;
        
		if (!columnDefinitions.hasPrimaryKey()
				&& !columnDefinitions.hasBLOBColumns()
				&& columnDefinitions.hasNonBLOBColumns()) {
			tableType = NO_PK_FIELDS_NO_BLOBS;
		} else if (!columnDefinitions.hasPrimaryKey() 
				&& columnDefinitions.hasBLOBColumns()
				&& columnDefinitions.hasNonBLOBColumns()) {
			tableType = NO_PK_FIELDS_BLOBS;
		} else if (columnDefinitions.hasPrimaryKey()
				&& !columnDefinitions.hasBLOBColumns()
				&& !columnDefinitions.hasNonBLOBColumns()) {
			tableType = PK_NO_FIELDS_NO_BLOBS;
		} else if (columnDefinitions.hasPrimaryKey()
				&& !columnDefinitions.hasBLOBColumns()
				&& columnDefinitions.hasNonBLOBColumns()) {
			tableType = PK_FIELDS_NO_BLOBS;
		} else if (columnDefinitions.hasPrimaryKey()
				&& columnDefinitions.hasBLOBColumns()
				&& !columnDefinitions.hasNonBLOBColumns()) {
			tableType = PK_NO_FIELDS_BLOBS;
		} else if (columnDefinitions.hasPrimaryKey()
				&& columnDefinitions.hasBLOBColumns()
				&& columnDefinitions.hasNonBLOBColumns()) {
			tableType = PK_FIELDS_BLOBS;
		} else {
		    throw new RuntimeException("Internal Error - Unsupported Table Type");
		}
		
		return tableType;
    }
}
