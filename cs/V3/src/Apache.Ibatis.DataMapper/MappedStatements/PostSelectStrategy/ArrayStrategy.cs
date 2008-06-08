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
using Apache.Ibatis.DataMapper.Scope;

namespace Apache.Ibatis.DataMapper.MappedStatements.PostSelectStrategy
{
	/// <summary>
	/// <see cref="IPostSelectStrategy"/> implementation to exceute a query for <see cref="Array"/>.
	/// </summary>
    public sealed class ArrayStrategy : IPostSelectStrategy
	{
		#region IPostSelectStrategy Members

		/// <summary>
		/// Executes the specified <see cref="PostBindind"/>.
		/// </summary>
		/// <param name="postSelect">The <see cref="PostBindind"/>.</param>
		/// <param name="request">The <see cref="RequestScope"/></param>
		public void Execute(PostBindind postSelect, RequestScope request)
		{
			IList values = postSelect.Statement.ExecuteQueryForList(request.Session, postSelect.Keys);
            Type elementType = postSelect.ResultProperty.SetAccessor.MemberType.GetElementType();

			Array array = Array.CreateInstance(elementType, values.Count);
			int count = values.Count;
			for(int i=0;i<count;i++)
			{
				array.SetValue(values[i],i);
			}

            postSelect.ResultProperty.Set(postSelect.Target, array);
		}

		#endregion
	}
}
