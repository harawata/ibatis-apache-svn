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
package org.apache.ibatis.abator.internal.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.abator.api.GeneratedJavaFile;
import org.apache.ibatis.abator.api.JavaModelGenerator;
import org.apache.ibatis.abator.api.ProgressCallback;
import org.apache.ibatis.abator.api.dom.java.CompilationUnit;
import org.apache.ibatis.abator.api.dom.java.Field;
import org.apache.ibatis.abator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.abator.api.dom.java.JavaVisibility;
import org.apache.ibatis.abator.api.dom.java.Method;
import org.apache.ibatis.abator.api.dom.java.Parameter;
import org.apache.ibatis.abator.api.dom.java.TopLevelClass;
import org.apache.ibatis.abator.config.FullyQualifiedTable;
import org.apache.ibatis.abator.config.TableConfiguration;
import org.apache.ibatis.abator.internal.db.ColumnDefinition;
import org.apache.ibatis.abator.internal.db.ColumnDefinitions;
import org.apache.ibatis.abator.internal.rules.AbatorRules;
import org.apache.ibatis.abator.internal.sqlmap.ExampleClause;
import org.apache.ibatis.abator.internal.util.JavaBeansUtil;
import org.apache.ibatis.abator.internal.util.StringUtility;
import org.apache.ibatis.abator.internal.util.messages.Messages;

/**
 * This class supports the following properties:
 * 
 * <dl>
 *   <dt>trimStrings<dt>
 *   <dd>If true, the setters will trim all Strings.  Default is false.</dd>
 * 
 *   <dt>enableSubPackages<dt>
 *   <dd>If true, the classes will be generated in sub-packaged based on the
 *       database catalg and schema - else the will be generated in the specified
 *       package (the targetPackage attribute).  Default is false.</dd>
 * 
 *   <dt>rootClass<dt>
 *   <dd>If specified, then the root class of all objects generated by the generator will
 *       be used as specified.  No checking is done to see if the specified class exists, or
 *       if the generated classes hide any attributes or methods in the specified class.
 *       Note that the root class is not the base class of all objects - just the root
 *       class.  For example, if there is a primary key then the primary key will extend the
 *       root class and the record class will still extend the primary key.</dd>
 * </dl>
 * 
 * @author Jeff Butler
 */
public class JavaModelGeneratorDefaultImpl implements JavaModelGenerator {

	protected List warnings;
	
    /**
     * The properties from the JavaModelGenerator congiguration element
     */
    protected Map properties;

    /**
     * The target package from the JavaModelGenerator congiguration element
     */
	protected String targetPackage;

    /**
     * The target project from the JavaModelGenerator congiguration element
     */
	protected String targetProject;

	private Map tableValueMaps;

	public JavaModelGeneratorDefaultImpl() {
		super();
		tableValueMaps = new HashMap();
	}

	/*
	 *  (non-Javadoc)
	 * @see org.apache.ibatis.abator.api.JavaModelGenerator#setProperties(java.util.Map)
	 */
	public void setProperties(Map properties) {
		this.properties = properties;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.apache.ibatis.abator.api.JavaModelGenerator#setTargetPackage(java.lang.String)
	 */
	public void setTargetPackage(String targetPackage) {
		this.targetPackage = targetPackage;
	}

	private Map getTableValueMap(FullyQualifiedTable table) {
	    Map map = (Map) tableValueMaps.get(table);
	    if (map == null) {
	        map = new HashMap();
	        tableValueMaps.put(table, map);
	    }
	    
	    return map;
	}

	/**
	 * Adds fields and getter/setter methods for each ColumnDefinition
	 * passed into the method.
	 * 
	 * @param table the table from which the ColumnDefinitions are derived.  This
	 *  is used to generate appropriate JavaDoc comments for the generated fields and
	 *  methods.
	 * @param columnDefinitions the collection of ColumnDefinitions used to generate
	 *   fields and getter/setter methods.
	 * @param answer the generated fields and methods will be added to this object
	 */
	protected void generateClassParts(FullyQualifiedTable table, Collection columnDefinitions,
			TopLevelClass topLevelClass) {

		boolean trimStrings = "true".equalsIgnoreCase((String) properties //$NON-NLS-1$
				.get("trimStrings")); //$NON-NLS-1$
		
		StringBuffer sb = new StringBuffer();
		Field field;
		Method method;

		Iterator iter = columnDefinitions.iterator();
		while (iter.hasNext()) {
			ColumnDefinition cd = (ColumnDefinition) iter.next();
			FullyQualifiedJavaType fqjt = cd.getResolvedJavaType().getFullyQualifiedJavaType();
			
			topLevelClass.addImportedType(fqjt);

			String property = cd.getJavaProperty();
			
			field = new Field();
			field.addFieldComment(table, cd.getColumnName());
			field.setVisibility(JavaVisibility.PRIVATE);
			field.setType(fqjt);
			field.setName(property);
			topLevelClass.addField(field);

			method = new Method();
			method.addGetterMethodComment(table, cd);
	        method.setVisibility(JavaVisibility.PUBLIC);
			method.setReturnType(fqjt);
			method.setName(JavaBeansUtil.getGetterMethodName(property));
			sb.setLength(0);
			sb.append("return "); //$NON-NLS-1$
			sb.append(property);
			sb.append(';');
			method.addBodyLine(sb.toString());
			topLevelClass.addMethod(method);
			
			method = new Method();
			method.addSetterMethodComment(table, cd);
	        method.setVisibility(JavaVisibility.PUBLIC);
			method.setName(JavaBeansUtil.getSetterMethodName(property));
			method.addParameter(new Parameter(fqjt, property));

			if (trimStrings && cd.isCharacterColumn()) {
				sb.setLength(0);
				sb.append("if ("); //$NON-NLS-1$
				sb.append(property);
				sb.append(" != null) {"); //$NON-NLS-1$
				method.addBodyLine(sb.toString());
				sb.setLength(0);
				sb.append(property);
				sb.append(" = "); //$NON-NLS-1$
				sb.append(property);
				sb.append(".trim();"); //$NON-NLS-1$
				method.addBodyLine(sb.toString());
				method.addBodyLine("}"); //$NON-NLS-1$
			}

			sb.setLength(0);
			sb.append("this."); //$NON-NLS-1$
			sb.append(property);
			sb.append(" = "); //$NON-NLS-1$
			sb.append(property);
			sb.append(';');
			method.addBodyLine(sb.toString());
			topLevelClass.addMethod(method);
		}
	}

	/**
	 * Calculates the package for generated domain objects.
	 * 
	 * @param table the current table
	 * @return the calculated package
	 */
	protected String getJavaModelPackage(FullyQualifiedTable table) {
		String key = "getJavaModelPackage"; //$NON-NLS-1$
		String s;

		Map map = getTableValueMap(table);
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
	
	protected TopLevelClass getExample(ColumnDefinitions columnDefinitions,
			TableConfiguration tableConfiguration) {
		if (!AbatorRules.generateExampleExtendingPrimaryKey(columnDefinitions, tableConfiguration)
				&& !AbatorRules.generateExampleExtendingBaseRecord(columnDefinitions, tableConfiguration)) {
			return null;
		}

		FullyQualifiedJavaType type = getExampleType(tableConfiguration.getTable());
		TopLevelClass topLevelClass = new TopLevelClass(type);
		topLevelClass.setVisibility(JavaVisibility.PUBLIC);
		
		if (AbatorRules.generateExampleExtendingPrimaryKey(columnDefinitions, tableConfiguration)) {
			topLevelClass.setSuperClass(getPrimaryKeyType(tableConfiguration.getTable()));
		} else {
			topLevelClass.setSuperClass(getRecordType(tableConfiguration.getTable()));
		}
		
		StringBuffer sb = new StringBuffer();
		Field field = new Field();
		field.addFieldComment(tableConfiguration.getTable());
		field.setVisibility(JavaVisibility.PUBLIC);
		field.setModifierStatic(true);
		field.setModifierFinal(true);
		field.setType(FullyQualifiedJavaType.getIntInstance());
		field.setName("EXAMPLE_IGNORE"); //$NON-NLS-1$
		field.setInitializationString("0"); //$NON-NLS-1$
		topLevelClass.addField(field);
		
		Iterator iter = ExampleClause.getAllExampleClauses();
		while (iter.hasNext()) {
			ExampleClause clause = (ExampleClause) iter.next();
			field = new Field();
			field.addFieldComment(tableConfiguration.getTable());
			field.setVisibility(JavaVisibility.PUBLIC);
			field.setModifierStatic(true);
			field.setModifierFinal(true);
			field.setType(FullyQualifiedJavaType.getIntInstance());
			field.setName(clause.getExamplePropertyName());
			field.setInitializationString(Integer.toString(clause.getExamplePropertyValue()));
			topLevelClass.addField(field);
		}
		
		field = new Field();
		field.addFieldComment(tableConfiguration.getTable());
		field.setVisibility(JavaVisibility.PRIVATE);
		field.setType(FullyQualifiedJavaType.getBooleanInstance());
		field.setName("combineTypeOr"); //$NON-NLS-1$
		topLevelClass.addField(field);

		Method method = new Method();
		method.addMethodComment(tableConfiguration.getTable());
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setName("setCombineTypeOr"); //$NON-NLS-1$
		method.addParameter(new Parameter(FullyQualifiedJavaType.getBooleanInstance(),
		        "combineTypeOr")); //$NON-NLS-1$
		method.addBodyLine("this.combineTypeOr = combineTypeOr;"); //$NON-NLS-1$
		topLevelClass.addMethod(method);
		
		method = new Method();
		method.addMethodComment(tableConfiguration.getTable());
		method.setVisibility(JavaVisibility.PUBLIC);
		method.setReturnType(FullyQualifiedJavaType.getBooleanInstance());
		method.setName("isCombineTypeOr"); //$NON-NLS-1$
		method.addBodyLine("return combineTypeOr;"); //$NON-NLS-1$
		topLevelClass.addMethod(method);

		iter = columnDefinitions.getAllColumns().iterator();
		while (iter.hasNext()) {
			ColumnDefinition cd = (ColumnDefinition) iter.next();

			if (cd.isBLOBColumn()) {
				continue;
			}

			String fieldName = cd.getJavaProperty() + "_Indicator"; //$NON-NLS-1$
			
			field = new Field();
			field.addFieldComment(tableConfiguration.getTable());
			field.setVisibility(JavaVisibility.PRIVATE);
			field.setType(FullyQualifiedJavaType.getIntInstance());
			field.setName(fieldName);
			topLevelClass.addField(field);
			
			method = new Method();
			method.addMethodComment(tableConfiguration.getTable());
			method.setVisibility(JavaVisibility.PUBLIC);
			method.setReturnType(FullyQualifiedJavaType.getIntInstance());
			method.setName(JavaBeansUtil.getGetterMethodName(fieldName));
			sb.setLength(0);
			sb.append("return "); //$NON-NLS-1$
			sb.append(fieldName);
			sb.append(';');
			method.addBodyLine(sb.toString());
			topLevelClass.addMethod(method);

			method = new Method();
			method.addMethodComment(tableConfiguration.getTable());
			method.setVisibility(JavaVisibility.PUBLIC);
			method.setName(JavaBeansUtil.getSetterMethodName(fieldName));
			method.addParameter(new Parameter(FullyQualifiedJavaType.getIntInstance(), fieldName));
			sb.setLength(0);
			sb.append("this."); //$NON-NLS-1$
			sb.append(fieldName);
			sb.append(" = "); //$NON-NLS-1$
			sb.append(fieldName);
			sb.append(';');
			method.addBodyLine(sb.toString());
			topLevelClass.addMethod(method);
		}

		return topLevelClass;
	}
	
	protected TopLevelClass getPrimaryKey(ColumnDefinitions columnDefinitions,
			TableConfiguration tableConfiguration) {

		if (!AbatorRules.generatePrimaryKey(columnDefinitions)) {
			return null;
		}
		
		TopLevelClass answer = new TopLevelClass(getPrimaryKeyType(tableConfiguration.getTable()));
		answer.setVisibility(JavaVisibility.PUBLIC);
		
		if (properties.containsKey("rootClass")) { //$NON-NLS-1$
		    FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType((String) properties.get("rootClass")); //$NON-NLS-1$
	        answer.setSuperClass(fqjt);
	        answer.addImportedType(fqjt);
	    }

		generateClassParts(tableConfiguration.getTable(), columnDefinitions.getPrimaryKey(), answer);

		return answer;
	}
	
	protected TopLevelClass getRecord(ColumnDefinitions columnDefinitions,
			TableConfiguration tableConfiguration) {
		
		if (!AbatorRules.generateBaseRecordWithNoSuperclass(columnDefinitions)
				&& ! AbatorRules.generateBaseRecordExtendingPrimaryKey(columnDefinitions)) {
			return null;
		}
		
		TopLevelClass answer = new TopLevelClass(getRecordType(tableConfiguration.getTable()));
		answer.setVisibility(JavaVisibility.PUBLIC);
		
		if (AbatorRules.generateBaseRecordExtendingPrimaryKey(columnDefinitions)) {
			answer.setSuperClass(getPrimaryKeyType(tableConfiguration.getTable()));
		} else {
		    if (properties.containsKey("rootClass")) { //$NON-NLS-1$
			    FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType((String) properties.get("rootClass")); //$NON-NLS-1$
		        answer.setSuperClass(fqjt);
		        answer.addImportedType(fqjt);
		    }
		}
		
		generateClassParts(tableConfiguration.getTable(), columnDefinitions.getNonBLOBColumns(), answer);

		return answer;
	}
	
	protected TopLevelClass getRecordWithBLOBs(ColumnDefinitions columnDefinitions,
			TableConfiguration tableConfiguration) {
		
		if (!AbatorRules.generateRecordWithBLOBsExtendingPrimaryKey(columnDefinitions)
				&& !AbatorRules.generateRecordWithBLOBsExtendingBaseRecord(columnDefinitions)) {
			return null;
		}
		
		TopLevelClass answer = new TopLevelClass(getRecordWithBLOBsType(tableConfiguration.getTable()));
		answer.setVisibility(JavaVisibility.PUBLIC);
		
		if (AbatorRules.generateRecordWithBLOBsExtendingPrimaryKey(columnDefinitions)) {
			answer.setSuperClass(getPrimaryKeyType(tableConfiguration.getTable()));
		} else {
			answer.setSuperClass(getRecordType(tableConfiguration.getTable()));
		}
		
		generateClassParts(tableConfiguration.getTable(), columnDefinitions.getBLOBColumns(), answer);

		return answer;
	}
	
    public void setTargetProject(String targetProject) {
        this.targetProject = targetProject;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.abator.api.JavaModelGenerator#getExampleType(org.apache.ibatis.abator.config.FullyQualifiedTable)
     */
    public FullyQualifiedJavaType getExampleType(FullyQualifiedTable table) {
		String key = "getExampleType"; //$NON-NLS-1$

		Map map = getTableValueMap(table);
		FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) map.get(key);
		if (fqjt == null) {
			StringBuffer sb = new StringBuffer();
			sb.append(getJavaModelPackage(table));
			sb.append('.');
			sb.append(table.getDomainObjectName());
			sb.append("Example"); //$NON-NLS-1$

			fqjt = new FullyQualifiedJavaType(sb.toString());
			map.put(key, fqjt);
		}

		return fqjt;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.abator.api.JavaModelGenerator#getGeneratedJavaFiles(org.apache.ibatis.abator.internal.db.ColumnDefinitions, org.apache.ibatis.abator.config.TableConfiguration, org.apache.ibatis.abator.api.ProgressCallback)
     */
    public List getGeneratedJavaFiles(ColumnDefinitions columnDefinitions,
            TableConfiguration tableConfiguration, ProgressCallback callback) {
        List list = new ArrayList();
        
		String tableName = tableConfiguration.getTable().getFullyQualifiedTableName();
		
		callback.startSubTask(Messages.getString("JavaModelGeneratorDefaultImpl.0", tableName)); //$NON-NLS-1$
        CompilationUnit cu = getExample(columnDefinitions, tableConfiguration);
        if (cu != null) {
            GeneratedJavaFile gjf = new GeneratedJavaFile(cu, targetProject);
            list.add(gjf);
        }
        
		callback.startSubTask(Messages.getString("JavaModelGeneratorDefaultImpl.1", tableName)); //$NON-NLS-1$
        cu = getPrimaryKey(columnDefinitions, tableConfiguration);
        if (cu != null) {
            GeneratedJavaFile gjf = new GeneratedJavaFile(cu, targetProject);
            list.add(gjf);
        }
        
		callback.startSubTask(Messages.getString("JavaModelGeneratorDefaultImpl.2", tableName)); //$NON-NLS-1$
        cu = getRecord(columnDefinitions, tableConfiguration);
        if (cu != null) {
            GeneratedJavaFile gjf = new GeneratedJavaFile(cu, targetProject);
            list.add(gjf);
        }
        
		callback.startSubTask(Messages.getString("JavaModelGeneratorDefaultImpl.3", tableName)); //$NON-NLS-1$
        cu = getRecordWithBLOBs(columnDefinitions, tableConfiguration);
        if (cu != null) {
            GeneratedJavaFile gjf = new GeneratedJavaFile(cu, targetProject);
            list.add(gjf);
        }

        return list;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.abator.api.JavaModelGenerator#getPrimaryKeyType(org.apache.ibatis.abator.config.FullyQualifiedTable)
     */
    public FullyQualifiedJavaType getPrimaryKeyType(FullyQualifiedTable table) {
		String key = "getPrimaryKeyType"; //$NON-NLS-1$

		Map map = getTableValueMap(table);
		FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) map.get(key);
		if (fqjt == null) {
			StringBuffer sb = new StringBuffer();
			sb.append(getJavaModelPackage(table));
			sb.append('.');
			sb.append(table.getDomainObjectName());
			sb.append("Key"); //$NON-NLS-1$

			fqjt = new FullyQualifiedJavaType(sb.toString());
			map.put(key, fqjt);
		}

		return fqjt;
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.abator.api.JavaModelGenerator#getRecordType(org.apache.ibatis.abator.config.FullyQualifiedTable)
     */
    public FullyQualifiedJavaType getRecordType(FullyQualifiedTable table) {
		String key = "getRecordType"; //$NON-NLS-1$

		Map map = getTableValueMap(table);
		FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) map.get(key);
		if (fqjt == null) {
			StringBuffer sb = new StringBuffer();
			sb.append(getJavaModelPackage(table));
			sb.append('.');
			sb.append(table.getDomainObjectName());

			fqjt = new FullyQualifiedJavaType(sb.toString());
			map.put(key, fqjt);
		}

		return fqjt;
    }
    
    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.abator.api.JavaModelGenerator#getRecordWithBLOBsType(org.apache.ibatis.abator.config.FullyQualifiedTable)
     */
    public FullyQualifiedJavaType getRecordWithBLOBsType(
            FullyQualifiedTable table) {
		String key = "getRecordWithBLOBsType"; //$NON-NLS-1$

		Map map = getTableValueMap(table);
		FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) map.get(key);
		if (fqjt == null) {
			StringBuffer sb = new StringBuffer();
			sb.append(getJavaModelPackage(table));
			sb.append('.');
			sb.append(table.getDomainObjectName());
			sb.append("WithBLOBs"); //$NON-NLS-1$

			fqjt = new FullyQualifiedJavaType(sb.toString());
			map.put(key, fqjt);
		}

		return fqjt;
    }
    
	/* (non-Javadoc)
	 * @see org.apache.ibatis.abator.api.JavaModelGenerator#setWarnings(java.util.List)
	 */
	public void setWarnings(List warnings) {
		this.warnings = warnings;
	}
}
