#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: 378715 $
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

using System;
using System.Text;

namespace IBatisNet.Common.Utilities.Objects
{
    /// <summary>
    /// The ILPropertyAccessor class provides an reflection access   
    /// to a property of a specified target class.
    /// </summary>
    public class ReflectionPropertyAccessor : IPropertyAccessor
    {

		/// <summary>
		/// Creates a new Reflection property accessor.
		/// </summary>
		/// <param name="targetType">Target object type.</param>
		/// <param name="propertyName">Property name.</param>
		public ReflectionPropertyAccessor(Type targetType, string propertyName)
		{
		}

        /// <summary>
        /// Gets the value stored in the property for 
        /// the specified target.
        /// </summary>
        /// <param name="target">Object to retrieve the property from.</param>
        /// <returns>Property value.</returns>
        public object Get(object target)
        {
            return null;
        }

        /// <summary>
        /// Sets the value for the property of
        /// the specified target.
        /// </summary>
        /// <param name="target">Object to set the property on.</param>
        /// <param name="value">Property value.</param>
        public void Set(object target, object value)
        {
        }
    }
}
