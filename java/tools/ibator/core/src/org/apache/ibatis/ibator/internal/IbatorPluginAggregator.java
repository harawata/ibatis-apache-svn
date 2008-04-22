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
package org.apache.ibatis.ibator.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.ibator.api.GeneratedJavaFile;
import org.apache.ibatis.ibator.api.GeneratedXmlFile;
import org.apache.ibatis.ibator.api.IbatorPlugin;
import org.apache.ibatis.ibator.api.IntrospectedTable;
import org.apache.ibatis.ibator.api.dom.java.Interface;
import org.apache.ibatis.ibator.api.dom.java.Method;
import org.apache.ibatis.ibator.api.dom.java.TopLevelClass;
import org.apache.ibatis.ibator.api.dom.xml.Document;
import org.apache.ibatis.ibator.api.dom.xml.XmlElement;
import org.apache.ibatis.ibator.config.IbatorContext;

public class IbatorPluginAggregator implements IbatorPlugin {
    private List<IbatorPlugin> plugins;

    public IbatorPluginAggregator() {
        plugins = new ArrayList<IbatorPlugin>();
    }
    
    public void addPlugin(IbatorPlugin plugin) {
        plugins.add(plugin);
    }

    public void modelBaseRecordClassGenerated(TopLevelClass tlc,
            IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.modelBaseRecordClassGenerated(tlc, introspectedTable);
        }
    }

    public void modelRecordWithBLOBsClassGenerated(TopLevelClass tlc,
            IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.modelRecordWithBLOBsClassGenerated(tlc, introspectedTable);
        }
    }

    public void sqlMapCountByExampleElementGenerated(XmlElement element,
            IntrospectedTable table) {
        for (IbatorPlugin plugin : plugins) {
            plugin.sqlMapCountByExampleElementGenerated(element, table);
        }
    }

    public void sqlMapDeleteByExampleElementGenerated(XmlElement element,
            IntrospectedTable table) {
        for (IbatorPlugin plugin : plugins) {
            plugin.sqlMapDeleteByExampleElementGenerated(element, table);
        }
    }

    public void sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element,
            IntrospectedTable table) {
        for (IbatorPlugin plugin : plugins) {
            plugin.sqlMapDeleteByPrimaryKeyElementGenerated(element, table);
        }
    }

    public void modelExampleClassGenerated(TopLevelClass tlc,
            IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.modelExampleClassGenerated(tlc, introspectedTable);
        }
    }

    public List<GeneratedJavaFile> generateAdditionalModelClasses(
            IntrospectedTable introspectedTable) {
        List<GeneratedJavaFile> answer = new ArrayList<GeneratedJavaFile>();
        for (IbatorPlugin plugin : plugins) {
            List<GeneratedJavaFile> temp = plugin.generateAdditionalModelClasses(introspectedTable);
            if (temp != null) {
                answer.addAll(temp);
            }
        }
        return answer;
    }

    public List<GeneratedXmlFile> generateAdditionalXMLFiles(
            IntrospectedTable introspectedTable) {
        List<GeneratedXmlFile> answer = new ArrayList<GeneratedXmlFile>();
        for (IbatorPlugin plugin : plugins) {
            List<GeneratedXmlFile> temp = plugin.generateAdditionalXMLFiles(introspectedTable);
            if (temp != null) {
                answer.addAll(temp);
            }
        }
        return answer;
    }

    public void modelPrimaryKeyClassGenerated(TopLevelClass tlc,
            IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.modelPrimaryKeyClassGenerated(tlc, introspectedTable);
        }
    }

    public void setIbatorContext(IbatorContext ibatorContext) {
        // ignore - not needed here
        ;
    }

    public void sqlMapBaseResultMapGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.sqlMapBaseResultMapGenerated(element, introspectedTable);
        }
    }

    public void sqlMapExampleWhereClauseGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.sqlMapExampleWhereClauseGenerated(element, introspectedTable);
        }
    }

    public void sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.sqlMapInsertElementGenerated(element, introspectedTable);
        }
    }

    public void sqlMapResultMapWithBLOBsGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.sqlMapResultMapWithBLOBsGenerated(element, introspectedTable);
        }
    }

    public void sqlMapSelectByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.sqlMapSelectByExampleElementGenerated(element, introspectedTable);
        }
    }

    public void sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.sqlMapSelectByExampleWithBLOBsElementGenerated(element, introspectedTable);
        }
    }

    public void sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.sqlMapSelectByPrimaryKeyElementGenerated(element, introspectedTable);
        }
    }

    public void sqlMapGenerated(Document document, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.sqlMapGenerated(document, introspectedTable);
        }
    }

    public void sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.sqlMapUpdateByExampleSelectiveElementGenerated(element, introspectedTable);
        }
    }

    public void sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.sqlMapUpdateByExampleWithBLOBsElementGenerated(element, introspectedTable);
        }
    }

    public void sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.sqlMapUpdateByExampleWithoutBLOBsElementGenerated(element, introspectedTable);
        }
    }

    public void sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.sqlMapUpdateByPrimaryKeySelectiveElementGenerated(element, introspectedTable);
        }
    }

    public void sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(element, introspectedTable);
        }
    }

    public void sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(element, introspectedTable);
        }
    }

    public void setProperties(Properties properties) {
        // ignore - not needed here
        ;
    }

    public void daoCountByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoCountByExampleMethodGenerated(method, interfaze, introspectedTable);
        }
    }

    public void daoCountByExampleMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoCountByExampleMethodGenerated(method, topLevelClass, introspectedTable);
        }
    }

    public void daoDeleteByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoDeleteByExampleMethodGenerated(method, interfaze, introspectedTable);
        }
    }

    public void daoDeleteByExampleMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoDeleteByExampleMethodGenerated(method, topLevelClass, introspectedTable);
        }
    }

    public void daoDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoDeleteByPrimaryKeyMethodGenerated(method, interfaze, introspectedTable);
        }
    }

    public void daoDeleteByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoDeleteByPrimaryKeyMethodGenerated(method, topLevelClass, introspectedTable);
        }
    }

    public void daoImplementationGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoImplementationGenerated(topLevelClass, introspectedTable);
        }
    }

    public void daoInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoInsertMethodGenerated(method, interfaze, introspectedTable);
        }
    }

    public void daoInsertMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoInsertMethodGenerated(method, topLevelClass, introspectedTable);
        }
    }

    public void daoInterfaceGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoInterfaceGenerated(interfaze, introspectedTable);
        }
    }

    public void daoSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoSelectByExampleWithBLOBsMethodGenerated(method, interfaze, introspectedTable);
        }
    }

    public void daoSelectByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoSelectByExampleWithBLOBsMethodGenerated(method, topLevelClass, introspectedTable);
        }
    }

    public void daoSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoSelectByExampleWithoutBLOBsMethodGenerated(method, interfaze, introspectedTable);
        }
    }

    public void daoSelectByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoSelectByExampleWithoutBLOBsMethodGenerated(method, topLevelClass, introspectedTable);
        }
    }

    public void daoSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoSelectByPrimaryKeyMethodGenerated(method, interfaze, introspectedTable);
        }
    }

    public void daoSelectByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoSelectByPrimaryKeyMethodGenerated(method, topLevelClass, introspectedTable);
        }
    }

    public void daoUpdateByExampleSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoUpdateByExampleSelectiveMethodGenerated(method, interfaze, introspectedTable);
        }
    }

    public void daoUpdateByExampleSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoUpdateByExampleSelectiveMethodGenerated(method, topLevelClass, introspectedTable);
        }
    }

    public void daoUpdateByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoUpdateByExampleWithBLOBsMethodGenerated(method, interfaze, introspectedTable);
        }
    }

    public void daoUpdateByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoUpdateByExampleWithBLOBsMethodGenerated(method, topLevelClass, introspectedTable);
        }
    }

    public void daoUpdateByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoUpdateByExampleWithoutBLOBsMethodGenerated(method, interfaze, introspectedTable);
        }
    }

    public void daoUpdateByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoUpdateByExampleWithoutBLOBsMethodGenerated(method, topLevelClass, introspectedTable);
        }
    }

    public void daoUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoUpdateByPrimaryKeySelectiveMethodGenerated(method, interfaze, introspectedTable);
        }
    }

    public void daoUpdateByPrimaryKeySelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoUpdateByPrimaryKeySelectiveMethodGenerated(method, topLevelClass, introspectedTable);
        }
    }

    public void daoUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoUpdateByPrimaryKeyWithBLOBsMethodGenerated(method, interfaze, introspectedTable);
        }
    }

    public void daoUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoUpdateByPrimaryKeyWithBLOBsMethodGenerated(method, topLevelClass, introspectedTable);
        }
    }

    public void daoUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(method, interfaze, introspectedTable);
        }
    }

    public void daoUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        for (IbatorPlugin plugin : plugins) {
            plugin.daoUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(method, topLevelClass, introspectedTable);
        }
    }

    public List<GeneratedJavaFile> generateAdditionalDAOClasses(IntrospectedTable introspectedTable) {
        List<GeneratedJavaFile> answer = new ArrayList<GeneratedJavaFile>();
        for (IbatorPlugin plugin : plugins) {
            List<GeneratedJavaFile> temp = plugin.generateAdditionalDAOClasses(introspectedTable);
            if (temp != null) {
                answer.addAll(temp);
            }
        }
        return answer;
    }
}
