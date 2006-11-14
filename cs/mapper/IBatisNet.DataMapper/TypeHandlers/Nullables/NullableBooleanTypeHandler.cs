#region Apache Notice
/*****************************************************************************
 * $Revision: 378879 $
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

#if dotnet2
#region Using
using System;
using System.Data;

using System.Collections.Generic;
using IBatisNet.DataMapper.Configuration.ParameterMapping;
using IBatisNet.DataMapper.Configuration.ResultMapping;
#endregion

namespace IBatisNet.DataMapper.TypeHandlers.Nullables
{
    public sealed class NullableBooleanTypeHandler : BaseTypeHandler
	{

        public override void SetParameter(IDataParameter dataParameter, object parameterValue, string dbType)
        {
            bool? nullableValue = (bool?)parameterValue;

            if (nullableValue.HasValue)
            {
                dataParameter.Value = nullableValue.Value;
            }
            else
            {
                dataParameter.Value = DBNull.Value;
            }
        }

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
				return DBNull.Value;
			}
			else
			{
				// Don't used dataReader.GetInt32 to fix oracle who alwray return decimal type
                return new bool?(Convert.ToBoolean(dataReader.GetValue(index)));
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
                return new bool?( Convert.ToBoolean(dataReader.GetValue(mapping.ColumnIndex)));
            }
		}

		public override object GetDataBaseValue(object outputValue, Type parameterType )
		{
            return new bool?(Convert.ToBoolean(outputValue));
		}

		public override object ValueOf(Type type, string s)
		{
            return new bool?(Convert.ToBoolean(s));
		}


		public override bool IsSimpleType
		{
			get { return true; }
		}


        public override object NullValue
        {
            get { return new bool?(); }
        }
    }
}

#endif
