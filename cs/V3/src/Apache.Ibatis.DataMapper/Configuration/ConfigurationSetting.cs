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


using Apache.Ibatis.Common.Utilities.Objects;
using Apache.Ibatis.Common.Utilities.Objects.Members;
using Apache.Ibatis.DataMapper.Session;
using Apache.Ibatis.Common.Data;
using System.Collections.Generic;

namespace Apache.Ibatis.DataMapper.Configuration
{
    /// <summary>
    /// The <see cref="ConfigurationSetting"/> class allows the user to set by code some
    /// setting that will drive the iBATIS engine and his configuration.
    /// </summary>
    public sealed class ConfigurationSetting
    {
        private IObjectFactory objectFactory = null;
        private IGetAccessorFactory getAccessorFactory = null;
        private ISetAccessorFactory setAccessorFactory = null;
        private ISessionFactory sessionFactory = null;
        private IDataSource dataSource = null;
        private ISessionStore sessionStore = null;
        private readonly IDictionary<string, string> properties = new Dictionary<string, string>();
        private bool validateMapperConfigFile = false;
        private ISqlSource dynamicSqlEngine = null;

        /// <summary>
        /// Gets or sets the dynamic SQL engine.
        /// </summary>
        /// <value>The dynamic SQL engine.</value>
        public ISqlSource DynamicSqlEngine
        {
            get { return dynamicSqlEngine; }
            set { dynamicSqlEngine = value; }
        }

        /// <summary>
        /// Indicates whether we enable or not the validation of Mapper config files.
        /// </summary>
        /// <value>
        /// 	<c>true</c> if must validate; otherwise, <c>false</c>.
        /// </value>
        public bool ValidateMapperConfigFile
        {
            get { return validateMapperConfigFile; }
            set { validateMapperConfigFile = value; }
        }

        /// <summary>
        /// Gets or sets the session store.
        /// </summary>
        /// <value>The session store.</value>
        public ISessionStore SessionStore
        {
            get { return sessionStore; }
            set { sessionStore = value; }
        }

        /// <summary>
        /// Gets or sets the object factory.
        /// </summary>
        /// <value>The object factory.</value>
        public IObjectFactory ObjectFactory
        {
            get { return objectFactory; }
            set { objectFactory = value; }
        }

        /// <summary>
        /// Gets or sets the set accessor factory.
        /// </summary>
        /// <value>The set accessor factory.</value>
        public ISetAccessorFactory SetAccessorFactory
        {
            get { return setAccessorFactory; }
            set { setAccessorFactory = value; }
        }

        /// <summary>
        /// Gets or sets the get accessor factory.
        /// </summary>
        /// <value>The get accessor factory.</value>
        public IGetAccessorFactory GetAccessorFactory
        {
            get { return getAccessorFactory; }
            set { getAccessorFactory = value; }
        }

        /// <summary>
        /// Gets or sets the session factory.
        /// </summary>
        /// <value>The session factory.</value>
        public ISessionFactory SessionFactory
        {
            get { return sessionFactory; }
            set { sessionFactory = value; }
        }

        /// <summary>
        /// Gets or sets the data source.
        /// </summary>
        /// <value>The data source.</value>
        public IDataSource DataSource
        {
            get { return dataSource; }
            set { dataSource = value; }
        }

        /// <summary>
        /// Use to add properties configuration by code.
        /// </summary>
        /// <value>The properties.</value>
        public IDictionary<string, string> Properties
        {
            get { return properties; }
        }
    }
}
