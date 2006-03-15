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

namespace IBatisNet.Common.Utilities.Objects
{
	/// <summary>
	/// Summary description for PropertyAccessorFactory.
	/// </summary>
	public class PropertyAccessorFactory
	{
		private IDictionary _cachedIPropertyAccessor = new HybridDictionary();
		private delegate IPropertyAccessor CreateIPropertyAccessor(Type targetType, string propertyName);
		private CreateIPropertyAccessor _createPropertyAccessor;
		private AssemblyBuilder _assemblyBuilder = null;
		private ModuleBuilder _moduleBuilder = null;
		private object _padlock = new object();

		/// <summary>
		/// Constructor
		/// </summary>
		/// <param name="allowCodeGeneration"></param>
		public PropertyAccessorFactory(bool allowCodeGeneration)
		{
			if (allowCodeGeneration)
			{
				AssemblyName assemblyName = new AssemblyName();
				assemblyName.Name = "iBATIS.FastPropertyAccessor"+HashCodeProvider.GetIdentityHashCode(this).ToString();

				// Create a new assembly with one module
				_assemblyBuilder = AppDomain.CurrentDomain.DefineDynamicAssembly(assemblyName, AssemblyBuilderAccess.Run);
				_moduleBuilder = _assemblyBuilder.DefineDynamicModule(assemblyName.Name + ".dll");

				// Detect runtime environment and create the appropriate factory
				if (Environment.Version.Major >= 2)
				{
					// To Do : a custom factory for .NET V2
					// optimize with DynamicMethod or Delegate.CreateDelegate
					_createPropertyAccessor = new CreateIPropertyAccessor(CreateILPropertyAccessor);
					
				}
				else
				{
					_createPropertyAccessor = new CreateIPropertyAccessor(CreateILPropertyAccessor);
				}
			}
			else
			{
				_createPropertyAccessor = new CreateIPropertyAccessor(CreateReflectionPropertyAccessor);
			}

		}

		/// <summary>
		/// Generate an IPropertyAccessor object
		/// </summary>
		/// <param name="targetType">Target object type.</param>
		/// <param name="propertyName">Property name.</param>
		/// <returns>null if the generation fail</returns>
		public IPropertyAccessor CreatePropertyAccessor(Type targetType, string propertyName)
		{
			string key = targetType.FullName+propertyName;
			
			if (_cachedIPropertyAccessor.Contains(key))
			{
				return (IPropertyAccessor)_cachedIPropertyAccessor[key];
			}
			else
			{
				IPropertyAccessor propertyAccessor = null;
				lock (_padlock)
				{
					if (!_cachedIPropertyAccessor.Contains(key))
					{
						propertyAccessor = _createPropertyAccessor(targetType, propertyName);
						_cachedIPropertyAccessor[key] = propertyAccessor;
					}
					else
					{
						propertyAccessor = (IPropertyAccessor)_cachedIPropertyAccessor[key];
					}
				}
				return propertyAccessor;
			}
		}

		/// <summary>
		/// Generate a ILPropertyAccessor object
		/// </summary>
		/// <param name="targetType">Target object type.</param>
		/// <param name="propertyName">Property name.</param>
		/// <returns>null if the generation fail</returns>
		private IPropertyAccessor CreateILPropertyAccessor(Type targetType, string propertyName)
		{
			return  new ILPropertyAccessor(targetType, propertyName, _assemblyBuilder, _moduleBuilder);
		}

		/// <summary>
		/// Generate a ReflectionPropertyAccessor object
		/// </summary>
		/// <param name="targetType">Target object type.</param>
		/// <param name="propertyName">Property name.</param>
		/// <returns>null if the generation fail</returns>
		private IPropertyAccessor CreateReflectionPropertyAccessor(Type targetType, string propertyName)
		{
			return  new ReflectionPropertyAccessor(targetType, propertyName);
		}
	}
}
