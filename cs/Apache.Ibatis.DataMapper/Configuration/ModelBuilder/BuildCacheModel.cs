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

using System;
using Apache.Ibatis.Common.Configuration;
using Apache.Ibatis.Common.Data;

using Apache.Ibatis.DataMapper.Configuration.Serializers;
using Apache.Ibatis.DataMapper.Configuration.Interpreters.Config.Xml;
using Apache.Ibatis.DataMapper.Model.Cache;
using Apache.Ibatis.DataMapper.Configuration.Interpreters.Config;
using Apache.Ibatis.Common.Exceptions;
using System.Collections;
using System.Collections.Specialized;

namespace Apache.Ibatis.DataMapper.Configuration
{
    public partial class DefaultModelBuilder
    {

        /// <summary>
        /// Builds the cache model.
        /// </summary>
        /// <param name="store">The store.</param>
        private void BuildCacheModels(IConfigurationStore store)
        {
            foreach (IConfiguration cacheModelConfig in store.CacheModels)
            {
                IDictionary properties = GetProperties(cacheModelConfig);

                CacheModel cacheModel = CacheModelDeSerializer.Deserialize(cacheModelConfig, properties, modelStore.DataExchangeFactory.TypeHandlerFactory);

                string nameSpace = ConfigurationUtils.GetMandatoryStringAttribute(cacheModelConfig, ConfigConstants.ATTRIBUTE_NAMESPACE);

                // Gets all the flush on excecute statement id
                ConfigurationCollection flushConfigs = cacheModelConfig.Children.Find(ConfigConstants.ELEMENT_FLUSHONEXECUTE);
                foreach (IConfiguration flushOnExecute in flushConfigs)
                {
                    string statementId= flushOnExecute.Attributes[ConfigConstants.ATTRIBUTE_STATEMENT];
                    if (useStatementNamespaces)
                    {
                        statementId = ApplyNamespace(nameSpace, statementId);
                    }

                    cacheModel.StatementFlushNames.Add(statementId);
                }

                cacheModel.FlushInterval = BuildFlushInterval(cacheModelConfig);

                modelStore.AddCacheModel(cacheModel);
            }
        }

        /// <summary>
        /// Gets the cacheModel properties.
        /// </summary>
        /// <param name="cacheModelConfiguration">The cache model configuration.</param>
        /// <returns></returns>
        private IDictionary GetProperties(IConfiguration cacheModelConfiguration)
        {
            IDictionary properties = new HybridDictionary();

            // Get Properties 
            ConfigurationCollection propertiesConfigs = cacheModelConfiguration.Children.Find(ConfigConstants.ELEMENT_PROPERTY);
            foreach (IConfiguration propertie in propertiesConfigs)
            {
                string name = propertie.Attributes[ConfigConstants.ATTRIBUTE_NAME];
                string value = propertie.Attributes[ConfigConstants.ATTRIBUTE_VALUE];

                properties.Add(name, value);
            }

            return properties;
        }

        /// <summary>
        /// Builds the flush interval.
        /// </summary>
        /// <param name="cacheModelConfiguration">The cache model configuration.</param>
        private FlushInterval BuildFlushInterval(IConfiguration cacheModelConfiguration)
        {
            FlushInterval flushInterval = null;
            ConfigurationCollection flushIntervalConfigs = cacheModelConfiguration.Children.Find(ConfigConstants.ELEMENT_FLUSHINTERVAL);

            if (flushIntervalConfigs.Count > 0)
            {
                int hours = ConfigurationUtils.GetIntAttribute(flushIntervalConfigs[0].Attributes, "hours", 0);
                int minutes = ConfigurationUtils.GetIntAttribute(flushIntervalConfigs[0].Attributes, "minutes", 0);
                int seconds = ConfigurationUtils.GetIntAttribute(flushIntervalConfigs[0].Attributes, "seconds", 0);
                int milliseconds = ConfigurationUtils.GetIntAttribute(flushIntervalConfigs[0].Attributes, "milliseconds", 0);

                flushInterval = new FlushInterval(hours, minutes, seconds, milliseconds);
            }

            return flushInterval;
        }
    }
}
