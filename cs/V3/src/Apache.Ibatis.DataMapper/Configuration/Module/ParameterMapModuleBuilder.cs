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

using System;
using Apache.Ibatis.Common.Configuration;
using Apache.Ibatis.Common.Contracts;
using Apache.Ibatis.DataMapper.Configuration.Interpreters.Config;

namespace Apache.Ibatis.DataMapper.Configuration.Module
{
    /// <summary>
    /// Handles fluent configuration for ParameterMap
    /// </summary>
    public partial class ModuleBuilder
    {
        private ParameterMapExpression parameterMapExpression = null;

        /// <summary>
        /// Registers a ParameterMap.
        /// </summary>
        /// <param name="id">The id.</param>
        public ParameterMapExpression RegisterParameterMap<TParameterClass>(string id)
        {
            Contract.Require.That(id, Is.Not.Null & Is.Not.Empty).When("retrieving id argument in RegisterParameterMap method");

            RegisterConfiguration();
            parameterMapExpression = new ParameterMapExpression(this);

            Type parameterType = typeof(TParameterClass);

            currentConfiguration = new MutableConfiguration(
                ConfigConstants.ELEMENT_PARAMETERMAP,
                ApplyNamespace(id),
                parameterType.AssemblyQualifiedName);
            currentConfiguration.CreateAttribute(ConfigConstants.ATTRIBUTE_CLASS, parameterType.AssemblyQualifiedName);
            currentConfiguration.CreateAttribute(ConfigConstants.ATTRIBUTE_NAMESPACE, nameSpace);

            return parameterMapExpression;
        }

        private void BuildParameterMap(IConfigurationStore configurationStore)
        {
            foreach (IConfiguration configuration in store.ParameterMaps)
            {
                configurationStore.AddParameterMapConfiguration(configuration);
            }
        }
    }
}