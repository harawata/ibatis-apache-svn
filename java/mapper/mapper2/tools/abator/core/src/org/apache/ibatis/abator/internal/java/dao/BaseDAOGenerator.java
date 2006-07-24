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
import org.apache.ibatis.abator.api.DAOMethodNameCalculator;
import org.apache.ibatis.abator.api.FullyQualifiedTable;
import org.apache.ibatis.abator.api.GeneratedJavaFile;
import org.apache.ibatis.abator.api.IntrospectedTable;
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
import org.apache.ibatis.abator.api.dom.java.PrimitiveTypeWrapper;
import org.apache.ibatis.abator.api.dom.java.TopLevelClass;
import org.apache.ibatis.abator.internal.AbatorObjectFactory;
import org.apache.ibatis.abator.internal.DefaultDAOMethodNameCalculator;
import org.apache.ibatis.abator.internal.ExtendedDAOMethodNameCalculator;
import org.apache.ibatis.abator.internal.db.ColumnDefinition;
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
 * <dt>enableSubPackages</dt>
 * <dd>If true, the classes will be generated in sub-packaged based on the
 * database catalg and schema - else the will be generated in the specified
 * package (the targetPackage attribute). Default is false.</dd>
 * 
 * <dt>rootInterface</dt>
 * <dd>If specified, then the root interface of the DAO interface class will be
 * set to the specified value. No checking is done to see if the specified
 * interface exists, or if the generated interface overrides any root interface
 * methods.</dd>
 *
 * <dt>exampleMethodVisibility</dt>
 * <dd>This property can be used the change the vilsibility of the various
 * example methods (selectByExample, deleteByExample, etc.).  If "public" (the default)
 * then the implementation methods are public and the methods are declared in the
 * interface declaration.  If any of the other valid values (private, protected,
 * default), then the methods have the specified visibility in the implmentation
 * class and the methods are not declared in the interface class.</dd>
 * 
 * <dt>methodNameCalculator</dt>
 * <dd>This property can be used to specify different method name
 * calculators.  A method name calculator is used to create the DAO method
 * names.  Abator offers two choices - default, and extended.  If you wish to
 * supply a different version, you can specify the fully qualified name of a
 * class that implements the
 * <code>org.apache.ibatis.abator.api.DAOMethodNameCalculator</code>
 * interface.</dd>
 * </dl>
 * 
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

    private JavaVisibility exampleMethodVisibility = JavaVisibility.PUBLIC;
    
    private DAOMethodNameCalculator methodNameCalculator = new DefaultDAOMethodNameCalculator();
    
    /**
     * 
     */
    public BaseDAOGenerator(AbstractDAOTemplate daoTemplate,
            boolean useJava5Features) {
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
        if (properties.containsKey("exampleMethodVisibility")) { //$NON-NLS-1$
            String value = (String) properties.get("exampleMethodVisibility"); //$NON-NLS-1$
            
            if ("public".equalsIgnoreCase(value)) { //$NON-NLS-1$
                exampleMethodVisibility = JavaVisibility.PUBLIC;
            } else if ("private".equalsIgnoreCase(value)) { //$NON-NLS-1$
                exampleMethodVisibility = JavaVisibility.PRIVATE;
            } else if ("protected".equalsIgnoreCase(value)) { //$NON-NLS-1$
                exampleMethodVisibility = JavaVisibility.PROTECTED;
            } else if ("default".equalsIgnoreCase(value)) { //$NON-NLS-1$
                exampleMethodVisibility = JavaVisibility.DEFAULT;
            } else {
                warnings.add(Messages.getString("Warning.16", value)); //$NON-NLS-1$
            }
        }
        
        if (properties.containsKey("methodNameCalculator")) { //$NON-NLS-1$
            String value = (String) properties.get("methodNameCalculator"); //$NON-NLS-1$
            
            if ("extended".equalsIgnoreCase(value)) { //$NON-NLS-1$
                methodNameCalculator = new ExtendedDAOMethodNameCalculator();
            } else if (!"default".equalsIgnoreCase(value) //$NON-NLS-1$
                    && StringUtility.stringHasValue(value)) {
                try {
                    methodNameCalculator = (DAOMethodNameCalculator)
                        AbatorObjectFactory.createObject(value);
                } catch (Exception e) {
                    warnings.add(Messages.getString("Warning.17", value, e.getMessage())); //$NON-NLS-1$
                }
            }
        }
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
     * @see org.apache.ibatis.abator.api.DAOGenerator#getGeneratedJavaFiles(org.apache.ibatis.abator.internal.db.IntrospectedTable,
     *      org.apache.ibatis.abator.api.ProgressCallback)
     */
    public List getGeneratedJavaFiles(IntrospectedTable introspectedTable,
            ProgressCallback callback) {
        List list = new ArrayList();

        String tableName = introspectedTable.getTable()
                .getFullyQualifiedTableName();

        callback.startSubTask(Messages.getString("Progress.10", //$NON-NLS-1$
                tableName));
        CompilationUnit cu = getDAOImplementation(introspectedTable);
        GeneratedJavaFile gjf = new GeneratedJavaFile(cu, targetProject);
        list.add(gjf);

        callback.startSubTask(Messages.getString("Progress.11", //$NON-NLS-1$
                tableName));
        cu = getDAOInterface(introspectedTable);
        gjf = new GeneratedJavaFile(cu, targetProject);
        list.add(gjf);

        return list;
    }

    protected TopLevelClass getDAOImplementation(
            IntrospectedTable introspectedTable) {

        FullyQualifiedTable table = introspectedTable.getTable();
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
        Method method = daoTemplate.getConstructorClone(
                getDAOImplementationType(table), table);
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

        List methods = getInsertMethods(introspectedTable, false, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getUpdateByPrimaryKeyMethods(introspectedTable, false, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getUpdateByPrimaryKeyWithBLOBsMethods(introspectedTable,
                false, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getSelectByExampleMethods(introspectedTable, false, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getSelectByExampleWithBLOBsMethods(introspectedTable, false,
                answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getSelectByPrimaryKeyMethods(introspectedTable, false, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getDeleteByExampleMethods(introspectedTable, false, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getDeleteByPrimaryKeyMethods(introspectedTable, false, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        return answer;
    }

    protected Interface getDAOInterface(IntrospectedTable introspectedTable) {
        FullyQualifiedTable table = introspectedTable.getTable();
        Interface answer = new Interface(getDAOInterfaceType(table));
        answer.setVisibility(JavaVisibility.PUBLIC);

        if (properties.containsKey("rootInterface")) { //$NON-NLS-1$
            FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(
                    (String) properties.get("rootInterface")); //$NON-NLS-1$
            answer.addSuperInterface(fqjt);
            answer.addImportedType(fqjt);
        }

        Iterator iter = daoTemplate.getInterfaceImports().iterator();
        while (iter.hasNext()) {
            answer.addImportedType((FullyQualifiedJavaType) iter.next());
        }

        List methods = getInsertMethods(introspectedTable, true, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getUpdateByPrimaryKeyMethods(introspectedTable, true, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getUpdateByPrimaryKeyWithBLOBsMethods(introspectedTable,
                true, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getSelectByExampleMethods(introspectedTable, true, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getSelectByExampleWithBLOBsMethods(introspectedTable, true,
                answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getSelectByPrimaryKeyMethods(introspectedTable, true, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getDeleteByExampleMethods(introspectedTable, true, answer);
        if (methods != null) {
            iter = methods.iterator();
            while (iter.hasNext()) {
                answer.addMethod((Method) iter.next());
            }
        }

        methods = getDeleteByPrimaryKeyMethods(introspectedTable, true, answer);
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

    protected List getInsertMethods(IntrospectedTable introspectedTable,
            boolean interfaceMethod, CompilationUnit compilationUnit) {

        if (!AbatorRules.generateInsert(introspectedTable)) {
            return null;
        }

        FullyQualifiedTable table = introspectedTable.getTable();
        Method method = new Method();
        method.addComment(table);

        FullyQualifiedJavaType returnType;
        if (introspectedTable.getGeneratedKey() != null) {
            ColumnDefinition cd = introspectedTable.getColumn(
                            introspectedTable.getGeneratedKey().getColumn());
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
        method.setName(methodNameCalculator.getInsertMethodName(introspectedTable));

        FullyQualifiedJavaType parameterType;
        if (AbatorRules
                .generateRecordWithBLOBsExtendingPrimaryKey(introspectedTable)
                || AbatorRules
                        .generateRecordWithBLOBsExtendingBaseRecord(introspectedTable)) {
            parameterType = javaModelGenerator.getRecordWithBLOBsType(table);
        } else if (AbatorRules
                .generateBaseRecordWithNoSuperclass(introspectedTable)
                || AbatorRules
                        .generateBaseRecordExtendingPrimaryKey(introspectedTable)) {
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
                    // no need to cast if the return type is Object
                    method.addBodyLine("return newKey;"); //$NON-NLS-1$
                } else {
                    sb.setLength(0);

                    if (returnType.isPrimitive()) {
                        PrimitiveTypeWrapper ptw = returnType
                                .getPrimitiveTypeWrapper();
                        sb.append("return (("); //$NON-NLS-1$
                        sb.append(ptw.getShortName());
                        sb.append(") newKey"); //$NON-NLS-1$
                        sb.append(")."); //$NON-NLS-1$
                        sb.append(ptw.getToPrimitiveMethod());
                        sb.append(';');
                    } else {
                        sb.append("return ("); //$NON-NLS-1$
                        sb.append(returnType.getShortName());
                        sb.append(") newKey;"); //$NON-NLS-1$
                    }

                    method.addBodyLine(sb.toString());
                }
            }
        }

        List answer = new ArrayList();
        answer.add(method);

        return answer;
    }

    protected List getUpdateByPrimaryKeyMethods(
            IntrospectedTable introspectedTable, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (!AbatorRules
                .generateUpdateByPrimaryKeyWithoutBLOBs(introspectedTable)) {
            return null;
        }

        FullyQualifiedTable table = introspectedTable.getTable();
        FullyQualifiedJavaType type = javaModelGenerator.getRecordType(table);
        compilationUnit.addImportedType(type);

        Method method = new Method();
        method.addComment(table);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(methodNameCalculator.getUpdateByPrimaryKeyMethodName(introspectedTable));
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
            IntrospectedTable introspectedTable, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (!AbatorRules.generateUpdateByPrimaryKeyWithBLOBs(introspectedTable)) {
            return null;
        }

        FullyQualifiedTable table = introspectedTable.getTable();
        FullyQualifiedJavaType type = javaModelGenerator
                .getRecordWithBLOBsType(table);
        compilationUnit.addImportedType(type);

        Method method = new Method();
        method.addComment(table);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(methodNameCalculator.getUpdateByPrimaryKeyMethodName(introspectedTable));
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
            IntrospectedTable introspectedTable, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (!AbatorRules.generateSelectByExampleWithoutBLOBs(introspectedTable)) {
            return null;
        }

        if (interfaceMethod && exampleMethodVisibility != JavaVisibility.PUBLIC) {
            return null;
        }
        
        FullyQualifiedTable table = introspectedTable.getTable();
        FullyQualifiedJavaType type = javaModelGenerator.getExampleType(table);
        compilationUnit.addImportedType(type);
        compilationUnit.addImportedType(FullyQualifiedJavaType
                .getNewListInstance());

        Method method = new Method();
        method.addComment(table);
        method.setVisibility(exampleMethodVisibility);

        FullyQualifiedJavaType returnType;
        if (useJava5Features) {
            FullyQualifiedJavaType fqjt;
            if (AbatorRules
                    .generateBaseRecordExtendingPrimaryKey(introspectedTable)
                    || AbatorRules
                            .generateBaseRecordWithNoSuperclass(introspectedTable)) {
                fqjt = javaModelGenerator.getRecordType(table);
            } else if (AbatorRules.generatePrimaryKey(introspectedTable)) {
                fqjt = javaModelGenerator.getPrimaryKeyType(table);
            } else {
                throw new RuntimeException(Messages
                        .getString("RuntimeError.12")); //$NON-NLS-1$
            }

            compilationUnit.addImportedType(fqjt);
            returnType = FullyQualifiedJavaType.getNewListInstance();
            returnType.addTypeArgument(fqjt);
        } else {
            returnType = FullyQualifiedJavaType.getNewListInstance();
        }
        method.setReturnType(returnType);

        method.setName(methodNameCalculator.getSelectByExampleMethodName(introspectedTable));
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

            sb.append(daoTemplate.getQueryForListMethod(sqlMapGenerator
                    .getSqlMapNamespace(table), sqlMapGenerator
                    .getSelectByExampleStatementId(), "example")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
            method.addBodyLine("return list;"); //$NON-NLS-1$
        }

        ArrayList answer = new ArrayList();
        answer.add(method);

        return answer;
    }

    protected List getSelectByExampleWithBLOBsMethods(
            IntrospectedTable introspectedTable, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (!AbatorRules.generateSelectByExampleWithBLOBs(introspectedTable)) {
            return null;
        }

        if (interfaceMethod && exampleMethodVisibility != JavaVisibility.PUBLIC) {
            return null;
        }
        
        FullyQualifiedTable table = introspectedTable.getTable();
        FullyQualifiedJavaType type = javaModelGenerator.getExampleType(table);
        compilationUnit.addImportedType(type);
        compilationUnit.addImportedType(FullyQualifiedJavaType
                .getNewListInstance());

        Method method = new Method();
        method.addComment(table);
        method.setVisibility(exampleMethodVisibility);

        FullyQualifiedJavaType returnType;
        if (useJava5Features) {
            FullyQualifiedJavaType fqjt = javaModelGenerator
                    .getRecordWithBLOBsType(table);
            compilationUnit.addImportedType(fqjt);
            returnType = FullyQualifiedJavaType.getNewListInstance();
            returnType.addTypeArgument(fqjt);
        } else {
            returnType = FullyQualifiedJavaType.getNewListInstance();
        }
        method.setReturnType(returnType);

        method.setName(methodNameCalculator.getSelectByExampleWithBLOBsMethodName(introspectedTable)); //$NON-NLS-1$
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

            sb.append(daoTemplate.getQueryForListMethod(sqlMapGenerator
                    .getSqlMapNamespace(table), sqlMapGenerator
                    .getSelectByExampleWithBLOBsStatementId(), "example")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
            method.addBodyLine("return list;"); //$NON-NLS-1$
        }

        ArrayList answer = new ArrayList();
        answer.add(method);

        return answer;
    }

    protected List getSelectByPrimaryKeyMethods(
            IntrospectedTable introspectedTable, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (!AbatorRules.generateSelectByPrimaryKey(introspectedTable)) {
            return null;
        }

        FullyQualifiedTable table = introspectedTable.getTable();
        FullyQualifiedJavaType type = javaModelGenerator
                .getPrimaryKeyType(table);
        compilationUnit.addImportedType(type);

        Method method = new Method();
        method.addComment(table);
        method.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType returnType;
        if (AbatorRules
                .generateRecordWithBLOBsExtendingPrimaryKey(introspectedTable)
                || AbatorRules
                        .generateRecordWithBLOBsExtendingBaseRecord(introspectedTable)) {
            returnType = javaModelGenerator.getRecordWithBLOBsType(table);

        } else {
            returnType = javaModelGenerator.getRecordType(table);
        }
        method.setReturnType(returnType);
        compilationUnit.addImportedType(returnType);

        method.setName(methodNameCalculator.getSelectByPrimaryKeyMethodName(introspectedTable));
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
            sb.append(daoTemplate.getQueryForObjectMethod(sqlMapGenerator
                    .getSqlMapNamespace(table), sqlMapGenerator
                    .getSelectByPrimaryKeyStatementId(), "key")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
            method.addBodyLine("return record;"); //$NON-NLS-1$
        }

        ArrayList answer = new ArrayList();
        answer.add(method);

        return answer;
    }

    protected List getDeleteByExampleMethods(
            IntrospectedTable introspectedTable, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (!AbatorRules.generateDeleteByExample(introspectedTable)) {
            return null;
        }

        if (interfaceMethod && exampleMethodVisibility != JavaVisibility.PUBLIC) {
            return null;
        }
        
        FullyQualifiedTable table = introspectedTable.getTable();
        FullyQualifiedJavaType type = javaModelGenerator.getExampleType(table);
        compilationUnit.addImportedType(type);

        Method method = new Method();
        method.addComment(table);
        method.setVisibility(exampleMethodVisibility);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(methodNameCalculator.getDeleteByExampleMethodName(introspectedTable));
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
            sb.append(daoTemplate.getDeleteMethod(sqlMapGenerator
                    .getSqlMapNamespace(table), sqlMapGenerator
                    .getDeleteByExampleStatementId(), "example")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
            method.addBodyLine("return rows;"); //$NON-NLS-1$
        }

        ArrayList answer = new ArrayList();
        answer.add(method);

        return answer;
    }

    protected List getDeleteByPrimaryKeyMethods(
            IntrospectedTable introspectedTable, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (!AbatorRules.generateDeleteByPrimaryKey(introspectedTable)) {
            return null;
        }

        FullyQualifiedTable table = introspectedTable.getTable();
        FullyQualifiedJavaType type = javaModelGenerator
                .getPrimaryKeyType(table);
        compilationUnit.addImportedType(type);

        Method method = new Method();
        method.addComment(table);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(methodNameCalculator.getDeleteByPrimaryKeyMethodName(introspectedTable));
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
            sb.append(daoTemplate.getDeleteMethod(sqlMapGenerator
                    .getSqlMapNamespace(table), sqlMapGenerator
                    .getDeleteByPrimaryKeyStatementId(), "key")); //$NON-NLS-1$
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
