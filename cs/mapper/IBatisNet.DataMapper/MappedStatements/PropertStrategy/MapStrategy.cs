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
#if dotnet2
using System.Collections.Generic;
#endif
using System.Data;
using IBatisNet.DataMapper.Configuration.ResultMapping;
using IBatisNet.DataMapper.MappedStatements.PropertyStrategy;
using IBatisNet.DataMapper.Scope;

namespace IBatisNet.DataMapper.MappedStatements.PropertStrategy
{
    /// <summary>
    /// 
    /// </summary>
    public class MapStrategy : IPropertyStrategy
    {
        private static IPropertyStrategy _resultMapStrategy = null;
        private static IPropertyStrategy _groupByStrategy = null;

        /// <summary>
        /// Initializes the <see cref="MapStrategy"/> class.
		/// </summary>
        static MapStrategy()
		{
		    _resultMapStrategy = new ResultMapStrategy();
		    _groupByStrategy = new GroupByStrategy();
		}
        
        #region IPropertyStrategy Members

        /// <summary>
        /// Sets value of the specified <see cref="ResultProperty"/> on the target object
        /// when a 'resultMapping' attribute exists
        /// on the <see cref="ResultProperty"/>.
        /// </summary>
        /// <param name="request">The request.</param>
        /// <param name="resultMap">The result map.</param>
        /// <param name="mapping">The ResultProperty.</param>
        /// <param name="target">The target.</param>
        /// <param name="reader">The reader.</param>
        /// <param name="keys">The keys</param>
        public void Set(RequestScope request, IResultMap resultMap, ResultProperty mapping, ref object target, IDataReader reader, object keys)
        {
#if dotnet2
            if (mapping.MemberType.IsGenericType &&
                typeof(IList<>).IsAssignableFrom(mapping.MemberType.GetGenericTypeDefinition()))
            {
                _groupByStrategy.Set(request, resultMap, mapping, ref target, reader, keys);
            }
            else
#endif
                if (typeof(IList).IsAssignableFrom(mapping.MemberType))
                {
                    _groupByStrategy.Set(request, resultMap, mapping, ref target, reader, keys);
                }
                else
                {
                    _resultMapStrategy.Set(request, resultMap, mapping, ref target, reader, keys);
                }
        }
                    
        /// <summary>
        /// Gets the value of the specified <see cref="ResultProperty"/> that must be set on the target object.
        /// </summary>
        /// <param name="request">The request.</param>
        /// <param name="resultMap">The result map.</param>
        /// <param name="mapping">The mapping.</param>
        /// <param name="reader">The reader.</param>
        public object Get(RequestScope request, IResultMap resultMap, ResultProperty mapping, ref object target, IDataReader reader)
        {
#if dotnet2
            if (mapping.MemberType.IsGenericType &&
                typeof(IList<>).IsAssignableFrom(mapping.MemberType.GetGenericTypeDefinition()))
            {
                return _groupByStrategy.Get(request, resultMap, mapping, ref target, reader);
            }
            else
#endif
                if (typeof(IList).IsAssignableFrom(mapping.MemberType))
                {
                    return _groupByStrategy.Get(request, resultMap, mapping, ref target, reader);
                }
                else
                {
                    return _resultMapStrategy.Get(request, resultMap, mapping, ref target, reader);
                }
        }

        #endregion
    }
}
