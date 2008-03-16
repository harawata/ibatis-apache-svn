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
        /// Processes the dynamic elements.
        /// </summary>
        /// <param name="element">The element.</param>
        /// <param name="configurationStore">The configuration store.</param>
        private void ProcessDynamicElement(Tag element, IConfigurationStore configurationStore)
        {
            if (element.Parent != null)
            {
                MutableConfiguration config = new MutableConfiguration(element.Name);
                config.CreateAttributes(element.Attributes);
                config.Parent = element.Parent.Configuration;

                element.Parent.Configuration.Children.Add(config);
                element.Configuration = config;
            }
        }

    }
}
