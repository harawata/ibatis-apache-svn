#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: 576082 $
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
using Apache.Ibatis.Common.Configuration;
using Apache.Ibatis.Common.Xml;
using Apache.Ibatis.DataMapper.Model.ResultMapping;
using Apache.Ibatis.DataMapper.Scope;
using Apache.Ibatis.DataMapper.Configuration.Interpreters.Config;
#endregion 


namespace Apache.Ibatis.DataMapper.Configuration.Serializers
{
	/// <summary>
	/// Summary description for SubMapDeSerializer.
	/// </summary>
	public sealed class SubMapDeSerializer
	{
        /// <summary>
        /// Build a SubMap object
        /// </summary>
        /// <param name="configuration">The configuration.</param>
        /// <returns>a SubMap object</returns>
        public static SubMap Deserialize(IConfiguration configuration)
        {
            string discriminatorValue = ConfigurationUtils.GetStringAttribute(configuration.Attributes, ConfigConstants.ATTRIBUTE_VALUE);
            string resultMapName = ConfigurationUtils.GetStringAttribute(configuration.Attributes, ConfigConstants.ATTRIBUTE_RESULTMAPPING);

            return new SubMap(discriminatorValue, resultMapName);
        }

	}
}
