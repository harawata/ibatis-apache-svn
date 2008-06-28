
#region Apache Notice
/*****************************************************************************
 * $Revision: 408099 $
 * $LastChangedDate$
 * $LastChangedBy$
 * 
 * iBATIS.NET Data Mapper
 * Copyright (C) 2008/2005 - The Apache Software Foundation
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
using Apache.Ibatis.DataMapper.Model.ResultMapping;

namespace Apache.Ibatis.DataMapper.Model.Events
{
    /// <summary>
    /// Lauchs before setting the property value in an instance of a <see cref="IResultMap"/> object.
    /// </summary>
    public sealed class PrePropertyEventArgs : EventArgs
    {
        private object dataBaseValue = null;
        private object target = null;

        /// <summary>
        /// Gets or sets the data base value.
        /// </summary>
        /// <value>The data base value.</value>
        public object DataBaseValue
        {
            get { return dataBaseValue; }
            set { dataBaseValue = value; }
        }

        /// <summary>
        /// Gets or sets the target.
        /// </summary>
        /// <value>The target.</value>
        public object Target
        {
            get { return target; }
            set { target = value; }
        }
    }
}
