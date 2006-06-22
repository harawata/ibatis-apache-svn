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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.abator.api.JavaTypeResolver;
import org.apache.ibatis.abator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.abator.config.ColumnOverride;
import org.apache.ibatis.abator.config.TableConfiguration;
import org.apache.ibatis.abator.exception.UnknownTableException;
import org.apache.ibatis.abator.exception.UnsupportedDataTypeException;
import org.apache.ibatis.abator.internal.rules.TableType;
import org.apache.ibatis.abator.internal.util.JavaBeansUtil;
import org.apache.ibatis.abator.internal.util.StringUtility;
import org.apache.ibatis.abator.internal.util.messages.Messages;

/**
 * 
 * @author Jeff Butler
 */
public class DatabaseIntrospector {

	private DatabaseIntrospector() {
		super();
	}

	public static IntrospectedTable introspectTable(
			Connection connection, TableConfiguration tc,
			JavaTypeResolver javaTypeResolver, List warnings)
			throws SQLException, UnknownTableException {

		ColumnDefinitions cds = new ColumnDefinitions();

		DatabaseMetaData dbmd = connection.getMetaData();
		
		String localCatalog;
		String localSchema;
		String localTableName;
		
		if (dbmd.storesLowerCaseIdentifiers()) {
		    localCatalog = tc.getTable().getCatalog() == null ? null : tc.getTable().getCatalog().toLowerCase();
		    localSchema = tc.getTable().getSchema() == null ? null : tc.getTable().getSchema().toLowerCase();
		    localTableName = tc.getTable().getTableName() == null ? null : tc.getTable().getTableName().toLowerCase();
		} else if (dbmd.storesUpperCaseIdentifiers()) {
		    localCatalog = tc.getTable().getCatalog() == null ? null : tc.getTable().getCatalog().toUpperCase();
		    localSchema = tc.getTable().getSchema() == null ? null : tc.getTable().getSchema().toUpperCase();
		    localTableName = tc.getTable().getTableName() == null ? null : tc.getTable().getTableName().toUpperCase();
		} else {
		    localCatalog = tc.getTable().getCatalog();
		    localSchema = tc.getTable().getSchema();
		    localTableName = tc.getTable().getTableName();
		}
		
		ResultSet rs = dbmd.getColumns(localCatalog, localSchema, localTableName, null);

		int columnCount = 0;
		boolean hasNonBlobColumns = false;
		while (rs.next()) {
			columnCount++;
			ColumnDefinition cd = new ColumnDefinition(tc.getTable().getAlias());

			cd.setJdbcType(rs.getInt("DATA_TYPE")); //$NON-NLS-1$
			cd.setLength(rs.getInt("COLUMN_SIZE")); //$NON-NLS-1$
			cd.setColumnName(rs.getString("COLUMN_NAME")); //$NON-NLS-1$
			cd
					.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable); //$NON-NLS-1$
			cd.setScale(rs.getInt("DECIMAL_DIGITS")); //$NON-NLS-1$
			cd.setTypeName(rs.getString("TYPE_NAME")); //$NON-NLS-1$

			ColumnOverride columnOverride = tc.getColumnOverride(cd
					.getColumnName());

			if (columnOverride == null
					|| !StringUtility.stringHasValue(columnOverride
							.getJavaProperty())) {
				if ("true".equals(tc.getProperties().get("useActualColumnNames"))) { //$NON-NLS-1$ //$NON-NLS-2$
					cd.setJavaProperty(JavaBeansUtil.getValidPropertyName(cd.getColumnName()));
				} else {
					cd.setJavaProperty(JavaBeansUtil.getCamelCaseString(cd
							.getColumnName(), false));
				}
			} else {
				cd.setJavaProperty(columnOverride.getJavaProperty());
			}

			try {
				javaTypeResolver.initializeResolvedJavaType(cd);
			} catch (UnsupportedDataTypeException e) {
				// if the type is not supported, then we'll report a warning and
				// ignore the column
				warnings.add(Messages.getString("DatabaseIntrospector.0", //$NON-NLS-1$
				        tc.getTable().getFullyQualifiedTableName(),
				        cd.getColumnName()));
				continue;
			}

			if (columnOverride != null
					&& StringUtility.stringHasValue(columnOverride
							.getJavaType())) {
				cd.getResolvedJavaType().setFullyQualifiedJavaType(
						new FullyQualifiedJavaType(columnOverride.getJavaType()));
			}

			if (columnOverride != null
					&& StringUtility.stringHasValue(columnOverride
							.getJdbcType())) {
				cd.getResolvedJavaType().setJdbcTypeName(
						columnOverride.getJdbcType());
			}

			if (tc.getGeneratedKey().isConfigured()
			        && tc.getGeneratedKey().isIdentity()
			        && cd.getColumnName().equalsIgnoreCase(tc.getGeneratedKey().getColumn())) {
			    cd.setIdentity(true);
			} else {
				cd.setIdentity(false);
			}
			
			if (!tc.isColumnIgnored(cd.getColumnName())) {
				if (!cd.isBLOBColumn()) {
					hasNonBlobColumns = true;
				}
				cds.addColumn(cd);
			}
		}

		rs.close();

		if (columnCount == 0) {
			throw new UnknownTableException(tc);
		}
		
		if (!hasNonBlobColumns) {
			// we don't support tables that only have BLOB columns
			throw new UnknownTableException(tc); 
		}
		
		// now make sure that all columns called out in the configuration actually exist
		tc.reportWarnings(cds, warnings);

		// now calculate the primary key
		List primaryKeyColumns = findPrimaryKeyColumns(dbmd, localCatalog, localSchema,
		        localTableName, warnings);
		Iterator iter = primaryKeyColumns.iterator();
		while (iter.hasNext()) {
		    cds.addPrimaryKeyColumn((String) iter.next());
		}

        IntrospectedTable answer = new IntrospectedTable(tc, cds, TableType.calculateTableType(cds));
		return answer;
	}
	
	private static List findPrimaryKeyColumns(DatabaseMetaData dbmd, String localCatalog,
	        String localSchema, String localTableName, List warnings) {
	    List answer = new ArrayList();
	    ResultSet rs = null;
	    
		try {
		    rs = dbmd.getPrimaryKeys(localCatalog, localSchema, localTableName);
		} catch (SQLException e) {
		    warnings.add(Messages.getString("DatabaseIntrospector.1")); //$NON-NLS-1$
		}

		if (rs != null) {
		    try {
		        while (rs.next()) {
		            answer.add(rs.getString("COLUMN_NAME")); //$NON-NLS-1$
		        }
		    } catch (SQLException e) {
		        // ignore the primary key if there's any error
		        answer.clear();
		    } finally {
		        closeResultSet(rs);
		    }
		}
		
		return answer;
	}
	
	private static void closeResultSet(ResultSet rs) {
	    if (rs != null) {
	        try {
	            rs.close();
	        } catch (SQLException e) {
	            // ignore
	            ;
	        }
	    }
	}
}
