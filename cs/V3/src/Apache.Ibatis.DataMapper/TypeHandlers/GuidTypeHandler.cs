
#region Apache Notice
/*****************************************************************************
 * $Revision: 476843 $
 * $LastChangedDate$
 * $LastChangedBy$
 * 
 * iBATIS.NET Data Mapper
 * Copyright (C) 2008/2005 - The Apache Software Foundation
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
using Apache.Ibatis.DataMapper.Model.ResultMapping;

#endregion 

namespace Apache.Ibatis.DataMapper.TypeHandlers
{
	/// <summary>
	/// Description r�sum�e de GuidTypeHandler.
	/// </summary>
    public sealed class GuidTypeHandler : BaseTypeHandler
	{

        /// <summary>
        /// Gets a column value by the name
        /// </summary>
        /// <param name="mapping"></param>
        /// <param name="dataReader"></param>
        /// <returns></returns>
		public override object GetValueByName(ResultProperty mapping, IDataReader dataReader)
		{
			int index = dataReader.GetOrdinal(mapping.ColumnName);

            return GetValue(index, dataReader);
		}

        /// <summary>
        /// Gets a column value by the index
        /// </summary>
        /// <param name="mapping"></param>
        /// <param name="dataReader"></param>
        /// <returns></returns>
		public override object GetValueByIndex(ResultProperty mapping, IDataReader dataReader) 
		{
		    return GetValue(mapping.ColumnIndex, dataReader);
		}

        /// <summary>
        /// Gets the value.
        /// </summary>
        /// <param name="index">The index.</param>
        /// <param name="dataRecord">The data record.</param>
        /// <returns></returns>
        private object GetValue(int index, IDataRecord dataRecord)
        {
            if (dataRecord.IsDBNull(index))
            {
                return DBNull.Value;
            }
            else if (dataRecord.GetFieldType(index) == typeof(Guid))
            {
                return dataRecord.GetGuid(index);
            }
            else
            {
                return new Guid(Convert.ToString(dataRecord[index]));
            } 
        }

        /// <summary>
        /// Converts the String to the type that this handler deals with
        /// </summary>
        /// <param name="type">the tyepe of the property (used only for enum conversion)</param>
        /// <param name="s">the String value</param>
        /// <returns>the converted value</returns>
		public override object ValueOf(Type type, string s)
		{
			return new Guid(s);
		}

        /// <summary>
        /// Retrieve ouput database value of an output parameter
        /// </summary>
        /// <param name="outputValue">ouput database value</param>
        /// <param name="parameterType">type used in EnumTypeHandler</param>
        /// <returns></returns>
		public override object GetDataBaseValue(object outputValue, Type parameterType )
		{
			return new Guid(outputValue.ToString());
		}


        /// <summary>
        /// Gets a value indicating whether this instance is simple type.
        /// </summary>
        /// <value>
        /// 	<c>true</c> if this instance is simple type; otherwise, <c>false</c>.
        /// </value>
		public override bool IsSimpleType
		{
			get { return true; }
		}

        //public override object NullValue
        //{
        //    get { throw new InvalidCastException("GuidTypeHandler could not cast a null value in Guid field."); }
        //}
	}
}
