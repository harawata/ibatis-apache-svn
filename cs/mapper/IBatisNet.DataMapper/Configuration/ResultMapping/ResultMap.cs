
#region Apache Notice
/*****************************************************************************
 * $Revision$
 * $LastChangedDate$
 * $LastChangedBy$
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
using System.Reflection;
using System.Xml;
using System.Xml.Serialization;
using IBatisNet.Common.Exceptions;
using IBatisNet.Common.Utilities.Objects;
using IBatisNet.Common.Utilities.TypesResolver;
using IBatisNet.DataMapper.Configuration.Serializers;
using IBatisNet.DataMapper.DataExchange;
using IBatisNet.DataMapper.Exceptions;
using IBatisNet.DataMapper.Scope;
using IBatisNet.Common.Utilities;

#endregion

namespace IBatisNet.DataMapper.Configuration.ResultMapping
{
	/// <summary>
	/// Summary description for ResultMap.
	/// </summary>
	[Serializable]
	[XmlRoot("resultMap", Namespace="http://ibatis.apache.org/mapping")]
	public class ResultMap
	{
		/// <summary>
		/// Token for xml path to argument constructor elements.
		/// </summary>
		public static BindingFlags ANY_VISIBILITY_INSTANCE = BindingFlags.Instance | BindingFlags.Public | BindingFlags.NonPublic;

		/// <summary>
		/// Token for xml path to result elements.
		/// </summary>
		private const string XML_RESULT = "result";

		/// <summary>
		/// Token for xml path to result elements.
		/// </summary>
		private const string XML_CONSTRUCTOR_ARGUMENT = "constructor/argument";

		/// <summary>
		/// Token for xml path to discriminator elements.
		/// </summary>
		private const string XML_DISCRIMNATOR = "discriminator";

		/// <summary>
		/// Token for xml path to subMap elements.
		/// </summary>
		private const string XML_SUBMAP = "subMap";


		#region Fields
		[NonSerialized]
		private string _id = string.Empty;
		[NonSerialized]
		private string _className = string.Empty;
		[NonSerialized]
		private string _extendMap = string.Empty;
		[NonSerialized]
		private Type _class = null;
		
		[NonSerialized]
		private ResultPropertyCollection _properties = new ResultPropertyCollection();
		[NonSerialized]
		private ResultPropertyCollection _parameters = new ResultPropertyCollection();

		[NonSerialized]
		private Discriminator _discriminator = null;
		[NonSerialized]
		private string _sqlMapNameSpace = string.Empty;
		[NonSerialized]
		private IFactory _objectFactory = null;
		[NonSerialized]
		private DataExchangeFactory _dataExchangeFactory = null;
		[NonSerialized]
		private IDataExchange _dataExchange = null;
		#endregion

		#region Properties

		/// <summary>
		/// The sqlMap namespace
		/// </summary>
		[XmlIgnore]
		public string SqlMapNameSpace
		{
			get { return _sqlMapNameSpace; }	
			set { _sqlMapNameSpace = value; }	
		}

		/// <summary>
		/// The discriminator used to choose the good SubMap
		/// </summary>
		[XmlIgnore]
		public Discriminator Discriminator
		{
			get { return _discriminator; }	
			set { _discriminator = value; }	
		}

		/// <summary>
		/// The collection of ResultProperty.
		/// </summary>
		[XmlIgnore]
		public ResultPropertyCollection Properties
		{
			get { return _properties; }
		}

		/// <summary>
		/// The collection of constructor parameters.
		/// </summary>
		[XmlIgnore]
		public ResultPropertyCollection Parameters
		{
			get { return _parameters; }
		}

		/// <summary>
		/// Identifier used to identify the resultMap amongst the others.
		/// </summary>
		/// <example>GetProduct</example>
		[XmlAttribute("id")]
		public string Id
		{
			get { return _id; }
			set 
			{ 
				if ((value == null) || (value.Length < 1))
					throw new ArgumentNullException("The id attribute is mandatory in a ResultMap tag.");

				_id = value; 
			}
		}

		/// <summary>
		/// Extend ResultMap attribute
		/// </summary>
		[XmlAttribute("extends")]
		public string ExtendMap
		{
			get { return _extendMap; }
			set { _extendMap = value; }
		}

		/// <summary>
		/// The output type class of the resultMap.
		/// </summary>
		[XmlIgnore]
		public Type Class
		{
			get { return _class; }
		}


		/// <summary>
		/// The output class name of the resultMap.
		/// </summary>
		/// <example>Com.Site.Domain.Product</example>
		[XmlAttribute("class")]
		public string ClassName
		{
			get { return _className; }
			set 
			{ 
				if ((value == null) || (value.Length < 1))
				{
					throw new ArgumentNullException("The class attribute is mandatory in a ResultMap tag.");				
				}

				_className = value; 
			}
		}

		/// <summary>
		/// Sets the IDataExchange
		/// </summary>
		[XmlIgnore]
		public IDataExchange DataExchange
		{
			set { _dataExchange = value; }
		}
		#endregion

		#region Constructor (s) / Destructor
		/// <summary>
		/// Do not use direclty, only for serialization.
		/// </summary>
		public ResultMap(DataExchangeFactory dataExchangeFactory)
		{
			_dataExchangeFactory = dataExchangeFactory;
		}
		#endregion

		#region Methods

		#region Configuration
		/// <summary>
		/// Initialize the resultMap from an xmlNode..
		/// </summary>
		/// <param name="configScope"></param>
		public void Initialize( ConfigurationScope configScope )
		{
			try
			{
				_class = configScope.SqlMapper.TypeHandlerFactory.GetType(_className);
				_dataExchange = _dataExchangeFactory.GetDataExchangeForClass(_class);

				// Load the child node
				GetChildNode(configScope);
			}
			catch(Exception e)
			{
				throw new ConfigurationException(
					string.Format("Could not configure ResultMap. ResultMap named \"{0}\" not found, failed. {1} Cause: {2}", _id, Environment.NewLine, e.Message)
					);
			}
		}


		/// <summary>
		/// Get the result properties and the subMap properties.
		/// </summary>
		/// <param name="configScope"></param>
		private void GetChildNode(ConfigurationScope configScope)
		{
			ResultProperty mapping = null;
			SubMap subMap = null;

			#region Load the parameters constructor
			XmlNodeList nodeList = configScope.NodeContext.SelectNodes( DomSqlMapBuilder.ApplyMappingNamespacePrefix(XML_CONSTRUCTOR_ARGUMENT), configScope.XmlNamespaceManager);
			if (nodeList.Count>0)
			{
				Type[] parametersType= new Type[nodeList.Count];
				string[] parametersName = new string[nodeList.Count];
				for( int i =0; i<nodeList.Count; i++)
				{
					ArgumentProperty argumentMapping = ArgumentPropertyDeSerializer.Deserialize( nodeList[i], configScope );
					_parameters.Add( argumentMapping  );
					parametersName[i] = argumentMapping.ArgumentName;
				}
				ConstructorInfo constructorInfo = this.GetConstructor( _class, parametersName );
				for(int i=0;i<_parameters.Count;i++)
				{
					ArgumentProperty argumentMapping = (ArgumentProperty)_parameters[i];

					configScope.ErrorContext.MoreInfo = "initialize argument property : " + argumentMapping.ArgumentName;
					argumentMapping.Initialize( configScope, constructorInfo);
					parametersType[i] = argumentMapping.MemberType;
				}		
				// Init the object factory
				_objectFactory = configScope.SqlMapper.ObjectFactory.CreateFactory(_class, parametersType);
			}
			else
			{
				if (Type.GetTypeCode(_class) == TypeCode.Object)
				{
					_objectFactory = configScope.SqlMapper.ObjectFactory.CreateFactory(_class, Type.EmptyTypes);
				}
			}

			#endregion

			#region Load the Result Properties

			foreach ( XmlNode resultNode in configScope.NodeContext.SelectNodes( DomSqlMapBuilder.ApplyMappingNamespacePrefix(XML_RESULT), configScope.XmlNamespaceManager) )
			{
				mapping = ResultPropertyDeSerializer.Deserialize( resultNode, configScope );
					
				configScope.ErrorContext.MoreInfo = "initialize result property: "+mapping.PropertyName;

				mapping.Initialize( configScope, _class );

				this.AddResultPropery( mapping  );
			}
			#endregion 

			#region Load the Discriminator Property

			XmlNode discriminatorNode = configScope.NodeContext.SelectSingleNode(DomSqlMapBuilder.ApplyMappingNamespacePrefix(XML_DISCRIMNATOR), configScope.XmlNamespaceManager);
			if (discriminatorNode != null)
			{
				configScope.ErrorContext.MoreInfo = "initialize discriminator";

				this.Discriminator = DiscriminatorDeSerializer.Deserialize(discriminatorNode, configScope); 
				this.Discriminator.SetMapping( configScope, _class );
			}
			#endregion 

			#region Load the SubMap Properties

			if (configScope.NodeContext.SelectNodes(DomSqlMapBuilder.ApplyMappingNamespacePrefix(XML_SUBMAP), configScope.XmlNamespaceManager).Count>0 && this.Discriminator==null)
			{
				throw new ConfigurationException("The discriminator is null, but somehow a subMap was reached.  This is a bug.");
			}
			foreach ( XmlNode resultNode in configScope.NodeContext.SelectNodes(DomSqlMapBuilder.ApplyMappingNamespacePrefix(XML_SUBMAP), configScope.XmlNamespaceManager) )
			{
				configScope.ErrorContext.MoreInfo = "initialize subMap";
				subMap = SubMapDeSerializer.Deserialize(resultNode, configScope);

				subMap.ResultMapName = this.SqlMapNameSpace + DomSqlMapBuilder.DOT + subMap.ResultMapName;
				this.Discriminator.Add( subMap );
			}
			#endregion 
		}

		/// <summary>
		/// Finds the constructor that takes the parameters.
		/// </summary>
		/// <param name="type">The <see cref="System.Type"/> to find the constructor in.</param> 
		/// <param name="parametersName">The parameters name to use to find the appropriate constructor.</param>
		/// <returns>
		/// An <see cref="ConstructorInfo"/> that can be used to create the type with 
		/// the specified parameters.
		/// </returns>
		/// <exception cref="DataMapperException">
		/// Thrown when no constructor with the correct signature can be found.
		/// </exception> 
		public ConstructorInfo GetConstructor( System.Type type, string[] parametersName )
		{
			ConstructorInfo[] candidates = type.GetConstructors(ANY_VISIBILITY_INSTANCE);
			foreach( ConstructorInfo constructor in candidates )
			{
				ParameterInfo[] parameters = constructor.GetParameters();

				if( parameters.Length == parametersName.Length )
				{
					bool found = true;

					for( int j = 0; j < parameters.Length; j++ )
					{
						bool ok = (parameters[ j ].Name == parametersName[ j ]);
						if( !ok )
						{
							found = false;
							break;
						}
					}

					if( found )
					{
						return constructor;
					}
				}
			}
			throw new DataMapperException( "Cannot find an appropriate constructor which map parameters in class: "+ type.Name );
		}

		#endregion

		/// <summary>
		/// Create an instance Of result.
		/// </summary>
		/// <param name="parameters">
		/// An array of values that matches the number, order and type 
		/// of the parameters for this constructor. 
		/// </param>
		/// <returns>An object.</returns>
		public object CreateInstanceOfResult(object[] parameters)
		{
			TypeCode typeCode = Type.GetTypeCode(_class);

			if (typeCode == TypeCode.Object)
			{
				return _objectFactory.CreateInstance(parameters);
			}
			else
			{
                return TypeUtils.InstantiatePrimitiveType(typeCode);
			}
		}

		/// <summary>
		/// Add a ResultProperty to the list of ResultProperty.
		/// </summary>
		/// <param name="property">The property to add.</param>
		public void AddResultPropery(ResultProperty property)
		{
			_properties.Add( property  );
		}

		/// <summary>
		/// Set the value of an object property.
		/// </summary>
		/// <param name="target">The object to set the property.</param>
		/// <param name="property">The result property to use.</param>
		/// <param name="dataBaseValue">The database value to set.</param>
		public void SetValueOfProperty( ref object target, ResultProperty property, object dataBaseValue )
		{
			_dataExchange.SetData(ref target, property, dataBaseValue);
		}

		/// <summary>
		/// 
		/// </summary>
		/// <param name="dataReader"></param>
		/// <returns></returns>
		public ResultMap ResolveSubMap(IDataReader dataReader)
		{
			ResultMap subMap = this;
			if (_discriminator != null)
			{	
				ResultProperty mapping = _discriminator.ResultProperty;
				object dataBaseValue = mapping.GetDataBaseValue( dataReader );
				subMap = _discriminator.GetSubMap( dataBaseValue.ToString() );

				if (subMap == null) 
				{
					subMap = this;
				} 
				else if (subMap != this) 
				{
					subMap = subMap.ResolveSubMap(dataReader);
				}
			}
			return subMap;
		}

		
		#endregion
	}
}
