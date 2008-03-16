#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: 591621 $
 * $Date$
 * 
 * iBATIS.NET Data Mapper
 * Copyright (C) 2004 - Gilles Bayon
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

using Apache.Ibatis.Common.Resources;
using Apache.Ibatis.Common.Configuration;

namespace Apache.Ibatis.DataMapper.Configuration.Interpreters.Config
{
    /// <summary>
    /// <see cref="IConfigurationInterpreter"/> is reponsible for translating the DataMapper configuration 
    /// from what is being read to what the IConfigurationEngine expects
    /// </summary>
    public interface IConfigurationInterpreter
    {
        /// <summary>
        /// Exposes the reference to <see cref="IResource"/>
        /// which the interpreter is likely to hold
        /// </summary>
        IResource Resource { get; }

        /// <summary>
        /// Should obtain the contents from the resource,
        /// interpret it and populate the <see cref="IConfigurationStore"/>
        /// accordingly.
        /// </summary>
        /// <param name="store">The store.</param>
        void ProcessResource(IConfigurationStore store);
    }

}
