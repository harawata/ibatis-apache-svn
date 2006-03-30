#region Apache Notice
/*****************************************************************************
 * $Revision: 374175 $
 * $LastChangedDate: 2006-03-22 22:39:21 +0100 (mer., 22 mars 2006) $
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

namespace IBatisNet.Common.Utilities.Objects
{
	/// <summary>
	/// Create objects via Activator.CreateInstance
	/// </summary>
	public class ActivatorFactory : IFactory
	{
		private Type _typeToCreate = null;

		/// <summary>
		/// 
		/// </summary>
		/// <param name="typeToCreate"></param>
		public ActivatorFactory(Type typeToCreate)
		{
			_typeToCreate = typeToCreate;
		}

		#region IFactory members

		/// <summary>
		/// Create a new object instance via via Activator.CreateInstance
		/// </summary>
		/// <returns></returns>
		public object CreateInstance()
		{
			return Activator.CreateInstance( _typeToCreate );
		}

		#endregion
	}
}