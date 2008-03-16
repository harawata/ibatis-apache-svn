﻿
#region Apache Notice
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

using System.Collections.Generic;
using System.Text;
using Apache.Ibatis.Common.Data;
using Apache.Ibatis.DataMapper.DataExchange;
using Apache.Ibatis.DataMapper.Exceptions;
using Apache.Ibatis.DataMapper.MappedStatements;
using Apache.Ibatis.DataMapper.Model.Cache;
using Apache.Ibatis.DataMapper.Model.ParameterMapping;
using Apache.Ibatis.DataMapper.Model.ResultMapping;
using Apache.Ibatis.DataMapper.Data;
using Apache.Ibatis.DataMapper.Session;
using Apache.Ibatis.Common.Utilities;
using System.Diagnostics;
using System;

namespace Apache.Ibatis.DataMapper.Model
{
    /// <summary>
    /// The default implementation of <see cref="IModelStore"/> contract.
    /// </summary>
    [DebuggerDisplay("DefaultModelStore: {Id}")]
    public class DefaultModelStore : IModelStore
    {
        private IDataMapper dataMapper = null;
        private ISessionFactory sessionFactory = null;
        private ISessionStore sessionStore = null;
        private DataExchangeFactory dataExchangeFactory = null;
        private readonly IDictionary<string, IResultMap> resultMaps = new Dictionary<string, IResultMap>();
        private readonly IDictionary<string, ParameterMap> parameterMaps = new Dictionary<string, ParameterMap>();
        private readonly IDictionary<string, CacheModel> cacheModels = new Dictionary<string, CacheModel>();
        private readonly IDictionary<string, IMappedStatement> mappedStatements = new Dictionary<string, IMappedStatement>();
        private readonly DBHelperParameterCache dbHelperParameterCache = new DBHelperParameterCache();
        private readonly string id = string.Empty;

        /// <summary>
        /// Initializes a new instance of the <see cref="DefaultModelStore"/> class.
        /// </summary>
        public DefaultModelStore()
        {
            id = HashCodeProvider.GetIdentityHashCode(this).ToString();
        }

        #region IModelStore Members

        /// <summary>
        /// Name used to identify the <see cref="IModelStore"/>
        /// </summary>
        /// <value></value>
        public string Id { get { return id; } }

        /// <summary>
        /// Gets or sets the data mapper.
        /// </summary>
        /// <value>The data mapper.</value>
        public IDataMapper DataMapper
        {
            get { return dataMapper; }
            set { dataMapper = value; }
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
        /// Gets the session factory.
        /// </summary>
        /// <value>The session factory.</value>
        public ISessionFactory SessionFactory
        {
            get { return sessionFactory; }
            set { sessionFactory = value; }
        }

        /// <summary>
        /// Gets the DB helper parameter cache.
        /// </summary>
        /// <value>The DB helper parameter cache.</value>
        public DBHelperParameterCache DBHelperParameterCache
        {
            get { return dbHelperParameterCache; }
        }

        /// <summary>
        /// Gets or sets the data exchange factory.
        /// </summary>
        /// <value>The data exchange factory.</value>
        public DataExchangeFactory DataExchangeFactory
        {
            get { return dataExchangeFactory; }
            set { dataExchangeFactory = value; }
        }

        /// <summary>
        /// Gets a ResultMap by Id
        /// </summary>
        /// <param name="id">The id.</param>
        /// <returns>The ResultMap</returns>
        public IResultMap GetResultMap(string id)
        {
            if (resultMaps.ContainsKey(id) == false)
            {
                throw new DataMapperException("The DataMapper does not contain an ResultMap named " + id);
            }
            return resultMaps[id];
        }

        /// <summary>
        /// Adds a (named) ResultMap
        /// </summary>
        /// <param name="resultMap">The ResultMap to add</param>
        public void AddResultMap(IResultMap resultMap)
        {
            if (resultMaps.ContainsKey(resultMap.Id))
            {
                throw new DataMapperException("The DataMapper already contains an ResultMap named " + resultMap.Id);
            }
            resultMaps.Add(resultMap.Id, resultMap);
        }

        /// <summary>
        /// Get a ParameterMap by id
        /// </summary>
        /// <param name="id">The id of the ParameterMap</param>
        /// <returns>The ParameterMap</returns>
        public ParameterMap GetParameterMap(string id)
        {
            if (!parameterMaps.ContainsKey(id))
            {
                throw new DataMapperException("The DataMapper does not contain an ParameterMap named " + id + ".  ");
            }
            return parameterMaps[id];
        }

        /// <summary>
        /// Adds a (named) ParameterMap.
        /// </summary>
        /// <param name="parameterMap">the ParameterMap to add</param>
        public void AddParameterMap(ParameterMap parameterMap)
        {
            if (parameterMaps.ContainsKey(parameterMap.Id))
            {
                throw new DataMapperException("The DataMapper already contains an ParameterMap named " + parameterMap.Id);
            }
            parameterMaps.Add(parameterMap.Id, parameterMap);
        }

        /// <summary>
        /// Gets a MappedStatement by name
        /// </summary>
        /// <param name="id"> The id of the statement</param>
        /// <returns> The MappedStatement</returns>
        public IMappedStatement GetMappedStatement(string id)
        {
            if (!mappedStatements.ContainsKey(id))
            {
                throw new DataMapperException("The DataMapper does not contain a MappedStatement named " + id);
            }
            return mappedStatements[id];
        }

        /// <summary>
        /// Adds a (named) MappedStatement.
        /// </summary>
        /// <param name="mappedStatement">The statement to add</param>
        public void AddMappedStatement(IMappedStatement mappedStatement)
        {
            if (mappedStatements.ContainsKey(mappedStatement.Id))
            {
                throw new DataMapperException("The DataMapper already contains a MappedStatement named " + mappedStatement.Id);
            }
            mappedStatements.Add(mappedStatement.Id, mappedStatement);
        }


        /// <summary>
        /// Gets a cache model by id
        /// </summary>
        /// <param name="id">The id of the cache model</param>
        /// <returns>The cache model</returns>
        public CacheModel GetCacheModel(string id)
        {
            if (!cacheModels.ContainsKey(id))
            {
                throw new DataMapperException("The DataMapper does not contain a CacheModel named " + id);
            }
            return cacheModels[id];
        }


        /// <summary>
        /// Adds a (named) cache model.
        /// </summary>
        /// <param name="cacheModel">The cache model.</param>
        public void AddCacheModel(CacheModel cacheModel)
        {
            if (cacheModels.ContainsKey(cacheModel.Id))
            {
                throw new DataMapperException("The DataMapper already contains a CacheModel named " + cacheModel.Id);
            }
            cacheModels.Add(cacheModel.Id, cacheModel);
        }


        /// <summary>
        /// Flushes all cached objects that belong to this <see cref="IModelStore"/>
        /// </summary>
        public void FlushCaches()
        {
            foreach(CacheModel cacheModel in cacheModels.Values)
            {
                cacheModel.Flush();
            }
        }

        /// <summary>
        /// Gets the data cache statistique.
        /// </summary>
        /// <returns></returns>
        public string GetDataCacheStats()
        {
            StringBuilder buffer = new StringBuilder();
            buffer.Append(Environment.NewLine);
            buffer.Append("Cache Data Statistics");
            buffer.Append(Environment.NewLine);
            buffer.Append("=====================");
            buffer.Append(Environment.NewLine);

            foreach(IMappedStatement mappedStatement in mappedStatements.Values)
            {
                buffer.Append(mappedStatement.Id);
                buffer.Append(": ");

                if (mappedStatement is CachingStatement)
                {
                    double hitRatio = ((CachingStatement)mappedStatement).GetDataCacheHitRatio();
                    if (hitRatio != -1)
                    {
                        buffer.Append(Math.Round(hitRatio * 100));
                        buffer.Append("%");
                    }
                    else
                    {
                        // this statement has a cache but it hasn't been accessed yet
                        // buffer.Append("Cache has not been accessed."); ???
                        buffer.Append("No Cache.");
                    }
                }
                else
                {
                    buffer.Append("No Cache.");
                }

                buffer.Append(Environment.NewLine);
            }

            return buffer.ToString();
        }

        #endregion

        /// <summary>
        /// Returns a <see cref="T:System.String"></see> that represents the current <see cref="T:System.Object"></see>.
        /// </summary>
        /// <returns>
        /// A <see cref="T:System.String"></see> that represents the current <see cref="T:System.Object"></see>.
        /// </returns>
        public override string ToString()
        {
            StringBuilder builder = new StringBuilder();

            int level = 1;
            builder.AppendLine("DataSource:");
            builder.AppendLine(string.Empty.PadLeft(level * 3, ' ') + sessionFactory.DataSource);
            builder.AppendLine(string.Empty);

            builder.AppendLine("CacheModels (" + cacheModels.Count + "):");
            IEnumerator<CacheModel> caches = cacheModels.Values.GetEnumerator();
            while (caches.MoveNext())
            {
                builder.AppendLine(string.Empty.PadLeft(level * 3, ' ') + caches.Current.Id + "/" + caches.Current.Implementation);
            }
            builder.AppendLine(string.Empty);

            builder.AppendLine("ResultMaps (" + resultMaps.Count + "):");
            IEnumerator<IResultMap> rMaps = resultMaps.Values.GetEnumerator();
            while (rMaps.MoveNext())
            {
               builder.AppendLine(string.Empty.PadLeft(level * 3, ' ') + rMaps.Current.Id + "/" + rMaps.Current.Class.Name);
               for (int i = 0; i < rMaps.Current.Parameters.Count; i++)
               {
                   builder.AppendLine(string.Empty.PadLeft(level * 2 * 3, ' ') + " Argument: "+((ArgumentProperty)rMaps.Current.Parameters[i]).ArgumentName);
               }
               for (int i = 0; i < rMaps.Current.Properties.Count; i++ )
               {
                   builder.AppendLine(string.Empty.PadLeft(level * 2 * 3, ' ') + " Property: " + rMaps.Current.Properties[i].PropertyName);
               }
            }
            builder.AppendLine(string.Empty);

            builder.AppendLine("ParameterMaps (" + parameterMaps.Count + "):");
            IEnumerator<ParameterMap> pMaps = parameterMaps.Values.GetEnumerator();
            while (pMaps.MoveNext())
            {
                builder.AppendLine(string.Empty.PadLeft(level * 3, ' ') + pMaps.Current.Id + "/" + pMaps.Current.Class.Name);
                for (int i = 0; i < pMaps.Current.Properties.Count; i++)
                {
                    builder.AppendLine(string.Empty.PadLeft(level * 2 * 3, ' ') + " Property: " + pMaps.Current.Properties[i].PropertyName);
                }
            }
            builder.AppendLine(string.Empty);

            builder.AppendLine("MappedStatements (" + mappedStatements.Count + "):");
            IEnumerator<IMappedStatement> statements = mappedStatements.Values.GetEnumerator();
            while (statements.MoveNext())
            {
                builder.AppendLine(string.Empty.PadLeft(level * 3, ' ') + statements.Current.Id);
            }
            builder.AppendLine(string.Empty);

            return builder.ToString();
        }

    }
}
