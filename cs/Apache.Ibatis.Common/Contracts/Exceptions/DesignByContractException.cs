#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: 383115 $
 * $Date$
 * 
 * iBATIS.NET Data Mapper
 * Copyright (C) 2006 - Apache Fondation
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

namespace Apache.Ibatis.Common.Contracts.Exceptions
{
    /// <summary>
    /// Base class for all exception raise when a contract is broken
    /// </summary>
    public class DesignByContractException: ApplicationException
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="DesignByContractException"/> class.
        /// </summary>
        protected DesignByContractException()
        {}
        
        /// <summary>
        /// Initializes a new instance of the <see cref="DesignByContractException"/> class.
        /// </summary>
        /// <param name="message">The message.</param>
        protected DesignByContractException(string message): base(message)
        {}

        /// <summary>
        /// Initializes a new instance of the <see cref="DesignByContractException"/> class.
        /// </summary>
        /// <param name="message">The message.</param>
        /// <param name="inner">The inner.</param>
        protected DesignByContractException(string message, Exception inner): base(message, inner)
        {}
    }
}
