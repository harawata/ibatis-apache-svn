#region Apache Notice
/*****************************************************************************
 * $Revision: 374175 $
 * $LastChangedDate: 2006-04-25 19:40:27 +0200 (mar., 25 avr. 2006) $
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

using System;
using System.Collections;
using System.Reflection;
using Castle.DynamicProxy;
using IBatisNet.Common.Logging;
using IBatisNet.Common.Utilities.Objects.Members;
using IBatisNet.Common.Utilities.Proxy;
using IBatisNet.DataMapper.Exceptions;
using IBatisNet.DataMapper.MappedStatements;

namespace IBatisNet.DataMapper.Proxy
{
	/// <summary>
	/// This class is responsible of create all <see cref="IList"/> lazy load proxies.
	/// </summary>
	public class LazyLoadProxyFactory
	{
		#region Fields
		private static readonly ILog _logger = LogManager.GetLogger( MethodBase.GetCurrentMethod().DeclaringType );
		#endregion

		#region Constructor
		/// <summary>
		/// Private constructor
		/// </summary>
		private LazyLoadProxyFactory()
		{
		}
		#endregion
		
		/// <summary>
		/// Builds the specified lazy load proxy.
		/// </summary>
        /// <param name="selectStatement">The mapped statement used to build the lazy loaded object.</param>
		/// <param name="param">The parameter object used to build lazy loaded object.</param>
		/// <param name="target">The target object which contains the property proxydied..</param>
        /// <param name="setAccessor">The proxified member accessor.</param>
		/// <returns>Return a proxy object</returns>
		public static object Build(IMappedStatement selectStatement, object param, 
			object target, ISetAccessor setAccessor)
		{
			object proxy = null;
			Type typeProxified = setAccessor.MemberType;

            bool isIList = typeof(IList).IsAssignableFrom(setAccessor.MemberType) || setAccessor.MemberType.IsSubclassOf(typeof(IList));
			Type returnedTypeByStatement = LazyLoadProxyFactory.GetTypeReturnedByStatemet(selectStatement, isIList); 
	
			//Test if the result of the lazy load is assigable to property, test now load time instead
			//wait to error when the method of proxy are called
			if (typeProxified.IsAssignableFrom(returnedTypeByStatement) == false)
			{
                throw new DataMapperException("Error building LazyLoad proxy for " + target.GetType() + "." + setAccessor.Name + " can not assing " + typeProxified + " to " + returnedTypeByStatement);
			}

			//Build the proxy
            IInterceptor handler = new LazyLoadInterceptor(selectStatement, param, target, setAccessor);
			if (isIList)
			{
				if (_logger.IsDebugEnabled) 
				{
                    _logger.Debug(string.Format("Statement '{0}', create list proxy for member {1}.", selectStatement.Id, setAccessor.MemberType));
				}

				if (selectStatement.Statement.ListClass == null)
				{
					 proxy = ProxyGeneratorFactory.GetProxyGenerator().CreateProxy(typeof(IList), handler, new ArrayList());
				}
				else
				{
					proxy = ProxyGeneratorFactory.GetProxyGenerator().CreateClassProxy(typeProxified, handler, Type.EmptyTypes);
				}	
			}
			else
			{
				throw new DataMapperException(string.Format("Only proxy on IList type are supported, the member type ({0}) cannot be proxyfied.", typeProxified) ); 
			}

			return proxy;
		}


		/// <summary>
		/// Gets the type returned by statemet.
		/// </summary>
		/// <param name="mappedStatement">The mapped statement.</param>
		/// <param name="isIList">if set to <c>true</c> [is Ilist].</param>
		/// <returns>The type object return by the statement.</returns>
		private static Type GetTypeReturnedByStatemet(IMappedStatement mappedStatement,
			bool isIList)
		{
			Type returnedType = null;

			if ( isIList )
			{
				if (mappedStatement.Statement.ListClass != null)
				{
					//Strongly type collection
					returnedType = mappedStatement.Statement.ListClass;	
				}
				else
				{
					//Generic List, IList is the return type of ExecuteQueryForList(..)
					returnedType = typeof(IList);
				}
			}
			else 
			{
				//Property to proxified is a simple object
				if (mappedStatement.Statement.ResultClass != null)
				{
					returnedType = mappedStatement.Statement.ResultClass; 
				}
				else if (mappedStatement.Statement.ParameterMap != null)
				{
					returnedType = mappedStatement.Statement.ParameterMap.Class;
				}
				else
				{
					throw new DataMapperException("We must never get here !");
				}
			}
		
			return returnedType;
		}

		



	}
}
