
#region Apache Notice
/*****************************************************************************
 * $Revision: 575913 $
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

#region Using

using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Runtime.CompilerServices;
using System.Runtime.Serialization.Formatters.Binary;
using Apache.Ibatis.Common.Contracts;
using Apache.Ibatis.Common.Exceptions;
using Apache.Ibatis.Common.Logging;
using Apache.Ibatis.Common.Utilities;
using Apache.Ibatis.DataMapper.MappedStatements;

#endregion

namespace Apache.Ibatis.DataMapper.Model.Cache
{

    /// <summary>
    /// Summary description for CacheModel.
    /// </summary>
	[Serializable]
    [DebuggerDisplay("CacheModel: {Id}-{Implementation}")]
	public class CacheModel
	{
		#region Fields

		private readonly static IDictionary lockMap = new HybridDictionary();

		private static readonly ILog logger = LogManager.GetLogger( MethodBase.GetCurrentMethod().DeclaringType );
		/// <summary>
		/// This is used to represent null objects that are returned from the cache so 
		/// that they can be cached, too.
		/// </summary>
		public readonly static object NULL_OBJECT = new Object(); 

		/// <summary>
		/// Constant to turn off periodic cache flushes
		/// </summary>
		public const long NO_FLUSH_INTERVAL = -99999;

        private readonly object syncLock = new Object();
		private int requests = 0;
		private int hits = 0;
        private readonly string id = string.Empty;
		private ICacheController controller = null;
		private FlushInterval flushInterval = null;
		private long lastFlush = 0;
        private readonly string implementation = string.Empty;
		private readonly bool isReadOnly = true;
        private readonly bool isSerializable = false;
        private readonly IList<string> statementFlushNames = new List<string>();

		#endregion

		#region Properties
		/// <summary>
		/// Identifier used to identify the CacheModel amongst the others.
		/// </summary>
		public string Id
		{
			get { return id; }
		}


        /// <summary>
        /// Gets the statement flush on execute names.
        /// </summary>
        /// <value>The statement flush names.</value>
        public IList<string> StatementFlushNames
        {
            get { return statementFlushNames; }
        }

		/// <summary>
		/// Cache controller implementation name.
		/// </summary>
		public string Implementation
		{
			get { return implementation; }
		}

		/// <summary>
		/// Set the cache controller
		/// </summary>
		public ICacheController CacheController
		{
			set{ controller =value; }	
		}

		/// <summary>
		/// Set or get the flushInterval (in Ticks)
		/// </summary>
		public FlushInterval FlushInterval
		{
			get { return flushInterval; }
			set { flushInterval = value; }
		}

		/// <summary>
		/// Specifie how the cache content should be returned.
		/// If true a deep copy is returned.
		/// </summary>
		/// <remarks>
		/// Combinaison
		/// IsReadOnly=true/IsSerializable=false : Returned instance of cached object
		/// IsReadOnly=false/IsSerializable=true : Returned coopy of cached object
		/// </remarks>
		public bool IsSerializable
		{
			get { return isSerializable; }
		}

		/// <summary>
		/// Determines if the cache will be used as a read-only cache.
		/// Tells the cache model that is allowed to pass back a reference to the object
		/// existing in the cache.
		/// </summary>
		/// <remarks>
		/// The IsReadOnly properties works in conjonction with the IsSerializable propertie.
		/// </remarks>
		public bool IsReadOnly
		{
			get { return isReadOnly; }
		}
		#endregion

		#region Constructor (s) / Destructor

        /// <summary>
        /// Initializes a new instance of the <see cref="CacheModel"/> class.
        /// </summary>
        /// <param name="name">The name.</param>
        /// <param name="controllerImplementation">The controller implementation.</param>
        /// <param name="properties">The properties.</param>
        public CacheModel(
            string name, 
            string controllerImplementation,
            IDictionary properties)
            : this(name, controllerImplementation, properties, true, false)
        {}

        /// <summary>
        /// Initializes a new instance of the <see cref="CacheModel"/> class.
        /// </summary>
        /// <param name="id">The id.</param>
        /// <param name="implementation">The controller implementation.</param>
        /// <param name="properties">The properties.</param>
        /// <param name="isReadOnly">if set to <c>true</c> [is read only].</param>
        /// <param name="isSerializable">if set to <c>true</c> [is serializable].</param>
        public CacheModel(
            string id,
            string implementation, 
            IDictionary properties,
            bool isReadOnly,
            bool isSerializable) 
		{
            Contract.Require.That(id, Is.Not.Null & Is.Not.Empty).When("retrieving argument id in CacheModel constructor");
            Contract.Require.That(implementation, Is.Not.Null & Is.Not.Empty).When("retrieving argument implementation in CacheModel constructor");
            Contract.Require.That(properties, Is.Not.Null).When("retrieving argument properties in CacheModel constructor");

            this.id = id;
            this.implementation = implementation;
            this.isReadOnly = isReadOnly;
            this.isSerializable = isSerializable;

			lastFlush = DateTime.Now.Ticks;

			try 
			{
				// Build the CacheController
                Type type = TypeUtils.ResolveType(implementation);
				object[] arguments = new object[0];

				controller = (ICacheController)Activator.CreateInstance(type, arguments);
			} 
			catch (Exception e) 
			{
				throw new ConfigurationException("Error instantiating cache controller for cache named '"+id+". Cause: " + e.Message, e);
			}

			//------------ configure Controller---------------------
			try 
			{
				controller.Configure(properties);
			} 
			catch (Exception e) 
			{
				throw new ConfigurationException ("Error configuring controller named '"+id+"'. Cause: " + e.Message, e);
			}
		}


		/// <summary>
		/// Event listener
		/// </summary>
		/// <param name="mappedStatement">A MappedStatement on which we listen ExecuteEventArgs event.</param>
		public void RegisterTriggerStatement(IMappedStatement mappedStatement)
		{
			mappedStatement.Execute +=FlushHandler;
		}
		
		
		/// <summary>
		/// FlushHandler which clear the cache 
		/// </summary>
		/// <param name="sender"></param>
		/// <param name="e"></param>
		private void FlushHandler(object sender, ExecuteEventArgs e)
		{
			if (logger.IsDebugEnabled) 
			{
				logger.Debug("Flush cacheModel named "+id+" for statement '"+e.StatementName+"'");
			}

			Flush();
		}


		/// <summary>
		/// Clears all elements from the cache.
		/// </summary>
		public void Flush() 
		{
			lastFlush = DateTime.Now.Ticks;
			controller.Flush();
		}


		/// <summary>
		/// Adds an item with the specified key and value into cached data.
		/// Gets a cached object with the specified key.
		/// </summary>
		/// <value>The cached object or <c>null</c></value>
		/// <remarks>
		/// A side effect of this method is that is may clear the cache
		/// if it has not been cleared in the flushInterval.
		/// </remarks> 
		public object this [CacheKey key] 
		{
			get
			{
				lock(this) 
				{
					if (lastFlush != NO_FLUSH_INTERVAL
						&& (DateTime.Now.Ticks - lastFlush > flushInterval.Interval)) 
					{
						Flush();
					}
				}

				object value = null;
				lock (GetLock(key)) 
				{
					value = controller[key];
				}

				if(isSerializable && !isReadOnly &&
					(value != NULL_OBJECT && value != null))
				{
					try
					{
						MemoryStream stream = new MemoryStream((byte[]) value);
						stream.Position = 0;
						BinaryFormatter formatter = new BinaryFormatter();
						value = formatter.Deserialize( stream );
					}
					catch(Exception ex)
					{
						throw new IbatisException("Error caching serializable object.  Be sure you're not attempting to use " +
							"a serialized cache for an object that may be taking advantage of lazy loading.  Cause: "+ex.Message, ex);
					}
				}

				lock(syncLock) 
				{
					requests++;
					if (value != null) 
					{
						hits++;
					}
				}

                if (logger.IsDebugEnabled)
                {
                    if (value != null)
                    {
                        logger.Debug(String.Format("Retrieved cached object '{0}' using key '{1}' ", value, key));
                    }
                    else
                    {
                        logger.Debug(String.Format("Cache miss using key '{0}' ", key));
                    }
                }
				return value;
			}
			set
			{
				if (null == value) {value = NULL_OBJECT;}
				if(isSerializable && !isReadOnly && value != NULL_OBJECT)
				{
					try
					{
						MemoryStream stream = new MemoryStream();
						BinaryFormatter formatter = new BinaryFormatter();
						formatter.Serialize(stream, value);
						value = stream.ToArray();
					}
					catch(Exception ex)
					{
						throw new IbatisException("Error caching serializable object. Cause: "+ex.Message, ex);
					}
				}
				controller[key] = value;
                if (logger.IsDebugEnabled)
                {
                    logger.Debug(String.Format("Cache object '{0}' using key '{1}' ", value, key));
                }
			}
		}

		/// <summary>
		/// 
		/// </summary>
		public double HitRatio 
		{
			get 
			{
				if (requests!=0)
				{
					return (double)hits/(double)requests;
				}
				else
				{
					return 0;
				}
			}
		}

		#endregion

		/// <summary>
		/// 
		/// </summary>
		/// <param name="key"></param>
		/// <returns></returns>
		[MethodImpl(MethodImplOptions.Synchronized)]
		public object GetLock(CacheKey key) 
		{
			int controllerId = HashCodeProvider.GetIdentityHashCode(controller);
			int keyHash = key.GetHashCode();
			int lockKey = 29 * controllerId + keyHash;
			object lok =lockMap[lockKey];
			if (lok == null) 
			{
				lok = lockKey; //might as well use the same object
				lockMap[lockKey] = lok;
			}
			return lok;
		}
	}
}
