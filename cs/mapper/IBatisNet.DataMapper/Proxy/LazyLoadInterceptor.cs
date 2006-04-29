
#region Apache Notice
/*****************************************************************************
 * $Revision: 374175 $
 * $LastChangedDate: 2006-03-22 22:39:21 +0100 (mer., 22 mars 2006) $
 * $LastChangedBy: gbayon $
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
using System.Reflection;
using Castle.DynamicProxy;
using IBatisNet.Common.Logging;
using IBatisNet.Common.Utilities.Objects;
using IBatisNet.Common.Utilities.Objects.Members;
using IBatisNet.Common.Utilities.Proxy;
using IBatisNet.DataMapper.MappedStatements;
#if dotnet2
using System.Collections.Generic;
#endif
#endregion

namespace IBatisNet.DataMapper.Proxy
{
	/// <summary>
	/// Default implementation of the interceptor reponsible of load the lazy element
	/// Could load collections and single objects
	/// </summary>
	[Serializable]
	internal class LazyLoadInterceptor : IInterceptor
	{
		#region Fields
		private object _param = null;
		private object _target = null;
		private IMemberAccessor _memberAccessor= null;
		private SqlMapper _sqlMap = null;
		private string _statementName = string.Empty;
		private bool _loaded = false;
		private IList _innerList = null;
		private object _loadLock = new object();
		private static ArrayList _passthroughMethods = new ArrayList();

		private static readonly ILog _logger = LogManager.GetLogger( MethodBase.GetCurrentMethod().DeclaringType );
		#endregion

		#region  Constructor (s) / Destructor

		/// <summary>
		/// Static Constructor for a lazy list loader
		/// </summary>
		static LazyLoadInterceptor()
		{
			_passthroughMethods.Add("GetType");
			_passthroughMethods.Add("ToString");
		}

		/// <summary>
		/// Constructor for a lazy list loader
		/// </summary>
		/// <param name="mappedSatement">The mapped statement used to build the list</param>
		/// <param name="param">The parameter object used to build the list</param>
		/// <param name="memberAccessor">The proxified member accessor.</param>
		/// <param name="target">The target object which contains the property proxydied.</param>
		internal LazyLoadInterceptor(IMappedStatement mappedSatement, object param, 
			object target, IMemberAccessor memberAccessor)
		{
			_param = param;
			_statementName = mappedSatement.Id;
			_sqlMap = mappedSatement.SqlMap;
			_target = target; 
			_memberAccessor = memberAccessor;
		}		
		#endregion

		#region IInterceptor member

		/// <summary>
		/// Intercepts the specified invocation.
		/// </summary>
		/// <param name="invocation">The invocation.</param>
		/// <param name="arguments">The target arguments.</param>
		/// <returns></returns>
		public object Intercept(IInvocation invocation, params object[] arguments)
		{
			if (_logger.IsDebugEnabled) 
			{
				_logger.Debug("Proxyfying call to " + invocation.Method.Name);
			}

			lock(_loadLock)
			{
				if ((_loaded == false) && (!_passthroughMethods.Contains(invocation.Method.Name)))
				{
					if (_logger.IsDebugEnabled) 
					{
						_logger.Debug("Proxyfying call, query statement " + _statementName);
					}
					_innerList = _sqlMap.QueryForList(_statementName, _param);
					_loaded = true;
					_memberAccessor.Set(_target, _innerList);
				}
			}

			object returnValue = invocation.Method.Invoke( _innerList, arguments);		

			if (_logger.IsDebugEnabled) 
			{
				_logger.Debug("End of proxyfied call to " + invocation.Method.Name);
			}

			return returnValue;
		}

		#endregion
	}
}
