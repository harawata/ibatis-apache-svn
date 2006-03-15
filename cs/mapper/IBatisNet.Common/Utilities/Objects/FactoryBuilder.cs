#region Apache Notice
/*****************************************************************************
 * $Revision: 374175 $
 * $LastChangedDate: 2006-02-19 12:37:22 +0100 (Sun, 19 Feb 2006) $
 * $LastChangedBy: gbayon $
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
using IBatisNet.Common.Exceptions;

namespace IBatisNet.Common.Utilities.Objects
{
	/// <summary>
	/// Build IFactory object via IL 
	/// </summary>
	public class FactoryBuilder
	{
		private ModuleBuilder _moduleBuilder = null;

		/// <summary>
		/// constructor
		/// </summary>
		public FactoryBuilder()
		{
			AssemblyName assemblyName = new AssemblyName();
            assemblyName.Name = "iBATIS.EmitFactory" + HashCodeProvider.GetIdentityHashCode(this).ToString();

			// Create a new assembly with one module
			AssemblyBuilder _assemblyBuilder = AppDomain.CurrentDomain.DefineDynamicAssembly(assemblyName, AssemblyBuilderAccess.Run);
			_moduleBuilder = _assemblyBuilder.DefineDynamicModule(assemblyName.Name + ".dll");
		}


		/// <summary>
		/// 
		/// </summary>
		/// <param name="typeToCreate"></param>
		/// <returns></returns>
		public IFactory CreateFactory(Type typeToCreate)
		{
			Type innerType = CreateFactoryType(typeToCreate);
			ConstructorInfo ctor = innerType.GetConstructor(new Type[] {});
			return (IFactory) ctor.Invoke(new object[] {});
		}


		/// <summary>
		/// 
		/// </summary>
		/// <param name="typeToCreate"></param>
		/// <returns></returns>
		private Type CreateFactoryType(Type typeToCreate)
		{
			TypeBuilder typeBuilder = _moduleBuilder.DefineType("EmitFactoryFor" + typeToCreate.Name, TypeAttributes.Public);
			typeBuilder.AddInterfaceImplementation(typeof (IFactory));
			ImplementCreateInstance(typeBuilder, typeToCreate);
			return typeBuilder.CreateType();
		}

		
		private MethodAttributes createMethodAttributes = MethodAttributes.Public | MethodAttributes.HideBySig | MethodAttributes.NewSlot | MethodAttributes.Virtual | MethodAttributes.Final;

		private void ImplementCreateInstance(TypeBuilder typeBuilder, Type typeToCreate)
		{
			MethodBuilder meth = typeBuilder.DefineMethod("CreateInstance", createMethodAttributes, typeof (object), Type.EmptyTypes);
			ILGenerator il = meth.GetILGenerator();

			// Add test if contructeur not public
			ConstructorInfo ctor = typeToCreate.GetConstructor(Type.EmptyTypes);
			if (!ctor.IsPublic)
			{
				throw new ProbeException(string.Format("Unable to optimize create instance for type \"{0}\". Cause no public constructor on type. ", typeToCreate.Name));
			}
			il.Emit(OpCodes.Newobj, ctor);
			il.Emit(OpCodes.Ret);
		}
	}
}
