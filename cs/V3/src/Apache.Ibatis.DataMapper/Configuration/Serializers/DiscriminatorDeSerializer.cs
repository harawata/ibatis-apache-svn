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

using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Xml;
using Apache.Ibatis.Common.Configuration;
using Apache.Ibatis.Common.Xml;
using Apache.Ibatis.DataMapper.Model.ResultMapping;
using Apache.Ibatis.DataMapper.Scope;
using Apache.Ibatis.DataMapper.Configuration.Interpreters.Config;
using Apache.Ibatis.DataMapper.TypeHandlers;
using Apache.Ibatis.DataMapper.DataExchange;
#endregion 

namespace Apache.Ibatis.DataMapper.Configuration.Serializers
{
	/// <summary>
	/// Summary description for DiscriminatorDeSerializer.
	/// </summary>
	public sealed class DiscriminatorDeSerializer
	{
        /// <summary>
        /// Build a Discriminator object
        /// </summary>
        /// <param name="configuration">The configuration.</param>
        /// <param name="resultClass">The result class.</param>
        /// <param name="dataExchangeFactory">The data exchange factory.</param>
        /// <param name="subMaps">The sub maps.</param>
        /// <returns></returns>
        public static Discriminator Deserialize(
            IConfiguration configuration,
            Type resultClass, 
            DataExchangeFactory dataExchangeFactory, 
            IList<SubMap> subMaps)
        {
            string callBackName = ConfigurationUtils.GetStringAttribute(configuration.Attributes, ConfigConstants.ATTRIBUTE_TYPEHANDLER);
            string clrType = ConfigurationUtils.GetStringAttribute(configuration.Attributes, ConfigConstants.ATTRIBUTE_TYPE);
            int columnIndex = ConfigurationUtils.GetIntAttribute(configuration.Attributes, ConfigConstants.ATTRIBUTE_COLUMNINDEX, ResultProperty.UNKNOWN_COLUMN_INDEX);
            string columnName = ConfigurationUtils.GetStringAttribute(configuration.Attributes, ConfigConstants.ATTRIBUTE_COLUMN);
            string dbType = ConfigurationUtils.GetStringAttribute(configuration.Attributes, ConfigConstants.ATTRIBUTE_DBTYPE);
            string nullValue = configuration.GetAttributeValue(ConfigConstants.ATTRIBUTE_NULLVALUE);

            Discriminator discriminator = new Discriminator(
                callBackName, 
                clrType, 
                columnIndex,
                columnName, 
                dbType, 
                nullValue,
                subMaps,
                resultClass,
                dataExchangeFactory
                );

            return discriminator;
        }
	}
}
