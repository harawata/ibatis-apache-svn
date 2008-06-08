
#region Apache Notice
/*****************************************************************************
 * $Revision: 408099 $
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

namespace Apache.Ibatis.DataMapper.Model.Events.Listeners
{
    /// <summary>
    /// Handles event of type <see cref="IResultPropertyEvent"/>.
    /// </summary>
    public interface IResultPropertyEventListener<IResultPropertyEvent>
    {
        /// <summary>
        /// Calls on the specified event.
        /// </summary>
        /// <param name="evnt">The event.</param>
        /// <returns></returns>
        object OnEvent(IResultPropertyEvent evnt);
    }
}
