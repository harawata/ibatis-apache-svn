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
	/// The EmitFieldAccessor class provides an IL-based access   
	/// to a field of a specified target class.
	/// </summary>
	/// <remarks>Not Finish. Throw FieldAccessException on SetValue</remarks>
	public class EmitFieldAccessor : IMemberAccessor
	{
		private Type _targetType = null;
		private string _fieldName = string.Empty;
		private Type _fieldType = null;
		private IMemberAccessor _emittedFieldAccessor = null;
		private AssemblyBuilder _assemblyBuilder = null;
		private ModuleBuilder _moduleBuilder = null;
		private object _nullInternal = null;

		private static IDictionary _typeToOpcode = new HybridDictionary();

		/// <summary>
		/// Static constructor
		/// "Initialize a private hashtable with type-opCode pairs 
		/// </summary>
		static EmitFieldAccessor()
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
		/// Creates a new IL field accessor.
		/// </summary>
		/// <param name="targetType">Target object type.</param>
		/// <param name="fieldName">Field name.</param>
		/// <param name="assemblyBuilder"></param>
		/// <param name="moduleBuilder"></param>
		public EmitFieldAccessor(Type targetType, string fieldName, AssemblyBuilder assemblyBuilder, ModuleBuilder moduleBuilder)
		{
			_assemblyBuilder = assemblyBuilder;
			_moduleBuilder = moduleBuilder;
			_targetType = targetType;
			_fieldName = fieldName;

			FieldInfo fieldInfo = targetType.GetField(fieldName, BindingFlags.NonPublic | BindingFlags.Instance | BindingFlags.Public);

			// Make sure the field exists
			if(fieldInfo == null)
			{
				throw new MissingMethodException(
					string.Format("Field \"{0}\" does not exist for type "
					+ "{1}.", fieldName, targetType));
			}
			else
			{
				this._fieldType = fieldInfo.FieldType;
				this.EmitIL();
			}
		}

		
		/// <summary>
		/// This method a new type oject for the the field accessor class 
		/// that will provide dynamic access.
		/// </summary>
		private void EmitIL()
		{
			// Create a new type oject for the the field accessor class.
			EmitType();

			// Create a new instance
			_emittedFieldAccessor = _assemblyBuilder.CreateInstance("FieldAccessorFor" + _targetType.FullName + _fieldName) as IMemberAccessor;
            
			_nullInternal = GetNullInternal(_fieldType);

			if(_emittedFieldAccessor == null)
			{
				throw new MethodAccessException(
					string.Format("Unable to create field accessor for \"{0}\".", _fieldType));
			}
		}

		
		/// <summary>
		/// Create an type that will provide the get and set methods.
		/// </summary>
		private void EmitType()
		{
			// Define a public class named "FieldAccessorFor.FullTagetTypeName.FieldName" in the assembly.
			TypeBuilder typeBuilder = _moduleBuilder.DefineType("FieldAccessorFor" + _targetType.FullName + _fieldName, TypeAttributes.Class | TypeAttributes.Public | TypeAttributes.Sealed);

			// Mark the class as implementing IMemberAccessor. 
			typeBuilder.AddInterfaceImplementation(typeof(IMemberAccessor));

			// Add a constructor
			typeBuilder.DefineDefaultConstructor(MethodAttributes.Public);

			// Define a method named "Get" for the get operation (IMemberAccessor). 
			Type[] getParamTypes = new Type[] {typeof(object)};
			MethodBuilder getMethod = typeBuilder.DefineMethod("Get", 
				MethodAttributes.Public | MethodAttributes.Virtual,
				typeof(object), 
				getParamTypes);

			// Get an ILGenerator and used it to emit the IL that we want.
			ILGenerator getIL = getMethod.GetILGenerator();

			FieldInfo targetField = _targetType.GetField(_fieldName, BindingFlags.NonPublic | BindingFlags.Instance | BindingFlags.Public);
			
			// Emit the IL for get access. 
			if(targetField != null)
			{
				getIL.Emit(OpCodes.Ldarg_1); 
				getIL.Emit(OpCodes.Castclass, targetField.DeclaringType); 
				getIL.Emit(OpCodes.Ldfld, targetField); 

//				getIL.DeclareLocal(typeof(object));
//				getIL.Emit(OpCodes.Ldarg_0);//Load the first argument,(target object)
//				getIL.Emit(OpCodes.Castclass, _targetType);	//Cast to the source type
//				getIL.Emit(OpCodes.Ldfld, targetField);
//				if(targetField.FieldType.IsValueType)
//				{
//					getIL.Emit(OpCodes.Box, targetField.FieldType); //Box if necessary
//				}
//				getIL.Emit(OpCodes.Stloc_0);
//				getIL.Emit(OpCodes.Ldloc_0);
			}
			else
			{
				getIL.ThrowException(typeof(MissingMethodException));
			}
			getIL.Emit(OpCodes.Ret);

			// Define a method named "Set" for the set operation (IMemberAccessor).
			Type[] setParamTypes = new Type[] {typeof(object), typeof(object)};
			MethodBuilder setMethod = typeBuilder.DefineMethod("Set", 
				MethodAttributes.Public | MethodAttributes.Virtual, 
				null, 
				setParamTypes);

			// Get an ILGenerator and used to emit the IL that we want.
			ILGenerator setIL = setMethod.GetILGenerator();
			// Emit the IL for the set access. 
			if(targetField != null)
			{
				setIL.Emit(OpCodes.Ldarg_0);//Load the first argument (target object)
				setIL.Emit(OpCodes.Ldarg_1);//Load the second argument (value object)
				setIL.Emit(OpCodes.Stfld, targetField); //Set the field value
			}
			else
			{
				setIL.ThrowException(typeof(MissingMethodException));
			}
			setIL.Emit(OpCodes.Ret);

			// Load the type
			typeBuilder.CreateType();
		}

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


		#region IMemberAccessor Members

		/// <summary>
		/// Gets the field value from the specified target.
		/// </summary>
		/// <param name="target">Target object.</param>
		/// <returns>Property value.</returns>
		public object Get(object target)
		{
			return _emittedFieldAccessor.Get(target);
		}

		/// <summary>
		/// Sets the field for the specified target.
		/// </summary>
		/// <param name="target">Target object.</param>
		/// <param name="value">Value to set.</param>
		public void Set(object target, object value)
		{
			object newValue = value;
			if (newValue == null)
			{
				// If the value to assign is null, assign null internal value
				newValue = _nullInternal;
			}
			_emittedFieldAccessor.Set(target, newValue);
		}

		#endregion
	}
}


