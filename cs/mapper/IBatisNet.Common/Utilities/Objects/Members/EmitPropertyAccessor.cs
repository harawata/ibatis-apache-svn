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
using System.Reflection;
using System.Reflection.Emit;

namespace IBatisNet.Common.Utilities.Objects.Members
{
    /// <summary>
    /// The EmitPropertyAccessor class provides an IL-based access   
    /// to a property of a specified target class.
    /// </summary>
    public class EmitPropertyAccessor : IMemberAccessor
	{
        private Type _targetType = null;
        private string _propertyName = string.Empty;
        private Type _propertyType = null;
        private IMemberAccessor _emittedPropertyAccessor = null;
		private AssemblyBuilder _assemblyBuilder = null;
		private ModuleBuilder _moduleBuilder = null;
        private object _nullInternal = null;
		private bool _canRead = false;
		private bool _canWrite = false;

		private static IDictionary _typeToOpcode = new HybridDictionary();

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

        /// <summary>
        /// Static constructor
        /// "Initialize a private hashtable with type-opCode pairs 
        /// </summary>
        static EmitPropertyAccessor()
        {
            _typeToOpcode[typeof(sbyte)] = OpCodes.Ldind_I1;
            _typeToOpcode[typeof(byte)] = OpCodes.Ldind_U1;
            _typeToOpcode[typeof(char)] = OpCodes.Ldind_U2;
            _typeToOpcode[typeof(short)] = OpCodes.Ldind_I2;
            _typeToOpcode[typeof(ushort)] = OpCodes.Ldind_U2;
            _typeToOpcode[typeof(int)] = OpCodes.Ldind_I4;
            _typeToOpcode[typeof(uint)] = OpCodes.Ldind_U4;
            _typeToOpcode[typeof(long)] = OpCodes.Ldind_I8;
            _typeToOpcode[typeof(ulong)] = OpCodes.Ldind_I8;
            _typeToOpcode[typeof(bool)] = OpCodes.Ldind_I1;
            _typeToOpcode[typeof(double)] = OpCodes.Ldind_R8;
            _typeToOpcode[typeof(float)] = OpCodes.Ldind_R4;
        }

        		
		/// <summary>
		/// Creates a new IL property accessor.
		/// </summary>
		/// <param name="targetType">Target object type.</param>
        /// <param name="propertyName">Property name.</param>
        /// <param name="assemblyBuilder"></param>
        /// <param name="moduleBuilder"></param>
        public EmitPropertyAccessor(Type targetType, string propertyName, AssemblyBuilder assemblyBuilder, ModuleBuilder moduleBuilder)
		{
			_assemblyBuilder = assemblyBuilder;
			_moduleBuilder = moduleBuilder;
			_targetType = targetType;
            _propertyName = propertyName;

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
				_propertyType = propertyInfo.PropertyType;
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
		public object Get(object target)
		{
			if (_canRead)
			{
				return _emittedPropertyAccessor.Get(target);
			}
			else
			{
				throw new MissingMethodException(
					string.Format("Property \"{0}\" on type "
					+ "{1} doesn't have a get method.", _propertyName, _targetType));
			}
		}


		/// <summary>
		/// Sets the property for the specified target.
		/// </summary>
		/// <param name="target">Target object.</param>
		/// <param name="value">Value to set.</param>
		public void Set(object target, object value)
		{
			if (_canWrite)
			{
				object newValue = value;
				if (newValue == null)
				{
					// If the value to assign is null, assign null internal value
					newValue = _nullInternal;
				}

				_emittedPropertyAccessor.Set(target, newValue);
			}
			else
			{
				throw new MissingMethodException(
					string.Format("Property \"{0}\" on type "
					+ "{1} doesn't have a set method.", _propertyName, _targetType));
			}
		}


		#endregion

		/// <summary>
		/// Get the null value for a given type
		/// </summary>
		/// <param name="type"></param>
		/// <returns></returns>
        private object GetNullInternal(Type type)
        {
            if (type.IsValueType)
            {
                if (type.IsEnum) 
				{ 
					return GetNullInternal(  Enum.GetUnderlyingType(type) );
				}

                if (type.IsPrimitive)
                {
                    if (type == typeof(Int32)) {return 0; }
                    if (type == typeof(Double)) {return (Double)0; }
                    if (type == typeof(Int16)) {return (Int16)0; }
                    if (type == typeof(SByte)) {return (SByte)0; }
                    if (type == typeof(Int64)) {return (Int64)0; }
                    if (type == typeof(Byte)) {return (Byte)0; }
                    if (type == typeof(UInt16)) {return (UInt16)0; }
                    if (type == typeof(UInt32)) {return (UInt32)0; }
                    if (type == typeof(UInt64)) {return (UInt64)0; }
                    if (type == typeof(UInt64)) {return (UInt64)0; }
                    if (type == typeof(Single)) {return (Single)0; }
                    if (type == typeof(Boolean)) {return false; }
                    if (type == typeof(char)) {return '\0'; }
                }
                else
                {
                    if (type == typeof(DateTime)) {return DateTime.MinValue; }
                    if (type == typeof(Decimal)) {return 0m; }
                    if (type == typeof(Guid)) {return Guid.Empty; }
                    if (type == typeof(TimeSpan)) { return TimeSpan.MinValue; }
                }
            }
 
            return null;
        }


		/// <summary>
		/// This method a new type oject for the the property accessor class 
		/// that will provide dynamic access.
		/// </summary>
		private void EmitIL()
		{
			// Create a new type oject for the the property accessor class.
            EmitType();

			// Create a new instance
            _emittedPropertyAccessor = _assemblyBuilder.CreateInstance("PropertyAccessorFor" + _targetType.FullName + _propertyName) as IMemberAccessor;
            
            _nullInternal = GetNullInternal(_propertyType);

			if(_emittedPropertyAccessor == null)
			{
                throw new MethodAccessException(
					string.Format("Unable to create property accessor for \"{0}\".", _propertyName));
			}
		}

		
		/// <summary>
		/// Create an type that will provide the get and set access method.
		/// </summary>
		private void EmitType()
		{
			// Define a public class named "PropertyAccessorFor.FullTagetTypeName.PropertyName" in the assembly.
            TypeBuilder typeBuilder = _moduleBuilder.DefineType("PropertyAccessorFor" + _targetType.FullName + _propertyName, TypeAttributes.Class | TypeAttributes.Public | TypeAttributes.Sealed);

			// Mark the class as implementing IMemberAccessor. 
			typeBuilder.AddInterfaceImplementation(typeof(IMemberAccessor));

			// Add a constructor
			typeBuilder.DefineDefaultConstructor(MethodAttributes.Public);

			// Define a method named "Get" for the get operation (IMemberAccessor). 
			Type[] getParamTypes = new Type[] {typeof(object)};
			MethodBuilder getMethod = 
				typeBuilder.DefineMethod("Get", 
				MethodAttributes.Public | MethodAttributes.Virtual,
                typeof(object), 
				getParamTypes);

			// Get an ILGenerator and used it to emit the IL that we want.
			ILGenerator getIL = getMethod.GetILGenerator();

			// Emit the IL for get access. 
			MethodInfo targetGetMethod = _targetType.GetMethod("get_" + _propertyName);

			if(targetGetMethod != null)
			{
				getIL.DeclareLocal(typeof(object));
				getIL.Emit(OpCodes.Ldarg_1);	//Load the first argument,(target object)
				getIL.Emit(OpCodes.Castclass, _targetType);	//Cast to the source type
				getIL.EmitCall(OpCodes.Call, targetGetMethod, null); //Get the property value
				if(targetGetMethod.ReturnType.IsValueType)
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


			// Define a method named "Set" for the set operation (IMemberAccessor).
			Type[] setParamTypes = new Type[] {typeof(object), typeof(object)};
			MethodBuilder setMethod = 
				typeBuilder.DefineMethod("Set", 
				MethodAttributes.Public | MethodAttributes.Virtual, 
				null, 
				setParamTypes);

			// Get an ILGenerator and  used to emit the IL that we want.
			ILGenerator setIL = setMethod.GetILGenerator();
			// Emit the IL for the set access. 
			MethodInfo targetSetMethod = _targetType.GetMethod("set_" + _propertyName);
			if(targetSetMethod != null)
			{
				Type paramType = targetSetMethod.GetParameters()[0].ParameterType;
				setIL.DeclareLocal(paramType);
				setIL.Emit(OpCodes.Ldarg_1); //Load the first argument (target object)
				setIL.Emit(OpCodes.Castclass, _targetType); //Cast to the source type
				setIL.Emit(OpCodes.Ldarg_2); //Load the second argument (value object)
				if(paramType.IsValueType)
				{
					setIL.Emit(OpCodes.Unbox, paramType); //Unbox it 	
					if(_typeToOpcode[paramType]!=null)					
					{
						OpCode load = (OpCode)_typeToOpcode[paramType];
						setIL.Emit(load); //and load
					}
					else
					{
						setIL.Emit(OpCodes.Ldobj,paramType);
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

			// Load the type
			typeBuilder.CreateType();
		}

	}
}