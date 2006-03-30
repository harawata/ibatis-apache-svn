#region Apache Notice
/*****************************************************************************
 * $Revision: 374175 $
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
using System.Collections;
using IBatisNet.DataMapper.TypeHandlers;

#if dotnet2
using System.Collections.Generic;
#endif

namespace IBatisNet.DataMapper.DataExchange
{
	/// <summary>
	/// Factory for DataExchange objects
	/// </summary>
	public class DataExchangeFactory
	{
		private TypeHandlerFactory _typeHandlerFactory = null;
		private IDataExchange _primitiveDataExchange = null;
		private IDataExchange _complexDataExchange = null;
		private IDataExchange _listDataExchange = null;
		private IDataExchange _dictionaryDataExchange = null;

		
		/// <summary>
		///  Getter for the type handler factory
		/// </summary>
		public TypeHandlerFactory TypeHandlerFactory
		{
			get{ return _typeHandlerFactory; }
		}

		/// <summary>
		/// Constructor for the factory
		/// </summary>
		/// <param name="typeHandlerFactory">A type handler factory for the factory</param>
		public DataExchangeFactory(TypeHandlerFactory typeHandlerFactory)
		{
			_typeHandlerFactory = typeHandlerFactory;
			_primitiveDataExchange = new PrimitiveDataExchange(this);
			_complexDataExchange = new ComplexDataExchange(this);
			_listDataExchange = new ListDataExchange(this);
			_dictionaryDataExchange = new DictionaryDataExchange(this);
		}

		/// <summary>
		/// Get a DataExchange object for the passed in Class
		/// </summary>
		/// <param name="clazz">The class to get a DataExchange object for</param>
		/// <returns>The IDataExchange object</returns>
		public IDataExchange GetDataExchangeForClass(Type clazz)
		{
			IDataExchange dataExchange = null;
			if (clazz == null) 
			{
				dataExchange = _complexDataExchange;
			}
			else if (typeof(IList).IsAssignableFrom(clazz)) 
			{
				dataExchange = _listDataExchange;
			} 
			else if (typeof(IDictionary).IsAssignableFrom(clazz)) 
			{
				dataExchange = _dictionaryDataExchange;
			} 
			else if (_typeHandlerFactory.GetTypeHandler(clazz) != null) 
			{
				dataExchange = _primitiveDataExchange;
			} 
			else 
			{
				dataExchange = new DotNetObjectDataExchange(clazz, this);
			}
			
			return dataExchange;
		}

#if dotnet2
        public bool IsImplementGenericIListInterface(Type type)
        {
            Type[] interfaceTypes = type.GetInterfaces();
            foreach (Type interfaceType in interfaceTypes)
            {
                if (interfaceType.IsGenericType &&
                  interfaceType.GetGenericTypeDefinition() == typeof(IList<>))
                {
                    return true;
                }
            }
            return false;
        } 
#endif
	}
}
