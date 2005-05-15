
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
	/// Summary description for ITypeHandler.
	/// </summary>
	public interface ITypeHandler
	{
		/// <summary>
		/// 
		/// </summary>
		/// <param name="mapping"></param>
		/// <param name="dataReader"></param>
		/// <returns></returns>
		object GetDataBaseValue(ResultProperty mapping, IDataReader dataReader);

		/// <summary>
		/// 
		/// </summary>
		/// <returns></returns>
		bool IsSimpleType();

		/// <summary>
		/// Retrieve ouput database value of an output parameter
		/// </summary>
		/// <param name="outputValue">ouput database value</param>
		/// <param name="parameterType">type used in EnumTypeHandler</param>
		/// <returns></returns>
		object GetDataBaseValue(object outputValue, Type parameterType);

		/// <summary>
		/// Performs processing on a value before it is used to set
		/// the parameter of a IDbCommand.
		/// </summary>
		/// <param name="mapping">The mapping between data parameter and object property.</param>
		/// <param name="dataParameter"></param>
		/// <param name="parameterValue">The value to be set</param>
		/// <param name="dbType">Data base type</param>
		void SetParameter(ParameterProperty mapping, IDataParameter dataParameter, object parameterValue, string dbType);

	}
}
