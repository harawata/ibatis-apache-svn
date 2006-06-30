/*
 *  Copyright 2005, 2006 The Apache Software Foundation
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
package org.apache.ibatis.abator.config.xml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.ibatis.abator.config.AbatorConfiguration;
import org.apache.ibatis.abator.config.AbatorContext;
import org.apache.ibatis.abator.config.ColumnOverride;
import org.apache.ibatis.abator.config.DAOGeneratorConfiguration;
import org.apache.ibatis.abator.config.FullyQualifiedTable;
import org.apache.ibatis.abator.config.GeneratedKey;
import org.apache.ibatis.abator.config.JDBCConnectionConfiguration;
import org.apache.ibatis.abator.config.JavaModelGeneratorConfiguration;
import org.apache.ibatis.abator.config.JavaTypeResolverConfiguration;
import org.apache.ibatis.abator.config.PropertyHolder;
import org.apache.ibatis.abator.config.SqlMapGeneratorConfiguration;
import org.apache.ibatis.abator.config.TableConfiguration;
import org.apache.ibatis.abator.exception.XMLParserException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Jeff Butler
 */
public class AbatorConfigurationParser {
    private List warnings;

    private List parseErrors;

    /**
     * 
     */
    public AbatorConfigurationParser(List warnings) {
        super();
        this.warnings = warnings;
        parseErrors = new ArrayList();
    }

    public AbatorConfiguration parseAbatorConfiguration(File inputFile)
            throws IOException, XMLParserException {

        FileReader fr = new FileReader(inputFile);

        return parseAbatorConfiguration(fr);
    }

    public AbatorConfiguration parseAbatorConfiguration(Reader reader)
            throws IOException, XMLParserException {

        InputSource is = new InputSource(reader);

        return parseAbatorConfiguration(is);
    }

    public AbatorConfiguration parseAbatorConfiguration(InputStream inputStream)
            throws IOException, XMLParserException {

        InputSource is = new InputSource(inputStream);

        return parseAbatorConfiguration(is);
    }

    private AbatorConfiguration parseAbatorConfiguration(InputSource inputSource)
            throws IOException, XMLParserException {
        parseErrors.clear();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(true);

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(new ParserEntityResolver());

            ParserErrorHandler handler = new ParserErrorHandler(warnings,
                    parseErrors);
            builder.setErrorHandler(handler);

            Document document = null;
            try {
                document = builder.parse(inputSource);
            } catch (SAXParseException e) {
                throw new XMLParserException(parseErrors);
            } catch (SAXException e) {
                if (e.getException() == null) {
                    parseErrors.add(e.getMessage());
                } else {
                    parseErrors.add(e.getException().getMessage());
                }
            }

            if (parseErrors.size() > 0) {
                throw new XMLParserException(parseErrors);
            }

            NodeList nodeList = document.getChildNodes();
            AbatorConfiguration gc = null;
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == 1
                        && "abatorConfiguration".equals(node.getNodeName())) { //$NON-NLS-1$
                    gc = parseAbatorConfiguration(node);
                    break;
                }
            }

            if (parseErrors.size() > 0) {
                throw new XMLParserException(parseErrors);
            }

            return gc;
        } catch (ParserConfigurationException e) {
            parseErrors.add(e.getMessage());
            throw new XMLParserException(parseErrors);
        }
    }

    private AbatorConfiguration parseAbatorConfiguration(Node node) {

        AbatorConfiguration abatorConfiguration = new AbatorConfiguration();

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);

            if (childNode.getNodeType() != 1) {
                continue;
            }

            if ("abatorContext".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseAbatorContext(abatorConfiguration, childNode);
            }
        }

        return abatorConfiguration;
    }

    private void parseAbatorContext(AbatorConfiguration abatorConfiguration,
            Node node) {

        NamedNodeMap nnm = node.getAttributes();
        Node attribute = nnm.getNamedItem("generatorSet"); //$NON-NLS-1$
        AbatorContext abatorContext;
        if (attribute == null) {
            abatorContext = new AbatorContext();
        } else {
            abatorContext = new AbatorContext(attribute.getNodeValue());
        }

        abatorConfiguration.addAbatorContext(abatorContext);

        attribute = nnm.getNamedItem("id"); //$NON-NLS-1$
        if (attribute != null) {
            abatorContext.setId(attribute.getNodeValue());
        }

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);

            if (childNode.getNodeType() != 1) {
                continue;
            }

            if ("jdbcConnection".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseJdbcConnection(abatorContext, childNode);
            } else if ("javaModelGenerator".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseJavaModelGenerator(abatorContext, childNode);
            } else if ("javaTypeResolver".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseJavaTypeResolver(abatorContext, childNode);
            } else if ("sqlMapGenerator".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseSqlMapGenerator(abatorContext, childNode);
            } else if ("daoGenerator".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseDaoGenerator(abatorContext, childNode);
            } else if ("table".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseTable(abatorContext, childNode);
            }
        }
    }

    private void parseSqlMapGenerator(AbatorContext abatorContext, Node node) {
        SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration = new SqlMapGeneratorConfiguration();

        abatorContext
                .setSqlMapGeneratorConfiguration(sqlMapGeneratorConfiguration);

        NamedNodeMap nnm = node.getAttributes();

        Node attribute = nnm.getNamedItem("type"); //$NON-NLS-1$
        if (attribute != null) {
            sqlMapGeneratorConfiguration.setConfigurationType(attribute
                    .getNodeValue());
        }

        attribute = nnm.getNamedItem("targetPackage"); //$NON-NLS-1$
        sqlMapGeneratorConfiguration.setTargetPackage(attribute.getNodeValue());

        attribute = nnm.getNamedItem("targetProject"); //$NON-NLS-1$
        sqlMapGeneratorConfiguration.setTargetProject(attribute.getNodeValue());

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);

            if (childNode.getNodeType() != 1) {
                continue;
            }

            if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseProperty(sqlMapGeneratorConfiguration, childNode);
            }
        }
    }

    private void parseTable(AbatorContext abatorContext, Node node) {
        TableConfiguration tc = new TableConfiguration();
        abatorContext.addTableConfiguration(tc);

        NamedNodeMap nnm = node.getAttributes();

        Node attribute = nnm.getNamedItem("catalog"); //$NON-NLS-1$
        String catalog = attribute == null ? null : attribute.getNodeValue();

        attribute = nnm.getNamedItem("schema"); //$NON-NLS-1$
        String schema = attribute == null ? null : attribute.getNodeValue();

        attribute = nnm.getNamedItem("domainObjectName"); //$NON-NLS-1$
        String domainObjectName = attribute == null ? null : attribute
                .getNodeValue();

        attribute = nnm.getNamedItem("tableName"); //$NON-NLS-1$
        String tableName = attribute.getNodeValue();

        attribute = nnm.getNamedItem("alias"); //$NON-NLS-1$
        String alias = attribute == null ? null : attribute.getNodeValue();

        FullyQualifiedTable table = new FullyQualifiedTable(catalog, schema,
                tableName, domainObjectName, alias);

        tc.setTable(table);

        attribute = nnm.getNamedItem("enableInsert"); //$NON-NLS-1$
        if (attribute != null) {
            tc.setInsertStatementEnabled("true" //$NON-NLS-1$
                    .equals(attribute.getNodeValue()));
        }

        attribute = nnm.getNamedItem("enableSelectByPrimaryKey"); //$NON-NLS-1$
        if (attribute != null) {
            tc.setSelectByPrimaryKeyStatementEnabled("true".equals(attribute //$NON-NLS-1$
                    .getNodeValue()));
        }

        attribute = nnm.getNamedItem("enableSelectByExample"); //$NON-NLS-1$
        if (attribute != null) {
            tc.setSelectByExampleStatementEnabled("true".equals(attribute //$NON-NLS-1$
                    .getNodeValue()));
        }

        attribute = nnm.getNamedItem("enableUpdateByPrimaryKey"); //$NON-NLS-1$
        if (attribute != null) {
            tc.setUpdateByPrimaryKeyStatementEnabled("true".equals(attribute //$NON-NLS-1$
                    .getNodeValue()));
        }

        attribute = nnm.getNamedItem("enableDeleteByPrimaryKey"); //$NON-NLS-1$
        if (attribute != null) {
            tc.setDeleteByPrimaryKeyStatementEnabled("true".equals(attribute //$NON-NLS-1$
                    .getNodeValue()));
        }

        attribute = nnm.getNamedItem("enableDeleteByExample"); //$NON-NLS-1$
        if (attribute != null) {
            tc.setDeleteByExampleStatementEnabled("true".equals(attribute //$NON-NLS-1$
                    .getNodeValue()));
        }

        attribute = nnm.getNamedItem("selectByPrimaryKeyQueryId"); //$NON-NLS-1$
        if (attribute != null) {
            tc.setSelectByPrimaryKeyQueryId(attribute.getNodeValue());
        }

        attribute = nnm.getNamedItem("selectByExampleQueryId"); //$NON-NLS-1$
        if (attribute != null) {
            tc.setSelectByExampleQueryId(attribute.getNodeValue());
        }

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);

            if (childNode.getNodeType() != 1) {
                continue;
            }

            if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseProperty(tc, childNode);
            } else if ("columnOverride".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseColumnOverride(tc, childNode);
            } else if ("ignoreColumn".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseIgnoreColumn(tc, childNode);
            } else if ("generatedKey".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseGeneratedKey(tc, childNode);
            }
        }
    }

    private void parseColumnOverride(TableConfiguration tc, Node node) {
        NamedNodeMap nnm = node.getAttributes();

        ColumnOverride co = new ColumnOverride();

        Node attribute = nnm.getNamedItem("column"); //$NON-NLS-1$
        co.setColumnName(attribute.getNodeValue());

        tc.addColumnOverride(co);

        attribute = nnm.getNamedItem("property"); //$NON-NLS-1$
        if (attribute != null) {
            co.setJavaProperty(attribute.getNodeValue());
        }

        attribute = nnm.getNamedItem("javaType"); //$NON-NLS-1$
        if (attribute != null) {
            co.setJavaType(attribute.getNodeValue());
        }

        attribute = nnm.getNamedItem("jdbcType"); //$NON-NLS-1$
        if (attribute != null) {
            co.setJdbcType(attribute.getNodeValue());
        }

        attribute = nnm.getNamedItem("typeHandler"); //$NON-NLS-1$
        if (attribute != null) {
            co.setTypeHandler(attribute.getNodeValue());
        }
    }

    private void parseGeneratedKey(TableConfiguration tc, Node node) {
        NamedNodeMap nnm = node.getAttributes();

        String column = nnm.getNamedItem("column").getNodeValue(); //$NON-NLS-1$
        boolean identity = "true".equals(nnm.getNamedItem("identity").getNodeValue()); //$NON-NLS-1$ //$NON-NLS-2$
        String sqlStatement = nnm.getNamedItem("sqlStatement").getNodeValue(); //$NON-NLS-1$

        GeneratedKey gk = new GeneratedKey(column, sqlStatement, identity);
        tc.setGeneratedKey(gk);
    }

    private void parseIgnoreColumn(TableConfiguration tc, Node node) {
        NamedNodeMap nnm = node.getAttributes();

        Node attribute = nnm.getNamedItem("column"); //$NON-NLS-1$
        tc.addIgnoredColumn(attribute.getNodeValue());
    }

    private void parseJavaTypeResolver(AbatorContext abatorContext, Node node) {
        JavaTypeResolverConfiguration javaTypeResolverConfiguration = new JavaTypeResolverConfiguration();

        abatorContext
                .setJavaTypeResolverConfiguration(javaTypeResolverConfiguration);

        NamedNodeMap nnm = node.getAttributes();

        Node attribute = nnm.getNamedItem("type"); //$NON-NLS-1$
        if (attribute != null) {
            javaTypeResolverConfiguration.setConfigurationType(attribute
                    .getNodeValue());
        }

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);

            if (childNode.getNodeType() != 1) {
                continue;
            }

            if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseProperty(javaTypeResolverConfiguration, childNode);
            }
        }
    }

    private void parseJavaModelGenerator(AbatorContext abatorContext, Node node) {
        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();

        abatorContext
                .setJavaModelGeneratorConfiguration(javaModelGeneratorConfiguration);

        NamedNodeMap nnm = node.getAttributes();

        Node attribute = nnm.getNamedItem("type"); //$NON-NLS-1$
        if (attribute != null) {
            javaModelGeneratorConfiguration.setConfigurationType(attribute
                    .getNodeValue());
        }

        attribute = nnm.getNamedItem("targetPackage"); //$NON-NLS-1$
        javaModelGeneratorConfiguration.setTargetPackage(attribute
                .getNodeValue());

        attribute = nnm.getNamedItem("targetProject"); //$NON-NLS-1$
        javaModelGeneratorConfiguration.setTargetProject(attribute
                .getNodeValue());

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);

            if (childNode.getNodeType() != 1) {
                continue;
            }

            if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseProperty(javaModelGeneratorConfiguration, childNode);
            }
        }
    }

    private void parseDaoGenerator(AbatorContext abatorContext, Node node) {
        DAOGeneratorConfiguration daoGeneratorConfiguration = new DAOGeneratorConfiguration();

        abatorContext.setDaoGeneratorConfiguration(daoGeneratorConfiguration);

        NamedNodeMap nnm = node.getAttributes();

        Node attribute = nnm.getNamedItem("type"); //$NON-NLS-1$
        daoGeneratorConfiguration
                .setConfigurationType(attribute.getNodeValue());

        attribute = nnm.getNamedItem("targetPackage"); //$NON-NLS-1$
        daoGeneratorConfiguration.setTargetPackage(attribute.getNodeValue());

        attribute = nnm.getNamedItem("targetProject"); //$NON-NLS-1$
        daoGeneratorConfiguration.setTargetProject(attribute.getNodeValue());

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);

            if (childNode.getNodeType() != 1) {
                continue;
            }

            if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseProperty(daoGeneratorConfiguration, childNode);
            }
        }
    }

    private void parseJdbcConnection(AbatorContext abatorContext, Node node) {
        JDBCConnectionConfiguration jdbcConnectionConfiguration = new JDBCConnectionConfiguration();

        abatorContext
                .setJdbcConnectionConfiguration(jdbcConnectionConfiguration);

        NamedNodeMap nnm = node.getAttributes();

        Node attribute = nnm.getNamedItem("driverClass"); //$NON-NLS-1$
        jdbcConnectionConfiguration.setDriverClass(attribute.getNodeValue());

        attribute = nnm.getNamedItem("connectionURL"); //$NON-NLS-1$
        jdbcConnectionConfiguration.setConnectionURL(attribute.getNodeValue());

        attribute = nnm.getNamedItem("userId"); //$NON-NLS-1$
        if (attribute != null) {
            jdbcConnectionConfiguration.setUserId(attribute.getNodeValue());
        }

        attribute = nnm.getNamedItem("password"); //$NON-NLS-1$
        if (attribute != null) {
            jdbcConnectionConfiguration.setPassword(attribute.getNodeValue());
        }

        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);

            if (childNode.getNodeType() != 1) {
                continue;
            }

            if ("classPathEntry".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseClassPathEntry(jdbcConnectionConfiguration, childNode);
            } else if ("property".equals(childNode.getNodeName())) { //$NON-NLS-1$
                parseProperty(jdbcConnectionConfiguration, childNode);
            }
        }
    }

    private void parseClassPathEntry(
            JDBCConnectionConfiguration jdbcConnectionConfiguration, Node node) {
        NamedNodeMap nnm = node.getAttributes();

        jdbcConnectionConfiguration.addClasspathEntry(nnm.getNamedItem(
                "location").getNodeValue()); //$NON-NLS-1$
    }

    private void parseProperty(PropertyHolder propertyHolder, Node node) {
        NamedNodeMap nnm = node.getAttributes();

        String name = nnm.getNamedItem("name").getNodeValue(); //$NON-NLS-1$
        String value = nnm.getNamedItem("value").getNodeValue(); //$NON-NLS-1$

        propertyHolder.addProperty(name, value);
    }
}
