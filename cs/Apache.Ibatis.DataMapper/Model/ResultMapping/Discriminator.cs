
#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: 469233 $
 * $Date$
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
using System.Collections.Generic;
using System.Diagnostics;
using System.Reflection;
using Apache.Ibatis.Common.Contracts;
using Apache.Ibatis.Common.Logging;
using Apache.Ibatis.DataMapper.DataExchange;

#endregion

namespace Apache.Ibatis.DataMapper.Model.ResultMapping
{
	/// <summary>
	/// Summary description for Discriminator.
	/// </summary>
	[Serializable]
    [DebuggerDisplay("Discriminator: {ColumnName}-")]
    public class Discriminator
	{
        private static readonly ILog logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

		#region Fields
		[NonSerialized]
		private readonly ResultProperty mapping = null;
		/// <summary>
		/// (discriminatorValue (string), ResultMap)
		/// </summary>
		[NonSerialized]
		private readonly IDictionary<string, IResultMap> resultMaps = null;
		/// <summary>
		/// The subMaps name who used this discriminator
		/// </summary>
		[NonSerialized]
		private readonly IList<SubMap> subMaps = null;

		[NonSerialized]
		private readonly string nullValue = string.Empty;
		[NonSerialized]
        private readonly string columnName = string.Empty;
		[NonSerialized]
        private readonly int columnIndex = ResultProperty.UNKNOWN_COLUMN_INDEX;
		[NonSerialized]
        private readonly string dbType = string.Empty;
		[NonSerialized]
        private readonly string clrType = string.Empty;
		[NonSerialized]
        private readonly string callBackName = string.Empty;
		#endregion 

		#region Properties

		/// <summary>
		/// Specify the custom type handlers to used.
		/// </summary>
		/// <remarks>Will be an alias to a class wchic implement ITypeHandlerCallback</remarks>
		public string CallBackName
		{
			get { return callBackName; }
		}

		/// <summary>
		/// Give an entry in the 'DbType' enumeration
		/// </summary>
		/// <example >
		/// For Sql Server, give an entry of SqlDbType : Bit, Decimal, Money...
		/// <br/>
		/// For Oracle, give an OracleType Enumeration : Byte, Int16, Number...
		/// </example>
		public string DbType
		{
			get { return dbType; }
		}
		/// <summary>
		/// Specify the CLR type of the result.
		/// </summary>
		/// <remarks>
		/// The type attribute is used to explicitly specify the property type of the property to be set.
		/// Normally this can be derived from a property through reflection, but certain mappings such as
		/// HashTable cannot provide the type to the framework.
		/// </remarks>
		public string CLRType
		{
			get { return clrType; }
		}

		/// <summary>
		/// Column Index
		/// </summary>
		public int ColumnIndex
		{
			get { return columnIndex; }
		}

		/// <summary>
		/// Column Name
		/// </summary>
		public string ColumnName
		{
			get { return columnName; }
		}

		/// <summary>
		/// Null value replacement.
		/// </summary>
		/// <example>"no_email@provided.com"</example>
		public string NullValue
		{
			get { return nullValue; }
		}

		/// <summary>
		/// The underlying ResultProperty
		/// </summary>
		public ResultProperty ResultProperty
		{
			get { return mapping; }
		}
		#endregion 

		#region Constructor

        /// <summary>
        /// Constructor
        /// </summary>
        /// <param name="callBackName">Name of the call back.</param>
        /// <param name="clrType">Type of the CLR.</param>
        /// <param name="columnIndex">Index of the column.</param>
        /// <param name="columnName">Name of the column.</param>
        /// <param name="dbType">Type of the db.</param>
        /// <param name="nullValue">The null value.</param>
        /// <param name="subMaps">The sub maps.</param>
        /// <param name="resultClass">The result class.</param>
        /// <param name="dataExchangeFactory">The data exchange factory.</param>
        public Discriminator(
            string callBackName,
            string clrType,
            int columnIndex,
            string columnName,
            string dbType,
            string nullValue,
            IList<SubMap> subMaps,
            Type resultClass,
            DataExchangeFactory dataExchangeFactory)
		{
            Contract.Require.That(columnName, Is.Not.Null & Is.Not.Empty).When("retrieving argument columnName in Discriminator constructor");

            this.nullValue = nullValue;
            this.callBackName = callBackName;
            this.clrType = clrType;
            this.columnIndex = columnIndex;
            this.columnName = columnName;
            this.dbType = dbType;
            this.subMaps = subMaps;

			resultMaps = new Dictionary<string, IResultMap>();

            mapping = new ResultProperty(
                "value",
                columnName,
                columnIndex,
                clrType,
                callBackName,
                dbType,
                false,
                string.Empty,
                nullValue,
                string.Empty,
                resultClass,
                dataExchangeFactory,
                null);

            //mapping.Initialize(configScope, resultClass);
		}
		#endregion 

		#region Methods

        /// <summary>
        /// Initialize the Discriminator
        /// </summary>
        /// <param name="modelStore">The model store.</param>
        public void Initialize(IModelStore modelStore)
		{
			// Set the ResultMaps
			int count = subMaps.Count;
			for(int index=0; index<count; index++)
			{
				SubMap subMap = subMaps[index];
                resultMaps.Add(subMap.DiscriminatorValue, modelStore.GetResultMap(subMap.ResultMapName) );
			}
		}

		/// <summary>
		/// Find the SubMap to use.
		/// </summary>
		/// <param name="discriminatorValue">the discriminator value</param>
		/// <returns>The find ResultMap</returns>
		public IResultMap GetSubMap(string discriminatorValue)
		{
            IResultMap resultMap = null;
            if (!resultMaps.TryGetValue(discriminatorValue, out resultMap))
            {
                logger.Info("There's no SubMap for the key '" + discriminatorValue + "'. Checks yours mapping files.");
            }
            return resultMap;
		}

		#endregion 


	}
}
