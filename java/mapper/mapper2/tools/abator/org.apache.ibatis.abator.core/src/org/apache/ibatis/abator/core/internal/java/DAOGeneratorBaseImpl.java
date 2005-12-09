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
package org.apache.ibatis.abator.core.internal.java;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.ibatis.abator.core.api.DAOGenerator;
import org.apache.ibatis.abator.core.api.GeneratedJavaFile;
import org.apache.ibatis.abator.core.api.JavaModelGenerator;
import org.apache.ibatis.abator.core.api.SqlMapGenerator;
import org.apache.ibatis.abator.core.config.TableConfiguration;
import org.apache.ibatis.abator.core.config.FullyQualifiedTable;
import org.apache.ibatis.abator.core.internal.db.ColumnDefinition;
import org.apache.ibatis.abator.core.internal.db.ColumnDefinitions;
import org.apache.ibatis.abator.core.internal.sqlmap.ExampleClause;
import org.apache.ibatis.abator.core.internal.types.ResolvedJavaType;
import org.apache.ibatis.abator.core.internal.util.JavadocUtil;
import org.apache.ibatis.abator.core.internal.util.JavaBeansUtil;
import org.apache.ibatis.abator.core.internal.util.StringUtility;

/**
 * This class generates DAO classes based on the values in the supplied
 * DAOGeneratorTemplate.
 * 
 * @author Jeff Butler
 */
public class DAOGeneratorBaseImpl implements DAOGenerator {

	private Map properties;

	private String targetPackage;

	private String targetProject;

	private Map tableStringMaps;

	private JavaModelGenerator javaModelGenerator;

	private SqlMapGenerator sqlMapGenerator;

	private DAOGeneratorTemplate daoGeneratorTemplate;

	/**
	 *  
	 */
	public DAOGeneratorBaseImpl(DAOGeneratorTemplate daoGeneratorTemplate) {
		super();
		tableStringMaps = new HashMap();
		this.daoGeneratorTemplate = daoGeneratorTemplate;
	}

	public void setProperties(Map properties) {
		this.properties = properties;
	}

	public void setTargetPackage(String targetPackage) {
		this.targetPackage = targetPackage;
	}

	public void setJavaModelGenerator(JavaModelGenerator javaModelGenerator) {
		this.javaModelGenerator = javaModelGenerator;
	}

	public void setSqlMapGenerator(SqlMapGenerator sqlMapGenerator) {
		this.sqlMapGenerator = sqlMapGenerator;
	}
	
	private Map getTableStringMap(FullyQualifiedTable table) {
	    Map map = (Map) tableStringMaps.get(table);
	    if (map == null) {
	        map = new HashMap();
	        tableStringMaps.put(table, map);
	    }
	    
	    return map;
	}

	private String getDAOImplementationShortClassName(FullyQualifiedTable table) {
		String key = "getDAOImplementationShortClassName"; //$NON-NLS-1$
		String s;

		Map map = getTableStringMap(table);
		s = (String) map.get(key);
		if (s == null) {
			StringBuffer sb = new StringBuffer(table.getDomainObjectName());
			sb.append("DAOImpl"); //$NON-NLS-1$
			if (Character.isLowerCase(sb.charAt(0))) {
				sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
			}

			s = sb.toString();
			map.put(key, s);
		}

		return s;
	}

	private String getDAOInterfaceShortClassName(FullyQualifiedTable table) {
		String key = "getDAOInterfaceShortClassName"; //$NON-NLS-1$
		String s;

		Map map = getTableStringMap(table);
		s = (String) map.get(key);
		if (s == null) {
			StringBuffer sb = new StringBuffer(table.getDomainObjectName());
			sb.append("DAO"); //$NON-NLS-1$
			if (Character.isLowerCase(sb.charAt(0))) {
				sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
			}

			s = sb.toString();
			map.put(key, s);
		}

		return s;
	}

	private String getPackageStatement(FullyQualifiedTable table) {
		String key = "getPackageStatement"; //$NON-NLS-1$
		String s;

		Map map = getTableStringMap(table);
		s = (String) map.get(key);
		if (s == null) {
			StringBuffer sb = new StringBuffer();
			sb.append("package ");
			sb.append(getDAOPackage(table));

			s = sb.toString();
			map.put(key, s);
		}

		return s;
	}

	private String getDAOPackage(FullyQualifiedTable table) {
		String key = "getDAOPackage"; //$NON-NLS-1$
		String s;

		Map map = getTableStringMap(table);
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
	
	private void generateGetExampleParmsMethod(FullyQualifiedTable table, ColumnDefinitions cds, GeneratedJavaFile answer) {

		answer.addImportedType("java.util.Map"); //$NON-NLS-1$
		answer.addImportedType("java.util.HashMap"); //$NON-NLS-1$
		answer.addImportedType(javaModelGenerator.getExampleFullyQualifiedClassName(table));

		StringBuffer method = new StringBuffer();
		method.append(JavadocUtil.getMethodComment(table));
		method.append("private Map getExampleParms("); //$NON-NLS-1$
		method.append(javaModelGenerator.getExampleShortClassName(table));
		method.append(" example) {\n"); //$NON-NLS-1$

		method.append("Map parms = new HashMap();\n"); //$NON-NLS-1$

		Iterator iter = cds.getAllColumns().iterator();
		while (iter.hasNext()) {
			ColumnDefinition cd = (ColumnDefinition) iter.next();

			if (cd.isBLOBColumn()) {
				continue;
			}

			method.append("\nswitch (example."); //$NON-NLS-1$
			String property = cd.getJavaProperty() + "_Indicator"; //$NON-NLS-1$
			method.append(JavaBeansUtil.getGetterMethodName(property));
			method.append("()) {\n"); //$NON-NLS-1$

			Iterator clauseIterator = ExampleClause.getAllExampleClauses();
			while (clauseIterator.hasNext()) {
				ExampleClause clause = (ExampleClause) clauseIterator.next();

				if (clause.isCharacterOnly() && !cd.isCharacterColumn()) {
					continue;
				}

				method.append("case "); //$NON-NLS-1$
				method.append(javaModelGenerator.getExampleShortClassName(table));
				method.append('.');
				method.append(clause.getExamplePropertyName());
				method.append(":\n"); //$NON-NLS-1$
				method.append("if (example.isCombineTypeOr()) {\n"); //$NON-NLS-1$
				method.append("parms.put(\""); //$NON-NLS-1$
				method.append(clause.getSelectorOrProperty(cd));
				method.append("\", \"Y\");\n"); //$NON-NLS-1$
				method.append("} else {\n"); //$NON-NLS-1$
				method.append("parms.put(\""); //$NON-NLS-1$
				method.append(clause.getSelectorAndProperty(cd));
				method.append("\", \"Y\");\n"); //$NON-NLS-1$
				method.append("}\n"); //$NON-NLS-1$

				if (clause.isPropertyInMapRequired()) {
					String exampleProperty = cd.getJavaProperty();
					method.append("parms.put(\""); //$NON-NLS-1$
					method.append(exampleProperty);
					method.append("\", example."); //$NON-NLS-1$
					method.append(JavaBeansUtil
							.getGetterMethodName(exampleProperty));
					method.append("());\n"); //$NON-NLS-1$
				}
				method.append("break;\n\n"); //$NON-NLS-1$
			}

			method.append("}\n"); //$NON-NLS-1$
		}

		method.append("\nreturn parms;\n}\n"); //$NON-NLS-1$
		
		answer.addMethod(method.toString());
	}
	
	public GeneratedJavaFile getDAOImplementation(
			ColumnDefinitions columnDefinitions,
			TableConfiguration tableConfiguration) {
	    FullyQualifiedTable table = tableConfiguration.getTable();
	    
		GeneratedJavaFile answer = new GeneratedJavaFile();
		
		answer.setFileName(getDAOImplementationShortClassName(table) + ".java");
		answer.setJavaInterface(false);
		answer.setJavaName(getDAOImplementationShortClassName(table));
		answer.setPackageStatement(getPackageStatement(table).toString());
		if (StringUtility.stringHasValue(daoGeneratorTemplate.getSuperClass())) {
			answer.setSuperClass(daoGeneratorTemplate.getSuperClass());
		}
		
		answer.addSuperInterface(getDAOInterfaceShortClassName(table));
		
		answer.setTargetPackage(getDAOPackage(table));
		answer.setTargetProject(targetProject);
		
		answer.getImportedTypes().addAll(daoGeneratorTemplate.getImplementationImports());

		StringBuffer buffer = new StringBuffer();
		Iterator iter = daoGeneratorTemplate.getFields().iterator();
		while (iter.hasNext()) {
			buffer.setLength(0);
			buffer.append(JavadocUtil.getFieldComment(table));
			buffer.append(iter.next());
			answer.addField(buffer.toString());
		}

		// add constructor
		buffer.setLength(0);
		buffer.append(JavadocUtil.getMethodComment(table));
		buffer.append(daoGeneratorTemplate
				.getConstructor(getDAOImplementationShortClassName(table)));
		answer.addMethod(buffer.toString());

		iter = daoGeneratorTemplate.getMethods().iterator();
		while (iter.hasNext()) {
			buffer.setLength(0);
			buffer.append(JavadocUtil.getMethodComment(table));
			buffer.append(iter.next());
			answer.addMethod(buffer.toString());
		}

		if (columnDefinitions.generateDeleteByPrimaryKey(tableConfiguration)) {
			answer.addImportedType(javaModelGenerator
					.getPrimaryKeyFullyQualifiedClassName(table));
			
			buffer.setLength(0);
			buffer.append(JavadocUtil.getMethodComment(table));
			buffer.append("public int deleteByPrimaryKey("); //$NON-NLS-1$
			buffer.append(javaModelGenerator.getPrimaryKeyShortClassName(table));
			buffer.append(" key)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				buffer.append(" throws "); //$NON-NLS-1$
				buffer.append(daoGeneratorTemplate.getCheckedExceptions());
			}

			buffer.append(" { \n"); //$NON-NLS-1$

			buffer.append("int rows = "); //$NON-NLS-1$
			buffer.append(daoGeneratorTemplate.getDeleteMethod());
			buffer.append("(\""); //$NON-NLS-1$
			buffer.append(sqlMapGenerator.getSqlMapNamespace(table));
			buffer.append('.');
			buffer.append(sqlMapGenerator.getDeleteByPrimaryKeyStatementId());
			buffer.append("\", key);\n"); //$NON-NLS-1$

			buffer.append("return rows;\n"); //$NON-NLS-1$

			buffer.append("}\n"); //$NON-NLS-1$
			
			answer.addMethod(buffer.toString());
		}

		if (columnDefinitions.generateDeleteByExample(tableConfiguration)) {
			answer.addImportedType(javaModelGenerator
					.getExampleFullyQualifiedClassName(table));
			
			buffer.setLength(0);
			buffer.append(JavadocUtil.getMethodComment(table));
			buffer.append("public int deleteByExample("); //$NON-NLS-1$
			buffer.append(javaModelGenerator.getExampleShortClassName(table));
			buffer.append(" example)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				buffer.append(" throws "); //$NON-NLS-1$
				buffer.append(daoGeneratorTemplate.getCheckedExceptions());
			}

			buffer.append(" { \n"); //$NON-NLS-1$

			buffer.append("int rows = "); //$NON-NLS-1$
			buffer.append(daoGeneratorTemplate.getDeleteMethod());
			buffer.append("(\""); //$NON-NLS-1$
			buffer.append(sqlMapGenerator.getSqlMapNamespace(table));
			buffer.append('.');
			buffer.append(sqlMapGenerator.getDeleteByExampleStatementId());
			buffer.append("\", getExampleParms(example));\n"); //$NON-NLS-1$

			buffer.append("return rows;\n"); //$NON-NLS-1$

			buffer.append("}\n"); //$NON-NLS-1$
			answer.addMethod(buffer.toString());
		}

		if (columnDefinitions.generateInsert(tableConfiguration)) {

			String returnType;
			if (tableConfiguration.getGeneratedKey().isConfigured()) {
				ColumnDefinition cd = columnDefinitions.getColumn(tableConfiguration.getGeneratedKey()
						.getColumn());
				ResolvedJavaType rjt = cd.getResolvedJavaType();

				returnType = rjt.getShortName();
				if (rjt.isExplicitlyImported()) {
					answer.addImportedType(rjt.getFullyQualifiedName());
				}
			} else {
				returnType = "void"; //$NON-NLS-1$
			}

			buffer.setLength(0);
			buffer.append(JavadocUtil.getMethodComment(table));
			buffer.append("public "); //$NON-NLS-1$
			buffer.append(returnType);
			buffer.append(" insert("); //$NON-NLS-1$
			
			if (columnDefinitions.generateRecordWithBLOBsExtendingPrimaryKey()
					|| columnDefinitions.generateRecordWithBLOBsExtendingRecord()) {
				buffer.append(javaModelGenerator.getRecordWithBLOBsShortClassName(table));
				answer.addImportedType(javaModelGenerator
						.getRecordWithBLOBsFullyQualifiedClassName(table));
			} else if (columnDefinitions.generateRecordExtendingNothing()
					|| columnDefinitions.generateRecordExtendingPrimaryKey()) {
				buffer.append(javaModelGenerator.getRecordShortClassName(table));
				answer.addImportedType(javaModelGenerator
						.getRecordFullyQualifiedClassName(table));
			} else {
				buffer
						.append(javaModelGenerator
								.getPrimaryKeyShortClassName(table));
				answer.addImportedType(javaModelGenerator
						.getPrimaryKeyFullyQualifiedClassName(table));
			}
			buffer.append(" record)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				buffer.append(" throws "); //$NON-NLS-1$
				buffer.append(daoGeneratorTemplate.getCheckedExceptions());
			}

			buffer.append(" { \n"); //$NON-NLS-1$

			if (!"void".equals(returnType)) {
			    buffer.append("Object newKey = "); //$NON-NLS-1$
			}
			
			buffer.append(daoGeneratorTemplate.getInsertMethod());
			buffer.append("(\""); //$NON-NLS-1$
			buffer.append(sqlMapGenerator.getSqlMapNamespace(table));
			buffer.append('.');
			buffer.append(sqlMapGenerator.getInsertStatementId());
			buffer.append("\", record);\n"); //$NON-NLS-1$

			if ("Object".equals(returnType)) { //$NON-NLS-1$
				buffer.append("return newKey;\n"); //$NON-NLS-1$
			} else if (!"void".equals(returnType)) { //$NON-NLS-1$
				buffer.append("return ("); //$NON-NLS-1$
				buffer.append(returnType);
				buffer.append(") newKey;\n"); //$NON-NLS-1$
			}

			buffer.append("}\n"); //$NON-NLS-1$
			answer.addMethod(buffer.toString());
		}

		if (columnDefinitions.generateSelectByPrimaryKey(tableConfiguration)) {
			answer.addImportedType(javaModelGenerator
					.getPrimaryKeyFullyQualifiedClassName(table));
			String shortClassName;
			if (columnDefinitions.generateRecordWithBLOBsExtendingPrimaryKey()
					|| columnDefinitions.generateRecordWithBLOBsExtendingRecord()) {
				shortClassName = javaModelGenerator.getRecordWithBLOBsShortClassName(table);
				answer.addImportedType(javaModelGenerator
						.getRecordWithBLOBsFullyQualifiedClassName(table));
				
			} else {
				shortClassName = javaModelGenerator.getRecordShortClassName(table);
				answer.addImportedType(javaModelGenerator
						.getRecordFullyQualifiedClassName(table));
			}
			
			buffer.setLength(0);
			buffer.append(JavadocUtil.getMethodComment(table));
			buffer.append("public "); //$NON-NLS-1$
			buffer.append(shortClassName);
			buffer.append(" selectByPrimaryKey("); //$NON-NLS-1$
			buffer.append(javaModelGenerator.getPrimaryKeyShortClassName(table));
			buffer.append(" key)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				buffer.append(" throws "); //$NON-NLS-1$
				buffer.append(daoGeneratorTemplate.getCheckedExceptions());
			}

			buffer.append(" { \n"); //$NON-NLS-1$

			buffer.append(shortClassName);
			buffer.append(" record = ("); //$NON-NLS-1$
			buffer.append(shortClassName);
			buffer.append(") "); //$NON-NLS-1$
			buffer.append(daoGeneratorTemplate.getQueryForObjectMethod());
			buffer.append("(\""); //$NON-NLS-1$
			buffer.append(sqlMapGenerator.getSqlMapNamespace(table));
			buffer.append('.');
			buffer.append(sqlMapGenerator.getSelectByPrimaryKeyStatementId());
			buffer.append("\", key);\n"); //$NON-NLS-1$

			buffer.append("return record;\n"); //$NON-NLS-1$

			buffer.append("}\n"); //$NON-NLS-1$
			answer.addMethod(buffer.toString());
		}

		if (columnDefinitions.generateSelectByExample(tableConfiguration)) {
			answer.addImportedType("java.util.List"); //$NON-NLS-1$
			answer.addImportedType("java.util.Map"); //$NON-NLS-1$
			answer.addImportedType(javaModelGenerator
					.getExampleFullyQualifiedClassName(table));

			buffer.setLength(0);
			buffer.append(JavadocUtil.getMethodComment(table));
			buffer.append("public List selectByExample("); //$NON-NLS-1$
			buffer.append(javaModelGenerator.getExampleShortClassName(table));
			buffer.append(" example, String orderByClause)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				buffer.append(" throws "); //$NON-NLS-1$
				buffer.append(daoGeneratorTemplate.getCheckedExceptions());
			}

			buffer.append(" { \n"); //$NON-NLS-1$

			buffer.append("Map parms = getExampleParms(example);\n"); //$NON-NLS-1$

			buffer.append('\n'); //$NON-NLS-1$
			buffer.append("if (orderByClause != null) {\n"); //$NON-NLS-1$
			buffer
					.append("parms.put(\"ABATOR_ORDER_BY_CLAUSE\", orderByClause);\n"); //$NON-NLS-1$
			buffer.append("}\n"); //$NON-NLS-1$

			buffer.append('\n');
			buffer.append("List list = "); //$NON-NLS-1$
			buffer.append(daoGeneratorTemplate.getQueryForListMethod());
			buffer.append("(\""); //$NON-NLS-1$
			buffer.append(sqlMapGenerator.getSqlMapNamespace(table));
			buffer.append('.');
			buffer.append(sqlMapGenerator.getSelectByExampleStatementId());
			buffer.append("\", parms);\n"); //$NON-NLS-1$

			buffer.append("return list;\n"); //$NON-NLS-1$
			buffer.append("}\n"); //$NON-NLS-1$
			answer.addMethod(buffer.toString());

			buffer.setLength(0);
			buffer.append(JavadocUtil.getMethodComment(table));
			buffer.append("public List selectByExample("); //$NON-NLS-1$
			buffer.append(javaModelGenerator.getExampleShortClassName(table));
			buffer.append(" example)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				buffer.append(" throws "); //$NON-NLS-1$
				buffer.append(daoGeneratorTemplate.getCheckedExceptions());
			}
			buffer.append(" { \n"); //$NON-NLS-1$
			buffer.append("return selectByExample(example, null);\n"); //$NON-NLS-1$
			buffer.append("}\n"); //$NON-NLS-1$
			answer.addMethod(buffer.toString());
		}

		if (columnDefinitions.generateSelectByExampleWithBLOBs(tableConfiguration)) {
			answer.addImportedType("java.util.List"); //$NON-NLS-1$
			answer.addImportedType("java.util.Map"); //$NON-NLS-1$
			answer.addImportedType(javaModelGenerator
					.getExampleFullyQualifiedClassName(table));

			buffer.setLength(0);
			buffer.append(JavadocUtil.getMethodComment(table));
			buffer.append("public List selectByExampleWithBLOBs("); //$NON-NLS-1$
			buffer.append(javaModelGenerator.getExampleShortClassName(table));
			buffer.append(" example, String orderByClause)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				buffer.append(" throws "); //$NON-NLS-1$
				buffer.append(daoGeneratorTemplate.getCheckedExceptions());
			}

			buffer.append(" { \n"); //$NON-NLS-1$

			buffer.append("Map parms = getExampleParms(example);\n"); //$NON-NLS-1$

			buffer.append('\n'); //$NON-NLS-1$
			buffer.append("if (orderByClause != null) {\n"); //$NON-NLS-1$
			buffer
					.append("parms.put(\"ABATOR_ORDER_BY_CLAUSE\", orderByClause);\n"); //$NON-NLS-1$
			buffer.append("}\n"); //$NON-NLS-1$

			buffer.append('\n');
			buffer.append("List list = "); //$NON-NLS-1$
			buffer.append(daoGeneratorTemplate.getQueryForListMethod());
			buffer.append("(\""); //$NON-NLS-1$
			buffer.append(sqlMapGenerator.getSqlMapNamespace(table));
			buffer.append('.');
			buffer.append(sqlMapGenerator.getSelectByExampleWithBLOBsStatementId());
			buffer.append("\", parms);\n"); //$NON-NLS-1$

			buffer.append("return list;\n"); //$NON-NLS-1$
			buffer.append("}\n"); //$NON-NLS-1$
			answer.addMethod(buffer.toString());

			buffer.setLength(0);
			buffer.append(JavadocUtil.getMethodComment(table));
			buffer.append("public List selectByExampleWithBLOBs("); //$NON-NLS-1$
			buffer.append(javaModelGenerator.getExampleShortClassName(table));
			buffer.append(" example)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				buffer.append(" throws "); //$NON-NLS-1$
				buffer.append(daoGeneratorTemplate.getCheckedExceptions());
			}
			buffer.append(" { \n"); //$NON-NLS-1$
			buffer.append("return selectByExampleWithBLOBs(example, null);\n"); //$NON-NLS-1$
			buffer.append("}\n"); //$NON-NLS-1$
			answer.addMethod(buffer.toString());
		}

		if (columnDefinitions.generateUpdateByPrimaryKeyWithBLOBs(tableConfiguration)) {
			answer.addImportedType(javaModelGenerator
					.getRecordWithBLOBsFullyQualifiedClassName(table));
			
			buffer.setLength(0);
			buffer.append(JavadocUtil.getMethodComment(table));
			buffer.append("public int updateByPrimaryKey("); //$NON-NLS-1$
			buffer.append(javaModelGenerator.getRecordWithBLOBsShortClassName(table));
			buffer.append(" record)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				buffer.append(" throws "); //$NON-NLS-1$
				buffer.append(daoGeneratorTemplate.getCheckedExceptions());
			}

			buffer.append(" { \n"); //$NON-NLS-1$

			buffer.append("int rows = "); //$NON-NLS-1$
			buffer.append(daoGeneratorTemplate.getUpdateMethod());
			buffer.append("(\""); //$NON-NLS-1$
			buffer.append(sqlMapGenerator.getSqlMapNamespace(table));
			buffer.append('.');
			buffer.append(sqlMapGenerator.getUpdateByPrimaryKeyWithBLOBsStatementId());
			buffer.append("\", record);\n"); //$NON-NLS-1$

			buffer.append("return rows;\n"); //$NON-NLS-1$

			buffer.append("}\n"); //$NON-NLS-1$
			answer.addMethod(buffer.toString());
		}

		if (columnDefinitions.generateUpdateByPrimaryKey(tableConfiguration)) {
			answer.addImportedType(javaModelGenerator
					.getRecordFullyQualifiedClassName(table));
			
			buffer.setLength(0);
			buffer.append(JavadocUtil.getMethodComment(table));
			buffer.append("public int updateByPrimaryKey("); //$NON-NLS-1$
			buffer.append(javaModelGenerator.getRecordShortClassName(table));
			buffer.append(" record)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				buffer.append(" throws "); //$NON-NLS-1$
				buffer.append(daoGeneratorTemplate.getCheckedExceptions());
			}

			buffer.append(" { \n"); //$NON-NLS-1$

			buffer.append("int rows = "); //$NON-NLS-1$
			buffer.append(daoGeneratorTemplate.getUpdateMethod());
			buffer.append("(\""); //$NON-NLS-1$
			buffer.append(sqlMapGenerator.getSqlMapNamespace(table));
			buffer.append('.');
			buffer.append(sqlMapGenerator.getUpdateByPrimaryKeyStatementId());
			buffer.append("\", record);\n"); //$NON-NLS-1$

			buffer.append("return rows;\n"); //$NON-NLS-1$

			buffer.append("}\n"); //$NON-NLS-1$
			answer.addMethod(buffer.toString());
		}
		
		if (columnDefinitions.generateDeleteByExample(tableConfiguration)
				|| columnDefinitions.generateSelectByExample(tableConfiguration)) {
			generateGetExampleParmsMethod(table, columnDefinitions, answer);
		}

		return answer;
	}
	
	public GeneratedJavaFile getDAOInterface(
			ColumnDefinitions columnDefinitions,
			TableConfiguration tableConfiguration) {
	    FullyQualifiedTable table = tableConfiguration.getTable();
		GeneratedJavaFile answer = new GeneratedJavaFile();
		
		answer.setFileName(getDAOInterfaceShortClassName(table) + ".java");
		answer.setJavaInterface(true);
		answer.setJavaName(getDAOInterfaceShortClassName(table));
		answer.setPackageStatement(getPackageStatement(table).toString());
		
		answer.setTargetPackage(getDAOPackage(table));
		answer.setTargetProject(targetProject);

		answer.getImportedTypes().addAll(daoGeneratorTemplate.getInterfaceImports());

		StringBuffer method = new StringBuffer();
		if (columnDefinitions.generateDeleteByPrimaryKey(tableConfiguration)) {
			answer.addImportedType(javaModelGenerator.getPrimaryKeyFullyQualifiedClassName(table));
			
			method.setLength(0);
			method.append(JavadocUtil.getMethodComment(table));
			method.append("int deleteByPrimaryKey("); //$NON-NLS-1$
			method.append(javaModelGenerator.getPrimaryKeyShortClassName(table));
			method.append(" key)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				method.append(" throws "); //$NON-NLS-1$
				method.append(daoGeneratorTemplate.getCheckedExceptions());
			}

			method.append(";\n"); //$NON-NLS-1$
			
			answer.addMethod(method.toString());
		}

		if (columnDefinitions.generateDeleteByExample(tableConfiguration)) {
			answer.addImportedType(javaModelGenerator
					.getExampleFullyQualifiedClassName(table));

			method.setLength(0);
			method.append(JavadocUtil.getMethodComment(table));
			method.append("int deleteByExample("); //$NON-NLS-1$
			method.append(javaModelGenerator.getExampleShortClassName(table));
			method.append(" example)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				method.append(" throws "); //$NON-NLS-1$
				method.append(daoGeneratorTemplate.getCheckedExceptions());
			}

			method.append(";\n"); //$NON-NLS-1$
			answer.addMethod(method.toString());
		}

		if (columnDefinitions.generateInsert(tableConfiguration)) {
			String returnType;
			if (tableConfiguration.getGeneratedKey().isConfigured()) {
				ColumnDefinition cd = columnDefinitions.getColumn(tableConfiguration.getGeneratedKey()
						.getColumn());
				ResolvedJavaType rjt = cd.getResolvedJavaType();

				returnType = rjt.getShortName();
				if (rjt.isExplicitlyImported()) {
					answer.addImportedType(rjt.getFullyQualifiedName());
				}
			} else {
				returnType = "void"; //$NON-NLS-1$
			}

			method.setLength(0);
			method.append(JavadocUtil.getMethodComment(table));
			method.append(returnType);
			method.append(" insert("); //$NON-NLS-1$
			if (columnDefinitions.generateRecordWithBLOBsExtendingPrimaryKey()
					|| columnDefinitions.generateRecordWithBLOBsExtendingRecord()) {
				method.append(javaModelGenerator.getRecordWithBLOBsShortClassName(table));
				answer.addImportedType(javaModelGenerator
						.getRecordWithBLOBsFullyQualifiedClassName(table));
			} else if (columnDefinitions.generateRecordExtendingNothing()
					|| columnDefinitions.generateRecordExtendingPrimaryKey()) {
				method.append(javaModelGenerator.getRecordShortClassName(table));
				answer.addImportedType(javaModelGenerator
						.getRecordFullyQualifiedClassName(table));
			} else {
				method
						.append(javaModelGenerator
								.getPrimaryKeyShortClassName(table));
				answer.addImportedType(javaModelGenerator
						.getPrimaryKeyFullyQualifiedClassName(table));
			}
			
			method.append(" record)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				method.append(" throws "); //$NON-NLS-1$
				method.append(daoGeneratorTemplate.getCheckedExceptions());
			}

			method.append(";\n"); //$NON-NLS-1$
			answer.addMethod(method.toString());
		}

		if (columnDefinitions.generateSelectByPrimaryKey(tableConfiguration)) {
			answer.addImportedType(javaModelGenerator
					.getPrimaryKeyFullyQualifiedClassName(table));
			
			method.setLength(0);
			method.append(JavadocUtil.getMethodComment(table));
			
			if (columnDefinitions.generateRecordWithBLOBsExtendingPrimaryKey()
					|| columnDefinitions.generateRecordWithBLOBsExtendingRecord()) {
				method.append(javaModelGenerator.getRecordWithBLOBsShortClassName(table));
				answer.addImportedType(javaModelGenerator
						.getRecordWithBLOBsFullyQualifiedClassName(table));
				
			} else {
				method.append(javaModelGenerator.getRecordShortClassName(table));
				answer.addImportedType(javaModelGenerator
						.getRecordFullyQualifiedClassName(table));
			}
			
			method.append(" selectByPrimaryKey("); //$NON-NLS-1$
			method.append(javaModelGenerator.getPrimaryKeyShortClassName(table));
			method.append(" key)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				method.append(" throws "); //$NON-NLS-1$
				method.append(daoGeneratorTemplate.getCheckedExceptions());
			}

			method.append(";\n"); //$NON-NLS-1$
			answer.addMethod(method.toString());
		}

		if (columnDefinitions.generateSelectByExample(tableConfiguration)) {
			answer.addImportedType(javaModelGenerator
					.getExampleFullyQualifiedClassName(table));
			answer.addImportedType("java.util.List"); //$NON-NLS-1$

			method.setLength(0);
			method.append(JavadocUtil.getMethodComment(table));
			method.append("List selectByExample("); //$NON-NLS-1$
			method.append(javaModelGenerator.getExampleShortClassName(table));
			method.append(" example, String orderByClause)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				method.append(" throws "); //$NON-NLS-1$
				method.append(daoGeneratorTemplate.getCheckedExceptions());
			}

			method.append(";\n"); //$NON-NLS-1$
			answer.addMethod(method.toString());

			method.setLength(0);
			method.append(JavadocUtil.getMethodComment(table));
			method.append("List selectByExample("); //$NON-NLS-1$
			method.append(javaModelGenerator.getExampleShortClassName(table));
			method.append(" example)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				method.append(" throws "); //$NON-NLS-1$
				method.append(daoGeneratorTemplate.getCheckedExceptions());
			}

			method.append(";\n"); //$NON-NLS-1$
			answer.addMethod(method.toString());
		}

		if (columnDefinitions.generateSelectByExampleWithBLOBs(tableConfiguration)) {
			answer.addImportedType(javaModelGenerator
					.getExampleFullyQualifiedClassName(table));
			answer.addImportedType("java.util.List"); //$NON-NLS-1$

			method.setLength(0);
			method.append(JavadocUtil.getMethodComment(table));
			method.append("List selectByExampleWithBLOBs("); //$NON-NLS-1$
			method.append(javaModelGenerator.getExampleShortClassName(table));
			method.append(" example, String orderByClause)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				method.append(" throws "); //$NON-NLS-1$
				method.append(daoGeneratorTemplate.getCheckedExceptions());
			}

			method.append(";\n"); //$NON-NLS-1$
			answer.addMethod(method.toString());

			method.setLength(0);
			method.append(JavadocUtil.getMethodComment(table));
			method.append("List selectByExampleWithBLOBs("); //$NON-NLS-1$
			method.append(javaModelGenerator.getExampleShortClassName(table));
			method.append(" example)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				method.append(" throws "); //$NON-NLS-1$
				method.append(daoGeneratorTemplate.getCheckedExceptions());
			}

			method.append(";\n"); //$NON-NLS-1$
			answer.addMethod(method.toString());
		}
		
		if (columnDefinitions.generateUpdateByPrimaryKeyWithBLOBs(tableConfiguration)) {
			answer.addImportedType(javaModelGenerator
					.getRecordWithBLOBsFullyQualifiedClassName(table));

			method.setLength(0);
			method.append(JavadocUtil.getMethodComment(table));
			method.append("int updateByPrimaryKey("); //$NON-NLS-1$
			method.append(javaModelGenerator.getRecordWithBLOBsShortClassName(table));
			method.append(" record)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				method.append(" throws "); //$NON-NLS-1$
				method.append(daoGeneratorTemplate.getCheckedExceptions());
			}

			method.append(";\n"); //$NON-NLS-1$
			answer.addMethod(method.toString());
		}

		if (columnDefinitions.generateUpdateByPrimaryKey(tableConfiguration)) {
			answer.addImportedType(javaModelGenerator
					.getRecordFullyQualifiedClassName(table));

			method.setLength(0);
			method.append(JavadocUtil.getMethodComment(table));
			method.append("int updateByPrimaryKey("); //$NON-NLS-1$
			method.append(javaModelGenerator.getRecordShortClassName(table));
			method.append(" record)"); //$NON-NLS-1$
			if (StringUtility.stringHasValue(daoGeneratorTemplate
					.getCheckedExceptions())) {
				method.append(" throws "); //$NON-NLS-1$
				method.append(daoGeneratorTemplate.getCheckedExceptions());
			}

			method.append(";\n"); //$NON-NLS-1$
			answer.addMethod(method.toString());
		}

		return answer;
	}

    public void setTargetProject(String targetProject) {
        this.targetProject = targetProject;
    }
}
