﻿#region Apache Notice
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
    public partial class XmlConfigProcessor
    {
        /// <summary>
        /// Processes the type alias element.
        /// </summary>
        /// <param name="element">The element.</param>
        /// <param name="configurationStore">The configuration store.</param>
        private void ProcessTypeAliasElement(Tag element, IConfigurationStore configurationStore)
        {
            IConfiguration config = new MutableConfiguration(
                element.Name,
                element.Attributes[ConfigConstants.ATTRIBUTE_ALIAS],
                element.Attributes[ConfigConstants.ATTRIBUTE_TYPE]);

            configurationStore.AddAliasConfiguration(config);
        }
    }
}