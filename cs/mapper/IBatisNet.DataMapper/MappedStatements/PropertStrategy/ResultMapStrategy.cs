#region Apache Notice
/*****************************************************************************
 * $Revision: 374175 $
 * $LastChangedDate: 2006-04-25 19:40:27 +0200 (mar., 25 avr. 2006) $
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

using System.Data;
using IBatisNet.DataMapper.Configuration.ResultMapping;
using IBatisNet.DataMapper.Scope;

namespace IBatisNet.DataMapper.MappedStatements.PropertyStrategy
{
	/// <summary>
	/// <see cref="IPropertyStrategy"/> implementation when a 'resultMapping' attribute exists
	/// on a <see cref="ResultProperty"/>.
	/// </summary>
    public sealed class ResultMapStrategy : BaseStrategy, IPropertyStrategy
	{
		#region IPropertyStrategy Members

		/// <summary>
		/// Sets value of the specified <see cref="ResultProperty"/> on the target object
		/// when a 'resultMapping' attribute exists
		/// on the <see cref="ResultProperty"/> is not empty.
		/// </summary>
		/// <param name="request">The request.</param>
		/// <param name="resultMap">The result map.</param>
		/// <param name="mapping">The ResultProperty.</param>
		/// <param name="target">The target.</param>
		/// <param name="reader">The reader.</param>
		/// <param name="keys">The keys</param>
		public void Set(RequestScope request, ResultMap resultMap, 
			ResultProperty mapping, ref object target, IDataReader reader, object keys)
		{
			// Creates object
			object[] parameters = null;
			if (mapping.NestedResultMap.Parameters.Count >0)
			{
				parameters = new object[resultMap.Parameters.Count];
				// Fill parameters array
				for(int index=0; index< mapping.NestedResultMap.Parameters.Count; index++)
				{
					ResultProperty resultProperty = mapping.NestedResultMap.Parameters[index];
					parameters[index] = resultProperty.ArgumentStrategy.GetValue(request, resultMap, resultProperty, ref reader, null);
					request.IsRowDataFound = request.IsRowDataFound || (parameters[index] != null);
				}
			}

			object obj = mapping.NestedResultMap.CreateInstanceOfResult(parameters);
			
			// Fills properties on the new object
			if (this.FillObjectWithReaderAndResultMap(request, reader, mapping.NestedResultMap, obj) == false)
			{
				obj = null;
			}

			// Sets created object on the property
			resultMap.SetValueOfProperty( ref target, mapping, obj );		}

		#endregion
	}
}
