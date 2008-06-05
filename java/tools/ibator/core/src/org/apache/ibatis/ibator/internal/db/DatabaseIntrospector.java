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
package org.apache.ibatis.ibator.internal.db;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.ibator.api.FullyQualifiedTable;
import org.apache.ibatis.ibator.api.IntrospectedTable;
import org.apache.ibatis.ibator.api.JavaTypeResolver;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.config.IbatorContext;
import org.apache.ibatis.ibator.config.ColumnOverride;
import org.apache.ibatis.ibator.config.GeneratedKey;
import org.apache.ibatis.ibator.config.PropertyRegistry;
import org.apache.ibatis.ibator.config.TableConfiguration;
import org.apache.ibatis.ibator.internal.IbatorObjectFactory;
import org.apache.ibatis.ibator.internal.types.ResolvedJavaType;
import org.apache.ibatis.ibator.internal.util.JavaBeansUtil;
import org.apache.ibatis.ibator.internal.util.StringUtility;
import org.apache.ibatis.ibator.internal.util.messages.Messages;

/**
 * 
 * @author Jeff Butler
 */
public class DatabaseIntrospector {
    
    private DatabaseMetaData databaseMetaData;
    private JavaTypeResolver javaTypeResolver;
    private List<String> warnings;
    private IbatorContext ibatorContext;

    public DatabaseIntrospector(IbatorContext ibatorContext, DatabaseMetaData databaseMetaData,
            JavaTypeResolver javaTypeResolver, List<String> warnings) {
        super();
        this.ibatorContext = ibatorContext;
        this.databaseMetaData = databaseMetaData;
        this.javaTypeResolver = javaTypeResolver;
        this.warnings = warnings;
    }

    private void calculatePrimaryKey(FullyQualifiedTable table, ColumnDefinitions columnDefinitions) {
        ResultSet rs = null;
        
        try {
            rs = databaseMetaData.getPrimaryKeys(table.getIntrospectedCatalog(),
                    table.getIntrospectedSchema(),
                    table.getIntrospectedTableName());
        } catch (SQLException e) {
            closeResultSet(rs);
            warnings.add(Messages.getString("Warning.15")); //$NON-NLS-1$
            return;
        }

        try {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME"); //$NON-NLS-1$
                
                columnDefinitions.addPrimaryKeyColumn(columnName);
            }
        } catch (SQLException e) {
            // ignore the primary key if there's any error
        } finally {
            closeResultSet(rs);
        }
    }

    private void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // ignore
                ;
            }
        }
    }

    private void reportIntrospectionWarnings(
            IntrospectedTable introspectedTable,
            TableConfiguration tableConfiguration, 
            FullyQualifiedTable table) {
        // make sure that every column listed in column overrides
        // actually exists in the table
        for (ColumnOverride columnOverride : tableConfiguration.getColumnOverrides()) {
            if (introspectedTable.getColumn(columnOverride.getColumnName()) == null) {
                warnings.add(Messages.getString("Warning.3", //$NON-NLS-1$
                        columnOverride.getColumnName(), table.toString()));
            }
        }

        // make sure that every column listed in ignored columns
        // actually exists in the table
        for (String string : tableConfiguration.getIgnoredColumnsInError()) {
            warnings.add(Messages.getString("Warning.4", //$NON-NLS-1$
                    string, table.toString()));
        }

        GeneratedKey generatedKey = tableConfiguration.getGeneratedKey();
        if (generatedKey != null
                && introspectedTable.getColumn(generatedKey.getColumn()) == null) {
            if (generatedKey.isIdentity()) {
                warnings.add(Messages.getString("Warning.5", //$NON-NLS-1$
                        generatedKey.getColumn(), table.toString()));
            } else {
                warnings.add(Messages.getString("Warning.6", //$NON-NLS-1$
                        generatedKey.getColumn(), table.toString()));
            }
        }
    }


    /**
     * Returns a List<IntrospectedTable> that matches the specified table
     * configuration. 
     * 
     * @param tc
     * @return a list of introspected tables
     * @throws SQLException
     */
    public List<IntrospectedTable> introspectTables(TableConfiguration tc) throws SQLException {

        // get the raw columns from the DB
        Map<ActualTableName, List<ColumnDefinition>> columns = getColumns(tc);
        
        if (columns.isEmpty()) {
            warnings.add(Messages.getString("Warning.19", tc.getCatalog(), //$NON-NLS-1$
                    tc.getSchema(), tc.getTableName()));
            return null;
        }
        
        removeIgnoredColumns(tc, columns);
        calculateExtraColumnInformation(tc, columns);
        applyColumnOverrides(tc, columns);
        calculateIdentityColumns(tc, columns);
        
        List<IntrospectedTable> introspectedTables = calculateIntrospectedTables(tc, columns);
        
        // now introspectedTables has all the columns from all the 
        // tables in the configuration.  Do some validation...

        Iterator<IntrospectedTable> iter = introspectedTables.iterator();
        while (iter.hasNext()) {
            IntrospectedTable introspectedTable = iter.next();
            
            if (!introspectedTable.hasAnyColumns()) {
                // add warning that the table has no columns, remove from the list
                warnings.add(Messages.getString("Warning.1", introspectedTable.getFullyQualifiedTable().toString())); //$NON-NLS-1$
                iter.remove();
            } else if (!introspectedTable.hasPrimaryKeyColumns()
                    && !introspectedTable.hasBaseColumns()) {
                // add warning that the table has only BLOB columns, remove from the list
                warnings.add(Messages.getString("Warning.18", introspectedTable.getFullyQualifiedTable().toString())); //$NON-NLS-1$
                iter.remove();
            } else {
                // now make sure that all columns called out in the configuration
                // actually exist
                reportIntrospectionWarnings(introspectedTable, tc, introspectedTable.getFullyQualifiedTable());
            }
        }

        return introspectedTables;
    }

    /**
     * @param tc
     * @param columns
     */
    private void removeIgnoredColumns(TableConfiguration tc, Map<ActualTableName, List<ColumnDefinition>> columns) {
        for (Map.Entry<ActualTableName, List<ColumnDefinition>> entry : columns.entrySet()) {
            Iterator<ColumnDefinition> tableColumns = (entry.getValue()).iterator();
            while (tableColumns.hasNext()) {
                ColumnDefinition cd = tableColumns.next();
                if (tc.isColumnIgnored(cd.getActualColumnName())) {
                    tableColumns.remove();
                }
            }
        }
    }
    
    private void calculateExtraColumnInformation(TableConfiguration tc,
    		Map<ActualTableName, List<ColumnDefinition>> columns) {
        Pattern pattern = null;
        String replaceString = null;
        if (tc.getColumnRenamingRule() != null) {
            pattern = Pattern.compile(tc.getColumnRenamingRule().getSearchString());
            replaceString = tc.getColumnRenamingRule().getReplaceString();
            replaceString = replaceString == null ? "" : replaceString;//$NON-NLS-1$
        }

        for (Map.Entry<ActualTableName, List<ColumnDefinition>> entry : columns.entrySet()) {
            for (ColumnDefinition cd : entry.getValue()) {
                String calculatedColumnName;
                if (pattern == null) {
                    calculatedColumnName = cd.getActualColumnName();
                } else {
                    Matcher matcher = pattern.matcher(cd.getActualColumnName());
                    calculatedColumnName = matcher.replaceAll(replaceString);
                }
                
                if (StringUtility.isTrue(tc.getProperty(PropertyRegistry.TABLE_USE_ACTUAL_COLUMN_NAMES))) {
                    cd.setJavaProperty(JavaBeansUtil.getValidPropertyName(calculatedColumnName));
                } else {
                    cd.setJavaProperty(JavaBeansUtil.getCamelCaseString(calculatedColumnName, false));
                }
                
                if (!javaTypeResolver.initializeResolvedJavaType(cd)) {
                    // type cannot be resolved.  Check for ignored or overridden
                    boolean warn = true;
                    if (tc.isColumnIgnored(cd.getActualColumnName())) {
                        warn = false;
                    }

                    ColumnOverride co = tc.getColumnOverride(cd.getActualColumnName());
                    if (co != null) {
                        if (StringUtility.stringHasValue(co.getJavaType())
                                && StringUtility.stringHasValue(co.getJavaType())) {
                            warn = false;
                        }
                    }
                    
                    // if the type is not supported, then we'll report a warning
                    if (warn) {
                        ResolvedJavaType type = new ResolvedJavaType();
                        type.setJdbcTypeName("OTHER"); //$NON-NLS-1$
                        type.setFullyQualifiedJavaType(new FullyQualifiedJavaType(Object.class.getName()));
                        cd.setResolvedJavaType(type);
                        
                        warnings.add(Messages.getString("Warning.14", //$NON-NLS-1$
                            entry.getKey().toString(),
                            cd.getActualColumnName()));
                    }
                    
                }
            }
        }
    }
    
    private void calculateIdentityColumns(TableConfiguration tc,
    		Map<ActualTableName, List<ColumnDefinition>> columns) {
        for (Map.Entry<ActualTableName, List<ColumnDefinition>> entry : columns.entrySet()) {
            for (ColumnDefinition cd : entry.getValue()) {
                if (tc.getGeneratedKey() != null
                        && tc.getGeneratedKey().isIdentity()) {
                    if (cd.isColumnNameDelimited()) {
                        if (cd.getActualColumnName().equals(tc.getGeneratedKey().getColumn())) {
                            cd.setIdentity(true);
                        }
                    } else {
                        if (cd.getActualColumnName().equalsIgnoreCase(tc.getGeneratedKey().getColumn())) {
                            cd.setIdentity(true);
                        }
                    }
                } else {
                    cd.setIdentity(false);
                }
            }
        }
    }
    
    private void applyColumnOverrides(TableConfiguration tc,
    		Map<ActualTableName, List<ColumnDefinition>> columns) {
        for (Map.Entry<ActualTableName, List<ColumnDefinition>> entry : columns.entrySet()) {
            for (ColumnDefinition cd : entry.getValue()) {
                ColumnOverride columnOverride = tc.getColumnOverride(cd
                        .getActualColumnName());

                if (columnOverride != null) {
                    if (StringUtility.stringHasValue(columnOverride.getJavaProperty())) {
                        cd.setJavaProperty(columnOverride.getJavaProperty());
                    }

                    if (StringUtility.stringHasValue(columnOverride.getJavaType())) {
                        cd.getResolvedJavaType()
                                .setFullyQualifiedJavaType(
                                        new FullyQualifiedJavaType(columnOverride
                                                .getJavaType()));
                    }

                    if (StringUtility.stringHasValue(columnOverride.getJdbcType())) {
                        cd.getResolvedJavaType().setJdbcTypeName(
                                columnOverride.getJdbcType());
                    }

                    if (StringUtility.stringHasValue(columnOverride.getTypeHandler())) {
                        cd.setTypeHandler(columnOverride.getTypeHandler());
                    }
                    
                    if (columnOverride.isColumnNameDelimited()) {
                        cd.setColumnNameDelimited(true);
                    }
                }
            }
        }
    }
    
    /**
     * This method returns a Map<ActualTableName, List<ColumnDefinitions>> of columns
     * returned from the database introspection.
     * 
     * @param tc
     * @return introspected columns
     * @throws SQLException
     */
    private Map<ActualTableName, List<ColumnDefinition>> getColumns(TableConfiguration tc) throws SQLException {
        String localCatalog;
        String localSchema;
        String localTableName;
        
        boolean delimitIdentifiers = tc.isDelimitIdentifiers()
            || StringUtility.stringContainsSpace(tc.getCatalog())
            || StringUtility.stringContainsSpace(tc.getSchema())
            || StringUtility.stringContainsSpace(tc.getTableName());

        if (delimitIdentifiers) {
            localCatalog = tc.getCatalog();
            localSchema = tc.getSchema();
            localTableName = tc.getTableName();
        } else if (databaseMetaData.storesLowerCaseIdentifiers()) {
            localCatalog = tc.getCatalog() == null ? null : tc.getCatalog()
                    .toLowerCase();
            localSchema = tc.getSchema() == null ? null : tc.getSchema()
                    .toLowerCase();
            localTableName = tc.getTableName() == null ? null : tc
                    .getTableName().toLowerCase();
        } else if (databaseMetaData.storesUpperCaseIdentifiers()) {
            localCatalog = tc.getCatalog() == null ? null : tc.getCatalog()
                    .toUpperCase();
            localSchema = tc.getSchema() == null ? null : tc.getSchema()
                    .toUpperCase();
            localTableName = tc.getTableName() == null ? null : tc
                    .getTableName().toUpperCase();
        } else {
            localCatalog = tc.getCatalog();
            localSchema = tc.getSchema();
            localTableName = tc.getTableName();
        }

        if (tc.isWildcardEscapingEnabled()) {
            String escapeString = databaseMetaData.getSearchStringEscape();
            
            StringBuffer sb = new StringBuffer();
            StringTokenizer st;
            if (localSchema != null) {
                st = new StringTokenizer(localSchema, "_%", true); //$NON-NLS-1$
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if (token.equals("_") //$NON-NLS-1$
                            || token.equals("%")) { //$NON-NLS-1$
                        sb.append(escapeString);
                    }
                    sb.append(token);
                }
                localSchema = sb.toString();
            }
            
            sb.setLength(0);
            st = new StringTokenizer(localTableName, "_%", true); //$NON-NLS-1$
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                if (token.equals("_") //$NON-NLS-1$
                        || token.equals("%")) { //$NON-NLS-1$
                    sb.append(escapeString);
                }
                sb.append(token);
            }
            localTableName = sb.toString();
        }

        Map<ActualTableName, List<ColumnDefinition>> answer = new HashMap<ActualTableName, List<ColumnDefinition>>();
        
        ResultSet rs = databaseMetaData.getColumns(localCatalog, localSchema,
                localTableName, null);

        while (rs.next()) {
            ColumnDefinition cd = new ColumnDefinition(tc.getAlias(), ibatorContext);

            cd.setJdbcType(rs.getInt("DATA_TYPE")); //$NON-NLS-1$
            cd.setLength(rs.getInt("COLUMN_SIZE")); //$NON-NLS-1$
            cd.setActualColumnName(rs.getString("COLUMN_NAME")); //$NON-NLS-1$
            cd.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable); //$NON-NLS-1$
            cd.setScale(rs.getInt("DECIMAL_DIGITS")); //$NON-NLS-1$
            cd.setTypeName(rs.getString("TYPE_NAME")); //$NON-NLS-1$
            
            ActualTableName atn = new ActualTableName(rs.getString("TABLE_CAT"), //$NON-NLS-1$
                    rs.getString("TABLE_SCHEM"), //$NON-NLS-1$
                    rs.getString("TABLE_NAME")); //$NON-NLS-1$
            
            List<ColumnDefinition> columns = answer.get(atn);
            if (columns == null) {
                columns = new ArrayList<ColumnDefinition>();
                answer.put(atn, columns);
            }
            
            columns.add(cd);
        }
        
        closeResultSet(rs);
        
        return answer;
    }
    
    private List<IntrospectedTable> calculateIntrospectedTables(TableConfiguration tc,
    		Map<ActualTableName, List<ColumnDefinition>> columns) {
        boolean delimitIdentifiers = tc.isDelimitIdentifiers()
            || StringUtility.stringContainsSpace(tc.getCatalog())
            || StringUtility.stringContainsSpace(tc.getSchema())
            || StringUtility.stringContainsSpace(tc.getTableName());
        
        List<IntrospectedTable> answer = new ArrayList<IntrospectedTable>();

        for (Map.Entry<ActualTableName, List<ColumnDefinition>> entry : columns.entrySet()) {
            ActualTableName atn = entry.getKey();
            
            // we only use the returned catalog and schema if something was actually
            // specified on the table configuration.  If something was returned
            // from the DB for these fields, but nothing was specified on the table
            // configuration, then some sort of DB default is being returned
            // and we don't want that in our SQL
            FullyQualifiedTable table = new FullyQualifiedTable(
                    StringUtility.stringHasValue(tc.getCatalog()) ? atn.getCatalog() : null,
                    StringUtility.stringHasValue(tc.getSchema()) ? atn.getSchema() : null,
                    atn.getTableName(),
                    tc.getDomainObjectName(),
                    tc.getAlias(),
                    StringUtility.isTrue(tc.getProperty(PropertyRegistry.TABLE_IGNORE_QUALIFIERS_AT_RUNTIME)),
                    tc.getProperty(PropertyRegistry.TABLE_RUNTIME_CATALOG),
                    tc.getProperty(PropertyRegistry.TABLE_RUNTIME_SCHEMA),
                    tc.getProperty(PropertyRegistry.TABLE_RUNTIME_TABLE_NAME),
                    delimitIdentifiers,
                    ibatorContext);

            ColumnDefinitions cds = new ColumnDefinitions();

            for (ColumnDefinition cd : entry.getValue()) {
                cds.addColumn(cd);
            }

            calculatePrimaryKey(table, cds);

            IntrospectedTable introspectedTable =
                IbatorObjectFactory.createIntrospectedTable(tc, cds, table, ibatorContext);
            answer.add(introspectedTable);
        }
        
        return answer;
    }
}
