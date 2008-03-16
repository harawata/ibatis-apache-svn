
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

using Apache.Ibatis.Common.Contracts;

namespace Apache.Ibatis.DataMapper.Configuration.Module
{
    /// <summary>
    /// Base class for code configuration modules. 
    /// </summary>
    public abstract class Module : ModuleBuilder, IModule
    {
        public Module()
        {
        }

        public Module(string nameSpace)
            : base(nameSpace)
        {
        }

        /// <summary>
        /// Apply the module to the container.
        /// </summary>
        /// <param name="engine">The engine.</param>
        public void Configure(IConfigurationEngine engine)
        {
            Contract.Require.That(engine, Is.Not.Null).When("retrieving argument engine in Configure method");

            Load();
            Build(engine.ConfigurationStore);
        }

        /// <summary>
        /// Override to add code configuration mapping to the <see cref="IConfigurationEngine"/>.
        /// </summary>
        public abstract void Load();
    }
}
