
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
using System.Collections.Specialized;
using System.Configuration;
using System.IO;
using log4net.Config;

namespace IBatisNet.Common.Logging.Impl
{
	/// <summary>
	/// Concrete subclass of ILoggerFactoryAdapter specific to log4n. 
	/// </summary>
	public class Log4NetLoggerFA : ILoggerFactoryAdapter
	{
		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="properties"></param>
		public Log4NetLoggerFA(NameValueCollection properties)
		{
			string configurationType = string.Empty;
			
			if ( properties["configType"] != null )
			{
				configurationType = properties["configType"].ToUpper();	
			}

			string configurationFile = string.Empty;
			if ( properties["configFile"] != null )
			{
				configurationFile = properties["configFile"];			
			}

			if ( configurationType == "FILE" || configurationType == "FILE-WATCH" )
			{
				if ( configurationFile == string.Empty )
					throw new ConfigurationException( "Configration property 'configurationFile' must be set for log4Net configuration of type 'FILE'."  );
			
				if ( !File.Exists( configurationFile ) )
					throw new ConfigurationException( "log4net configuration file '" + configurationFile + "' does not exists" );
			}

			switch ( configurationType )
			{
				case "INLINE":
					DOMConfigurator.Configure();
					break;
				case "FILE":
					DOMConfigurator.Configure( new FileInfo( configurationFile ) );
					break;
				case "FILE-WATCH":
					DOMConfigurator.ConfigureAndWatch( new FileInfo( configurationFile ) );
					break;
				default:
					BasicConfigurator.Configure();
					break;
			}
		}

		#region ILoggerFactoryAdapter Members

		/// <summary>
		/// Get a ILog instance by type name 
		/// </summary>
		/// <param name="name"></param>
		/// <returns></returns>
		public ILog GetLogger(string name)
		{
			return new Log4NetLogger( log4net.LogManager.GetLogger( name ) );
		}

		/// <summary>
		/// Get a ILog instance by type 
		/// </summary>
		/// <param name="type"></param>
		/// <returns></returns>
		public ILog GetLogger(Type type)
		{
			return new Log4NetLogger( log4net.LogManager.GetLogger( type ) );
		}

		#endregion
	}
}
