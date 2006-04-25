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

namespace IBatisNet.Common.Utilities.Objects.Members
{
	/// <summary>
	/// The IMemberAccessor interface defines a field/property accessor and
	/// provides <c>Reflection.Emit</c>-generated <see cref="Get"/> and <see cref="Set"/> 
	/// implementations for drastically improved performance over default late-bind 
	/// invoke.
	/// </summary>
	public interface IMemberAccessor
	{
		/// <summary>
		/// Gets the member name.
		/// </summary>
		string Name { get; }

		/// <summary>
		/// Gets the type of this member (field or property).
		/// </summary>
		Type MemberType { get; }

		/// <summary>
		/// Gets the value stored in the field/property for the specified target.
		/// </summary>
		/// <param name="target">Object to retrieve the field/property from.</param>
		/// <returns>Value.</returns>
		object Get(object target);

		/// <summary>
		/// Sets the value for the field/property of the specified target.
		/// </summary>
		/// <param name="target">Object to set the field/property on.</param>
		/// <param name="value">Value.</param>
		void Set(object target, object value);
	}
}
