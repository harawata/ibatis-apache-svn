
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
using System.Data;
using System.Collections;
using System.Collections.Specialized;
using System.Xml;

using IBatisNet.DataMapper;
using IBatisNet.Common;

#endregion

namespace IBatisNet.DataMapper.Scope
{
	/// <summary>
	/// The ConfigurationScope maintains the state of the build process.
	/// </summary>
	public class ConfigurationScope
	{
		#region Fields
		
		private ErrorContext _errorContext = null;
		private HybridDictionary _providers = new HybridDictionary();
		private NameValueCollection _properties = new NameValueCollection();

		private XmlDocument _sqlMapConfigDocument = null;
		private XmlDocument _sqlMapDocument = null;
		private XmlNode _nodeContext = null;

		private bool _useConfigFileWatcher = false;
		private bool _useStatementNamespaces = false;
		private bool _isCacheModelsEnabled = false;
		private bool _isCallFromDao = false;

		private SqlMapper _sqlMapper = null;
		private string _sqlMapNamespace = null;
		private DataSource _dataSource = null;
		private bool _isXmlValid = true;

		#endregion
	
		#region Constructors

		/// <summary>
		/// Default constructor
		/// </summary>
		public ConfigurationScope()
		{
			_errorContext = new ErrorContext();

			_providers.Clear();
		}
		#endregion 

		#region Properties

		/// <summary>
		/// Tells us if the xml configuration file validate the schema 
		/// </summary>
		public bool IsXmlValid
		{
			set
			{
				_isXmlValid = value;
			}
			get
			{
				return _isXmlValid;
			}
		}


		/// <summary>
		/// A SqlMap namespace.
		/// </summary>
		public string SqlMapNamespace
		{
			set
			{
				_sqlMapNamespace = value;
			}
			get
			{
				return _sqlMapNamespace;
			}
		}

		/// <summary>
		/// The SqlMapper we are building.
		/// </summary>
		public SqlMapper SqlMapper
		{
			set
			{
				_sqlMapper = value;
			}
			get
			{
				return _sqlMapper;
			}
		}

		/// <summary>
		/// Tell us if we are in a DataAccess context.
		/// </summary>
		public bool IsCallFromDao
		{
			set
			{
				_isCallFromDao = value;
			}
			get
			{
				return _isCallFromDao;
			}
		}

		/// <summary>
		/// Tell us if we cache model is enabled.
		/// </summary>
		public bool IsCacheModelsEnabled
		{
			set
			{
				_isCacheModelsEnabled = value;
			}
			get
			{
				return _isCacheModelsEnabled;
			}
		}
		

		/// <summary>
		/// External data source
		/// </summary>
		public DataSource DataSource
		{
			set
			{
				_dataSource = value;
			}
			get
			{
				return _dataSource;
			}
		}

		/// <summary>
		/// The current context node we are analizing
		/// </summary>
		public XmlNode NodeContext
		{
			set
			{
				_nodeContext = value;
			}
			get
			{
				return _nodeContext;
			}
		}

		/// <summary>
		/// The XML SqlMap config file
		/// </summary>
		public XmlDocument SqlMapConfigDocument
		{
			set
			{
				_sqlMapConfigDocument = value;
			}
			get
			{
				return _sqlMapConfigDocument;
			}
		}

		/// <summary>
		/// A XML SqlMap file
		/// </summary>
		public XmlDocument SqlMapDocument
		{
			set
			{
				_sqlMapDocument = value;
			}
			get
			{
				return _sqlMapDocument;
			}
		}

		/// <summary>
		/// Tell us if we use Configuration File Watcher
		/// </summary>
		public bool UseConfigFileWatcher
		{
			set
			{
				_useConfigFileWatcher = value;
			}
			get
			{
				return _useConfigFileWatcher;
			}
		}

		/// <summary>
		/// Tell us if we use statements namespaces
		/// </summary>
		public bool UseStatementNamespaces
		{
			set
			{
				_useStatementNamespaces = value;
			}
			get
			{
				return _useStatementNamespaces;
			}
		}
		
		/// <summary>
		///  Get the request's error context
		/// </summary>
		public ErrorContext ErrorContext
		{
			get
			{
				return _errorContext;
			}
		}

		/// <summary>
		///  List of providers
		/// </summary>
		public HybridDictionary Providers
		{
			get
			{
				return _providers;
			}
		}

		/// <summary>
		///  List of global properties
		/// </summary>
		public NameValueCollection Properties
		{
			get
			{
				return _properties;
			}
		}

		#endregion 
	}
}