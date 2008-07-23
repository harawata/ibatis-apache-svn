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
package org.apache.ibatis.ibator.internal.java.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.ibator.api.CommentGenerator;
import org.apache.ibatis.ibator.api.DAOGenerator;
import org.apache.ibatis.ibator.api.DAOMethodNameCalculator;
import org.apache.ibatis.ibator.api.FullyQualifiedTable;
import org.apache.ibatis.ibator.api.GeneratedJavaFile;
import org.apache.ibatis.ibator.api.IbatorPlugin;
import org.apache.ibatis.ibator.api.IntrospectedTable;
import org.apache.ibatis.ibator.api.ProgressCallback;
import org.apache.ibatis.ibator.api.dom.java.CompilationUnit;
import org.apache.ibatis.ibator.api.dom.java.Field;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.java.InnerClass;
import org.apache.ibatis.ibator.api.dom.java.Interface;
import org.apache.ibatis.ibator.api.dom.java.JavaVisibility;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.Parameter;
import org.apache.ibatis.ibator.api.dom.java.PrimitiveTypeWrapper;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;
import org.apache.ibatis.ibator.config.IbatorContext;
import org.apache.ibatis.ibator.config.PropertyRegistry;
import org.apache.ibatis.ibator.internal.DefaultDAOMethodNameCalculator;
import org.apache.ibatis.ibator.internal.ExtendedDAOMethodNameCalculator;
import org.apache.ibatis.ibator.internal.IbatorObjectFactory;
import org.apache.ibatis.ibator.internal.db.ColumnDefinition;
import org.apache.ibatis.ibator.internal.rules.IbatorRules;
import org.apache.ibatis.ibator.internal.sqlmap.XmlConstants;
import org.apache.ibatis.ibator.internal.util.JavaBeansUtil;
import org.apache.ibatis.ibator.internal.util.StringUtility;
import org.apache.ibatis.ibator.internal.util.messages.Messages;

/**
 * This class generates DAO classes based on the values in the supplied
 * DAOTemplate.
 * 
 * This class supports the following properties:
 * 
 * <dl>
 * <dt>enableSubPackages</dt>
 * <dd>If true, the classes will be generated in sub-packaged based on the
 * database catalog and schema - else the will be generated in the specified
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
 * names.  ibator offers two choices - default, and extended.  If you wish to
 * supply a different version, you can specify the fully qualified name of a
 * class that implements the
 * <code>org.apache.ibatis.ibator.api.DAOMethodNameCalculator</code>
 * interface.</dd>
 * </dl>
 * 
 * 
 * @author Jeff Butler
 */
public class BaseDAOGenerator implements DAOGenerator {

    protected IbatorContext ibatorContext;
    protected AbstractDAOTemplate daoTemplate;

    protected Properties properties;

    protected List<String> warnings;

    private boolean useJava5Features;
    
    protected JavaVisibility exampleMethodVisibility = JavaVisibility.PUBLIC;
    
    protected DAOMethodNameCalculator methodNameCalculator = new DefaultDAOMethodNameCalculator();
    
    /**
     * 
     */
    public BaseDAOGenerator(AbstractDAOTemplate daoTemplate,
            boolean useJava5Features) {
        super();
        this.daoTemplate = daoTemplate;
        this.useJava5Features = useJava5Features;
        properties = new Properties();
    }

    public void addConfigurationProperties(Properties properties) {
        this.properties.putAll(properties);
        
        String value = properties.getProperty(PropertyRegistry.DAO_EXAMPLE_METHOD_VISIBILITY);
        if (StringUtility.stringHasValue(value)) {
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
        
        value = properties.getProperty(PropertyRegistry.DAO_METHOD_NAME_CALCULATOR);
        if (StringUtility.stringHasValue(value)) {
            if ("extended".equalsIgnoreCase(value)) { //$NON-NLS-1$
                methodNameCalculator = new ExtendedDAOMethodNameCalculator();
            } else if (!"default".equalsIgnoreCase(value) //$NON-NLS-1$
                    && StringUtility.stringHasValue(value)) {
                try {
                    methodNameCalculator = (DAOMethodNameCalculator)
                        IbatorObjectFactory.createObject(value);
                } catch (Exception e) {
                    warnings.add(Messages.getString("Warning.17", value, e.getMessage())); //$NON-NLS-1$
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ibatis.ibator.api.DAOGenerator#setWarnings(java.util.List)
     */
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.ibatis.ibator.api.DAOGenerator#getGeneratedJavaFiles(org.apache.ibatis.ibator.internal.db.IntrospectedTable,
     *      org.apache.ibatis.ibator.api.ProgressCallback)
     */
    public List<GeneratedJavaFile> getGeneratedJavaFiles(IntrospectedTable introspectedTable,
            ProgressCallback callback) {
        String targetProject = ibatorContext.getDaoGeneratorConfiguration().getTargetProject();
        
        List<GeneratedJavaFile> list = new ArrayList<GeneratedJavaFile>();
        IbatorPlugin plugins = ibatorContext.getPlugins();

        callback.startSubTask(Messages.getString("Progress.10", //$NON-NLS-1$
                introspectedTable.getFullyQualifiedTable().toString()));
        TopLevelClass tlc = getDAOImplementation(introspectedTable);
        if (plugins.daoImplementationGenerated(tlc, introspectedTable)) {
            GeneratedJavaFile gjf = new GeneratedJavaFile(tlc, targetProject);
            list.add(gjf);
        }

        callback.startSubTask(Messages.getString("Progress.11", //$NON-NLS-1$
                introspectedTable.getFullyQualifiedTable().toString()));
        Interface interfaze = getDAOInterface(introspectedTable);
        if (plugins.daoInterfaceGenerated(interfaze, introspectedTable)) {
            GeneratedJavaFile gjf = new GeneratedJavaFile(interfaze, targetProject);
            list.add(gjf);
        }

        return list;
    }

    protected TopLevelClass getDAOImplementation(
            IntrospectedTable introspectedTable) {

        FullyQualifiedJavaType interfaceType = introspectedTable.getDAOInterfaceType();
        FullyQualifiedJavaType implementationType = introspectedTable.getDAOImplementationType();
        
        CommentGenerator commentGenerator = ibatorContext.getCommentGenerator();
        
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        TopLevelClass answer = new TopLevelClass(implementationType);
        answer.setVisibility(JavaVisibility.PUBLIC);
        answer.setSuperClass(daoTemplate.getSuperClass());
        answer.addImportedType(daoTemplate.getSuperClass());
        answer.addSuperInterface(interfaceType);
        answer.addImportedType(interfaceType);

        for (FullyQualifiedJavaType fqjt : daoTemplate.getImplementationImports()) {
            answer.addImportedType(fqjt);
        }
        
        commentGenerator.addJavaFileComment(answer);

        // add constructor
        answer.addMethod(daoTemplate.getConstructorClone(commentGenerator,
                implementationType, table));

        // add any fields from the template
        for (Field field : daoTemplate.getFieldClones(commentGenerator, table)) {
            answer.addField(field);
        }

        // add any methods from the template
        for (Method method : daoTemplate.getMethodClones(commentGenerator, table)) {
            answer.addMethod(method);
        }

        IbatorRules rules = introspectedTable.getRules();
        Method method;
        IbatorPlugin plugins = ibatorContext.getPlugins();
        
        if (rules.generateInsert()) {
            method = getInsertMethod(introspectedTable, false, answer);
            if (method != null) {
                if (plugins.daoInsertMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateInsertSelective()) {
            method = getInsertSelectiveMethod(introspectedTable, false, answer);
            if (method != null) {
                if (plugins.daoInsertSelectiveMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateUpdateByPrimaryKeyWithoutBLOBs()) {
            method = getUpdateByPrimaryKeyWithoutBLOBsMethod(introspectedTable, false, answer);
            if (method != null) {
                if (plugins.daoUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateUpdateByPrimaryKeyWithBLOBs()) {
            method = getUpdateByPrimaryKeyWithBLOBsMethod(introspectedTable,
                    false, answer);
            if (method != null) {
                if (plugins.daoUpdateByPrimaryKeyWithBLOBsMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateUpdateByPrimaryKeySelective()) {
            method = getUpdateByPrimaryKeySelectiveMethod(introspectedTable,
                    false, answer);
            if (method != null) {
                if (plugins.daoUpdateByPrimaryKeySelectiveMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateSelectByExampleWithoutBLOBs()) {
            method = getSelectByExampleWithoutBLOBsMethod(introspectedTable, false, answer);
            if (method != null) {
                if (plugins.daoSelectByExampleWithoutBLOBsMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateSelectByExampleWithBLOBs()) {
            method = getSelectByExampleWithBLOBsMethod(introspectedTable, false,
                    answer);
            if (method != null) {
                if (plugins.daoSelectByExampleWithBLOBsMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateSelectByPrimaryKey()) {
            method = getSelectByPrimaryKeyMethod(introspectedTable, false, answer);
            if (method != null) {
                if (plugins.daoSelectByPrimaryKeyMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateDeleteByExample()) {
            method = getDeleteByExampleMethod(introspectedTable, false, answer);
            if (method != null) {
                if (plugins.daoDeleteByExampleMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateDeleteByPrimaryKey()) {
            method = getDeleteByPrimaryKeyMethod(introspectedTable, false, answer);
            if (method != null) {
                if (plugins.daoDeleteByPrimaryKeyMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateCountByExample()) {
            method = getCountByExampleMethod(introspectedTable, false, answer);
            if (method != null) {
                if (plugins.daoCountByExampleMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }
        
        if (rules.generateUpdateByExampleSelective()) {
            method = getUpdateByExampleSelectiveMethod(introspectedTable, false, answer);
            if (method != null) {
                if (plugins.daoUpdateByExampleSelectiveMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }
        
        if (rules.generateUpdateByExampleWithBLOBs()) {
            method = getUpdateByExampleWithBLOBsMethod(introspectedTable, false, answer);
            if (method != null) {
                if (plugins.daoUpdateByExampleWithBLOBsMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateUpdateByExampleWithoutBLOBs()) {
            method = getUpdateByExampleWithoutBLOBsMethod(introspectedTable, false, answer);
            if (method != null) {
                if (plugins.daoUpdateByExampleWithoutBLOBsMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }
        
        if (rules.generateUpdateByExampleSelective()
                || rules.generateUpdateByExampleWithBLOBs()
                || rules.generateUpdateByExampleWithoutBLOBs()) {
            InnerClass innerClass = getUpdateByExampleParms(introspectedTable, answer);
            if (innerClass != null) {
                answer.addInnerClass(innerClass);
            }
        }

        return answer;
    }

    protected Interface getDAOInterface(IntrospectedTable introspectedTable) {
        Interface answer = new Interface(introspectedTable.getDAOInterfaceType());
        answer.setVisibility(JavaVisibility.PUBLIC);

        String rootInterface = introspectedTable.getTableConfigurationProperty(PropertyRegistry.ANY_ROOT_INTERFACE);
        if (rootInterface == null) {
            rootInterface = properties.getProperty(PropertyRegistry.ANY_ROOT_INTERFACE);
        }
        
        if (StringUtility.stringHasValue(rootInterface)) {
            FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(rootInterface);
            answer.addSuperInterface(fqjt);
            answer.addImportedType(fqjt);
        }

        for (FullyQualifiedJavaType fqjt : daoTemplate.getInterfaceImports()) {
            answer.addImportedType(fqjt);
        }

        ibatorContext.getCommentGenerator().addJavaFileComment(answer);
        
        IbatorRules rules = introspectedTable.getRules();
        Method method;
        IbatorPlugin plugins = ibatorContext.getPlugins();
        
        if (rules.generateInsert()) {
            method = getInsertMethod(introspectedTable, true, answer);
            if (method != null) {
                if (plugins.daoInsertMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateInsertSelective()) {
            method = getInsertSelectiveMethod(introspectedTable, true, answer);
            if (method != null) {
                if (plugins.daoInsertSelectiveMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateUpdateByPrimaryKeyWithoutBLOBs()) {
            method = getUpdateByPrimaryKeyWithoutBLOBsMethod(introspectedTable, true, answer);
            if (method != null) {
                if (plugins.daoUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateUpdateByPrimaryKeyWithBLOBs()) {
            method = getUpdateByPrimaryKeyWithBLOBsMethod(introspectedTable,
                    true, answer);
            if (method != null) {
                if (plugins.daoUpdateByPrimaryKeyWithBLOBsMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateUpdateByPrimaryKeySelective()) {
            method = getUpdateByPrimaryKeySelectiveMethod(introspectedTable,
                    true, answer);
            if (method != null) {
                if (plugins.daoUpdateByPrimaryKeySelectiveMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateSelectByExampleWithoutBLOBs()) {
            method = getSelectByExampleWithoutBLOBsMethod(introspectedTable, true, answer);
            if (method != null) {
                if (plugins.daoSelectByExampleWithoutBLOBsMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateSelectByExampleWithBLOBs()) {
            method = getSelectByExampleWithBLOBsMethod(introspectedTable, true,
                    answer);
            if (method != null) {
                if (plugins.daoSelectByExampleWithBLOBsMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateSelectByPrimaryKey()) {
            method = getSelectByPrimaryKeyMethod(introspectedTable, true, answer);
            if (method != null) {
                if (plugins.daoSelectByPrimaryKeyMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateDeleteByExample()) {
            method = getDeleteByExampleMethod(introspectedTable, true, answer);
            if (method != null) {
                if (plugins.daoDeleteByExampleMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateDeleteByPrimaryKey()) {
            method = getDeleteByPrimaryKeyMethod(introspectedTable, true, answer);
            if (method != null) {
                if (plugins.daoDeleteByPrimaryKeyMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateCountByExample()) {
            method = getCountByExampleMethod(introspectedTable, true, answer);
            if (method != null) {
                if (plugins.daoCountByExampleMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }
        
        if (rules.generateUpdateByExampleSelective()) {
            method = getUpdateByExampleSelectiveMethod(introspectedTable, true, answer);
            if (method != null) {
                if (plugins.daoUpdateByExampleSelectiveMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }
        
        if (rules.generateUpdateByExampleWithBLOBs()) {
            method = getUpdateByExampleWithBLOBsMethod(introspectedTable, true, answer);
            if (method != null) {
                if (plugins.daoUpdateByExampleWithBLOBsMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }

        if (rules.generateUpdateByExampleWithoutBLOBs()) {
            method = getUpdateByExampleWithoutBLOBsMethod(introspectedTable, true, answer);
            if (method != null) {
                if (plugins.daoUpdateByExampleWithoutBLOBsMethodGenerated(method, answer, introspectedTable)) {
                    answer.addMethod(method);
                }
            }
        }
        
        return answer;
    }

    protected Method getInsertMethod(IntrospectedTable introspectedTable,
            boolean interfaceMethod, CompilationUnit compilationUnit) {

        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        Method method = new Method();

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

        FullyQualifiedJavaType parameterType =
            introspectedTable.getRules().calculateAllFieldsClass();
        
        compilationUnit.addImportedType(parameterType);
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        for (FullyQualifiedJavaType fqjt : daoTemplate.getCheckedExceptions()) {
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        ibatorContext.getCommentGenerator().addGeneralMethodComment(method, table);
        
        if (!interfaceMethod) {
            // generate the implementation method
            StringBuilder sb = new StringBuilder();

            if (returnType != null) {
                sb.append("Object newKey = "); //$NON-NLS-1$
            }

            sb.append(daoTemplate.getInsertMethod(table.getSqlMapNamespace(), 
                    XmlConstants.INSERT_STATEMENT_ID,
                    "record")); //$NON-NLS-1$
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

        return method;
    }

    protected Method getInsertSelectiveMethod(IntrospectedTable introspectedTable,
            boolean interfaceMethod, CompilationUnit compilationUnit) {

        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        Method method = new Method();

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
        method.setName(methodNameCalculator.getInsertSelectiveMethodName(introspectedTable));

        FullyQualifiedJavaType parameterType =
            introspectedTable.getRules().calculateAllFieldsClass();
        
        compilationUnit.addImportedType(parameterType);
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        for (FullyQualifiedJavaType fqjt : daoTemplate.getCheckedExceptions()) {
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        ibatorContext.getCommentGenerator().addGeneralMethodComment(method, table);
        
        if (!interfaceMethod) {
            // generate the implementation method
            StringBuilder sb = new StringBuilder();

            if (returnType != null) {
                sb.append("Object newKey = "); //$NON-NLS-1$
            }

            sb.append(daoTemplate.getInsertMethod(table.getSqlMapNamespace(), 
                    XmlConstants.INSERT_SELECTIVE_STATEMENT_ID,
                    "record")); //$NON-NLS-1$
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

        return method;
    }

    protected Method getUpdateByPrimaryKeyWithoutBLOBsMethod(
            IntrospectedTable introspectedTable, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        FullyQualifiedJavaType parameterType = 
            introspectedTable.getBaseRecordType();
        compilationUnit.addImportedType(parameterType);

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(methodNameCalculator.getUpdateByPrimaryKeyWithoutBLOBsMethodName(introspectedTable));
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        for (FullyQualifiedJavaType fqjt : daoTemplate.getCheckedExceptions()) {
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        ibatorContext.getCommentGenerator().addGeneralMethodComment(method, table);

        if (!interfaceMethod) {
            // generate the implementation method
            StringBuilder sb = new StringBuilder();

            sb.append("int rows = "); //$NON-NLS-1$
            sb.append(daoTemplate.getUpdateMethod(table.getSqlMapNamespace(),
                    XmlConstants.UPDATE_BY_PRIMARY_KEY_STATEMENT_ID,
                    "record")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());

            method.addBodyLine("return rows;"); //$NON-NLS-1$
        }

        return method;
    }

    protected Method getUpdateByPrimaryKeyWithBLOBsMethod(
            IntrospectedTable introspectedTable, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        FullyQualifiedJavaType parameterType;
        
        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            parameterType = introspectedTable.getRecordWithBLOBsType();
        } else {
            parameterType = introspectedTable.getBaseRecordType();
        }
        
        compilationUnit.addImportedType(parameterType);

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(methodNameCalculator.getUpdateByPrimaryKeyWithBLOBsMethodName(introspectedTable));
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        for (FullyQualifiedJavaType fqjt : daoTemplate.getCheckedExceptions()) {
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        ibatorContext.getCommentGenerator().addGeneralMethodComment(method, table);
        
        if (!interfaceMethod) {
            // generate the implementation method
            StringBuilder sb = new StringBuilder();

            sb.append("int rows = "); //$NON-NLS-1$
            sb.append(daoTemplate.getUpdateMethod(table.getSqlMapNamespace(),
                    XmlConstants.UPDATE_BY_PRIMARY_KEY_WITH_BLOBS_STATEMENT_ID,
                    "record")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());

            method.addBodyLine("return rows;"); //$NON-NLS-1$
        }

        return method;
    }

    protected Method getUpdateByPrimaryKeySelectiveMethod(
            IntrospectedTable introspectedTable, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        FullyQualifiedJavaType parameterType;
        
        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            parameterType = introspectedTable.getRecordWithBLOBsType();
        } else {
            parameterType = introspectedTable.getBaseRecordType();
        }
        
        compilationUnit.addImportedType(parameterType);

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(methodNameCalculator.getUpdateByPrimaryKeySelectiveMethodName(introspectedTable));
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$

        for (FullyQualifiedJavaType fqjt : daoTemplate.getCheckedExceptions()) {
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        ibatorContext.getCommentGenerator().addGeneralMethodComment(method, table);

        if (!interfaceMethod) {
            // generate the implementation method
            StringBuilder sb = new StringBuilder();

            sb.append("int rows = "); //$NON-NLS-1$
            sb.append(daoTemplate.getUpdateMethod(table.getSqlMapNamespace(),
                    XmlConstants.UPDATE_BY_PRIMARY_KEY_SELECTIVE_STATEMENT_ID,
                    "record")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());

            method.addBodyLine("return rows;"); //$NON-NLS-1$
        }

        return method;
    }

    protected Method getSelectByExampleWithoutBLOBsMethod(
            IntrospectedTable introspectedTable, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (interfaceMethod && exampleMethodVisibility != JavaVisibility.PUBLIC) {
            return null;
        }
        
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        FullyQualifiedJavaType type = introspectedTable.getExampleType();
        compilationUnit.addImportedType(type);
        compilationUnit.addImportedType(FullyQualifiedJavaType
                .getNewListInstance());

        Method method = new Method();
        method.setVisibility(exampleMethodVisibility);

        FullyQualifiedJavaType returnType = FullyQualifiedJavaType.getNewListInstance();;
        if (useJava5Features) {
            FullyQualifiedJavaType fqjt;
            if (introspectedTable.getRules().generateBaseRecordClass()) {
                fqjt = introspectedTable.getBaseRecordType();
            } else if (introspectedTable.getRules().generatePrimaryKeyClass()) {
                fqjt = introspectedTable.getPrimaryKeyType();
            } else {
                throw new RuntimeException(Messages
                        .getString("RuntimeError.12")); //$NON-NLS-1$
            }

            compilationUnit.addImportedType(fqjt);
            returnType.addTypeArgument(fqjt);
        }
        
        method.setReturnType(returnType);

        method.setName(methodNameCalculator.getSelectByExampleWithoutBLOBsMethodName(introspectedTable));
        method.addParameter(new Parameter(type, "example")); //$NON-NLS-1$

        for (FullyQualifiedJavaType fqjt : daoTemplate.getCheckedExceptions()) {
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        ibatorContext.getCommentGenerator().addGeneralMethodComment(method, table);

        if (!interfaceMethod) {
            // generate the implementation method

            if (useJava5Features) {
                method.addSuppressTypeWarningsAnnotation();
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(returnType.getShortName());
            sb.append(" list = "); //$NON-NLS-1$
            sb.append(daoTemplate.getQueryForListMethod(table.getSqlMapNamespace(),
                    XmlConstants.SELECT_BY_EXAMPLE_STATEMENT_ID,
                    "example")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
            method.addBodyLine("return list;"); //$NON-NLS-1$
        }

        return method;
    }

    protected Method getSelectByExampleWithBLOBsMethod(
            IntrospectedTable introspectedTable, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (interfaceMethod && exampleMethodVisibility != JavaVisibility.PUBLIC) {
            return null;
        }
        
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        FullyQualifiedJavaType type = introspectedTable.getExampleType();
        compilationUnit.addImportedType(type);
        compilationUnit.addImportedType(FullyQualifiedJavaType
                .getNewListInstance());

        Method method = new Method();
        method.setVisibility(exampleMethodVisibility);

        FullyQualifiedJavaType returnType = FullyQualifiedJavaType.getNewListInstance();
        if (useJava5Features) {
            FullyQualifiedJavaType fqjt;
            if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
                fqjt = introspectedTable.getRecordWithBLOBsType();
            } else {
                // the blob fields must be rolled up into the base class
                fqjt = introspectedTable.getBaseRecordType();
            }
            
            compilationUnit.addImportedType(fqjt);
            returnType.addTypeArgument(fqjt);
        }
        method.setReturnType(returnType);

        method.setName(methodNameCalculator.getSelectByExampleWithBLOBsMethodName(introspectedTable));
        method.addParameter(new Parameter(type, "example")); //$NON-NLS-1$

        for (FullyQualifiedJavaType fqjt : daoTemplate.getCheckedExceptions()) {
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        ibatorContext.getCommentGenerator().addGeneralMethodComment(method, table);

        if (!interfaceMethod) {
            // generate the implementation method

            if (useJava5Features) {
                method.addSuppressTypeWarningsAnnotation();
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append(returnType.getShortName());
            sb.append(" list = "); //$NON-NLS-1$
            sb.append(daoTemplate.getQueryForListMethod(table.getSqlMapNamespace(),
                    XmlConstants.SELECT_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID,
                    "example")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
            method.addBodyLine("return list;"); //$NON-NLS-1$
        }

        return method;
    }

    protected Method getSelectByPrimaryKeyMethod(
            IntrospectedTable introspectedTable, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);

        FullyQualifiedJavaType returnType =
            introspectedTable.getRules().calculateAllFieldsClass();
        method.setReturnType(returnType);
        compilationUnit.addImportedType(returnType);

        method.setName(methodNameCalculator.getSelectByPrimaryKeyMethodName(introspectedTable));
        
        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            FullyQualifiedJavaType type = introspectedTable.getPrimaryKeyType();
            compilationUnit.addImportedType(type);
            method.addParameter(new Parameter(type, "key")); //$NON-NLS-1$
        } else {
            for (ColumnDefinition cd : introspectedTable.getPrimaryKeyColumns()) {
                FullyQualifiedJavaType type = cd.getResolvedJavaType().getFullyQualifiedJavaType();
                compilationUnit.addImportedType(type);
                method.addParameter(new Parameter(type, cd.getJavaProperty()));
            }
        }

        for (FullyQualifiedJavaType fqjt : daoTemplate.getCheckedExceptions()) {
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        ibatorContext.getCommentGenerator().addGeneralMethodComment(method, table);

        if (!interfaceMethod) {
            // generate the implementation method
            StringBuilder sb = new StringBuilder();

            if (!introspectedTable.getRules().generatePrimaryKeyClass()) {
                // no primary key class, but primary key is enabled.  Primary
                // key columns must be in the base class.
                FullyQualifiedJavaType keyType = introspectedTable.getBaseRecordType();
                compilationUnit.addImportedType(keyType);
                
                sb.setLength(0);
                sb.append(keyType.getShortName());
                sb.append(" key = new "); //$NON-NLS-1$
                sb.append(keyType.getShortName());
                sb.append("();"); //$NON-NLS-1$
                method.addBodyLine(sb.toString());
                
                for (ColumnDefinition cd : introspectedTable.getPrimaryKeyColumns()) {
                    sb.setLength(0);
                    sb.append("key."); //$NON-NLS-1$
                    sb.append(JavaBeansUtil.getSetterMethodName(cd.getJavaProperty()));
                    sb.append('(');
                    sb.append(cd.getJavaProperty());
                    sb.append(");"); //$NON-NLS-1$
                    method.addBodyLine(sb.toString());
                }
            }

            sb.setLength(0);
            sb.append(returnType.getShortName());
            sb.append(" record = ("); //$NON-NLS-1$
            sb.append(returnType.getShortName());
            sb.append(") "); //$NON-NLS-1$
            sb.append(daoTemplate.getQueryForObjectMethod(table.getSqlMapNamespace(),
                    XmlConstants.SELECT_BY_PRIMARY_KEY_STATEMENT_ID,
                    "key")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
            method.addBodyLine("return record;"); //$NON-NLS-1$
        }

        return method;
    }

    protected Method getDeleteByExampleMethod(
            IntrospectedTable introspectedTable, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (interfaceMethod && exampleMethodVisibility != JavaVisibility.PUBLIC) {
            return null;
        }
        
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        FullyQualifiedJavaType type = introspectedTable.getExampleType();
        compilationUnit.addImportedType(type);

        Method method = new Method();
        method.setVisibility(exampleMethodVisibility);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(methodNameCalculator.getDeleteByExampleMethodName(introspectedTable));
        method.addParameter(new Parameter(type, "example")); //$NON-NLS-1$

        for (FullyQualifiedJavaType fqjt : daoTemplate.getCheckedExceptions()) {
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        ibatorContext.getCommentGenerator().addGeneralMethodComment(method, table);

        if (!interfaceMethod) {
            // generate the implementation method
            StringBuilder sb = new StringBuilder();

            sb.append("int rows = "); //$NON-NLS-1$
            sb.append(daoTemplate.getDeleteMethod(table.getSqlMapNamespace(),
                    XmlConstants.DELETE_BY_EXAMPLE_STATEMENT_ID,
                    "example")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
            method.addBodyLine("return rows;"); //$NON-NLS-1$
        }

        return method;
    }

    protected Method getDeleteByPrimaryKeyMethod(
            IntrospectedTable introspectedTable, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();

        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(methodNameCalculator.getDeleteByPrimaryKeyMethodName(introspectedTable));

        if (introspectedTable.getRules().generatePrimaryKeyClass()) {
            FullyQualifiedJavaType type = introspectedTable.getPrimaryKeyType();
            compilationUnit.addImportedType(type);
            method.addParameter(new Parameter(type, "key")); //$NON-NLS-1$
        } else {
            for (ColumnDefinition cd : introspectedTable.getPrimaryKeyColumns()) {
                FullyQualifiedJavaType type = cd.getResolvedJavaType().getFullyQualifiedJavaType();
                compilationUnit.addImportedType(type);
                method.addParameter(new Parameter(type, cd.getJavaProperty()));
            }
        }

        for (FullyQualifiedJavaType fqjt : daoTemplate.getCheckedExceptions()) {
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        ibatorContext.getCommentGenerator().addGeneralMethodComment(method, table);

        if (!interfaceMethod) {
            // generate the implementation method
            StringBuilder sb = new StringBuilder();

            if (!introspectedTable.getRules().generatePrimaryKeyClass()) {
                // no primary key class, but primary key is enabled.  Primary
                // key columns must be in the base class.
                FullyQualifiedJavaType keyType = introspectedTable.getBaseRecordType();
                compilationUnit.addImportedType(keyType);
                
                sb.setLength(0);
                sb.append(keyType.getShortName());
                sb.append(" key = new "); //$NON-NLS-1$
                sb.append(keyType.getShortName());
                sb.append("();"); //$NON-NLS-1$
                method.addBodyLine(sb.toString());
                
                for (ColumnDefinition cd : introspectedTable.getPrimaryKeyColumns()) {
                    sb.setLength(0);
                    sb.append("key."); //$NON-NLS-1$
                    sb.append(JavaBeansUtil.getSetterMethodName(cd.getJavaProperty()));
                    sb.append('(');
                    sb.append(cd.getJavaProperty());
                    sb.append(");"); //$NON-NLS-1$
                    method.addBodyLine(sb.toString());
                }
            }
            
            sb.setLength(0);
            sb.append("int rows = "); //$NON-NLS-1$
            sb.append(daoTemplate.getDeleteMethod(table.getSqlMapNamespace(),
                    XmlConstants.DELETE_BY_PRIMARY_KEY_STATEMENT_ID,
                    "key")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
            method.addBodyLine("return rows;"); //$NON-NLS-1$
        }

        return method;
    }

    protected Method getCountByExampleMethod(
            IntrospectedTable introspectedTable, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (interfaceMethod && exampleMethodVisibility != JavaVisibility.PUBLIC) {
            return null;
        }
        
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        FullyQualifiedJavaType type = introspectedTable.getExampleType();
        compilationUnit.addImportedType(type);

        Method method = new Method();
        method.setVisibility(exampleMethodVisibility);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(methodNameCalculator.getCountByExampleMethodName(introspectedTable));
        method.addParameter(new Parameter(type, "example")); //$NON-NLS-1$

        for (FullyQualifiedJavaType fqjt : daoTemplate.getCheckedExceptions()) {
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        ibatorContext.getCommentGenerator().addGeneralMethodComment(method, table);

        if (!interfaceMethod) {
            // generate the implementation method
            StringBuilder sb = new StringBuilder();

            sb.append("Integer count = (Integer)  "); //$NON-NLS-1$
            sb.append(daoTemplate.getQueryForObjectMethod(table.getSqlMapNamespace(),
                    XmlConstants.COUNT_BY_EXAMPLE_STATEMENT_ID,
                    "example")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());
            
            if (useJava5Features) {
                method.addBodyLine("return count;"); //$NON-NLS-1$
            } else {
                method.addBodyLine("return count.intValue();"); //$NON-NLS-1$
            }
        }

        return method;
    }

    public void setIbatorContext(IbatorContext ibatorContext) {
        this.ibatorContext = ibatorContext;
    }

    protected Method getUpdateByExampleSelectiveMethod(
            IntrospectedTable introspectedTable, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (interfaceMethod && exampleMethodVisibility != JavaVisibility.PUBLIC) {
            return null;
        }
        
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        FullyQualifiedJavaType parameterType;
        
        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            parameterType = introspectedTable.getRecordWithBLOBsType();
        } else if (introspectedTable.getRules().generateBaseRecordClass()) {
            parameterType = introspectedTable.getBaseRecordType();
        } else {
            parameterType = introspectedTable.getPrimaryKeyType();
        }
        
        compilationUnit.addImportedType(parameterType);

        Method method = new Method();
        method.setVisibility(exampleMethodVisibility);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(methodNameCalculator.getUpdateByExampleSelectiveMethodName(introspectedTable));
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$
        method.addParameter(new Parameter(introspectedTable.getExampleType(), "example")); //$NON-NLS-1$
        
        for (FullyQualifiedJavaType fqjt : daoTemplate.getCheckedExceptions()) {
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        ibatorContext.getCommentGenerator().addGeneralMethodComment(method, table);

        if (!interfaceMethod) {
            // generate the implementation method
            method.addBodyLine("UpdateByExampleParms parms = new UpdateByExampleParms(record, example);"); //$NON-NLS-1$
            
            StringBuilder sb = new StringBuilder();
            
            sb.append("int rows = "); //$NON-NLS-1$
            
            sb.append(daoTemplate.getUpdateMethod(table.getSqlMapNamespace(),
                    XmlConstants.UPDATE_BY_EXAMPLE_SELECTIVE_STATEMENT_ID,
                    "parms")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());

            method.addBodyLine("return rows;"); //$NON-NLS-1$
        }

        return method;
    }
    
    protected InnerClass getUpdateByExampleParms (IntrospectedTable introspectedTable,
            CompilationUnit compilationUnit) {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        compilationUnit.addImportedType(introspectedTable.getExampleType());
        
        InnerClass answer = new InnerClass(
                new FullyQualifiedJavaType("UpdateByExampleParms")); //$NON-NLS-1$
        answer.setVisibility(JavaVisibility.PRIVATE);
        answer.setStatic(true);
        answer.setSuperClass(introspectedTable.getExampleType());
        ibatorContext.getCommentGenerator().addClassComment(answer, table);
        
        Method method = new Method();
        method.setConstructor(true);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName(answer.getType().getShortName());
        method.addParameter(
                new Parameter(FullyQualifiedJavaType.getObjectInstance(),
                        "record")); //$NON-NLS-1$
        method.addParameter(
                new Parameter(introspectedTable.getExampleType(),
                        "example")); //$NON-NLS-1$
        method.addBodyLine("super(example);"); //$NON-NLS-1$
        method.addBodyLine("this.record = record;"); //$NON-NLS-1$
        answer.addMethod(method);
        
        Field field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(FullyQualifiedJavaType.getObjectInstance());
        field.setName("record"); //$NON-NLS-1$
        answer.addField(field);
        
        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getObjectInstance());
        method.setName("getRecord"); //$NON-NLS-1$
        method.addBodyLine("return record;"); //$NON-NLS-1$
        answer.addMethod(method);
        
        return answer;
    }

    protected Method getUpdateByExampleWithBLOBsMethod(
            IntrospectedTable introspectedTable, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (interfaceMethod && exampleMethodVisibility != JavaVisibility.PUBLIC) {
            return null;
        }
        
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        FullyQualifiedJavaType parameterType; 
        if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
            parameterType = introspectedTable.getRecordWithBLOBsType();
        } else {
            parameterType = introspectedTable.getBaseRecordType();
        }

        compilationUnit.addImportedType(parameterType);

        Method method = new Method();
        method.setVisibility(exampleMethodVisibility);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(methodNameCalculator.getUpdateByExampleWithBLOBsMethodName(introspectedTable));
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$
        method.addParameter(new Parameter(introspectedTable.getExampleType(), "example")); //$NON-NLS-1$

        for (FullyQualifiedJavaType fqjt : daoTemplate.getCheckedExceptions()) {
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        ibatorContext.getCommentGenerator().addGeneralMethodComment(method, table);

        if (!interfaceMethod) {
            // generate the implementation method
            method.addBodyLine("UpdateByExampleParms parms = new UpdateByExampleParms(record, example);"); //$NON-NLS-1$
            
            StringBuilder sb = new StringBuilder();

            sb.append("int rows = "); //$NON-NLS-1$
            sb.append(daoTemplate.getUpdateMethod(table.getSqlMapNamespace(),
                    XmlConstants.UPDATE_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID,
                    "parms")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());

            method.addBodyLine("return rows;"); //$NON-NLS-1$
        }

        return method;
    }

    protected Method getUpdateByExampleWithoutBLOBsMethod(
            IntrospectedTable introspectedTable, boolean interfaceMethod,
            CompilationUnit compilationUnit) {

        if (interfaceMethod && exampleMethodVisibility != JavaVisibility.PUBLIC) {
            return null;
        }
        
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        FullyQualifiedJavaType parameterType; 
        if (introspectedTable.getRules().generateBaseRecordClass()) {
            parameterType = introspectedTable.getBaseRecordType();
        } else {
            parameterType = introspectedTable.getPrimaryKeyType();
        }

        compilationUnit.addImportedType(parameterType);

        Method method = new Method();
        method.setVisibility(exampleMethodVisibility);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.setName(methodNameCalculator.getUpdateByExampleWithoutBLOBsMethodName(introspectedTable));
        method.addParameter(new Parameter(parameterType, "record")); //$NON-NLS-1$
        method.addParameter(new Parameter(introspectedTable.getExampleType(), "example")); //$NON-NLS-1$

        for (FullyQualifiedJavaType fqjt : daoTemplate.getCheckedExceptions()) {
            method.addException(fqjt);
            compilationUnit.addImportedType(fqjt);
        }

        ibatorContext.getCommentGenerator().addGeneralMethodComment(method, table);

        if (!interfaceMethod) {
            // generate the implementation method
            method.addBodyLine("UpdateByExampleParms parms = new UpdateByExampleParms(record, example);"); //$NON-NLS-1$
            
            StringBuilder sb = new StringBuilder();

            sb.append("int rows = "); //$NON-NLS-1$
            sb.append(daoTemplate.getUpdateMethod(table.getSqlMapNamespace(),
                    XmlConstants.UPDATE_BY_EXAMPLE_STATEMENT_ID,
                    "parms")); //$NON-NLS-1$
            method.addBodyLine(sb.toString());

            method.addBodyLine("return rows;"); //$NON-NLS-1$
        }

        return method;
    }
}
