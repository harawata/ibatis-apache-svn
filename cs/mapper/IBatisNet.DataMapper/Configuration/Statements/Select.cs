
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

using System;
using System.Data;
using System.Xml.Serialization;

namespace IBatisNet.DataMapper.Configuration.Statements
{
	/// <summary>
	/// Summary description for Select.
	/// </summary>
	[Serializable]
	[XmlRoot("select", Namespace="http://ibatis.apache.org")]
	public class Select : Statement
	{
		#region Fields
		[NonSerialized]
		private Generate _generate = null;
		#endregion

		/// <summary>
		/// The Generate tag used by a generated select statement.
		/// (CRUD operation)
		/// </summary>
		[XmlElement("generate",typeof(Generate))]
		public Generate Generate
		{
			get { return _generate; }
			set { _generate = value; }
		}

		/// <summary>
		/// Do not use direclty, only for serialization.
		/// </summary>
		[Obsolete("This public constructor with no parameter is not really obsolete, but is reserved for serialization.", false)]
		public Select():base()
		{}
	}
}
