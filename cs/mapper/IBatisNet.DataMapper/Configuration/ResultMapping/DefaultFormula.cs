
#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: $
 * $Date: $
 * Author : Gilles Bayon
 * iBATIS.NET Data Mapper
 * Copyright (C) 2004 - Apache Fondation
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

using IBatisNet.Common.Exceptions;

#endregion

namespace IBatisNet.DataMapper.Configuration.ResultMapping
{
	/// <summary>
	///  It is a basic string comparaison formula based on the string column value.
	/// </summary>
	public class DefaultFormula : IDiscriminatorFormula
	{

		#region Fields

		private string _discriminatorColumn = string.Empty;

		#endregion 

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="discriminatorColumn"></param>
		public DefaultFormula(string discriminatorColumn)
		{
			_discriminatorColumn = discriminatorColumn;
		}
		#region IDiscriminatorFormula members

		/// <summary>
		/// Calulate the discriminator value
		/// from the IDataReader fields
		/// </summary>
		/// <param name="dataReader">An IDataReader</param>
		/// <returns>Return the discriminator value</returns>
		public string GetDiscriminatorValue(IDataReader dataReader)
		{
			return dataReader[_discriminatorColumn].ToString();
		}

		#endregion
	}
}
