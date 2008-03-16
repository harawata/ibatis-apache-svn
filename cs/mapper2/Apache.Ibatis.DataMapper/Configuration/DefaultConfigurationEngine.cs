
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

using System.Collections.Generic;
using System.Reflection;
using Apache.Ibatis.Common.Configuration;
using Apache.Ibatis.Common.Contracts;
using Apache.Ibatis.Common.Exceptions;
using Apache.Ibatis.Common.Logging;
using Apache.Ibatis.DataMapper.Configuration.Interpreters.Config;
using Apache.Ibatis.DataMapper.Model;
using Apache.Ibatis.DataMapper.Configuration.Module;

namespace Apache.Ibatis.DataMapper.Configuration
{
    /// <summary>
    /// The default <see cref="IConfigurationEngine"/> implementation.
    /// </summary>
    /// <example>
    /// Use:
    /// 
    /// IConfigurationEngine engine = 
    ///     new DefaultConfigurationEngine( 
    ///             new XmlConfigurationInterpreter(
    ///                 new new FileResource("SqlMap.config") ) );
    /// </example>
    public class DefaultConfigurationEngine : IConfigurationEngine
    {
        private readonly IConfigurationStore configurationStore = null;
        private readonly ConfigurationSetting configurationSetting = null;
        private readonly IList<IModule> modules = null;
        private IModelStore modelStore = null;
        private IConfigurationInterpreter interpreter = null;

        private static readonly ILog logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        #region constructors

        /// <summary>
        /// Initializes a new instance of the <see cref="DefaultConfigurationEngine"/> class.
        /// </summary>
        public DefaultConfigurationEngine()
        {
            configurationStore = new DefaultConfigurationStore();
            configurationSetting = new ConfigurationSetting();
            modules = new List<IModule>();
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="DefaultConfigurationEngine"/> class.
        /// </summary>
        /// <param name="configurationSetting">The configuration setting.</param>
        public DefaultConfigurationEngine(ConfigurationSetting configurationSetting):this()
        {
            Contract.Require.That(configurationSetting, Is.Not.Null).When("retrieving argument ConfigurationSetting in DefaultConfigurationEngine constructor");

            this.configurationSetting = configurationSetting;

            IConfiguration config = new MutableConfiguration(
                ConfigConstants.ATTRIBUTE_VALIDATE_SQLMAP,
                ConfigConstants.ATTRIBUTE_VALIDATE_SQLMAP,
                configurationSetting.ValidateMapperConfigFile.ToString()
                );
            configurationStore.AddSettingConfiguration(config);

            if (configurationSetting.Properties.Count > 0)
            {
                IEnumerator<KeyValuePair<string, string>> properties = configurationSetting.Properties.GetEnumerator();
                while (properties.MoveNext())
                {
                    config = new MutableConfiguration(
                    ConfigConstants.ELEMENT_PROPERTY,
                    properties.Current.Key,
                    properties.Current.Value);
                    configurationStore.AddPropertyConfiguration(config);
                }
            }
        }
        #endregion

        #region IConfigurationEngine Members

        /// <summary>
        /// Gets the model store.
        /// </summary>
        /// <value>The model store.</value>
        public IModelStore ModelStore
        {
            get { return modelStore; }
        }

        /// <summary>
        /// Gets the configuration store.
        /// </summary>
        /// <value>The configuration store.</value>
        public IConfigurationStore ConfigurationStore
        {
            get { return configurationStore; }
        }

        /// <summary>
        /// Registers the <see cref="IConfigurationInterpreter"/>.
        /// </summary>
        /// <param name="interpreter">The interpreter.</param>
        public void RegisterInterpreter(IConfigurationInterpreter interpreter)
        {
            Contract.Require.That(interpreter, Is.Not.Null).When("retrieving argument interpreter in RegisterInterpreter method");

            this.interpreter = interpreter;
        }

        /// <summary>
        /// Add a module to the <see cref="IConfigurationEngine"/>.
        /// </summary>
        /// <param name="module">The module.</param>
        public void RegisterModule(IModule module)
        {
            Contract.Require.That(module, Is.Not.Null).When("retrieving argument module in RegisterModule method");
            modules.Add(module);
        }


        /// <summary>
        /// Builds the mapper factory.
        /// </summary>
        /// <returns>the mapper factory</returns>
        public IMapperFactory BuildMapperFactory()
        {
            // Registers file Xml, ... configuration element
            if (interpreter!=null)
            {
                interpreter.ProcessResource(configurationStore);
            }

            // Registers code configuration element
            foreach (IModule module in modules)
            {
                module.Configure(this);
            }

            // Process Extends ResultMap
            List<IConfiguration> resolved = new List<IConfiguration>();
            foreach (IConfiguration resultMap in configurationStore.ResultMaps)
            {
                ResolveExtendResultMap(resolved, resultMap);
            }

            // Process Extends ParameterMap
            resolved = new List<IConfiguration>();
            foreach (IConfiguration parameterMap in configurationStore.ParameterMaps)
            {
                ResolveExtendParameterMap(resolved, parameterMap);
            }

            // Process Include Sql statement
            foreach (IConfiguration statement in configurationStore.Statements)
            {
                ConfigurationCollection includes = statement.Children.RecursiveFind(ConfigConstants.ELEMENT_INCLUDE);

                if (includes.Count > 0)
                {
                    ResolveIncludeStatement(includes);
                }
            }

            // Process Extends statement
            resolved = new List<IConfiguration>();
            foreach (IConfiguration statement in configurationStore.Statements)
            {
                ResolveExtendStatement(resolved, statement);
            }

            modelStore = new DefaultModelStore();

            IModelBuilder builder = new DefaultModelBuilder(modelStore);

            builder.BuildModel(configurationSetting, configurationStore);

            IDataMapper dataMapper = new DataMapper(modelStore);

            return new DefaultMapperFactory(dataMapper);
        }

        #endregion

        private IConfiguration ResolveExtendResultMap(IList<IConfiguration> resolvedResultMap, IConfiguration resultMap)
        {
            if (resultMap.Attributes.ContainsKey(ConfigConstants.ATTRIBUTE_EXTENDS))
            {
                if (!resolvedResultMap.Contains(resultMap))
                {
                    resolvedResultMap.Add(resultMap);

                    // Find the extended resultMap
                    string extends = resultMap.Attributes[ConfigConstants.ATTRIBUTE_EXTENDS];

                    IConfiguration father = configurationStore.GetResultMapConfiguration(extends);
                    if (father == null)
                    {
                        throw new ConfigurationException("There's no extended resultMap named '" + extends + "' for the resultMap '" + resultMap.Id + "'");
                    }

                    father = ResolveExtendResultMap(resolvedResultMap, father);
                    resultMap.Children.AddRange(father.Children.Remove(ConfigConstants.ELEMENT_DISCRIMINATOR));   
 
                    // Copy the groupBy attribute
                    if (father.Attributes.ContainsKey(ConfigConstants.ATTRIBUTE_GROUPBY))
                    {
                        resultMap.Attributes[ConfigConstants.ATTRIBUTE_GROUPBY] = father.Attributes[ConfigConstants.ATTRIBUTE_GROUPBY];
                    }
                }
            }
            return resultMap;
        }

        private IConfiguration ResolveExtendParameterMap(IList<IConfiguration> resolvedParameterMap, IConfiguration parameterMap)
        {
            if (parameterMap.Attributes.ContainsKey(ConfigConstants.ATTRIBUTE_EXTENDS))
            {
                if (!resolvedParameterMap.Contains(parameterMap))
                {
                    resolvedParameterMap.Add(parameterMap);

                    // Find the extended resultMap
                    string extends = parameterMap.Attributes[ConfigConstants.ATTRIBUTE_EXTENDS];

                    IConfiguration father = configurationStore.GetParameterMapConfiguration(extends);
                    if (father == null)
                    {
                        throw new ConfigurationException("There's no extended parameterMap named '" + extends + "' for the parameterMap '" + parameterMap.Id + "'");
                    }
                    father = ResolveExtendParameterMap(resolvedParameterMap, father);
                    parameterMap.Children.InsertRange(0,father.Children);
                }
            }
            return parameterMap;
        }

        private void ResolveIncludeStatement(ConfigurationCollection includes)
        {
            foreach (IConfiguration include in includes)
            {
              IConfiguration toInclude =
                        configurationStore.GetStatementConfiguration(include.Id);
              if (toInclude == null)
              {
                  throw new ConfigurationException("There's no include statement named '" + include.Id + "'");
              }
                IConfiguration parent = include.Parent;
                int childIndex = include.Parent.Children.IndexOf(include);
                parent.Children.RemoveAt(childIndex);
                parent.Children.InsertRange(childIndex, toInclude.Children);
            }
        }

        private IConfiguration ResolveExtendStatement(IList<IConfiguration> resolvedStatement, IConfiguration statement)
        {
            if (statement.Attributes.ContainsKey(ConfigConstants.ATTRIBUTE_EXTENDS))
            {
                if (!resolvedStatement.Contains(statement))
                {
                    resolvedStatement.Add(statement);

                    // Find the extended resultMap
                    string extends = statement.Attributes[ConfigConstants.ATTRIBUTE_EXTENDS];
                    IConfiguration father = configurationStore.GetStatementConfiguration(extends);
                    if (father==null)
                    {
                        throw new ConfigurationException("There's no extended statement named '" + extends + "' for the statement '" + statement.Id+"'");
                    }
                    father = ResolveExtendStatement(resolvedStatement, father);
                    statement.Children.InsertRange(0, father.Children);
                }
            }
            return statement;
        }

    }
}
