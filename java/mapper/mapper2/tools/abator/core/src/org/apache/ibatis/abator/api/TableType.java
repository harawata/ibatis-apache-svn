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
package org.apache.ibatis.abator.api;

import org.apache.ibatis.abator.internal.db.ColumnDefinitions;
import org.apache.ibatis.abator.internal.util.messages.Messages;

/**
 * Typesafe enum (from "Effective Java" #21) of table types supported by Abator.
 * See package JavaDoc for more information.
 * 
 * @author Jeff Butler
 */
public class TableType {
    /**
     * 
     */
    private TableType() {
        super();
    }
    
	/**
	 * A table that has no primary key and all columns are not BLOBs.
	 */
    public static final TableType NO_PK_FIELDS_NO_BLOBS = new TableType();
    
	/**
	 * A table that has no primary key and columns that are both BLOBs and non BLOBs.
	 */
    public static final TableType NO_PK_FIELDS_BLOBS = new TableType();
    
	/**
	 * A table with a primary key only.
	 */
    public static final TableType PK_NO_FIELDS_NO_BLOBS = new TableType();
    
	/**
	 * A table with a primary key, and all other columns are not BLOBs.
	 */
    public static final TableType PK_FIELDS_NO_BLOBS = new TableType();
    
	/**
	 * A table with a primary key, and all other columns are BLOBs.
	 */
    public static final TableType PK_NO_FIELDS_BLOBS = new TableType();
    
	/**
	 * A table with a primary key and other columns that are both BLOBs and
	 * non BLOBs.
	 */
    public static final TableType PK_FIELDS_BLOBS = new TableType();

    /**
     * Calculates the table type from the physical structure of the table.
     * 
     * @param columnDefinitions the introspected table columns
     * @return the calculated TableType
     */
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
		    throw new RuntimeException(Messages.getString("RuntimeError.10")); //$NON-NLS-1$
		}
		
		return tableType;
    }
}
