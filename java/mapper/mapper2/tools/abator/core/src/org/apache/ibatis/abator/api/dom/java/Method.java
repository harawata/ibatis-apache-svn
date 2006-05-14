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
package org.apache.ibatis.abator.api.dom.java;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.ibatis.abator.api.dom.OutputUtilities;
import org.apache.ibatis.abator.config.FullyQualifiedTable;
import org.apache.ibatis.abator.internal.db.ColumnDefinition;

/**
 * @author Jeff Butler
 */
public class Method extends JavaElement {

    private List bodyLines;

    private boolean constructor;

    private FullyQualifiedJavaType returnType;

    private String name;

    private List parameters;

    private List exceptions;

    /**
     *  
     */
    public Method() {
        super();
        bodyLines = new ArrayList();
        parameters = new ArrayList();
        exceptions = new ArrayList();
    }

    /**
     * @return Returns the bodyLines.
     */
    public List getBodyLines() {
        return bodyLines;
    }

    public void addBodyLine(String line) {
        bodyLines.add(line);
    }

    public String getFormattedContent(int indentLevel, boolean interfaceMethod) {
        StringBuffer sb = new StringBuffer();

        Iterator iter = getJavaDocLines().iterator();
        while (iter.hasNext()) {
            OutputUtilities.javaIndent(sb, indentLevel);
            sb.append(iter.next());
            OutputUtilities.newLine(sb);
        }

        OutputUtilities.javaIndent(sb, indentLevel);

        if (!interfaceMethod) {
            if (getVisibility() == JavaVisibility.PRIVATE) {
                sb.append("private "); //$NON-NLS-1$
            } else if (getVisibility() == JavaVisibility.PROTECTED) {
                sb.append("protected "); //$NON-NLS-1$
            } else if (getVisibility() == JavaVisibility.PUBLIC) {
                sb.append("public "); //$NON-NLS-1$
            }

            if (isModifierStatic()) {
                sb.append("static "); //$NON-NLS-1$
            }

            if (isModifierFinal()) {
                sb.append("final "); //$NON-NLS-1$
            }
            
            if (bodyLines.size() == 0) {
                sb.append("abstract "); //$NON-NLS-1$
            }
        }

        if (!constructor) {
            if (getReturnType() == null) {
                sb.append("void"); //$NON-NLS-1$
            } else {
                sb.append(getReturnType().getShortName());
            }
            sb.append(' ');
        }

        sb.append(getName());
        sb.append('(');

        iter = getParameters().iterator();
        boolean comma = false;
        while (iter.hasNext()) {
            if (comma) {
                sb.append(", "); //$NON-NLS-1$
            } else {
                comma = true;
            }

            Parameter parameter = (Parameter) iter.next();
            sb.append(parameter.getType().getShortName());
            sb.append(' ');
            sb.append(parameter.getName());
        }

        sb.append(')');

        if (getExceptions().size() > 0) {
            sb.append(" throws "); //$NON-NLS-1$
            iter = getExceptions().iterator();
            comma = false;
            while (iter.hasNext()) {
                if (comma) {
                    sb.append(", "); //$NON-NLS-1$
                } else {
                    comma = true;
                }

                FullyQualifiedJavaType fqjt = (FullyQualifiedJavaType) iter
                        .next();
                sb.append(fqjt.getShortName());
            }
        }

        // if no body lines, then this is an abstract method
        if (bodyLines.size() == 0) {
            sb.append(';');
        } else {
            sb.append(" {"); //$NON-NLS-1$
            indentLevel++;

            ListIterator listIter = bodyLines.listIterator();
            while (listIter.hasNext()) {
                String line = (String) listIter.next();
                if (line.startsWith("}")) { //$NON-NLS-1$
                    indentLevel--;
                }

                OutputUtilities.newLine(sb);
                OutputUtilities.javaIndent(sb, indentLevel);
                sb.append(line);

                if ((line.endsWith("{") && !line.startsWith("switch")) //$NON-NLS-1$ //$NON-NLS-2$
                        || line.endsWith(":")) { //$NON-NLS-1$
                    indentLevel++;
                }
                
                if (line.startsWith("break")) { //$NON-NLS-1$
                    // if the next line is '}', then don't outdent
                    if (listIter.hasNext()) {
                        String nextLine = (String) listIter.next();
                        if (nextLine.startsWith("}")) { //$NON-NLS-1$
                            indentLevel++;
                        }
                        
                        // set back to the previous element
                        listIter.previous();
                    }
                    indentLevel--;
                }
            }

            indentLevel--;
            OutputUtilities.newLine(sb);
            OutputUtilities.javaIndent(sb, indentLevel);
            sb.append('}');
        }

        return sb.toString();
    }

    /**
     * @return Returns the constructor.
     */
    public boolean isConstructor() {
        return constructor;
    }

    /**
     * @param constructor
     *            The constructor to set.
     */
    public void setConstructor(boolean constructor) {
        this.constructor = constructor;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    public List getParameters() {
        return parameters;
    }

    public void addParameter(Parameter parameter) {
        parameters.add(parameter);
    }

    /**
     * @return Returns the returnType.
     */
    public FullyQualifiedJavaType getReturnType() {
        return returnType;
    }

    /**
     * @param returnType
     *            The returnType to set.
     */
    public void setReturnType(FullyQualifiedJavaType returnType) {
        this.returnType = returnType;
    }

    /**
     * @return Returns the exceptions.
     */
    public List getExceptions() {
        return exceptions;
    }

    public void addException(FullyQualifiedJavaType exception) {
        exceptions.add(exception);
    }

    public void addGetterMethodComment(FullyQualifiedTable table, ColumnDefinition columnDefinition) {
    	StringBuffer sb = new StringBuffer();
    	
    	javaDocLines.add("/**"); //$NON-NLS-1$
    	javaDocLines.add(" * This method was generated by Abator for iBATIS."); //$NON-NLS-1$
    
    	sb.append(" * This method returns the value of the database column "); //$NON-NLS-1$
    	sb.append(table.getFullyQualifiedTableName());
    	sb.append('.');
    	sb.append(columnDefinition.getColumnName());
    	javaDocLines.add(sb.toString());
    	
    	javaDocLines.add(" *"); //$NON-NLS-1$
    	
    	sb.setLength(0);
    	sb.append(" * @return the value of "); //$NON-NLS-1$
    	sb.append(table.getFullyQualifiedTableName());
    	sb.append('.');
    	sb.append(columnDefinition.getColumnName());
    	javaDocLines.add(sb.toString());
    	
    	javaDocLines.add(" *"); //$NON-NLS-1$
    	
    	sb.setLength(0);
    	sb.append(" * @abatorgenerated "); //$NON-NLS-1$
    	sb.append(new Date());
    	javaDocLines.add(sb.toString());
    	
    	javaDocLines.add(" */"); //$NON-NLS-1$
    }

    public void addSetterMethodComment(FullyQualifiedTable table, ColumnDefinition columnDefinition) {
    	StringBuffer sb = new StringBuffer();
    	
    	javaDocLines.add("/**"); //$NON-NLS-1$
    	javaDocLines.add(" * This method was generated by Abator for iBATIS."); //$NON-NLS-1$
    
    	sb.append(" * This method sets the value of the database column "); //$NON-NLS-1$
    	sb.append(table.getFullyQualifiedTableName());
    	sb.append('.');
    	sb.append(columnDefinition.getColumnName());
    	javaDocLines.add(sb.toString());
    	
    	javaDocLines.add(" *"); //$NON-NLS-1$
    
    	sb.setLength(0);
    	sb.append(" * @param "); //$NON-NLS-1$
    	sb.append(columnDefinition.getJavaProperty());
    	sb.append(" the value for "); //$NON-NLS-1$
    	sb.append(table.getFullyQualifiedTableName());
    	sb.append('.');
    	sb.append(columnDefinition.getColumnName());
    	javaDocLines.add(sb.toString());
    	
    	javaDocLines.add(" *"); //$NON-NLS-1$
    	
    	sb.setLength(0);
    	sb.append(" * @abatorgenerated "); //$NON-NLS-1$
    	sb.append(new Date());
    	javaDocLines.add(sb.toString());
    	
    	javaDocLines.add(" */"); //$NON-NLS-1$
    }

    public void addMethodComment(FullyQualifiedTable table) {
    	StringBuffer sb = new StringBuffer();
    	
    	javaDocLines.add("/**"); //$NON-NLS-1$
    	javaDocLines.add(" * This method was generated by Abator for iBATIS."); //$NON-NLS-1$
    	
    	sb.append(" * This method corresponds to the database table "); //$NON-NLS-1$
    	sb.append(table.getFullyQualifiedTableName());
    	javaDocLines.add(sb.toString());
    	
    	javaDocLines.add(" *"); //$NON-NLS-1$
    
    	sb.setLength(0);
    	sb.append(" * @abatorgenerated "); //$NON-NLS-1$
    	sb.append(new Date());
    	javaDocLines.add(sb.toString());
    	
    	javaDocLines.add(" */"); //$NON-NLS-1$
    }
}
