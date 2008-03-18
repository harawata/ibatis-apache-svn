#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: 512878 $
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
using Apache.Ibatis.Common.Configuration;
#endregion 

namespace Apache.Ibatis.Common.Data
{
	/// <summary>
	/// Summary description for DataSourceDeSerializer.
	/// </summary>
	public sealed class DataSourceDeSerializer
	{
        /// <summary>
        /// Deserialize a DataSource object
        /// </summary>
        /// <param name="config">The config.</param>
        /// <returns></returns>
        public static DataSource Deserialize(IDbProvider dbProvider,IConfiguration config)
		{
            IConfiguration dataSourceConfig = config.Children.Find(DataConstants.ELEMENT_DATASOURCE)[0];

            string connectionString = dataSourceConfig.Attributes[DataConstants.ATTRIBUTE_CONNECTIONSTRING];
            string name = dataSourceConfig.Attributes[DataConstants.ATTRIBUTE_NAME];

            return new DataSource(name, connectionString, dbProvider);
		}

	}
}
