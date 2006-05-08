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

using IBatisNet.DataMapper.Configuration.ResultMapping;

namespace IBatisNet.DataMapper.MappedStatements.PropertyStrategy
{
	/// <summary>
	/// Factory to get <see cref="IPropertyStrategy"/> implementation.
	/// </summary>
	public class PropertyStrategyFactory
	{
		private static IPropertyStrategy _defaultStrategy = null;
		private static IPropertyStrategy _resultMapStrategy = null;

        private static IPropertyStrategy _selectArrayStrategy = null;
        private static IPropertyStrategy _selectGenericListStrategy = null;
        private static IPropertyStrategy _selectListStrategy = null;
        private static IPropertyStrategy _selectObjectStrategy = null;

		/// <summary>
		/// Initializes the <see cref="PropertyStrategyFactory"/> class.
		/// </summary>
		static PropertyStrategyFactory()
		{
			_defaultStrategy = new DefaultStrategy();
			_resultMapStrategy = new ResultMapStrategy();

            _selectArrayStrategy = new SelectArrayStrategy();
            _selectListStrategy = new SelectListStrategy();
            _selectObjectStrategy = new SelectObjectStrategy();
#if dotnet2
            _selectGenericListStrategy = new SelectGenericListStrategy();
#endif
		}

		/// <summary>
		/// Finds the <see cref="IPropertyStrategy"/>.
		/// </summary>
		/// <param name="mapping">The <see cref="ResultProperty"/>.</param>
		/// <returns>The <see cref="IPropertyStrategy"/></returns>
		public static IPropertyStrategy Get(ResultProperty mapping)
		{
			// no 'select' or 'resultMap' attributes
			if (mapping.Select.Length == 0 && mapping.NestedResultMap == null)
			{
				// We have a 'normal' ResultMap
				return _defaultStrategy;
			}
			else if (mapping.NestedResultMap != null) // 'resultMap' attribut
			{
				return _resultMapStrategy;
			}
			else //'select' ResultProperty 
			{
				return new SelectStrategy(mapping,
                    _selectArrayStrategy,
                    _selectGenericListStrategy,
                    _selectListStrategy,
                    _selectObjectStrategy);
			}
		}
	}
}
