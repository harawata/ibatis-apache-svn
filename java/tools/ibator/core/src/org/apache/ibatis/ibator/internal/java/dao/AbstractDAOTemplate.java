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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
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
        configure();
    }

    public Method getConstructorClone(CommentGenerator commentGenerator, FullyQualifiedJavaType type, FullyQualifiedTable table) {
        Method answer = new Method();
        answer.setConstructor(true);
        answer.setName(type.getShortName());
        answer.setVisibility(constructorTemplate.getVisibility());
        Iterator<Parameter> parmIter = constructorTemplate.getParameters().iterator();
        while (parmIter.hasNext()) {
            answer.addParameter(parmIter.next());
        }
        
        Iterator<String> strIter = constructorTemplate.getBodyLines().iterator();
        while (strIter.hasNext()) {
            answer.addBodyLine(strIter.next());
        }
        
        Iterator<FullyQualifiedJavaType> fqjtIter = constructorTemplate.getExceptions().iterator();
        while (fqjtIter.hasNext()) {
            answer.addException(fqjtIter.next());
        }
        
        commentGenerator.addGeneralMethodComment(answer, table);
        
        return answer;
    }

    public String getDeleteMethod(String sqlMapNamespace, String statementId,
            String parameter) {
        String answer = MessageFormat.format(deleteMethodTemplate,
                new Object[] { sqlMapNamespace, statementId, parameter });

        return answer;
    }

    public List<FullyQualifiedJavaType> getInterfaceImports() {
        return interfaceImports;
    }

    public List<FullyQualifiedJavaType> getImplementationImports() {
        return implementationImports;
    }

    public String getInsertMethod(String sqlMapNamespace, String statementId,
            String parameter) {
        String answer = MessageFormat.format(insertMethodTemplate,
                new Object[] { sqlMapNamespace, statementId, parameter });

        return answer;
    }

    public String getQueryForListMethod(String sqlMapNamespace, String statementId,
            String parameter) {
        String answer = MessageFormat.format(queryForListMethodTemplate,
                new Object[] { sqlMapNamespace, statementId, parameter });

        return answer;
    }

    public String getQueryForObjectMethod(String sqlMapNamespace, String statementId,
            String parameter) {
        String answer = MessageFormat.format(queryForObjectMethodTemplate,
                new Object[] { sqlMapNamespace, statementId, parameter });

        return answer;
    }

    public FullyQualifiedJavaType getSuperClass() {
        return superClass;
    }

    public String getUpdateMethod(String sqlMapNamespace, String statementId,
            String parameter) {
        String answer = MessageFormat.format(updateMethodTemplate,
                new Object[] { sqlMapNamespace, statementId, parameter });

        return answer;
    }

    public List<FullyQualifiedJavaType> getCheckedExceptions() {
        return checkedExceptions;
    }

    public Iterator<Field> getFieldClones(CommentGenerator commentGenerator, FullyQualifiedTable table) {
        List<Field> answer = new ArrayList<Field>();
        Iterator<Field> iter = fields.iterator();
        while (iter.hasNext()) {
            Field field = new Field();
            Field oldField = iter.next();
            
            field.setInitializationString(oldField.getInitializationString());
            field.setModifierFinal(oldField.isModifierFinal());
            field.setModifierStatic(oldField.isModifierStatic());
            field.setName(oldField.getName());
            field.setType(oldField.getType());
            field.setVisibility(oldField.getVisibility());
            commentGenerator.addFieldComment(field, table);
            answer.add(field);
        }
        
        return answer.iterator();
    }

    public Iterator<Method> getMethodClones(CommentGenerator commentGenerator, FullyQualifiedTable table) {
        List<Method> answer = new ArrayList<Method>();
        Iterator<Method> iter = methods.iterator();
        while (iter.hasNext()) {
            Method method = new Method();
            Method oldMethod = iter.next();

            Iterator<String> strIter = oldMethod.getBodyLines().iterator();
            while (strIter.hasNext()) {
                method.addBodyLine(strIter.next());
            }
            
            Iterator<FullyQualifiedJavaType> fqjtIter = oldMethod.getExceptions().iterator();
            while (fqjtIter.hasNext()) {
                method.addException(fqjtIter.next());
            }
            
            Iterator<Parameter> parmIter = oldMethod.getParameters().iterator();
            while (parmIter.hasNext()) {
                method.addParameter(parmIter.next());
            }
            
            method.setConstructor(oldMethod.isConstructor());
            method.setModifierFinal(oldMethod.isModifierFinal());
            method.setModifierStatic(oldMethod.isModifierStatic());
            method.setName(oldMethod.getName());
            method.setReturnType(oldMethod.getReturnType());
            method.setVisibility(oldMethod.getVisibility());
            
            commentGenerator.addGeneralMethodComment(method, table);
            
            answer.add(method);
        }
        
        return answer.iterator();
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
     * This method is called in the subclasses to configure the DAO template.
     * Subclasses should set the super class, define the methods, etc. that are
     * relevant for this type of DAO.
     *
     */
    protected abstract void configure();
}
