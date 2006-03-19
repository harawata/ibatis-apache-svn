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
	/// The ReflectionFieldAccessor class provides an reflection access   
	/// to a field of a specified target class.
	/// </summary>
	public class ReflectionFieldAccessor : IMemberAccessor
	{
		private FieldInfo _fieldInfo = null;

		/// <summary>
		/// Creates a new Reflection field accessor.
		/// </summary>
		/// <param name="targetType">Target object type.</param>
		/// <param name="fieldName">Field name.</param>
		public ReflectionFieldAccessor(Type targetType, string fieldName)
		{
			_fieldInfo = targetType.GetField(fieldName, BindingFlags.NonPublic | BindingFlags.Instance | BindingFlags.Public);
		}

		#region IMemberAccessor Members

		/// <summary>
		/// Gets the value stored in the field for 
		/// the specified target.
		/// </summary>
		/// <param name="target">Object to retrieve the property from.</param>
		/// <returns>Property value.</returns>
		public object Get(object target)
		{
			return _fieldInfo.GetValue(target);
		}

		/// <summary>
		/// Sets the value for the field of
		/// the specified target.
		/// </summary>
		/// <param name="target">Object to set the property on.</param>
		/// <param name="value">Property value.</param>
		public void Set(object target, object value)
		{
			_fieldInfo.SetValue(target, value);
		}

		#endregion
	}
}
