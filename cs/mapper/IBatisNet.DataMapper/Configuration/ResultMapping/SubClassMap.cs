
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
	/// Summary description for  SubClassMap.
	/// </summary>
	[Serializable]
	public class SubMap
	{

		#region Fields

		/// <summary>
		/// (discriminatorValue (string), ResulMap)
		/// </summary>
		private HybridDictionary _resultMaps = null;
		private IDiscriminatorFormula _formula = null;
		private string _discriminatorColumn = string.Empty;

		#endregion 

		/// <summary>
		/// A formula to calculate the discriminator value to use
		/// </summary>
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

		/// <summary>
		/// Constructor
		/// </summary>
		public SubMap(string discriminatorColumn)
		{
			_formula = new DefaultFormula(discriminatorColumn);
			_discriminatorColumn = discriminatorColumn;
			_resultMaps = new HybridDictionary();
		}

		/// <summary>
		/// Add a subclass resultMap
		/// </summary>
		/// <param name="discriminatorValue">The discriminator Value which identify the ResultMap</param>
		/// <param name="resultMap">The corresponding ResultMap</param>
		public void Add(string discriminatorValue, ResultMap resultMap)
		{
			_resultMaps.Add(discriminatorValue, resultMap);
		}

//		<resultMap name="document" class="Document">
//			<result property="Id" column="Document_ID"/>
//			<result property="Title" column="Document_Title"/>
//			<subMap column="Document_Type" 
//					value="Book" -- discriminator value
//					formula="DefaultFormula, iBatisNet.DataMapper" -- discriminator type (DefaultFormula is default), else used an aliasType wich implement IDiscriminatorFormula)
//					resultMap="bookResultMap"
//			/>
//		</resultMap>

		/// <summary>
		/// Find the ResultMap to use.
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
	}
}
