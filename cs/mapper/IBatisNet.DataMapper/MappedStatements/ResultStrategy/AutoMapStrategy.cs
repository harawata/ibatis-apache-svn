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
using System.Reflection;
using IBatisNet.Common.Logging;
using IBatisNet.DataMapper.Scope;

namespace IBatisNet.DataMapper.MappedStatements.ResultStrategy
{
	/// <summary>
	/// <see cref="IResultStrategy"/> implementation used when implicit 'ResultMap'.
	/// </summary>
    public sealed class AutoMapStrategy : IResultStrategy
    {
		private static readonly ILog _logger = LogManager.GetLogger( MethodBase.GetCurrentMethod().DeclaringType );

		/// <summary>
		/// Auto-map the reader to the result object.
		/// </summary>
		/// <param name="request">The request.</param>
		/// <param name="reader">The reader.</param>
		/// <param name="resultObject">The result object.</param>
		private void AutoMapReader(RequestScope request, ref IDataReader reader,ref object resultObject) 
		{
			if (request.Statement.RemapResults)
			{
				ReaderAutoMapper readerAutoMapper = new ReaderAutoMapper(request.DataExchangeFactory,
					reader, 
					ref resultObject);
				readerAutoMapper.AutoMapReader( reader, ref resultObject );
				_logger.Debug("The RemapResults");
			}
			else
			{
				if (request.MappedStatement.ReaderAutoMapper == null)
				{
					lock (request.MappedStatement) 
					{
						if (request.MappedStatement.ReaderAutoMapper == null) 
						{
							request.MappedStatement.ReaderAutoMapper = new ReaderAutoMapper(
								request.DataExchangeFactory,
								reader, 
								ref resultObject);
						}
					}
				}
				_logger.Debug("The AutoMapReader");
				request.MappedStatement.ReaderAutoMapper.AutoMapReader( reader, ref resultObject );				
			}

		}


        #region IResultStrategy Members

        /// <summary>
        /// Processes the specified <see cref="IDataReader"/> 
        /// when ..XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.
        /// </summary>
        /// <param name="request">The request.</param>
        /// <param name="reader">The reader.</param>
        /// <param name="resultObject">The result object.</param>
        public object Process(RequestScope request, ref IDataReader reader, object resultObject)
        {
			object outObject = resultObject; 

			if (outObject == null) 
			{
				outObject = request.Statement.CreateInstanceOfResultClass();
			}

            AutoMapReader(request, ref reader, ref outObject);

			return outObject;
        }

        #endregion
    }
}
