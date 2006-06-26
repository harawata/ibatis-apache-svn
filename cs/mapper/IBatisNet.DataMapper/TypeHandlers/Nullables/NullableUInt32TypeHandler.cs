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
	/// <summary>
	/// Summary description for Int32TypeHandler.
	/// </summary>
    internal class NullableUInt32TypeHandler : BaseTypeHandler
    {

        public override void SetParameter(IDataParameter dataParameter, object parameterValue, string dbType)
        {
            UInt32? nullableValue = (UInt32?)parameterValue;

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
                return new UInt32?(Convert.ToUInt32(dataReader.GetValue(index)));
            }
        }

        public override object GetValueByIndex(ResultProperty mapping, IDataReader dataReader)
        {
            if (dataReader.IsDBNull(mapping.ColumnIndex) == true)
            {
                return DBNull.Value;
            }
            else
            {
                return new UInt32?(Convert.ToUInt32(dataReader.GetValue(mapping.ColumnIndex)));
            }
        }

        public override object GetDataBaseValue(object outputValue, Type parameterType)
        {
            return new UInt32?(Convert.ToUInt32(outputValue));
        }

        public override object ValueOf(Type type, string s)
        {
            return new UInt32?(UInt32.Parse(s));
        }


        public override bool IsSimpleType
        {
            get { return true; }
        }


        public override object NullValue
        {
            get { return new UInt32?(); }
        }

        //public override bool Equals(object x, object y)
        //{
        //    //get boxed values.
        //    Int32? xTyped = (Int32?)x;
        //    return xTyped.Equals(y);
        //}
    }
}

#endif