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

package org.apache.ibatis.ibator.internal.db;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.ibator.api.FullyQualifiedTable;
import org.apache.ibatis.ibator.api.IntrospectedTable;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.config.DAOGeneratorConfiguration;
import org.apache.ibatis.ibator.config.GeneratedKey;
import org.apache.ibatis.ibator.config.IbatorContext;
import org.apache.ibatis.ibator.config.JavaModelGeneratorConfiguration;
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
 * @author Jeff Butler
 *
 */
public class IntrospectedTableDefaultImpl implements IntrospectedTable {

    protected TableConfiguration tableConfiguration;
    protected ColumnDefinitions columnDefinitions;
    protected FullyQualifiedTable fullyQualifiedTable;
    protected IbatorContext ibatorContext;
    private IbatorRules rules;
    
    /**
     * 
     */
    public IntrospectedTableDefaultImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.ibator.api.IntrospectedTable#getTable()
     */
    public FullyQualifiedTable getFullyQualifiedTable() {
        return fullyQualifiedTable;
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.ibator.api.IntrospectedTable#getSelectByExampleQueryId()
     */
    public String getSelectByExampleQueryId() {
        return tableConfiguration.getSelectByExampleQueryId();
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.ibator.api.IntrospectedTable#getSelectByPrimaryKeyQueryId()
     */
    public String getSelectByPrimaryKeyQueryId() {
        return tableConfiguration.getSelectByPrimaryKeyQueryId();
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.ibator.api.IntrospectedTable#getGeneratedKey()
     */
    public GeneratedKey getGeneratedKey() {
        return tableConfiguration.getGeneratedKey();
    }

    public ColumnDefinition getColumn(String columnName) {
        return columnDefinitions.getColumn(columnName);
    }

    public boolean hasJDBCDateColumns() {
        return columnDefinitions.hasJDBCDateColumns();
    }

    public boolean hasJDBCTimeColumns() {
        return columnDefinitions.hasJDBCTimeColumns();
    }

    public IbatorRules getRules() {
        if (rules == null) {
            if (tableConfiguration.getModelType() == ModelType.HIERARCHICAL) {
                rules = new HierarchicalModelRules(tableConfiguration, this);
            } else if (tableConfiguration.getModelType() == ModelType.FLAT) {
                rules = new FlatModelRules(tableConfiguration, this);
            } else {
                rules = new ConditionalModelRules(tableConfiguration, this);
            }
        }
        
        return rules;
    }

    public List<ColumnDefinition> getAllColumns() {
        List<ColumnDefinition> answer = new ArrayList<ColumnDefinition>();
        answer.addAll(columnDefinitions.getPrimaryKeyColumns());
        answer.addAll(columnDefinitions.getBaseColumns());
        answer.addAll(columnDefinitions.getBLOBColumns());
        
        return answer;
    }

    public List<ColumnDefinition> getNonBLOBColumns() {
        List<ColumnDefinition> answer = new ArrayList<ColumnDefinition>();
        answer.addAll(columnDefinitions.getPrimaryKeyColumns());
        answer.addAll(columnDefinitions.getBaseColumns());
        
        return answer;
    }


    public int getNonBLOBColumnCount() {
        return columnDefinitions.getPrimaryKeyColumns().size()
            + columnDefinitions.getBaseColumns().size();
    }
    
    public List<ColumnDefinition> getPrimaryKeyColumns() {
        return columnDefinitions.getPrimaryKeyColumns();
    }

    public List<ColumnDefinition> getBaseColumns() {
        return columnDefinitions.getBaseColumns();
    }

    public boolean hasPrimaryKeyColumns() {
        return columnDefinitions.hasPrimaryKeyColumns();
    }

    public List<ColumnDefinition> getBLOBColumns() {
        return columnDefinitions.getBLOBColumns();
    }

    public boolean hasBLOBColumns() {
        return columnDefinitions.hasBLOBColumns();
    }

    public List<ColumnDefinition> getNonPrimaryKeyColumns() {
        List<ColumnDefinition> answer = new ArrayList<ColumnDefinition>();
        answer.addAll(columnDefinitions.getBaseColumns());
        answer.addAll(columnDefinitions.getBLOBColumns());
        
        return answer;
    }

    public String getTableConfigurationProperty(String property) {
        return tableConfiguration.getProperty(property);
    }

    public FullyQualifiedJavaType getPrimaryKeyType() {
        StringBuilder sb = new StringBuilder();
        sb.append(getJavaModelPackage());
        sb.append('.');
        sb.append(fullyQualifiedTable.getDomainObjectName());
        sb.append("Key"); //$NON-NLS-1$

        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(sb.toString());

        return fqjt;
    }

    public FullyQualifiedJavaType getBaseRecordType() {
        StringBuilder sb = new StringBuilder();
        sb.append(getJavaModelPackage());
        sb.append('.');
        sb.append(fullyQualifiedTable.getDomainObjectName());

        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(sb.toString());

        return fqjt;
    }

    public FullyQualifiedJavaType getRecordWithBLOBsType() {
        StringBuilder sb = new StringBuilder();
        sb.append(getJavaModelPackage());
        sb.append('.');
        sb.append(fullyQualifiedTable.getDomainObjectName());
        sb.append("WithBLOBs"); //$NON-NLS-1$

        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(sb.toString());

        return fqjt;
    }

    public FullyQualifiedJavaType getExampleType() {
        StringBuilder sb = new StringBuilder();
        sb.append(getJavaModelPackage());
        sb.append('.');
        sb.append(fullyQualifiedTable.getDomainObjectName());
        sb.append("Example"); //$NON-NLS-1$

        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(sb.toString());

        return fqjt;
    }

    public boolean hasBaseColumns() {
        return columnDefinitions.hasBaseColumns();
    }

    /**
     * Calculates an SQL Map file name for the table. Typically the name is
     * "XXXX_SqlMap.xml" where XXXX is the fully qualified table name (delimited
     * with underscores).
     * 
     * @return the name of the SqlMap file
     */
    public String getSqlMapFileName() {
        StringBuilder sb = new StringBuilder();
        sb.append(fullyQualifiedTable.getSqlMapNamespace());
        sb.append("_SqlMap.xml"); //$NON-NLS-1$

        return sb.toString();
    }

    /**
     * Calculates the package for the current table.
     * 
     * @return the package for the SqlMap for the current table
     */
    public String getSqlMapPackage() {
        SqlMapGeneratorConfiguration config = ibatorContext.getSqlMapGeneratorConfiguration();
        
        StringBuilder sb = new StringBuilder(config.getTargetPackage());
        if (StringUtility.isTrue(config.getProperty(PropertyRegistry.ANY_ENABLE_SUB_PACKAGES))) {
            sb.append(fullyQualifiedTable.getSubPackage());
        }
            
        return sb.toString();
    }

    public FullyQualifiedJavaType getDAOImplementationType() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDAOPackage());
        sb.append('.');
        sb.append(fullyQualifiedTable.getDomainObjectName());
        sb.append("DAOImpl"); //$NON-NLS-1$

        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(sb.toString());

        return fqjt;
    }

    public FullyQualifiedJavaType getDAOInterfaceType() {
        StringBuilder sb = new StringBuilder();
        sb.append(getDAOPackage());
        sb.append('.');
        sb.append(fullyQualifiedTable.getDomainObjectName());
        sb.append("DAO"); //$NON-NLS-1$

        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(sb.toString());

        return fqjt;
    }
    
    public boolean hasAnyColumns() {
        return columnDefinitions.hasAnyColumns();
    }
    
    protected String getDAOPackage() {
        DAOGeneratorConfiguration config = ibatorContext.getDaoGeneratorConfiguration();
        
        StringBuilder sb = new StringBuilder();
        sb.append(config.getTargetPackage());
        if (StringUtility.isTrue(config.getProperty(PropertyRegistry.ANY_ENABLE_SUB_PACKAGES))) {
            sb.append(fullyQualifiedTable.getSubPackage());
        }
        
        return sb.toString();
    }

    protected String getJavaModelPackage() {
        JavaModelGeneratorConfiguration config = ibatorContext.getJavaModelGeneratorConfiguration();

        StringBuilder sb = new StringBuilder();
        sb.append(config.getTargetPackage());
        if (StringUtility.isTrue(config.getProperty(PropertyRegistry.ANY_ENABLE_SUB_PACKAGES))) {
            sb.append(fullyQualifiedTable.getSubPackage());
        }
        
        return sb.toString();
    }

    public void setColumnDefinitions(ColumnDefinitions columnDefinitions) {
        this.columnDefinitions = columnDefinitions;
    }

    public void setFullyQualifiedTable(FullyQualifiedTable fullyQualifiedTable) {
        this.fullyQualifiedTable = fullyQualifiedTable;
    }

    public void setIbatorContext(IbatorContext ibatorContext) {
        this.ibatorContext = ibatorContext;
    }

    public void setTableConfiguration(TableConfiguration tableConfiguration) {
        this.tableConfiguration = tableConfiguration;
    }
}
