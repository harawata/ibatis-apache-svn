
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

#region Using

using System;
using IBatisNet.Common.Logging;
#endregion 



namespace IBatisNet.Common.Logging.Impl
{
	/// <summary>
	/// Summary description for Log4NetLogger.
	/// </summary>
	public class Log4NetLogger : ILog
	{

		#region Fields

		private log4net.ILog _log = null;

		#endregion 

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="log"></param>
		internal Log4NetLogger( log4net.ILog log )
		{
			_log = log;
		}

		#region ILog Members

		/// <summary>
		/// 
		/// </summary>
		public bool IsInfoEnabled
		{
			get { return _log.IsInfoEnabled; }
		}

		/// <summary>
		/// 
		/// </summary>
		public bool IsWarnEnabled
		{
			get { return _log.IsWarnEnabled; }
		}

		/// <summary>
		/// 
		/// </summary>
		public bool IsErrorEnabled
		{
			get { return _log.IsErrorEnabled; }
		}

		/// <summary>
		/// 
		/// </summary>
		public bool IsFatalEnabled
		{
			get { return _log.IsFatalEnabled; }
		}

		/// <summary>
		/// 
		/// </summary>
		public bool IsDebugEnabled
		{
			get { return _log.IsDebugEnabled; }
		}

		/// <summary>
		/// 
		/// </summary>
		public bool IsTraceEnabled
		{
			get { return _log.IsDebugEnabled; }
		}

		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		/// <param name="e"></param>
		public void Info(object message, Exception e)
		{
			_log.Info( message, e );
		}

		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		public void Info(object message)
		{
			_log.Info( message );
		}

		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		/// <param name="e"></param>
		public void Debug(object message, Exception e)
		{
			_log.Debug( message, e );
		}

		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		public void Debug(object message)
		{
			_log.Debug( message );
		}


		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		/// <param name="e"></param>
		public void Warn(object message, Exception e)
		{
			_log.Warn( message, e );
		}

		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		public void Warn(object message)
		{
			_log.Warn( message );
		}


		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		/// <param name="e"></param>
		public void Trace(object message, Exception e)
		{
			_log.Debug( message, e );
		}

		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		public void Trace(object message)
		{
			_log.Debug( message );
		}

		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		/// <param name="e"></param>
		public void Fatal(object message, Exception e)
		{
			_log.Fatal( message, e );
		}

		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		public void Fatal(object message)
		{
			_log.Fatal( message );
		}

		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		/// <param name="e"></param>
		public void Error(object message, Exception e)
		{
			_log.Error( message, e );
		}

		/// <summary>
		/// 
		/// </summary>
		/// <param name="message"></param>
		public void Error(object message)
		{
			_log.Error( message );
		}

		#endregion
	}
}
