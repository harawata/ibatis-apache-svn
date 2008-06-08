
#region Apache Notice
/*****************************************************************************
 * $Revision: 408099 $
 * $LastChangedDate: 2008-06-07 10:14:33 +0200 (sam., 07 juin 2008) $
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

using Apache.Ibatis.DataMapper.MappedStatements;

namespace Apache.Ibatis.DataMapper.Model.Events
{
    /// <summary>
    /// Defines the contract for events generated during statement execution.
    /// </summary>
    public interface ISatementEvent
    {
        /// <summary>
        /// Gets the mapped statement.
        /// </summary>
        /// <value>The mapped statement.</value>
        IMappedStatement MappedStatement { get; set;}

        /// <summary>
        /// Gets the event type.
        /// </summary>
        /// <value>The type.</value>
        StatementEventType Type { get; }

        /// <summary>
        /// Gets or sets the parameter object.
        /// </summary>
        /// <value>The parameter object.</value>
        object ParameterObject { get; set;}

    }
}
