
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

namespace IBatisNet.Common.Logging
{
	/// <summary>
	/// The 8 logging levels used by Log are (in order): 
	/// </summary>
	public enum LogLevel
	{
		/// <summary>
		/// 
		/// </summary>
		All   = 0,
		/// <summary>
		/// 
		/// </summary>
		Trace = 1,
		/// <summary>
		/// 
		/// </summary>
		Debug = 2,
		/// <summary>
		/// 
		/// </summary>
		Info  = 3,
		/// <summary>
		/// 
		/// </summary>
		Warn  = 4,
		/// <summary>
		/// 
		/// </summary>
		Error = 5,
		/// <summary>
		/// 
		/// </summary>
		Fatal = 6,
		/// <summary>
		/// 
		/// </summary>
		Off  = 7,
	}

	/// <summary>
	/// A simple logging interface abstracting logging APIs. 
	/// </summary>
	public interface ILog
	{
		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		void Debug( object message );
		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		/// <param name="e"></param>
		void Debug( object message, Exception e );

		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		void Error( object message );
		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		/// <param name="e"></param>
		void Error( object message, Exception e );

		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		void Fatal( object message );
		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		/// <param name="e"></param>
		void Fatal( object message, Exception e );

		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		void Info( object message );
		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		/// <param name="e"></param>
		void Info( object message, Exception e );

		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		void Trace( object message );
		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		/// <param name="e"></param>
		void Trace( object message, Exception e );

		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		void Warn( object message );
		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		/// <param name="e"></param>
		void Warn( object message, Exception e );

		/// <summary>
		/// 
		/// </summary>
		bool IsDebugEnabled
		{
			get;
		}

		/// <summary>
		/// 
		/// </summary>
		bool IsErrorEnabled
		{
			get;
		}

		/// <summary>
		/// 
		/// </summary>
		bool IsFatalEnabled
		{
			get;
		}

		/// <summary>
		/// 
		/// </summary>
		bool IsInfoEnabled
		{
			get;
		}

		/// <summary>
		/// 
		/// </summary>
		bool IsTraceEnabled
		{
			get;
		}

		/// <summary>
		/// 
		/// </summary>
		bool IsWarnEnabled
		{
			get;
		}
	}
}
