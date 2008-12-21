/*
 *  Copyright 2008 The Apache Software Foundation
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

package org.apache.ibatis.ibator.eclipse.ui.content;

import org.eclipse.jdt.core.IJavaProject;

/**
 * This is the adaptor class for a project that does not contain
 * Ibator on it's classpath
 * 
 * @author Jeff Butler
 *
 */
public class JavaProjectAdapter {
    
    private IJavaProject javaProject;

    public JavaProjectAdapter(IJavaProject javaProject) {
        super();
        this.javaProject = javaProject;
    }

    public IJavaProject getJavaProject() {
        return javaProject;
    }
}

