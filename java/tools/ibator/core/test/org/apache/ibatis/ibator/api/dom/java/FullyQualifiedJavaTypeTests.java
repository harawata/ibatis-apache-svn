/*
 *  Copyright 2009 The Apache Software Foundation
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

package org.apache.ibatis.ibator.api.dom.java;

import junit.framework.TestCase;

/**
 * @author Jeff Butler
 *
 */
public class FullyQualifiedJavaTypeTests extends TestCase {
    
    public void testJavaType() {
        FullyQualifiedJavaType fqjt =
            new FullyQualifiedJavaType("java.lang.String"); //$NON-NLS-1$
        assertFalse(fqjt.isExplicitlyImported());
        assertEquals("String", fqjt.getShortName()); //$NON-NLS-1$
        assertEquals("java.lang.String", fqjt.getFullyQualifiedName()); //$NON-NLS-1$
        assertEquals("java.lang", fqjt.getPackageName()); //$NON-NLS-1$
        assertEquals(0, fqjt.getImportList().size());
    }

    public void testSimpleType() {
        FullyQualifiedJavaType fqjt =
            new FullyQualifiedJavaType("com.foo.Bar"); //$NON-NLS-1$
        assertTrue(fqjt.isExplicitlyImported());
        assertEquals("Bar", fqjt.getShortName()); //$NON-NLS-1$
        assertEquals("com.foo.Bar", fqjt.getFullyQualifiedName()); //$NON-NLS-1$
        assertEquals("com.foo", fqjt.getPackageName()); //$NON-NLS-1$
        assertEquals(1, fqjt.getImportList().size());
    }

    public void testGenericType1() {
        FullyQualifiedJavaType fqjt =
            new FullyQualifiedJavaType("java.util.List<java.lang.String>"); //$NON-NLS-1$
        assertTrue(fqjt.isExplicitlyImported());
        assertEquals("List<String>", fqjt.getShortName()); //$NON-NLS-1$
        assertEquals("java.util.List<java.lang.String>", fqjt.getFullyQualifiedName()); //$NON-NLS-1$
        assertEquals("java.util", fqjt.getPackageName()); //$NON-NLS-1$
        assertEquals(1, fqjt.getImportList().size());
    }


    public void testGenericType2() {
        FullyQualifiedJavaType fqjt =
            new FullyQualifiedJavaType("java.util.Map<java.lang.String, java.util.List<java.lang.String>>"); //$NON-NLS-1$
        assertTrue(fqjt.isExplicitlyImported());
        assertEquals("Map<String, List<String>>", fqjt.getShortName()); //$NON-NLS-1$
        assertEquals("java.util.Map<java.lang.String, java.util.List<java.lang.String>>", fqjt.getFullyQualifiedName()); //$NON-NLS-1$
        assertEquals("java.util", fqjt.getPackageName()); //$NON-NLS-1$
        assertEquals(2, fqjt.getImportList().size());
    }


    public void testGenericType3() {
        FullyQualifiedJavaType listOfStrings = new FullyQualifiedJavaType("java.util.List"); //$NON-NLS-1$
        listOfStrings.addTypeArgument(new FullyQualifiedJavaType("java.lang.String")); //$NON-NLS-1$
        
        FullyQualifiedJavaType fqjt =
            new FullyQualifiedJavaType("java.util.Map"); //$NON-NLS-1$
        fqjt.addTypeArgument(new FullyQualifiedJavaType("java.lang.String")); //$NON-NLS-1$
        fqjt.addTypeArgument(listOfStrings);
        
        assertTrue(fqjt.isExplicitlyImported());
        assertEquals("Map<String, List<String>>", fqjt.getShortName()); //$NON-NLS-1$
        assertEquals("java.util.Map<java.lang.String, java.util.List<java.lang.String>>", fqjt.getFullyQualifiedName()); //$NON-NLS-1$
        assertEquals("java.util", fqjt.getPackageName()); //$NON-NLS-1$
        assertEquals(2, fqjt.getImportList().size());
    }
}
