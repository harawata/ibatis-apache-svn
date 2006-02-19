
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

#region Imports

using System;
using System.Collections;
using System.Collections.Specialized;

#endregion

namespace IBatisNet.Common.Utilities.TypesResolver
{
	/// <summary>
	/// Provides (and resolves) aliases for the common types.
	/// </summary>
	/// <remarks>
	/// <p>
	/// It's really just syntactic sugar so that a type definition can be referenced as 
	/// (in, say, a config file) can use 'int' instead of 'System.Int32'.
	/// </p>
	/// </remarks>
	public class TypeAliasResolver
	{
		#region Constants

		/// <summary>
		/// The alias around the 'list' type.
		/// </summary>
		public const string ArrayListAlias1 = "arraylist";
		/// <summary>
		/// Another alias around the 'list' type.
		/// </summary>
		public const string ArrayListAlias2 = "list";

		/// <summary>
		/// Another alias around the 'bool' type.
		/// </summary>
		public const string BoolAlias = "bool";
		/// <summary>
		/// The alias around the 'Boolean' type (Visual Basic.NET style).
		/// </summary>
		public const string BoolAliasVB = "Boolean";
		/// <summary>
		/// The alias around the 'bool' type.
		/// </summary>
		public const string BooleanAlias = "boolean";

		/// <summary>
		/// The alias around the 'byte' type.
		/// </summary>
		public const string ByteAlias = "byte";

		/// <summary>
		/// The alias around the 'char' type.
		/// </summary>
		public const string CharAlias = "char";
		/// <summary>
		/// The alias around the 'Char' type (Visual Basic.NET style).
		/// </summary>
		public const string CharAliasVB = "Char";

		/// <summary>
		/// The alias around the 'DateTime' type.
		/// </summary>
		public const string DateAlias1 = "datetime";
		/// <summary>
		/// Another alias around the 'DateTime' type.
		/// </summary>
		public const string DateAlias2 = "date";
		/// <summary>
		/// The alias around the 'DateTime' type (Visual Basic.NET style).
		/// </summary>
		public const string DateAliasVB = "Date";

		/// <summary>
		/// The alias around the 'decimal' type.
		/// </summary>
		public const string DecimalAlias = "decimal";
		/// <summary>
		/// The alias around the 'Decimal' type (Visual Basic.NET style).
		/// </summary>
		public const string DecimalAliasVB = "Decimal";

		/// <summary>
		/// The alias around the 'double' type.
		/// </summary>
		public const string DoubleAlias = "double";
		/// <summary>
		/// The alias around the 'Double' type (Visual Basic.NET style).
		/// </summary>
		public const string DoubleAliasVB = "Double";

		/// <summary>
		/// The alias around the 'float' type.
		/// </summary>
		public const string FloatAlias = "float";
		/// <summary>
		/// Another alias around the 'float' type.
		/// </summary>
		public const string FloatAliasVB = "Single";
		/// <summary>
		/// Another alias around the 'float' type.
		/// </summary>
		public const string SingleAlias = "single";

		/// <summary>
		/// The alias around the 'guid' type.
		/// </summary>
		public const string GuidAlias = "guid";

		/// <summary>
		/// The alias around the 'Hashtable' type.
		/// </summary>
		public const string HashtableAlias1 = "hashtable";
		/// <summary>
		/// Another alias around the 'Hashtable' type.
		/// </summary>
		public const string HashtableAlias2 = "map";
		/// <summary>
		/// Another alias around the 'Hashtable' type.
		/// </summary>
		public const string HashtableAlias3 = "hashmap";

		/// <summary>
		/// The alias around the 'short' type.
		/// </summary>
		public const string Int16Alias1 = "int16";
		/// <summary>
		/// Another alias around the 'short' type.
		/// </summary>
		public const string Int16Alias2 = "short";
		/// <summary>
		/// The alias around the 'Short' type (Visual Basic.NET style).
		/// </summary>
		public const string Int16AliasVB = "Short";

		/// <summary>
		/// The alias around the 'int' type.
		/// </summary>
		public const string Int32Alias1 = "Int32";
		/// <summary>
		/// Another alias around the 'int' type.
		/// </summary>
		public const string Int32Alias2 = "int";
		/// <summary>
		/// Another alias around the 'int' type.
		/// </summary>
		public const string Int32Alias3 = "integer";
		/// <summary>
		/// The alias around the 'Integer' type (Visual Basic.NET style).
		/// </summary>
		public const string Int32AliasVB = "Integer";

		/// <summary>
		/// The alias around the 'long' type.
		/// </summary>
		public const string Int64Alias1 = "int64";
		/// <summary>
		/// Another alias around the 'long' type.
		/// </summary>
		public const string Int64Alias2 = "long";
		/// <summary>
		/// The alias around the 'Long' type (Visual Basic.NET style).
		/// </summary>
		public const string Int64AliasVB = "Long";

		/// <summary>
		/// The alias around the 'unsigned short' type.
		/// </summary>
		public const string UInt16Alias1 = "uint16";
		/// <summary>
		/// Another alias around the 'unsigned short' type.
		/// </summary>
		public const string UInt16Alias2 = "ushort";

		/// <summary>
		/// The alias around the 'unsigned int' type.
		/// </summary>
		public const string UInt32Alias1 = "uint32";
		/// <summary>
		/// Another alias around the 'unsigned int' type.
		/// </summary>
		public const string UInt32Alias2 = "uint";

		/// <summary>
		/// The alias around the 'unsigned long' type.
		/// </summary>
		public const string UInt64Alias1 = "uint64";
		/// <summary>
		/// Another alias around the 'unsigned long' type.
		/// </summary>
		public const string UInt64Alias2 = "ulong";

		/// <summary>
		/// The alias around the 'SByte' type.
		/// </summary>
		public const string SByteAlias = "sbyte";

		/// <summary>
		/// The alias around the 'string' type.
		/// </summary>
		public const string StringAlias = "string";
		/// <summary>
		/// The alias around the 'string' type (Visual Basic.NET style).
		/// </summary>
		public const string StringAliasVB = "String";

		/// <summary>
		/// The alias around the 'TimeSpan' type.
		/// </summary>
		public const string TimeSpanAlias = "timespan";

		#endregion

		#region Fields
		private static readonly StringDictionary _aliases = new StringDictionary();
		#endregion

		#region Constructor (s) / Destructor
		/// <summary>
		/// Creates a new instance of the TypeAliasFactory class.
		/// </summary>
		/// <remarks>
		/// <p>
		/// This is a utility class, and as such has no publicly visible
		/// constructors.
		/// </p>
		/// </remarks>
		private TypeAliasResolver() {}

		/// <summary>
		/// Initialises the static properties of the TypeAliasResolver class.
		/// </summary>
		static TypeAliasResolver()
		{
			// Initialize a dictionary with some fully qualifiaed name 
			_aliases [TypeAliasResolver.ArrayListAlias1] = typeof (ArrayList).FullName;
			_aliases [TypeAliasResolver.ArrayListAlias2] = typeof (ArrayList).FullName;

			_aliases [TypeAliasResolver.BoolAlias] = typeof (bool).FullName;
			_aliases [TypeAliasResolver.BoolAliasVB] = typeof (bool).FullName;
			_aliases [TypeAliasResolver.BooleanAlias] = typeof (bool).FullName;

			_aliases [TypeAliasResolver.ByteAlias] = typeof (byte).FullName;

			_aliases [TypeAliasResolver.CharAlias] = typeof (char).FullName;
			_aliases [TypeAliasResolver.CharAliasVB] = typeof (char).FullName;

			_aliases [TypeAliasResolver.DateAlias1] = typeof (DateTime).FullName;
			_aliases [TypeAliasResolver.DateAlias2] = typeof (DateTime).FullName;
			_aliases [TypeAliasResolver.DateAliasVB] = typeof (DateTime).FullName;

			_aliases [TypeAliasResolver.DecimalAlias] = typeof (decimal).FullName;
			_aliases [TypeAliasResolver.DecimalAliasVB] = typeof (decimal).FullName;

			_aliases [TypeAliasResolver.DoubleAlias] = typeof (double).FullName;
			_aliases [TypeAliasResolver.DoubleAliasVB] = typeof (double).FullName;

			_aliases [TypeAliasResolver.FloatAlias] = typeof (float).FullName;
			_aliases [TypeAliasResolver.FloatAliasVB] = typeof (float).FullName;
			_aliases [TypeAliasResolver.SingleAlias] = typeof (float).FullName;

			_aliases [TypeAliasResolver.GuidAlias] = typeof (Guid).FullName;

			_aliases [TypeAliasResolver.HashtableAlias1] = typeof (Hashtable).FullName;
			_aliases [TypeAliasResolver.HashtableAlias2] = typeof (Hashtable).FullName;
			_aliases [TypeAliasResolver.HashtableAlias3] = typeof (Hashtable).FullName;

			_aliases [TypeAliasResolver.Int16Alias1] = typeof (short).FullName;
			_aliases [TypeAliasResolver.Int16Alias2] = typeof (short).FullName;
			_aliases [TypeAliasResolver.Int16AliasVB] = typeof (short).FullName;

			_aliases [TypeAliasResolver.Int32Alias1] = typeof (int).FullName;
			_aliases [TypeAliasResolver.Int32Alias2] = typeof (int).FullName;
			_aliases [TypeAliasResolver.Int32Alias3] = typeof (int).FullName;
			_aliases [TypeAliasResolver.Int32AliasVB] = typeof (int).FullName;

			_aliases [TypeAliasResolver.Int64Alias1] = typeof (long).FullName;
			_aliases [TypeAliasResolver.Int64Alias2] = typeof (long).FullName;
			_aliases [TypeAliasResolver.Int64AliasVB] = typeof (long).FullName;

			_aliases [TypeAliasResolver.UInt16Alias1] = typeof (ushort).FullName;
			_aliases [TypeAliasResolver.UInt16Alias2] = typeof (ushort).FullName;

			_aliases [TypeAliasResolver.UInt32Alias1] = typeof (uint).FullName;
			_aliases [TypeAliasResolver.UInt32Alias2] = typeof (uint).FullName;

			_aliases [TypeAliasResolver.UInt64Alias1] = typeof (ulong).FullName;
			_aliases [TypeAliasResolver.UInt64Alias2] = typeof (ulong).FullName;

			_aliases [TypeAliasResolver.SByteAlias] = typeof (sbyte).FullName;

			_aliases [TypeAliasResolver.StringAlias] = typeof (string).FullName;
			_aliases [TypeAliasResolver.StringAliasVB] = typeof (string).FullName;

			_aliases [TypeAliasResolver.TimeSpanAlias] = typeof (string).FullName;

		}
		#endregion

		#region Methods
		
		/// <summary>
		/// Resolves the supplied type name.
		/// </summary>
		/// <remarks>
		/// <p>
		/// If the supplied type name is an alias, the fully resolved
		/// type name is returned. If no alias could be found that matches
		/// the supplied type name, then the type name will be returned as is.
		/// </p>
		/// </remarks>
		/// <param name="type">The supplied type name.</param>
		/// <returns>
		/// If the supplied type name is an alias, the fully resolved
		/// type name is returned. If no alias could be found that matches
		/// the supplied type name, then the type name will be returned as is.
		/// </returns>
		public static string Resolve (string type) 
		{
			if (type != null)
			{
				if (_aliases.ContainsKey (type.ToLower ())) 
				{
					return _aliases [type] as string;
				}
			}
			return type;
		}


		/// <summary>
		/// Instantiate a 'Primitive' Type.
		/// </summary>
		/// <param name="typeCode">a typeCode.</param>
		/// <returns>An object.</returns>
		public static object InstantiatePrimitiveType(TypeCode typeCode)
		{
			object resultObject = null;

			switch(typeCode)
			{
				case TypeCode.Boolean :
					resultObject = new Boolean();
					break;
				case TypeCode.Byte :
					resultObject = new Byte();
					break;
				case TypeCode.Char :
					resultObject = new Char();
					break;						
				case TypeCode.DateTime :
					resultObject = new DateTime();
					break;
				case TypeCode.Decimal :
					resultObject = new Decimal();
					break;
				case TypeCode.Double :
					resultObject = new Double();
					break;	
				case TypeCode.Int16 :
					resultObject = new Int16();
					break;	
				case TypeCode.Int32 :
					resultObject = new Int32();
					break;	
				case TypeCode.Int64 :
					resultObject = new Int64();
					break;	
				case TypeCode.SByte :
					resultObject = new SByte();
					break;	
				case TypeCode.Single :
					resultObject = new Single();
					break;	
				case TypeCode.String :
					resultObject = "";
					break;	
				case TypeCode.UInt16 :
					resultObject = new UInt16();
					break;		
				case TypeCode.UInt32 :
					resultObject = new UInt32();
					break;	
				case TypeCode.UInt64 :
					resultObject = new UInt64();
					break;		
			}
			return resultObject;
		}

		#endregion

	}
}
