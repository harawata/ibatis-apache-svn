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

package org.apache.ibatis.abator.internal.db;

import java.util.Iterator;

import org.apache.ibatis.abator.api.FullyQualifiedTable;
import org.apache.ibatis.abator.api.IntrospectedTable;
import org.apache.ibatis.abator.api.TableType;
import org.apache.ibatis.abator.config.GeneratedKey;
import org.apache.ibatis.abator.config.TableConfiguration;

/**
 * @author Jeff Butler
 *
 */
public class IntrospectedTableImpl implements IntrospectedTable {

    private TableConfiguration tableConfiguration;
    private ColumnDefinitions columnDefinitions;
    private TableType tableType;
    private FullyQualifiedTable table;
    
    /**
     * 
     */
    public IntrospectedTableImpl(TableConfiguration tableConfiguration, ColumnDefinitions columnDefinitions,
            FullyQualifiedTable table) {
        super();
        this.columnDefinitions = columnDefinitions;
        this.tableConfiguration = tableConfiguration;
        this.table = table;
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.api.IntrospectedTable#getTable()
     */
    public FullyQualifiedTable getTable() {
        return table;
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.api.IntrospectedTable#getTableType()
     */
    public TableType getTableType() {
        if (tableType == null) {
            tableType = TableType.calculateTableType(columnDefinitions);
        }
        return tableType;
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.api.IntrospectedTable#isSelectByExampleStatementEnabled()
     */
    public boolean isSelectByExampleStatementEnabled() {
        return tableConfiguration.isSelectByExampleStatementEnabled();
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.api.IntrospectedTable#isDeleteByExampleStatementEnabled()
     */
    public boolean isDeleteByExampleStatementEnabled() {
        return tableConfiguration.isDeleteByExampleStatementEnabled();
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.api.IntrospectedTable#isSelectByPrimaryKeyStatementEnabled()
     */
    public boolean isSelectByPrimaryKeyStatementEnabled() {
        return tableConfiguration.isSelectByPrimaryKeyStatementEnabled();
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.api.IntrospectedTable#isUpdateByPrimaryKeyStatementEnabled()
     */
    public boolean isUpdateByPrimaryKeyStatementEnabled() {
        return tableConfiguration.isUpdateByPrimaryKeyStatementEnabled();
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.api.IntrospectedTable#isDeleteByPrimaryKeyStatementEnabled()
     */
    public boolean isDeleteByPrimaryKeyStatementEnabled() {
        return tableConfiguration.isDeleteByPrimaryKeyStatementEnabled();
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.api.IntrospectedTable#isInsertStatementEnabled()
     */
    public boolean isInsertStatementEnabled() {
        return tableConfiguration.isInsertStatementEnabled();
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.api.IntrospectedTable#getSelectByExampleQueryId()
     */
    public String getSelectByExampleQueryId() {
        return tableConfiguration.getSelectByExampleQueryId();
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.api.IntrospectedTable#getSelectByPrimaryKeyQueryId()
     */
    public String getSelectByPrimaryKeyQueryId() {
        return tableConfiguration.getSelectByPrimaryKeyQueryId();
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.api.IntrospectedTable#getGeneratedKey()
     */
    public GeneratedKey getGeneratedKey() {
        return tableConfiguration.getGeneratedKey();
    }

    public Iterator getAllColumns() {
        return columnDefinitions.getAllColumns().iterator();
    }

    public Iterator getBLOBColumns() {
        return columnDefinitions.getBLOBColumns().iterator();
    }

    public ColumnDefinition getColumn(String columnName) {
        return columnDefinitions.getColumn(columnName);
    }

    public Iterator getNonBLOBColumns() {
        return columnDefinitions.getNonBLOBColumns().iterator();
    }

    public Iterator getNonPrimaryKeyColumns() {
        return columnDefinitions.getNonPrimaryKeyColumns().iterator();
    }

    public Iterator getPrimaryKeyColumns() {
        return columnDefinitions.getPrimaryKey().iterator();
    }

    public boolean hasJDBCDateColumns() {
        return columnDefinitions.hasJDBCDateColumns();
    }

    public boolean hasJDBCTimeColumns() {
        return columnDefinitions.hasJDBCTimeColumns();
    }

    public ColumnDefinitions getColumnDefinitions() {
        return columnDefinitions;
    }
}
