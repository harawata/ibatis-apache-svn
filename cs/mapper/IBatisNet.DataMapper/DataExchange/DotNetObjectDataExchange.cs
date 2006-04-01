#region Apache Notice
/*****************************************************************************
 * $Revision: 374175 $
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

using System;
using IBatisNet.Common.Utilities.Objects;
using IBatisNet.DataMapper.Configuration.ParameterMapping;
using IBatisNet.DataMapper.Configuration.ResultMapping;

namespace IBatisNet.DataMapper.DataExchange
{
	/// <summary>
	/// IDataExchange implementation for .NET object
	/// </summary>
	public sealed class DotNetObjectDataExchange : BaseDataExchange
	{

		private Type _clazz =null;

		/// <summary>
		/// Cosntructor
		/// </summary>
		/// <param name="dataExchangeFactory"></param>
		/// <param name="clazz"></param>
		public DotNetObjectDataExchange(Type clazz, DataExchangeFactory dataExchangeFactory):base(dataExchangeFactory)
		{
			_clazz = clazz;
		}

		#region IDataExchange Members

		/// <summary>
		/// Gets the data to be set into a IDataParameter.
		/// </summary>
		/// <param name="mapping"></param>
		/// <param name="parameterObject"></param>
		public override object GetData(ParameterProperty mapping, object parameterObject)
		{
			if ( mapping.IsComplexMemberName || _clazz!=parameterObject.GetType())
			{
				return ObjectProbe.GetPropertyValue(parameterObject, mapping.PropertyName);
			}
			else
			{
				return mapping.MemberAccessor.Get(parameterObject);
			}
		}

		/// <summary>
		/// Sets the value to the result property.
		/// </summary>
		/// <param name="mapping"></param>
		/// <param name="target"></param>
		/// <param name="dataBaseValue"></param>
		public override void SetData(ref object target, ResultProperty mapping, object dataBaseValue)
		{
			if ( target.GetType() != _clazz )
			{
				throw new ArgumentException( "Could not set value of type '"+ target.GetType() +"' in property '"+mapping.PropertyName+"' of type '"+_clazz+"'" );
			}
			if ( mapping.IsComplexMemberName)
			{
				ObjectProbe.SetPropertyValue(target, mapping.PropertyName, dataBaseValue);
			}
			else
			{
				mapping.MemberAccessor.Set(target, dataBaseValue);
			}
		}

		/// <summary>
		/// Sets the value to the parameter property.
		/// </summary>
		/// <remarks>Use to set value on output parameter</remarks>
		/// <param name="mapping"></param>
		/// <param name="target"></param>
		/// <param name="dataBaseValue"></param>
		public override void SetData(ref object target, ParameterProperty mapping, object dataBaseValue)
		{
			if (mapping.IsComplexMemberName)
			{	
				ObjectProbe.SetPropertyValue(target, mapping.PropertyName, dataBaseValue);
			}
			else
			{
				mapping.MemberAccessor.Set(target, dataBaseValue);
			}
		}

		#endregion
	}
}
