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
package org.apache.ibatis.abator.internal.java.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.abator.api.DAOGenerator;
import org.apache.ibatis.abator.api.FullyQualifiedJavaType;
import org.apache.ibatis.abator.api.GeneratedJavaFile;
import org.apache.ibatis.abator.api.JavaModelGenerator;
import org.apache.ibatis.abator.api.ProgressCallback;
import org.apache.ibatis.abator.api.SqlMapGenerator;
import org.apache.ibatis.abator.config.FullyQualifiedTable;
import org.apache.ibatis.abator.config.TableConfiguration;
import org.apache.ibatis.abator.internal.db.ColumnDefinition;
import org.apache.ibatis.abator.internal.db.ColumnDefinitions;
import org.apache.ibatis.abator.internal.java.BaseJavaCodeGenerator;
import org.apache.ibatis.abator.internal.rules.AbatorRules;
import org.apache.ibatis.abator.internal.util.StringUtility;
import org.apache.ibatis.abator.internal.util.messages.Messages;

/**
 * This class generates DAO classes based on the values in the supplied
 * DAOTemplate.
 * 
 * This class supports the following properties:
 * 
 * <dl>
 *   <dt>enableSubPackages<dt>
 *   <dd>If true, the classes will be generated in sub-packaged based on the
 *       database catalg and schema - else the will be generated in the specified
 *       package (the targetPackage attribute).  Default is false.</dd>
 * 
 *   <dt>rootInterface<dt>
 *   <dd>If specified, then the root interface of the DAO interface class will be set to
 *       the specified value.  No checking is done to see if the specified interface exists,
 *       or if the generated interface overrides any root interface methods.</dd>
 * </dl>
 * 
 * @author Jeff Butler
 */
public class BaseJava2DAOGenerator extends BaseJavaCodeGenerator implements
        DAOGenerator {

    private AbstractDAOTemplate daoTemplate;
    protected Map properties;
    protected List warnings;
    protected String targetPackage;
    protected String targetProject;
    protected JavaModelGenerator javaModelGenerator;
    protected SqlMapGenerator sqlMapGenerator;
    private Map tableValueMaps;
    
    /**
     * 
     */
    public BaseJava2DAOGenerator(AbstractDAOTemplate daoTemplate) {
        super();
        this.daoTemplate = daoTemplate;
        tableValueMaps = new HashMap();
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.api.DAOGenerator#setProperties(java.util.Map)
     */
    public void setProperties(Map properties) {
        this.properties = properties;
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.api.DAOGenerator#setWarnings(java.util.List)
     */
    public void setWarnings(List warnings) {
        this.warnings = warnings;
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.api.DAOGenerator#setTargetPackage(java.lang.String)
     */
    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.api.DAOGenerator#setTargetProject(java.lang.String)
     */
    public void setTargetProject(String targetProject) {
        this.targetProject = targetProject;
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.api.DAOGenerator#setJavaModelGenerator(org.apache.ibatis.abator.api.JavaModelGenerator)
     */
    public void setJavaModelGenerator(JavaModelGenerator javaModelGenerator) {
        this.javaModelGenerator = javaModelGenerator;
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.api.DAOGenerator#setSqlMapGenerator(org.apache.ibatis.abator.api.SqlMapGenerator)
     */
    public void setSqlMapGenerator(SqlMapGenerator sqlMapGenerator) {
        this.sqlMapGenerator = sqlMapGenerator;
    }

    /* (non-Javadoc)
     * @see org.apache.ibatis.abator.api.DAOGenerator#getGeneratedJavaFiles(org.apache.ibatis.abator.internal.db.ColumnDefinitions, org.apache.ibatis.abator.config.TableConfiguration, org.apache.ibatis.abator.api.ProgressCallback)
     */
    public List getGeneratedJavaFiles(ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration, ProgressCallback callback) {
        List list = new ArrayList();

        String tableName = tableConfiguration.getTable()
                .getFullyQualifiedTableName();

        callback.startSubTask(Messages.getString("DAOGeneratorBaseImpl.0", //$NON-NLS-1$
                tableName));
        list.add(getDAOImplementation(columnDefinitions, tableConfiguration));

        callback.startSubTask(Messages.getString("DAOGeneratorBaseImpl.1", //$NON-NLS-1$
                tableName));
        list.add(getDAOInterface(columnDefinitions, tableConfiguration));

        return list;
    }

    protected GeneratedJavaFile getDAOImplementation(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration) {

        FullyQualifiedTable table = tableConfiguration.getTable();
        GeneratedJavaFile answer = new GeneratedJavaFile(
                getDAOImplementationType(table));

        answer.setJavaInterface(false);
        if (daoTemplate.getSuperClass() != null) {
            answer.setSuperClass(daoTemplate.getSuperClass());
        }

        answer.addSuperInterfaceType(getDAOInterfaceType(table));

        answer.setTargetProject(targetProject);

        Iterator iter = daoTemplate.getImplementationImports()
                .iterator();
        while (iter.hasNext()) {
            answer.addImportedType((FullyQualifiedJavaType) iter.next());
        }

        StringBuffer buffer = new StringBuffer();

        // add constructor
        buffer.append(getMethodComment(table));
        newLine(buffer);
        buffer.append(daoTemplate
                .getConstructor(getDAOImplementationType(table)));
        answer.addMethod(buffer.toString());

        // add any fields from the template
        iter = daoTemplate.getFields().iterator();
        while (iter.hasNext()) {
            buffer.setLength(0);
            buffer.append(getFieldComment(table));
            newLine(buffer);
            indent(buffer, 1);
            buffer.append(iter.next());
            answer.addField(buffer.toString());
        }

        // add any methods from the template
        iter = daoTemplate.getMethods().iterator();
        while (iter.hasNext()) {
            buffer.setLength(0);
            buffer.append(getMethodComment(table));
            newLine(buffer);
            buffer.append(iter.next());
            answer.addMethod(buffer.toString());
        }

        List methods = getInsertMethods(columnDefinitions, tableConfiguration,
                false, answer.getImportedTypes());
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((String) iter.next());
            }
        }

        methods = getUpdateByPrimaryKeyMethods(columnDefinitions,
                tableConfiguration, false, answer.getImportedTypes());
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((String) iter.next());
            }
        }

        methods = getUpdateByPrimaryKeyWithBLOBsMethods(columnDefinitions,
                tableConfiguration, false, answer.getImportedTypes());
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((String) iter.next());
            }
        }

        methods = getSelectByExampleMethods(columnDefinitions,
                tableConfiguration, false, answer.getImportedTypes());
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((String) iter.next());
            }
        }

        methods = getSelectByExampleWithBLOBsMethods(columnDefinitions,
                tableConfiguration, false, answer.getImportedTypes());
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((String) iter.next());
            }
        }

        methods = getSelectByPrimaryKeyMethods(columnDefinitions,
                tableConfiguration, false, answer.getImportedTypes());
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((String) iter.next());
            }
        }

        methods = getDeleteByExampleMethods(columnDefinitions,
                tableConfiguration, false, answer.getImportedTypes());
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((String) iter.next());
            }
        }

        methods = getDeleteByPrimaryKeyMethods(columnDefinitions,
                tableConfiguration, false, answer.getImportedTypes());
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((String) iter.next());
            }
        }

        return answer;
    }

    protected GeneratedJavaFile getDAOInterface(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration) {
        FullyQualifiedTable table = tableConfiguration.getTable();
        GeneratedJavaFile answer = new GeneratedJavaFile(
                getDAOInterfaceType(table));

        answer.setJavaInterface(true);

        answer.setTargetProject(targetProject);

		if (properties.containsKey("rootInterface")) { //$NON-NLS-1$
	        answer.setSuperClass(new FullyQualifiedJavaType((String) properties.get("rootInterface"))); //$NON-NLS-1$
	    }

        Iterator iter = daoTemplate.getInterfaceImports().iterator();
        while (iter.hasNext()) {
            answer.addImportedType((FullyQualifiedJavaType) iter.next());
        }

        List methods = getInsertMethods(columnDefinitions, tableConfiguration,
                true, answer.getImportedTypes());
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((String) iter.next());
            }
        }

        methods = getUpdateByPrimaryKeyMethods(columnDefinitions,
                tableConfiguration, true, answer.getImportedTypes());
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((String) iter.next());
            }
        }

        methods = getUpdateByPrimaryKeyWithBLOBsMethods(columnDefinitions,
                tableConfiguration, true, answer.getImportedTypes());
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((String) iter.next());
            }
        }

        methods = getSelectByExampleMethods(columnDefinitions,
                tableConfiguration, true, answer.getImportedTypes());
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((String) iter.next());
            }
        }

        methods = getSelectByExampleWithBLOBsMethods(columnDefinitions,
                tableConfiguration, true, answer.getImportedTypes());
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((String) iter.next());
            }
        }

        methods = getSelectByPrimaryKeyMethods(columnDefinitions,
                tableConfiguration, true, answer.getImportedTypes());
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((String) iter.next());
            }
        }

        methods = getDeleteByExampleMethods(columnDefinitions,
                tableConfiguration, true, answer.getImportedTypes());
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((String) iter.next());
            }
        }

        methods = getDeleteByPrimaryKeyMethods(columnDefinitions,
                tableConfiguration, true, answer.getImportedTypes());
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((String) iter.next());
            }
        }

        return answer;
    }

    protected FullyQualifiedJavaType getDAOImplementationType(
            FullyQualifiedTable table) {
        String key = "getDAOImplementationType"; //$NON-NLS-1$

        Map map = getTableValueMap(table);
        FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) map.get(key);
        if (fqjt == null) {
            StringBuffer sb = new StringBuffer();
            sb.append(getDAOPackage(table));
            sb.append('.');
            sb.append(table.getDomainObjectName());
            sb.append("DAOImpl"); //$NON-NLS-1$

            fqjt = new FullyQualifiedJavaType(sb.toString());
            map.put(key, fqjt);
        }

        return fqjt;
    }
    
    protected List getInsertMethods(ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration, boolean interfaceMethod,
            Set imports) {

        if (!AbatorRules.generateInsert(tableConfiguration)) {
            return null;
        }

        String returnType;
        if (tableConfiguration.getGeneratedKey().isConfigured()) {
            ColumnDefinition cd = columnDefinitions
                    .getColumn(tableConfiguration.getGeneratedKey().getColumn());
            if (cd == null) {
                // the specified column doesn't exist, so don't do the generated
                // key
                // (the warning has already been reported)
                returnType = "void"; //$NON-NLS-1$
            } else {
                FullyQualifiedJavaType fqjt = cd.getResolvedJavaType()
                        .getFullyQualifiedJavaType();
                returnType = fqjt.getShortName();
                imports.add(fqjt);
            }
        } else {
            returnType = "void"; //$NON-NLS-1$
        }

        StringBuffer buffer = new StringBuffer();
        FullyQualifiedTable table = tableConfiguration.getTable();
        buffer.append(getMethodComment(table));
        newLine(buffer);
        indent(buffer, 1);
        if (!interfaceMethod) {
            buffer.append("public "); //$NON-NLS-1$
        }
        buffer.append(returnType);
        buffer.append(" insert("); //$NON-NLS-1$
        if (AbatorRules.generateRecordWithBLOBsExtendingPrimaryKey(columnDefinitions)
                || AbatorRules.generateRecordWithBLOBsExtendingBaseRecord(columnDefinitions)) {
            buffer.append(javaModelGenerator.getRecordWithBLOBsType(table)
                    .getShortName());
            imports.add(javaModelGenerator.getRecordWithBLOBsType(table));
        } else if (AbatorRules.generateBaseRecordWithNoSuperclass(columnDefinitions)
                || AbatorRules.generateBaseRecordExtendingPrimaryKey(columnDefinitions)) {
            buffer.append(javaModelGenerator.getRecordType(table)
                    .getShortName());
            imports.add(javaModelGenerator.getRecordType(table));
        } else {
            buffer.append(javaModelGenerator.getPrimaryKeyType(table)
                    .getShortName());
            imports.add(javaModelGenerator.getPrimaryKeyType(table));
        }

        buffer.append(" record)"); //$NON-NLS-1$
        if (daoTemplate.getCheckedExceptions().size() > 0) {
            buffer.append(" throws "); //$NON-NLS-1$

            Iterator iter = daoTemplate.getCheckedExceptions()
                    .iterator();
            boolean comma = false;
            while (iter.hasNext()) {
                if (comma) {
                    buffer.append(", "); //$NON-NLS-1$
                } else {
                    comma = true;
                }

                FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) iter
                        .next();
                buffer.append(fqjt.getShortName());
                imports.add(fqjt);
            }
        }

        if (interfaceMethod) {
            // generate the interface method
            buffer.append(';');
        } else {
            // generate the implementation method
            buffer.append(" {"); //$NON-NLS-1$

            newLine(buffer);
            indent(buffer, 2);
            if (!"void".equals(returnType)) { //$NON-NLS-1$
                buffer.append("Object newKey = "); //$NON-NLS-1$
            }

            buffer.append(daoTemplate.getInsertMethod());
            buffer.append("(\""); //$NON-NLS-1$
            buffer.append(sqlMapGenerator.getSqlMapNamespace(table));
            buffer.append('.');
            buffer.append(sqlMapGenerator.getInsertStatementId());
            buffer.append("\", record);"); //$NON-NLS-1$

            if ("Object".equals(returnType)) { //$NON-NLS-1$
                newLine(buffer);
                newLine(buffer);
                indent(buffer, 2);
                buffer.append("return newKey;"); //$NON-NLS-1$
            } else if (!"void".equals(returnType)) { //$NON-NLS-1$
                newLine(buffer);
                newLine(buffer);
                indent(buffer, 2);
                buffer.append("return ("); //$NON-NLS-1$
                buffer.append(returnType);
                buffer.append(") newKey;"); //$NON-NLS-1$
            }

            newLine(buffer);
            indent(buffer, 1);
            buffer.append('}');
        }

        ArrayList answer = new ArrayList();
        answer.add(buffer.toString());

        return answer;
    }

    /**
     * 
     * @param columnDefinitions
     *            column definitions for the current table
     * @param tableConfiguration
     *            table configuration for the current table
     * @param interfaceMethod
     *            true if the method is an interface method, false if the method
     *            is an implementation method
     * @param imports
     *            the method can add FullyQualifiedJavaType objects to this set
     *            if they are required by the resulting method
     * @return a List of methods (as Strings). A method includes Javadoc.
     */
    protected List getUpdateByPrimaryKeyMethods(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration, boolean interfaceMethod,
            Set imports) {

        if (!AbatorRules.generateUpdateByPrimaryKeyWithoutBLOBs(columnDefinitions, tableConfiguration)) {
            return null;
        }

        StringBuffer buffer = new StringBuffer();
        FullyQualifiedTable table = tableConfiguration.getTable();
        imports.add(javaModelGenerator.getRecordType(table));

        buffer.append(getMethodComment(table));
        newLine(buffer);
        indent(buffer, 1);
        if (!interfaceMethod) {
            buffer.append("public "); //$NON-NLS-1$
        }
        buffer.append("int updateByPrimaryKey("); //$NON-NLS-1$
        buffer.append(javaModelGenerator.getRecordType(table).getShortName());
        buffer.append(" record)"); //$NON-NLS-1$
        if (daoTemplate.getCheckedExceptions().size() > 0) {
            buffer.append(" throws "); //$NON-NLS-1$

            Iterator iter = daoTemplate.getCheckedExceptions()
                    .iterator();
            boolean comma = false;
            while (iter.hasNext()) {
                if (comma) {
                    buffer.append(", "); //$NON-NLS-1$
                } else {
                    comma = true;
                }

                FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) iter
                        .next();
                buffer.append(fqjt.getShortName());
                imports.add(fqjt);
            }
        }

        if (interfaceMethod) {
            // generate the interface method
            buffer.append(';');
        } else {
            // generate the implementation method
            buffer.append(" {"); //$NON-NLS-1$

            newLine(buffer);
            indent(buffer, 2);
            buffer.append("int rows = "); //$NON-NLS-1$
            buffer.append(daoTemplate.getUpdateMethod());
            buffer.append("(\""); //$NON-NLS-1$
            buffer.append(sqlMapGenerator.getSqlMapNamespace(table));
            buffer.append('.');
            buffer.append(sqlMapGenerator.getUpdateByPrimaryKeyStatementId());
            buffer.append("\", record);"); //$NON-NLS-1$

            newLine(buffer);
            newLine(buffer);
            indent(buffer, 2);
            buffer.append("return rows;"); //$NON-NLS-1$

            newLine(buffer);
            indent(buffer, 1);
            buffer.append('}');
        }

        ArrayList answer = new ArrayList();
        answer.add(buffer.toString());

        return answer;
    }

    protected List getUpdateByPrimaryKeyWithBLOBsMethods(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration, boolean interfaceMethod,
            Set imports) {

        if (!AbatorRules
                .generateUpdateByPrimaryKeyWithBLOBs(columnDefinitions, tableConfiguration)) {
            return null;
        }

        StringBuffer buffer = new StringBuffer();
        FullyQualifiedTable table = tableConfiguration.getTable();
        imports.add(javaModelGenerator.getRecordWithBLOBsType(table));

        buffer.append(getMethodComment(table));
        newLine(buffer);
        indent(buffer, 1);
        if (!interfaceMethod) {
            buffer.append("public "); //$NON-NLS-1$
        }
        buffer.append("int updateByPrimaryKey("); //$NON-NLS-1$
        buffer.append(javaModelGenerator.getRecordWithBLOBsType(table)
                .getShortName());
        buffer.append(" record)"); //$NON-NLS-1$
        if (daoTemplate.getCheckedExceptions().size() > 0) {
            buffer.append(" throws "); //$NON-NLS-1$

            Iterator iter = daoTemplate.getCheckedExceptions()
                    .iterator();
            boolean comma = false;
            while (iter.hasNext()) {
                if (comma) {
                    buffer.append(", "); //$NON-NLS-1$
                } else {
                    comma = true;
                }

                FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) iter
                        .next();
                buffer.append(fqjt.getShortName());
                imports.add(fqjt);
            }
        }

        if (interfaceMethod) {
            // generate the interface method
            buffer.append(';');
        } else {
            // generate the implementation method
            buffer.append(" {"); //$NON-NLS-1$

            newLine(buffer);
            indent(buffer, 2);
            buffer.append("int rows = "); //$NON-NLS-1$
            buffer.append(daoTemplate.getUpdateMethod());
            buffer.append("(\""); //$NON-NLS-1$
            buffer.append(sqlMapGenerator.getSqlMapNamespace(table));
            buffer.append('.');
            buffer.append(sqlMapGenerator
                    .getUpdateByPrimaryKeyWithBLOBsStatementId());
            buffer.append("\", record);"); //$NON-NLS-1$

            newLine(buffer);
            newLine(buffer);
            indent(buffer, 2);
            buffer.append("return rows;"); //$NON-NLS-1$

            newLine(buffer);
            indent(buffer, 1);
            buffer.append('}');
        }

        ArrayList answer = new ArrayList();
        answer.add(buffer.toString());

        return answer;
    }

    protected List getSelectByExampleMethods(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration, boolean interfaceMethod,
            Set imports) {

        // TODO - remove the overloaded - and deprecated - select by example method after the next release
        if (!AbatorRules.generateSelectByExampleWithoutBLOBs(tableConfiguration)) {
            return null;
        }

        StringBuffer buffer1 = new StringBuffer();
        StringBuffer buffer2 = new StringBuffer();
        FullyQualifiedTable table = tableConfiguration.getTable();
        imports.add(javaModelGenerator.getExampleType(table));
        imports.add(new FullyQualifiedJavaType("java.util.List")); //$NON-NLS-1$

        buffer1.append(getMethodComment(table,
                "Set the order by clause directly in the example object")); //$NON-NLS-1$
        newLine(buffer1);
        indent(buffer1, 1);
        if (!interfaceMethod) {
            buffer1.append("public "); //$NON-NLS-1$
        }
        buffer1.append("List selectByExample("); //$NON-NLS-1$
        buffer1.append(javaModelGenerator.getExampleType(table).getShortName());
        buffer1.append(" example, String orderByClause)"); //$NON-NLS-1$

        buffer2.append(getMethodComment(table));
        newLine(buffer2);
        indent(buffer2, 1);
        if (!interfaceMethod) {
            buffer2.append("public "); //$NON-NLS-1$
        }
        buffer2.append("List selectByExample("); //$NON-NLS-1$
        buffer2.append(javaModelGenerator.getExampleType(table).getShortName());
        buffer2.append(" example)"); //$NON-NLS-1$

        if (daoTemplate.getCheckedExceptions().size() > 0) {
            buffer1.append(" throws "); //$NON-NLS-1$
            buffer2.append(" throws "); //$NON-NLS-1$

            Iterator iter = daoTemplate.getCheckedExceptions()
                    .iterator();
            boolean comma = false;
            while (iter.hasNext()) {
                if (comma) {
                    buffer1.append(", "); //$NON-NLS-1$
                    buffer2.append(", "); //$NON-NLS-1$
                } else {
                    comma = true;
                }

                FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) iter
                        .next();
                buffer1.append(fqjt.getShortName());
                buffer2.append(fqjt.getShortName());
                imports.add(fqjt);
            }
        }

        if (interfaceMethod) {
            // generate the interface method
            buffer1.append(';');
            buffer2.append(';');
        } else {
            // generate the implementation method
            buffer1.append(" {"); //$NON-NLS-1$

            newLine(buffer1);
            indent(buffer1, 2);
            buffer1.append("example.setOrderByClause(orderByClause);"); //$NON-NLS-1$

            newLine(buffer1);
            newLine(buffer1);
            indent(buffer1, 2);
            buffer1.append("return selectByExample(example);"); //$NON-NLS-1$
            newLine(buffer1);
            indent(buffer1, 1);
            buffer1.append('}');

            buffer2.append(" {"); //$NON-NLS-1$

            newLine(buffer2);
            indent(buffer2, 2);
            buffer2.append("List list = "); //$NON-NLS-1$
            buffer2.append(daoTemplate.getQueryForListMethod());
            buffer2.append("(\""); //$NON-NLS-1$
            buffer2.append(sqlMapGenerator.getSqlMapNamespace(table));
            buffer2.append('.');
            buffer2.append(sqlMapGenerator.getSelectByExampleStatementId());
            buffer2.append("\", example);"); //$NON-NLS-1$

            newLine(buffer2);
            newLine(buffer2);
            indent(buffer2, 2);
            buffer2.append("return list;"); //$NON-NLS-1$
            newLine(buffer2);
            indent(buffer2, 1);
            buffer2.append('}');
        }

        ArrayList answer = new ArrayList();
        answer.add(buffer1.toString());
        answer.add(buffer2.toString());

        return answer;
    }

    protected List getSelectByExampleWithBLOBsMethods(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration, boolean interfaceMethod,
            Set imports) {

        if (!AbatorRules
                .generateSelectByExampleWithBLOBs(columnDefinitions, tableConfiguration)) {
            return null;
        }

        StringBuffer buffer1 = new StringBuffer();
        StringBuffer buffer2 = new StringBuffer();
        FullyQualifiedTable table = tableConfiguration.getTable();
        imports.add(javaModelGenerator.getExampleType(table));
        imports.add(new FullyQualifiedJavaType("java.util.List")); //$NON-NLS-1$

        buffer1.append(getMethodComment(table,
                "Set the order by clause directly in the example object")); //$NON-NLS-1$
        newLine(buffer1);
        indent(buffer1, 1);
        if (!interfaceMethod) {
            buffer1.append("public "); //$NON-NLS-1$
        }
        buffer1.append("List selectByExampleWithBLOBs("); //$NON-NLS-1$
        buffer1.append(javaModelGenerator.getExampleType(table).getShortName());
        buffer1.append(" example, String orderByClause)"); //$NON-NLS-1$

        buffer2.append(getMethodComment(table));
        newLine(buffer2);
        indent(buffer2, 1);
        if (!interfaceMethod) {
            buffer2.append("public "); //$NON-NLS-1$
        }
        buffer2.append("List selectByExampleWithBLOBs("); //$NON-NLS-1$
        buffer2.append(javaModelGenerator.getExampleType(table).getShortName());
        buffer2.append(" example)"); //$NON-NLS-1$

        if (daoTemplate.getCheckedExceptions().size() > 0) {
            buffer1.append(" throws "); //$NON-NLS-1$
            buffer2.append(" throws "); //$NON-NLS-1$

            Iterator iter = daoTemplate.getCheckedExceptions()
                    .iterator();
            boolean comma = false;
            while (iter.hasNext()) {
                if (comma) {
                    buffer1.append(", "); //$NON-NLS-1$
                    buffer2.append(", "); //$NON-NLS-1$
                } else {
                    comma = true;
                }

                FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) iter
                        .next();
                buffer1.append(fqjt.getShortName());
                buffer2.append(fqjt.getShortName());
                imports.add(fqjt);
            }
        }

        if (interfaceMethod) {
            // generate the interface method
            buffer1.append(';');
            buffer2.append(';');
        } else {
            // generate the implementation method
            buffer1.append(" {"); //$NON-NLS-1$

            newLine(buffer1);
            indent(buffer1, 2);
            buffer1.append("example.setOrderByClause(orderByClause);"); //$NON-NLS-1$

            newLine(buffer1);
            newLine(buffer1);
            indent(buffer1, 2);
            buffer1.append("return selectByExampleWithBLOBs(example);"); //$NON-NLS-1$
            newLine(buffer1);
            indent(buffer1, 1);
            buffer1.append('}');

            buffer2.append(" {"); //$NON-NLS-1$

            newLine(buffer2);
            indent(buffer2, 2);
            buffer2.append("List list = "); //$NON-NLS-1$
            buffer2.append(daoTemplate.getQueryForListMethod());
            buffer2.append("(\""); //$NON-NLS-1$
            buffer2.append(sqlMapGenerator.getSqlMapNamespace(table));
            buffer2.append('.');
            buffer2.append(sqlMapGenerator.getSelectByExampleWithBLOBsStatementId());
            buffer2.append("\", example);"); //$NON-NLS-1$

            newLine(buffer2);
            newLine(buffer2);
            indent(buffer2, 2);
            buffer2.append("return list;"); //$NON-NLS-1$
            newLine(buffer2);
            indent(buffer2, 1);
            buffer2.append('}');
        }

        ArrayList answer = new ArrayList();
        answer.add(buffer1.toString());
        answer.add(buffer2.toString());

        return answer;
    }

    protected List getSelectByPrimaryKeyMethods(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration, boolean interfaceMethod,
            Set imports) {

        if (!AbatorRules.generateSelectByPrimaryKey(columnDefinitions, tableConfiguration)) {
            return null;
        }

        StringBuffer buffer = new StringBuffer();
        FullyQualifiedTable table = tableConfiguration.getTable();
        imports.add(javaModelGenerator.getPrimaryKeyType(table));

        buffer.append(getMethodComment(table));
        newLine(buffer);
        indent(buffer, 1);

        if (!interfaceMethod) {
            buffer.append("public "); //$NON-NLS-1$
        }

        FullyQualifiedJavaType returnType;
        if (AbatorRules.generateRecordWithBLOBsExtendingPrimaryKey(columnDefinitions)
                || AbatorRules.generateRecordWithBLOBsExtendingBaseRecord(columnDefinitions)) {
            returnType = javaModelGenerator.getRecordWithBLOBsType(table);

        } else {
            returnType = javaModelGenerator.getRecordType(table);
        }
        buffer.append(returnType.getShortName());
        imports.add(returnType);

        buffer.append(" selectByPrimaryKey("); //$NON-NLS-1$
        buffer.append(javaModelGenerator.getPrimaryKeyType(table)
                .getShortName());
        buffer.append(" key)"); //$NON-NLS-1$
        if (daoTemplate.getCheckedExceptions().size() > 0) {
            buffer.append(" throws "); //$NON-NLS-1$

            Iterator iter = daoTemplate.getCheckedExceptions()
                    .iterator();
            boolean comma = false;
            while (iter.hasNext()) {
                if (comma) {
                    buffer.append(", "); //$NON-NLS-1$
                } else {
                    comma = true;
                }

                FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) iter
                        .next();
                buffer.append(fqjt.getShortName());
                imports.add(fqjt);
            }
        }

        if (interfaceMethod) {
            // generate the interface method
            buffer.append(';');
        } else {
            // generate the implementation method
            buffer.append(" {"); //$NON-NLS-1$

            newLine(buffer);
            indent(buffer, 2);
            buffer.append(returnType.getShortName());
            buffer.append(" record = ("); //$NON-NLS-1$
            buffer.append(returnType.getShortName());
            buffer.append(") "); //$NON-NLS-1$
            buffer.append(daoTemplate.getQueryForObjectMethod());
            buffer.append("(\""); //$NON-NLS-1$
            buffer.append(sqlMapGenerator.getSqlMapNamespace(table));
            buffer.append('.');
            buffer.append(sqlMapGenerator.getSelectByPrimaryKeyStatementId());
            buffer.append("\", key);"); //$NON-NLS-1$

            newLine(buffer);
            newLine(buffer);
            indent(buffer, 2);
            buffer.append("return record;"); //$NON-NLS-1$

            newLine(buffer);
            indent(buffer, 1);
            buffer.append('}');
        }

        ArrayList answer = new ArrayList();
        answer.add(buffer.toString());

        return answer;
    }

    protected List getDeleteByExampleMethods(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration, boolean interfaceMethod,
            Set imports) {

        if (!AbatorRules.generateDeleteByExample(tableConfiguration)) {
            return null;
        }

        StringBuffer buffer = new StringBuffer();
        FullyQualifiedTable table = tableConfiguration.getTable();
        imports.add(javaModelGenerator.getExampleType(table));

        buffer.append(getMethodComment(table));
        newLine(buffer);
        indent(buffer, 1);
        if (!interfaceMethod) {
            buffer.append("public "); //$NON-NLS-1$
        }
        buffer.append("int deleteByExample("); //$NON-NLS-1$
        buffer.append(javaModelGenerator.getExampleType(table).getShortName());
        buffer.append(" example)"); //$NON-NLS-1$
        if (daoTemplate.getCheckedExceptions().size() > 0) {
            buffer.append(" throws "); //$NON-NLS-1$

            Iterator iter = daoTemplate.getCheckedExceptions()
                    .iterator();
            boolean comma = false;
            while (iter.hasNext()) {
                if (comma) {
                    buffer.append(", "); //$NON-NLS-1$
                } else {
                    comma = true;
                }

                FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) iter
                        .next();
                buffer.append(fqjt.getShortName());
                imports.add(fqjt);
            }
        }

        if (interfaceMethod) {
            // generate the interface method
            buffer.append(';');
        } else {
            // generate the implementation method
            buffer.append(" {"); //$NON-NLS-1$

            newLine(buffer);
            indent(buffer, 2);
            buffer.append("int rows = "); //$NON-NLS-1$
            buffer.append(daoTemplate.getDeleteMethod());
            buffer.append("(\""); //$NON-NLS-1$
            buffer.append(sqlMapGenerator.getSqlMapNamespace(table));
            buffer.append('.');
            buffer.append(sqlMapGenerator.getDeleteByExampleStatementId());
            buffer.append("\", example);"); //$NON-NLS-1$

            newLine(buffer);
            newLine(buffer);
            indent(buffer, 2);
            buffer.append("return rows;"); //$NON-NLS-1$

            newLine(buffer);
            indent(buffer, 1);
            buffer.append('}');
        }

        ArrayList answer = new ArrayList();
        answer.add(buffer.toString());

        return answer;
    }

    protected List getDeleteByPrimaryKeyMethods(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration, boolean interfaceMethod,
            Set imports) {

        if (!AbatorRules.generateDeleteByPrimaryKey(columnDefinitions, tableConfiguration)) {
            return null;
        }

        StringBuffer buffer = new StringBuffer();
        FullyQualifiedTable table = tableConfiguration.getTable();
        imports.add(javaModelGenerator.getPrimaryKeyType(table));

        buffer.append(getMethodComment(table));
        newLine(buffer);
        indent(buffer, 1);
        if (!interfaceMethod) {
            buffer.append("public "); //$NON-NLS-1$
        }
        buffer.append("int deleteByPrimaryKey("); //$NON-NLS-1$
        buffer.append(javaModelGenerator.getPrimaryKeyType(table)
                .getShortName());
        buffer.append(" key)"); //$NON-NLS-1$
        if (daoTemplate.getCheckedExceptions().size() > 0) {
            buffer.append(" throws "); //$NON-NLS-1$

            Iterator iter = daoTemplate.getCheckedExceptions()
                    .iterator();
            boolean comma = false;
            while (iter.hasNext()) {
                if (comma) {
                    buffer.append(", "); //$NON-NLS-1$
                } else {
                    comma = true;
                }

                FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) iter
                        .next();
                buffer.append(fqjt.getShortName());
                imports.add(fqjt);
            }
        }

        if (interfaceMethod) {
            // generate the interface method
            buffer.append(';');
        } else {
            // generate the implementation method
            buffer.append(" {"); //$NON-NLS-1$

            newLine(buffer);
            indent(buffer, 2);
            buffer.append("int rows = "); //$NON-NLS-1$
            buffer.append(daoTemplate.getDeleteMethod());
            buffer.append("(\""); //$NON-NLS-1$
            buffer.append(sqlMapGenerator.getSqlMapNamespace(table));
            buffer.append('.');
            buffer.append(sqlMapGenerator.getDeleteByPrimaryKeyStatementId());
            buffer.append("\", key);"); //$NON-NLS-1$

            newLine(buffer);
            newLine(buffer);
            indent(buffer, 2);
            buffer.append("return rows;"); //$NON-NLS-1$

            newLine(buffer);
            indent(buffer, 1);
            buffer.append('}');
        }

        ArrayList answer = new ArrayList();
        answer.add(buffer.toString());

        return answer;
    }

    protected String getDAOPackage(FullyQualifiedTable table) {
        String key = "getDAOPackage"; //$NON-NLS-1$
        String s;

        Map map = getTableValueMap(table);
        s = (String) map.get(key);
        if (s == null) {
            StringBuffer sb = new StringBuffer(targetPackage);
            if ("true".equals(properties.get("enableSubPackages"))) { //$NON-NLS-1$  //$NON-NLS-2$
                if (StringUtility.stringHasValue(table.getCatalog())) {
                    sb.append('.');
                    sb.append(table.getCatalog().toLowerCase());
                }

                if (StringUtility.stringHasValue(table.getSchema())) {
                    sb.append('.');
                    sb.append(table.getSchema().toLowerCase());
                }
            }

            s = sb.toString();
            map.put(key, s);
        }

        return s;
    }

    protected FullyQualifiedJavaType getDAOInterfaceType(
            FullyQualifiedTable table) {
        String key = "getDAOInterfaceType"; //$NON-NLS-1$

        Map map = getTableValueMap(table);
        FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) map.get(key);
        if (fqjt == null) {
            StringBuffer sb = new StringBuffer();
            sb.append(getDAOPackage(table));
            sb.append('.');
            sb.append(table.getDomainObjectName());
            sb.append("DAO"); //$NON-NLS-1$

            fqjt = new FullyQualifiedJavaType(sb.toString());
            map.put(key, fqjt);
        }

        return fqjt;
    }

    private Map getTableValueMap(FullyQualifiedTable table) {
        Map map = (Map) tableValueMaps.get(table);
        if (map == null) {
            map = new HashMap();
            tableValueMaps.put(table, map);
        }

        return map;
    }
}
