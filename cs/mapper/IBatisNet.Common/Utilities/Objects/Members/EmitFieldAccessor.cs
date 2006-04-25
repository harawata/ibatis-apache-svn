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
using System.Reflection.Emit;

namespace IBatisNet.Common.Utilities.Objects.Members
{
	/// <summary>
	/// The EmitFieldAccessor class provides an IL-based access   
	/// to a field of a specified target class.
	/// </summary>
	/// <remarks>Will Throw FieldAccessException on private field</remarks>
	public sealed class EmitFieldAccessor : BaseEmitAccessor
	{
		/// <summary>
		/// Creates a new IL field accessor.
		/// </summary>
		/// <param name="targetObjectType">Target object type.</param>
		/// <param name="fieldName">Field name.</param>
		/// <param name="assemBuilder"></param>
		/// <param name="modBuilder"></param>
		public EmitFieldAccessor(Type targetObjectType, string fieldName, AssemblyBuilder assemBuilder, ModuleBuilder modBuilder)
		{
			assemblyBuilder = assemBuilder;
			moduleBuilder = modBuilder;
			targetType = targetObjectType;
			memberName = fieldName;

			FieldInfo fieldInfo = targetType.GetField(fieldName, BindingFlags.NonPublic | BindingFlags.Instance | BindingFlags.Public);

			// Make sure the field exists
			if(fieldInfo == null)
			{
				throw new NotSupportedException(
					string.Format("Field \"{0}\" does not exist for type "
					+ "{1}.", fieldName, targetObjectType));
			}
			else
			{
				baseMemberType = fieldInfo.FieldType;
				this.EmitIL();
			}
		}

			
		private void ImplementProperty(TypeBuilder type, FieldBuilder field, PropertyInfo prop)
		{
			MethodBuilder getter = type.DefineMethod("get_" + prop.Name, MethodAttributes.Public | MethodAttributes.Virtual | MethodAttributes.HideBySig | MethodAttributes.SpecialName, prop.PropertyType, null);
			type.DefineMethodOverride(getter, prop.GetGetMethod());
			ILGenerator getterIL = getter.GetILGenerator();
			getterIL.Emit(OpCodes.Ldarg_0); 
			getterIL.Emit(OpCodes.Ldfld, field);
			getterIL.Emit(OpCodes.Ret);
		}

		/// <summary>
		/// Create an type that will provide the get and set methods.
		/// </summary>
		protected override void EmitType()
		{
			// Define a public class named "FieldAccessorFor.FullTagetTypeName.FieldName" in the assembly.
			TypeBuilder typeBuilder = moduleBuilder.DefineType("MemberAccessorFor" + targetType.FullName + memberName, TypeAttributes.Class | TypeAttributes.Public | TypeAttributes.Sealed);

			// Mark the class as implementing IMemberAccessor. 
			typeBuilder.AddInterfaceImplementation(typeof(IMemberAccessor));

			// Define 2 private fields
			FieldBuilder fieldBuilderMemberType = typeBuilder.DefineField("_memberType",
				typeof(Type),
				FieldAttributes.Private);

			FieldBuilder fieldBuilderName = typeBuilder.DefineField("_name",
				typeof(Type),
				FieldAttributes.Private);

			#region Emit constructor
			// Create a new constructor (public)
			ConstructorBuilder cb = typeBuilder.DefineConstructor(MethodAttributes.Public, 
				CallingConventions.Standard, Type.EmptyTypes);
			// Get the constructor's IL generator
			ILGenerator constructorIL = cb.GetILGenerator();

			// Load "this"
			constructorIL.Emit(OpCodes.Ldarg_0);
			// Call the base constructor (no args)
			constructorIL.Emit(OpCodes.Call, typeof(Object).GetConstructor(Type.EmptyTypes));
			//            // Load "this"
			//            constructorIL.Emit(OpCodes.Ldarg_0);
			//            // Load member type
			//            constructorIL.Emit(OpCodes.Call, baseMemberType.GetType());
			//            // Store in field "_memberType"
			//            constructorIL.Emit(OpCodes.Stfld, fieldBuilder);
			// Emit return opcode
			constructorIL.Emit(OpCodes.Ret);
			#endregion

			PropertyInfo prop =typeof(IMemberAccessor).GetProperty("MemberType");
			ImplementProperty(typeBuilder, fieldBuilderMemberType, prop);

			prop =typeof(IMemberAccessor).GetProperty("Name");
			ImplementProperty(typeBuilder, fieldBuilderName, prop);

			#region Emit Get
			// Define a method named "Get" for the get operation (IMemberAccessor). 
			Type[] getParamTypes = new Type[] { typeof(object) };
			MethodBuilder getMethod = typeBuilder.DefineMethod("Get",
				MethodAttributes.Public | MethodAttributes.Virtual,
				typeof(object),
				getParamTypes);

			// Get an ILGenerator and used it to emit the IL that we want.
			ILGenerator getIL = getMethod.GetILGenerator();

			FieldInfo targetField = targetType.GetField(memberName, BindingFlags.NonPublic | BindingFlags.Instance | BindingFlags.Public);

			// Emit the IL for get access. 
			if (targetField != null)
			{
				// We need a reference to the current instance (stored in local argument index 1) 
				// so Ldfld can load from the correct instance (this one).
				getIL.Emit(OpCodes.Ldarg_1);
				getIL.Emit(OpCodes.Ldfld, targetField);
				if (baseMemberType.IsValueType)
				{
					// Now, we execute the box opcode, which pops the value of field 'x',
					// returning a reference to the filed value boxed as an object.
					getIL.Emit(OpCodes.Box, targetField.FieldType);
				}
			}
			else
			{
				getIL.ThrowException(typeof(MissingMethodException));
			}
			getIL.Emit(OpCodes.Ret); 
			#endregion

			#region Emit Set
			// Define a method named "Set" for the set operation (IMemberAccessor).
			Type[] setParamTypes = new Type[] { typeof(object), typeof(object) };
			MethodBuilder setMethod = typeBuilder.DefineMethod("Set",
				MethodAttributes.Public | MethodAttributes.Virtual,
				null,
				setParamTypes);

			// Get an ILGenerator and used to emit the IL that we want.
			ILGenerator setIL = setMethod.GetILGenerator();
			// Emit the IL for the set access. 
			if (targetField != null)
			{
				setIL.Emit(OpCodes.Ldarg_1);//Load the first argument (target object)
				setIL.Emit(OpCodes.Castclass, targetType); //Cast to the source type
				setIL.Emit(OpCodes.Ldarg_2);//Load the second argument (value object)
				if (baseMemberType.IsValueType)
				{
					setIL.Emit(OpCodes.Unbox, baseMemberType); //Unbox it 	
					if (typeToOpcode[baseMemberType] != null)
					{
						OpCode load = (OpCode)typeToOpcode[baseMemberType];
						setIL.Emit(load); //and load
					}
					else
					{
						setIL.Emit(OpCodes.Ldobj, baseMemberType);
					}
				}
				else
				{
					setIL.Emit(OpCodes.Castclass, baseMemberType); //Cast class
				}
				setIL.Emit(OpCodes.Stfld, targetField); //Set the field value
			}
			else
			{
				setIL.ThrowException(typeof(MissingMethodException));
			}
			setIL.Emit(OpCodes.Ret); 
			#endregion

			// Load the type
			typeBuilder.CreateType();
		}


		#region IMemberAccessor Members

		/// <summary>
		/// Gets the field value from the specified target.
		/// </summary>
		/// <param name="target">Target object.</param>
		/// <returns>Property value.</returns>
		public override object Get(object target)
		{
			return emittedMemberAccessor.Get(target);
		}

		/// <summary>
		/// Sets the field for the specified target.
		/// </summary>
		/// <param name="target">Target object.</param>
		/// <param name="value">Value to set.</param>
		public override void Set(object target, object value)
		{
			object newValue = value;
			if (newValue == null)
			{
				// If the value to assign is null, assign null internal value
				newValue = nullInternal;
			}
			emittedMemberAccessor.Set(target, newValue);
		}

		#endregion
	}
}


