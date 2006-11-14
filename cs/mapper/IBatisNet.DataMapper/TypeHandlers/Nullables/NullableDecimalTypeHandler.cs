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
using System.Globalization;

using System.Collections.Generic;
using IBatisNet.DataMapper.Configuration.ParameterMapping;
using IBatisNet.DataMapper.Configuration.ResultMapping;
#endregion

namespace IBatisNet.DataMapper.TypeHandlers.Nullables
{
    public class NullableDecimalTypeHandler : BaseTypeHandler
    {

        /// <summary>
        /// Sets a parameter on a IDbCommand
        /// </summary>
        /// <param name="dataParameter">the parameter</param>
        /// <param name="parameterValue">the parameter value</param>
        /// <param name="dbType">the dbType of the parameter</param>
        public sealed override void SetParameter(IDataParameter dataParameter, object parameterValue, string dbType)
        {
            decimal? nullableValue = (decimal?)parameterValue;

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
                return new decimal?( dataReader.GetDecimal(index) );
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
                return new decimal?( dataReader.GetDecimal(mapping.ColumnIndex) );
            }
        }

        public override object GetDataBaseValue(object outputValue, Type parameterType)
        {
            return new char?(Convert.ToChar(outputValue));
        }

        public override object ValueOf(Type type, string s)
        {
            CultureInfo culture = new CultureInfo("en-US");
            // value decimal must be  in format ######.##
            // where . is separator for decimal
            return new decimal?(decimal.Parse(s, culture));
        }


        public override bool IsSimpleType
        {
            get { return true; }
        }


        public override object NullValue
        {
            get { return new decimal?(); }
        }
    }
}

#endif