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
package org.apache.ibatis.abator.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.abator.api.DAOGenerator;
import org.apache.ibatis.abator.api.IntrospectedTable;
import org.apache.ibatis.abator.api.JavaModelGenerator;
import org.apache.ibatis.abator.api.JavaTypeResolver;
import org.apache.ibatis.abator.api.ProgressCallback;
import org.apache.ibatis.abator.api.SqlMapGenerator;
import org.apache.ibatis.abator.api.dom.xml.Attribute;
import org.apache.ibatis.abator.api.dom.xml.XmlElement;
import org.apache.ibatis.abator.exception.InvalidConfigurationException;
import org.apache.ibatis.abator.internal.AbatorObjectFactory;
import org.apache.ibatis.abator.internal.NullProgressCallback;
import org.apache.ibatis.abator.internal.db.ConnectionFactory;
import org.apache.ibatis.abator.internal.db.DatabaseIntrospector;
import org.apache.ibatis.abator.internal.util.StringUtility;
import org.apache.ibatis.abator.internal.util.messages.Messages;

/**
 * @author Jeff Butler
 */
public class AbatorContext extends PropertyHolder {
    private String id;
    
	private JDBCConnectionConfiguration jdbcConnectionConfiguration;

	private SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration;

	private JavaTypeResolverConfiguration javaTypeResolverConfiguration;

	private JavaModelGeneratorConfiguration javaModelGeneratorConfiguration;

	private DAOGeneratorConfiguration daoGeneratorConfiguration;

	private ArrayList tableConfigurations;
    
    private GeneratorSet generatorSet;
    
    private ModelType defaultModelType;
    
    private String configuredGeneratorSet;
    
    private char beginingDelimiter = '\"';
	
    private char endingDelimiter = '\"';
    
    /**
     * Constructs an AbatorContext object.
     * 
     * @param generatorSetType - may be null
     * @param defaultModelType - may be null
     */
    public AbatorContext(String generatorSetType, ModelType defaultModelType) {
        super();
        
        this.configuredGeneratorSet = generatorSetType;
        
        if (defaultModelType == null) {
            this.defaultModelType = ModelType.CONDITIONAL;
        } else {
            this.defaultModelType = defaultModelType;
        }
        
        if (generatorSetType == null) {
            // TODO - make Java2 the default in some future release
            generatorSet = new LegacyGeneratorSet();
        } else if ("Legacy".equalsIgnoreCase(generatorSetType)) { //$NON-NLS-1$
            generatorSet = new LegacyGeneratorSet();
        } else if ("Java2".equalsIgnoreCase(generatorSetType)) { //$NON-NLS-1$
            generatorSet = new Java2GeneratorSet();
        } else if ("Java5".equalsIgnoreCase(generatorSetType)) { //$NON-NLS-1$
            generatorSet = new Java5GeneratorSet();
        } else {
            generatorSet = (GeneratorSet) AbatorObjectFactory.createObject(generatorSetType);
        }
        
		tableConfigurations = new ArrayList();
    }

	public void addTableConfiguration(TableConfiguration tc) {
		tableConfigurations.add(tc);
	}

	public JDBCConnectionConfiguration getJdbcConnectionConfiguration() {
		return jdbcConnectionConfiguration;
	}

	public DAOGeneratorConfiguration getDaoGeneratorConfiguration() {
		return daoGeneratorConfiguration;
	}

	public JavaModelGeneratorConfiguration getJavaModelGeneratorConfiguration() {
		return javaModelGeneratorConfiguration;
	}

	public JavaTypeResolverConfiguration getJavaTypeResolverConfiguration() {
		return javaTypeResolverConfiguration;
	}

	public SqlMapGeneratorConfiguration getSqlMapGeneratorConfiguration() {
		return sqlMapGeneratorConfiguration;
	}

	/**
	 * This method does a simple validate, it makes sure that all required
	 * fields have been filled in and that all implementation classes exist and
	 * are of the proper type. It does not do any more complex operations such
	 * as: Validating that database tables exist or Validating that named
	 * columns exist
	 */
	public void validate(List errors) {
		validateJdbcConnectionConfiguration(errors);

        if (javaModelGeneratorConfiguration == null) {
            errors.add(Messages.getString("ValidationError.8")); //$NON-NLS-1$
        } else {
            if (!StringUtility.stringHasValue(javaModelGeneratorConfiguration.getTargetProject())) {
                errors.add(Messages.getString("ValidationError.0", id)); //$NON-NLS-1$
            }

            if (!StringUtility.stringHasValue(javaModelGeneratorConfiguration.getTargetPackage())) {
                errors.add(Messages.getString("ValidationError.12", //$NON-NLS-1$
                        "JavaModelGenerator", id)); //$NON-NLS-1$
            }
        }

        if (sqlMapGeneratorConfiguration == null) {
            errors.add(Messages.getString("ValidationError.9")); //$NON-NLS-1$
        } else {
            if (!StringUtility.stringHasValue(sqlMapGeneratorConfiguration.getTargetProject())) {
                errors.add(Messages.getString("ValidationError.1", id)); //$NON-NLS-1$
            }

            if (!StringUtility.stringHasValue(sqlMapGeneratorConfiguration.getTargetPackage())) {
                errors.add(Messages.getString("ValidationError.12", //$NON-NLS-1$
                        "SQLMapGenerator", id)); //$NON-NLS-1$
            }
		}

		if (daoGeneratorConfiguration != null) {
			if (!StringUtility.stringHasValue(daoGeneratorConfiguration.getTargetProject())) {
				errors.add(Messages.getString("ValidationError.2", id)); //$NON-NLS-1$
			}

            if (!StringUtility.stringHasValue(daoGeneratorConfiguration.getTargetPackage())) {
                errors.add(Messages.getString("ValidationError.12", //$NON-NLS-1$
                        "DAOGenerator", id)); //$NON-NLS-1$
            }
		}
        
		if (tableConfigurations.size() == 0) {
			errors.add(Messages.getString("ValidationError.3")); //$NON-NLS-1$
		} else {
			for (int i = 0; i < tableConfigurations.size(); i++) {
				TableConfiguration tc = (TableConfiguration) tableConfigurations
						.get(i);

				validateTableConfiguration(tc, errors, i);
			}
		}
	}
	
	private void validateJdbcConnectionConfiguration(List errors) {
        if (jdbcConnectionConfiguration == null) {
            errors.add(Messages.getString("ValidationError.10")); //$NON-NLS-1$
            return;
        }
        
		if (!StringUtility.stringHasValue(jdbcConnectionConfiguration
				.getDriverClass())) {
			errors.add(Messages.getString("ValidationError.4")); //$NON-NLS-1$
		}

		if (!StringUtility.stringHasValue(jdbcConnectionConfiguration
				.getConnectionURL())) {
			errors.add(Messages.getString("ValidationError.5")); //$NON-NLS-1$
		}
	}

	private void validateTableConfiguration(TableConfiguration tc, List errors,
			int listPosition) {
        if (!StringUtility.stringHasValue(tc.getTableName())) {
			errors.add(Messages.getString("ValidationError.6", Integer.toString(listPosition))); //$NON-NLS-1$
		}

		if (tc.getGeneratedKey() != null
				&& !StringUtility.stringHasValue(tc.getGeneratedKey()
						.getRuntimeSqlStatement())) {
            String tableName = StringUtility.composeFullyQualifiedTableName(
                    tc.getCatalog(), tc.getSchema(), tc.getTableName(), '.');
	        errors
				.add(Messages.getString("ValidationError.7",  //$NON-NLS-1$
						tableName));
		}
	}

	/**
	 * Generate iBATIS artifacts based on the configuration specified in the
	 * constructor.  This method is long running.
	 * 
	 * @param callback a progress callback if progress information is desired, or <code>null</code>
	 * @param generatedJavaFiles any Java file generated from this method will be added to the List
	 *                            The objects will be of type GeneratedJavaFile.
	 * @param generatedXmlFiles any XML file generated from this method will be added to the List.
	 *                            The objects will be of type GeneratedXMLFile.
	 * @param warnings any warning generated from this method will be added to the List.  Warnings
	 *                   are always Strings.
	 * 
	 * @throws InvalidConfigurationException if some configuration error prevents
	 *                                       continuation
	 * @throws SQLException if some error arrises while introspecting the specified
	 *                      database tables.
	 * 
	 * @throws InterruptedException if the progress callback reports a cancel
	 */
	public void generateFiles(ProgressCallback callback, List generatedJavaFiles, List generatedXmlFiles, List warnings) throws InvalidConfigurationException,
			SQLException, InterruptedException {
	    
	    if (callback == null) {
	        callback = new NullProgressCallback();
	    }
	    
	    JavaTypeResolver javaTypeResolver = AbatorObjectFactory.createJavaTypeResolver(this, warnings);
	    JavaModelGenerator javaModelGenerator = AbatorObjectFactory.createJavaModelGenerator(this, warnings);
	    SqlMapGenerator sqlMapGenerator = AbatorObjectFactory.createSqlMapGenerator(this, javaModelGenerator, warnings);
	    DAOGenerator daoGenerator = AbatorObjectFactory.createDAOGenerator(this, javaModelGenerator, sqlMapGenerator, warnings);

		Connection connection = null;
		
		try {
			callback.startSubTask(Messages.getString("Progress.0")); //$NON-NLS-1$
			connection = getConnection();
            
            DatabaseIntrospector databaseIntrospector =
                new DatabaseIntrospector(this, connection.getMetaData(), javaTypeResolver, warnings);
			
			Iterator iter = tableConfigurations.iterator();
			while (iter.hasNext()) {
				TableConfiguration tc = (TableConfiguration) iter.next();
				String tableName = StringUtility.composeFullyQualifiedTableName(
                        tc.getCatalog(), tc.getSchema(), tc.getTableName(), '.');
				
				if (!tc.areAnyStatementsEnabled()) {
				    warnings.add(Messages.getString("Warning.0", tableName)); //$NON-NLS-1$
				    continue;
				}
				

				Collection introspectedTables;
				callback.startSubTask(Messages.getString("Progress.1", tableName)); //$NON-NLS-1$
                introspectedTables  = databaseIntrospector.introspectTables(tc);
				callback.checkCancel();
                
                Iterator iter2 = introspectedTables.iterator();
                while (iter2.hasNext()) {
                    callback.checkCancel();
                    IntrospectedTable introspectedTable = (IntrospectedTable) iter2.next();

                    if (daoGenerator != null) {
                        generatedJavaFiles.addAll(daoGenerator.getGeneratedJavaFiles(introspectedTable, callback));
                    }
                    generatedJavaFiles.addAll(javaModelGenerator.getGeneratedJavaFiles(introspectedTable, callback));
                    generatedXmlFiles.addAll(sqlMapGenerator.getGeneratedXMLFiles(introspectedTable, callback));
                }
			}
		} finally {
			closeConnection(connection);
			callback.finished();
		}
	}
	
	public int getTotalSteps() {
	    int steps = 0;
	    
	    steps++;  // connect to database
	    steps += tableConfigurations.size() * 8;
	    
	    return steps;
	}

	private Connection getConnection() throws SQLException {
		Connection connection = ConnectionFactory.getInstance().
				getConnection(jdbcConnectionConfiguration);

		return connection;
	}

	private void closeConnection(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				// ignore
				;
			}
		}
	}
	
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public GeneratorSet getGeneratorSet() {
        return generatorSet;
    }

    public void setDaoGeneratorConfiguration(
            DAOGeneratorConfiguration daoGeneratorConfiguration) {
        this.daoGeneratorConfiguration = daoGeneratorConfiguration;
    }

    public void setJavaModelGeneratorConfiguration(
            JavaModelGeneratorConfiguration javaModelGeneratorConfiguration) {
        this.javaModelGeneratorConfiguration = javaModelGeneratorConfiguration;
    }

    public void setJavaTypeResolverConfiguration(
            JavaTypeResolverConfiguration javaTypeResolverConfiguration) {
        this.javaTypeResolverConfiguration = javaTypeResolverConfiguration;
    }

    public void setJdbcConnectionConfiguration(
            JDBCConnectionConfiguration jdbcConnectionConfiguration) {
        this.jdbcConnectionConfiguration = jdbcConnectionConfiguration;
    }

    public void setSqlMapGeneratorConfiguration(
            SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration) {
        this.sqlMapGeneratorConfiguration = sqlMapGeneratorConfiguration;
    }

    public ModelType getDefaultModelType() {
        return defaultModelType;
    }

    /**
     * Builds an XmlElement representation of this context.  Note that the
     * XML may not necessarity validate if the context is invalid.  Call the
     * <code>validate</code> method to check validity of this context.
     * 
     * @return the XML representation of this context
     */
    public XmlElement toXmlElement() {
        XmlElement xmlElement = new XmlElement("abatorContext"); //$NON-NLS-1$
        
        if (defaultModelType != ModelType.CONDITIONAL) {
            xmlElement.addAttribute(new Attribute("defaultModelType", defaultModelType.getModelType())); //$NON-NLS-1$
        }
        
        if (StringUtility.stringHasValue(configuredGeneratorSet)) {
            xmlElement.addAttribute(new Attribute("generatorSet", configuredGeneratorSet)); //$NON-NLS-1$
        }
        
        addPropertyXmlElements(xmlElement);
        
        if (jdbcConnectionConfiguration != null) {
            xmlElement.addElement(jdbcConnectionConfiguration.toXmlElement());
        }
        
        if (javaTypeResolverConfiguration != null) {
            xmlElement.addElement(javaTypeResolverConfiguration.toXmlElement());
        }
        
        if (javaModelGeneratorConfiguration != null) {
            xmlElement.addElement(javaModelGeneratorConfiguration.toXmlElement());
        }
        
        if (sqlMapGeneratorConfiguration != null) {
            xmlElement.addElement(sqlMapGeneratorConfiguration.toXmlElement());
        }
        
        if (daoGeneratorConfiguration != null) {
            xmlElement.addElement(daoGeneratorConfiguration.toXmlElement());
        }
        
        Iterator iter = tableConfigurations.iterator();
        while (iter.hasNext()) {
            TableConfiguration tableConfiguration = (TableConfiguration) iter.next();
            xmlElement.addElement(tableConfiguration.toXmlElement());
        }
        
        return xmlElement;
    }

    public List getTableConfigurations() {
        return tableConfigurations;
    }
}
