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

import org.apache.ibatis.abator.config.TableConfiguration;
import org.apache.ibatis.abator.internal.rules.TableType;
import org.apache.ibatis.abator.internal.util.EqualsUtil;

/**
 * @author Jeff Butler
 *
 */
public class IntrospectedTable {
    private TableConfiguration tableConfiguration;
    private ColumnDefinitions columnDefinitions;
    private TableType tableType;

    /**
     * 
     */
    public IntrospectedTable(TableConfiguration tableConfiguration,
            ColumnDefinitions columnDefinitions,
            TableType tableType) {
        super();
        this.columnDefinitions = columnDefinitions;
        this.tableConfiguration = tableConfiguration;
        this.tableType = tableType;
    }

    public ColumnDefinitions getColumnDefinitions() {
        return columnDefinitions;
    }

    public TableConfiguration getTableConfiguration() {
        return tableConfiguration;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof IntrospectedTable)) {
            return false;
        }

        IntrospectedTable other = (IntrospectedTable) obj;

        return EqualsUtil.areEqual(this.getTableConfiguration().getTable(),
                other.getTableConfiguration().getTable());
    }

    public int hashCode() {
        return tableConfiguration.getTable().hashCode();
    }

    /*
     * This method is primarily used for debugging, so we don't externalize the strings
     */
    public String toString() {
        String newLine = System.getProperty("line.separator"); //$NON-NLS-1$
        if (newLine == null) {
            newLine = "\n"; //$NON-NLS-1$
        }
        
        StringBuffer sb = new StringBuffer();

        sb.append("Table: "); //$NON-NLS-1$
        sb.append(tableConfiguration.getTable().getFullyQualifiedTableName());
        sb.append(newLine);

        sb.append("Primary Key:"); //$NON-NLS-1$
        sb.append(newLine);
        Iterator iter = columnDefinitions.getPrimaryKey().iterator();
        while (iter.hasNext()) {
            sb.append("   "); //$NON-NLS-1$
            sb.append(iter.next());
            sb.append(newLine);
        }

        sb.append("Columns:"); //$NON-NLS-1$
        sb.append(newLine);
        iter = columnDefinitions.getNonPrimaryKeyColumns().iterator();
        while (iter.hasNext()) {
            sb.append("   "); //$NON-NLS-1$
            sb.append(iter.next());
            sb.append(newLine);
        }
        return sb.toString();
    }

    public TableType getTableType() {
        return tableType;
    }
}
