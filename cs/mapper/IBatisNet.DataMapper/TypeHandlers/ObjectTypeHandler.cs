
#region Apache Notice
/*****************************************************************************
 * $Revision$
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
using System.Data;
using System.Globalization;

using IBatisNet.DataMapper.Configuration.ResultMapping;
#endregion 

namespace IBatisNet.DataMapper.TypeHandlers
{
	/// <summary>
	/// Description r�sum�e de ObjectTypeHandler.
	/// </summary>
    internal sealed class ObjectTypeHandler : BaseTypeHandler
	{

		/// <summary>
		/// 
		/// </summary>
		/// <param name="mapping"></param>
		/// <param name="dataReader"></param>
		/// <returns></returns>
		public override object GetValueByName(ResultProperty mapping, IDataReader dataReader)
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

		public override object GetValueByIndex(ResultProperty mapping, IDataReader dataReader) 
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

		public override object ValueOf(Type type, string s)
		{
			return s;
		}

		public override object GetDataBaseValue(object outputValue, Type parameterType )
		{
			return outputValue;
		}


		public override bool IsSimpleType
		{
			get { return false; }
		}

        //public override object NullValue
        //{
        //    get { return null; }
        //}

	}
}
