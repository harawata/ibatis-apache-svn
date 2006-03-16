using System;
using System.Collections;
using System.Collections.Specialized;
using System.Reflection;
using System.Reflection.Emit;
using IBatisNet.Common.Exceptions;

namespace IBatisNet.Common.Utilities.Objects.Members
{
	/// <summary>
	/// The EmitFieldAccessor class provides an IL-based access   
	/// to a field of a specified target class.
	/// </summary>
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
			this._assemblyBuilder = assemblyBuilder;
			this._moduleBuilder = moduleBuilder;
			this._targetType = targetType;
			this._fieldName = fieldName;

			FieldInfo fieldInfo = targetType.GetField(fieldName);

			// Make sure the property exists
			if(fieldInfo == null)
			{
				throw new ProbeException(
					string.Format("Field \"{0}\" does not exist for type "
					+ "{1}.", fieldName, targetType));
			}
			else
			{
				this._fieldType = fieldInfo.FieldType;
//				this.Emit();
			}
		}

		#region IMemberAccessor Members

		/// <summary>
		/// Gets the field value from the specified target.
		/// </summary>
		/// <param name="target">Target object.</param>
		/// <returns>Property value.</returns>
		public object Get(object target)
		{
			// TODO:  Add EmitFieldAccessor.Get implementation
			return null;
		}

		/// <summary>
		/// Sets the field for the specified target.
		/// </summary>
		/// <param name="target">Target object.</param>
		/// <param name="value">Value to set.</param>
		public void Set(object target, object value)
		{
			// TODO:  Add EmitFieldAccessor.Set implementation
		}

		#endregion
	}
}
