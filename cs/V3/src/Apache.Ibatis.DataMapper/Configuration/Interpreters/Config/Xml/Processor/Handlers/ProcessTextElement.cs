﻿#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: 591621 $
 * $Date$
 * 
 * iBATIS.NET Data Mapper
 * Copyright (C) 2008/2005 - The Apache Software Foundation
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
using System.Text;
using System.Xml;

using Apache.Ibatis.Common.Configuration;
using Apache.Ibatis.Common.Resources;

namespace Apache.Ibatis.DataMapper.Configuration.Interpreters.Config.Xml.Processor
{
    public partial class XmlMappingProcessor
    {
        /// <summary>
        /// Processes the text element.
        /// </summary>
        /// <param name="element">The element.</param>
        /// <param name="configurationStore">The configuration store.</param>
        protected override void ProcessTextElement(Tag element, IConfigurationStore configurationStore)
        {
            if (element.Parent != null)
            {
                string text = element.Value.Replace('\n', ' ').Replace('\r', ' ').Replace('\t', ' ').Trim(); 

                MutableConfiguration config = new MutableConfiguration(
                                    element.Name,
                                    string.Empty,
                                   text);

                element.Parent.Configuration.Children.Add(config);
                element.Configuration = config;
            }
        }
    }
}
