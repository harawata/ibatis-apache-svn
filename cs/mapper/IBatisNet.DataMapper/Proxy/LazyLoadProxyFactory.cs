#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: 391784 $
 * $Date: 2006-04-05 22:23:27 +0200 (mer., 05 avr. 2006) $
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
	/// This class is responsible of create all lazy load proxies.
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
		/// <param name="mappedStatement">The mapped statement used to build the lazy loaded object.</param>
		/// <param name="param">The parameter object used to build lazy loaded object.</param>
		/// <param name="target">The target object which contains the property proxydied..</param>
		/// <param name="memberAccessor">The proxified member accessor.</param>
		/// <returns></returns>
		public static object Build(IMappedStatement mappedStatement, object param, 
			object target, IMemberAccessor memberAccessor)
		{
			object proxy = null;
			Type typeProxified = memberAccessor.MemberType; 
	
			bool isIList = typeof(IList).IsAssignableFrom(memberAccessor.MemberType);
			Type returnedTypeByStatement = LazyLoadProxyFactory.GetTypeReturnedByStatemet(mappedStatement, isIList); 
	
			//Test if the result of the lazy load is assigable to property, test now load time instead
			//wait to error when the method of proxy are called
			if (typeProxified.IsAssignableFrom(returnedTypeByStatement) == false)
			{
				throw new DataMapperException("Error building LazyLoad proxy for " + target.GetType() + "." + memberAccessor.Name + " can not assing " + typeProxified + " to " + returnedTypeByStatement);
			}

			//Build the proxy
			IInterceptor handler = new LazyLoadInterceptor(mappedStatement, param, target, memberAccessor);
			if (isIList)
			{
				if (_logger.IsDebugEnabled) 
				{
					_logger.Debug(string.Format("Statement '{0}', create list proxy for member {1}.", mappedStatement.Id ,memberAccessor.MemberType));
				}

				if (mappedStatement.Statement.ListClass == null)
				{
					 proxy = ProxyGeneratorFactory.GetProxyGenerator().CreateProxy(typeof(IList), handler, new ArrayList());
				}
				else
				{
					proxy = ProxyGeneratorFactory.GetProxyGenerator().CreateClassProxy(typeProxified, handler, Type.EmptyTypes);
				}	
			}
			else if (typeProxified.IsClass)
			{
				if (_logger.IsDebugEnabled) 
				{
					_logger.Debug(string.Format("Statement '{0}', create class proxy for member {1}.", mappedStatement.Id ,memberAccessor.MemberType));
				}

				//TODO test if param fit with constructor arguments 
				proxy = ProxyGeneratorFactory.GetProxyGenerator().CreateClassProxy(typeProxified, handler, 
					LazyLoadProxyFactory.CreateArgumentsForConstructor(typeProxified, param));
			}
			else
			{
				throw new DataMapperException(string.Format("Not implemented: the type ({0}) of property to proxify is not and interface or class.", typeProxified) ); 
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

		
		/// <summary>
		/// Creates the arguments for constructor.
		/// </summary>
		/// <param name="type">The object type.</param>
		/// <param name="lazyLoadParam">The lazy load param.</param>
		/// <returns>A list of objects argument</returns>
		private static object[] CreateArgumentsForConstructor(Type type,object lazyLoadParam)
		{
			object[] argumentsForConstructor = null;

			if (type.GetInterface(typeof(ICollection).FullName) != null)
			{
				//the collection build whitout arguments
				argumentsForConstructor = new object[]{};
			}
			else
			{
				if (lazyLoadParam == null)
				{
					argumentsForConstructor = new object[]{};
				}
				if (lazyLoadParam is object[])
				{
					//Multiple primary key
					argumentsForConstructor = (object[])lazyLoadParam;
				}
				else
				{
					argumentsForConstructor = new object[]{lazyLoadParam};
				}
			}

			return argumentsForConstructor;
		}


	}
}
