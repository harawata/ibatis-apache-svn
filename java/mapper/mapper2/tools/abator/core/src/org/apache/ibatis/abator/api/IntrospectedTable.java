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

import java.util.Iterator;

import org.apache.ibatis.abator.config.GeneratedKey;
import org.apache.ibatis.abator.internal.db.ColumnDefinition;

/**
 * Read only interface for dealing with introspected tables.
 * 
 * @author Jeff Butler
 *
 */
public interface IntrospectedTable {
    FullyQualifiedTable getTable();
    TableType getTableType();
    boolean isSelectByExampleStatementEnabled();
    boolean isDeleteByExampleStatementEnabled();
    boolean isSelectByPrimaryKeyStatementEnabled();
    boolean isUpdateByPrimaryKeyStatementEnabled();
    boolean isDeleteByPrimaryKeyStatementEnabled();
    boolean isInsertStatementEnabled();
    String getSelectByExampleQueryId();
    String getSelectByPrimaryKeyQueryId();
    GeneratedKey getGeneratedKey();

    ColumnDefinition getColumn(String columnName);
    
    /**
     * Returns an iterator containing every column in the table except for
     * BLOB columns.
     * 
     * @return iterator of ColumnDefinition objects
     */
    Iterator getNonBLOBColumns();
    
    /**
     * Returns an iterator containing every BLOB column in the table
     * (may be empty if the table doesn't have BLOBs).
     * 
     * @return iterator of ColumnDefinition objects
     */
    Iterator getBLOBColumns();

    /**
     * Returns an iterator containing every column in the table
     * 
     * @return iterator of ColumnDefinition objects
     */
    Iterator getAllColumns();
    
    /**
     * Returns an iterator containing every column in the table's
     * primary key (may be empty if the table doesn't have a primary key)
     * 
     * @return iterator of ColumnDefinition objects
     */
    Iterator getPrimaryKeyColumns();
    
    /**
     * Returns an iterator containing every column in the table
     * that is not in the primary key (including BLOB columns)
     * 
     * @return iterator of ColumnDefinition objects
     */
    Iterator getNonPrimaryKeyColumns();

    /**
     * Returns true if any of the columns in the table are JDBC Dates
     * (as opposed to timestamps).
     * 
     * @return
     */
    boolean hasJDBCDateColumns();

    /**
     * Returns true if any of the columns in the table are JDBC Times
     * (as opposed to timestamps).
     * 
     * @return
     */
    boolean hasJDBCTimeColumns();
}
