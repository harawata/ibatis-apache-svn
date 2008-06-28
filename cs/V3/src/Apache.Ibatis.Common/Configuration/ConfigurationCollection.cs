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
using System.Collections.Generic;

namespace Apache.Ibatis.Common.Configuration
{
    /// <summary>
    /// A collection of <see cref="IConfiguration"/> objects.
    /// </summary>
    [Serializable]
    public class ConfigurationCollection : List<IConfiguration>
    {

        /// <summary>
        /// Gets the <see cref="IConfiguration"/> with the specified id.
        /// </summary>
        /// <value></value>
        public IConfiguration this[string id]
        {
            get
            {
                foreach(IConfiguration config in this)
                {
                    if (id.Equals(config.Id))
                    {
                        return config;
                    }
                }

                return null;
            }
        }

        /// <summary>
        /// Finds the IConfiguration element that are from the specified element type.
        /// </summary>
        /// <param name="elementType">Type of the element.</param>
        /// <returns>A list of IConfiguration</returns>
        public ConfigurationCollection Find(string elementType) 
        {
            ConfigurationCollection liste = new ConfigurationCollection();

            foreach (IConfiguration config in this)
            {
                if (elementType.Equals(config.Type))
                {
                    liste.Add( config );
                }
            }

            return liste;
        }


        /// <summary>
        /// Recursive find of the IConfiguration element that are from the specified element type.
        /// </summary>
        /// <param name="elementType">Type of the element.</param>
        /// <returns>A list of IConfiguration</returns>
        public ConfigurationCollection RecursiveFind(string elementType)
        {
            ConfigurationCollection list = new ConfigurationCollection();

            foreach (IConfiguration config in this)
            {
                if (elementType.Equals(config.Type))
                {
                    list.Add(config);
                }
                list.AddRange(config.Children.RecursiveFind(elementType));
            }

            return list;
        }


        /// <summary>
        /// Builds a new collection where element of the specified type
        /// are removed.
        /// </summary>
        /// <param name="elementType">Type of the element.</param>
        /// <returns></returns>
        public ConfigurationCollection Remove(string elementType)
        {
            ConfigurationCollection newCollection = new ConfigurationCollection();
            foreach (IConfiguration configuration in this)
            {
                if (configuration.Type != elementType)
                {
                    newCollection.Add(configuration);
                }
            }
            return newCollection;
        }
    }
}
