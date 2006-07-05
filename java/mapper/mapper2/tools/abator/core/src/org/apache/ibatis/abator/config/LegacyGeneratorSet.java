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

package org.apache.ibatis.abator.config;

import org.apache.ibatis.abator.internal.java.DAOGeneratorGenericConstructorInjectionImpl;
import org.apache.ibatis.abator.internal.java.DAOGeneratorGenericSetterInjectionImpl;
import org.apache.ibatis.abator.internal.java.DAOGeneratorIbatisImpl;
import org.apache.ibatis.abator.internal.java.DAOGeneratorSpringImpl;
import org.apache.ibatis.abator.internal.java.JavaModelGeneratorDefaultImpl;
import org.apache.ibatis.abator.internal.sqlmap.SqlMapGeneratorDefaultImpl;
import org.apache.ibatis.abator.internal.types.JavaTypeResolverDefaultImpl;

/**
 * @author Jeff Butler
 *
 */
public class LegacyGeneratorSet extends GeneratorSet {
    
    /**
     * 
     */
    public LegacyGeneratorSet() {
        super();
        super.javaModelGeneratorType = JavaModelGeneratorDefaultImpl.class.getName();
        super.javaTypeResolverType = JavaTypeResolverDefaultImpl.class.getName();
        super.sqlMapGeneratorType = SqlMapGeneratorDefaultImpl.class.getName();
    }

    public String translateDAOGeneratorType(String configurationType) {
        String answer;
        
        if ("IBATIS".equalsIgnoreCase(configurationType)) { //$NON-NLS-1$
            answer = DAOGeneratorIbatisImpl.class.getName();
        } else if ("SPRING".equalsIgnoreCase(configurationType)) { //$NON-NLS-1$
            answer = DAOGeneratorSpringImpl.class.getName();
        } else if ("GENERIC-CI".equalsIgnoreCase(configurationType)) { //$NON-NLS-1$
            answer = DAOGeneratorGenericConstructorInjectionImpl.class.getName();
        } else if ("GENERIC-SI".equalsIgnoreCase(configurationType)) { //$NON-NLS-1$
            answer = DAOGeneratorGenericSetterInjectionImpl.class.getName();
        } else {
            answer = configurationType;
        }
        
        return answer;
    }
}