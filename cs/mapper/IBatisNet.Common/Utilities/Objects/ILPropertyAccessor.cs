#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: 383310 $
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
using System.Reflection;
using System.Reflection.Emit;
using System.Threading;
using System.Collections;
using System.Collections.Specialized;
using IBatisNet.Common.Exceptions;

namespace IBatisNet.Common.Utilities.Objects
{
    /// <summary>
    /// The ILPropertyAccessor class provides an IL-based access   
    /// to a property of a specified target class.
    /// </summary>
    public class ILPropertyAccessor : IPropertyAccessor
	{
        private Type _targetType = null;
        private string _propertyName = string.Empty;
        private Type _propertyType = null;
        private IPropertyAccessor _emittedPropertyAccessor = null;
        private static IDictionary _typeToOpcode = new HybridDictionary();
        private static AssemblyBuilder _assemblyBuilder = null;
        private static ModuleBuilder _moduleBuilder = null;
        private static IDictionary _cachedIPropertyAccessor = new HybridDictionary();
        private static IDictionary _defaultValueType = new HybridDictionary();

        /// <summary>
        /// Static constructor
        /// "Initialize a private hashtable with type-opCode pairs 
        /// so we dont have to write a long if/else statement when outputting msil"
        /// </summary>
        static ILPropertyAccessor()
        {
            _defaultValueType[typeof(sbyte)] = 0;
            _defaultValueType[typeof(byte)] = 0;
            _defaultValueType[typeof(char)] = '\0';
            _defaultValueType[typeof(short)] = 0;
            _defaultValueType[typeof(ushort)] = 0;
            _defaultValueType[typeof(int)] = 0;
            _defaultValueType[typeof(uint)] = 0;
            _defaultValueType[typeof(long)] = 0L;
            _defaultValueType[typeof(ulong)] = 0L;
            _defaultValueType[typeof(bool)] = false;
            _defaultValueType[typeof(double)] = 0.0D;
            _defaultValueType[typeof(float)] = 0.0F;
           // _defaultValueType[typeof(decimal)] = 0.0M;

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

            AssemblyName assemblyName = new AssemblyName();
            assemblyName.Name = "iBATIS.FastPropertyAccessor";

            // Create a new assembly with one module
            _assemblyBuilder = AppDomain.CurrentDomain.DefineDynamicAssembly(assemblyName, AssemblyBuilderAccess.Run);
            _moduleBuilder = _assemblyBuilder.DefineDynamicModule(assemblyName.Name + ".dll");
        }

        /// <summary>
        /// Generate the IPropertyAccessor object
        /// </summary>
        /// <param name="targetType">Target object type.</param>
        /// <param name="propertyName">Property name.</param>
        /// <returns>null if the generation fail</returns>
        public static IPropertyAccessor CreatePropertyAccessor(Type targetType, string propertyName)
        {
            string key = targetType.FullName+propertyName;
            lock (_assemblyBuilder)
            {
                if (_cachedIPropertyAccessor.Contains(key))
                {
                    return (IPropertyAccessor)_cachedIPropertyAccessor[key];
                }
                else
                {
                    IPropertyAccessor propertyAccessor = new ILPropertyAccessor(targetType, propertyName);
                    _cachedIPropertyAccessor[key] = propertyAccessor;
                    return propertyAccessor;
                }
            }
        }

		/// <summary>
		/// Creates a new IL property accessor.
		/// </summary>
		/// <param name="targetType">Target object type.</param>
        /// <param name="propertyName">Property name.</param>
        private ILPropertyAccessor(Type targetType, string propertyName)
		{
			this._targetType = targetType;
            this._propertyName = propertyName;

            PropertyInfo propertyInfo = targetType.GetProperty(propertyName);

			//
			// Make sure the property exists
			//
			if(propertyInfo == null)
			{
				throw new ProbeException(
					string.Format("Property \"{0}\" does not exist for type "
                    + "{1}.", propertyName, targetType));
			}
			else
			{
				this._propertyType = propertyInfo.PropertyType;
                this.Init();
			}
		}


		/// <summary>
		/// Gets the property value from the specified target.
		/// </summary>
		/// <param name="target">Target object.</param>
		/// <returns>Property value.</returns>
		public object Get(object target)
		{
			return this._emittedPropertyAccessor.Get(target);
		}

		/// <summary>
		/// Sets the property for the specified target.
		/// </summary>
		/// <param name="target">Target object.</param>
		/// <param name="value">Value to set.</param>
		public void Set(object target, object value)
		{
            // If the value to assign is null and this property
            // accessor is for a value type, use a default value instead
            object newValue = value;
            if (newValue == null && _propertyType.IsValueType)
            {
                newValue = _defaultValueType[_propertyType];
            }

            this._emittedPropertyAccessor.Set(target, newValue);
		}


		/// <summary>
		/// The Type of object this property accessor was
		/// created for.
		/// </summary>
		public Type TargetType
		{
			get { return this._targetType; }
		}

		/// <summary>
		/// The Type of the Property being accessed.
		/// </summary>
		public Type PropertyType
		{
			get { return this._propertyType; }
		}


		/// <summary>
		/// This method generates creates a new assembly containing
		/// the Type that will provide dynamic access.
		/// </summary>
		private void Init()
		{
			// Create the assembly and an instance of the property accessor class.
            EmitType();

            _emittedPropertyAccessor = _assemblyBuilder.CreateInstance("PropertyAccessorFor" + _propertyName) as IPropertyAccessor;

			if(_emittedPropertyAccessor == null)
			{
                throw new ProbeException(string.Format("Unable to create property accessor for \"{0}\".", _propertyName));
			}
		}

		/// <summary>
		/// Create an assembly that will provide the get and set methods.
		/// </summary>
		private void EmitType()
		{
			//  Define a public class named "Property" in the assembly.
            TypeBuilder typeBuilder = _moduleBuilder.DefineType("PropertyAccessorFor" + _propertyName, TypeAttributes.Public);

			// Mark the class as implementing IPropertyAccessor. 
			typeBuilder.AddInterfaceImplementation(typeof(IPropertyAccessor));

			// Add a constructor
			ConstructorBuilder constructorBuilder = typeBuilder.DefineDefaultConstructor(MethodAttributes.Public);

			// Define a method for the get operation. 
			Type[] getParamTypes = new Type[] {typeof(object)};
			MethodBuilder getMethod = 
				typeBuilder.DefineMethod("Get", 
				MethodAttributes.Public | MethodAttributes.Virtual,
                typeof(object), 
				getParamTypes);

			// From the method, get an ILGenerator. This is used to
			// emit the IL that we want.
			ILGenerator getIL = getMethod.GetILGenerator();

			// Emit the IL. 
			MethodInfo targetGetMethod = this._targetType.GetMethod("get_" + this._propertyName);

			if(targetGetMethod != null)
			{
				
				getIL.DeclareLocal(typeof(object));
				getIL.Emit(OpCodes.Ldarg_1);								//Load the first argument 
																			//(target object)

				getIL.Emit(OpCodes.Castclass, this._targetType);			//Cast to the source type

				getIL.EmitCall(OpCodes.Call, targetGetMethod, null);		//Get the property value

				if(targetGetMethod.ReturnType.IsValueType)
				{
					getIL.Emit(OpCodes.Box, targetGetMethod.ReturnType);	//Box if necessary
				}
				getIL.Emit(OpCodes.Stloc_0);								//Store it
			
				getIL.Emit(OpCodes.Ldloc_0);
			}
			else
			{
				getIL.ThrowException(typeof(MissingMethodException));
			}

			getIL.Emit(OpCodes.Ret);


			// Define a method for the set operation.
			Type[] setParamTypes = new Type[] {typeof(object), typeof(object)};
			Type setReturnType = null;
			MethodBuilder setMethod = 
				typeBuilder.DefineMethod("Set", 
				MethodAttributes.Public | MethodAttributes.Virtual, 
				setReturnType, 
				setParamTypes);

			// From the method, get an ILGenerator. This is used to
			// emit the IL that we want.
			ILGenerator setIL = setMethod.GetILGenerator();
			// Emit the IL. 
			MethodInfo targetSetMethod = this._targetType.GetMethod("set_" + this._propertyName);
			if(targetSetMethod != null)
			{
				Type paramType = targetSetMethod.GetParameters()[0].ParameterType;

				setIL.DeclareLocal(paramType);
				setIL.Emit(OpCodes.Ldarg_1);						//Load the first argument 
																	//(target object)

				setIL.Emit(OpCodes.Castclass, this._targetType);	//Cast to the source type

				setIL.Emit(OpCodes.Ldarg_2);						//Load the second argument 
																	//(value object)

				if(paramType.IsValueType)
				{
					setIL.Emit(OpCodes.Unbox, paramType);			//Unbox it 	
					if(_typeToOpcode[paramType]!=null)					//and load
					{
						OpCode load = (OpCode)_typeToOpcode[paramType];
						setIL.Emit(load);
					}
					else
					{
						setIL.Emit(OpCodes.Ldobj,paramType);
					}
				}
				else
				{
					setIL.Emit(OpCodes.Castclass, paramType);		//Cast class
				}
			
				setIL.EmitCall(OpCodes.Callvirt, 
					targetSetMethod, null);							//Set the property value
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