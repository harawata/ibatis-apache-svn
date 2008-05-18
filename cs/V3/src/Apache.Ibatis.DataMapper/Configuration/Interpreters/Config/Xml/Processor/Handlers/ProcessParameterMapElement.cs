#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: 591621 $
 * $Date$
 * 
 * iBATIS.NET Data Mapper
 * Copyright (C) 2004 - Gilles Bayon
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

namespace Apache.Ibatis.DataMapper.Configuration.Interpreters.Config.Xml.Processor
{
    public partial class XmlMappingProcessor
    {
        /// <summary>
        /// Processes a parameter map element.
        /// </summary>
        /// <param name="element">The element.</param>
        /// <param name="configurationStore">The configuration store.</param>
        private void ProcessParameterMapElement(Tag element, IConfigurationStore configurationStore)
        {
            MutableConfiguration config = null;

            if (element.Attributes.ContainsKey(ConfigConstants.ATTRIBUTE_CLASS))
            {
                config = new MutableConfiguration(
                        element.Name,
                        ApplyNamespace(element.Attributes[ConfigConstants.ATTRIBUTE_ID]),
                        element.Attributes[ConfigConstants.ATTRIBUTE_CLASS]);
            }
            else
            {
                config = new MutableConfiguration(
                        element.Name,
                        ApplyNamespace(element.Attributes[ConfigConstants.ATTRIBUTE_ID]));
            }
            config.CreateAttributes(element.Attributes);
            config.CreateAttribute(ConfigConstants.ATTRIBUTE_NAMESPACE, nameSpace);

            if (config.Attributes.ContainsKey(ConfigConstants.ATTRIBUTE_EXTENDS))
            {
                config.Attributes[ConfigConstants.ATTRIBUTE_EXTENDS] =
                    ApplyNamespace(config.Attributes[ConfigConstants.ATTRIBUTE_EXTENDS]);
            }

            configurationStore.AddParameterMapConfiguration(config);
            element.Configuration = config;
        }

    }
}
