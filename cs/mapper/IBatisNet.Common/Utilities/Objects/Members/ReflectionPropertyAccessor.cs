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
    public class ReflectionPropertyAccessor : IMemberAccessor
    {
//		private static BindingFlags BINDING_FLAGS
//			= BindingFlags.Public 
//			| BindingFlags.Instance 
//			;

		private PropertyInfo _propertyInfo = null;
		private string _propertyName = string.Empty;
		private Type _targetType = null;

		/// <summary>
		/// Creates a new Reflection property accessor.
		/// </summary>
		/// <param name="targetType">Target object type.</param>
		/// <param name="propertyName">Property name.</param>
		public ReflectionPropertyAccessor(Type targetType, string propertyName)
		{
			_propertyInfo = ObjectProbe.GetPropertyInfoForSetter(targetType, propertyName);
				//targetType.GetProperty(propertyName, BINDING_FLAGS);
			_targetType = targetType;
			_propertyName = propertyName;
		}

		#region IMemberAccessor Members
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
				throw new MissingMethodException(
					string.Format("Property \"{0}\" on type "
					+ "{1} doesn't have a get method.", _propertyName, _targetType));
			}        
		}

        /// <summary>
        /// Sets the value for the property of
        /// the specified target.
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
				throw new MissingMethodException(
					string.Format("Property \"{0}\" on type "
					+ "{1} doesn't have a set method.", _propertyName, _targetType));
			}
        }
		#endregion
    }
}
