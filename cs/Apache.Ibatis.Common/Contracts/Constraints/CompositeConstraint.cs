﻿
#region Apache Notice
/*****************************************************************************
 * $Revision: 387044 $
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

namespace Apache.Ibatis.Common.Contracts
{
    /// <summary>
    /// Abstract base class for Composite constraints 
    /// </summary>
    public abstract class CompositeConstraint : BaseConstraint
    {
        protected readonly BaseConstraint leftSide = null;
        protected readonly BaseConstraint rightSide = null;

        /// <summary>
        /// Initializes a new instance of the <see cref="CompositeConstraint"/> class.
        /// </summary>
        /// <param name="left">The left side.</param>
        /// <param name="right">The right side.</param>
        public CompositeConstraint(
            BaseConstraint left, 
            BaseConstraint right)
        {
            leftSide = left;
            rightSide = right;
        }

    }
}
