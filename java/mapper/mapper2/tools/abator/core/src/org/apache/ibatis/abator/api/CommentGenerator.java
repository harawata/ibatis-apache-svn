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
package org.apache.ibatis.abator.api;

import org.apache.ibatis.abator.api.dom.java.CompilationUnit;
import org.apache.ibatis.abator.api.dom.java.Field;
import org.apache.ibatis.abator.api.dom.java.InnerClass;
import org.apache.ibatis.abator.api.dom.java.InnerEnum;
import org.apache.ibatis.abator.api.dom.java.Method;
import org.apache.ibatis.abator.api.dom.xml.Document;
import org.apache.ibatis.abator.api.dom.xml.XmlElement;
import org.apache.ibatis.abator.internal.db.ColumnDefinition;

/**
 * 
 * @author Jeff Butler
 */
public interface CommentGenerator {

    public void addFieldComment(Field field, FullyQualifiedTable table, String columnName);

    public void addFieldComment(Field field, FullyQualifiedTable table);

    public void addClassComment(InnerClass innerClass, FullyQualifiedTable table);

    public void addEnumComment(InnerEnum innerEnum, FullyQualifiedTable table);

    public void addGetterComment(Method method, FullyQualifiedTable table, ColumnDefinition columnDefinition);

    public void addSetterComment(Method method, FullyQualifiedTable table, ColumnDefinition columnDefinition);

    public void addGeneralMethodComment(Method method, FullyQualifiedTable table);

    public void addJavaFileComment(CompilationUnit compilationUnit);

    /**
     * Adds a suitable comment to warn users that the element was generated, and
     * when it was generated.
     */
    public void addComment(XmlElement xmlElement);
    
    public void addComment(Document document);
}
