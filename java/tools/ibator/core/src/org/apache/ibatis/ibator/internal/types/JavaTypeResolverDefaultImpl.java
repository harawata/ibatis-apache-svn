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
package org.apache.ibatis.ibator.internal.types;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.ibator.api.IntrospectedColumn;
import org.apache.ibatis.ibator.api.JavaTypeResolver;
import org.apache.ibatis.ibator.api.dom.java.FullyQualifiedJavaType;
import org.apache.ibatis.ibator.config.IbatorContext;
import org.apache.ibatis.ibator.config.PropertyRegistry;
import org.apache.ibatis.ibator.internal.util.StringUtility;

/**
 * 
 * @author Jeff Butler
 */
public class JavaTypeResolverDefaultImpl implements JavaTypeResolver {

    protected List<String> warnings;
	
    protected Properties properties;
    
    protected IbatorContext ibatorContext;
    
    protected boolean forceBigDecimals;
    
    protected Map<Integer, FullyQualifiedJavaType> typeMap;

    public JavaTypeResolverDefaultImpl() {
	    super();
        properties = new Properties();
        typeMap = new HashMap<Integer, FullyQualifiedJavaType>();
        
        typeMap.put(Types.ARRAY, new FullyQualifiedJavaType(Object.class.getName()));
        typeMap.put(Types.BIGINT, new FullyQualifiedJavaType(Long.class.getName()));
        typeMap.put(Types.BINARY, new FullyQualifiedJavaType("byte[]")); //$NON-NLS-1$
        typeMap.put(Types.BIT, new FullyQualifiedJavaType(Boolean.class.getName()));
        typeMap.put(Types.BLOB, new FullyQualifiedJavaType("byte[]")); //$NON-NLS-1$
        typeMap.put(Types.BOOLEAN, new FullyQualifiedJavaType(Boolean.class.getName()));
        typeMap.put(Types.CHAR, new FullyQualifiedJavaType(String.class.getName()));
        typeMap.put(Types.CLOB, new FullyQualifiedJavaType(String.class.getName()));
        typeMap.put(Types.DATALINK, new FullyQualifiedJavaType(Object.class.getName()));
        typeMap.put(Types.DATE, new FullyQualifiedJavaType(Date.class.getName()));
        typeMap.put(Types.DISTINCT, new FullyQualifiedJavaType(Object.class.getName()));
        typeMap.put(Types.DOUBLE, new FullyQualifiedJavaType(Double.class.getName()));
        typeMap.put(Types.FLOAT, new FullyQualifiedJavaType(Double.class.getName()));
        typeMap.put(Types.INTEGER, new FullyQualifiedJavaType(Integer.class.getName()));
        typeMap.put(Types.JAVA_OBJECT, new FullyQualifiedJavaType(Object.class.getName()));
        typeMap.put(Types.LONGVARBINARY, new FullyQualifiedJavaType("byte[]")); //$NON-NLS-1$
        typeMap.put(Types.LONGVARCHAR, new FullyQualifiedJavaType(String.class.getName()));
        typeMap.put(Types.NULL, new FullyQualifiedJavaType(Object.class.getName()));
        typeMap.put(Types.OTHER, new FullyQualifiedJavaType(Object.class.getName()));
        typeMap.put(Types.REAL, new FullyQualifiedJavaType(Float.class.getName()));
        typeMap.put(Types.REF, new FullyQualifiedJavaType(Object.class.getName()));
        typeMap.put(Types.SMALLINT, new FullyQualifiedJavaType(Short.class.getName()));
        typeMap.put(Types.STRUCT, new FullyQualifiedJavaType(Object.class.getName()));
        typeMap.put(Types.TIME, new FullyQualifiedJavaType(Date.class.getName()));
        typeMap.put(Types.TIMESTAMP, new FullyQualifiedJavaType(Date.class.getName()));
        typeMap.put(Types.TINYINT, new FullyQualifiedJavaType(Byte.class.getName()));
        typeMap.put(Types.VARBINARY, new FullyQualifiedJavaType("byte[]")); //$NON-NLS-1$
        typeMap.put(Types.VARCHAR, new FullyQualifiedJavaType(String.class.getName()));
    }

    public void addConfigurationProperties(Properties properties) {
        this.properties.putAll(properties);
        forceBigDecimals = StringUtility.isTrue(properties
                .getProperty(PropertyRegistry.TYPE_RESOLVER_FORCE_BIG_DECIMALS));
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.ibatis.ibator.api.JavaTypeResolver#initializeResolvedJavaType(org.apache.ibatis.ibator.internal.db.ColumnDefinition)
     */
    public FullyQualifiedJavaType calculateJavaType(IntrospectedColumn introspectedColumn) {

        FullyQualifiedJavaType answer;
        
        switch (introspectedColumn.getJdbcType()) {
        case Types.DECIMAL:
        case Types.NUMERIC:
            answer = typeMap.get(introspectedColumn.getJdbcType());
            if (answer == null) {
                if (introspectedColumn.getScale() > 0 || introspectedColumn.getLength() > 18 || forceBigDecimals) {
                    answer = new FullyQualifiedJavaType(BigDecimal.class.getName());
                } else if (introspectedColumn.getLength() > 9) {
                    answer = new FullyQualifiedJavaType(Long.class.getName());
                } else if (introspectedColumn.getLength() > 4) {
                    answer = new FullyQualifiedJavaType(Integer.class.getName());
                } else {
                    answer = new FullyQualifiedJavaType(Short.class.getName());
                }
            }
            break;

        default:
		    answer = typeMap.get(introspectedColumn.getJdbcType());
            break;
        }

        return answer;
    }
	
    /* (non-Javadoc)
     * @see org.apache.ibatis.ibator.api.JavaTypeResolver#setWarnings(java.util.List)
     */
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public void setIbatorContext(IbatorContext ibatorContext) {
        this.ibatorContext = ibatorContext;
    }
}
