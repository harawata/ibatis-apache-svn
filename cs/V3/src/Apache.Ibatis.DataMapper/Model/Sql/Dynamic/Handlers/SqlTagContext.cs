
#region Apache Notice
/*****************************************************************************
 * $Revision: 408164 $
 * $LastChangedDate$
 * $LastChangedBy$
 * 
 * iBATIS.NET Data Mapper
 * Copyright (C) 2008/2005 - The Apache Software Foundation
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

using System.Collections;
using System.Text;
using Apache.Ibatis.DataMapper.Model.ParameterMapping;
using Apache.Ibatis.DataMapper.Model.Sql.Dynamic.Elements;
using System.Collections.Generic;

#endregion


namespace Apache.Ibatis.DataMapper.Model.Sql.Dynamic.Handlers
{
	/// <summary>
	/// Summary description for SqlTagContext.
	/// </summary>
	public sealed class SqlTagContext
	{
        private Hashtable attributes = new Hashtable();
		private bool _overridePrepend = false;
		private SqlTag _firstNonDynamicTagWithPrepend = null;
		private ArrayList _parameterMappings = new ArrayList();
		private StringBuilder buffer = new StringBuilder();

        /// <summary>
        /// Initializes a new instance of the <see cref="SqlTagContext"/> class.
        /// </summary>
		public SqlTagContext() 
		{
			_overridePrepend = false;
		}

        /// <summary>
        /// Gets the writer.
        /// </summary>
        /// <returns></returns>
		public StringBuilder GetWriter() 
		{
			return buffer;
		}

        /// <summary>
        /// Gets the body text.
        /// </summary>
        /// <value>The body text.</value>
		public string BodyText 
		{
			get { return buffer.ToString().Trim(); }
		}

        /// <summary>
        /// Gets or sets a value indicating whether this instance is override prepend.
        /// </summary>
        /// <value>
        /// 	<c>true</c> if this instance is override prepend; otherwise, <c>false</c>.
        /// </value>
		public bool IsOverridePrepend
		{
			set { _overridePrepend = value; }
			get { return _overridePrepend; }
		}


        /// <summary>
        /// Gets or sets the first non dynamic tag with prepend.
        /// </summary>
        /// <value>The first non dynamic tag with prepend.</value>
		public SqlTag FirstNonDynamicTagWithPrepend
		{
			get { return _firstNonDynamicTagWithPrepend; }
			set { _firstNonDynamicTagWithPrepend = value; }
		}


        /// <summary>
        /// Adds the attribute.
        /// </summary>
        /// <param name="key">The key.</param>
        /// <param name="value">The value.</param>
		public void AddAttribute(object key, object value) 
		{
			attributes.Add(key, value);
		}


        /// <summary>
        /// Gets the attribute.
        /// </summary>
        /// <param name="key">The key.</param>
        /// <returns></returns>
		public object GetAttribute(object key) 
		{
			return attributes[key];
		}


        /// <summary>
        /// Adds the parameter mapping.
        /// </summary>
        /// <param name="mapping">The mapping.</param>
		public void AddParameterMapping(ParameterProperty mapping) 
		{
			_parameterMappings.Add(mapping);
		}


        /// <summary>
        /// Gets the parameter mappings.
        /// </summary>
        /// <returns></returns>
		public IList GetParameterMappings() 
		{
			return _parameterMappings;
		}
	}
}
