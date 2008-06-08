#region Apache Notice
/*****************************************************************************
 * $Revision: 476843 $
 * $LastChangedDate: 2008-05-31 15:52:05 +0200 (sam., 31 mai 2008) $
 * $LastChangedBy: gbayon $
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

using Apache.Ibatis.DataMapper.MappedStatements;

namespace Apache.Ibatis.DataMapper.Model.Events
{
    /// <summary>
    /// Base class for <see cref="ISatementEvent"/>
    /// </summary>
    public abstract class BaseSatementEvent : ISatementEvent
    {
        private IMappedStatement mappedStatement = null;
        private object parameterObject = null;


        #region ISatementEvent Members

        /// <summary>
        /// Gets or sets the parameter object.
        /// </summary>
        /// <value>The parameter object.</value>
        public object ParameterObject
        {
            get { return parameterObject; }
            set { parameterObject = value; }
        }

        /// <summary>
        /// Gets the mapped statement.
        /// </summary>
        /// <value>The mapped statement.</value>
        public IMappedStatement MappedStatement
        {
            get { return mappedStatement; }
            set { mappedStatement = value; }
        }


        /// <summary>
        /// Gets the event type.
        /// </summary>
        /// <value>The type.</value>
        public abstract StatementEventType Type { get; }


        #endregion
    }
}
