
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
	/// Contains parameters that specify Transaction behaviors.
	/// </summary>
	public struct TransactionOptions
	{
		/// <summary>
		/// Length of time that the transaction waits before automatically 
		/// closing itself
		/// </summary>
		public TimeSpan TimeOut;
		/// <summary>
		/// The isolation level of the transaction.
		/// </summary>
		public IsolationLevel IsolationLevel;
	}
}
