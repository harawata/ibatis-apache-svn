#region Apache Notice
/*****************************************************************************
 * $Revision: 576082 $
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
using System.Collections.Specialized;
using System.Data;
using System.Reflection;
using System.Xml;
using System.Xml.Serialization;
using Apache.Ibatis.Common.Exceptions;
using Apache.Ibatis.Common.Utilities.Objects;
using Apache.Ibatis.DataMapper.Configuration.Serializers;
using Apache.Ibatis.DataMapper.DataExchange;
using Apache.Ibatis.DataMapper.Exceptions;
using Apache.Ibatis.DataMapper.Scope;
using Apache.Ibatis.Common.Utilities;
using System.Collections.Generic;

#endregion

namespace Apache.Ibatis.DataMapper.Model.ResultMapping
{
    /// <summary>
    /// This is a grouping of ResultMapping objects used to map results back to objects
    /// </summary>
    public interface IResultMap
    {
        /// <summary>
        /// The collection of constructor parameters.
        /// </summary>
        ResultPropertyCollection Parameters { get; }
        
        /// <summary>
        /// The collection of ResultProperty.
        /// </summary>
        ResultPropertyCollection Properties { get; }

        /// <summary>
        /// The GroupBy Properties.
        /// </summary>
        ResultPropertyCollection GroupByProperties { get; }

        /// <summary>
        /// Identifier used to identify the resultMap amongst the others.
        /// </summary>
        /// <example>GetProduct</example>
        string Id { get; }

        /// <summary>
        /// The GroupBy Properties name.
        /// </summary>
        List<string> GroupByPropertyNames { get; }
        
        /// <summary>
        /// The output type class of the resultMap.
        /// </summary>
        Type Class { get; }

        /// <summary>
        /// Sets the IDataExchange
        /// </summary>
        IDataExchange DataExchange { set; }

        /// <summary>
        /// Gets or sets a value indicating whether this instance is initalized.
        /// </summary>
        /// <value>
        /// 	<c>true</c> if this instance is initalized; otherwise, <c>false</c>.
        /// </value>
        bool IsInitalized { get; set; }


        /// <summary>
        /// Create an instance Of result.
        /// </summary>
        /// <param name="parameters">
        /// An array of values that matches the number, order and type 
        /// of the parameters for this constructor. 
        /// </param>
        /// <returns>An object.</returns>
        object CreateInstanceOfResult(object[] parameters);

        /// <summary>
        /// Set the value of an object property.
        /// </summary>
        /// <param name="target">The object to set the property.</param>
        /// <param name="property">The result property to use.</param>
        /// <param name="dataBaseValue">The database value to set.</param>
        void SetValueOfProperty(ref object target, ResultProperty property, object dataBaseValue);

        /// <summary>
        /// 
        /// </summary>
        /// <param name="dataReader"></param>
        /// <returns></returns>
        IResultMap ResolveSubMap(IDataReader dataReader);
    }
}