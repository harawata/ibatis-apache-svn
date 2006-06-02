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
package org.apache.ibatis.abator.internal.sqlmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.abator.api.GeneratedXmlFile;
import org.apache.ibatis.abator.api.JavaModelGenerator;
import org.apache.ibatis.abator.api.ProgressCallback;
import org.apache.ibatis.abator.api.SqlMapGenerator;
import org.apache.ibatis.abator.api.dom.OutputUtilities;
import org.apache.ibatis.abator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.abator.api.dom.xml.Attribute;
import org.apache.ibatis.abator.api.dom.xml.Document;
import org.apache.ibatis.abator.api.dom.xml.TextElement;
import org.apache.ibatis.abator.api.dom.xml.XmlElement;
import org.apache.ibatis.abator.config.FullyQualifiedTable;
import org.apache.ibatis.abator.config.TableConfiguration;
import org.apache.ibatis.abator.internal.db.ColumnDefinition;
import org.apache.ibatis.abator.internal.db.ColumnDefinitions;
import org.apache.ibatis.abator.internal.rules.AbatorRules;
import org.apache.ibatis.abator.internal.util.StringUtility;
import org.apache.ibatis.abator.internal.util.messages.Messages;

/**
 * @author Jeff Butler
 */
public class SqlMapGeneratorIterateImpl implements SqlMapGenerator {

	protected List warnings;

    /**
     * Contains any properties passed in from the SqlMap configuration element.
     */
    protected Map properties;

    /**
     * This is the target package from the SqlMap configuration element
     */
    protected String targetPackage;

    /**
     * This is the target project from the SqlMap configuration element
     */
    protected String targetProject;

    /**
     * This is the java model generator associated with the current generation
     * context. Methods in this interface can be used to determine the
     * appropriate result and parameter class names.
     */
    protected JavaModelGenerator javaModelGenerator;

    /**
     * This is a map of maps. The map is keyed by a FullyQualifiedTable object.
     * The inner map holds generated strings keyed by the String name. This
     * Map is used to cache generated Strings.
     */
    private Map tableStringMaps;

    /**
     * Constructs an instance of SqlMapGeneratorDefaultImpl
     */
    public SqlMapGeneratorIterateImpl() {
        super();
        tableStringMaps = new HashMap();
    }

    private Map getTableStringMap(FullyQualifiedTable table) {
        Map map = (Map) tableStringMaps.get(table);
        if (map == null) {
            map = new HashMap();
            tableStringMaps.put(table, map);
        }

        return map;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.abator.api.SqlMapGenerator#setProperties(java.util.Map)
     */
    public void setProperties(Map properties) {
        this.properties = properties;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.abator.api.SqlMapGenerator#setTargetPackage(java.lang.String)
     */
    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.abator.api.SqlMapGenerator#setJavaModelGenerator(org.apache.ibatis.abator.api.JavaModelGenerator)
     */
    public void setJavaModelGenerator(JavaModelGenerator javaModelGenerator) {
        this.javaModelGenerator = javaModelGenerator;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.abator.api.SqlMapGenerator#getGeneratedXMLFiles(org.apache.ibatis.abator.internal.db.ColumnDefinitions, org.apache.ibatis.abator.config.TableConfiguration, org.apache.ibatis.abator.api.ProgressCallback)
     */
    public List getGeneratedXMLFiles(ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration, ProgressCallback callback) {
        ArrayList list = new ArrayList();

        callback.startSubTask(Messages.getString("SqlMapGeneratorDefaultImpl.0", //$NON-NLS-1$
                tableConfiguration.getTable().getFullyQualifiedTableName()));
        list.add(getSqlMap(columnDefinitions, tableConfiguration));

        return list;
    }

    /**
     * Creates the default implementation of the Sql Map
     * 
     * @param columnDefinitions introspected column definitions for the current table
     * @param tableConfiguration table configuration for the current table
     * @return A GeneratedXMLFile for the current table
     */
    protected GeneratedXmlFile getSqlMap(ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration) {

        Document document = new Document(XmlConstants.SQL_MAP_PUBLIC_ID, XmlConstants.SQL_MAP_SYSTEM_ID);
        document.setRootElement(getSqlMapElement(columnDefinitions, tableConfiguration));

        GeneratedXmlFile answer = new GeneratedXmlFile(document,
                getSqlMapFileName(tableConfiguration.getTable()),
                getSqlMapPackage(tableConfiguration.getTable()),
                targetProject);
        
        return answer;
    }

    /**
     * Creates the sqlMap element (the root element, and all child elements).
     * 
     * @param columnDefinitions introspected column definitions for the current table
     * @param tableConfiguration table configuration for the current table
     * @return the sqlMap element including all child elements
     */
    protected XmlElement getSqlMapElement(ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration) {
        
        XmlElement answer = new XmlElement("sqlMap"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("namespace", //$NON-NLS-1$
                getSqlMapNamespace(tableConfiguration.getTable())));
        
        XmlElement element;
        if (AbatorRules.generateResultMapWithoutBLOBs(tableConfiguration)) {
            element = getBaseResultMapElement(columnDefinitions,
                    tableConfiguration);
            if (element != null) {
                answer.addElement(element);
            }
        }

        if (AbatorRules.generateResultMapWithBLOBs(columnDefinitions, tableConfiguration)) {
            element = getResultMapWithBLOBsElement(columnDefinitions,
                    tableConfiguration);
            if (element != null) {
                answer.addElement(element);
            }
        }

        if (AbatorRules.generateSQLExampleWhereClause(tableConfiguration)) {
            element = getByExampleWhereClauseFragment();
            if (element != null) {
                answer.addElement(element);
            }
        }

        if (AbatorRules.generateSelectByPrimaryKey(columnDefinitions, tableConfiguration)) {
            element = getSelectByPrimaryKey(columnDefinitions,
                    tableConfiguration);
            if (element != null) {
                answer.addElement(element);
            }
        }

        if (AbatorRules.generateSelectByExampleWithoutBLOBs(tableConfiguration)) {
            element = getSelectByExample(columnDefinitions, tableConfiguration);
            if (element != null) {
                answer.addElement(element);
            }
        }

        if (AbatorRules
                .generateSelectByExampleWithBLOBs(columnDefinitions, tableConfiguration)) {
            element = getSelectByExampleWithBLOBs(columnDefinitions,
                    tableConfiguration);
            if (element != null) {
                answer.addElement(element);
            }
        }

        if (AbatorRules.generateDeleteByPrimaryKey(columnDefinitions, tableConfiguration)) {
            element = getDeleteByPrimaryKey(columnDefinitions,
                    tableConfiguration);
            if (element != null) {
                answer.addElement(element);
            }
        }

        if (AbatorRules.generateDeleteByExample(tableConfiguration)) {
            element = getDeleteByExample(columnDefinitions, tableConfiguration);
            if (element != null) {
                answer.addElement(element);
            }
        }

        if (AbatorRules.generateInsert(tableConfiguration)) {
            element = getInsertElement(columnDefinitions, tableConfiguration);
            if (element != null) {
                answer.addElement(element);
            }
        }

        if (AbatorRules
                .generateUpdateByPrimaryKeyWithBLOBs(columnDefinitions, tableConfiguration)) {
            element = getUpdateByPrimaryKeyWithBLOBs(columnDefinitions,
                    tableConfiguration);
            if (element != null) {
                answer.addElement(element);
            }
        }

        if (AbatorRules.generateUpdateByPrimaryKeyWithoutBLOBs(columnDefinitions, tableConfiguration)) {
            element = getUpdateByPrimaryKey(columnDefinitions,
                    tableConfiguration);
            if (element != null) {
                answer.addElement(element);
            }
        }

        return answer;
    }

    /**
     * This method should return an XmlElement which is the result
     * map (without any BLOBs if they exist in the table).
     * 
     * @param columnDefinitions
     *            introspected column definitions for the current table
     * @param tableConfiguration
     *            table configuration for the current table
     * @return the resultMap element
     */
    protected XmlElement getBaseResultMapElement(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration) {
        XmlElement answer = new XmlElement("resultMap"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("id", //$NON-NLS-1$
                getResultMapName(tableConfiguration.getTable())));
        
        if (AbatorRules.generateBaseRecordWithNoSuperclass(columnDefinitions)
                || AbatorRules.generateBaseRecordExtendingPrimaryKey(columnDefinitions)) {
            answer.addAttribute(new Attribute("class", javaModelGenerator //$NON-NLS-1$
                    .getRecordType(tableConfiguration.getTable())
                    .getFullyQualifiedName()));
        } else {
            answer.addAttribute(new Attribute("class", javaModelGenerator //$NON-NLS-1$
                    .getPrimaryKeyType(tableConfiguration.getTable())
                    .getFullyQualifiedName()));
        }
        
        answer.addComment();

        Iterator iter = columnDefinitions.getAllColumns().iterator();
        while (iter.hasNext()) {
            ColumnDefinition cd = (ColumnDefinition) iter.next();
            if (cd.isBLOBColumn()) {
                continue;
            }
            
            XmlElement resultElement = new XmlElement("result"); //$NON-NLS-1$

            resultElement.addAttribute(new Attribute("column", cd.getRenamedColumnName())); //$NON-NLS-1$
            resultElement.addAttribute(new Attribute("property", cd.getJavaProperty())); //$NON-NLS-1$
            resultElement.addAttribute(new Attribute("jdbcType", //$NON-NLS-1$
                    cd.getResolvedJavaType().getJdbcTypeName()));
            
            answer.addElement(resultElement);
        }

        return answer;
    }

    /**
     * This method should return an XmlElement which is the result
     * map (with any BLOBs if they exist in the table). Typically this result
     * map extends the base result map.
     * 
     * @param columnDefinitions
     *            introspected column definitions for the current table
     * @param tableConfiguration
     *            table configuration for the current table
     * @return the resultMap element
     */
    protected XmlElement getResultMapWithBLOBsElement(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration) {
        
        XmlElement answer = new XmlElement("resultMap"); //$NON-NLS-1$
        
        StringBuffer sb = new StringBuffer();
        sb.append(getResultMapName(tableConfiguration.getTable()));
        sb.append("WithBLOBs"); //$NON-NLS-1$
        
        answer.addAttribute(new Attribute("id", sb.toString())); //$NON-NLS-1$
        answer.addAttribute(new Attribute("class", javaModelGenerator //$NON-NLS-1$
                .getRecordWithBLOBsType(tableConfiguration.getTable())
                .getFullyQualifiedName()));

        sb.setLength(0);
        sb.append(getSqlMapNamespace(tableConfiguration.getTable()));
        sb.append('.');
        sb.append(getResultMapName(tableConfiguration.getTable()));
        answer.addAttribute(new Attribute("extends", sb.toString())); //$NON-NLS-1$

        answer.addComment();

        Iterator iter = columnDefinitions.getAllColumns().iterator();
        while (iter.hasNext()) {
            ColumnDefinition cd = (ColumnDefinition) iter.next();
            if (!cd.isBLOBColumn()) {
                continue;
            }

            XmlElement resultElement = new XmlElement("result"); //$NON-NLS-1$
            resultElement.addAttribute(new Attribute("column", cd.getRenamedColumnName())); //$NON-NLS-1$
            resultElement.addAttribute(new Attribute("property", cd.getJavaProperty())); //$NON-NLS-1$
            resultElement.addAttribute(new Attribute(
                    "jdbcType", cd.getResolvedJavaType().getJdbcTypeName())); //$NON-NLS-1$
            
            answer.addElement(resultElement);
        }

        return answer;
    }

    /**
     * This method should return an XmlElement which the insert
     * statement.
     * 
     * @param columnDefinitions
     *            introspected column definitions for the current table
     * @param tableConfiguration
     *            table configuration for the current table
     * @return the insert element
     */
    protected XmlElement getInsertElement(ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration) {

        XmlElement answer = new XmlElement("insert"); //$NON-NLS-1$
        
        answer.addAttribute(new Attribute("id", getInsertStatementId())); //$NON-NLS-1$
        if (AbatorRules.generateRecordWithBLOBsExtendingPrimaryKey(columnDefinitions)
                || AbatorRules.generateRecordWithBLOBsExtendingBaseRecord(columnDefinitions)) {
            answer.addAttribute(new Attribute("parameterClass", javaModelGenerator //$NON-NLS-1$
                    .getRecordWithBLOBsType(tableConfiguration.getTable())
                    .getFullyQualifiedName()));
        } else if (AbatorRules.generateBaseRecordWithNoSuperclass(columnDefinitions)
                || AbatorRules.generateBaseRecordExtendingPrimaryKey(columnDefinitions)) {
            answer.addAttribute(new Attribute("parameterClass", javaModelGenerator //$NON-NLS-1$
                    .getRecordType(tableConfiguration.getTable())
                    .getFullyQualifiedName()));
        } else {
            answer.addAttribute(new Attribute("parameterClass", javaModelGenerator //$NON-NLS-1$
                    .getPrimaryKeyType(tableConfiguration.getTable())
                    .getFullyQualifiedName()));
        }
        
        answer.addComment();

        if (tableConfiguration.getGeneratedKey().isConfigured()
                && !tableConfiguration.getGeneratedKey().isIdentity()) {
        	ColumnDefinition cd = columnDefinitions.getColumn(tableConfiguration
                    .getGeneratedKey().getColumn());
        	// if the column is null, then it's a configuration error.  The
        	// warning has already been reported
        	if (cd != null) {
        	    // pre-generated key
        	    answer.addElement(getSelectKey(cd, tableConfiguration));
        	}
        }

        StringBuffer insertClause = new StringBuffer();
        StringBuffer valuesClause = new StringBuffer();

        insertClause.append("insert into "); //$NON-NLS-1$
        insertClause.append(tableConfiguration.getTable()
                .getFullyQualifiedTableName());
        insertClause.append(" ("); //$NON-NLS-1$

        valuesClause.append("values ("); //$NON-NLS-1$

        ColumnDefinition identityColumn = null;
        boolean comma = false;
        Iterator iter = columnDefinitions.getAllColumns().iterator();
        while (iter.hasNext()) {
            ColumnDefinition cd = (ColumnDefinition) iter.next();

            if (cd.isIdentity()) {
                identityColumn = cd;
                // cannot set values on identity fields
                continue;
            }
            
            if (comma) {
                insertClause.append(", "); //$NON-NLS-1$
                valuesClause.append(", "); //$NON-NLS-1$
            } else {
                comma = true; // turn on comma for next time
            }

            insertClause.append(cd.getColumnName());
            
            valuesClause.append('#');
            valuesClause.append(cd.getJavaProperty());
            valuesClause.append(':');
            valuesClause.append(cd.getResolvedJavaType().getJdbcTypeName());
            valuesClause.append('#');
        }
        insertClause.append(')');
        valuesClause.append(')');
        
        answer.addElement(new TextElement(insertClause.toString()));
        answer.addElement(new TextElement(valuesClause.toString()));

        if (identityColumn != null) {
            answer.addElement(getSelectKey(identityColumn, tableConfiguration));
        }

        return answer;
    }

    /**
     * This method should return an XmlElement for the update by primary
     * key statement that updates all fields in the table (including BLOB
     * fields).
     * 
     * @param columnDefinitions
     *            introspected column definitions for the current table
     * @param tableConfiguration
     *            table configuration for the current table
     * @return the update element
     */
    protected XmlElement getUpdateByPrimaryKeyWithBLOBs(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration) {
        
        XmlElement answer = new XmlElement("update"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", getUpdateByPrimaryKeyWithBLOBsStatementId())); //$NON-NLS-1$
        answer.addAttribute(new Attribute("parameterClass", javaModelGenerator //$NON-NLS-1$
                .getRecordWithBLOBsType(tableConfiguration.getTable())
                .getFullyQualifiedName()));

        answer.addComment();

        StringBuffer sb = new StringBuffer();
        
        sb.append("update "); //$NON-NLS-1$
        sb.append(tableConfiguration.getTable().getFullyQualifiedTableName());
        answer.addElement(new TextElement(sb.toString()));

        // set up for first column
        sb.setLength(0);
        sb.append("set "); //$NON-NLS-1$

        Iterator iter = columnDefinitions.getNonPrimaryKeyColumns().iterator();
        while (iter.hasNext()) {
            ColumnDefinition cd = (ColumnDefinition) iter.next();
            
            sb.append(cd.getColumnName());
            sb.append(" = #"); //$NON-NLS-1$
            sb.append(cd.getJavaProperty());
            sb.append(':');
            sb.append(cd.getResolvedJavaType().getJdbcTypeName());
            sb.append('#');

            if (iter.hasNext()) {
                sb.append(',');
            }
            
            answer.addElement(new TextElement(sb.toString()));
            
            // set up for the next column
            if (iter.hasNext()) {
                sb.setLength(0);
                OutputUtilities.xmlIndent(sb, 1);
            }
        }

        boolean and = false;
        iter = columnDefinitions.getPrimaryKey().iterator();
        while (iter.hasNext()) {
            ColumnDefinition cd = (ColumnDefinition) iter.next();

            sb.setLength(0);
            if (and) {
                sb.append("  and "); //$NON-NLS-1$
            } else {
                sb.append("where "); //$NON-NLS-1$
                and = true;
            }

            sb.append(cd.getColumnName());
            sb.append(" = #"); //$NON-NLS-1$
            sb.append(cd.getJavaProperty());
            sb.append('#');
            answer.addElement(new TextElement(sb.toString()));
        }

        return answer;
    }

    /**
     * This method should return an XmlElement for the update by primary
     * key statement that updates all fields in the table (excluding BLOB
     * fields).
     * 
     * @param columnDefinitions
     *            introspected column definitions for the current table
     * @param tableConfiguration
     *            table configuration for the current table
     * @return the update element
     */
    protected XmlElement getUpdateByPrimaryKey(ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration) {

        XmlElement answer = new XmlElement("update"); //$NON-NLS-1$
        
        answer.addAttribute(new Attribute("id", getUpdateByPrimaryKeyStatementId())); //$NON-NLS-1$
        answer.addAttribute(new Attribute("parameterClass", javaModelGenerator //$NON-NLS-1$
                .getRecordType(tableConfiguration.getTable())
                .getFullyQualifiedName()));

        answer.addComment();

        StringBuffer sb = new StringBuffer();
        sb.append("update "); //$NON-NLS-1$
        sb.append(tableConfiguration.getTable().getFullyQualifiedTableName());
        answer.addElement(new TextElement(sb.toString()));
        
        // set up for first column
        sb.setLength(0);
        sb.append("set "); //$NON-NLS-1$

        Iterator iter = columnDefinitions.getNonBLOBColumns().iterator();
        while (iter.hasNext()) {
            ColumnDefinition cd = (ColumnDefinition) iter.next();

            sb.append(cd.getColumnName());
            sb.append(" = #"); //$NON-NLS-1$
            sb.append(cd.getJavaProperty());
            sb.append(':');
            sb.append(cd.getResolvedJavaType().getJdbcTypeName());
            sb.append('#');

            if (iter.hasNext()) {
                sb.append(',');
            }
            
            answer.addElement(new TextElement(sb.toString()));
            
            // set up for the next column
            if (iter.hasNext()) {
                sb.setLength(0);
                OutputUtilities.xmlIndent(sb, 1);
            }
        }

        boolean and = false;
        iter = columnDefinitions.getPrimaryKey().iterator();
        while (iter.hasNext()) {
            ColumnDefinition cd = (ColumnDefinition) iter.next();

            sb.setLength(0);
            if (and) {
                sb.append("  and "); //$NON-NLS-1$
            } else {
                sb.append("where "); //$NON-NLS-1$
                and = true;
            }

            sb.append(cd.getColumnName());
            sb.append(" = #"); //$NON-NLS-1$
            sb.append(cd.getJavaProperty());
            sb.append(':');
            sb.append(cd.getResolvedJavaType().getJdbcTypeName());
            sb.append('#');
            answer.addElement(new TextElement(sb.toString()));
        }

        return answer;
    }

    /**
     * This method should return an XmlElement for the delete by primary
     * key statement.
     * 
     * @param columnDefinitions
     *            introspected column definitions for the current table
     * @param tableConfiguration
     *            table configuration for the current table
     * @return the delete element
     */
    protected XmlElement getDeleteByPrimaryKey(ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration) {
        
        XmlElement answer = new XmlElement("delete"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", getDeleteByPrimaryKeyStatementId())); //$NON-NLS-1$
        answer.addAttribute(new Attribute("parameterClass", javaModelGenerator //$NON-NLS-1$
                .getPrimaryKeyType(tableConfiguration.getTable())
                .getFullyQualifiedName()));

        answer.addComment();

        StringBuffer sb = new StringBuffer();
        sb.append("delete from "); //$NON-NLS-1$
        sb.append(tableConfiguration.getTable().getFullyQualifiedTableName());
        answer.addElement(new TextElement(sb.toString()));

        boolean and = false;
        Iterator iter = columnDefinitions.getPrimaryKey().iterator();
        while (iter.hasNext()) {
            ColumnDefinition cd = (ColumnDefinition) iter.next();

            sb.setLength(0);
            if (and) {
                sb.append("  and "); //$NON-NLS-1$
            } else {
                sb.append("where "); //$NON-NLS-1$
                and = true;
            }

            sb.append(cd.getColumnName());
            sb.append(" = #"); //$NON-NLS-1$
            sb.append(cd.getJavaProperty());
            sb.append(':');
            sb.append(cd.getResolvedJavaType().getJdbcTypeName());
            sb.append('#');
            answer.addElement(new TextElement(sb.toString()));
        }

        return answer;
    }

    /**
     * This method should return an XmlElement for the delete by example
     * statement. This statement uses the "by example" SQL fragment
     * 
     * @param columnDefinitions
     *            introspected column definitions for the current table
     * @param tableConfiguration
     *            table configuration for the current table
     * @return the delete by example element
     */
    protected XmlElement getDeleteByExample(ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration) {
        
        XmlElement answer = new XmlElement("delete"); //$NON-NLS-1$

        FullyQualifiedJavaType fqjt = javaModelGenerator.getExampleType(tableConfiguration.getTable());

        answer.addAttribute(new Attribute("id", getDeleteByExampleStatementId())); //$NON-NLS-1$
        answer.addAttribute(new Attribute("parameterClass", fqjt.getFullyQualifiedName())); //$NON-NLS-1$

        answer.addComment();

        StringBuffer sb = new StringBuffer();
        sb.append("delete from "); //$NON-NLS-1$
        sb.append(tableConfiguration.getTable().getAliasedFullyQualifiedTableName());
        answer.addElement(new TextElement(sb.toString()));

        XmlElement includeElement = new XmlElement("include"); //$NON-NLS-1$
        sb.setLength(0);
        sb.append(getSqlMapNamespace(tableConfiguration.getTable()));
        sb.append('.');
        sb.append(getExampleWhereClauseId());
        includeElement.addAttribute(new Attribute("refid", //$NON-NLS-1$
                sb.toString()));
        
        answer.addElement(includeElement);

        return answer;
    }

    /**
     * This method should return an XmlElement for the select by primary
     * key statement. The statement should include all fields in the table,
     * including BLOB fields.
     * 
     * @param columnDefinitions
     *            introspected column definitions for the current table
     * @param tableConfiguration
     *            table configuration for the current table
     * @return the select by primary key element
     */
    protected XmlElement getSelectByPrimaryKey(ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration) {

        XmlElement answer = new XmlElement("select"); //$NON-NLS-1$
        
        answer.addAttribute(new Attribute("id", getSelectByPrimaryKeyStatementId())); //$NON-NLS-1$
        if (AbatorRules.generateResultMapWithBLOBs(columnDefinitions, tableConfiguration)) {
            answer.addAttribute(new Attribute("resultMap", //$NON-NLS-1$
                    getResultMapName(tableConfiguration.getTable()) + "WithBLOBs")); //$NON-NLS-1$
        } else {
            answer.addAttribute(new Attribute("resultMap", //$NON-NLS-1$
                    getResultMapName(tableConfiguration.getTable())));
        }
        answer.addAttribute(new Attribute("parameterClass", javaModelGenerator //$NON-NLS-1$
                .getPrimaryKeyType(tableConfiguration.getTable())
                .getFullyQualifiedName()));

        answer.addComment();

        StringBuffer sb = new StringBuffer();
        sb.append("select "); //$NON-NLS-1$

        boolean comma = false;
        if (StringUtility.stringHasValue(tableConfiguration
                .getSelectByPrimaryKeyQueryId())) {
            sb.append('\'');
            sb.append(tableConfiguration.getSelectByPrimaryKeyQueryId());
            sb.append("' as QUERYID"); //$NON-NLS-1$
            comma = true;
        }

        Iterator iter = columnDefinitions.getAllColumns().iterator();
        while (iter.hasNext()) {
            ColumnDefinition cd = (ColumnDefinition) iter.next();
            if (comma) {
                sb.append(", "); //$NON-NLS-1$
            } else {
                comma = true;
            }

            sb.append(cd.getSelectListPhrase());
        }
        
        answer.addElement(new TextElement(sb.toString()));

        sb.setLength(0);
        sb.append("from "); //$NON-NLS-1$
        sb.append(tableConfiguration.getTable().getAliasedFullyQualifiedTableName());
        answer.addElement(new TextElement(sb.toString()));

        boolean and = false;
        iter = columnDefinitions.getPrimaryKey().iterator();
        while (iter.hasNext()) {
            ColumnDefinition cd = (ColumnDefinition) iter.next();
            
            sb.setLength(0);
            if (and) {
                sb.append("  and "); //$NON-NLS-1$
            } else {
                sb.append("where "); //$NON-NLS-1$
                and = true;
            }

            sb.append(cd.getAliasedColumnName());
            sb.append(" = #"); //$NON-NLS-1$
            sb.append(cd.getJavaProperty());
            sb.append(':');
            sb.append(cd.getResolvedJavaType().getJdbcTypeName());
            sb.append('#');
            answer.addElement(new TextElement(sb.toString()));
        }

        return answer;
    }

    /**
     * This method should return an XmlElement for the select key 
     * used to automatically generate keys.
     * 
     * @param columnDefinition the column related to the select key statement
     * @param tableConfiguration table configuration for the current table
     * @return the selectKey element
     */
    protected XmlElement getSelectKey(ColumnDefinition columnDefinition,
            TableConfiguration tableConfiguration) {
        String identityColumnType = columnDefinition.getResolvedJavaType()
                .getFullyQualifiedJavaType().getFullyQualifiedName();

        XmlElement answer = new XmlElement("selectKey"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("resultClass", identityColumnType)); //$NON-NLS-1$
        answer.addAttribute(new Attribute("keyProperty", columnDefinition.getJavaProperty())); //$NON-NLS-1$
        answer.addElement(new TextElement(tableConfiguration.getGeneratedKey().getSqlStatement()));

        return answer;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.abator.api.SqlMapGenerator#getSqlMapNamespace(org.apache.ibatis.abator.config.FullyQualifiedTable)
     */
    public String getSqlMapNamespace(FullyQualifiedTable table) {
        String key = "getSqlMapNamespace"; //$NON-NLS-1$
        String s;

        Map map = getTableStringMap(table);
        s = (String) map.get(key);
        if (s == null) {
            s = table.getFullyQualifiedTableNameWithUnderscores();
            map.put(key, s);
        }

        return s;
    }

    /**
     * Calculates the name of the result map. Typically this is the String
     * "abatorgenerated_XXXXResult" where XXXX is the name of the domain object
     * related to this table. The prefix "abatorgenerated_" is important because it
     * allows Abator to regenerate this element on subsequent runs.
     * 
     * @param table the current table
     * @return the name of the result map
     */
    protected String getResultMapName(FullyQualifiedTable table) {
        String key = "getResultMapName"; //$NON-NLS-1$
        String s;

        Map map = getTableStringMap(table);
        s = (String) map.get(key);
        if (s == null) {
            StringBuffer sb = new StringBuffer();

            sb.append("abatorgenerated_"); //$NON-NLS-1$
            sb.append(table.getDomainObjectName());
            sb.append("Result"); //$NON-NLS-1$

            s = sb.toString();
            map.put(key, s);
        }

        return s;
    }

    /**
     * Calculates a file name for the current table. Typically the name is
     * "XXXX_SqlMap.xml" where XXXX is the fully qualified table name (delimited
     * with underscores).
     * 
     * @param table the current table
     * @return tha name of the SqlMap file
     */
    protected String getSqlMapFileName(FullyQualifiedTable table) {
        String key = "getSqlMapFileName"; //$NON-NLS-1$
        String s;

        Map map = getTableStringMap(table);
        s = (String) map.get(key);
        if (s == null) {
            StringBuffer sb = new StringBuffer();
            sb.append(table.getFullyQualifiedTableNameWithUnderscores());

            sb.append("_SqlMap.xml"); //$NON-NLS-1$

            s = sb.toString();
            map.put(key, s);
        }

        return s;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.abator.api.SqlMapGenerator#getDeleteByPrimaryKeyStatementId()
     */
    public String getDeleteByPrimaryKeyStatementId() {
        return "abatorgenerated_deleteByPrimaryKey"; //$NON-NLS-1$
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.abator.api.SqlMapGenerator#getDeleteByExampleStatementId()
     */
    public String getDeleteByExampleStatementId() {
        return "abatorgenerated_deleteByExample"; //$NON-NLS-1$
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.abator.api.SqlMapGenerator#getInsertStatementId()
     */
    public String getInsertStatementId() {
        return "abatorgenerated_insert"; //$NON-NLS-1$
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.abator.api.SqlMapGenerator#getSelectByPrimaryKeyStatementId()
     */
    public String getSelectByPrimaryKeyStatementId() {
        return "abatorgenerated_selectByPrimaryKey"; //$NON-NLS-1$
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.abator.api.SqlMapGenerator#getSelectByExampleStatementId()
     */
    public String getSelectByExampleStatementId() {
        return "abatorgenerated_selectByExample"; //$NON-NLS-1$
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.abator.api.SqlMapGenerator#getSelectByExampleWithBLOBsStatementId()
     */
    public String getSelectByExampleWithBLOBsStatementId() {
        return "abatorgenerated_selectByExampleWithBLOBs"; //$NON-NLS-1$
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.abator.api.SqlMapGenerator#getUpdateByPrimaryKeyWithBLOBsStatementId()
     */
    public String getUpdateByPrimaryKeyWithBLOBsStatementId() {
        return "abatorgenerated_updateByPrimaryKeyWithBLOBs"; //$NON-NLS-1$
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.abator.api.SqlMapGenerator#getUpdateByPrimaryKeyStatementId()
     */
    public String getUpdateByPrimaryKeyStatementId() {
        return "abatorgenerated_updateByPrimaryKey"; //$NON-NLS-1$
    }

    /**
     * Calculates the package for the current table.
     * 
     * @param table the current table
     * @return the package for the SqlMap for the current table
     */
    protected String getSqlMapPackage(FullyQualifiedTable table) {
        String key = "getSqlMapPackage"; //$NON-NLS-1$
        String s;

        Map map = getTableStringMap(table);
        s = (String) map.get(key);
        if (s == null) {
            if ("true".equals(properties.get("enableSubPackages"))) { //$NON-NLS-1$ //$NON-NLS-2$
                StringBuffer sb = new StringBuffer(targetPackage);

                if (StringUtility.stringHasValue(table.getCatalog())) {
                    sb.append('.');
                    sb.append(table.getCatalog().toLowerCase());
                }

                if (StringUtility.stringHasValue(table.getSchema())) {
                    sb.append('.');
                    sb.append(table.getSchema().toLowerCase());
                }

                s = sb.toString();
            } else {
                s = targetPackage;
            }

            map.put(key, s);
        }

        return s;
    }

    /**
     * Calculates the name of the example where clause element
     * 
     * @return the name of the example where clause element
     */
    protected String getExampleWhereClauseId() {
        return "abatorgenerated_Example_Where_Clause"; //$NON-NLS-1$
    }

    /**
     * This method should return an XmlElement for the example where clause
     * SQL fragment (an sql fragment).
     * 
     * @return the SQL element
     */
    protected XmlElement getByExampleWhereClauseFragment() {
        
        XmlElement answer = new XmlElement("sql"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", getExampleWhereClauseId())); //$NON-NLS-1$

        answer.addComment();

        XmlElement outerIterateElement = new XmlElement("iterate"); //$NON-NLS-1$
        outerIterateElement.addAttribute(new Attribute("property", "oredConditions")); //$NON-NLS-1$ //$NON-NLS-2$
        outerIterateElement.addAttribute(new Attribute("conjunction", "or")); //$NON-NLS-1$ //$NON-NLS-2$
        outerIterateElement.addAttribute(new Attribute("prepend", "where")); //$NON-NLS-1$ //$NON-NLS-2$
        answer.addElement(outerIterateElement);

        outerIterateElement.addElement(new TextElement("(")); //$NON-NLS-1$

        XmlElement innerIterateElement = new XmlElement("iterate"); //$NON-NLS-1$
        innerIterateElement.addAttribute(new Attribute("property", "oredConditions[].conditionsWithoutValue")); //$NON-NLS-1$ //$NON-NLS-2$
        innerIterateElement.addAttribute(new Attribute("conjunction", "and")); //$NON-NLS-1$ //$NON-NLS-2$
        innerIterateElement.addElement(new TextElement("$oredConditions[].conditionsWithoutValue[]$")); //$NON-NLS-1$
        outerIterateElement.addElement(innerIterateElement);

        XmlElement isEqualElement = new XmlElement("isEqual"); //$NON-NLS-1$
        isEqualElement.addAttribute(new Attribute("property", //$NON-NLS-1$
                "oredConditions[].firstAndNeeded")); //$NON-NLS-1$
        isEqualElement.addAttribute(new Attribute("compareValue", //$NON-NLS-1$
                "true")); //$NON-NLS-1$
        isEqualElement.addElement(new TextElement("and")); //$NON-NLS-1$
        outerIterateElement.addElement(isEqualElement);

        innerIterateElement = new XmlElement("iterate"); //$NON-NLS-1$
        innerIterateElement.addAttribute(new Attribute("property", "oredConditions[].conditionsWithSingleValue")); //$NON-NLS-1$ //$NON-NLS-2$
        innerIterateElement.addAttribute(new Attribute("conjunction", "and")); //$NON-NLS-1$ //$NON-NLS-2$
        innerIterateElement.addElement(
                new TextElement("$oredConditions[].conditionsWithSingleValue[].condition$ #oredConditions[].conditionsWithSingleValue[].value#")); //$NON-NLS-1$
        outerIterateElement.addElement(innerIterateElement);
        
        isEqualElement = new XmlElement("isEqual"); //$NON-NLS-1$
        isEqualElement.addAttribute(new Attribute("property", //$NON-NLS-1$
                "oredConditions[].secondAndNeeded")); //$NON-NLS-1$
        isEqualElement.addAttribute(new Attribute("compareValue", //$NON-NLS-1$
                "true")); //$NON-NLS-1$
        isEqualElement.addElement(new TextElement("and")); //$NON-NLS-1$
        outerIterateElement.addElement(isEqualElement);

        innerIterateElement = new XmlElement("iterate"); //$NON-NLS-1$
        innerIterateElement.addAttribute(new Attribute("property", "oredConditions[].conditionsWithListValue")); //$NON-NLS-1$ //$NON-NLS-2$
        innerIterateElement.addAttribute(new Attribute("conjunction", "and")); //$NON-NLS-1$ //$NON-NLS-2$
        innerIterateElement.addElement(
                new TextElement("$oredConditions[].conditionsWithListValue[].condition$")); //$NON-NLS-1$
        XmlElement innerInnerIterateElement = new XmlElement("iterate"); //$NON-NLS-1$
        innerInnerIterateElement.addAttribute(new Attribute("property", //$NON-NLS-1$
                "oredConditions[].conditionsWithListValue[].values")); //$NON-NLS-1$
        innerInnerIterateElement.addAttribute(new Attribute("open", "(")); //$NON-NLS-1$ //$NON-NLS-2$
        innerInnerIterateElement.addAttribute(new Attribute("close", ")")); //$NON-NLS-1$ //$NON-NLS-2$
        innerInnerIterateElement.addAttribute(new Attribute("conjunction", ",")); //$NON-NLS-1$ //$NON-NLS-2$
        innerInnerIterateElement.addElement(new TextElement("#oredConditions[].conditionsWithListValue[].values[]#")); //$NON-NLS-1$
        innerIterateElement.addElement(innerInnerIterateElement);
        outerIterateElement.addElement(innerIterateElement);

        isEqualElement = new XmlElement("isEqual"); //$NON-NLS-1$
        isEqualElement.addAttribute(new Attribute("property", //$NON-NLS-1$
                "oredConditions[].thirdAndNeeded")); //$NON-NLS-1$
        isEqualElement.addAttribute(new Attribute("compareValue", //$NON-NLS-1$
                "true")); //$NON-NLS-1$
        isEqualElement.addElement(new TextElement("and")); //$NON-NLS-1$
        outerIterateElement.addElement(isEqualElement);

        innerIterateElement = new XmlElement("iterate"); //$NON-NLS-1$
        innerIterateElement.addAttribute(new Attribute("property", "oredConditions[].conditionsWithBetweenValue")); //$NON-NLS-1$ //$NON-NLS-2$
        innerIterateElement.addAttribute(new Attribute("conjunction", "and")); //$NON-NLS-1$ //$NON-NLS-2$
        innerIterateElement.addElement(
                new TextElement("$oredConditions[].conditionsWithBetweenValue[].condition$")); //$NON-NLS-1$
        innerIterateElement.addElement(
                new TextElement("#oredConditions[].conditionsWithBetweenValue[].values[0]# and")); //$NON-NLS-1$
        innerIterateElement.addElement(
                new TextElement("#oredConditions[].conditionsWithBetweenValue[].values[1]#")); //$NON-NLS-1$
        outerIterateElement.addElement(innerIterateElement);

        outerIterateElement.addElement(new TextElement(")")); //$NON-NLS-1$

        return answer;
    }

    /**
     * This method should an XmlElement for the select by example
     * statement that returns all fields in the table (except BLOB fields).
     * 
     * @param columnDefinitions introspected column definitions for the current table
     * @param tableConfiguration table configuration for the current table
     * @return the select element
     */
    protected XmlElement getSelectByExample(ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration) {
        FullyQualifiedJavaType fqjt = javaModelGenerator.getExampleType(tableConfiguration.getTable());

        XmlElement answer = new XmlElement("select"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", getSelectByExampleStatementId())); //$NON-NLS-1$
        answer.addAttribute(new Attribute(
                "resultMap", getResultMapName(tableConfiguration.getTable()))); //$NON-NLS-1$
        answer.addAttribute(new Attribute("parameterClass", fqjt.getFullyQualifiedName())); //$NON-NLS-1$

        answer.addComment();

        StringBuffer sb = new StringBuffer();
        sb.append("select "); //$NON-NLS-1$

        boolean comma = false;
        if (StringUtility.stringHasValue(tableConfiguration
                .getSelectByExampleQueryId())) {
            sb.append('\'');
            sb.append(tableConfiguration.getSelectByExampleQueryId());
            sb.append("' as QUERYID"); //$NON-NLS-1$
            comma = true;
        }

        Iterator iter = columnDefinitions.getAllColumns().iterator();
        while (iter.hasNext()) {
            ColumnDefinition cd = (ColumnDefinition) iter.next();

            if (cd.isBLOBColumn()) {
                // don't return BLOBs in select be examples
                continue;
            }

            if (comma) {
                sb.append(", "); //$NON-NLS-1$
            } else {
                comma = true;
            }

            sb.append(cd.getSelectListPhrase());
        }
        answer.addElement((new TextElement(sb.toString())));

        sb.setLength(0);
        sb.append("from "); //$NON-NLS-1$
        sb.append(tableConfiguration.getTable().getAliasedFullyQualifiedTableName());
        answer.addElement((new TextElement(sb.toString())));

        XmlElement includeElement = new XmlElement("include"); //$NON-NLS-1$
        includeElement.addAttribute(new Attribute("refid", //$NON-NLS-1$
                getSqlMapNamespace(tableConfiguration.getTable())
                        + "." + getExampleWhereClauseId())); //$NON-NLS-1$
        answer.addElement(includeElement);

        XmlElement isNotNullElement = new XmlElement("isNotNull"); //$NON-NLS-1$
        isNotNullElement.addAttribute(new Attribute("property", "orderByClause")); //$NON-NLS-1$ //$NON-NLS-2$
        isNotNullElement.addElement(new TextElement("order by $orderByClause$")); //$NON-NLS-1$
        answer.addElement(isNotNullElement);

        return answer;
    }

    /**
     * This method should return an XmlElement for the select by example
     * statement that returns all fields in the table (including BLOB fields).
     * 
     * @param columnDefinitions introspected column definitions for the current table
     * @param tableConfiguration table configuration for the current table
     * @return the select element
     */
    protected XmlElement getSelectByExampleWithBLOBs(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration) {

        FullyQualifiedJavaType fqjt = javaModelGenerator.getExampleType(tableConfiguration.getTable());
        
        XmlElement answer = new XmlElement("select"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("id", getSelectByExampleWithBLOBsStatementId())); //$NON-NLS-1$
        answer.addAttribute(
                new Attribute("resultMap", getResultMapName(tableConfiguration.getTable()) + "WithBLOBs")); //$NON-NLS-1$ //$NON-NLS-2$
        answer.addAttribute(new Attribute("parameterClass", fqjt.getFullyQualifiedName())); //$NON-NLS-1$

        answer.addComment();

        StringBuffer sb = new StringBuffer();
        sb.append("select "); //$NON-NLS-1$

        boolean comma = false;

        if (StringUtility.stringHasValue(tableConfiguration
                .getSelectByExampleQueryId())) {
            sb.append('\'');
            sb.append(tableConfiguration.getSelectByExampleQueryId());
            sb.append("' as QUERYID"); //$NON-NLS-1$
            comma = true;
        }

        Iterator iter = columnDefinitions.getAllColumns().iterator();
        while (iter.hasNext()) {
            ColumnDefinition cd = (ColumnDefinition) iter.next();

            if (comma) {
                sb.append(", "); //$NON-NLS-1$
            } else {
                comma = true;
            }

            sb.append(cd.getSelectListPhrase());
        }
        answer.addElement(new TextElement(sb.toString()));

        sb.setLength(0);
        sb.append("from "); //$NON-NLS-1$
        sb.append(tableConfiguration.getTable().getAliasedFullyQualifiedTableName());
        answer.addElement(new TextElement(sb.toString()));

        XmlElement includeElement = new XmlElement("include"); //$NON-NLS-1$
        includeElement.addAttribute(new Attribute("refid", //$NON-NLS-1$
                getSqlMapNamespace(tableConfiguration.getTable())
                        + "." + getExampleWhereClauseId())); //$NON-NLS-1$
        answer.addElement(includeElement);

        XmlElement isNotNullElement = new XmlElement("isNotNull"); //$NON-NLS-1$
        isNotNullElement.addAttribute(new Attribute("property", "orderByClause")); //$NON-NLS-1$ //$NON-NLS-2$
        isNotNullElement.addElement(new TextElement("order by $orderByClause$")); //$NON-NLS-1$
        answer.addElement(isNotNullElement);

        return answer;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.abator.api.SqlMapGenerator#setTargetProject(java.lang.String)
     */
    public void setTargetProject(String targetProject) {
        this.targetProject = targetProject;
    }
    
	/* (non-Javadoc)
	 * @see org.apache.ibatis.abator.api.SqlMapGenerator#setWarnings(java.util.List)
	 */
	public void setWarnings(List warnings) {
		this.warnings = warnings;
	}
}
