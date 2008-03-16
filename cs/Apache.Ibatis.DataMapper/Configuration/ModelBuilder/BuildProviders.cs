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

using System;
using Apache.Ibatis.Common.Configuration;
using Apache.Ibatis.DataMapper.Configuration.Interpreters.Config.Xml;
using Apache.Ibatis.Common.Data;


namespace Apache.Ibatis.DataMapper.Configuration
{
    public partial class DefaultModelBuilder
    {
        /// <summary>
        /// Builds the providers.
        /// </summary>
        /// <param name="store">The store.</param>
        private void BuildProviders(IConfigurationStore store)
        {
            dbProviderFactory = new DbProviderFactory(store.Providers);
        }
    }
}
