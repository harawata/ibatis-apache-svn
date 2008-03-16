#region Apache Notice
/*****************************************************************************
 * $Revision: 408099 $
 * $LastChangedDate$
 * $LastChangedBy$
 * 
 * iBATIS.NET Data Mapper
 * Copyright (C) 2006/2005 - The Apache Software Foundation
 *  
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 ********************************************************************************/
#endregion

using Apache.Ibatis.Common.Configuration;
using Apache.Ibatis.DataMapper.Model.Statements;
using Apache.Ibatis.DataMapper.Configuration.Interpreters.Config;
using Apache.Ibatis.DataMapper.Configuration.Serializers;
using Apache.Ibatis.Common.Exceptions;
using Apache.Ibatis.DataMapper.MappedStatements;
using System.Text;
using Apache.Ibatis.DataMapper.Model.Sql.Dynamic;
using Apache.Ibatis.DataMapper.Model.Sql.Dynamic.Elements;
using Apache.Ibatis.DataMapper.Model.ParameterMapping;
using System;
using Apache.Ibatis.DataMapper.DataExchange;
using Apache.Ibatis.DataMapper.Model.Sql;
using Apache.Ibatis.DataMapper.Model.Sql.SimpleDynamic;
using Apache.Ibatis.DataMapper.Model.Sql.Static;
using Apache.Ibatis.DataMapper.Session;
using Apache.Ibatis.Common.Contracts;


namespace Apache.Ibatis.DataMapper.Configuration
{
    /// <summary>
    /// This implementation of <see cref="IConfigurationStore"/>, builds all statement.
    /// </summary>
    public partial class DefaultModelBuilder
    {
        private readonly InlineParameterMapParser paramParser = new InlineParameterMapParser();

        /// <summary>
        /// Builds the mapped statements.
        /// </summary>
        /// <param name="store">The store.</param>
        private void BuildMappedStatements(IConfigurationStore store)
        {
            foreach (IConfiguration statementConfig in store.Statements)
            {
                IMappedStatement mappedStatement = null;

                switch (statementConfig.Type)
                {
                    case ConfigConstants.ELEMENT_STATEMENT:
                        mappedStatement = BuildStatement(statementConfig);
                        break;
                    case ConfigConstants.ELEMENT_SELECT:
                        mappedStatement = BuildSelect(statementConfig);
                        break;
                    case ConfigConstants.ELEMENT_INSERT:
                        mappedStatement = BuildInsert(statementConfig);
                        break;
                    case ConfigConstants.ELEMENT_UPDATE:
                        mappedStatement = BuildUpdate(statementConfig);
                        break;
                    case ConfigConstants.ELEMENT_DELETE:
                        mappedStatement = BuildDelete(statementConfig);
                        break;
                    case ConfigConstants.ELEMENT_PROCEDURE:
                        mappedStatement = BuildProcedure(statementConfig);
                        break;
                    case ConfigConstants.ELEMENT_SQL:
                        break;
                    default:
                        throw new ConfigurationException("Cannot build the statement, cause invalid statement type '" + statementConfig.Type + "'.");

                }
                if (mappedStatement!=null)
                {
                    modelStore.AddMappedStatement(mappedStatement);
                }
            }
        }

        /// <summary>
        /// Builds a Mapped Statement for a statement.
        /// </summary>
        /// <param name="statement">The statement.</param>
        /// <param name="mappedStatement">The mapped statement.</param>
        /// <returns></returns>
        private IMappedStatement BuildCachingStatement(IStatement statement, MappedStatement mappedStatement)
        {
            IMappedStatement mapStatement = mappedStatement;
            if (statement.CacheModel != null && isCacheModelsEnabled)
            {
                mapStatement = new CachingStatement(mappedStatement);
            }
            return mapStatement;
        }

        /// <summary>
        /// Builds a <see cref="Statement"/> for a statement configuration.
        /// </summary>
        /// <param name="statementConfig">The statement config.</param>
        private IMappedStatement BuildStatement(IConfiguration statementConfig)
        {
            BaseStatementDeSerializer statementDeSerializer = new StatementDeSerializer();
            IStatement statement = statementDeSerializer.Deserialize(modelStore, statementConfig);
            ProcessSqlStatement(statementConfig, statement);
            MappedStatement mappedStatement = new MappedStatement(modelStore, statement);

            return BuildCachingStatement(statement, mappedStatement);
        }

        /// <summary>
        /// Builds an <see cref="Insert"/> for a insert configuration.
        /// </summary>
        /// <param name="statementConfig">The statement config.</param>
        private IMappedStatement BuildInsert(IConfiguration statementConfig)
        {
            BaseStatementDeSerializer insertDeSerializer = new InsertDeSerializer();
            IStatement statement = insertDeSerializer.Deserialize(modelStore, statementConfig);
            ProcessSqlStatement(statementConfig, statement);
            MappedStatement mappedStatement = new InsertMappedStatement(modelStore, statement);
            Insert insert = (Insert)statement;
            if (insert.SelectKey != null)
            {
                ConfigurationCollection selectKeys = statementConfig.Children.Find(ConfigConstants.ELEMENT_SELECTKEY);
                IConfiguration selectKeyConfig = selectKeys[0];

                ProcessSqlStatement(selectKeyConfig, insert.SelectKey);
                MappedStatement mapStatement = new MappedStatement(modelStore, insert.SelectKey);
                modelStore.AddMappedStatement(mapStatement);
            }

            return BuildCachingStatement(statement, mappedStatement);
        }

        /// <summary>
        /// Builds an <see cref="Statement"/> for a statement configuration.
        /// </summary>
        /// <param name="statementConfig">The statement config.</param>
        private IMappedStatement BuildUpdate(IConfiguration statementConfig)
        {
            BaseStatementDeSerializer updateDeSerializer = new UpdateDeSerializer();
            IStatement statement = updateDeSerializer.Deserialize(modelStore, statementConfig);
            ProcessSqlStatement(statementConfig, statement);
            MappedStatement mappedStatement = new UpdateMappedStatement(modelStore, statement);

            return BuildCachingStatement(statement, mappedStatement);

        }

        /// <summary>
        /// Builds an <see cref="Delete"/> for a delete configuration.
        /// </summary>
        /// <param name="statementConfig">The statement config.</param>
        private IMappedStatement BuildDelete(IConfiguration statementConfig)
        {
            BaseStatementDeSerializer deleteDeSerializer = new DeleteDeSerializer();
            IStatement statement = deleteDeSerializer.Deserialize(modelStore, statementConfig);
            ProcessSqlStatement(statementConfig, statement);
            MappedStatement mappedStatement = new DeleteMappedStatement(modelStore, statement);

            return BuildCachingStatement(statement, mappedStatement);

        }

        /// <summary>
        /// Builds an <see cref="Select"/> for a select configuration.
        /// </summary>
        /// <param name="statementConfig">The statement config.</param>
        private IMappedStatement BuildSelect(IConfiguration statementConfig)
        {
            BaseStatementDeSerializer selectDeSerializer = new SelectDeSerializer();
            IStatement statement = selectDeSerializer.Deserialize(modelStore, statementConfig);
            ProcessSqlStatement(statementConfig, statement);
            MappedStatement mappedStatement = new SelectMappedStatement(modelStore, statement);

            return BuildCachingStatement(statement, mappedStatement);

        }

        /// <summary>
        /// Builds an <see cref="Procedure"/> for a procedure configuration.
        /// </summary>
        /// <param name="statementConfig">The statement config.</param>
        private IMappedStatement BuildProcedure(IConfiguration statementConfig)
        {
            BaseStatementDeSerializer procedureDeSerializer = new ProcedureDeSerializer();
            IStatement statement = procedureDeSerializer.Deserialize(modelStore, statementConfig);
            ProcessSqlStatement(statementConfig, statement);
            MappedStatement mappedStatement = new MappedStatement(modelStore, statement);

            return BuildCachingStatement(statement, mappedStatement);
        }

        /// <summary>
        /// Process the Sql cpmmand text statement (Build ISql)
        /// </summary>
        /// <param name="statementConfiguration">The statement configuration.</param>
        /// <param name="statement">The statement.</param>
        private void ProcessSqlStatement(IConfiguration statementConfiguration, IStatement statement)
        {
            bool isDynamic = false;

            DynamicSql dynamic = new DynamicSql(
                modelStore.SessionFactory.DataSource.DbProvider.UsePositionalParameters,
                modelStore.DBHelperParameterCache,
                modelStore.DataExchangeFactory,
                statement);
            StringBuilder sqlBuffer = new StringBuilder();

            isDynamic = ParseDynamicTags(statementConfiguration, dynamic, sqlBuffer, isDynamic, false, statement);

            if (isDynamic)
            {
                statement.Sql = dynamic;
            }
            else
            {
                string sqlText = sqlBuffer.ToString();
                ApplyInlineParemeterMap(statement, sqlText);
            }
            Contract.Ensure.That(statement.Sql, Is.Not.Null).When("process Sql statement.");
        }


        /// <summary>
        /// Parse dynamic tags
        /// </summary>
        /// <param name="statementConfig">The statement config.</param>
        /// <param name="dynamic">The dynamic.</param>
        /// <param name="sqlBuffer">The SQL buffer.</param>
        /// <param name="isDynamic">if set to <c>true</c> [is dynamic].</param>
        /// <param name="postParseRequired">if set to <c>true</c> [post parse required].</param>
        /// <param name="statement">The statement.</param>
        /// <returns></returns>
        private bool ParseDynamicTags(
            IConfiguration statementConfig, 
            IDynamicParent dynamic,
            StringBuilder sqlBuffer, 
            bool isDynamic, 
            bool postParseRequired, 
            IStatement statement)
        {
            ConfigurationCollection children = statementConfig.Children;
            int count = children.Count;
            for (int i = 0; i < count; i++)
            {
                IConfiguration child = children[i];
                if (child.Type == ConfigConstants.ELEMENT_TEXT || child.Type == ConfigConstants.ELEMENT_CDATA)
                {
                    SqlText sqlText = null;
                    if (postParseRequired)
                    {
                        sqlText = new SqlText();
                        sqlText.Text = child.Value;
                    }
                    else
                    {
                        sqlText = paramParser.ParseInlineParameterMap(modelStore.DataExchangeFactory, null, child.Value);
                    }

                    dynamic.AddChild(sqlText);
                    sqlBuffer.Append(" "+child.Value);
                }
                else if (child.Type == ConfigConstants.ELEMENT_SELECTKEY || child.Type == ConfigConstants.ELEMENT_INCLUDE)
                { }
                else
                {
                    IDeSerializer serializer = deSerializerFactory.GetDeSerializer(child.Type);

                    if (serializer != null)
                    {
                        isDynamic = true;
                        SqlTag tag;

                        tag = serializer.Deserialize(child);

                        dynamic.AddChild(tag);

                        if (child.Children.Count > 0)
                        {
                            isDynamic = ParseDynamicTags(child, tag, sqlBuffer, isDynamic, tag.Handler.IsPostParseRequired, statement);
                        }
                    }
                }
            }

            return isDynamic;
        }


        /// <summary>
        /// Apply inline paremeterMap
        /// </summary>
        /// <param name="statement"></param>
        /// <param name="sqlStatement"></param>
        private void ApplyInlineParemeterMap(IStatement statement, string sqlStatement)
        {
            string newSql = sqlStatement;

            // Check the inline parameter
            if (statement.ParameterMap == null)
            {
                // Build a Parametermap with the inline parameters.
                // if they exist. Then delete inline infos from sqltext.

                SqlText sqlText = paramParser.ParseInlineParameterMap(modelStore.DataExchangeFactory, statement, newSql);

                if (sqlText.Parameters.Length > 0)
                {
                    string id = statement.Id + "-InLineParameterMap";
                    string className = string.Empty;
                    Type classType = null;
                    IDataExchange dataExchange = null;

                    if (statement.ParameterClass != null)
                    {
                        className = statement.ParameterClass.Name;
                        classType = statement.ParameterClass;
                        dataExchange = modelStore.DataExchangeFactory.GetDataExchangeForClass(classType);
                    }

                    if (statement.ParameterClass == null &&
                        sqlText.Parameters.Length == 1 && sqlText.Parameters[0].PropertyName == "value")//#value# parameter with no parameterClass attribut
                    {
                        dataExchange = modelStore.DataExchangeFactory.GetDataExchangeForClass(typeof(int));//Get the primitiveDataExchange
                    }
                    else
                    {
                        dataExchange = modelStore.DataExchangeFactory.GetDataExchangeForClass(null);
                    }

                    ParameterMap map = new ParameterMap(
                        id,
                        className,
                        string.Empty,
                        classType,
                        dataExchange,
                        modelStore.SessionFactory.DataSource.DbProvider.UsePositionalParameters
                        )
                        ;
                    statement.ParameterMap = map;
                    int lenght = sqlText.Parameters.Length;
                    for (int index = 0; index < lenght; index++)
                    {
                        map.AddParameterProperty(sqlText.Parameters[index]);
                    }
                }
                newSql = sqlText.Text;
            }

            ISql sql = null;

            newSql = newSql.Trim();

            if (SimpleDynamicSql.IsSimpleDynamicSql(newSql))
            {
                sql = new SimpleDynamicSql(
                    modelStore.DataExchangeFactory, 
                    modelStore.DBHelperParameterCache,
                    newSql, 
                    statement);
            }
            else
            {
                if (statement is Procedure)
                {
                    sql = new ProcedureSql(
                        modelStore.DataExchangeFactory,
                        modelStore.DBHelperParameterCache,
                        newSql, 
                        statement);
                    // Could not call BuildPreparedStatement for procedure because when NUnit Test
                    // the database is not here (but in theory procedure must be prepared like statement)
                    // It's even better as we can then switch DataSource.
                }
                else if (statement is Statement)
                {
                    sql = new StaticSql(
                        modelStore.DataExchangeFactory,
                        modelStore.DBHelperParameterCache,
                        statement);
                    ISession session = modelStore.SessionFactory.OpenSession();

                    ((StaticSql)sql).BuildPreparedStatement(session, newSql);

                    session.Close();
                }
            }
            statement.Sql = sql;
        }

    }
}
