
#region Apache Notice
/*****************************************************************************
 * $Revision: 378879 $
 * $LastChangedDate: 2006-02-19 12:37:22 +0100 (dim., 19 févr. 2006) $
 * $LastChangedBy: gbayon $
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

using IBatisNet.DataMapper.Configuration.ResultMapping;

#endregion 

namespace IBatisNet.DataMapper.TypeHandlers
{
	/// <summary>
	/// DBNull TypeHandler.
	/// </summary>
    internal sealed class DBNullTypeHandler : BaseTypeHandler
	{
		public override object GetValueByName(ResultProperty mapping, IDataReader dataReader)
		{
			return DBNull.Value;
		}

		public override object GetValueByIndex(ResultProperty mapping, IDataReader dataReader) 
		{
			return DBNull.Value;
		}

		public override object GetDataBaseValue(object outputValue, Type parameterType )
		{
			return DBNull.Value;
		}

		public override bool IsSimpleType
		{
			get { return false; }
		}

		public override object ValueOf(Type type, string s)
		{
			return DBNull.Value;
		}

		public override void SetParameter(IDataParameter dataParameter, object parameterValue, string dbType)
		{
			dataParameter.Value = DBNull.Value;
		}

        //public override object NullValue
        //{
        //    get { return null; }
        //}
	}
}