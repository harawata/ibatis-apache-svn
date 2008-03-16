
#region Apache Notice
/*****************************************************************************
 * $Revision: 450157 $
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
using System.Collections.Generic;
using System.Data;
using Apache.Ibatis.Common.Contracts;
using Apache.Ibatis.Common.Utilities;
using Apache.Ibatis.Common.Utilities.Objects;
using Apache.Ibatis.DataMapper.DataExchange;
using Apache.Ibatis.DataMapper.TypeHandlers;

#endregion

namespace Apache.Ibatis.DataMapper.Model.ResultMapping
{
    /// <summary>
    /// Implementation of <see cref="IResultMap"/> interface for auto mapping
    /// </summary>
    public class AutoResultMap : IResultMap 
    {
        [NonSerialized]
        private bool isInitalized = false;
        [NonSerialized]
        private readonly Type resultClass = null;
        [NonSerialized]
        private readonly IFactory resultClassFactory = null;
        [NonSerialized]
        private readonly ResultPropertyCollection properties = new ResultPropertyCollection();
        [NonSerialized]
        private IDataExchange dataExchange = null;
        [NonSerialized]
        private readonly bool isSimpleType = false;

        /// <summary>
        /// Initializes a new instance of the <see cref="AutoResultMap"/> class.
        /// </summary>
        /// <param name="resultClass">The result class.</param>
        /// <param name="resultClassFactory">The result class factory.</param>
        /// <param name="dataExchange">The data exchange.</param>
        public AutoResultMap(Type resultClass, IFactory resultClassFactory, IDataExchange dataExchange, bool isSimpleType)
        {
            Contract.Require.That(resultClass, Is.Not.Null).When("retrieving argument resultClass in AutoResultMap constructor");
            Contract.Require.That(dataExchange, Is.Not.Null).When("retrieving argument dataExchange in AutoResultMap constructor");
            Contract.Require.That(dataExchange, Is.Not.Null).When("retrieving argument typeHandlerFactory in AutoResultMap constructor");

            this.resultClass = resultClass;
            this.resultClassFactory = resultClassFactory;
            this.dataExchange = dataExchange;
            this.isSimpleType = isSimpleType;

        }
        
        #region IResultMap Members

        /// <summary>
        /// The GroupBy Properties.
        /// </summary>
        public List<string> GroupByPropertyNames
        {
            get { throw new NotImplementedException("The property 'GroupByPropertyNames' is not implemented."); }
        }
        
        /// <summary>
        /// The collection of ResultProperty.
        /// </summary>
        public ResultPropertyCollection Properties
        {
            get { return properties; }
        }

        /// <summary>
        /// The GroupBy Properties.
        /// </summary>
        /// <value></value>
        public ResultPropertyCollection GroupByProperties
        {
            get { throw new NotImplementedException("The property 'GroupByProperties' is not implemented."); }
        }

        /// <summary>
        /// The collection of constructor parameters.
        /// </summary>
        public ResultPropertyCollection Parameters
        {
            get { throw new NotImplementedException("The property 'Parameters' is not implemented."); }
        }

        /// <summary>
        /// Gets or sets a value indicating whether this instance is initalized.
        /// </summary>
        /// <value>
        /// 	<c>true</c> if this instance is initalized; otherwise, <c>false</c>.
        /// </value>
        public bool IsInitalized
        {
            get { return isInitalized; }
            set { isInitalized = value; }
        }

        /// <summary>
        /// Identifier used to identify the resultMap amongst the others.
        /// </summary>
        /// <value></value>
        /// <example>GetProduct</example>
        public string Id
        {
            get { return resultClass.Name; }
        }


        /// <summary>
        /// The output type class of the resultMap.
        /// </summary>
        /// <value></value>
        public Type Class
        {
            get { return resultClass; }
        }


        /// <summary>
        /// Sets the IDataExchange
        /// </summary>
        /// <value></value>
        public IDataExchange DataExchange
        {
            set { dataExchange = value; }
        }


        /// <summary>
        /// Create an instance Of result.
        /// </summary>
        /// <param name="parameters">An array of values that matches the number, order and type
        /// of the parameters for this constructor.</param>
        /// <returns>An object.</returns>
        public object CreateInstanceOfResult(object[] parameters)
        {
            return CreateInstanceOfResultClass();
        }

        /// <summary>
        /// Set the value of an object property.
        /// </summary>
        /// <param name="target">The object to set the property.</param>
        /// <param name="property">The result property to use.</param>
        /// <param name="dataBaseValue">The database value to set.</param>
        public void SetValueOfProperty(ref object target, ResultProperty property, object dataBaseValue)
        {
            dataExchange.SetData(ref target, property, dataBaseValue);
        }

        /// <summary>
        /// </summary>
        /// <param name="dataReader"></param>
        /// <returns></returns>
        public IResultMap ResolveSubMap(IDataReader dataReader)
        {
           return this;
        }

        #endregion

        /// <summary>
        /// Clones this instance.
        /// </summary>
        /// <returns></returns>
        public AutoResultMap Clone()
        {
            return new AutoResultMap(resultClass, resultClassFactory, dataExchange, isSimpleType);
        }
        
        /// <summary>
        /// Create an instance of result class.
        /// </summary>
        /// <returns>An object.</returns>
        public object CreateInstanceOfResultClass()
        {
            if (!isSimpleType)
            {
                return resultClassFactory.CreateInstance(null);
            }
            else
            {
                return TypeUtils.InstantiatePrimitiveType(resultClass);
            }
        }

    }
}
