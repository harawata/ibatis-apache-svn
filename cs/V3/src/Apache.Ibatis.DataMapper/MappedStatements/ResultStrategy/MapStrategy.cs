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

using System.Data;
using Apache.Ibatis.DataMapper.Model.ResultMapping;
using Apache.Ibatis.DataMapper.Scope;

namespace Apache.Ibatis.DataMapper.MappedStatements.ResultStrategy
{
    /// <summary>
    /// Delegates on the <see cref="ResultMapStrategy"/>  or on the 
    /// <see cref="GroupByStrategy"/> implementation if a grouBy attribute is specify on the resultMap tag.
    /// </summary>
    public sealed class MapStrategy : IResultStrategy
    {
        private static readonly IResultStrategy resultMapStrategy = null;
        private static readonly IResultStrategy groupByStrategy = null;
        private static readonly IResultStrategy cirularStrategy = null;
        private static readonly IResultStrategy dataTableStrategy = null;

        /// <summary>
        /// Initializes the <see cref="MapStrategy"/> class.
        /// </summary>
        static MapStrategy()
        {
            resultMapStrategy = new ResultMapStrategy();
            groupByStrategy = new GroupByStrategy();
            cirularStrategy = new CirularStrategy();
            dataTableStrategy = new DataRowStrategy();
        }
        
        #region IResultStrategy Members

        /// <summary>
        /// Processes the specified <see cref="IDataReader"/>.
        /// </summary>
        /// <param name="request">The request.</param>
        /// <param name="reader">The reader.</param>
        /// <param name="resultObject">The result object.</param>
        public object Process(RequestScope request, ref IDataReader reader, object resultObject)
        {
            IResultMap resultMap = request.CurrentResultMap.ResolveSubMap(reader);

            if (resultMap.GroupByPropertyNames.Count>0)
            {
                return groupByStrategy.Process(request, ref reader, resultObject);
            }
            else if (resultMap.KeyPropertyNames.Count > 0)
            {
                return cirularStrategy.Process(request, ref reader, resultObject);
            }
            else if (typeof(DataRow).IsAssignableFrom(resultMap.Class))
            {
                return dataTableStrategy.Process(request, ref reader, resultObject);
            }
            else
            {
                return resultMapStrategy.Process(request, ref reader, resultObject);
            }
        }

        #endregion
    }
}
