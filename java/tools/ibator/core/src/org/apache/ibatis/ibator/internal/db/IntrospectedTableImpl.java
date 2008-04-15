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

import java.util.Iterator;

import org.apache.ibatis.ibator.api.FullyQualifiedTable;
import org.apache.ibatis.ibator.api.IntrospectedTable;
import org.apache.ibatis.ibator.config.GeneratedKey;
import org.apache.ibatis.ibator.config.ModelType;
import org.apache.ibatis.ibator.config.TableConfiguration;
import org.apache.ibatis.ibator.internal.rules.IbatorRules;
import org.apache.ibatis.ibator.internal.rules.ConditionalModelRules;
import org.apache.ibatis.ibator.internal.rules.FlatModelRules;
import org.apache.ibatis.ibator.internal.rules.HierarchicalModelRules;
import org.apache.ibatis.ibator.internal.util.AggregatingIterator;

/**
 * @author Jeff Butler
 *
 */
public class IntrospectedTableImpl implements IntrospectedTable {

    private TableConfiguration tableConfiguration;
    private ColumnDefinitions columnDefinitions;
    private FullyQualifiedTable table;
    private IbatorRules rules;
    
    /**
     * 
     */
    public IntrospectedTableImpl(TableConfiguration tableConfiguration, ColumnDefinitions columnDefinitions,
            FullyQualifiedTable table) {
        super();
        this.columnDefinitions = columnDefinitions;
        this.tableConfiguration = tableConfiguration;
        this.table = table;
        
        if (tableConfiguration.getModelType() == ModelType.HIERARCHICAL) {
            this.rules = new HierarchicalModelRules(tableConfiguration, columnDefinitions);
        } else if (tableConfiguration.getModelType() == ModelType.FLAT) {
            this.rules = new FlatModelRules(tableConfiguration, columnDefinitions);
        } else {
            this.rules = new ConditionalModelRules(tableConfiguration, columnDefinitions);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.ibator.api.IntrospectedTable#getTable()
     */
    public FullyQualifiedTable getTable() {
        return table;
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

    public ColumnDefinitions getColumnDefinitions() {
        return columnDefinitions;
    }

    public IbatorRules getRules() {
        return rules;
    }

    public Iterator<ColumnDefinition> getAllColumns() {
        return new AggregatingIterator<ColumnDefinition>(columnDefinitions.getPrimaryKeyColumns().iterator(),
                columnDefinitions.getBaseColumns().iterator(),
                columnDefinitions.getBLOBColumns().iterator());
    }

    public Iterator<ColumnDefinition> getNonBLOBColumns() {
        return new AggregatingIterator<ColumnDefinition>(columnDefinitions.getPrimaryKeyColumns().iterator(),
                columnDefinitions.getBaseColumns().iterator());
    }


    public int getNonBLOBColumnCount() {
        return columnDefinitions.getPrimaryKeyColumns().size()
            + columnDefinitions.getBaseColumns().size();
    }
    
    public Iterator<ColumnDefinition> getPrimaryKeyColumns() {
        return columnDefinitions.getPrimaryKeyColumns().iterator();
    }

    public Iterator<ColumnDefinition> getBaseColumns() {
        return columnDefinitions.getBaseColumns().iterator();
    }

    public boolean hasPrimaryKeyColumns() {
        return columnDefinitions.hasPrimaryKeyColumns();
    }

    public Iterator<ColumnDefinition> getBLOBColumns() {
        return columnDefinitions.getBLOBColumns().iterator();
    }

    public boolean hasBLOBColumns() {
        return columnDefinitions.hasBLOBColumns();
    }

    public Iterator<ColumnDefinition> getNonPrimaryKeyColumns() {
        return new AggregatingIterator<ColumnDefinition>(columnDefinitions.getBaseColumns().iterator(),
                columnDefinitions.getBLOBColumns().iterator());
    }

    public String getTableConfigurationProperty(String property) {
        return tableConfiguration.getProperty(property);
    }
}
