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
	/// The EmitPropertyAccessor class provides an IL-based access   
	/// to a property of a specified target class.
	/// </summary>
	public sealed class EmitPropertyAccessor : BaseEmitAccessor, IPropertyAccessor
	{
		private bool _canRead = false;
		private bool _canWrite = false;

		#region IPropertyAccessor
		/// <summary>
		/// Gets a value indicating whether the property can be read. 
		/// </summary>
		public bool CanRead
		{
			get { return _canRead; }
		}

		/// <summary>
		/// Gets a value indicating whether the property can be written to. 
		/// </summary>
		public bool CanWrite
		{
			get { return _canWrite; }
		}
		#endregion

		/// <summary>
		/// Creates a new IL property accessor.
		/// </summary>
		/// <param name="targetObjectType">Target object type.</param>
		/// <param name="propertyName">Property name.</param>
		/// <param name="assBuilder"></param>
		/// <param name="modBuilder"></param>
		public EmitPropertyAccessor(Type targetObjectType, string propertyName, AssemblyBuilder assBuilder, ModuleBuilder modBuilder)
		{
			assemblyBuilder = assBuilder;
			moduleBuilder = modBuilder;
			targetType = targetObjectType;
			memberName = propertyName;

			PropertyInfo propertyInfo = targetType.GetProperty(propertyName);

			// Make sure the property exists
			if(propertyInfo == null)
			{
				throw new MissingMethodException(
					string.Format("Property \"{0}\" does not exist for type "
					+ "{1}.", propertyName, targetType));
			}
			else
			{
				baseMemberType = propertyInfo.PropertyType;
				_canRead = propertyInfo.CanRead;
				_canWrite = propertyInfo.CanWrite;
				this.EmitIL();
			}
		}

		
		#region IMemberAccessor Members

		/// <summary>
		/// Gets the property value from the specified target.
		/// </summary>
		/// <param name="target">Target object.</param>
		/// <returns>Property value.</returns>
		public override object Get(object target)
		{
			if (_canRead)
			{
				return emittedMemberAccessor.Get(target);
			}
			else
			{
				throw new MissingMethodException(
					string.Format("Property \"{0}\" on type "
					+ "{1} doesn't have a get method.", memberName, targetType));
			}
		}


		/// <summary>
		/// Sets the property for the specified target.
		/// </summary>
		/// <param name="target">Target object.</param>
		/// <param name="value">Value to set.</param>
		public override void Set(object target, object value)
		{
			if (_canWrite)
			{
				object newValue = value;
				if (newValue == null)
				{
					// If the value to assign is null, assign null internal value
					newValue = nullInternal;
				}

				emittedMemberAccessor.Set(target, newValue);
			}
			else
			{
				throw new MissingMethodException(
					string.Format("Property \"{0}\" on type "
					+ "{1} doesn't have a set method.", memberName, targetType));
			}
		}


		#endregion
		
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
		/// Create an type that will provide the get and set access method.
		/// </summary>
		/// <remarks>
		///  new ReflectionPermission(PermissionState.Unrestricted).Assert();
		///  CodeAccessPermission.RevertAssert();
		/// </remarks>
		protected override void EmitType()
		{
			// Define a public class named "PropertyAccessorFor.FullTagetTypeName.PropertyName" in the assembly.
			TypeBuilder typeBuilder = moduleBuilder.DefineType("MemberAccessorFor" + targetType.FullName + memberName, 
				TypeAttributes.Class | TypeAttributes.Public | TypeAttributes.Sealed);

			// Mark the class as implementing IMemberAccessor. 
			typeBuilder.AddInterfaceImplementation(typeof(IMemberAccessor));

			// Define 2 private fields
			FieldBuilder fieldBuilderMemberType = typeBuilder.DefineField("_memberType",
				typeof(Type),
				FieldAttributes.Private);

			FieldBuilder fieldBuilderName = typeBuilder.DefineField("_name",
				typeof(string),
				FieldAttributes.Private);

			#region Emit constructor
			// Create a new constructor (public)
			ConstructorBuilder cb = typeBuilder.DefineConstructor(MethodAttributes.Public 
				| MethodAttributes.HideBySig | MethodAttributes.SpecialName | MethodAttributes.RTSpecialName, 
				CallingConventions.Standard, Type.EmptyTypes);
			// Get the constructor's IL generator
			ILGenerator constructorIL = cb.GetILGenerator();

			// Load "this"
			constructorIL.Emit(OpCodes.Ldarg_0);
			// Call the base constructor (no args)
			constructorIL.Emit(OpCodes.Call, typeof(object).GetConstructor(Type.EmptyTypes));
			// this._name = memberName
			// Load "this"
			constructorIL.Emit(OpCodes.Ldarg_0);
			// Store name in field "_name"
			constructorIL.Emit(OpCodes.Ldstr, memberName);
			constructorIL.Emit(OpCodes.Stfld, fieldBuilderName);
			// this._memberType = baseMemberType
			// Store type in field "_memberType"
//			constructorIL.Emit(OpCodes.Ldtoken, baseMemberType);
//			MethodInfo miGetTypeFromHandle = typeof(System.Type).GetMethod("GetTypeFromHandle", new Type[] {typeof(System.RuntimeTypeHandle)});
//			constructorIL.EmitCall(OpCodes.Call, miGetTypeFromHandle, null); 
//			constructorIL.Emit(OpCodes.Stfld, fieldBuilderMemberType);
			// Emit return opcode
			constructorIL.Emit(OpCodes.Ret);
			#endregion

			PropertyInfo prop =typeof(IMemberAccessor).GetProperty("MemberType");
			ImplementProperty(typeBuilder, fieldBuilderMemberType, prop);

			prop =typeof(IMemberAccessor).GetProperty("Name");
			ImplementProperty(typeBuilder, fieldBuilderName, prop);

			//            #region Emit MemberType
			//            // Define the property MemberType (IMemberAccessor). 
			//            PropertyBuilder propertyBuilder = typeBuilder.DefineProperty("MemberType",
			//                             PropertyAttributes.None,
			//                             typeof(Type),
			//                             new Type[] { typeof(Type) });
			//
			//			// Define the get method for the property for MemberType
			//            MethodBuilder getMethodBuilder = typeBuilder.DefineMethod("get_MemberType",
			//                                    MethodAttributes.Public | MethodAttributes.Virtual ,
			//                                    typeof(Type),
			//                                    Type.EmptyTypes);
			//			typeBuilder.DefineMethodOverride(getMethodBuilder, typeof(IMemberAccessor).GetProperty("MemberType").GetGetMethod());
			//
			//            // Get an ILGenerator and used it to emit the IL that we want.
			//            ILGenerator getMethod  = getMethodBuilder.GetILGenerator();
			//
			//            // Emit the IL for get access. 
			//            getMethod.Emit(OpCodes.Ldarg_0);
			//            getMethod.Emit(OpCodes.Ldfld, fieldBuilder);
			//            getMethod.Emit(OpCodes.Ret);
			//
			//            // Last, we must map the method created above to our PropertyBuilder to 
			//            // the corresponding behavior "get". 
			//            propertyBuilder.SetGetMethod(getMethodBuilder);
			//
			//
			//            #endregion	

			#region Emit Get
			// Define a method named "Get" for the get operation (IMemberAccessor). 
			Type[] getParamTypes = new Type[] { typeof(object) };
			MethodBuilder methodBuilder = typeBuilder.DefineMethod("Get",
				MethodAttributes.Public | MethodAttributes.Virtual,
				typeof(object),
				getParamTypes);

			// Get an ILGenerator and used it to emit the IL that we want.
			ILGenerator getIL = methodBuilder.GetILGenerator();

			// Emit the IL for get access. 
			MethodInfo targetGetMethod = targetType.GetMethod("get_" + memberName);

			if (targetGetMethod != null)
			{
				getIL.DeclareLocal(typeof(object));
				getIL.Emit(OpCodes.Ldarg_1);	//Load the first argument,(target object)
				getIL.Emit(OpCodes.Castclass, targetType);	//Cast to the source type
				getIL.EmitCall(OpCodes.Call, targetGetMethod, null); //Get the property value
				if (targetGetMethod.ReturnType.IsValueType)
				{
					getIL.Emit(OpCodes.Box, targetGetMethod.ReturnType); //Box if necessary
				}
				getIL.Emit(OpCodes.Stloc_0); //Store it
				getIL.Emit(OpCodes.Ldloc_0);
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
			methodBuilder = typeBuilder.DefineMethod("Set",
				MethodAttributes.Public | MethodAttributes.Virtual,
				null,
				setParamTypes);

			// Get an ILGenerator and  used to emit the IL that we want.
			// Set(object, value);
			ILGenerator setIL = methodBuilder.GetILGenerator();
			// Emit the IL for the set access. 
			MethodInfo targetSetMethod = targetType.GetMethod("set_" + memberName);
			if (targetSetMethod != null)
			{
				Type paramType = targetSetMethod.GetParameters()[0].ParameterType;
				setIL.DeclareLocal(paramType);
				setIL.Emit(OpCodes.Ldarg_1); //Load the first argument (target object)
				setIL.Emit(OpCodes.Castclass, targetType); //Cast to the source type
				setIL.Emit(OpCodes.Ldarg_2); //Load the second argument (value object)
				if (paramType.IsValueType)
				{
					setIL.Emit(OpCodes.Unbox, paramType); //Unbox it 	
					if (typeToOpcode[paramType] != null)
					{
						OpCode load = (OpCode)typeToOpcode[paramType];
						setIL.Emit(load); //and load
					}
					else
					{
						setIL.Emit(OpCodes.Ldobj, paramType);
					}
				}
				else
				{
					setIL.Emit(OpCodes.Castclass, paramType); //Cast class
				}
				setIL.EmitCall(OpCodes.Callvirt, targetSetMethod, null); //Set the property value
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

	}
}