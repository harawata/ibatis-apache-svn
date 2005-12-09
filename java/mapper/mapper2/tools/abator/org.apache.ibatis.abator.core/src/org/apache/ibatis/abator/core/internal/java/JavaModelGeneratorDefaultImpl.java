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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.ibatis.abator.core.api.GeneratedJavaFile;
import org.apache.ibatis.abator.core.api.JavaModelGenerator;
import org.apache.ibatis.abator.core.config.FullyQualifiedTable;
import org.apache.ibatis.abator.core.config.TableConfiguration;
import org.apache.ibatis.abator.core.internal.db.ColumnDefinition;
import org.apache.ibatis.abator.core.internal.db.ColumnDefinitions;
import org.apache.ibatis.abator.core.internal.sqlmap.ExampleClause;
import org.apache.ibatis.abator.core.internal.types.ResolvedJavaType;
import org.apache.ibatis.abator.core.internal.util.JavadocUtil;
import org.apache.ibatis.abator.core.internal.util.JavaBeansUtil;
import org.apache.ibatis.abator.core.internal.util.StringUtility;

public class JavaModelGeneratorDefaultImpl implements JavaModelGenerator {

	private Map properties;

	private String targetPackage;

	private String targetProject;

	private Map tableStringMaps;

	public JavaModelGeneratorDefaultImpl() {
		super();
		tableStringMaps = new HashMap();
	}

	public void setProperties(Map properties) {
		this.properties = properties;
	}

	public void setTargetPackage(String targetPackage) {
		this.targetPackage = targetPackage;
	}

	private StringBuffer generatePackageStatement(FullyQualifiedTable table) {
		StringBuffer sb = new StringBuffer();
		sb.append("package "); //$NON-NLS-1$
		sb.append(getJavaModelPackage(table));

		return sb;
	}

	private Map getTableStringMap(FullyQualifiedTable table) {
	    Map map = (Map) tableStringMaps.get(table);
	    if (map == null) {
	        map = new HashMap();
	        tableStringMaps.put(table, map);
	    }
	    
	    return map;
	}

	private void generateClassParts(FullyQualifiedTable table, Collection columnDefinitions,
			GeneratedJavaFile answer) {

		boolean trimStrings = "true".equalsIgnoreCase((String) properties //$NON-NLS-1$
				.get("trimStrings")); //$NON-NLS-1$
		
		StringBuffer buffer = new StringBuffer();

		Iterator iter = columnDefinitions.iterator();
		while (iter.hasNext()) {
			ColumnDefinition cd = (ColumnDefinition) iter.next();

			ResolvedJavaType rjt = cd.getResolvedJavaType();

			if (rjt.isExplicitlyImported()) {
				answer.addImportedType(rjt.getFullyQualifiedName());
			}

			String property = cd.getJavaProperty();

			buffer.setLength(0);
			buffer.append(JavadocUtil.getFieldComment(table, cd.getColumnName()));
			buffer.append("private "); //$NON-NLS-1$
			buffer.append(rjt.getShortName());
			buffer.append(" "); //$NON-NLS-1$
			buffer.append(property);
			buffer.append(";\n"); //$NON-NLS-1$
			answer.addField(buffer.toString());

			buffer.setLength(0);
			buffer.append(JavadocUtil.getGetterMethodComment(table, cd));
			buffer.append("public "); //$NON-NLS-1$
			buffer.append(rjt.getShortName());
			buffer.append(" "); //$NON-NLS-1$
			buffer.append(JavaBeansUtil.getGetterMethodName(property));
			buffer.append("() {\n return "); //$NON-NLS-1$
			buffer.append(property);
			buffer.append(";\n}"); //$NON-NLS-1$
			answer.addMethod(buffer.toString());

			buffer.setLength(0);
			buffer.append(JavadocUtil.getSetterMethodComment(table, cd));
			buffer.append("public void "); //$NON-NLS-1$
			buffer.append(JavaBeansUtil.getSetterMethodName(property));
			buffer.append("("); //$NON-NLS-1$
			buffer.append(rjt.getShortName());
			buffer.append(" "); //$NON-NLS-1$
			buffer.append(property);
			buffer.append(") {\n"); //$NON-NLS-1$
			if (trimStrings && cd.isCharacterColumn()) {
				buffer.append("if ("); //$NON-NLS-1$
				buffer.append(property);
				buffer.append(" != null) {\n"); //$NON-NLS-1$
				buffer.append(property);
				buffer.append(" = "); //$NON-NLS-1$
				buffer.append(property);
				buffer.append(".trim();\n"); //$NON-NLS-1$
				buffer.append("}\n"); //$NON-NLS-1$
			}
			buffer.append("this."); //$NON-NLS-1$
			buffer.append(property);
			buffer.append(" = "); //$NON-NLS-1$
			buffer.append(property);
			buffer.append(";\n}"); //$NON-NLS-1$
			answer.addMethod(buffer.toString());
		}
	}

	public String getExampleShortClassName(FullyQualifiedTable table) {
		String key = "getSelectByExampleShortClassName"; //$NON-NLS-1$
		String s;

		Map map = getTableStringMap(table);
		s = (String) map.get(key);
		if (s == null) {
			StringBuffer sb = new StringBuffer(table.getDomainObjectName());
			sb.append("Example"); //$NON-NLS-1$

			s = sb.toString();
			map.put(key, s);
		}

		return s;
	}

	public String getExampleFullyQualifiedClassName(FullyQualifiedTable table) {
		String key = "getSelectByExampleFullyQualifiedClassName"; //$NON-NLS-1$
		String s;

		Map map = getTableStringMap(table);
		s = (String) map.get(key);
		if (s == null) {
			StringBuffer sb = new StringBuffer();
			sb.append(getJavaModelPackage(table));
			sb.append("."); //$NON-NLS-1$
			sb.append(getExampleShortClassName(table));

			s = sb.toString();
			map.put(key, s);
		}

		return s;
	}

	public String getPrimaryKeyShortClassName(FullyQualifiedTable table) {
		String key = "getPrimaryKeyShortClassName"; //$NON-NLS-1$
		String s;

		Map map = getTableStringMap(table);
		s = (String) map.get(key);
		if (s == null) {
			StringBuffer sb = new StringBuffer(table.getDomainObjectName());
			sb.append("Key"); //$NON-NLS-1$

			s = sb.toString();
			map.put(key, s);
		}

		return s;
	}

	public String getPrimaryKeyFullyQualifiedClassName(FullyQualifiedTable table) {
		String key = "getPrimaryKeyFullyQualifiedClassName"; //$NON-NLS-1$
		String s;

		Map map = getTableStringMap(table);
		s = (String) map.get(key);
		if (s == null) {
			StringBuffer sb = new StringBuffer();
			sb.append(getJavaModelPackage(table));
			sb.append("."); //$NON-NLS-1$
			sb.append(getPrimaryKeyShortClassName(table));

			s = sb.toString();
			map.put(key, s);
		}

		return s;
	}

	public String getRecordShortClassName(FullyQualifiedTable table) {
		String key = "getRecordShortClassName"; //$NON-NLS-1$
		String s;

		Map map = getTableStringMap(table);
		s = (String) map.get(key);
		if (s == null) {
			s = table.getDomainObjectName();
			map.put(key, s);
		}

		return s;
	}

	public String getRecordWithBLOBsShortClassName(FullyQualifiedTable table) {
		String key = "getRecordWithBLOBsShortClassName"; //$NON-NLS-1$
		String s;

		Map map = getTableStringMap(table);
		s = (String) map.get(key);
		if (s == null) {
			StringBuffer sb = new StringBuffer(table.getDomainObjectName());
			sb.append("WithBLOBs");
			s = sb.toString();
			map.put(key, s);
		}

		return s;
	}
	
	public String getRecordFullyQualifiedClassName(FullyQualifiedTable table) {
		String key = "getRecordFullyQualifiedClassName"; //$NON-NLS-1$
		String s;

		Map map = getTableStringMap(table);
		s = (String) map.get(key);
		if (s == null) {
			StringBuffer sb = new StringBuffer();
			sb.append(getJavaModelPackage(table));
			sb.append("."); //$NON-NLS-1$
			sb.append(getRecordShortClassName(table));

			s = sb.toString();
			map.put(key, s);
		}

		return s;
	}

	public String getRecordWithBLOBsFullyQualifiedClassName(FullyQualifiedTable table) {
		String key = "getRecordWithBLOBsFullyQualifiedClassName"; //$NON-NLS-1$
		String s;

		Map map = getTableStringMap(table);
		s = (String) map.get(key);
		if (s == null) {
			StringBuffer sb = new StringBuffer();
			sb.append(getJavaModelPackage(table));
			sb.append("."); //$NON-NLS-1$
			sb.append(getRecordWithBLOBsShortClassName(table));

			s = sb.toString();
			map.put(key, s);
		}

		return s;
	}

	private String getJavaModelPackage(FullyQualifiedTable table) {
		String key = "getJavaModelPackage"; //$NON-NLS-1$
		String s;

		Map map = getTableStringMap(table);
		s = (String) map.get(key);
		if (s == null) {
			if ("true".equals(properties.get("enableSubPackages"))) { //$NON-NLS-1$  //$NON-NLS-2$
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
	
	public GeneratedJavaFile getExample(ColumnDefinitions columnDefinitions,
			TableConfiguration tableConfiguration) {
		if (!columnDefinitions.generateExampleExtendingPrimaryKey(tableConfiguration)
				&& !columnDefinitions.generateExampleExtendingRecord(tableConfiguration)) {
			return null;
		}
		
		GeneratedJavaFile answer = new GeneratedJavaFile();
		
		answer.setFileName(getExampleShortClassName(tableConfiguration.getTable()) + ".java");
		answer.setJavaInterface(false);
		answer.setJavaName(getExampleShortClassName(tableConfiguration.getTable()));
		answer.setPackageStatement(generatePackageStatement(tableConfiguration.getTable()).toString());
		
		if (columnDefinitions.generateExampleExtendingPrimaryKey(tableConfiguration)) {
			answer.setSuperClass(getPrimaryKeyShortClassName(tableConfiguration.getTable()));
		} else {
			answer.setSuperClass(getRecordShortClassName(tableConfiguration.getTable()));
		}
		
		answer.setTargetPackage(getJavaModelPackage(tableConfiguration.getTable()));
		answer.setTargetProject(targetProject);
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(JavadocUtil.getFieldComment(tableConfiguration.getTable()));
		buffer.append("public static final int EXAMPLE_IGNORE = 0;\n"); //$NON-NLS-1$
		answer.addField(buffer.toString());
		
		Iterator iter = ExampleClause.getAllExampleClauses();
		while (iter.hasNext()) {
			ExampleClause clause = (ExampleClause) iter.next();
			buffer.setLength(0);
			buffer.append(JavadocUtil.getFieldComment(tableConfiguration.getTable()));
			buffer.append("public static final int "); //$NON-NLS-1$
			buffer.append(clause.getExamplePropertyName());
			buffer.append(" = "); //$NON-NLS-1$
			buffer.append(clause.getExamplePropertyValue());
			buffer.append(";\n"); //$NON-NLS-1$
			answer.addField(buffer.toString());
		}

		buffer.setLength(0);
		buffer.append(JavadocUtil.getFieldComment(tableConfiguration.getTable()));
		buffer.append("private boolean combineTypeOr;\n"); //$NON-NLS-1$
		answer.addField(buffer.toString());

		buffer.setLength(0);
		buffer.append(JavadocUtil.getMethodComment(tableConfiguration.getTable()));
		buffer
				.append("public void setCombineTypeOr(boolean combineTypeOr) {\n"); //$NON-NLS-1$
		buffer.append("this.combineTypeOr = combineTypeOr;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		answer.addMethod(buffer.toString());

		buffer.setLength(0);
		buffer.append(JavadocUtil.getMethodComment(tableConfiguration.getTable()));
		buffer.append("public boolean isCombineTypeOr() {\n"); //$NON-NLS-1$
		buffer.append("return combineTypeOr;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		answer.addMethod(buffer.toString());

		iter = columnDefinitions.getAllColumns().iterator();
		while (iter.hasNext()) {
			ColumnDefinition cd = (ColumnDefinition) iter.next();

			if (cd.isBLOBColumn()) {
				continue;
			}

			String fieldName = cd.getJavaProperty() + "_Indicator"; //$NON-NLS-1$

			buffer.setLength(0);
			buffer.append(JavadocUtil.getFieldComment(tableConfiguration.getTable()));
			buffer.append("private int "); //$NON-NLS-1$
			buffer.append(fieldName);
			buffer.append(";\n"); //$NON-NLS-1$
			answer.addField(buffer.toString());

			buffer.setLength(0);
			buffer.append(JavadocUtil.getMethodComment(tableConfiguration.getTable()));
			buffer.append("public int "); //$NON-NLS-1$
			buffer.append(JavaBeansUtil.getGetterMethodName(fieldName));
			buffer.append("() {\n return "); //$NON-NLS-1$
			buffer.append(fieldName);
			buffer.append(";\n}"); //$NON-NLS-1$
			answer.addMethod(buffer.toString());

			buffer.setLength(0);
			buffer.append(JavadocUtil.getMethodComment(tableConfiguration.getTable()));
			buffer.append("public void "); //$NON-NLS-1$
			buffer.append(JavaBeansUtil.getSetterMethodName(fieldName));
			buffer.append("("); //$NON-NLS-1$
			buffer.append("int "); //$NON-NLS-1$
			buffer.append(fieldName);
			buffer.append(") {\nthis."); //$NON-NLS-1$
			buffer.append(fieldName);
			buffer.append(" = "); //$NON-NLS-1$
			buffer.append(fieldName);
			buffer.append(";\n}"); //$NON-NLS-1$
			answer.addMethod(buffer.toString());
		}

		return answer;
	}
	public GeneratedJavaFile getPrimaryKey(ColumnDefinitions columnDefinitions,
			TableConfiguration tableConfiguration) {

		if (!columnDefinitions.generatePrimaryKey()) {
			return null;
		}
		
		GeneratedJavaFile answer = new GeneratedJavaFile();
		
		answer.setFileName(getPrimaryKeyShortClassName(tableConfiguration.getTable()) + ".java");
		answer.setJavaInterface(false);
		answer.setJavaName(getPrimaryKeyShortClassName(tableConfiguration.getTable()));
		answer.setPackageStatement(generatePackageStatement(tableConfiguration.getTable()).toString());
		
		answer.setTargetPackage(getJavaModelPackage(tableConfiguration.getTable()));
		answer.setTargetProject(targetProject);

		generateClassParts(tableConfiguration.getTable(), columnDefinitions.getPrimaryKey(), answer);

		return answer;
	}
	
	public GeneratedJavaFile getRecord(ColumnDefinitions columnDefinitions,
			TableConfiguration tableConfiguration) {
		
		if (!columnDefinitions.generateRecordExtendingNothing()
				&& ! columnDefinitions.generateRecordExtendingPrimaryKey()) {
			return null;
		}
		
		GeneratedJavaFile answer = new GeneratedJavaFile();
		
		answer.setFileName(getRecordShortClassName(tableConfiguration.getTable()) + ".java");
		answer.setJavaInterface(false);
		answer.setJavaName(getRecordShortClassName(tableConfiguration.getTable()));
		answer.setPackageStatement(generatePackageStatement(tableConfiguration.getTable()).toString());

		if (columnDefinitions.generateRecordExtendingPrimaryKey()) {
			answer.setSuperClass(getPrimaryKeyShortClassName(tableConfiguration.getTable()));
		}
		
		answer.setTargetPackage(getJavaModelPackage(tableConfiguration.getTable()));
		answer.setTargetProject(targetProject);
		
		generateClassParts(tableConfiguration.getTable(), columnDefinitions.getNonBLOBColumns(), answer);

		return answer;
	}
	
	public GeneratedJavaFile getRecordWithBLOBs(ColumnDefinitions columnDefinitions,
			TableConfiguration tableConfiguration) {
		
		if (!columnDefinitions.generateRecordWithBLOBsExtendingPrimaryKey()
				&& !columnDefinitions.generateRecordWithBLOBsExtendingRecord()) {
			return null;
		}
		
		GeneratedJavaFile answer = new GeneratedJavaFile();
		
		answer.setFileName(getRecordWithBLOBsShortClassName(tableConfiguration.getTable()) + ".java");
		answer.setJavaInterface(false);
		answer.setJavaName(getRecordWithBLOBsShortClassName(tableConfiguration.getTable()));
		answer.setPackageStatement(generatePackageStatement(tableConfiguration.getTable()).toString());

		if (columnDefinitions.generateRecordWithBLOBsExtendingPrimaryKey()) {
			answer.setSuperClass(getPrimaryKeyShortClassName(tableConfiguration.getTable()));
		} else {
			answer.setSuperClass(getRecordShortClassName(tableConfiguration.getTable()));
		}
		
		answer.setTargetPackage(getJavaModelPackage(tableConfiguration.getTable()));
		answer.setTargetProject(targetProject);

		generateClassParts(tableConfiguration.getTable(), columnDefinitions.getBLOBColumns(), answer);

		return answer;
	}
	
    public void setTargetProject(String targetProject) {
        this.targetProject = targetProject;
    }
}
