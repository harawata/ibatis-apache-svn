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
using Apache.Ibatis.DataMapper.Model.Sql.Dynamic.Elements;
using Apache.Ibatis.DataMapper.Scope;
using Apache.Ibatis.DataMapper.DataExchange;
using Apache.Ibatis.Common.Configuration;
using Apache.Ibatis.Common.Utilities.Objects.Members;

#endregion 

namespace Apache.Ibatis.DataMapper.Configuration.Serializers
{
	/// <summary>
	/// Summary description for IsNotPropertyAvailableDeSerializer.
	/// </summary>
    public sealed class IsNotPropertyAvailableDeSerializer : BaseDynamicDeSerializer
	{

        /// <summary>
        /// Initializes a new instance of the <see cref="IsNotPropertyAvailableDeSerializer"/> class.
        /// </summary>
        /// <param name="accessorFactory">The accessor factory.</param>
        public IsNotPropertyAvailableDeSerializer(AccessorFactory accessorFactory)
            : base(accessorFactory)
        { }

		#region IDeSerializer Members

        /// <summary>
        /// Deserializes the specified configuration in an <see cref="IsNotPropertyAvailable"/> object
        /// </summary>
        /// <param name="configuration">The configuration.</param>
        /// <returns></returns>
        public override SqlTag Deserialize(IConfiguration configuration)
		{
            IsNotPropertyAvailable isNotPropertyAvailable = new IsNotPropertyAvailable(accessorFactory);

			isNotPropertyAvailable.Prepend = ConfigurationUtils.GetStringAttribute(configuration.Attributes, "prepend");
			isNotPropertyAvailable.Property = ConfigurationUtils.GetStringAttribute(configuration.Attributes, "property");

			return isNotPropertyAvailable;
		}

		#endregion
	}
}
