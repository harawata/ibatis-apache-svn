#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: $
 * $Date: $
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

using System;
using System.Data;
using IBatisNet.DataMapper.Configuration.ParameterMapping;
using IBatisNet.DataMapper.Configuration.ResultMapping;
#endregion 

namespace IBatisNet.DataMapper.TypeHandlers
{
	/// <summary>
	/// Custom type handler for adding a TypeHandlerCallback
	/// </summary>
	internal class CustomTypeHandler : BaseTypeHandler
	{
		private ITypeHandlerCallback _callback = null;

		public CustomTypeHandler(ITypeHandlerCallback callback)
		{
			_callback = callback;
		}

		/// <summary>
		/// Performs processing on a value before it is used to set
		/// the parameter of a IDbCommand.
		/// </summary>
		/// <param name="mapping">The mapping between data parameter and object property.</param>
		/// <param name="dataParameter"></param>
		/// <param name="parameterValue">The value to be set</param>
		public override void SetParameter(ParameterProperty mapping, IDataParameter dataParameter, object parameterValue)
		{
			IParameterSetter setter = new ParameterSetterImpl(dataParameter);
			_callback.SetParameter(setter, parameterValue);
		}

		protected override object GetValueByName(ResultProperty mapping, IDataReader dataReader)
		{
			IResultGetter getter = new ResultGetterImpl(dataReader, mapping.ColumnName);
			return _callback.GetResult(getter);
		}

		protected override object GetValueByIndex(ResultProperty mapping, IDataReader dataReader)
		{
			IResultGetter getter = new ResultGetterImpl(dataReader, mapping.ColumnIndex);
			return _callback.GetResult(getter);		
		}

		protected override object GetNullValue(ResultProperty mapping)
		{
			return _callback.GetNullValue(mapping.NullValue);
		}

		public override object GetDataBaseValue(object outputValue, Type parameterType)
		{
			IResultGetter getter = new ResultGetterImpl(outputValue);
			return _callback.GetResult(getter);	
		}

		public override bool IsSimpleType()
		{
			return false;
		}
	}
}