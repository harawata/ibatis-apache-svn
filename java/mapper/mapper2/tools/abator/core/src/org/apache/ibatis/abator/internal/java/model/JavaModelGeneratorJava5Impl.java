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

import org.apache.ibatis.abator.api.dom.OutputUtilities;
import org.apache.ibatis.abator.api.dom.java.CompilationUnit;
import org.apache.ibatis.abator.api.dom.java.Field;
import org.apache.ibatis.abator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.abator.api.dom.java.InnerClass;
import org.apache.ibatis.abator.api.dom.java.JavaVisibility;
import org.apache.ibatis.abator.api.dom.java.JavaWildcardType;
import org.apache.ibatis.abator.api.dom.java.Method;
import org.apache.ibatis.abator.api.dom.java.Parameter;
import org.apache.ibatis.abator.api.dom.java.TopLevelClass;
import org.apache.ibatis.abator.config.TableConfiguration;
import org.apache.ibatis.abator.internal.db.ColumnDefinition;
import org.apache.ibatis.abator.internal.db.ColumnDefinitions;
import org.apache.ibatis.abator.internal.rules.AbatorRules;
import org.apache.ibatis.abator.internal.util.JavaBeansUtil;

/**
 * This class overrides methods in the Java2 implementation to provide Java5
 * support in the Example class - using typed collections.
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

        FullyQualifiedJavaType fqjt = FullyQualifiedJavaType
                .getNewListInstance();
        fqjt.addTypeArgument(new FullyQualifiedJavaType("AndedCondition")); //$NON-NLS-1$

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

        method.addBodyLine("if (andedCondition.isValid()) {"); //$NON-NLS-1$
        method.addBodyLine("oredConditions.add(andedCondition);"); //$NON-NLS-1$
        method.addBodyLine("} else {"); //$NON-NLS-1$
        method
                .addBodyLine("throw new RuntimeException(\"At least one condition must be specified\");"); //$NON-NLS-1$
        method.addBodyLine("}"); //$NON-NLS-1$

        topLevelClass.addMethod(method);

        // now generate the inner class that holds the AND conditions
        topLevelClass.addInnerClass(getAndedConditionInnerClass(topLevelClass,
                columnDefinitions, tableConfiguration));

        return topLevelClass;
    }

    protected InnerClass getAndedConditionInnerClass(
            TopLevelClass topLevelClass, ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration) {
        Field field;
        Method method;
        StringBuffer sb = new StringBuffer();

        InnerClass answer = new InnerClass(new FullyQualifiedJavaType(
                "AndedCondition")); //$NON-NLS-1$

        answer.setVisibility(JavaVisibility.PUBLIC);
        answer.setModifierStatic(true);
        answer.addComment(tableConfiguration.getTable());

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setName("AndedCondition"); //$NON-NLS-1$
        method.setConstructor(true);
        method.addBodyLine("super();"); //$NON-NLS-1$
        method.addBodyLine("conditionsWithoutValue = new ArrayList<String>();"); //$NON-NLS-1$
        method
                .addBodyLine("conditionsWithSingleValue = new ArrayList<Map<String, Object>>();"); //$NON-NLS-1$
        method
                .addBodyLine("conditionsWithListValue = new ArrayList<Map<String, Object>>();"); //$NON-NLS-1$
        method
                .addBodyLine("conditionsWithBetweenValue = new ArrayList<Map<String, Object>>();"); //$NON-NLS-1$
        answer.addMethod(method);

        // now we need to generate the methods that will be used in the SqlMap
        // to generate the dynamic where clause
        topLevelClass.addImportedType(FullyQualifiedJavaType
                .getNewMapInstance());
        topLevelClass.addImportedType(FullyQualifiedJavaType
                .getNewListInstance());
        topLevelClass.addImportedType(FullyQualifiedJavaType
                .getNewHashMapInstance());
        topLevelClass.addImportedType(FullyQualifiedJavaType
                .getNewArrayListInstance());

        field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        FullyQualifiedJavaType listOfStrings = FullyQualifiedJavaType
                .getNewListInstance();
        listOfStrings.addTypeArgument(FullyQualifiedJavaType
                .getStringInstance());
        field.setType(listOfStrings);
        field.setName("conditionsWithoutValue"); //$NON-NLS-1$
        answer.addField(field);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(field.getType());
        method.setName(JavaBeansUtil.getGetterMethodName(field.getName()));
        method.addBodyLine("return conditionsWithoutValue;"); //$NON-NLS-1$
        answer.addMethod(method);

        FullyQualifiedJavaType innerMapType = FullyQualifiedJavaType
                .getNewMapInstance();
        innerMapType
                .addTypeArgument(FullyQualifiedJavaType.getStringInstance());
        innerMapType
                .addTypeArgument(FullyQualifiedJavaType.getObjectInstance());

        FullyQualifiedJavaType listOfMaps = FullyQualifiedJavaType
                .getNewListInstance();
        listOfMaps.addTypeArgument(innerMapType);

        field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(listOfMaps);
        field.setName("conditionsWithSingleValue"); //$NON-NLS-1$
        answer.addField(field);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(field.getType());
        method.setName(JavaBeansUtil.getGetterMethodName(field.getName()));
        method.addBodyLine("return conditionsWithSingleValue;"); //$NON-NLS-1$
        answer.addMethod(method);

        field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(listOfMaps);
        field.setName("conditionsWithListValue"); //$NON-NLS-1$
        answer.addField(field);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(field.getType());
        method.setName(JavaBeansUtil.getGetterMethodName(field.getName()));
        method.addBodyLine("return conditionsWithListValue;"); //$NON-NLS-1$
        answer.addMethod(method);

        field = new Field();
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setType(listOfMaps);
        field.setName("conditionsWithBetweenValue"); //$NON-NLS-1$
        answer.addField(field);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(field.getType());
        method.setName(JavaBeansUtil.getGetterMethodName(field.getName()));
        method.addBodyLine("return conditionsWithBetweenValue;"); //$NON-NLS-1$
        answer.addMethod(method);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType
                .getBooleanPrimitiveInstance());
        method.setName("isValid"); //$NON-NLS-1$
        method.addBodyLine("return conditionsWithoutValue.size() > 0"); //$NON-NLS-1$
        sb.setLength(0);
        OutputUtilities.javaIndent(sb, 2);
        sb.append("|| conditionsWithSingleValue.size() > 0"); //$NON-NLS-1$
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        OutputUtilities.javaIndent(sb, 2);
        sb.append("|| conditionsWithListValue.size() > 0"); //$NON-NLS-1$
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        OutputUtilities.javaIndent(sb, 2);
        sb.append("|| conditionsWithBetweenValue.size() > 0;"); //$NON-NLS-1$
        method.addBodyLine(sb.toString());
        answer.addMethod(method);

        // now add the methods for simplifying the individual field set methods
        method = new Method();
        method.setVisibility(JavaVisibility.PRIVATE);
        method.setName("addSingleValueCondition"); //$NON-NLS-1$
        method.addParameter(new Parameter(FullyQualifiedJavaType
                .getStringInstance(), "condition")); //$NON-NLS-1$
        method.addParameter(new Parameter(FullyQualifiedJavaType
                .getObjectInstance(), "value")); //$NON-NLS-1$
        method.addParameter(new Parameter(FullyQualifiedJavaType
                .getStringInstance(), "property")); //$NON-NLS-1$
        method.addBodyLine("if (value == null) {"); //$NON-NLS-1$
        method
                .addBodyLine("throw new RuntimeException(\"Value for \" + property + \" cannot be null\");"); //$NON-NLS-1$
        method.addBodyLine("}"); //$NON-NLS-1$
        method
                .addBodyLine("Map<String, Object> map = new HashMap<String, Object>();"); //$NON-NLS-1$
        method.addBodyLine("map.put(\"condition\", condition);"); //$NON-NLS-1$
        method.addBodyLine("map.put(\"value\", value);"); //$NON-NLS-1$
        method.addBodyLine("conditionsWithSingleValue.add(map);"); //$NON-NLS-1$
        answer.addMethod(method);

        FullyQualifiedJavaType listOfObjects = FullyQualifiedJavaType
                .getNewListInstance();
        listOfObjects.addTypeArgument(new JavaWildcardType(
                "java.lang.Object", true)); //$NON-NLS-1$

        method = new Method();
        method.setVisibility(JavaVisibility.PRIVATE);
        method.setName("addListValueCondition"); //$NON-NLS-1$
        method.addParameter(new Parameter(FullyQualifiedJavaType
                .getStringInstance(), "condition")); //$NON-NLS-1$
        method.addParameter(new Parameter(listOfObjects, "values")); //$NON-NLS-1$
        method.addParameter(new Parameter(FullyQualifiedJavaType
                .getStringInstance(), "property")); //$NON-NLS-1$
        method.addBodyLine("if (values == null || values.size() == 0) {"); //$NON-NLS-1$
        method
                .addBodyLine("throw new RuntimeException(\"Value list for \" + property + \" cannot be null or empty\");"); //$NON-NLS-1$
        method.addBodyLine("}"); //$NON-NLS-1$
        method
                .addBodyLine("Map<String, Object> map = new HashMap<String, Object>();"); //$NON-NLS-1$
        method.addBodyLine("map.put(\"condition\", condition);"); //$NON-NLS-1$
        method.addBodyLine("map.put(\"values\", values);"); //$NON-NLS-1$
        method.addBodyLine("conditionsWithListValue.add(map);"); //$NON-NLS-1$
        answer.addMethod(method);

        method = new Method();
        method.setVisibility(JavaVisibility.PRIVATE);
        method.setName("addBetweenCondition"); //$NON-NLS-1$
        method.addParameter(new Parameter(FullyQualifiedJavaType
                .getStringInstance(), "condition")); //$NON-NLS-1$
        method.addParameter(new Parameter(FullyQualifiedJavaType
                .getObjectInstance(), "value1")); //$NON-NLS-1$
        method.addParameter(new Parameter(FullyQualifiedJavaType
                .getObjectInstance(), "value2")); //$NON-NLS-1$
        method.addParameter(new Parameter(FullyQualifiedJavaType
                .getStringInstance(), "property")); //$NON-NLS-1$
        method.addBodyLine("if (value1 == null || value2 == null) {"); //$NON-NLS-1$
        method
                .addBodyLine("throw new RuntimeException(\"Between values for \" + property + \" cannot be null\");"); //$NON-NLS-1$
        method.addBodyLine("}"); //$NON-NLS-1$
        method.addBodyLine("List<Object> list = new ArrayList<Object>();"); //$NON-NLS-1$
        method.addBodyLine("list.add(value1);"); //$NON-NLS-1$
        method.addBodyLine("list.add(value2);"); //$NON-NLS-1$
        method
                .addBodyLine("Map<String, Object> map = new HashMap<String, Object>();"); //$NON-NLS-1$
        method.addBodyLine("map.put(\"condition\", condition);"); //$NON-NLS-1$
        method.addBodyLine("map.put(\"values\", list);"); //$NON-NLS-1$
        method.addBodyLine("conditionsWithBetweenValue.add(map);"); //$NON-NLS-1$
        answer.addMethod(method);

        FullyQualifiedJavaType listOfDates = FullyQualifiedJavaType
                .getNewListInstance();
        listOfDates.addTypeArgument(FullyQualifiedJavaType.getDateInstance());

        if (columnDefinitions.hasJDBCDateColumns()) {
            topLevelClass.addImportedType(FullyQualifiedJavaType
                    .getDateInstance());
            topLevelClass.addImportedType(FullyQualifiedJavaType
                    .getNewIteratorInstance());
            method = new Method();
            method.setVisibility(JavaVisibility.PRIVATE);
            method.setName("addSingleDateValueCondition"); //$NON-NLS-1$
            method.addParameter(new Parameter(FullyQualifiedJavaType
                    .getStringInstance(), "condition")); //$NON-NLS-1$
            method.addParameter(new Parameter(FullyQualifiedJavaType
                    .getDateInstance(), "value")); //$NON-NLS-1$
            method.addParameter(new Parameter(FullyQualifiedJavaType
                    .getStringInstance(), "property")); //$NON-NLS-1$
            method
                    .addBodyLine("addSingleValueCondition(condition, new java.sql.Date(value.getTime()), property);"); //$NON-NLS-1$
            answer.addMethod(method);

            method = new Method();
            method.setVisibility(JavaVisibility.PRIVATE);
            method.setName("addDateListValueCondition"); //$NON-NLS-1$
            method.addParameter(new Parameter(FullyQualifiedJavaType
                    .getStringInstance(), "condition")); //$NON-NLS-1$
            method.addParameter(new Parameter(listOfDates, "values")); //$NON-NLS-1$
            method.addParameter(new Parameter(FullyQualifiedJavaType
                    .getStringInstance(), "property")); //$NON-NLS-1$
            method.addBodyLine("if (values == null || values.size() == 0) {"); //$NON-NLS-1$
            method
                    .addBodyLine("throw new RuntimeException(\"Value list for \" + property + \" cannot be null or empty\");"); //$NON-NLS-1$
            method.addBodyLine("}"); //$NON-NLS-1$
            method
                    .addBodyLine("List<java.sql.Date> dateList = new ArrayList<java.sql.Date>();"); //$NON-NLS-1$
            method.addBodyLine("Iterator<Date> iter = values.iterator();"); //$NON-NLS-1$
            method.addBodyLine("while (iter.hasNext()) {"); //$NON-NLS-1$
            method
                    .addBodyLine("dateList.add(new java.sql.Date(iter.next().getTime()));"); //$NON-NLS-1$
            method.addBodyLine("}"); //$NON-NLS-1$
            method
                    .addBodyLine("addListValueCondition(condition, dateList, property);"); //$NON-NLS-1$
            answer.addMethod(method);

            method = new Method();
            method.setVisibility(JavaVisibility.PRIVATE);
            method.setName("addDateBetweenCondition"); //$NON-NLS-1$
            method.addParameter(new Parameter(FullyQualifiedJavaType
                    .getStringInstance(), "condition")); //$NON-NLS-1$
            method.addParameter(new Parameter(FullyQualifiedJavaType
                    .getDateInstance(), "value1")); //$NON-NLS-1$
            method.addParameter(new Parameter(FullyQualifiedJavaType
                    .getDateInstance(), "value2")); //$NON-NLS-1$
            method.addParameter(new Parameter(FullyQualifiedJavaType
                    .getStringInstance(), "property")); //$NON-NLS-1$
            method.addBodyLine("if (value1 == null || value2 == null) {"); //$NON-NLS-1$
            method
                    .addBodyLine("throw new RuntimeException(\"Between values for \" + property + \" cannot be null\");"); //$NON-NLS-1$
            method.addBodyLine("}"); //$NON-NLS-1$
            method
                    .addBodyLine("addBetweenCondition(condition, new java.sql.Date(value1.getTime()), new java.sql.Date(value2.getTime()), property);"); //$NON-NLS-1$
            answer.addMethod(method);
        }

        if (columnDefinitions.hasJDBCTimeColumns()) {
            topLevelClass.addImportedType(FullyQualifiedJavaType
                    .getDateInstance());
            topLevelClass.addImportedType(FullyQualifiedJavaType
                    .getNewIteratorInstance());
            method = new Method();
            method.setVisibility(JavaVisibility.PRIVATE);
            method.setName("addSingleTimeValueCondition"); //$NON-NLS-1$
            method.addParameter(new Parameter(FullyQualifiedJavaType
                    .getStringInstance(), "condition")); //$NON-NLS-1$
            method.addParameter(new Parameter(FullyQualifiedJavaType
                    .getDateInstance(), "value")); //$NON-NLS-1$
            method.addParameter(new Parameter(FullyQualifiedJavaType
                    .getStringInstance(), "property")); //$NON-NLS-1$
            method
                    .addBodyLine("addSingleValueCondition(condition, new java.sql.Time(value.getTime()), property);"); //$NON-NLS-1$
            answer.addMethod(method);

            method = new Method();
            method.setVisibility(JavaVisibility.PRIVATE);
            method.setName("addTimeListValueCondition"); //$NON-NLS-1$
            method.addParameter(new Parameter(FullyQualifiedJavaType
                    .getStringInstance(), "condition")); //$NON-NLS-1$
            method.addParameter(new Parameter(listOfDates, "values")); //$NON-NLS-1$
            method.addParameter(new Parameter(FullyQualifiedJavaType
                    .getStringInstance(), "property")); //$NON-NLS-1$
            method.addBodyLine("if (values == null || values.size() == 0) {"); //$NON-NLS-1$
            method
                    .addBodyLine("throw new RuntimeException(\"Value list for \" + property + \" cannot be null or empty\");"); //$NON-NLS-1$
            method.addBodyLine("}"); //$NON-NLS-1$
            method
                    .addBodyLine("List<java.sql.Time> dateList = new ArrayList<java.sql.Time>();"); //$NON-NLS-1$
            method.addBodyLine("Iterator<Date> iter = values.iterator();"); //$NON-NLS-1$
            method.addBodyLine("while (iter.hasNext()) {"); //$NON-NLS-1$
            method
                    .addBodyLine("dateList.add(new java.sql.Time(iter.next().getTime()));"); //$NON-NLS-1$
            method.addBodyLine("}"); //$NON-NLS-1$
            method
                    .addBodyLine("addListValueCondition(condition, dateList, property);"); //$NON-NLS-1$
            answer.addMethod(method);

            method = new Method();
            method.setVisibility(JavaVisibility.PRIVATE);
            method.setName("addTimeBetweenCondition"); //$NON-NLS-1$
            method.addParameter(new Parameter(FullyQualifiedJavaType
                    .getStringInstance(), "condition")); //$NON-NLS-1$
            method.addParameter(new Parameter(FullyQualifiedJavaType
                    .getDateInstance(), "value1")); //$NON-NLS-1$
            method.addParameter(new Parameter(FullyQualifiedJavaType
                    .getDateInstance(), "value2")); //$NON-NLS-1$
            method.addParameter(new Parameter(FullyQualifiedJavaType
                    .getStringInstance(), "property")); //$NON-NLS-1$
            method.addBodyLine("if (value1 == null || value2 == null) {"); //$NON-NLS-1$
            method
                    .addBodyLine("throw new RuntimeException(\"Between values for \" + property + \" cannot be null\");"); //$NON-NLS-1$
            method.addBodyLine("}"); //$NON-NLS-1$
            method
                    .addBodyLine("addBetweenCondition(condition, new java.sql.Time(value1.getTime()), new java.sql.Time(value2.getTime()), property);"); //$NON-NLS-1$
            answer.addMethod(method);
        }

        /*
         * now add the methods that determine if the "ands" are required - this
         * is too complex a calculation to be done inside the sql map. The
         * methods assume that the SQL map clause is layed out in this order:
         * 
         * conditionsWithouttValue
         * 
         * (possible first and)
         * 
         * conditionsWithSingleValue
         * 
         * (possible second and)
         * 
         * conditionsWithListValue
         * 
         * (possible third and)
         * 
         * conditionsWithBetweenValue
         */

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType
                .getBooleanPrimitiveInstance());
        method.setName("isFirstAndNeeded"); //$NON-NLS-1$
        method.addBodyLine("return conditionsWithoutValue.size() > 0"); //$NON-NLS-1$
        sb.setLength(0);
        OutputUtilities.javaIndent(sb, 2);
        sb.append("&& conditionsWithSingleValue.size() > 0;"); //$NON-NLS-1$
        method.addBodyLine(sb.toString());
        answer.addMethod(method);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType
                .getBooleanPrimitiveInstance());
        method.setName("isSecondAndNeeded"); //$NON-NLS-1$
        method.addBodyLine("return (conditionsWithoutValue.size() > 0"); //$NON-NLS-1$
        sb.setLength(0);
        OutputUtilities.javaIndent(sb, 2);
        sb.append("|| conditionsWithSingleValue.size() > 0)"); //$NON-NLS-1$
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        OutputUtilities.javaIndent(sb, 2);
        sb.append("&& conditionsWithListValue.size() > 0;"); //$NON-NLS-1$
        method.addBodyLine(sb.toString());
        answer.addMethod(method);

        method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType
                .getBooleanPrimitiveInstance());
        method.setName("isThirdAndNeeded"); //$NON-NLS-1$
        method.addBodyLine("return (conditionsWithoutValue.size() > 0"); //$NON-NLS-1$
        sb.setLength(0);
        OutputUtilities.javaIndent(sb, 2);
        sb.append("|| conditionsWithSingleValue.size() > 0"); //$NON-NLS-1$
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        OutputUtilities.javaIndent(sb, 2);
        sb.append("|| conditionsWithListValue.size() > 0)"); //$NON-NLS-1$
        method.addBodyLine(sb.toString());
        sb.setLength(0);
        OutputUtilities.javaIndent(sb, 2);
        sb.append("&& conditionsWithBetweenValue.size() > 0;"); //$NON-NLS-1$
        method.addBodyLine(sb.toString());
        answer.addMethod(method);

        Iterator iter = columnDefinitions.getAllColumns().iterator();
        while (iter.hasNext()) {
            ColumnDefinition cd = (ColumnDefinition) iter.next();

            if (cd.isBLOBColumn()) {
                continue;
            }

            topLevelClass.addImportedType(cd.getResolvedJavaType()
                    .getFullyQualifiedJavaType());

            // here we need to add the individual methods for setting the
            // conditions for a field
            answer.addMethod(getSetNullMethod(cd));
            answer.addMethod(getSetNotNullMethod(cd));
            answer.addMethod(getSetEqualMethod(cd));
            answer.addMethod(getSetNotEqualMethod(cd));
            answer.addMethod(getSetGreaterThanMethod(cd));
            answer.addMethod(getSetGreaterThenOrEqualMethod(cd));
            answer.addMethod(getSetLessThanMethod(cd));
            answer.addMethod(getSetLessThanOrEqualMethod(cd));

            if (cd.isCharacterColumn()) {
                answer.addMethod(getSetLikeMethod(cd));
                answer.addMethod(getSetNotLikeMethod(cd));
            }

            answer.addMethod(getSetInOrNotInMethod(cd, true));
            answer.addMethod(getSetInOrNotInMethod(cd, false));
            answer.addMethod(getSetBetweenOrNotBetweenMethod(cd, true));
            answer.addMethod(getSetBetweenOrNotBetweenMethod(cd, false));
        }

        return answer;
    }

    /**
     * 
     * @param cd
     * @param inMethod
     *            if true generates an "in" method, else generates a "not in"
     *            method
     * @return
     */
    protected Method getSetInOrNotInMethod(ColumnDefinition cd, boolean inMethod) {
        Method method = new Method();
        method.setVisibility(JavaVisibility.PUBLIC);
        FullyQualifiedJavaType type = FullyQualifiedJavaType
                .getNewListInstance();
        if (cd.getResolvedJavaType().getFullyQualifiedJavaType().isPrimitive()) {
            type.addTypeArgument(cd.getResolvedJavaType()
                    .getFullyQualifiedJavaType().getPrimitiveTypeWrapper());
        } else {
            type.addTypeArgument(cd.getResolvedJavaType()
                    .getFullyQualifiedJavaType());
        }
        method.addParameter(new Parameter(type, "values")); //$NON-NLS-1$
        StringBuffer sb = new StringBuffer();
        sb.append(cd.getJavaProperty());
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        sb.insert(0, "add"); //$NON-NLS-1$
        if (inMethod) {
            sb.append("InCondition"); //$NON-NLS-1$
        } else {
            sb.append("NotInCondition"); //$NON-NLS-1$
        }
        method.setName(sb.toString());
        sb.setLength(0);

        if (cd.isJDBCDateColumn()) {
            sb.append("addDateListValueCondition(\""); //$NON-NLS-1$
        } else if (cd.isJDBCTimeColumn()) {
            sb.append("addTimeListValueCondition(\""); //$NON-NLS-1$
        } else {
            sb.append("addListValueCondition(\""); //$NON-NLS-1$
        }

        sb.append(cd.getAliasedColumnName());
        if (inMethod) {
            sb.append(" in"); //$NON-NLS-1$
        } else {
            sb.append(" not in"); //$NON-NLS-1$
        }
        sb.append("\", values, \""); //$NON-NLS-1$
        sb.append(cd.getJavaProperty());
        sb.append("\");"); //$NON-NLS-1$
        method.addBodyLine(sb.toString());

        return method;
    }
}
