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
package org.apache.ibatis.abator.internal;

import java.util.List;

import org.apache.ibatis.abator.api.DAOGenerator;
import org.apache.ibatis.abator.api.JavaModelGenerator;
import org.apache.ibatis.abator.api.JavaTypeResolver;
import org.apache.ibatis.abator.api.SqlMapGenerator;
import org.apache.ibatis.abator.config.DAOGeneratorConfiguration;
import org.apache.ibatis.abator.config.JavaModelGeneratorConfiguration;
import org.apache.ibatis.abator.config.JavaTypeResolverConfiguration;
import org.apache.ibatis.abator.config.SqlMapGeneratorConfiguration;
import org.apache.ibatis.abator.exception.GenerationRuntimeException;
import org.apache.ibatis.abator.internal.util.messages.Messages;

/**
 * This class creates the different configurable Abator generators
 * 
 * @author Jeff Butler
 */
public class AbatorObjectFactory {

    /**
     * Utility class.  No instances allowed 
     */
    private AbatorObjectFactory() {
        super();
    }

	private static Object createObject(String className) {
		Object answer;

		try {
			answer = Class.forName(className).newInstance();
		} catch (Exception e) {
			throw new GenerationRuntimeException(
			        Messages.getString("AbatorObjectFactory.0", className), e); //$NON-NLS-1$
		}

		return answer;
	}
	
	public static JavaTypeResolver createJavaTypeResolver(JavaTypeResolverConfiguration configuration,
			List warnings) {
	    JavaTypeResolver answer = (JavaTypeResolver) createObject(configuration.getType());
	    answer.setWarnings(warnings);
	    
	    answer.setProperties(configuration.getProperties());
	    
	    return answer;
	}
	
	public static SqlMapGenerator createSqlMapGenerator(SqlMapGeneratorConfiguration configuration,
	        JavaModelGenerator javaModelGenerator, List warnings) {
	    SqlMapGenerator answer = (SqlMapGenerator) createObject(configuration.getType());
	    answer.setWarnings(warnings);

	    answer.setJavaModelGenerator(javaModelGenerator);
	    answer.setProperties(configuration.getProperties());
	    answer.setTargetPackage(configuration.getTargetPackage());
	    answer.setTargetProject(configuration.getTargetProject());
	    
	    return answer;
	    
	}
	
	public static JavaModelGenerator createJavaModelGenerator(JavaModelGeneratorConfiguration configuration,
			List warnings) {
	    JavaModelGenerator answer = (JavaModelGenerator) createObject(configuration.getType());
	    answer.setWarnings(warnings);
	    
	    answer.setProperties(configuration.getProperties());
	    answer.setTargetPackage(configuration.getTargetPackage());
	    answer.setTargetProject(configuration.getTargetProject());
	    
	    return answer;
	}
	
	public static DAOGenerator createDAOGenerator(DAOGeneratorConfiguration configuration,
	        JavaModelGenerator javaModelGenerator, SqlMapGenerator sqlMapGenerator, List warnings) {
	    if (!configuration.isEnabled()) {
	        return null;
	    }
	    
	    DAOGenerator answer = (DAOGenerator) createObject(configuration.getType());
	    answer.setWarnings(warnings);

	    answer.setJavaModelGenerator(javaModelGenerator);
	    answer.setProperties(configuration.getProperties());
	    answer.setSqlMapGenerator(sqlMapGenerator);
	    answer.setTargetPackage(configuration.getTargetPackage());
	    answer.setTargetProject(configuration.getTargetProject());
	    
	    return answer;
	}
}
