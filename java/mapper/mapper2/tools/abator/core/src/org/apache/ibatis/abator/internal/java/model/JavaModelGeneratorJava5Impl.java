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
package org.apache.ibatis.abator.internal.java.model;

import java.util.Iterator;
import java.util.ListIterator;

import org.apache.ibatis.abator.api.dom.OutputUtilities;
import org.apache.ibatis.abator.api.dom.java.CompilationUnit;
import org.apache.ibatis.abator.api.dom.java.Field;
import org.apache.ibatis.abator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.abator.api.dom.java.InnerClass;
import org.apache.ibatis.abator.api.dom.java.InnerEnum;
import org.apache.ibatis.abator.api.dom.java.JavaVisibility;
import org.apache.ibatis.abator.api.dom.java.Method;
import org.apache.ibatis.abator.api.dom.java.Parameter;
import org.apache.ibatis.abator.api.dom.java.TopLevelClass;
import org.apache.ibatis.abator.config.FullyQualifiedTable;
import org.apache.ibatis.abator.config.TableConfiguration;
import org.apache.ibatis.abator.internal.db.ColumnDefinition;
import org.apache.ibatis.abator.internal.db.ColumnDefinitions;
import org.apache.ibatis.abator.internal.rules.AbatorRules;
import org.apache.ibatis.abator.internal.util.JavaBeansUtil;

/**
 * This class overrides methods in the Java2 implementation to provide
 * Java5 support in the Example class - using an inner enum and typed
 * collections.
 * 
 * @author Jeff Butler
 */
public class JavaModelGeneratorJava5Impl extends JavaModelGeneratorJava2Impl {

    public JavaModelGeneratorJava5Impl() {
        super();
    }

    protected CompilationUnit getExample(ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration) {
        if (!AbatorRules.generateExampleExtendingPrimaryKey(columnDefinitions,
                tableConfiguration)
                && !AbatorRules.generateExampleExtendingBaseRecord(
                        columnDefinitions, tableConfiguration)) {
            return null;
        }

        FullyQualifiedJavaType type = getExampleType(tableConfiguration
                .getTable());
        TopLevelClass topLevelClass = new TopLevelClass(type);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);

        topLevelClass.addInnerEnum(getCompareTypeEnum(tableConfiguration.getTable()));

        // add field, getter, setter for orderby clause
        Field field = new Field();
        field.addComment(tableConfiguration.getTable());
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(FullyQualifiedJavaType.getStringInstance());
        field.setName("orderByClause"); //$NON-NLS-1$
        topLevelClass.addField(field);

        Method method = new Method();
        method.addComment(tableConfiguration.getTable());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("setOrderByClause"); //$NON-NLS-1$
        method.addParameter(new Parameter(FullyQualifiedJavaType
                .getStringInstance(), "orderByClause")); //$NON-NLS-1$
        method.addBodyLine("this.orderByClause = orderByClause;"); //$NON-NLS-1$
        topLevelClass.addMethod(method);

        method = new Method();
        method.addComment(tableConfiguration.getTable());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getStringInstance());
        method.setName("getOrderByClause"); //$NON-NLS-1$
        method.addBodyLine("return orderByClause;"); //$NON-NLS-1$
        topLevelClass.addMethod(method);

        // add field and methods for the list of conditions
        field = new Field();
        field.addComment(tableConfiguration.getTable());
        field.setVisibility(JavaVisibility.PRIVATE);
        
        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType("java.util.List", //$NON-NLS-1$
                new FullyQualifiedJavaType[] {new FullyQualifiedJavaType("AndedCondition")}); //$NON-NLS-1$
        
        field.setType(fqjt);
        field.setName("oredConditions"); //$NON-NLS-1$
        field.setInitializationString("new ArrayList<AndedCondition>()"); //$NON-NLS-1$
        topLevelClass.addField(field);

        method = new Method();
        method.addComment(tableConfiguration.getTable());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(fqjt);
        method.setName("getOredConditions"); //$NON-NLS-1$
        method.addBodyLine("return oredConditions;"); //$NON-NLS-1$
        topLevelClass.addMethod(method);

        method = new Method();
        method.addComment(tableConfiguration.getTable());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("addOredCondition"); //$NON-NLS-1$
        method.addParameter(new Parameter(new FullyQualifiedJavaType(
                "AndedCondition"), //$NON-NLS-1$
                "andedCondition")); //$NON-NLS-1$

        StringBuffer sb = new StringBuffer();
        boolean ifStarted = false;
        ListIterator listIter = columnDefinitions.getAllColumns().listIterator();
        while (listIter.hasNext()) {
            ColumnDefinition cd = (ColumnDefinition) listIter.next();

            if (cd.isBLOBColumn()) {
                continue;
            }

            sb.setLength(0);
            if (ifStarted) {
                OutputUtilities.javaIndent(sb, 2);
                sb.append("|| "); //$NON-NLS-1$
            } else {
                sb.append("if ("); //$NON-NLS-1$
                ifStarted = true;
            }
            sb.append("andedCondition."); //$NON-NLS-1$
            sb.append(JavaBeansUtil.getGetterMethodName(cd.getByExampleIndicatorProperty()));
            sb.append("() != CompareType.EXAMPLE_IGNORE"); //$NON-NLS-1$
            
            if (listIter.hasNext()) {
                // if the next column is a BLOB, and it is the last column, then
                // we need to end the if statement
                ColumnDefinition cd2 = (ColumnDefinition) listIter.next();
                if (cd2.isBLOBColumn() && !listIter.hasNext()) {
                    sb.append(") {"); //$NON-NLS-1$
                }
                
                listIter.previous();
            } else {
                sb.append(") {"); //$NON-NLS-1$
            }
            
            method.addBodyLine(sb.toString());
        }
        
        method.addBodyLine("oredConditions.add(andedCondition);"); //$NON-NLS-1$
        method.addBodyLine("} else {"); //$NON-NLS-1$
        method.addBodyLine("throw new RuntimeException(\"At least one condition must be specified\");"); //$NON-NLS-1$
        method.addBodyLine("}"); //$NON-NLS-1$
        
        topLevelClass.addMethod(method);

        // now generate the inner class that holds the AND conditions
        topLevelClass.addInnerClass(getAndedConditionInnerClass(topLevelClass,
                columnDefinitions, tableConfiguration));

        return topLevelClass;
    }

    /**
     * This method returns a method that sets up QBE
     * conditions for an individual column. In the generated example, the method
     * will be called by the <code>calculateConditions</code> method. The
     * expectation is that there will be one column based method for each column
     * in the table (except BLOB columns). We do it this way to avoid generating
     * one huge method - which in some cases can actually be too large to
     * compile.
     * 
     * @param cd
     *            the column for which the method should be generated
     * @param table
     *            the table in which the column exists
     * @return the properly formatted method
     */
    protected Method getCalculateConditionMethod(ColumnDefinition cd,
            FullyQualifiedTable table) {
        if (cd.isBLOBColumn()) {
            return null;
        }
        StringBuffer sb = new StringBuffer();

        Method answer = new Method();
        answer.setVisibility(JavaVisibility.PRIVATE);
        sb.setLength(0);
        sb.append("calculate"); //$NON-NLS-1$
        sb.append(cd.getColumnName());
        sb.append("Condition"); //$NON-NLS-1$
        answer.setName(sb.toString());

        answer.addBodyLine("Map<String, Object> exampleMap;"); //$NON-NLS-1$
        answer.addBodyLine(""); //$NON-NLS-1$

        String getterMethodName = JavaBeansUtil.getGetterMethodName(cd.getByExampleIndicatorProperty());
        sb.setLength(0);
        sb.append("switch ("); //$NON-NLS-1$
        sb.append(getterMethodName);
        sb.append("()) {"); //$NON-NLS-1$
        answer.addBodyLine(sb.toString());

        Iterator clauseIterator = ExampleClause.getAllExampleClauses();
        while (clauseIterator.hasNext()) {
            ExampleClause clause = (ExampleClause) clauseIterator.next();

            if (clause.isCharacterOnly() && !cd.isCharacterColumn()) {
                continue;
            }

            sb.setLength(0);
            sb.append("case "); //$NON-NLS-1$
            sb.append(clause.getExamplePropertyName());
            sb.append(':');
            answer.addBodyLine(sb.toString());

            if (clause.isPropertyInMapRequired()) {
                answer.addBodyLine("exampleMap = new HashMap<String, Object>();"); //$NON-NLS-1$

                sb.setLength(0);
                sb.append("exampleMap.put(\"condition\", \""); //$NON-NLS-1$
                sb.append(clause.getClause(cd));
                sb.append("\");"); //$NON-NLS-1$
                answer.addBodyLine(sb.toString());

                sb.setLength(0);
                sb.append("exampleMap.put(\"value\", "); //$NON-NLS-1$

                String exampleProperty = cd.getJavaProperty();
                FullyQualifiedJavaType fqjt = cd.getResolvedJavaType()
                        .getFullyQualifiedJavaType();
                if (fqjt.isPrimitive()) {
                    sb.append("new "); //$NON-NLS-1$
                    sb.append(fqjt.getWrapperClass());
                    sb.append('(');
                    sb.append(JavaBeansUtil
                            .getGetterMethodName(exampleProperty));
                    sb.append("()));"); //$NON-NLS-1$
                } else {
                    sb.append(JavaBeansUtil
                            .getGetterMethodName(exampleProperty));
                    sb.append("());"); //$NON-NLS-1$
                }
                answer.addBodyLine(sb.toString());

                if ("DATE".equalsIgnoreCase(cd.getResolvedJavaType().getJdbcTypeName())) { //$NON-NLS-1$
                    answer.addBodyLine("conditionsWithDateValues.add(exampleMap);"); //$NON-NLS-1$
                } else if ("TIME".equalsIgnoreCase(cd.getResolvedJavaType().getJdbcTypeName())) { //$NON-NLS-1$
                    answer.addBodyLine("conditionsWithTimeValues.add(exampleMap);"); //$NON-NLS-1$
                } else {
                    answer.addBodyLine("conditionsWithValues.add(exampleMap);"); //$NON-NLS-1$
                }

                answer.addBodyLine("break;"); //$NON-NLS-1$
            } else {
                sb.setLength(0);
                sb.append("conditionsWithoutValues.add(\""); //$NON-NLS-1$
                sb.append(clause.getClause(cd));
                sb.append("\");"); //$NON-NLS-1$
                answer.addBodyLine(sb.toString());
                answer.addBodyLine("break;"); //$NON-NLS-1$
            }
        }

        answer.addBodyLine("}"); //$NON-NLS-1$

        return answer;
    }

    protected InnerClass getAndedConditionInnerClass(TopLevelClass topLevelClass,
            ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration) {
        InnerClass answer = new InnerClass(new FullyQualifiedJavaType(
                "AndedCondition")); //$NON-NLS-1$
        Field field;
        Method method;
        answer.setVisibility(JavaVisibility.PUBLIC);
        answer.setModifierStatic(true);
        answer.addComment(tableConfiguration.getTable());

        if (AbatorRules.generateExampleExtendingPrimaryKey(columnDefinitions,
                tableConfiguration)) {
            answer.setSuperClass(getPrimaryKeyType(tableConfiguration
                    .getTable()));
        } else {
            answer.setSuperClass(getRecordType(tableConfiguration.getTable()));
        }

        // generate indicator field getters and setters
        StringBuffer sb = new StringBuffer();
        Iterator iter = columnDefinitions.getAllColumns().iterator();
        while (iter.hasNext()) {
            ColumnDefinition cd = (ColumnDefinition) iter.next();

            if (cd.isBLOBColumn()) {
                continue;
            }

            String fieldName = cd.getByExampleIndicatorProperty();

            field = new Field();
            field.setVisibility(JavaVisibility.PRIVATE);
            field.setType(new FullyQualifiedJavaType("CompareType")); //$NON-NLS-1$
            field.setName(fieldName);
            field.setInitializationString("CompareType.EXAMPLE_IGNORE"); //$NON-NLS-1$
            answer.addField(field);

            method = new Method();
            method.setVisibility(JavaVisibility.PUBLIC);
            method.setReturnType(field.getType());
            method.setName(JavaBeansUtil.getGetterMethodName(fieldName));
            sb.setLength(0);
            sb.append("return "); //$NON-NLS-1$
            sb.append(fieldName);
            sb.append(';');
            method.addBodyLine(sb.toString());
            answer.addMethod(method);

            method = new Method();
            method.setVisibility(JavaVisibility.PUBLIC);
            method.setName(JavaBeansUtil.getSetterMethodName(fieldName));
            method.addParameter(new Parameter(field.getType(), fieldName));
            sb.setLength(0);
            sb.append("this."); //$NON-NLS-1$
            sb.append(fieldName);
            sb.append(" = "); //$NON-NLS-1$
            sb.append(fieldName);
            sb.append(';');
            method.addBodyLine(sb.toString());
            answer.addMethod(method);
        }

        // we now have the indicators and corresponding getters/setters
        // generated.
        // now we need to generate the methods that will be used in the SqlMap
        // to
        // generate the dynamic where clause
        topLevelClass.addImportedType(FullyQualifiedJavaType.getMapInstance());
        topLevelClass.addImportedType(FullyQualifiedJavaType.getListInstance());
        topLevelClass.addImportedType(FullyQualifiedJavaType
                .getHashMapInstance());
        topLevelClass.addImportedType(FullyQualifiedJavaType
                .getArrayListInstance());

        field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(new FullyQualifiedJavaType("java.util.List", //$NON-NLS-1$
                new FullyQualifiedJavaType[] {FullyQualifiedJavaType.getStringInstance()}));
        field.setName("conditionsWithoutValues"); //$NON-NLS-1$
        answer.addField(field);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(field.getType());
        method.setName(JavaBeansUtil.getGetterMethodName(field.getName()));
        method.addBodyLine("if (conditionsWithoutValues == null) {"); //$NON-NLS-1$
        method.addBodyLine("calculateConditions();"); //$NON-NLS-1$
        method.addBodyLine("}"); //$NON-NLS-1$
        method.addBodyLine(""); //$NON-NLS-1$
        method.addBodyLine("return conditionsWithoutValues;"); //$NON-NLS-1$
        answer.addMethod(method);

        FullyQualifiedJavaType innerMapType = new FullyQualifiedJavaType("java.util.Map", //$NON-NLS-1$
                new FullyQualifiedJavaType[] {FullyQualifiedJavaType.getStringInstance(),
                new FullyQualifiedJavaType("java.lang.Object")}); //$NON-NLS-1$
        
        field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(new FullyQualifiedJavaType("java.util.List", //$NON-NLS-1$
                new FullyQualifiedJavaType[] {innerMapType}));
        field.setName("conditionsWithDateValues"); //$NON-NLS-1$
        answer.addField(field);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(field.getType());
        method.setName(JavaBeansUtil.getGetterMethodName(field.getName()));
        method.addBodyLine("if (conditionsWithDateValues == null) {"); //$NON-NLS-1$
        method.addBodyLine("calculateConditions();"); //$NON-NLS-1$
        method.addBodyLine("}"); //$NON-NLS-1$
        method.addBodyLine(""); //$NON-NLS-1$
        method.addBodyLine("return conditionsWithDateValues;"); //$NON-NLS-1$
        answer.addMethod(method);

        field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(new FullyQualifiedJavaType("java.util.List", //$NON-NLS-1$
                new FullyQualifiedJavaType[] {innerMapType}));
        field.setName("conditionsWithTimeValues"); //$NON-NLS-1$
        answer.addField(field);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(field.getType());
        method.setName(JavaBeansUtil.getGetterMethodName(field.getName()));
        method.addBodyLine("if (conditionsWithTimeValues == null) {"); //$NON-NLS-1$
        method.addBodyLine("calculateConditions();"); //$NON-NLS-1$
        method.addBodyLine("}"); //$NON-NLS-1$
        method.addBodyLine(""); //$NON-NLS-1$
        method.addBodyLine("return conditionsWithTimeValues;"); //$NON-NLS-1$
        answer.addMethod(method);

        field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(new FullyQualifiedJavaType("java.util.List", //$NON-NLS-1$
                new FullyQualifiedJavaType[] {innerMapType}));
        field.setName("conditionsWithValues"); //$NON-NLS-1$
        answer.addField(field);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(field.getType());
        method.setName("getConditionsWithValues"); //$NON-NLS-1$
        method.addBodyLine("if (conditionsWithValues == null) {"); //$NON-NLS-1$
        method.addBodyLine("calculateConditions();"); //$NON-NLS-1$
        method.addBodyLine("}"); //$NON-NLS-1$
        method.addBodyLine(""); //$NON-NLS-1$
        method.addBodyLine("return conditionsWithValues;"); //$NON-NLS-1$
        answer.addMethod(method);

        // now add the methods that determine if the "ands" are required - this is too
        // complex a calculation to be done inside the sql map
        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getBooleanInstance());
        method.setName("isFirstAndNeeded"); //$NON-NLS-1$
        method.addBodyLine("if (conditionsWithValues.size() > 0 && conditionsWithDateValues.size() > 0) {"); //$NON-NLS-1$
        method.addBodyLine("return true;"); //$NON-NLS-1$
        method.addBodyLine("} else {"); //$NON-NLS-1$
        method.addBodyLine("return false;"); //$NON-NLS-1$
        method.addBodyLine("}"); //$NON-NLS-1$
        answer.addMethod(method);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getBooleanInstance());
        method.setName("isSecondAndNeeded"); //$NON-NLS-1$
        method.addBodyLine("if ((conditionsWithValues.size() > 0 || conditionsWithDateValues.size() > 0)"); //$NON-NLS-1$
        sb.setLength(0);
        OutputUtilities.javaIndent(sb, 2);
        sb.append("&& conditionsWithTimeValues.size() > 0) {"); //$NON-NLS-1$
        method.addBodyLine(sb.toString());
        method.addBodyLine("return true;"); //$NON-NLS-1$
        method.addBodyLine("} else {"); //$NON-NLS-1$
        method.addBodyLine("return false;"); //$NON-NLS-1$
        method.addBodyLine("}"); //$NON-NLS-1$
        answer.addMethod(method);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getBooleanInstance());
        method.setName("isThirdAndNeeded"); //$NON-NLS-1$
        method.addBodyLine("if ((conditionsWithValues.size() > 0 || conditionsWithDateValues.size() > 0 || conditionsWithTimeValues.size() > 0)"); //$NON-NLS-1$
        sb.setLength(0);
        OutputUtilities.javaIndent(sb, 2);
        sb.append("&& conditionsWithoutValues.size() > 0) {"); //$NON-NLS-1$
        method.addBodyLine(sb.toString());
        method.addBodyLine("return true;"); //$NON-NLS-1$
        method.addBodyLine("} else {"); //$NON-NLS-1$
        method.addBodyLine("return false;"); //$NON-NLS-1$
        method.addBodyLine("}"); //$NON-NLS-1$
        answer.addMethod(method);

        method = new Method();
        method.setVisibility(JavaVisibility.PRIVATE);
        method.setName("calculateConditions"); //$NON-NLS-1$
        method.addBodyLine("conditionsWithoutValues = new ArrayList<String>();"); //$NON-NLS-1$
        method.addBodyLine("conditionsWithValues = new ArrayList<Map<String, Object>>();"); //$NON-NLS-1$
        method.addBodyLine("conditionsWithDateValues = new ArrayList<Map<String, Object>>();"); //$NON-NLS-1$
        method.addBodyLine("conditionsWithTimeValues = new ArrayList<Map<String, Object>>();"); //$NON-NLS-1$
        method.addBodyLine(""); //$NON-NLS-1$
        answer.addMethod(method);

        iter = columnDefinitions.getAllColumns().iterator();
        while (iter.hasNext()) {
            ColumnDefinition cd = (ColumnDefinition) iter.next();

            if (cd.isBLOBColumn()) {
                continue;
            }

            Method otherMethod = getCalculateConditionMethod(cd,
                    tableConfiguration.getTable());
            if (otherMethod != null) {
                answer.addMethod(otherMethod);

                sb.setLength(0);
                sb.append(otherMethod.getName());
                sb.append("();"); //$NON-NLS-1$
                method.addBodyLine(sb.toString());
            }
        }

        return answer;
    }
    
    protected InnerEnum getCompareTypeEnum(FullyQualifiedTable table) {
        InnerEnum answer = new InnerEnum(new FullyQualifiedJavaType("CompareType")); //$NON-NLS-1$
        answer.setVisibility(JavaVisibility.PUBLIC);
        answer.addComment(table);

        answer.addEnumConstant("EXAMPLE_IGNORE"); //$NON-NLS-1$

        Iterator iter = ExampleClause.getAllExampleClauses();
        while (iter.hasNext()) {
            ExampleClause clause = (ExampleClause) iter.next();
            answer.addEnumConstant(clause.getExamplePropertyName());
        }
        
        return answer;
    }
}
