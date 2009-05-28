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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.config.DAOGeneratorConfiguration;
import org.apache.ibatis.ibator.config.GeneratedKey;
import org.apache.ibatis.ibator.config.IbatorContext;
import org.apache.ibatis.ibator.config.JavaModelGeneratorConfiguration;
import org.apache.ibatis.ibator.config.MergeConstants;
import org.apache.ibatis.ibator.config.ModelType;
import org.apache.ibatis.ibator.config.PropertyRegistry;
import org.apache.ibatis.ibator.config.SqlMapGeneratorConfiguration;
import org.apache.ibatis.ibator.config.TableConfiguration;
import org.apache.ibatis.ibator.internal.rules.ConditionalModelRules;
import org.apache.ibatis.ibator.internal.rules.FlatModelRules;
import org.apache.ibatis.ibator.internal.rules.HierarchicalModelRules;
import org.apache.ibatis.ibator.internal.rules.IbatorRules;
import org.apache.ibatis.ibator.internal.util.StringUtility;

/**
 * Base class for all code generator implementations.
 * This class provides many of the Ibator housekeeping methods needed
 * to implement a code generator, with only the actual code generation
 * methods left unimplemented.
 * 
 * @author Jeff Butler
 *
 */
public abstract class IntrospectedTable {
    protected enum InternalAttribute {
        ATTR_DAO_IMPLEMENTATION_TYPE,
        ATTR_DAO_INTERFACE_TYPE,
        ATTR_PRIMARY_KEY_TYPE,
        ATTR_BASE_RECORD_TYPE,
        ATTR_RECORD_WITH_BLOBS_TYPE,
        ATTR_EXAMPLE_TYPE,
        ATTR_SQL_MAP_PACKAGE,
        ATTR_SQL_MAP_FILE_NAME,
        ATTR_SQL_MAP_NAMESPACE,
        ATTR_SQL_MAP_FULLY_QUALIFIED_TABLE_NAME_AT_RUNTIME,
        ATTR_SQL_MAP_ALIASED_FULLY_QUALIFIED_TABLE_NAME_AT_RUNTIME,
        ATTR_COUNT_BY_EXAMPLE_STATEMENT_ID,
        ATTR_DELETE_BY_EXAMPLE_STATEMENT_ID,
        ATTR_DELETE_BY_PRIMARY_KEY_STATEMENT_ID,
        ATTR_INSERT_STATEMENT_ID,
        ATTR_INSERT_SELECTIVE_STATEMENT_ID,
        ATTR_SELECT_BY_EXAMPLE_STATEMENT_ID,
        ATTR_SELECT_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID,
        ATTR_SELECT_BY_PRIMARY_KEY_STATEMENT_ID,
        ATTR_UPDATE_BY_EXAMPLE_STATEMENT_ID,
        ATTR_UPDATE_BY_EXAMPLE_SELECTIVE_STATEMENT_ID,
        ATTR_UPDATE_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID,
        ATTR_UPDATE_BY_PRIMARY_KEY_STATEMENT_ID,
        ATTR_UPDATE_BY_PRIMARY_KEY_SELECTIVE_STATEMENT_ID,
        ATTR_UPDATE_BY_PRIMARY_KEY_WITH_BLOBS_STATEMENT_ID,
        ATTR_BASE_RESULT_MAP_ID,
        ATTR_RESULT_MAP_WITH_BLOBS_ID,
        ATTR_EXAMPLE_WHERE_CLAUSE_ID,
        ATTR_BASE_COLUMN_LIST_ID,
        ATTR_BLOB_COLUMN_LIST_ID
    }
    
    protected TableConfiguration tableConfiguration;
    protected FullyQualifiedTable fullyQualifiedTable;
    protected IbatorContext ibatorContext;
    protected IbatorRules rules;
    protected List<IntrospectedColumn> primaryKeyColumns;
    protected List<IntrospectedColumn> baseColumns;
    protected List<IntrospectedColumn> blobColumns;
    
    /**
     * Attributes may be used by plugins to capture table related state
     * between the different plugin calls.
     */
    protected Map<String, Object> attributes;
    
    /**
     * Internal attributes are used
     * to store commonly accessed items by all code generators
     */
    protected Map<IntrospectedTable.InternalAttribute, Object> internalAttributes;
    
    public IntrospectedTable() {
        super();
        primaryKeyColumns = new ArrayList<IntrospectedColumn>();
        baseColumns = new ArrayList<IntrospectedColumn>();
        blobColumns = new ArrayList<IntrospectedColumn>();
        attributes = new HashMap<String, Object>();
        internalAttributes = new HashMap<IntrospectedTable.InternalAttribute, Object>();
    }
    
    public FullyQualifiedTable getFullyQualifiedTable() {
        return fullyQualifiedTable;
    }
    
    public String getSelectByExampleQueryId() {
        return tableConfiguration.getSelectByExampleQueryId();
    }
    
    public String getSelectByPrimaryKeyQueryId() {
        return tableConfiguration.getSelectByPrimaryKeyQueryId();
    }

    public GeneratedKey getGeneratedKey() {
        return tableConfiguration.getGeneratedKey();
    }

    public IntrospectedColumn getColumn(String columnName) {
        if (columnName == null) {
            return null;
        } else {
            // search primary key columns
            for (IntrospectedColumn introspectedColumn : primaryKeyColumns) {
                if (introspectedColumn.isColumnNameDelimited()) {
                    if (introspectedColumn.getActualColumnName().equals(columnName)) {
                        return introspectedColumn;
                    }
                } else {
                    if (introspectedColumn.getActualColumnName().equalsIgnoreCase(columnName)) {
                        return introspectedColumn;
                    }
                }
            }
            
            // search base columns
            for (IntrospectedColumn introspectedColumn : baseColumns) {
                if (introspectedColumn.isColumnNameDelimited()) {
                    if (introspectedColumn.getActualColumnName().equals(columnName)) {
                        return introspectedColumn;
                    }
                } else {
                    if (introspectedColumn.getActualColumnName().equalsIgnoreCase(columnName)) {
                        return introspectedColumn;
                    }
                }
            }

            // search blob columns
            for (IntrospectedColumn introspectedColumn : blobColumns) {
                if (introspectedColumn.isColumnNameDelimited()) {
                    if (introspectedColumn.getActualColumnName().equals(columnName)) {
                        return introspectedColumn;
                    }
                } else {
                    if (introspectedColumn.getActualColumnName().equalsIgnoreCase(columnName)) {
                        return introspectedColumn;
                    }
                }
            }
            
            return null;
        }
    }
    
    /**
     * Returns true if any of the columns in the table are JDBC Dates
     * (as opposed to timestamps).
     * 
     * @return true if the table contains DATE columns
     */
    public boolean hasJDBCDateColumns() {
        boolean rc = false;
        
        for (IntrospectedColumn introspectedColumn : primaryKeyColumns) {
            if (introspectedColumn.isJDBCDateColumn()) {
                rc = true;
                break;
            }
        }
        
        if (!rc) {
            for (IntrospectedColumn introspectedColumn : baseColumns) {
                if (introspectedColumn.isJDBCDateColumn()) {
                    rc = true;
                    break;
                }
            }
        }
        
        return rc;
    }

    /**
     * Returns true if any of the columns in the table are JDBC Times
     * (as opposed to timestamps).
     * 
     * @return true if the table contains TIME columns
     */
    public boolean hasJDBCTimeColumns() {
        boolean rc = false;
        
        for (IntrospectedColumn introspectedColumn : primaryKeyColumns) {
            if (introspectedColumn.isJDBCTimeColumn()) {
                rc = true;
                break;
            }
        }
        
        if (!rc) {
            for (IntrospectedColumn introspectedColumn : baseColumns) {
                if (introspectedColumn.isJDBCTimeColumn()) {
                    rc = true;
                    break;
                }
            }
        }
        
        return rc;
    }
    
    /**
     * Returns the columns in the primary key.  If the
     * generatePrimaryKeyClass() method returns false, then these
     * columns will be iterated as the parameters of the 
     * selectByPrimaryKay and deleteByPrimaryKey methods
     * 
     * @return a List of ColumnDefinition objects for
     *   columns in the primary key
     */
    public List<IntrospectedColumn> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }
    
    public boolean hasPrimaryKeyColumns() {
        return primaryKeyColumns.size() > 0;
    }
    
    public List<IntrospectedColumn> getBaseColumns() {
        return baseColumns;
    }
    
    /**
     * Returns all columns in the table (for use by the select by
     * primary key and select by example with BLOBs methods)
     * 
     * @return a List of ColumnDefinition objects for
     *   all columns in the table
     */
    public List<IntrospectedColumn> getAllColumns() {
        List<IntrospectedColumn> answer = new ArrayList<IntrospectedColumn>();
        answer.addAll(primaryKeyColumns);
        answer.addAll(baseColumns);
        answer.addAll(blobColumns);
        
        return answer;
    }
    
    /**
     * Returns all columns except BLOBs (for use by the select by
     * example without BLOBs method)
     * 
     * @return a List of ColumnDefinition objects for
     *   columns in the table that are non BLOBs
     */
    public List<IntrospectedColumn> getNonBLOBColumns() {
        List<IntrospectedColumn> answer = new ArrayList<IntrospectedColumn>();
        answer.addAll(primaryKeyColumns);
        answer.addAll(baseColumns);
        
        return answer;
    }
    
    public int getNonBLOBColumnCount() {
        return primaryKeyColumns.size()
            + baseColumns.size();
    }
    
    public List<IntrospectedColumn> getNonPrimaryKeyColumns() {
        List<IntrospectedColumn> answer = new ArrayList<IntrospectedColumn>();
        answer.addAll(baseColumns);
        answer.addAll(blobColumns);
        
        return answer;
    }
    
    public List<IntrospectedColumn> getBLOBColumns() {
        return blobColumns;
    }
    
    public boolean hasBLOBColumns() {
        return blobColumns.size() > 0;
    }
    
    public boolean hasBaseColumns() {
        return baseColumns.size() > 0;
    }
    
    public IbatorRules getRules() {
        return rules;
    }
    
    public String getTableConfigurationProperty(String property) {
        return tableConfiguration.getProperty(property);
    }

    public FullyQualifiedJavaType getPrimaryKeyType() {
        return (FullyQualifiedJavaType) internalAttributes.get(InternalAttribute.ATTR_PRIMARY_KEY_TYPE);
    }

    /**
     * 
     * @return the type for the record (the class that holds non-primary
     *  key and non-BLOB fields).  Note that
     *  the value will be calculated regardless of whether the table has these columns or not.
     */
    public FullyQualifiedJavaType getBaseRecordType() {
        return (FullyQualifiedJavaType) internalAttributes.get(InternalAttribute.ATTR_BASE_RECORD_TYPE);
    }

    /**
     * 
     * @return the type for the example class.
     */
    public FullyQualifiedJavaType getExampleType() {
        return (FullyQualifiedJavaType) internalAttributes.get(InternalAttribute.ATTR_EXAMPLE_TYPE);
    }

    /**
     * 
     * @return the type for the record with BLOBs class.  Note that
     *  the value will be calculated regardless of whether the table has BLOB columns or not.
     */
    public FullyQualifiedJavaType getRecordWithBLOBsType() {
        return (FullyQualifiedJavaType) internalAttributes.get(InternalAttribute.ATTR_RECORD_WITH_BLOBS_TYPE);
    }

    /**
     * Calculates an SQL Map file name for the table. Typically the name is
     * "XXXX_SqlMap.xml" where XXXX is the fully qualified table name (delimited
     * with underscores).
     * 
     * @return the name of the SqlMap file
     */
    public String getSqlMapFileName() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_SQL_MAP_FILE_NAME);
    }
    
    public String getSqlMapNamespace() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_SQL_MAP_NAMESPACE);
    }

    /**
     * Calculates the package for the current table.
     * 
     * @return the package for the SqlMap for the current table
     */
    public String getSqlMapPackage() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_SQL_MAP_PACKAGE);
    }
    
    public FullyQualifiedJavaType getDAOImplementationType() {
        return (FullyQualifiedJavaType) internalAttributes.get(InternalAttribute.ATTR_DAO_IMPLEMENTATION_TYPE);
    }

    public FullyQualifiedJavaType getDAOInterfaceType() {
        return (FullyQualifiedJavaType) internalAttributes.get(InternalAttribute.ATTR_DAO_INTERFACE_TYPE);
    }

    public boolean hasAnyColumns() {
        return primaryKeyColumns.size() > 0
            || baseColumns.size() > 0
            || blobColumns.size() > 0;
    }
    
    public void setTableConfiguration(TableConfiguration tableConfiguration) {
        this.tableConfiguration = tableConfiguration;
    }

    public void setFullyQualifiedTable(FullyQualifiedTable fullyQualifiedTable) {
        this.fullyQualifiedTable = fullyQualifiedTable;
    }
    
    public void setIbatorContext(IbatorContext ibatorContext) {
        this.ibatorContext = ibatorContext;
    }

    public void addColumn(IntrospectedColumn introspectedColumn) {
        if (introspectedColumn.isBLOBColumn()) {
            blobColumns.add(introspectedColumn);
        } else {
            baseColumns.add(introspectedColumn);
        }
        
        introspectedColumn.setIntrospectedTable(this);
    }
    
    public void addPrimaryKeyColumn(String columnName) {
        boolean found = false;
        // first search base columns
        Iterator<IntrospectedColumn> iter = baseColumns.iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();
            if (introspectedColumn.getActualColumnName().equals(columnName)) {
                primaryKeyColumns.add(introspectedColumn);
                iter.remove();
                found = true;
                break;
            }
        }
        
        // search blob columns in the weird event that a blob is the primary key
        if (!found) {
            iter = blobColumns.iterator();
            while (iter.hasNext()) {
                IntrospectedColumn introspectedColumn = iter.next();
                if (introspectedColumn.getActualColumnName().equals(columnName)) {
                    primaryKeyColumns.add(introspectedColumn);
                    iter.remove();
                    found = true;
                    break;
                }
            }
        }
    }
    
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
    
    public void removeAttribute(String name) {
        attributes.remove(name);
    }
    
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }
    
    public void initialize() {
        calculateDAOAttributes();
        calculateModelAttributes();
        calculateXmlAttributes();
        
        if (tableConfiguration.getModelType() == ModelType.HIERARCHICAL) {
            rules = new HierarchicalModelRules(this);
        } else if (tableConfiguration.getModelType() == ModelType.FLAT) {
            rules = new FlatModelRules(this);
        } else {
            rules = new ConditionalModelRules(this);
        }
        
        ibatorContext.getPlugins().initialized(this);
    }

    /**
     * 
     */
    protected void calculateXmlAttributes() {
        setSqlMapPackage(calculateSqlMapPackage());
        setSqlMapFileName(calculateSqlMapFileName());
        setSqlMapNamespace(calculateSqlMapNamespace());
        setSqlMapFullyQualifiedRuntimeTableName(calculateSqlMapFullyQualifiedRuntimeTableName());
        setSqlMapAliasedFullyQualifiedRuntimeTableName(calculateSqlMapAliasedFullyQualifiedRuntimeTableName());

        StringBuilder sb = new StringBuilder();

        sb.append(MergeConstants.NEW_XML_ELEMENT_PREFIX);
        sb.append("countByExample"); //$NON-NLS-1$
        setCountByExampleStatementId(sb.toString());

        sb.setLength(0);
        sb.append(MergeConstants.NEW_XML_ELEMENT_PREFIX);
        sb.append("deleteByExample"); //$NON-NLS-1$
        setDeleteByExampleStatementId(sb.toString());
        
        sb.setLength(0);
        sb.append(MergeConstants.NEW_XML_ELEMENT_PREFIX);
        sb.append("deleteByPrimaryKey"); //$NON-NLS-1$
        setDeleteByPrimaryKeyStatementId(sb.toString());
        
        sb.setLength(0);
        sb.append(MergeConstants.NEW_XML_ELEMENT_PREFIX);
        sb.append("insert"); //$NON-NLS-1$
        setInsertStatementId(sb.toString());
        
        sb.setLength(0);
        sb.append(MergeConstants.NEW_XML_ELEMENT_PREFIX);
        sb.append("insertSelective"); //$NON-NLS-1$
        setInsertSelectiveStatementId(sb.toString());
        
        sb.setLength(0);
        sb.append(MergeConstants.NEW_XML_ELEMENT_PREFIX);
        sb.append("selectByExample"); //$NON-NLS-1$
        setSelectByExampleStatementId(sb.toString());

        sb.setLength(0);
        sb.append(MergeConstants.NEW_XML_ELEMENT_PREFIX);
        sb.append("selectByExampleWithBLOBs"); //$NON-NLS-1$
        setSelectByExampleWithBLOBsStatementId(sb.toString());

        sb.setLength(0);
        sb.append(MergeConstants.NEW_XML_ELEMENT_PREFIX);
        sb.append("selectByPrimaryKey"); //$NON-NLS-1$
        setSelectByPrimaryKeyStatementId(sb.toString());

        sb.setLength(0);
        sb.append(MergeConstants.NEW_XML_ELEMENT_PREFIX);
        sb.append("updateByExample"); //$NON-NLS-1$
        setUpdateByExampleStatementId(sb.toString());

        sb.setLength(0);
        sb.append(MergeConstants.NEW_XML_ELEMENT_PREFIX);
        sb.append("updateByExampleSelective"); //$NON-NLS-1$
        setUpdateByExampleSelectiveStatementId(sb.toString());

        sb.setLength(0);
        sb.append(MergeConstants.NEW_XML_ELEMENT_PREFIX);
        sb.append("updateByExampleWithBLOBs"); //$NON-NLS-1$
        setUpdateByExampleWithBLOBsStatementId(sb.toString());

        sb.setLength(0);
        sb.append(MergeConstants.NEW_XML_ELEMENT_PREFIX);
        sb.append("updateByPrimaryKey"); //$NON-NLS-1$
        setUpdateByPrimaryKeyStatementId(sb.toString());

        sb.setLength(0);
        sb.append(MergeConstants.NEW_XML_ELEMENT_PREFIX);
        sb.append("updateByPrimaryKeySelective"); //$NON-NLS-1$
        setUpdateByPrimaryKeySelectiveStatementId(sb.toString());

        sb.setLength(0);
        sb.append(MergeConstants.NEW_XML_ELEMENT_PREFIX);
        sb.append("updateByPrimaryKeyWithBLOBs"); //$NON-NLS-1$
        setUpdateByPrimaryKeyWithBLOBsStatementId(sb.toString());

        sb.setLength(0);
        sb.append(MergeConstants.NEW_XML_ELEMENT_PREFIX);
        sb.append("BaseResultMap"); //$NON-NLS-1$
        setBaseResultMapId(sb.toString());

        sb.setLength(0);
        sb.append(MergeConstants.NEW_XML_ELEMENT_PREFIX);
        sb.append("ResultMapWithBLOBs"); //$NON-NLS-1$
        setResultMapWithBLOBsId(sb.toString());

        sb.setLength(0);
        sb.append(MergeConstants.NEW_XML_ELEMENT_PREFIX);
        sb.append("Example_Where_Clause"); //$NON-NLS-1$
        setExampleWhereClauseId(sb.toString());

        sb.setLength(0);
        sb.append(MergeConstants.NEW_XML_ELEMENT_PREFIX);
        sb.append("Base_Column_List"); //$NON-NLS-1$
        setBaseColumnListId(sb.toString());

        sb.setLength(0);
        sb.append(MergeConstants.NEW_XML_ELEMENT_PREFIX);
        sb.append("Blob_Column_List"); //$NON-NLS-1$
        setBlobColumnListId(sb.toString());
    }

    public void setBlobColumnListId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_BLOB_COLUMN_LIST_ID, s);
    }

    public void setBaseColumnListId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_BASE_COLUMN_LIST_ID, s);
    }

    public void setExampleWhereClauseId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_EXAMPLE_WHERE_CLAUSE_ID, s);
    }

    public void setResultMapWithBLOBsId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_RESULT_MAP_WITH_BLOBS_ID, s);
    }

    public void setBaseResultMapId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_BASE_RESULT_MAP_ID, s);
    }

    public void setUpdateByPrimaryKeyWithBLOBsStatementId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_UPDATE_BY_PRIMARY_KEY_WITH_BLOBS_STATEMENT_ID, s);
    }

    public void setUpdateByPrimaryKeySelectiveStatementId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_UPDATE_BY_PRIMARY_KEY_SELECTIVE_STATEMENT_ID, s);
    }

    public void setUpdateByPrimaryKeyStatementId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_UPDATE_BY_PRIMARY_KEY_STATEMENT_ID, s);
    }

    public void setUpdateByExampleWithBLOBsStatementId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_UPDATE_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID, s);
    }

    public void setUpdateByExampleSelectiveStatementId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_UPDATE_BY_EXAMPLE_SELECTIVE_STATEMENT_ID, s);
    }

    public void setUpdateByExampleStatementId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_UPDATE_BY_EXAMPLE_STATEMENT_ID, s);
    }

    public void setSelectByPrimaryKeyStatementId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_SELECT_BY_PRIMARY_KEY_STATEMENT_ID, s);
    }

    public void setSelectByExampleWithBLOBsStatementId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_SELECT_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID, s);
    }

    public void setSelectByExampleStatementId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_SELECT_BY_EXAMPLE_STATEMENT_ID, s);
    }

    private void setInsertSelectiveStatementId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_INSERT_SELECTIVE_STATEMENT_ID, s);
    }

    public void setInsertStatementId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_INSERT_STATEMENT_ID, s);
    }

    public void setDeleteByPrimaryKeyStatementId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_DELETE_BY_PRIMARY_KEY_STATEMENT_ID, s);
    }

    public void setDeleteByExampleStatementId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_DELETE_BY_EXAMPLE_STATEMENT_ID, s);
    }

    public void setCountByExampleStatementId(String s) {
        internalAttributes.put(InternalAttribute.ATTR_COUNT_BY_EXAMPLE_STATEMENT_ID, s);
    }
    
    public String getBlobColumnListId() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_BLOB_COLUMN_LIST_ID);
    }

    public String getBaseColumnListId() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_BASE_COLUMN_LIST_ID);
    }

    public String getExampleWhereClauseId() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_EXAMPLE_WHERE_CLAUSE_ID);
    }

    public String getResultMapWithBLOBsId() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_RESULT_MAP_WITH_BLOBS_ID);
    }

    public String getBaseResultMapId() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_BASE_RESULT_MAP_ID);
    }

    public String getUpdateByPrimaryKeyWithBLOBsStatementId() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_UPDATE_BY_PRIMARY_KEY_WITH_BLOBS_STATEMENT_ID);
    }

    public String getUpdateByPrimaryKeySelectiveStatementId() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_UPDATE_BY_PRIMARY_KEY_SELECTIVE_STATEMENT_ID);
    }

    public String getUpdateByPrimaryKeyStatementId() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_UPDATE_BY_PRIMARY_KEY_STATEMENT_ID);
    }

    public String getUpdateByExampleWithBLOBsStatementId() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_UPDATE_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID);
    }

    public String getUpdateByExampleSelectiveStatementId() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_UPDATE_BY_EXAMPLE_SELECTIVE_STATEMENT_ID);
    }

    public String getUpdateByExampleStatementId() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_UPDATE_BY_EXAMPLE_STATEMENT_ID);
    }

    public String getSelectByPrimaryKeyStatementId() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_SELECT_BY_PRIMARY_KEY_STATEMENT_ID);
    }

    public String getSelectByExampleWithBLOBsStatementId() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_SELECT_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID);
    }

    public String getSelectByExampleStatementId() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_SELECT_BY_EXAMPLE_STATEMENT_ID);
    }

    public String getInsertSelectiveStatementId() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_INSERT_SELECTIVE_STATEMENT_ID);
    }

    public String getInsertStatementId() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_INSERT_STATEMENT_ID);
    }

    public String getDeleteByPrimaryKeyStatementId() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_DELETE_BY_PRIMARY_KEY_STATEMENT_ID);
    }

    public String getDeleteByExampleStatementId() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_DELETE_BY_EXAMPLE_STATEMENT_ID);
    }

    public String getCountByExampleStatementId() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_COUNT_BY_EXAMPLE_STATEMENT_ID);
    }
    
    protected String calculateDAOImplementationPackage() {
        DAOGeneratorConfiguration config = ibatorContext.getDaoGeneratorConfiguration();
        if (config == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        if (StringUtility.stringHasValue(config.getImplementationPackage())) {
            sb.append(config.getImplementationPackage());
        } else {
            sb.append(config.getTargetPackage());
        }
        if (StringUtility.isTrue(config.getProperty(PropertyRegistry.ANY_ENABLE_SUB_PACKAGES))) {
            sb.append(fullyQualifiedTable.getSubPackage());
        }
        
        return sb.toString();
    }
    
    protected String calculateDAOInterfacePackage() {
        DAOGeneratorConfiguration config = ibatorContext.getDaoGeneratorConfiguration();
        if (config == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(config.getTargetPackage());
        if (StringUtility.isTrue(config.getProperty(PropertyRegistry.ANY_ENABLE_SUB_PACKAGES))) {
            sb.append(fullyQualifiedTable.getSubPackage());
        }
        
        return sb.toString();
    }

    protected void calculateDAOAttributes() {
        if (ibatorContext.getDaoGeneratorConfiguration() == null) {
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(calculateDAOImplementationPackage());
        sb.append('.');
        sb.append(fullyQualifiedTable.getDomainObjectName());
        sb.append("DAOImpl"); //$NON-NLS-1$
        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(sb.toString());
        setDAOImplementationType(fqjt);
        
        sb.setLength(0);
        sb.append(calculateDAOInterfacePackage());
        sb.append('.');
        sb.append(fullyQualifiedTable.getDomainObjectName());
        sb.append("DAO"); //$NON-NLS-1$
        fqjt = new FullyQualifiedJavaType(sb.toString());
        setDAOInterfaceType(fqjt);
    }
    
    protected String calculateJavaModelPackage() {
        JavaModelGeneratorConfiguration config = ibatorContext.getJavaModelGeneratorConfiguration();

        StringBuilder sb = new StringBuilder();
        sb.append(config.getTargetPackage());
        if (StringUtility.isTrue(config.getProperty(PropertyRegistry.ANY_ENABLE_SUB_PACKAGES))) {
            sb.append(fullyQualifiedTable.getSubPackage());
        }
        
        return sb.toString();
    }
    
    protected void calculateModelAttributes() {
        String pakkage = calculateJavaModelPackage();
        
        StringBuilder sb = new StringBuilder();
        sb.append(pakkage);
        sb.append('.');
        sb.append(fullyQualifiedTable.getDomainObjectName());
        sb.append("Key"); //$NON-NLS-1$
        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(sb.toString());
        setPrimaryKeyType(fqjt);

        sb.setLength(0);
        sb.append(pakkage);
        sb.append('.');
        sb.append(fullyQualifiedTable.getDomainObjectName());
        fqjt = new FullyQualifiedJavaType(sb.toString());
        setBaseRecordType(fqjt);

        sb.setLength(0);
        sb.append(pakkage);
        sb.append('.');
        sb.append(fullyQualifiedTable.getDomainObjectName());
        sb.append("WithBLOBs"); //$NON-NLS-1$
        fqjt = new FullyQualifiedJavaType(sb.toString());
        setRecordWithBLOBsType(fqjt);

        sb.setLength(0);
        sb.append(pakkage);
        sb.append('.');
        sb.append(fullyQualifiedTable.getDomainObjectName());
        sb.append("Example"); //$NON-NLS-1$
        fqjt = new FullyQualifiedJavaType(sb.toString());
        setExampleType(fqjt);
    }
    
    protected String calculateSqlMapPackage() {
        SqlMapGeneratorConfiguration config = ibatorContext.getSqlMapGeneratorConfiguration();
        
        StringBuilder sb = new StringBuilder(config.getTargetPackage());
        if (StringUtility.isTrue(config.getProperty(PropertyRegistry.ANY_ENABLE_SUB_PACKAGES))) {
            sb.append(fullyQualifiedTable.getSubPackage());
        }
        return sb.toString();
    }

    protected String calculateSqlMapFileName() {
        StringBuilder sb = new StringBuilder();
        sb.append(fullyQualifiedTable.getSqlMapNamespace());
        sb.append("_SqlMap.xml"); //$NON-NLS-1$
        return sb.toString();
    }
    
    protected String calculateSqlMapNamespace() {
        return fullyQualifiedTable.getSqlMapNamespace();
    }
    
    protected String calculateSqlMapFullyQualifiedRuntimeTableName() {
        return fullyQualifiedTable.getFullyQualifiedTableNameAtRuntime();
    }
    
    protected String calculateSqlMapAliasedFullyQualifiedRuntimeTableName() {
        return fullyQualifiedTable.getAliasedFullyQualifiedTableNameAtRuntime();
    }
    
    public String getFullyQualifiedTableNameAtRuntime() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_SQL_MAP_FULLY_QUALIFIED_TABLE_NAME_AT_RUNTIME);
    }
    
    public String getAliasedFullyQualifiedTableNameAtRuntime() {
        return (String) internalAttributes.get(InternalAttribute.ATTR_SQL_MAP_ALIASED_FULLY_QUALIFIED_TABLE_NAME_AT_RUNTIME);
    }
    
    
    /**
     * This method can be used to initialize the generators before they
     * will be called. 
     * 
     * This method is called after all the setX methods, but before
     * getNumberOfSubtasks(), getGeneratedJavaFiles, and getGeneratedXmlFiles.
     * 
     * @param warnings
     * @param progressCallback
     */
    public abstract void calculateGenerators(List<String> warnings, ProgressCallback progressCallback);
    
    /**
     * This method should return a list of generated Java files related to
     * this table.  This list could include various types of model classes,
     * as well as DAO classes.
     * 
     * @return the list of generated Java files for this table
     */
    public abstract List<GeneratedJavaFile> getGeneratedJavaFiles();
    
    /**
     * This method should return a list of generated XML files related to
     * this table.  Most implementations will only return one file - 
     * the generated SqlMap file.
     * 
     * @return the list of generated XML files for this table
     */
    public abstract List<GeneratedXmlFile> getGeneratedXmlFiles();
    
    /**
     * Denotes whether generated code is targeted for Java version 5.0
     * or higher.
     *   
     * @return true if the generated code makes use of Java5 features
     */
    public abstract boolean isJava5Targeted();
    
    /**
     * This method should return the number of progress messages that
     * will be send during the generation phase.
     * 
     * @return the number of progress messages
     */
    public abstract int getGenerationSteps();

    /**
     * This method exists to give plugins the opportunity
     * to replace the calculated rules if necessary.
     * 
     * @param rules
     */
    public void setRules(IbatorRules rules) {
        this.rules = rules;
    }

    public TableConfiguration getTableConfiguration() {
        return tableConfiguration;
    }
    
    public void setDAOImplementationType(FullyQualifiedJavaType DAOImplementationType) {
        internalAttributes.put(InternalAttribute.ATTR_DAO_IMPLEMENTATION_TYPE, DAOImplementationType);
    }

    public void setDAOInterfaceType(FullyQualifiedJavaType DAOInterfaceType) {
        internalAttributes.put(InternalAttribute.ATTR_DAO_INTERFACE_TYPE, DAOInterfaceType);
    }

    public void setPrimaryKeyType(FullyQualifiedJavaType primaryKeyType) {
        internalAttributes.put(InternalAttribute.ATTR_PRIMARY_KEY_TYPE, primaryKeyType);
    }
    
    public void setBaseRecordType(FullyQualifiedJavaType baseRecordType) {
        internalAttributes.put(InternalAttribute.ATTR_BASE_RECORD_TYPE, baseRecordType);
    }
    
    public void setRecordWithBLOBsType(FullyQualifiedJavaType recordWithBLOBsType) {
        internalAttributes.put(InternalAttribute.ATTR_RECORD_WITH_BLOBS_TYPE, recordWithBLOBsType);
    }

    public void setExampleType(FullyQualifiedJavaType exampleType) {
        internalAttributes.put(InternalAttribute.ATTR_EXAMPLE_TYPE, exampleType);
    }
    
    public void setSqlMapPackage(String sqlMapPackage) {
        internalAttributes.put(InternalAttribute.ATTR_SQL_MAP_PACKAGE, sqlMapPackage);
    }

    public void setSqlMapFileName(String sqlMapFileName) {
        internalAttributes.put(InternalAttribute.ATTR_SQL_MAP_FILE_NAME, sqlMapFileName);
    }
    
    public void setSqlMapNamespace(String sqlMapNamespace) {
        internalAttributes.put(InternalAttribute.ATTR_SQL_MAP_NAMESPACE, sqlMapNamespace);
    }
    
    public void setSqlMapFullyQualifiedRuntimeTableName(String fullyQualifiedRuntimeTableName) {
        internalAttributes.put(InternalAttribute.ATTR_SQL_MAP_FULLY_QUALIFIED_TABLE_NAME_AT_RUNTIME,
                fullyQualifiedRuntimeTableName);
    }
    
    public void setSqlMapAliasedFullyQualifiedRuntimeTableName(String aliasedFullyQualifiedRuntimeTableName) {
        internalAttributes.put(InternalAttribute.ATTR_SQL_MAP_ALIASED_FULLY_QUALIFIED_TABLE_NAME_AT_RUNTIME,
                aliasedFullyQualifiedRuntimeTableName);
    }
}
