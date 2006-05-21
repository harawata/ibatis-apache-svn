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

using IBatisNet.DataMapper.Configuration.Statements;

namespace IBatisNet.DataMapper.MappedStatements.ResultStrategy
{
	/// <summary>
	/// Factory to get <see cref="IResultStrategy"/> implementation.
	/// </summary>
    public sealed class ResultStrategyFactory
    {
		private static IResultStrategy _resultClassStrategy = null;
		private static IResultStrategy _resultMapStrategy = null;
		private static IResultStrategy _objectStrategy = null;

		/// <summary>
		/// Initializes the <see cref="ResultStrategyFactory"/> class.
		/// </summary>
		static ResultStrategyFactory()
		{
			_resultMapStrategy = new ResultMapStrategy();
			_resultClassStrategy = new ResultClassStrategy();
			_objectStrategy = new ObjectStrategy();
		}

		/// <summary>
		/// Finds the <see cref="IResultStrategy"/>.
		/// </summary>
		/// <param name="statement">The statement.</param>
		/// <returns>The <see cref="IResultStrategy"/></returns>
		public static IResultStrategy Get(IStatement statement)
		{
			// If there's an ResultMap, use it
			if (statement.ResultMap != null) 
			{
				return _resultMapStrategy;
			} 
			else // else try to use a ResultClass
			{
				if (statement.ResultClass != null) 
				{
					return _resultClassStrategy;
				}
				else
				{
					return _objectStrategy;
				}
			}
		}
    }
}
