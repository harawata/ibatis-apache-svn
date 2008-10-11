#region Apache Notice
/*****************************************************************************
 * $Revision: 374175 $
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

using System;
using Apache.Ibatis.Common.Utilities.Objects;
using Apache.Ibatis.Common.Utilities.Objects.Members;
using Apache.Ibatis.DataMapper.Model.ParameterMapping;
using Apache.Ibatis.DataMapper.Model.ResultMapping;

namespace Apache.Ibatis.DataMapper.DataExchange
{
	/// <summary>
	/// IDataExchange implementation for .NET object
	/// </summary>
	public sealed class DotNetObjectDataExchange : BaseDataExchange
	{

        private readonly Type _parameterClass = null;


        /// <summary>
        /// Initializes a new instance of the <see cref="DotNetObjectDataExchange"/> class.
        /// </summary>
        /// <param name="parameterClass">The parameter class.</param>
        /// <param name="dataExchangeFactory">The data exchange factory.</param>
        public DotNetObjectDataExchange(Type parameterClass, DataExchangeFactory dataExchangeFactory)
            : base(dataExchangeFactory)
		{
            _parameterClass = parameterClass;
		}

		#region IDataExchange Members

		/// <summary>
		/// Gets the data to be set into a IDataParameter.
		/// </summary>
		/// <param name="mapping"></param>
		/// <param name="parameterObject"></param>
		public override object GetData(ParameterProperty mapping, object parameterObject)
		{
		    if (mapping.IsComplexMemberName || _parameterClass!=parameterObject.GetType())
			{
				return ObjectProbe.GetMemberValue(parameterObject, mapping.PropertyName,
					DataExchangeFactory.AccessorFactory);
			}
		    return mapping.GetAccessor.Get(parameterObject);
		}

	    /// <summary>
		/// Sets the value to the result property.
		/// </summary>
		/// <param name="mapping"></param>
		/// <param name="target"></param>
		/// <param name="dataBaseValue"></param>
		public override void SetData(ref object target, ResultProperty mapping, object dataBaseValue)
		{
		    Type targetType = target.GetType();
            if ((targetType != _parameterClass)
                && !targetType.IsSubclassOf(_parameterClass)) 
			{
                throw new ArgumentException("Could not set value of type '" + target.GetType() + "' in property '" + mapping.PropertyName + "' of type '" + _parameterClass + "'");
			}
			if ( mapping.IsComplexMemberName)
			{
				ObjectProbe.SetMemberValue(target, mapping.PropertyName, dataBaseValue, 
					DataExchangeFactory.ObjectFactory,
					DataExchangeFactory.AccessorFactory);
			}
			else
			{
                mapping.Set(target, dataBaseValue);
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
				ObjectProbe.SetMemberValue(target, mapping.PropertyName, dataBaseValue, 
					DataExchangeFactory.ObjectFactory,
					DataExchangeFactory.AccessorFactory);
			}
			else
			{
                ISetAccessorFactory setAccessorFactory = DataExchangeFactory.AccessorFactory.SetAccessorFactory;
                ISetAccessor _setAccessor = setAccessorFactory.CreateSetAccessor(_parameterClass, mapping.PropertyName);

                _setAccessor.Set(target, dataBaseValue);
			}
		}

		#endregion
	}
}
