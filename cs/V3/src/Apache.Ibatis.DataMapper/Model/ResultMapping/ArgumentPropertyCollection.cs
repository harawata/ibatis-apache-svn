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

using System.Collections.Generic;

namespace Apache.Ibatis.DataMapper.Model.ResultMapping
{
    /// <summary>
    /// An ArgumentProperty Collection.
    /// </summary>
    public class ArgumentPropertyCollection : List<ArgumentProperty>
    {
        /// <summary>
        /// Indicate if a ResultProperty is in the collection with the given property name
        /// </summary>
        /// <param name="argumentName">Name of the argument.</param>
        /// <returns>True if is in else false</returns>
        public bool Contains(string argumentName)
        {
            foreach (ArgumentProperty argumentProperty in this)
            {
                if (argumentProperty.ArgumentName == argumentName)
                {
                    return true;
                }
            }
            return false;
        }

        /// <summary>
        /// Finds an argument by his name.
        /// </summary>
        /// <param name="argumentName">Name of the argument.</param>
        /// <returns></returns>
        public ArgumentProperty FindByPropertyName(string argumentName)
        {
            ArgumentProperty argumentProperty = null;
            foreach (ArgumentProperty argument in this)
            {
                if (argument.ArgumentName == argumentName)
                {
                    argumentProperty = argument;
                    break;
                }
            }
            return argumentProperty;
        }
    }
}