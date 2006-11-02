#region Apache Notice
/*****************************************************************************
 * $Revision: 374175 $
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

using System;
using System.Collections;
#if dotnet2
using System.Collections.Generic;
#endif
using System.Data;
using System.Text;
using IBatisNet.Common.Utilities.Objects;
using IBatisNet.Common.Utilities.Objects.Members;
using IBatisNet.DataMapper.Configuration.ResultMapping;
using IBatisNet.DataMapper.Scope;

namespace IBatisNet.DataMapper.MappedStatements.ResultStrategy
{
    /// <summary>
    /// <see cref="IResultStrategy"/> implementation when 
    /// a 'groupBy' attribute is specified on the resultMap tag.
    /// </summary>
    public sealed class GroupByStrategy : BaseStrategy, IResultStrategy
    {
        private const string KEY_SEPARATOR = "\002";

        // il fait des pushRequest, popRequest à voir quoi cela sert
        // il faudrait faire un push de l currentKey au début de process ? 
        // un pop à la fin de process  ?
        private string GetUniqueKey(IResultMap resultMap, RequestScope request, IDataReader reader)
        {
            if (resultMap.GroupByProperties.Count > 0)
            {
                StringBuilder keyBuffer = new StringBuilder();

                for (int i = 0; i < resultMap.Properties.Count; i++)
                {
                    ResultProperty resultProperty = resultMap.Properties[i];
                    if (resultMap.GroupByProperties.Contains(resultProperty.PropertyName))
                    {
                        // on peut surement utiliser resultProperty.GetDataBaseValue
                        keyBuffer.Append(resultProperty.PropertyStrategy.Get(request, resultMap, resultProperty, reader));
                        keyBuffer.Append('-');
                    }
                }

                if (keyBuffer.Length < 1)
                {
                    return null;
                }
                else
                {
                    // separator value not likely to appear in a database
                    keyBuffer.Append(KEY_SEPARATOR);
                    return keyBuffer.ToString();
                }
            }
            else
            {
                return null;
            }
        }

        #region IResultStrategy Members

        /// <summary>
        /// Processes the specified <see cref="IDataReader"/>.
        /// </summary>
        /// <param name="request">The request.</param>
        /// <param name="reader">The reader.</param>
        /// <param name="resultObject">The result object.</param>
        /// <returns>The result object</returns>
        public object Process(RequestScope request, ref IDataReader reader, object resultObject)
        {
            object outObject = resultObject;

            IResultMap resultMap = request.CurrentResultMap.ResolveSubMap(reader);

            string uniqueKey = GetUniqueKey(resultMap, request, reader);
            // Gets the [key, result object] already build
            IDictionary buildObjects = request.GetUniqueKeys(resultMap);

            if (buildObjects != null && buildObjects.Contains(uniqueKey))
            {
                // Unique key is already known, so get the existing result object and process additional results.
                outObject = buildObjects[uniqueKey];
                // process additional property with resulMapping attribute
                for (int index = 0; index < resultMap.Properties.Count; index++)
                {
                    ResultProperty resultProperty = resultMap.Properties[index];
                    if (resultProperty.PropertyStrategy is PropertyStrategy.ResultMapStrategy)
                    {
                        // the ResultProperty is an IList implementation 
                        if (typeof(IList).IsAssignableFrom(resultProperty.SetAccessor.MemberType))
                        {
                            // appel PropertyStrategy.ResultMapStrategy.Get
                            object result = resultProperty.PropertyStrategy.Get(request, resultMap, resultProperty, reader);
                            IList list = (IList)ObjectProbe.GetMemberValue(outObject, resultProperty.PropertyName,
                                                       request.DataExchangeFactory.AccessorFactory);
                            list.Add(result);
                        }
                        else
                        {
                            resultProperty.PropertyStrategy.Set(request, resultMap, resultProperty, ref outObject, reader, null);
                        }
                    }
                }
                outObject = RequestScope.SKIP;
            }
            else if (uniqueKey == null || buildObjects == null || !buildObjects.Contains(uniqueKey))
            {
                // Unique key is NOT known, so create a new result object and process additional results.

                // temp ?, we don't support constructor tag with groupBy attribute
                outObject = resultMap.CreateInstanceOfResult(null);

                for (int index = 0; index < resultMap.Properties.Count; index++)
                {
                    ResultProperty resultProperty = resultMap.Properties[index];
#if dotnet2
                    if (resultProperty.MemberType.IsGenericType &&
                        typeof(IList<>).IsAssignableFrom(resultProperty.MemberType.GetGenericTypeDefinition()))
                    {
                        object result = resultProperty.PropertyStrategy.Get(request, resultMap, resultProperty, reader);
                        object property = ObjectProbe.GetMemberValue(outObject, resultProperty.PropertyName,
                                                   request.DataExchangeFactory.AccessorFactory);
                        if (property == null)// Create the list
                        {
                            IFactory factory = request.DataExchangeFactory.ObjectFactory.CreateFactory(resultProperty.MemberType,
                                                                                                       Type.EmptyTypes);
                            property = factory.CreateInstance(Type.EmptyTypes);
                            resultProperty.SetAccessor.Set(outObject, property);
                        }

                        IList list = (IList)property;
                        list.Add(result);
                    }
                    else
#endif
                        if (typeof(IList).IsAssignableFrom(resultProperty.MemberType))
                        {
                            object result = resultProperty.PropertyStrategy.Get(request, resultMap, resultProperty, reader);
                            object property = ObjectProbe.GetMemberValue(outObject, resultProperty.PropertyName,
                                                       request.DataExchangeFactory.AccessorFactory);
                            if (property == null)// Create the list
                            {
                                if (resultProperty.MemberType == typeof(IList))
                                {
                                    property = new ArrayList(); 
                                }
                                else // custom collection
                                {
                                    IFactory factory = request.DataExchangeFactory.ObjectFactory.CreateFactory(resultProperty.MemberType,
                                                                             Type.EmptyTypes);
                                    property = factory.CreateInstance(Type.EmptyTypes);
                                }
                                resultProperty.SetAccessor.Set(outObject, property);
                            }

                            IList list = (IList)property;
                            list.Add(result);
                        }
                        else
                        {
                            resultProperty.PropertyStrategy.Set(request, resultMap, resultProperty, ref outObject, reader, null);
                        }
                }

                if (buildObjects == null)
                {
                    buildObjects = new Hashtable();
                    request.SetUniqueKeys(resultMap, buildObjects);
                }
                buildObjects[uniqueKey] = outObject;
            }

            return outObject;
        }

        #endregion
    }
}
