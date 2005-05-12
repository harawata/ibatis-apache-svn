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
	///  Implementation of TypeHandler for dealing with unknown types
	/// </summary>
	internal class UnknownTypeHandler : BaseTypeHandler
	{

		private TypeHandlerFactory _factory = null;

		/// <summary>
		/// Constructor to create via a factory
		/// </summary>
		/// <param name="factory">the factory to associate this with</param>
		public UnknownTypeHandler(TypeHandlerFactory factory) 
		{
			_factory = factory;
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
			if (parameterValue!=null)
			{
				ITypeHandler handler = _factory.GetTypeHandler( parameterValue.GetType() );
				handler.SetParameter(mapping, dataParameter, parameterValue);
			}
			else
			{
				// When sending a null parameter value to the server,
				// the user must specify DBNull, not null. 
				dataParameter.Value = System.DBNull.Value;
			}
		}

		protected override object GetValueByName(ResultProperty mapping, IDataReader dataReader)
		{
			int index = dataReader.GetOrdinal(mapping.ColumnName);

			if (dataReader.IsDBNull(index) == true)
			{
				return System.DBNull.Value;
			}
			else
			{
				return dataReader.GetValue(index);
			}		
		}

		protected override object GetValueByIndex(ResultProperty mapping, IDataReader dataReader)
		{
			if (dataReader.IsDBNull(mapping.ColumnIndex) == true)
			{
				return System.DBNull.Value;
			}
			else
			{
				return dataReader.GetValue(mapping.ColumnIndex);
			}		
		}

		protected override object GetNullValue(ResultProperty mapping)
		{
			throw new NotImplementedException();
		}

		public override object GetDataBaseValue(object outputValue, Type parameterType)
		{
			return outputValue;
		}


		public override bool IsSimpleType()
		{
			throw new NotImplementedException();
		}
	}
}
