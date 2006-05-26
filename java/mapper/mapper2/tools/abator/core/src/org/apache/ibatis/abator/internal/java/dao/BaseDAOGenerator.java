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

import org.apache.ibatis.abator.api.DAOGenerator;
import org.apache.ibatis.abator.api.GeneratedJavaFile;
import org.apache.ibatis.abator.api.JavaModelGenerator;
import org.apache.ibatis.abator.api.ProgressCallback;
import org.apache.ibatis.abator.api.SqlMapGenerator;
import org.apache.ibatis.abator.api.dom.java.CompilationUnit;
import org.apache.ibatis.abator.api.dom.java.Field;
import org.apache.ibatis.abator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.abator.api.dom.java.Interface;
import org.apache.ibatis.abator.api.dom.java.JavaVisibility;
import org.apache.ibatis.abator.api.dom.java.Method;
import org.apache.ibatis.abator.api.dom.java.Parameter;
import org.apache.ibatis.abator.api.dom.java.TopLevelClass;
import org.apache.ibatis.abator.config.FullyQualifiedTable;
import org.apache.ibatis.abator.config.TableConfiguration;
import org.apache.ibatis.abator.exception.GenerationRuntimeException;
import org.apache.ibatis.abator.internal.db.ColumnDefinition;
import org.apache.ibatis.abator.internal.db.ColumnDefinitions;
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
 * <dt>enableSubPackages
 * <dt>
 * <dd>If true, the classes will be generated in sub-packaged based on the
 * database catalg and schema - else the will be generated in the specified
 * package (the targetPackage attribute). Default is false.</dd>
 * 
 * <dt>rootInterface
 * <dt>
 * <dd>If specified, then the root interface of the DAO interface class will be
 * set to the specified value. No checking is done to see if the specified
 * interface exists, or if the generated interface overrides any root interface
 * methods.</dd>
 * </dl>
 * 
 * @author Jeff Butler
 */
public class BaseDAOGenerator implements DAOGenerator {

    private AbstractDAOTemplate daoTemplate;

    protected Map properties;

    protected List warnings;

    protected String targetPackage;

    protected String targetProject;

    protected JavaModelGenerator javaModelGenerator;

    protected SqlMapGenerator sqlMapGenerator;

    private Map tableValueMaps;
    
    private boolean useJava5Features;

    /**
     *  
     */
    public BaseDAOGenerator(AbstractDAOTemplate daoTemplate, boolean useJava5Features) {
        super();
        this.daoTemplate = daoTemplate;
        this.useJava5Features = useJava5Features;
        tableValueMaps = new HashMap();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ibatis.abator.api.DAOGenerator#setProperties(java.util.Map)
     */
    public void setProperties(Map properties) {
        this.properties = properties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ibatis.abator.api.DAOGenerator#setWarnings(java.util.List)
     */
    public void setWarnings(List warnings) {
        this.warnings = warnings;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ibatis.abator.api.DAOGenerator#setTargetPackage(java.lang.String)
     */
    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ibatis.abator.api.DAOGenerator#setTargetProject(java.lang.String)
     */
    public void setTargetProject(String targetProject) {
        this.targetProject = targetProject;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ibatis.abator.api.DAOGenerator#setJavaModelGenerator(org.apache.ibatis.abator.api.JavaModelGenerator)
     */
    public void setJavaModelGenerator(JavaModelGenerator javaModelGenerator) {
        this.javaModelGenerator = javaModelGenerator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ibatis.abator.api.DAOGenerator#setSqlMapGenerator(org.apache.ibatis.abator.api.SqlMapGenerator)
     */
    public void setSqlMapGenerator(SqlMapGenerator sqlMapGenerator) {
        this.sqlMapGenerator = sqlMapGenerator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ibatis.abator.api.DAOGenerator#getGeneratedJavaFiles(org.apache.ibatis.abator.internal.db.ColumnDefinitions,
     *      org.apache.ibatis.abator.config.TableConfiguration,
     *      org.apache.ibatis.abator.api.ProgressCallback)
     */
    public List getGeneratedJavaFiles(ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration, ProgressCallback callback) {
        List list = new ArrayList();

        String tableName = tableConfiguration.getTable()
                .getFullyQualifiedTableName();

        callback.startSubTask(Messages.getString("DAOGeneratorBaseImpl.0", //$NON-NLS-1$
                tableName));
        CompilationUnit cu = getDAOImplementation(columnDefinitions,
                tableConfiguration);
        GeneratedJavaFile gjf = new GeneratedJavaFile(cu, targetProject);
        list.add(gjf);

        callback.startSubTask(Messages.getString("DAOGeneratorBaseImpl.1", //$NON-NLS-1$
                tableName));
        cu = getDAOInterface(columnDefinitions, tableConfiguration);
        gjf = new GeneratedJavaFile(cu, targetProject);
        list.add(gjf);

        return list;
    }

    protected TopLevelClass getDAOImplementation(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration) {

        FullyQualifiedTable table = tableConfiguration.getTable();
        FullyQualifiedJavaType type = getDAOImplementationType(table);
        TopLevelClass answer = new TopLevelClass(type);
        answer.setVisibility(JavaVisibility.PUBLIC);
        answer.setSuperClass(daoTemplate.getSuperClass());
        answer.addImportedType(daoTemplate.getSuperClass());
        answer.addSuperInterface(getDAOInterfaceType(table));
        answer.addImportedType(getDAOInterfaceType(table));

        Iterator iter = daoTemplate.getImplementationImports().iterator();
        while (iter.hasNext()) {
            answer.addImportedType((FullyQualifiedJavaType) iter.next());
        }

        // add constructor
        Method method = daoTemplate
                .getConstructorClone(getDAOImplementationType(table), table);
        answer.addMethod(method);

        // add any fields from the template
        iter = daoTemplate.getFieldClones(table);
        while (iter.hasNext()) {
            answer.addField((Field) iter.next());
        }

        // add any methods from the template
        iter = daoTemplate.getMethodClones(table);
        while (iter.hasNext()) {
            answer.addMethod((Method) iter.next());
        }

        List methods = getInsertMethods(columnDefinitions, tableConfiguration,
                false, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getUpdateByPrimaryKeyMethods(columnDefinitions,
                tableConfiguration, false, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getUpdateByPrimaryKeyWithBLOBsMethods(columnDefinitions,
                tableConfiguration, false, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getSelectByExampleMethods(columnDefinitions,
                tableConfiguration, false, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getSelectByExampleWithBLOBsMethods(columnDefinitions,
                tableConfiguration, false, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getSelectByPrimaryKeyMethods(columnDefinitions,
                tableConfiguration, false, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getDeleteByExampleMethods(columnDefinitions,
                tableConfiguration, false, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getDeleteByPrimaryKeyMethods(columnDefinitions,
                tableConfiguration, false, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        return answer;
    }

    protected Interface getDAOInterface(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration) {
        FullyQualifiedTable table = tableConfiguration.getTable();
        Interface answer = new Interface(getDAOInterfaceType(table));
        answer.setVisibility(JavaVisibility.PUBLIC);

        if (properties.containsKey("rootInterface")) { //$NON-NLS-1$
            FullyQualifiedJavaType fqjt =
                new FullyQualifiedJavaType((String) properties.get("rootInterface")); //$NON-NLS-1$
            answer.addSuperInterface(fqjt);
            answer.addImportedType(fqjt);
        }

        Iterator iter = daoTemplate.getInterfaceImports().iterator();
        while (iter.hasNext()) {
            answer.addImportedType((FullyQualifiedJavaType) iter.next());
        }

        List methods = getInsertMethods(columnDefinitions, tableConfiguration,
                true, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getUpdateByPrimaryKeyMethods(columnDefinitions,
                tableConfiguration, true, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getUpdateByPrimaryKeyWithBLOBsMethods(columnDefinitions,
                tableConfiguration, true, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getSelectByExampleMethods(columnDefinitions,
                tableConfiguration, true, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getSelectByExampleWithBLOBsMethods(columnDefinitions,
                tableConfiguration, true, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getSelectByPrimaryKeyMethods(columnDefinitions,
                tableConfiguration, true, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getDeleteByExampleMethods(columnDefinitions,
                tableConfiguration, true, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getDeleteByPrimaryKeyMethods(columnDefinitions,
                tableConfiguration, true, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
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
            CompilationUnit compilationUnit) {

        if (!AbatorRules.generateInsert(tableConfiguration)) {
            return null;
        }

        FullyQualifiedTable table = tableConfiguration.getTable();
        Method method = new Method();
        method.addComment(table);

        FullyQualifiedJavaType returnType;
        if (tableConfiguration.getGeneratedKey().isConfigured()) {
            ColumnDefinition cd = columnDefinitions
                    .getColumn(tableConfiguration.getGeneratedKey().getColumn());
            if (cd == null) {
                // the specified column doesn't exist, so don't do the generated
                // key
                // (the warning has already been reported)
                returnType = null;
            } else {
                returnType = cd.getResolvedJavaType()
                        .getFullyQualifiedJavaType();
                compilationUnit.addImportedType(returnType);
            }
        } else {
            returnType = null;
        }
        method.setReturnType(returnType);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("insert"); //$NON-NLS-1$

        FullyQualifiedJavaType parameterType;
        if (AbatorRules
                .generateRecordWithBLOBsExtendingPrimaryKey(columnDefinitions)
                || AbatorRules
                        .generateRecordWithBLOBsExtendingBaseRecord(columnDefinitions)) {
            parameterType = javaModelGenerator.getRecordWithBLOBsType(table);
        } else if (AbatorRules
                .generateBaseRecordWithNoSuperclass(columnDefinitions)
                || AbatorRules
                        .generateBaseRecordExtendingPrimaryKey(columnDefinitions)) {
            parameterType = javaModelGenerator.getRecordType(table);
        } else {
            parameterType = javaModelGenerator.getPrimaryKeyType(table);
        }
        compilationUnit.addImportedType(parameterType);
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        Iterator iter = daoTemplate.getCheckedExceptions().iterator();
        while (iter.hasNext()) {
            FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) iter.next();
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        if (!interfaceMethod) {
            // generate the implementation method
            StringBuffer sb = new StringBuffer();
            
            if (returnType != null) {
                sb.append("Object newKey = "); //$NON-NLS-1$
            }

            sb.append(daoTemplate.getInsertMethod(sqlMapGenerator
                    .getSqlMapNamespace(table), sqlMapGenerator
                    .getInsertStatementId(), "record")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());

            if (returnType != null) {
                if ("Object".equals(returnType.getShortName())) { //$NON-NLS-1$
                    method.addBodyLine("return newKey;"); //$NON-NLS-1$
                } else {
                    sb.setLength(0);
                    sb.append("return ("); //$NON-NLS-1$
                    sb.append(returnType.getShortName());
                    sb.append(") newKey;"); //$NON-NLS-1$
                    method.addBodyLine(sb.toString());
                }
            }
        }

        List answer = new ArrayList();
        answer.add(method);

        return answer;
    }

    protected List getUpdateByPrimaryKeyMethods(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (!AbatorRules.generateUpdateByPrimaryKeyWithoutBLOBs(
                columnDefinitions, tableConfiguration)) {
            return null;
        }

        FullyQualifiedTable table = tableConfiguration.getTable();
        FullyQualifiedJavaType type = javaModelGenerator.getRecordType(table);
        compilationUnit.addImportedType(type);

        Method method = new Method();
        method.addComment(table);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName("updateByPrimaryKey"); //$NON-NLS-1$
        method.addParameter(new Parameter(type, "record")); //$NON-NLS-1$

        Iterator iter = daoTemplate.getCheckedExceptions().iterator();
        while (iter.hasNext()) {
            FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) iter.next();
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        if (!interfaceMethod) {
            // generate the implementation method
            StringBuffer sb = new StringBuffer();

            sb.append("int rows = "); //$NON-NLS-1$
            sb.append(daoTemplate.getUpdateMethod(sqlMapGenerator
                    .getSqlMapNamespace(table), sqlMapGenerator
                    .getUpdateByPrimaryKeyStatementId(), "record")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());

            method.addBodyLine("return rows;"); //$NON-NLS-1$
        }

        ArrayList answer = new ArrayList();
        answer.add(method);

        return answer;
    }

    protected List getUpdateByPrimaryKeyWithBLOBsMethods(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (!AbatorRules.generateUpdateByPrimaryKeyWithBLOBs(columnDefinitions,
                tableConfiguration)) {
            return null;
        }

        FullyQualifiedTable table = tableConfiguration.getTable();
        FullyQualifiedJavaType type = javaModelGenerator
                .getRecordWithBLOBsType(table);
        compilationUnit.addImportedType(type);

        Method method = new Method();
        method.addComment(table);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName("updateByPrimaryKey"); //$NON-NLS-1$
        method.addParameter(new Parameter(type, "record")); //$NON-NLS-1$

        Iterator iter = daoTemplate.getCheckedExceptions().iterator();
        while (iter.hasNext()) {
            FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) iter.next();
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        if (!interfaceMethod) {
            // generate the implementation method
            StringBuffer sb = new StringBuffer();

            sb.append("int rows = "); //$NON-NLS-1$
            sb.append(daoTemplate.getUpdateMethod(sqlMapGenerator
                    .getSqlMapNamespace(table), sqlMapGenerator
                    .getUpdateByPrimaryKeyWithBLOBsStatementId(), "record")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());

            method.addBodyLine("return rows;"); //$NON-NLS-1$
        }

        ArrayList answer = new ArrayList();
        answer.add(method);

        return answer;
    }

    protected List getSelectByExampleMethods(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (!AbatorRules
                .generateSelectByExampleWithoutBLOBs(tableConfiguration)) {
            return null;
        }

        FullyQualifiedTable table = tableConfiguration.getTable();
        FullyQualifiedJavaType type = javaModelGenerator.getExampleType(table);
        compilationUnit.addImportedType(type);
        compilationUnit.addImportedType(FullyQualifiedJavaType.getNewListInstance());

        Method method = new Method();
        method.addComment(table);
        method.setVisibility(JavaVisibility.PUBLIC);
        
        FullyQualifiedJavaType returnType;
        if (useJava5Features) {
            FullyQualifiedJavaType fqjt;
            if (AbatorRules.generateBaseRecordExtendingPrimaryKey(columnDefinitions)
                    || AbatorRules.generateBaseRecordWithNoSuperclass(columnDefinitions)) {
                fqjt = javaModelGenerator.getRecordType(tableConfiguration.getTable());
            } else if (AbatorRules.generatePrimaryKey(columnDefinitions)) {
                fqjt = javaModelGenerator.getPrimaryKeyType(tableConfiguration.getTable());
            } else {
                throw new GenerationRuntimeException(Messages.getString("BaseDAOGenerator.0")); //$NON-NLS-1$
            }
            
            compilationUnit.addImportedType(fqjt);
            returnType = FullyQualifiedJavaType.getNewListInstance();
            returnType.addTypeArgument(fqjt);
        } else {
            returnType = FullyQualifiedJavaType.getNewListInstance();
        }
        method.setReturnType(returnType);
        
        method.setName("selectByExample"); //$NON-NLS-1$
        method.addParameter(new Parameter(type, "example")); //$NON-NLS-1$

        Iterator iter = daoTemplate.getCheckedExceptions().iterator();
        while (iter.hasNext()) {
            FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) iter.next();
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        if (!interfaceMethod) {
            // generate the implementation method
            StringBuffer sb = new StringBuffer();

            if (useJava5Features) {
                method.addAnnotation("@SuppressWarnings(\"unchecked\")"); //$NON-NLS-1$
                sb.append(returnType.getShortName());
                sb.append(" list = ("); //$NON-NLS-1$
                sb.append(returnType.getShortName());
                sb.append(") "); //$NON-NLS-1$
            } else {
                sb.append("List list = "); //$NON-NLS-1$
            }
            
            sb.append(daoTemplate.getQueryForListMethod(sqlMapGenerator.getSqlMapNamespace(table),
                    sqlMapGenerator.getSelectByExampleStatementId(), "example")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
            method.addBodyLine("return list;"); //$NON-NLS-1$
        }

        ArrayList answer = new ArrayList();
        answer.add(method);

        return answer;
    }

    protected List getSelectByExampleWithBLOBsMethods(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (!AbatorRules.generateSelectByExampleWithBLOBs(columnDefinitions,
                tableConfiguration)) {
            return null;
        }

        FullyQualifiedTable table = tableConfiguration.getTable();
        FullyQualifiedJavaType type = javaModelGenerator.getExampleType(table);
        compilationUnit.addImportedType(type);
        compilationUnit.addImportedType(FullyQualifiedJavaType.getNewListInstance());

        Method method = new Method();
        method.addComment(table);
        method.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType returnType;
        if (useJava5Features) {
            FullyQualifiedJavaType fqjt = javaModelGenerator.getRecordWithBLOBsType(tableConfiguration.getTable());
            compilationUnit.addImportedType(fqjt);
            returnType = FullyQualifiedJavaType.getNewListInstance();
            returnType.addTypeArgument(fqjt);
        } else {
            returnType = FullyQualifiedJavaType.getNewListInstance();
        }
        method.setReturnType(returnType);
        
        method.setName("selectByExampleWithBLOBs"); //$NON-NLS-1$
        method.addParameter(new Parameter(type, "example")); //$NON-NLS-1$
        
        Iterator iter = daoTemplate.getCheckedExceptions().iterator();
        while (iter.hasNext()) {
            FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) iter.next();
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        if (!interfaceMethod) {
            // generate the implementation method

            StringBuffer sb = new StringBuffer();

            if (useJava5Features) {
                method.addAnnotation("@SuppressWarnings(\"unchecked\")"); //$NON-NLS-1$
                sb.append(returnType.getShortName());
                sb.append(" list = ("); //$NON-NLS-1$
                sb.append(returnType.getShortName());
                sb.append(") "); //$NON-NLS-1$
            } else {
                sb.append("List list = "); //$NON-NLS-1$
            }
            
            sb.append(daoTemplate.getQueryForListMethod(sqlMapGenerator.getSqlMapNamespace(table),
                    sqlMapGenerator.getSelectByExampleWithBLOBsStatementId(),
                    "example")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
            method.addBodyLine("return list;"); //$NON-NLS-1$
        }

        ArrayList answer = new ArrayList();
        answer.add(method);

        return answer;
    }

    protected List getSelectByPrimaryKeyMethods(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (!AbatorRules.generateSelectByPrimaryKey(columnDefinitions,
                tableConfiguration)) {
            return null;
        }

        FullyQualifiedTable table = tableConfiguration.getTable();
        FullyQualifiedJavaType type = javaModelGenerator.getPrimaryKeyType(table);
        compilationUnit.addImportedType(type);
        
        Method method = new Method();
        method.addComment(table);
        method.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType returnType;
        if (AbatorRules
                .generateRecordWithBLOBsExtendingPrimaryKey(columnDefinitions)
                || AbatorRules
                        .generateRecordWithBLOBsExtendingBaseRecord(columnDefinitions)) {
            returnType = javaModelGenerator.getRecordWithBLOBsType(table);

        } else {
            returnType = javaModelGenerator.getRecordType(table);
        }
        method.setReturnType(returnType);
        compilationUnit.addImportedType(returnType);
        
        method.setName("selectByPrimaryKey"); //$NON-NLS-1$
        method.addParameter(new Parameter(type, "key")); //$NON-NLS-1$
        
        Iterator iter = daoTemplate.getCheckedExceptions().iterator();
        while (iter.hasNext()) {
            FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) iter.next();
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        if (!interfaceMethod) {
            // generate the implementation method
            StringBuffer sb = new StringBuffer();

            sb.append(returnType.getShortName());
            sb.append(" record = ("); //$NON-NLS-1$
            sb.append(returnType.getShortName());
            sb.append(") "); //$NON-NLS-1$
            sb.append(daoTemplate.getQueryForObjectMethod(sqlMapGenerator.getSqlMapNamespace(table),
                    sqlMapGenerator.getSelectByPrimaryKeyStatementId(),
                    "key")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
            method.addBodyLine("return record;"); //$NON-NLS-1$
        }

        ArrayList answer = new ArrayList();
        answer.add(method);

        return answer;
    }

    protected List getDeleteByExampleMethods(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (!AbatorRules.generateDeleteByExample(tableConfiguration)) {
            return null;
        }

        FullyQualifiedTable table = tableConfiguration.getTable();
        FullyQualifiedJavaType type = javaModelGenerator.getExampleType(table);
        compilationUnit.addImportedType(type);
        
        Method method = new Method();
        method.addComment(table);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName("deleteByExample"); //$NON-NLS-1$
        method.addParameter(new Parameter(type, "example")); //$NON-NLS-1$

        Iterator iter = daoTemplate.getCheckedExceptions().iterator();
        while (iter.hasNext()) {
            FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) iter.next();
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        if (!interfaceMethod) {
            // generate the implementation method
            StringBuffer sb = new StringBuffer();

            sb.append("int rows = "); //$NON-NLS-1$
            sb.append(daoTemplate.getDeleteMethod(sqlMapGenerator.getSqlMapNamespace(table),
                    sqlMapGenerator.getDeleteByExampleStatementId(),
                    "example")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
            method.addBodyLine("return rows;"); //$NON-NLS-1$
        }

        ArrayList answer = new ArrayList();
        answer.add(method);

        return answer;
    }

    protected List getDeleteByPrimaryKeyMethods(
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (!AbatorRules.generateDeleteByPrimaryKey(columnDefinitions,
                tableConfiguration)) {
            return null;
        }

        FullyQualifiedTable table = tableConfiguration.getTable();
        FullyQualifiedJavaType type = javaModelGenerator.getPrimaryKeyType(table);
        compilationUnit.addImportedType(type);

        Method method = new Method();
        method.addComment(table);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName("deleteByPrimaryKey"); //$NON-NLS-1$
        method.addParameter(new Parameter(type, "key")); //$NON-NLS-1$
        
        Iterator iter = daoTemplate.getCheckedExceptions().iterator();
        while (iter.hasNext()) {
            FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) iter.next();
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        if (!interfaceMethod) {
            // generate the implementation method
            StringBuffer sb = new StringBuffer();

            sb.append("int rows = "); //$NON-NLS-1$
            sb.append(daoTemplate.getDeleteMethod(sqlMapGenerator.getSqlMapNamespace(table),
                    sqlMapGenerator.getDeleteByPrimaryKeyStatementId(),
                    "key")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
            method.addBodyLine("return rows;"); //$NON-NLS-1$
        }

        ArrayList answer = new ArrayList();
        answer.add(method);

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
