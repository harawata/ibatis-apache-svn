#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: 475842 $
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

#region Using

using Apache.Ibatis.Common.Configuration;
using Apache.Ibatis.DataMapper.Model;
using Apache.Ibatis.DataMapper.Model.Statements;
using Apache.Ibatis.DataMapper.Scope;
using Apache.Ibatis.DataMapper.Configuration.Interpreters.Config;

#endregion 

namespace Apache.Ibatis.DataMapper.Configuration.Serializers
{
	/// <summary>
	/// Summary description for ProcedureDeSerializer.
	/// </summary>
    public sealed class ProcedureDeSerializer : BaseStatementDeSerializer
	{
        /// <summary>
        /// Deserializes the specified configuration in a <see cref="Procedure"/> object.
        /// </summary>
        /// <param name="modelStore">The model store.</param>
        /// <param name="config">The config.</param>
        /// <returns></returns>
        public override IStatement Deserialize(IModelStore modelStore, IConfiguration config)
        {
            BaseDeserialize(modelStore, config);

            if (parameterMap == null)
            {
                parameterMap = modelStore.GetParameterMap(ConfigConstants.EMPTY_PARAMETER_MAP);
            }

            return new Procedure(
                id,
                null,
                parameterMap,
                resultClass,
                resultsMap,
                listClass,
                listClassFactory,
                cacheModel,
                remapResults,
                string.Empty);
        }
	}
}