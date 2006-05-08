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

        #region ISetGet Members

        /// <summary>
		/// Gets the property value from the specified target.
		/// </summary>
		/// <param name="target">Target object.</param>
		/// <returns>Property value.</returns>
		public override object Get(object target)
		{
			if (_canRead)
			{
				return emittedSetGet.Get(target);
			}
			else
			{
				throw new NotSupportedException(
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

				emittedSetGet.Set(target, newValue);
			}
			else
			{
				throw new NotSupportedException(
					string.Format("Property \"{0}\" on type "
					+ "{1} doesn't have a set method.", memberName, targetType));
			}
		}


		#endregion

		/// <summary>
		/// Initializes the property and generates the implementation for getter and setter methods.
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
				throw new NotSupportedException(
					string.Format("Property \"{0}\" does not exist for type "
					+ "{1}.", propertyName, targetType));
			}
			else
			{
				this.baseMemberType = propertyInfo.PropertyType;
				_canRead = propertyInfo.CanRead;
				_canWrite = propertyInfo.CanWrite;
				this.EmitIL();
			}
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
            TypeBuilder typeBuilder = moduleBuilder.DefineType("SetGetFor" + targetType.FullName + memberName, 
				TypeAttributes.Class | TypeAttributes.Public | TypeAttributes.Sealed);

			// Mark the class as implementing IMemberAccessor. 
            typeBuilder.AddInterfaceImplementation(typeof(ISetGet));

            // Add a constructor
            ConstructorBuilder constructor = typeBuilder.DefineDefaultConstructor(MethodAttributes.Public);

			#region Emit Get
			// Define a method named "Get" for the get operation (IMemberAccessor). 
			Type[] getParamTypes = new Type[] { typeof(object) };
			MethodBuilder methodBuilder = typeBuilder.DefineMethod("Get",
				MethodAttributes.Public | MethodAttributes.Virtual, typeof(object), getParamTypes);
            // Get an ILGenerator and used it to emit the IL that we want.
            ILGenerator generatorIL = methodBuilder.GetILGenerator();
			if (_canRead)
            {
                // Emit the IL for get access. 
                MethodInfo targetGetMethod = targetType.GetMethod("get_" + memberName);

                generatorIL.DeclareLocal(typeof(object));
                generatorIL.Emit(OpCodes.Ldarg_1);	//Load the first argument,(target object)
                generatorIL.Emit(OpCodes.Castclass, targetType);	//Cast to the source type
                generatorIL.EmitCall(OpCodes.Call, targetGetMethod, null); //Get the property value
                if (targetGetMethod.ReturnType.IsValueType)
                {
                    generatorIL.Emit(OpCodes.Box, targetGetMethod.ReturnType); //Box if necessary
                }
                generatorIL.Emit(OpCodes.Stloc_0); //Store it
                generatorIL.Emit(OpCodes.Ldloc_0);
                generatorIL.Emit(OpCodes.Ret);
            }
			else
			{
				 generatorIL.ThrowException(typeof(MissingMethodException));
			}
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
			generatorIL = methodBuilder.GetILGenerator();
			if (_canWrite)
            {
                // Emit the IL for the set access. 
                MethodInfo targetSetMethod = targetType.GetMethod("set_" + memberName);
                Type paramType = targetSetMethod.GetParameters()[0].ParameterType;

                generatorIL.DeclareLocal(paramType);
                generatorIL.Emit(OpCodes.Ldarg_1); //Load the first argument (target object)
                generatorIL.Emit(OpCodes.Castclass, targetType); //Cast to the source type
                generatorIL.Emit(OpCodes.Ldarg_2); //Load the second argument (value object)
                if (paramType.IsValueType)
                {
                    generatorIL.Emit(OpCodes.Unbox, paramType); //Unbox it 	
                    if (typeToOpcode[paramType] != null)
                    {
                        OpCode load = (OpCode)typeToOpcode[paramType];
                        generatorIL.Emit(load); //and load
                    }
                    else
                    {
                        generatorIL.Emit(OpCodes.Ldobj, paramType);
                    }
                }
                else
                {
                    generatorIL.Emit(OpCodes.Castclass, paramType); //Cast class
                }
                generatorIL.EmitCall(OpCodes.Callvirt, targetSetMethod, null); //Set the property value
                generatorIL.Emit(OpCodes.Ret);
            }
			else
			{
				generatorIL.ThrowException(typeof(MissingMethodException));
			}
			#endregion

			// Load the type
			typeBuilder.CreateType();
		}

	}
}