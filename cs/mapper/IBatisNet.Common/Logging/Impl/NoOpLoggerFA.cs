
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

namespace IBatisNet.Common.Logging.Impl
{
	/// <summary>
	/// Summary description for NoOpLoggerFA.
	/// </summary>
	public class NoOpLoggerFA : ILoggerFactoryAdapter
	{
		private ILog _nopLogger = null;

		public NoOpLoggerFA()
		{
			_nopLogger = new NoOpLogger();
		}

		#region ILoggerFactoryAdapter Members

		public ILog GetLogger(Type type)
		{
			return _nopLogger;
		}

		ILog Logging.ILoggerFactoryAdapter.GetLogger(string name)
		{
			return _nopLogger;

		}

		#endregion
	}
}
