
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

#region Imports
using System;
using System.Reflection;
using System.Xml.Serialization;
using IBatisNet.Common.Exceptions;
using IBatisNet.DataMapper.Scope;
using IBatisNet.DataMapper.TypeHandlers;
using IBatisNet.Common.Utilities;
#endregion

namespace IBatisNet.DataMapper.Configuration.ResultMapping
{
	/// <summary>
	/// Summary description for ResultProperty.
	/// </summary>
	[Serializable]
	[XmlRoot("result")]
	public class ResultProperty
	{
		#region Const

		/// <summary>
		/// 
		/// </summary>		
		public const int UNKNOWN_COLUMN_INDEX = -999999;

		#endregion 

		#region Fields
		[NonSerialized]
		private PropertyInfo _propertyInfo;
		[NonSerialized]
		private string _nullValue = string.Empty;
		[NonSerialized]
		private string _property = string.Empty;
		[NonSerialized]
		private string _columnName = string.Empty;
		[NonSerialized]
		private int _columnIndex = UNKNOWN_COLUMN_INDEX;
		[NonSerialized]
		private string _select = string.Empty;
		[NonSerialized]
		private string _nestedResultMapName = string.Empty; 
		[NonSerialized]
		private ResultMap _nestedResultMap = null;
		[NonSerialized]
		private string _dbType = string.Empty;
		[NonSerialized]
		private string _clrType = string.Empty;
		[NonSerialized]
		private bool _isLazyLoad = false;
		[NonSerialized]
		private ITypeHandler _typeHandler = null;
		[NonSerialized]
		private string _callBackName= string.Empty;
		#endregion

		#region Properties

		/// <summary>
		/// Specify the custom type handlers to used.
		/// </summary>
		/// <remarks>Will be an alias to a class wchic implement ITypeHandlerCallback</remarks>
		[XmlAttribute("typeHandler")]
		public string CallBackName
		{
			get { return _callBackName; }
			set { _callBackName = value; }
		}

		/// <summary>
		/// Tell us if we must lazy load this property..
		/// </summary>
		[XmlAttribute("lazyLoad")]
		public bool IsLazyLoad
		{
			get { return _isLazyLoad; }
			set { _isLazyLoad = value; }
		}

		/// <summary>
		/// The typeHandler used to work with the result property.
		/// </summary>
		[XmlIgnoreAttribute]
		public ITypeHandler TypeHandler
		{
			get { return _typeHandler; }
			set { _typeHandler = value; }
		}

		/// <summary>
		/// Give an entry in the 'DbType' enumeration
		/// </summary>
		/// <example >
		/// For Sql Server, give an entry of SqlDbType : Bit, Decimal, Money...
		/// <br/>
		/// For Oracle, give an OracleType Enumeration : Byte, Int16, Number...
		/// </example>
		[XmlAttribute("dbType")]
		public string DbType
		{
			get { return _dbType; }
			set { _dbType = value; }
		}

		
		/// <summary>
		/// Specify the CLR type of the result.
		/// </summary>
		/// <remarks>
		/// The type attribute is used to explicitly specify the property type of the property to be set.
		/// Normally this can be derived from a property through reflection, but certain mappings such as
		/// HashTable cannot provide the type to the framework.
		/// </remarks>
		[XmlAttribute("type")]
		public string CLRType
		{
			get { return _clrType; }
			set { _clrType = value; }
		}

		/// <summary>
		/// Column Index
		/// </summary>
		[XmlAttribute("columnIndex")]
		public int ColumnIndex
		{
			get { return _columnIndex; }
			set { _columnIndex = value; }
		}

		/// <summary>
		/// The name of the statement to retrieve the property
		/// </summary>
		[XmlAttribute("select")]
		public string Select
		{
			get { return _select; }
			set { _select = value; }
		}

		/// <summary>
		/// The name of a nested ResultMap to set the property
		/// </summary>
		[XmlAttribute("resultMapping")]
		public string NestedResultMapName
		{
			get { return _nestedResultMapName; }
			set { _nestedResultMapName = value; }
		}

		/// <summary>
		/// Column Name
		/// </summary>
		[XmlAttribute("column")]
		public string ColumnName
		{
			get { return _columnName; }
			set { _columnName = value; }
		}

		/// <summary>
		/// The property name used to identify the property amongst the others.
		/// </summary>
		[XmlAttribute("property")]
		public string PropertyName
		{
			get { return _property; }
			set { _property = value; }
		}

		/// <summary>
		/// 
		/// </summary>
		[XmlIgnoreAttribute]
		public PropertyInfo PropertyInfo
		{
			get { return _propertyInfo; }
		}

		/// <summary>
		/// Tell if a nullValue is defined.
		/// </summary>
		[XmlIgnoreAttribute]
		public bool HasNullValue
		{
			get { return (_nullValue.Length>0); }
		}

		/// <summary>
		/// Null value replacement.
		/// </summary>
		/// <example>"no_email@provided.com"</example>
		[XmlAttribute("nullValue")]
		public string NullValue
		{
			get { return _nullValue; }
			set { _nullValue = value; }
		}

		/// <summary>
		/// A nested ResultMap use to set a property
		/// </summary>
		[XmlIgnoreAttribute]
		public ResultMap NestedResultMap
		{
			get { return _nestedResultMap; }
			set { _nestedResultMap = value; }
		}
		#endregion

		#region Constructor (s) / Destructor
		/// <summary>
		/// Do not use direclty, only for serialization.
		/// </summary>
		public ResultProperty()
		{
		}
		#endregion

		#region Methods

		/// <summary>
		/// Initialize the PropertyInfo of the result property.
		/// </summary>
		/// <param name="propertyInfo">A PropertyInfoot.</param>
		/// <param name="configScope"></param>
		public void Initialize( ConfigurationScope configScope, PropertyInfo propertyInfo )
		{
			_propertyInfo = propertyInfo;

			if ( propertyInfo != null)
			{
				_typeHandler =  configScope.TypeHandlerFactory.GetTypeHandler(propertyInfo.PropertyType);
			}
			// If we specify a type, it can overrride 
			if (this.CLRType.Length>0)
			{
				_typeHandler = configScope.TypeHandlerFactory.GetTypeHandler(Resources.TypeForName(this.CLRType));
			}
			// If we specify a typeHandler, it can overrride 
			if (this.CallBackName.Length >0)
			{
				configScope.ErrorContext.MoreInfo = "Check the parameter mapping typeHandler attribute '" + this.CallBackName + "' (must be a ITypeHandlerCallback implementation).";
				try 
				{
					Type type = configScope.SqlMapper.GetType(this.CallBackName);
					ITypeHandlerCallback typeHandlerCallback = (ITypeHandlerCallback) Activator.CreateInstance( type );
					_typeHandler = new CustomTypeHandler(typeHandlerCallback);
				}
				catch (Exception e) 
				{
					throw new ConfigurationException("Error occurred during custom type handler configuration.  Cause: " + e.Message, e);
				}
			}
		}

		/// <summary>
		/// Initialize the PropertyInfo of the result property
		/// for AutoMapper
		/// </summary>
		/// <param name="propertyInfo">A PropertyInfoot.</param>
		/// <param name="typeHandlerFactory"></param>
		internal void Initialize(TypeHandlerFactory typeHandlerFactory, PropertyInfo propertyInfo )
		{
			_propertyInfo = propertyInfo;

			_typeHandler =  typeHandlerFactory.GetTypeHandler(propertyInfo.PropertyType);
		}
		#endregion
	}

}
