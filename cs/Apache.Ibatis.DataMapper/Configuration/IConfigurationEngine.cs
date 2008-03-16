
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

using Apache.Ibatis.DataMapper.Configuration.Interpreters.Config;
using Apache.Ibatis.DataMapper.Configuration.Module;
using Apache.Ibatis.DataMapper.Model;

namespace Apache.Ibatis.DataMapper.Configuration
{

    /// <summary>
    /// The <see cref="IConfigurationEngine"/> defines the contract used to build
    /// an <see cref="IMapperFactory"/>.    
    /// </summary>
    public interface IConfigurationEngine
    {
        /// <summary>
        /// Gets the model store.
        /// </summary>
        /// <value>The model store.</value>
        IModelStore ModelStore { get; }

        /// <summary>
        /// Gets the configuration store.
        /// </summary>
        /// <value>The configuration store.</value>
        IConfigurationStore ConfigurationStore { get; }

        /// <summary>
        /// Builds the mapper factory.
        /// </summary>
        /// <returns>The mapper factory</returns>
        IMapperFactory BuildMapperFactory();

        /// <summary>
        /// Add a module to the <see cref="IConfigurationEngine"/>.
        /// </summary>
        /// <param name="module">The module.</param>
        void RegisterModule(IModule module);

        /// <summary>
        /// Registers an <see cref="IConfigurationInterpreter"/>.
        /// </summary>
        /// <param name="interpreter">The interpreter.</param>
        void RegisterInterpreter(IConfigurationInterpreter interpreter);
    }
}
