
#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: $
 * $Date: $
 * Author : Gilles Bayon
 * iBATIS.NET Data Mapper
 * Copyright (C) 2004 - Apache Fondation
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
using System.Reflection;
using System.Xml;
using System.Xml.Serialization;

using IBatisNet.Common.Exceptions;

#endregion

namespace IBatisNet.DataMapper.Configuration.ResultMapping
{
	/// <summary>
	/// Summary description for Discriminator.
	/// </summary>
	[Serializable]
	[XmlRoot("discriminator")]
	public class Discriminator
	{

		#region Fields
		[NonSerialized]
		private string _discriminatorColumn = string.Empty;
		[NonSerialized]
		private IDiscriminatorFormula _formula = null;
		/// <summary>
		/// (discriminatorValue (string), ResulMap)
		/// </summary>
		[NonSerialized]
		private HybridDictionary _resultMaps = null;
		/// <summary>
		/// The subMaps name who used this discriminator
		/// </summary>
		[NonSerialized]
		private ArrayList _subMaps = null;
		[NonSerialized]
		private string _formulaClassName = string.Empty;//typeof(DefaultFormula).FullName;
		#endregion 

		#region Properties

		/// <summary>
		/// Column Name
		/// </summary>
		[XmlAttribute("formula")]
		public string FormulaClassName
		{
			get { return _formulaClassName; }
			set { _formulaClassName = value; }
		}

		/// <summary>
		/// Column Name
		/// </summary>
		[XmlAttribute("column")]
		public string DiscriminatorColumn
		{
			get { return _discriminatorColumn; }
			set { _discriminatorColumn = value; }
		}

		/// <summary>
		/// A formula to calculate the discriminator value to use
		/// </summary>
		[XmlIgnoreAttribute]
		public IDiscriminatorFormula Formula
		{
			get
			{
				return _formula;
			}	
			set
			{
				_formula = value;
			}	
		}
		#endregion 

		#region Constructor

		/// <summary>
		/// Constructor
		/// </summary>
		public Discriminator()
		{
			_resultMaps = new HybridDictionary();
			_subMaps = new ArrayList();
		}
		#endregion 

		#region Methods

		/// <summary>
		/// Initialize the Discriminator
		/// </summary>
		/// <param name="sqlMap"></param>
		public void Initialize(SqlMapper sqlMap)
		{
			// Set the formula
			if (_formulaClassName.Length == 0)
			{
				_formula = new DefaultFormula(_discriminatorColumn);
			}
			else
			{
				Type formulaType = sqlMap.GetType(_formulaClassName);
				_formula = Activator.CreateInstance(formulaType) as IDiscriminatorFormula;
			}

			// Set the ResultMaps
			for(int index=0; index<_subMaps.Count; index++)
			{
				SubMap subMap = _subMaps[index] as SubMap;
				_resultMaps.Add(subMap.DiscriminatorValue, sqlMap.GetResultMap( subMap.ResulMapName ) );
			}
		}

		/// <summary>
		/// Add a subMap that the discrimator must treat
		/// </summary>
		/// <param name="subMap">A subMap</param>
		public void Add(SubMap subMap)
		{
			_subMaps.Add(subMap);
		}

		/// <summary>
		/// Find the SubMap to use.
		/// </summary>
		/// <param name="dataReader">A IDataReader which contains result values</param>
		/// <returns>The find ResultMap</returns>
		public ResultMap GetResultMap(IDataReader dataReader)
		{
			// récupérer le resultmap correspondant
			// 1/ récupérer la valeur de la colonne (en string)
			// 2/ récupérer 
			string discriminatorValue = _formula.GetDiscriminatorValue(dataReader);
			return _resultMaps[discriminatorValue] as ResultMap;
		}
		#endregion 


	}
}
