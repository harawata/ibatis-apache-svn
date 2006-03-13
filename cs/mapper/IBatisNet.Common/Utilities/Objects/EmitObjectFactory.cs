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
using System.Collections;
using System.Collections.Specialized;

namespace IBatisNet.Common.Utilities.Objects
{
	/// <summary>
	/// A factory that can create objects via IL code
	/// </summary>
	public sealed class EmitObjectFactory : IObjectFactory
	{
		private IDictionary _cachedfactories = new HybridDictionary();
		private FactoryBuilder _factoryBuilder = null;
		private object _padlock = new object();


		/// <summary>
		/// 
		/// </summary>
		public EmitObjectFactory()
		{
			_factoryBuilder = new FactoryBuilder();
		}

		#region IObjectFactory members

		/// <summary>
		/// Create a new factory instance for a given type
		/// </summary>
		/// <param name="typeToCreate"></param>
		/// <returns></returns>
		public IFactory CreateFactory(Type typeToCreate)
		{
			IFactory factory = (IFactory) _cachedfactories[typeToCreate];
			if (factory == null)
			{
				lock (_padlock)
				{
					factory = (IFactory) _cachedfactories[typeToCreate];
					if (factory == null) // double-check
					{
						factory = _factoryBuilder.CreateFactory(typeToCreate);
						_cachedfactories[typeToCreate] = factory;
					}
				}
			}
			return factory;
		}

		#endregion
	}
}
