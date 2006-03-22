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
using System.Collections;
using System.Collections.Specialized;
using System.Reflection.Emit;

namespace IBatisNet.Common.Utilities.Objects.Members
{
	/// <summary>
	/// 
	/// </summary>
	public abstract class BaseEmitAccessor : IMemberAccessor
	{
		/// <summary>
		/// The class parent type
		/// </summary>
		protected Type targetType = null;
		/// <summary>
		/// The property/field name
		/// </summary>
		protected string memberName = string.Empty;
		/// <summary>
		/// The property/field type
		/// </summary>
		protected Type baseMemberType = null;
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
		/// The null internal value used by this member type 
		/// </summary>
		protected object nullInternal = null;

		/// <summary>
		/// 
		/// </summary>
		protected static IDictionary typeToOpcode = new HybridDictionary();

		/// <summary>
		/// Static constructor
		/// "Initialize a private IDictionary with type-opCode pairs 
		/// </summary>
		static BaseEmitAccessor()
		{
			typeToOpcode[typeof(sbyte)] = OpCodes.Ldind_I1;
			typeToOpcode[typeof(byte)] = OpCodes.Ldind_U1;
			typeToOpcode[typeof(char)] = OpCodes.Ldind_U2;
			typeToOpcode[typeof(short)] = OpCodes.Ldind_I2;
			typeToOpcode[typeof(ushort)] = OpCodes.Ldind_U2;
			typeToOpcode[typeof(int)] = OpCodes.Ldind_I4;
			typeToOpcode[typeof(uint)] = OpCodes.Ldind_U4;
			typeToOpcode[typeof(long)] = OpCodes.Ldind_I8;
			typeToOpcode[typeof(ulong)] = OpCodes.Ldind_I8;
			typeToOpcode[typeof(bool)] = OpCodes.Ldind_I1;
			typeToOpcode[typeof(double)] = OpCodes.Ldind_R8;
			typeToOpcode[typeof(float)] = OpCodes.Ldind_R4;
		}

		/// <summary>
		/// Get the null value for a given type
		/// </summary>
		/// <param name="type"></param>
		/// <returns></returns>
		protected object GetNullInternal(Type type)
		{
			if (type.IsValueType)
			{
				if (type.IsEnum)
				{
					return GetNullInternal(Enum.GetUnderlyingType(type));
				}

				if (type.IsPrimitive)
				{
					if (type == typeof(Int32)) { return 0; }
					if (type == typeof(Double)) { return (Double)0; }
					if (type == typeof(Int16)) { return (Int16)0; }
					if (type == typeof(SByte)) { return (SByte)0; }
					if (type == typeof(Int64)) { return (Int64)0; }
					if (type == typeof(Byte)) { return (Byte)0; }
					if (type == typeof(UInt16)) { return (UInt16)0; }
					if (type == typeof(UInt32)) { return (UInt32)0; }
					if (type == typeof(UInt64)) { return (UInt64)0; }
					if (type == typeof(UInt64)) { return (UInt64)0; }
					if (type == typeof(Single)) { return (Single)0; }
					if (type == typeof(Boolean)) { return false; }
					if (type == typeof(char)) { return '\0'; }
				}
				else
				{
					if (type == typeof(DateTime)) { return DateTime.MinValue; }
					if (type == typeof(Decimal)) { return 0m; }
					if (type == typeof(Guid)) { return Guid.Empty; }
					if (type == typeof(TimeSpan)) { return TimeSpan.MinValue; }
				}
			}

			return null;
		}

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

			nullInternal = GetNullInternal(baseMemberType);

			if (emittedMemberAccessor == null)
			{
				throw new MethodAccessException(
					string.Format("Unable to create propert/field accessor for \"{0}\".", baseMemberType));
			}
		}

		/// <summary>
		/// Create an type that will provide the get and set methods.
		/// </summary>
		protected abstract void EmitType();

		#region IMemberAccessor Members

		/// <summary>
		/// Gets the type of this member, such as field, property.
		/// </summary>
		public Type MemberType
		{
			get { return baseMemberType; }
		}

		/// <summary>
		/// Gets the member name.
		/// </summary>
		public string Name
		{
			get { return memberName; }
		}

		/// <summary>
		/// Gets the field value from the specified target.
		/// </summary>
		/// <param name="target">Target object.</param>
		/// <returns>Property value.</returns>
		public abstract object Get(object target);

		/// <summary>
		/// Sets the field for the specified target.
		/// </summary>
		/// <param name="target">Target object.</param>
		/// <param name="value">Value to set.</param>
		public abstract void Set(object target, object value);

		#endregion
	}
}
