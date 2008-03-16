#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: 408164 $
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

#region Using

using System.Collections.Specialized;
using System.Xml;
using Apache.Ibatis.Common.Xml;
using Apache.Ibatis.DataMapper.Model.Cache;
using Apache.Ibatis.DataMapper.Scope;
using Apache.Ibatis.Common.Configuration;
using Apache.Ibatis.DataMapper.TypeHandlers;
using System.Collections;
using Apache.Ibatis.DataMapper.Configuration.Interpreters.Config;
#endregion 


namespace Apache.Ibatis.DataMapper.Configuration.Serializers
{
	/// <summary>
	/// Summary description for CacheModelDeSerializer.
	/// </summary>
	public sealed class CacheModelDeSerializer
	{

        /// <summary>
        /// Deserializes the specified config in a CacheModel object.
        /// </summary>
        /// <param name="config">The config.</param>
        /// <param name="properties">The cacheModel properties.</param>
        /// <param name="typeHandlerFactory">The type handler factory.</param>
        /// <returns>A CacheModel object</returns>
        public static CacheModel Deserialize(IConfiguration config, IDictionary properties, TypeHandlerFactory typeHandlerFactory)
        {
            string id = config.Id;
            string implementation = ConfigurationUtils.GetMandatoryStringAttribute(config, ConfigConstants.ATTRIBUTE_IMPLEMENTATION);
            implementation = typeHandlerFactory.GetTypeAlias(implementation).Type.AssemblyQualifiedName;
            bool isReadOnly = ConfigurationUtils.GetBooleanAttribute(config.Attributes, ConfigConstants.ATTRIBUTE_READONLY, true);
            bool isSerializable = ConfigurationUtils.GetBooleanAttribute(config.Attributes, ConfigConstants.ATTRIBUTE_SERIALIZE, false);

            return new CacheModel(id, implementation, properties, isReadOnly, isSerializable);
        }
	}
}
