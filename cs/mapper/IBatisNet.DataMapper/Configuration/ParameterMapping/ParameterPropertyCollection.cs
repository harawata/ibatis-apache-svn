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

using System.Collections;

namespace IBatisNet.DataMapper.Configuration.ParameterMapping
{
	/// <summary>
	/// A ParameterProperty Collection.
	/// </summary>
	public class ParameterPropertyCollection: CollectionBase 
	{
		/// <summary>
		/// Constructeur
		/// </summary>
		public ParameterPropertyCollection() {}


		/// <summary>
		/// Acces element in collection by index
		/// </summary>
		public ParameterProperty this[int index] 
		{
			get	{ return (ParameterProperty)List[index]; }
			set { List[index] = value; }
		}

		/// <summary>
		/// Add an ParameterProperty
		/// </summary>
		/// <param name="value"></param>
		public int Add(ParameterProperty value) 
		{
			return List.Add(value);
		}

		/// <summary>
		/// Add a list of ParameterProperty to the collection
		/// </summary>
		/// <param name="value"></param>
		public void AddRange(ParameterProperty[] value) 
		{
			for (int i = 0;	i < value.Length; i++) 
			{
				Add(value[i]);
			}
		}

		/// <summary>
		/// Add a list of ParameterProperty to the collection
		/// </summary>
		/// <param name="value"></param>
		public void AddRange(ParameterPropertyCollection value) 
		{
			for (int i = 0;	i < value.Count; i++) 
			{
				Add(value[i]);
			}
		}

		/// <summary>
		/// Indicate if a ParameterProperty is in the collection
		/// </summary>
		/// <param name="value">Un(e) ParameterProperty</param>
		/// <returns>Renvoir vrai s'il/elle appartinet à la collection</returns>
		public bool Contains(ParameterProperty value) 
		{
			return List.Contains(value);
		}


		/// <summary>
		/// Copy a collection in a ParameterProperty array
		/// </summary>
		/// <param name="array">A ParameterProperty array</param>
		/// <param name="index">Start index of the copy</param>
		public void CopyTo(ParameterProperty[] array, int index) 
		{
			List.CopyTo(array, index);
		}

		/// <summary>
		/// Position of the ParameterProperty in the collection.
		/// </summary>
		/// <param name="value">A ParameterProperty</param>
		/// <returns>Index found.</returns>
		public int IndexOf(ParameterProperty value) 
		{
			return List.IndexOf(value);
		}
		
		/// <summary>
		/// Insert a ParameterProperty in the collection.
		/// </summary>
		/// <param name="index">Index where to insert.</param>
		/// <param name="value">A ParameterProperty</param>
		public void Insert(int index, ParameterProperty value) 
		{
			List.Insert(index, value);
		}
		
		/// <summary>
		/// Remove a ParameterProperty of the collection.
		/// </summary>
		public void Remove(ParameterProperty value) 
		{
			List.Remove(value);
		}
	}
}

