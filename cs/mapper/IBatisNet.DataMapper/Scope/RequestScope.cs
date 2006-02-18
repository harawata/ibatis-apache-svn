
#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: $
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

#region Using

using System.Collections;
using System.Data;
using System.Runtime.CompilerServices;
using IBatisNet.DataMapper.Configuration.ParameterMapping;
using IBatisNet.DataMapper.Configuration.ResultMapping;
using IBatisNet.DataMapper.Configuration.Statements;

#endregion

namespace IBatisNet.DataMapper.Scope
{
	/// <summary>
	/// Hold data during the process of a mapped statement.
	/// </summary>
	public class RequestScope
	{
		#region Fields
		
		private ErrorContext _errorContext = null;
		private ParameterMap _parameterMap = null;
		private ResultMap _resultMap = null;
		private PreparedStatement _preparedStatement = null;
		private IDbCommand _command = null;
		private Queue _properties = new Queue();
		bool _rowDataFound= false;
		private static long _nextId = 0;
		private long _id = 0;
		#endregion
	
		#region Properties

		/// <summary>
		///  The IDbCommand to execute
		/// </summary>
		public IDbCommand IDbCommand
		{
			set { _command = value; }
			get { return _command; }
		}

		/// <summary>
		///  Indicate if the statement have find data
		/// </summary>
		public bool IsRowDataFound
		{
			set { _rowDataFound = value; }
			get { return _rowDataFound; }
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
		/// The 'select' result property to process after having process the main properties.
		/// </summary>
		public Queue QueueSelect
		{
			get { return _properties; }
			set { _properties = value; }
		}

		/// <summary>
		/// The ResultMap used by this request.
		/// </summary>
		public ResultMap ResultMap
		{
			get { return _resultMap; }
			set { _resultMap = value; }
		}

		/// <summary>
		/// The parameterMap used by this request.
		/// </summary>
		public ParameterMap ParameterMap
		{
			get { return _parameterMap; }
			set { _parameterMap = value; }
		}

		/// <summary>
		/// The PreparedStatement used by this request.
		/// </summary>
		public PreparedStatement PreparedStatement
		{
			get { return _preparedStatement; }
			set { _preparedStatement = value; }
		}
		#endregion

		#region Constructors

		/// <summary>
		/// Default constructor
		/// </summary>
		public RequestScope()
		{
			_errorContext = new ErrorContext();
			 _id = GetNextId();
		}
		#endregion 

		#region Method

		/// <summary>
		/// Check if the specify object is equal to the current object.
		/// </summary>
		/// <param name="obj"></param>
		/// <returns></returns>
		public override bool Equals(object obj)
		{
			if (this == obj) {return true;}
			if (!(obj is RequestScope)) {return false;}

			RequestScope scope = (RequestScope) obj;

			if (_id != scope._id) return false;

			return true;
		}

		/// <summary>
		/// Get the HashCode for this RequestScope
		/// </summary>
		/// <returns></returns>
		public override int GetHashCode() 
		{
			 return (int) (_id ^ (_id >> 32));
		}

		/// <summary>
		/// Check if the ResultMap is well set, process case of subMap resultMap.
		/// </summary>
		/// <param name="dataReader">The current IDataReader</param>
		/// <returns>the resultMap to use</returns>
		public ResultMap GetResultMap(IDataReader dataReader)
		{
			return _resultMap.ResolveSubMap(dataReader);
		}

		/// <summary>
		/// Method to get a unique ID
		/// </summary>
		/// <returns>The new ID</returns>
		[MethodImpl(MethodImplOptions.Synchronized)]
		public static long GetNextId() 
		{
			return _nextId++;
		}
		#endregion
	}
}
