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
package org.apache.ibatis.abator.core.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.abator.core.api.DAOGenerator;
import org.apache.ibatis.abator.core.api.GeneratedJavaFile;
import org.apache.ibatis.abator.core.api.GeneratedXmlFile;
import org.apache.ibatis.abator.core.api.JavaModelGenerator;
import org.apache.ibatis.abator.core.api.JavaTypeResolver;
import org.apache.ibatis.abator.core.api.ProgressCallback;
import org.apache.ibatis.abator.core.api.SqlMapGenerator;
import org.apache.ibatis.abator.core.exception.InvalidConfigurationException;
import org.apache.ibatis.abator.core.exception.UnknownTableException;
import org.apache.ibatis.abator.core.internal.AbatorObjectFactory;
import org.apache.ibatis.abator.core.internal.NullProgressCallback;
import org.apache.ibatis.abator.core.internal.db.ColumnDefinitions;
import org.apache.ibatis.abator.core.internal.db.ConnectionFactory;
import org.apache.ibatis.abator.core.internal.db.DatabaseIntrospector;
import org.apache.ibatis.abator.core.internal.util.StringUtility;

/**
 * @author Jeff Butler
 */
public class AbatorContext {
    private String id;
    
	private JDBCConnectionConfiguration jdbcConnectionConfiguration;

	private SqlMapGeneratorConfiguration sqlMapGeneratorConfiguration;

	private JavaTypeResolverConfiguration javaTypeResolverConfiguration;

	private JavaModelGeneratorConfiguration javaModelGeneratorConfiguration;

	private DAOGeneratorConfiguration daoGeneratorConfiguration;

	private ArrayList tableConfigurations;
	
    /**
     * 
     */
    public AbatorContext() {
        super();
		jdbcConnectionConfiguration = new JDBCConnectionConfiguration();
		sqlMapGeneratorConfiguration = new SqlMapGeneratorConfiguration();
		javaTypeResolverConfiguration = new JavaTypeResolverConfiguration();
		javaModelGeneratorConfiguration = new JavaModelGeneratorConfiguration();
		daoGeneratorConfiguration = new DAOGeneratorConfiguration();
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
	 * This method does a simple validate - it makes sure that all required
	 * fields have been filled in and that all implementation classes exist and
	 * are of the proper type. It does not do any more complex operations such
	 * as: - Validating that database tables exist - Validating that named
	 * columns exist
	 */
	public void validate(List errors) {
		validateJdbcConnectionConfiguration(errors);

		if (!StringUtility.stringHasValue(javaModelGeneratorConfiguration.getTargetProject())) {
			errors.add("Java Model Target Project is Required for context " + id);
		}

		if (!StringUtility.stringHasValue(sqlMapGeneratorConfiguration.getTargetProject())) {
			errors.add("SQL Map Generator Target Project is Required for context " + id);
		}

		if (daoGeneratorConfiguration.isEnabled()) {
			if (!StringUtility.stringHasValue(daoGeneratorConfiguration.getTargetProject())) {
				errors.add("DAO Generator Target Project is Required for context " + id);
			}
		}
		
		if (tableConfigurations.size() == 0) {
			errors.add("No Tables Specified");
		} else {
			for (int i = 0; i < tableConfigurations.size(); i++) {
				TableConfiguration tc = (TableConfiguration) tableConfigurations
						.get(i);

				validateTableConfiguration(tc, errors, i);
			}
		}
	}
	
	private void validateJdbcConnectionConfiguration(List errors) {
		if (!StringUtility.stringHasValue(jdbcConnectionConfiguration
				.getDriverClass())) {
			errors.add("JDBC Driver Class Must Be Specified");
		}

		if (!StringUtility.stringHasValue(jdbcConnectionConfiguration
				.getConnectionURL())) {
			errors.add("JDBC Connection URL Must Be Specified");
		}
	}

	private void validateTableConfiguration(TableConfiguration tc, List errors,
			int listPosition) {
		if (!StringUtility.stringHasValue(tc.getTable().getTableName())) {
			errors.add("Missing table name in table configuration at index "
					+ listPosition);
		}

		if (tc.getGeneratedKey().isConfigured()
				&& !StringUtility.stringHasValue(tc.getGeneratedKey()
						.getSqlStatement())) {
	        errors
				.add("SQL Statement is required if a generated key is specified in table configuration for table "
						+ tc.getTable().getFullyQualifiedTableName());
		}
	}

	/**
	 * Generate iBATIS artifacts based on the configuration specified in the
	 * constructor.  This method is long running.
	 * 
	 * @param callback - a progress callback if progress information is desired, or <code>null</code>
	 * @param generatedJavaFiles - any Java file generated from this method will be added to the List
	 *                            The objects will be of type GeneratedJavaFile.
	 * @param generatedXmlFiles - any XML file generated from this method will be added to the List.
	 *                            The objects will be of type GeneratedXMLFile.
	 * @param warnings - any warning generated from this method will be added to the List.  Warnings
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
	    
	    JavaTypeResolver javaTypeResolver = AbatorObjectFactory.createJavaTypeResolver(javaTypeResolverConfiguration);
	    JavaModelGenerator javaModelGenerator = AbatorObjectFactory.createJavaModelGenerator(javaModelGeneratorConfiguration);
	    SqlMapGenerator sqlMapGenerator = AbatorObjectFactory.createSqlMapGenerator(sqlMapGeneratorConfiguration, javaModelGenerator);
	    DAOGenerator daoGenerator = AbatorObjectFactory.createDAOGenerator(daoGeneratorConfiguration, javaModelGenerator, sqlMapGenerator);

		Connection connection = null;
		
		try {
			callback.setTaskName("Connecting to the Database");
			connection = getConnection();
			
			Iterator iter = tableConfigurations.iterator();
			while (iter.hasNext()) {
				TableConfiguration tc = (TableConfiguration) iter.next();
				String tableName = tc.getTable().toString();

				ColumnDefinitions columnDefinitions;
				try {
					callback.setTaskName("Introspecting table " + tableName);
					columnDefinitions  = DatabaseIntrospector.generateColumnDefinitions(connection, tc, javaTypeResolver, warnings);
					callback.checkCancel();
				} catch (UnknownTableException e) {
					StringBuffer sb = new StringBuffer();
					sb.append("Table ");
					sb.append(tc.getTable().getFullyQualifiedTableName());
					sb.append(" does not exist, or contains only LOB fields");

					warnings.add(sb.toString());
					continue;
				}

				GeneratedJavaFile gjf;
				
				if (daoGenerator != null) {
				    callback.setTaskName("Generating DAO Implementation for table " + tableName);
				    gjf = daoGenerator.getDAOImplementation(columnDefinitions, tc);
				    generatedJavaFiles.add(gjf);

				    callback.setTaskName("Generating DAO Interface for table " + tableName);
				    gjf = daoGenerator.getDAOInterface(columnDefinitions, tc);
				    generatedJavaFiles.add(gjf);
				}

				callback.setTaskName("Generating Example class for table " + tableName);
				gjf = javaModelGenerator.getExample(columnDefinitions, tc);
				if (gjf != null) {
					generatedJavaFiles.add(gjf);
				}

				callback.setTaskName("Generating Primary Key class for table " + tableName);
				gjf = javaModelGenerator.getPrimaryKey(columnDefinitions, tc);
				if (gjf != null) {
					generatedJavaFiles.add(gjf);
				}

				callback.setTaskName("Generating Record Class for table " + tableName);
				gjf = javaModelGenerator.getRecord(columnDefinitions, tc);
				if (gjf != null) {
					generatedJavaFiles.add(gjf);
				}

				callback.setTaskName("Generating Record Class(With BLOBs) for table " + tableName);
				gjf = javaModelGenerator.getRecordWithBLOBs(columnDefinitions, tc);
				if (gjf != null) {
					generatedJavaFiles.add(gjf);
				}
				
				callback.setTaskName("Generating SQL Map for table " + tableName);
				GeneratedXmlFile gxf = sqlMapGenerator.getSqlMap(columnDefinitions, tc);
				if (gxf != null) {
					generatedXmlFiles.add(gxf);
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
}