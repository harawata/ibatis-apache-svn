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
using System.Reflection.Emit;

namespace IBatisNet.Common.Utilities.Objects.Members
{
	/// <summary>
	/// 
	/// </summary>
    public abstract class BaseEmitAccessor : BaseAccessor
	{
		/// <summary>
		/// The IL emitted IMemberAccessor
		/// </summary>
		protected IMemberAccessor emittedMemberAccessor = null;
		/// <summary>
		/// The AssemblyBuilder use to keep the type
		/// </summary>
		protected AssemblyBuilder assemblyBuilder = null;
		/// <summary>
		/// The ModuleBuilder use to create the type
		/// </summary>
		protected ModuleBuilder moduleBuilder = null;

		/// <summary>
		/// This method create a new type oject for the the field accessor class 
		/// that will provide dynamic access.
		/// </summary>
		protected void EmitIL()
		{
			// Create a new type oject for the the field accessor class.
			EmitType();

			// Create a new instance
			emittedMemberAccessor = assemblyBuilder.CreateInstance("MemberAccessorFor" + targetType.FullName + memberName) as IMemberAccessor;

			this.nullInternal = this.GetNullInternal(baseMemberType);

			if (emittedMemberAccessor == null)
			{
                throw new NotSupportedException(
					string.Format("Unable to create propert/field accessor for \"{0}\".", baseMemberType));
			}
		}

		/// <summary>
		/// Create an type that will provide the get and set methods.
		/// </summary>
		protected abstract void EmitType();

	}
}
