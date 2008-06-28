
#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: 383115 $
 * $Date$
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

#region Imports
using System;
#endregion

namespace Apache.Ibatis.Common.Transaction
{
	/// <summary>
	/// Specifies the isolation level of a transaction.
	/// </summary>
	public enum IsolationLevel : int
	{
		/// <summary>
		/// Volatile data can be read but not modified, 
		/// and no new data can be added during the transaction.
		/// </summary>
		Serializable = 0,
		/// <summary>
		/// Volatile data can be read but not modified during the transaction. 
		/// New data may be added during the transaction.
		/// </summary>
		RepeatableRead = 1, 
		/// <summary>
		/// Volatile data cannot be read during the transaction, but can be modified.
		/// </summary>
		ReadCommitted = 2,
		/// <summary>
		/// Volatile data can be read and modified during the transaction.
		/// </summary>
		ReadUncommitted = 3,
		/// <summary>
		/// Volatile data can be read but not modified, 
		/// and no new data can be added during the transaction.
		/// </summary>
		Unspecified = 4
	}
}
