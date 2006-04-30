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
using System.Reflection;

namespace IBatisNet.Common.Utilities.Objects.Members
{
	/// <summary>
	/// The ReflectionPropertyAccessor class provides an reflection access   
	/// to a property of a specified target class.
	/// </summary>
	public class ReflectionPropertyAccessor : IMemberAccessor, IPropertyAccessor
	{
		private PropertyInfo _propertyInfo = null;
		private string _propertyName = string.Empty;
		private Type _targetType = null;

		/// <summary>
		/// Creates a new Reflection property accessor.
		/// Generates the implementation for getter and setter methods.
		/// </summary>
		/// <param name="targetType">Target object type.</param>
		/// <param name="propertyName">Property name.</param>
		public ReflectionPropertyAccessor(Type targetType, string propertyName)
		{
			ReflectionInfo reflectionCache = ReflectionInfo.GetInstance( targetType );
			_propertyInfo = (PropertyInfo)reflectionCache.GetGetter(propertyName);

			_targetType = targetType;
			_propertyName = propertyName;
		}

		#region IPropertyAccessor
		/// <summary>
		/// Gets a value indicating whether the property can be read. 
		/// </summary>
		public bool CanRead
		{
			get { return _propertyInfo.CanRead; }
		}

		/// <summary>
		/// Gets a value indicating whether the property can be written to. 
		/// </summary>
		public bool CanWrite
		{
			get { return _propertyInfo.CanWrite; }
		}
		#endregion

		#region IMemberAccessor Members

		/// <summary>
		/// Gets the member name.
		/// </summary>
		public string Name
		{
			get { return _propertyInfo.Name; }
		}

		/// <summary>
		/// Gets the type of this member, such as field, property.
		/// </summary>
		public Type MemberType
		{
			get { return _propertyInfo.PropertyType; }
		}

		/// <summary>
		/// Gets the value stored in the property for 
		/// the specified target.
		/// </summary>
		/// <param name="target">Object to retrieve the property from.</param>
		/// <returns>Property value.</returns>
		public object Get(object target)
		{
			if (_propertyInfo.CanRead)
			{
				return _propertyInfo.GetValue(target, null);
			}
			else
			{
				throw new NotSupportedException(
					string.Format("Property \"{0}\" on type "
					+ "{1} doesn't have a get method.", _propertyName, _targetType));
			}        
		}

		/// <summary>
		/// Sets the value for the property of the specified target.
		/// </summary>
		/// <param name="target">Object to set the property on.</param>
		/// <param name="value">Property value.</param>
		public void Set(object target, object value)
		{
			if (_propertyInfo.CanWrite)
			{
				_propertyInfo.SetValue(target, value, null);
			}
			else
			{
				throw new NotSupportedException(
					string.Format("Property \"{0}\" on type "
					+ "{1} doesn't have a set method.", _propertyName, _targetType));
			}
		}
		#endregion
	}
}
