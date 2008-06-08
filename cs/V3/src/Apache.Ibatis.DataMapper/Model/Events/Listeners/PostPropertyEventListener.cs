#region Apache Notice
/*****************************************************************************
 * $Revision: 576082 $
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

using Apache.Ibatis.DataMapper.Model.ResultMapping;

namespace Apache.Ibatis.DataMapper.Model.Events.Listeners
{
    /// <summary>
    /// Handles event of type <see cref="PostPropertyEvent"/>.
    /// </summary>
    public abstract class PostPropertyEventListener : IResultPropertyEventListener<PostPropertyEvent>
    {

        #region IResultMapEventListener<PostPropertyEvent> Members

        /// <summary>
        /// Calls after creating an instance of the <see cref="IResultMap"/> object.
        /// </summary>
        /// <param name="evnt">The event.</param>
        /// <returns>Returns is not used</returns>
        public abstract object OnEvent(PostPropertyEvent evnt);

        #endregion
    }
}
