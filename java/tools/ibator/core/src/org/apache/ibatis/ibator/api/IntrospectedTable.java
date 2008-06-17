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

package org.apache.ibatis.ibator.api;

import java.util.List;

import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.config.GeneratedKey;
import org.apache.ibatis.ibator.config.IbatorContext;
import org.apache.ibatis.ibator.config.TableConfiguration;
import org.apache.ibatis.ibator.internal.db.ColumnDefinition;
import org.apache.ibatis.ibator.internal.db.ColumnDefinitions;
import org.apache.ibatis.ibator.internal.rules.IbatorRules;

/**
 * Read only interface for dealing with introspected tables.
 * 
 * @author Jeff Butler
 *
 */
public interface IntrospectedTable {
    FullyQualifiedTable getFullyQualifiedTable();
    String getSelectByExampleQueryId();
    String getSelectByPrimaryKeyQueryId();
    GeneratedKey getGeneratedKey();

    ColumnDefinition getColumn(String columnName);
    
    /**
     * Returns true if any of the columns in the table are JDBC Dates
     * (as opposed to timestamps).
     * 
     * @return true if the table contains DATE columns
     */
    boolean hasJDBCDateColumns();

    /**
     * Returns true if any of the columns in the table are JDBC Times
     * (as opposed to timestamps).
     * 
     * @return true if the table contains TIME columns
     */
    boolean hasJDBCTimeColumns();
    
    /**
     * Returns the columns in the primary key.  If the
     * generatePrimaryKeyClass() method returns false, then these
     * columns will be iterated as the parameters of the 
     * selectByPrimaryKay and deleteByPrimaryKey methods
     * 
     * @return a List of ColumnDefinition objects for
     *   columns in the primary key
     */
    List<ColumnDefinition> getPrimaryKeyColumns();
    
    boolean hasPrimaryKeyColumns();
    
    List<ColumnDefinition> getBaseColumns();
    
    /**
     * Returns all columns in the table (for use by the select by
     * primary key and select by example with BLOBs methods)
     * 
     * @return a List of ColumnDefinition objects for
     *   all columns in the table
     */
    List<ColumnDefinition> getAllColumns();
    
    /**
     * Returns all columns axcept BLOBs (for use by the select by
     * example without BLOBs method)
     * 
     * @return a List of ColumnDefinition objects for
     *   columns in the table that are non BLOBs
     */
    List<ColumnDefinition> getNonBLOBColumns();
    
    int getNonBLOBColumnCount();
    
    List<ColumnDefinition> getNonPrimaryKeyColumns();
    
    List<ColumnDefinition> getBLOBColumns();
    
    boolean hasBLOBColumns();
    
    boolean hasBaseColumns();
    
    IbatorRules getRules();
    
    String getTableConfigurationProperty(String property);

    FullyQualifiedJavaType getPrimaryKeyType();

    /**
     * 
     * @return the type for the record (the class that holds non-primary
     *  key and non-BLOB fields).  Note that
     *  the value will be calculated regardless of whether the table has these columns or not.
     */
    FullyQualifiedJavaType getBaseRecordType();

    /**
     * 
     * @return the type for the example class.
     */
    FullyQualifiedJavaType getExampleType();

    /**
     * 
     * @return the type for the record with BLOBs class.  Note that
     *  the value will be calculated regardless of whether the table has BLOB columns or not.
     */
    FullyQualifiedJavaType getRecordWithBLOBsType();

    String getSqlMapFileName();

    /**
     * Calculates the package for the current table.
     * 
     * @return the package for the SqlMap for the current table
     */
    String getSqlMapPackage();
    
    FullyQualifiedJavaType getDAOImplementationType();

    FullyQualifiedJavaType getDAOInterfaceType();

    boolean hasAnyColumns();
    
    void setTableConfiguration(TableConfiguration tableConfiguration);
    void setColumnDefinitions(ColumnDefinitions columnDefinitions);
    void setFullyQualifiedTable(FullyQualifiedTable fullyQualifiedTable);
    void setIbatorContext(IbatorContext ibatorContext);
}