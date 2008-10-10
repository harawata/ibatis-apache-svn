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
package org.apache.ibatis.ibator.generator.ibatis2.dao.templates;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.ibator.api.CommentGenerator;
import org.apache.ibatis.ibator.api.FullyQualifiedTable;
import org.apache.ibatis.ibator.api.dom.java.Field;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.Parameter;

/**
 * @author Jeff Butler
 */
public abstract class AbstractDAOTemplate {
    private List<FullyQualifiedJavaType> interfaceImports;

    private List<FullyQualifiedJavaType> implementationImports;

    private FullyQualifiedJavaType superClass;

    private List<FullyQualifiedJavaType> checkedExceptions;

    private List<Field> fields;

    private List<Method> methods;

    private Method constructorTemplate;

    private String deleteMethodTemplate;

    private String insertMethodTemplate;

    private String updateMethodTemplate;

    private String queryForObjectMethodTemplate;

    private String queryForListMethodTemplate;
    
    private boolean configured;

    /**
     *  
     */
    public AbstractDAOTemplate() {
        super();
        interfaceImports = new ArrayList<FullyQualifiedJavaType>();
        implementationImports = new ArrayList<FullyQualifiedJavaType>();
        fields = new ArrayList<Field>();
        methods = new ArrayList<Method>();
        checkedExceptions = new ArrayList<FullyQualifiedJavaType>();
        configured = false;
    }

    public final Method getConstructorClone(CommentGenerator commentGenerator, FullyQualifiedJavaType type, FullyQualifiedTable table) {
        configure();
        Method answer = new Method();
        answer.setConstructor(true);
        answer.setName(type.getShortName());
        answer.setVisibility(constructorTemplate.getVisibility());
        for (Parameter parm : constructorTemplate.getParameters()) {
            answer.addParameter(parm);
        }

        for (String bodyLine: constructorTemplate.getBodyLines()) {
            answer.addBodyLine(bodyLine);
        }

        for (FullyQualifiedJavaType fqjt : constructorTemplate.getExceptions()) {
            answer.addException(fqjt);
        }
        
        commentGenerator.addGeneralMethodComment(answer, table);
        
        return answer;
    }

    public final String getDeleteMethod(String sqlMapNamespace, String statementId,
            String parameter) {
        configure();
        String answer = MessageFormat.format(deleteMethodTemplate,
                new Object[] { sqlMapNamespace, statementId, parameter });

        return answer;
    }

    public final List<FullyQualifiedJavaType> getInterfaceImports() {
        configure();
        return interfaceImports;
    }

    public final List<FullyQualifiedJavaType> getImplementationImports() {
        configure();
        return implementationImports;
    }

    public final String getInsertMethod(String sqlMapNamespace, String statementId,
            String parameter) {
        configure();
        String answer = MessageFormat.format(insertMethodTemplate,
                new Object[] { sqlMapNamespace, statementId, parameter });

        return answer;
    }

    public final String getQueryForListMethod(String sqlMapNamespace, String statementId,
            String parameter) {
        configure();
        String answer = MessageFormat.format(queryForListMethodTemplate,
                new Object[] { sqlMapNamespace, statementId, parameter });

        return answer;
    }

    public final String getQueryForObjectMethod(String sqlMapNamespace, String statementId,
            String parameter) {
        configure();
        String answer = MessageFormat.format(queryForObjectMethodTemplate,
                new Object[] { sqlMapNamespace, statementId, parameter });

        return answer;
    }

    public final FullyQualifiedJavaType getSuperClass() {
        configure();
        return superClass;
    }

    public final String getUpdateMethod(String sqlMapNamespace, String statementId,
            String parameter) {
        configure();
        String answer = MessageFormat.format(updateMethodTemplate,
                new Object[] { sqlMapNamespace, statementId, parameter });

        return answer;
    }

    public final List<FullyQualifiedJavaType> getCheckedExceptions() {
        configure();
        return checkedExceptions;
    }

    public final List<Field> getFieldClones(CommentGenerator commentGenerator, FullyQualifiedTable table) {
        configure();
        List<Field> answer = new ArrayList<Field>();
        for (Field oldField : fields) {
            Field field = new Field();
            
            field.setInitializationString(oldField.getInitializationString());
            field.setFinal(oldField.isFinal());
            field.setStatic(oldField.isStatic());
            field.setName(oldField.getName());
            field.setType(oldField.getType());
            field.setVisibility(oldField.getVisibility());
            commentGenerator.addFieldComment(field, table);
            answer.add(field);
        }
        
        return answer;
    }

    public final List<Method> getMethodClones(CommentGenerator commentGenerator, FullyQualifiedTable table) {
        configure();
        List<Method> answer = new ArrayList<Method>();
        for (Method oldMethod : methods) {
            Method method = new Method();

            for (String bodyLine : oldMethod.getBodyLines()) {
                method.addBodyLine(bodyLine);
            }
            
            for (FullyQualifiedJavaType fqjt : oldMethod.getExceptions()) {
                method.addException(fqjt);
            }

            for (Parameter parm : oldMethod.getParameters()) {
                method.addParameter(parm);
            }
            
            method.setConstructor(oldMethod.isConstructor());
            method.setFinal(oldMethod.isFinal());
            method.setStatic(oldMethod.isStatic());
            method.setName(oldMethod.getName());
            method.setReturnType(oldMethod.getReturnType());
            method.setVisibility(oldMethod.getVisibility());
            
            commentGenerator.addGeneralMethodComment(method, table);
            
            answer.add(method);
        }
        
        return answer;
    }

    protected void setConstructorTemplate(Method constructorTemplate) {
        this.constructorTemplate = constructorTemplate;
    }

    protected void setDeleteMethodTemplate(String deleteMethodTemplate) {
        this.deleteMethodTemplate = deleteMethodTemplate;
    }

    protected void addField(Field field) {
        fields.add(field);
    }

    protected void setInsertMethodTemplate(String insertMethodTemplate) {
        this.insertMethodTemplate = insertMethodTemplate;
    }

    protected void addMethod(Method method) {
        methods.add(method);
    }

    protected void setQueryForListMethodTemplate(String queryForListMethodTemplate) {
        this.queryForListMethodTemplate = queryForListMethodTemplate;
    }

    protected void setQueryForObjectMethodTemplate(String queryForObjectMethodTemplate) {
        this.queryForObjectMethodTemplate = queryForObjectMethodTemplate;
    }

    protected void setSuperClass(FullyQualifiedJavaType superClass) {
        this.superClass = superClass;
    }

    protected void setUpdateMethodTemplate(String updateMethodTemplate) {
        this.updateMethodTemplate = updateMethodTemplate;
    }

    protected void addInterfaceImport(FullyQualifiedJavaType type) {
        interfaceImports.add(type);
    }

    protected void addImplementationImport(FullyQualifiedJavaType type) {
        implementationImports.add(type);
    }

    protected void addCheckedException(FullyQualifiedJavaType type) {
        checkedExceptions.add(type);
    }
    
    /**
     * This method is called in the constructor to configure the DAO template.
     * Subclasses should implement the individual configureXXX methods to
     * setup the relevant parts of the DAO template (super class,
     * extra methods, etc.) that are relevant for this specific type of DAO.
     */
    private void configure() {
        if (!configured) {
            configureCheckedExceptions();
            configureConstructorTemplate();
            configureDeleteMethodTemplate();
            configureFields();
            configureImplementationImports();
            configureInsertMethodTemplate();
            configureInterfaceImports();
            configureMethods();
            configureQueryForListMethodTemplate();
            configureQueryForObjectMethodTemplate();
            configureSuperClass();
            configureUpdateMethodTemplate();
            configured = true;
        }
    }
    
    // TODO - fully document each of these methods
    protected void configureCheckedExceptions() {
    }
    protected void configureFields() {
    }
    protected void configureImplementationImports() {
    }
    protected void configureInterfaceImports() {
    }
    protected void configureMethods() {
    }
    protected void configureSuperClass() {
    }

    protected abstract void configureInsertMethodTemplate();
    protected abstract void configureQueryForListMethodTemplate();
    protected abstract void configureQueryForObjectMethodTemplate();
    protected abstract void configureUpdateMethodTemplate();
    protected abstract void configureConstructorTemplate();
    protected abstract void configureDeleteMethodTemplate();
}
