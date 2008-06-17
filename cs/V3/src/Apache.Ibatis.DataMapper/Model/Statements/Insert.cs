
#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: 383115 $
 * $Date$
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
using System.Xml.Serialization;

using Apache.Ibatis.DataMapper.Exceptions;
using Apache.Ibatis.DataMapper.Model.ParameterMapping;
using Apache.Ibatis.DataMapper.Model.ResultMapping;
using Apache.Ibatis.Common.Utilities.Objects;
using Apache.Ibatis.DataMapper.Model.Cache;
using System.Diagnostics;
using Apache.Ibatis.DataMapper.Model.Sql.External;

#endregion

namespace Apache.Ibatis.DataMapper.Model.Statements
{
	/// <summary>
	/// Represent an insert statement.
	/// </summary>
	[Serializable]
    [DebuggerDisplay("Insert: {Id}")]
	public class Insert : Statement
	{
		[NonSerialized]
		private SelectKey selectKey = null;

        /// <summary>
        /// Extend statement attribute
        /// </summary>
        public override string ExtendStatement
        {
            get { return string.Empty; }
        }

        /// <summary>
        /// The selectKey statement used by an insert statement.
        /// </summary>
        public SelectKey SelectKey
        {
            get { return selectKey; }
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="Insert"/> class.
        /// </summary>
        /// <param name="id">The id.</param>
        /// <param name="parameterClass">The parameter class.</param>
        /// <param name="parameterMap">The parameter map.</param>
        /// <param name="resultClass">The result class.</param>
        /// <param name="resultMaps">The result maps.</param>
        /// <param name="listClass">The list class.</param>
        /// <param name="listClassFactory">The list class factory.</param>
        /// <param name="cacheModel">The cache model.</param>
        /// <param name="remapResults">if set to <c>true</c> [remap results].</param>
        /// <param name="extends">The extends.</param>
        /// <param name="selectKey">The select key.</param>
        /// <param name="sqlSource">The SQL source.</param>
        public Insert(
            string id, 
            Type parameterClass,
            ParameterMap parameterMap,
            Type resultClass,
            ResultMapCollection resultMaps,
            Type listClass,
            IFactory listClassFactory,
            CacheModel cacheModel,
            bool remapResults,
            string extends,
            SelectKey selectKey,
            ISqlSource sqlSource
            )
            : base(id, parameterClass, parameterMap, resultClass, resultMaps, listClass, listClassFactory, cacheModel, remapResults, extends, sqlSource)
		{
            this.selectKey = selectKey;
        }


	}
}