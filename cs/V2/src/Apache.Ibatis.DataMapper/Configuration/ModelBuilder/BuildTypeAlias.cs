﻿#region Apache Notice
/*****************************************************************************
 * $Revision: 408099 $
 * $LastChangedDate$
 * $LastChangedBy$
 * 
 * iBATIS.NET Data Mapper
 * Copyright (C) 2006/2005 - The Apache Software Foundation
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
using Apache.Ibatis.DataMapper.Model.Alias;
using Apache.Ibatis.DataMapper.Configuration.Serializers;

namespace Apache.Ibatis.DataMapper.Configuration
{
    public partial class DefaultModelBuilder
    {
        /// <summary>
        /// Builds the type alias.
        /// </summary>
        /// <param name="store">The store.</param>
        private void BuildTypeAlias(IConfigurationStore store)
        {
            //_configScope.ErrorContext.Resource = nodeDataSource.OuterXml.ToString();
            //_configScope.ErrorContext.MoreInfo = "parse DataSource";
            
            foreach(IConfiguration aliasConfig in store.Alias)
            {
                TypeAlias typeAlias = TypeAliasDeSerializer.Deserialize(aliasConfig);
                modelStore.DataExchangeFactory.TypeHandlerFactory.AddTypeAlias(typeAlias.Id, typeAlias);
            }
        }
    }
}