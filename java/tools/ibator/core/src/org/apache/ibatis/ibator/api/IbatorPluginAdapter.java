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
package org.apache.ibatis.ibator.api;

import java.util.List;
import java.util.Properties;

import org.apache.ibatis.ibator.api.dom.java.Interface;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;
import org.apache.ibatis.ibator.api.dom.xml.Document;
import org.apache.ibatis.ibator.api.dom.xml.XmlElement;
import org.apache.ibatis.ibator.config.IbatorContext;

/**
 * This class includes no-operation methods for every method in the
 * IbatorPlugin interface.  Clients may extend this class to implement
 * some or all of the methods in a plugin. 
 * 
 * @author Jeff Butler
 *
 */
public class IbatorPluginAdapter implements IbatorPlugin {
    protected IbatorContext ibatorContext;
    protected Properties properties;

    public IbatorContext getIbatorContext() {
        return ibatorContext;
    }

    public void setIbatorContext(IbatorContext ibatorContext) {
        this.ibatorContext = ibatorContext;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void modelBaseRecordClassGenerated(TopLevelClass baseClass, IntrospectedTable introspectedTable) {
    }

    public void modelRecordWithBLOBsClassGenerated(TopLevelClass blobsClass, IntrospectedTable introspectedTable) {
    }

    public void sqlMapCountByExampleElementGenerated(XmlElement element, IntrospectedTable table) {
    }

    public void sqlMapDeleteByExampleElementGenerated(XmlElement element, IntrospectedTable table) {
    }

    public void sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable table) {
    }

    public void modelExampleClassGenerated(TopLevelClass example, IntrospectedTable introspectedTable) {
    }

    public List<GeneratedJavaFile> generateAdditionalModelClasses(IntrospectedTable introspectedTable) {
        return null;
    }

    public List<GeneratedXmlFile> generateAdditionalXMLFiles(IntrospectedTable introspectedTable) {
        return null;
    }

    public void modelPrimaryKeyClassGenerated(TopLevelClass primaryKey, IntrospectedTable introspectedTable) {
    }

    public void sqlMapGenerated(Document document, IntrospectedTable introspectedTable) {
    }

    public void sqlMapBaseResultMapGenerated(XmlElement element, IntrospectedTable introspectedTable) {
    }

    public void sqlMapExampleWhereClauseGenerated(XmlElement element, IntrospectedTable introspectedTable) {
    }

    public void sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
    }

    public void sqlMapResultMapWithBLOBsGenerated(XmlElement element, IntrospectedTable introspectedTable) {
    }

    public void sqlMapSelectByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
    }

    public void sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
    }

    public void sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
    }

    public void sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
    }

    public void sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
    }

    public void sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
    }

    public void sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
    }

    public void sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
    }

    public void sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
    }

    public void daoCountByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
    }

    public void daoCountByExampleMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    }

    public void daoDeleteByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
    }

    public void daoDeleteByExampleMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    }

    public void daoDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
    }

    public void daoDeleteByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    }

    public void daoImplementationGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    }

    public void daoInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
    }

    public void daoInsertMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    }

    public void daoInterfaceGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
    }

    public void daoSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
    }

    public void daoSelectByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    }

    public void daoSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
    }

    public void daoSelectByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    }

    public void daoSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
    }

    public void daoSelectByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    }

    public void daoUpdateByExampleSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
    }

    public void daoUpdateByExampleSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    }

    public void daoUpdateByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
    }

    public void daoUpdateByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    }

    public void daoUpdateByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
    }

    public void daoUpdateByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    }

    public void daoUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
    }

    public void daoUpdateByPrimaryKeySelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    }

    public void daoUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
    }

    public void daoUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    }

    public void daoUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
    }

    public void daoUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    }

    public List<GeneratedJavaFile> generateAdditionalDAOClasses(IntrospectedTable introspectedTable) {
        return null;
    }
}
