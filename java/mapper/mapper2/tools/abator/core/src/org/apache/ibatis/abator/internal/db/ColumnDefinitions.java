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
package org.apache.ibatis.abator.internal.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * This class holds the results of introspecting the database table.
 * 
 * @author Jeff Butler
 */
public class ColumnDefinitions {
	
	private String fullyQualifiedTableName;
	private LinkedHashMap columns;
	private LinkedHashMap primaryKey;

	// used by the getAllColumns convenience method only
	private ArrayList allColumns;

	public ColumnDefinitions(String fullyQualifiedTableName) {
		super();
		columns = new LinkedHashMap();
		primaryKey = new LinkedHashMap();
		this.fullyQualifiedTableName = fullyQualifiedTableName;
	}

	public Collection getBLOBColumns() {
		Collection answer = new ArrayList();
		Iterator iter = columns.values().iterator();
		while (iter.hasNext()) {
			ColumnDefinition cd = (ColumnDefinition) iter.next();
			if (cd.isBLOBColumn()) {
				answer.add(cd);
			}
		}
		
		return answer;
	}

	public Collection getNonBLOBColumns() {
		Collection answer = new ArrayList();
		Iterator iter = columns.values().iterator();
		while (iter.hasNext()) {
			ColumnDefinition cd = (ColumnDefinition) iter.next();
			if (!cd.isBLOBColumn()) {
				answer.add(cd);
			}
		}
		
		return answer;
	}
	
	public Collection getNonPrimaryKeyColumns() {
		return columns.values();
	}

	public Collection getPrimaryKey() {
		return primaryKey.values();
	}

	public void addColumn(ColumnDefinition cd) {
		columns.put(cd.getColumnName().toUpperCase(), cd);

		allColumns = null;
	}

	public void addPrimaryKeyColumn(String columnName) {
		String key = columnName.toUpperCase();
		if (columns.containsKey(key)) {
			primaryKey.put(key, columns.remove(key));

			allColumns = null;
		}
	}

	public String toString() {
	    String newLine = System.getProperty("line.separator"); //$NON-NLS-1$
	    if (newLine == null) {
	        newLine = "\n"; //$NON-NLS-1$
	    }
	    
		StringBuffer sb = new StringBuffer();

		sb.append("Table: ");
		sb.append(fullyQualifiedTableName);
		sb.append(newLine);

		sb.append("Primary Key:");
		sb.append(newLine);
		Iterator iter = primaryKey.values().iterator();
		while (iter.hasNext()) {
			sb.append("   ");
			sb.append(iter.next());
			sb.append(newLine);
		}

		sb.append("Columns:");
		sb.append(newLine);
		iter = columns.values().iterator();
		while (iter.hasNext()) {
			sb.append("   ");
			sb.append(iter.next());
			sb.append(newLine);
		}
		return sb.toString();
	}

	public boolean hasPrimaryKey() {
		return primaryKey.size() > 0;
	}

	public boolean hasBLOBColumns() {
		boolean rc = false;
		Iterator iter = columns.values().iterator();
		while (iter.hasNext()) {
			ColumnDefinition cd = (ColumnDefinition) iter.next();
			if (cd.isBLOBColumn()) {
				rc = true;
				break;
			}
		}
		
		return rc;
	}

	public boolean hasNonBLOBColumns() {
		boolean rc = false;
		Iterator iter = columns.values().iterator();
		while (iter.hasNext()) {
			ColumnDefinition cd = (ColumnDefinition) iter.next();
			if (!cd.isBLOBColumn()) {
				rc = true;
				break;
			}
		}
		
		return rc;
	}
	
	public Collection getAllColumns() {
		if (allColumns == null) {
			allColumns = new ArrayList();

			allColumns.addAll(primaryKey.values());
			allColumns.addAll(columns.values());
		}

		return allColumns;
	}

	public ColumnDefinition getColumn(String columnName) {
		String key = columnName.toUpperCase();
		ColumnDefinition cd = (ColumnDefinition) primaryKey.get(key);

		if (cd == null) {
			cd = (ColumnDefinition) columns.get(key);
		}

		return cd;
	}
}
