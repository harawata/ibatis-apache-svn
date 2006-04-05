
#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision$
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

using System;
using System.Collections;
using System.Reflection;

using IBatisNet.Common.Exceptions;
using IBatisNet.Common.Utilities.Objects.Members;

namespace IBatisNet.Common.Utilities.Objects
{
	/// <summary>
	/// Description résumée de ObjectProbe.
	/// </summary>
	public class ObjectProbe
	{
		private static ArrayList _simpleTypeMap = new ArrayList();

		static ObjectProbe() 
		{
			_simpleTypeMap.Add(typeof(string));
			_simpleTypeMap.Add(typeof(Byte));
			_simpleTypeMap.Add(typeof(Int16));
			_simpleTypeMap.Add(typeof(char));
			_simpleTypeMap.Add(typeof(Int32));
			_simpleTypeMap.Add(typeof(Int64));
			_simpleTypeMap.Add(typeof(Single));
			_simpleTypeMap.Add(typeof(Double));
			_simpleTypeMap.Add(typeof(Boolean));
			_simpleTypeMap.Add(typeof(DateTime));
			_simpleTypeMap.Add(typeof(Decimal));

			//			_simpleTypeMap.Add(typeof(Hashtable));
			//			_simpleTypeMap.Add(typeof(SortedList));
			//			_simpleTypeMap.Add(typeof(ArrayList));
			//			_simpleTypeMap.Add(typeof(Array));

			//			simpleTypeMap.Add(LinkedList.class);
			//			simpleTypeMap.Add(HashSet.class);
			//			simpleTypeMap.Add(TreeSet.class);
			//			simpleTypeMap.Add(Vector.class);
			//			simpleTypeMap.Add(Hashtable.class);
			_simpleTypeMap.Add(typeof(SByte));
			_simpleTypeMap.Add(typeof(UInt16));
			_simpleTypeMap.Add(typeof(UInt32));
			_simpleTypeMap.Add(typeof(UInt64));
			_simpleTypeMap.Add(typeof(IEnumerator));
		}


		/// <summary>
		/// Returns an array of the readable properties names exposed by an object
		/// </summary>
		/// <param name="obj">The object</param>
		/// <returns>The properties name</returns>
		public static string[] GetReadablePropertyNames(object obj) 
		{
			return ReflectionInfo.GetInstance(obj.GetType()).GetReadableMemberNames();
		}

	
		/// <summary>
		/// Returns an array of the writeable members name exposed by a object
		/// </summary>
		/// <param name="obj">The object</param>
		/// <returns>The members name</returns>
		public static string[] GetWriteableMemberNames(object obj) 
		{
			return ReflectionInfo.GetInstance(obj.GetType()).GetWriteableMemberNames();
		}


		/// <summary>
		///  Returns the type that the set expects to receive as a parameter when
		///  setting a member value.
		/// </summary>
		/// <param name="obj">The object to check</param>
		/// <param name="memberName">The name of the member</param>
		/// <returns>The type of the member</returns>
		public static Type GetMemberTypeForSetter(object obj, string memberName) 
		{
			Type type = obj.GetType();

			if (obj is IDictionary) 
			{
				IDictionary map = (IDictionary) obj;
				object value = map[memberName];
				if (value == null) 
				{
					type = typeof(object);
				} 
				else 
				{
					type = value.GetType();
				}
			} 
			else 
			{
				if (memberName.IndexOf('.') > -1) 
				{
					StringTokenizer parser = new StringTokenizer(memberName, ".");
					IEnumerator enumerator = parser.GetEnumerator();

					while (enumerator.MoveNext()) 
					{
						memberName = (string)enumerator.Current;
						type = ReflectionInfo.GetInstance(type).GetSetterType(memberName);
					}
				} 
				else 
				{
					type = ReflectionInfo.GetInstance(type).GetSetterType(memberName);
				}
			}

			return type;
		}


		/// <summary>
		///  Returns the type that the get expects to receive as a parameter when
		///  setting a member value.
		/// </summary>
		/// <param name="obj">The object to check</param>
		/// <param name="memberName">The name of the member</param>
		/// <returns>The type of the member</returns>
		public static Type GetMemberTypeForGetter(object obj, string memberName) 
		{
			Type type = obj.GetType();

			if (obj is IDictionary) 
			{
				IDictionary map = (IDictionary) obj;
				object value = map[memberName];
				if (value == null) 
				{
					type = typeof(object);
				} 
				else 
				{
					type = value.GetType();
				}
			} 
			else 
			{
				if (memberName.IndexOf('.') > -1) 
				{
					StringTokenizer parser = new StringTokenizer(memberName, ".");
					IEnumerator enumerator = parser.GetEnumerator();

					while (enumerator.MoveNext()) 
					{
						memberName = (string)enumerator.Current;
						type = ReflectionInfo.GetInstance(type).GetGetterType(memberName);
					}
				} 
				else 
				{
					type = ReflectionInfo.GetInstance(type).GetGetterType(memberName);
				}
			}

			return type;
		}


		/// <summary>
		///  Returns the type that the get expects to receive as a parameter when
		///  setting a member value.
		/// </summary>
		/// <param name="type">The type to check</param>
		/// <param name="memberName">The name of the member</param>
		/// <returns>The type of the member</returns>
		public static Type GetMemberTypeForGetter(Type type, string memberName) 
		{
			if (memberName.IndexOf('.') > -1) 
			{
				StringTokenizer parser = new StringTokenizer(memberName, ".");
				IEnumerator enumerator = parser.GetEnumerator();

				while (enumerator.MoveNext()) 
				{
					memberName = (string)enumerator.Current;
					type = ReflectionInfo.GetInstance(type).GetGetterType(memberName);
				}
			} 
			else 
			{
				type = ReflectionInfo.GetInstance(type).GetGetterType(memberName);
			}

			return type;
		}


		/// <summary>
		///  Returns the MemberInfo of the set member on the specified type.
		/// </summary>
		/// <param name="type">The type to check</param>
		/// <param name="memberName">The name of the member</param>
		/// <returns>The type of the member</returns>
		public static MemberInfo GetMemberInfoForSetter(Type type, string memberName) 
		{
			MemberInfo memberInfo =null;
			if (memberName.IndexOf('.') > -1) 
			{
				StringTokenizer parser = new StringTokenizer(memberName, ".");
				IEnumerator enumerator = parser.GetEnumerator();
				Type parentType = null;

				while (enumerator.MoveNext()) 
				{
					memberName = (string)enumerator.Current;
					parentType = type;
					type = ReflectionInfo.GetInstance(type).GetSetterType(memberName);
				}
				memberInfo = ReflectionInfo.GetInstance(parentType).GetSetter(memberName);
			} 
			else 
			{
				memberInfo = ReflectionInfo.GetInstance(type).GetSetter(memberName);
			}

			return memberInfo;
		}


		private static object GetArrayMember(object obj, string indexedName, IMemberAccessorFactory memberAccessorFactory)
		{
			object value = null;

			try 
			{
				int startIndex  = indexedName.IndexOf("[");
				int length = indexedName.IndexOf("]");
				string name = indexedName.Substring(0, startIndex);
				string index = indexedName.Substring( startIndex+1, length-(startIndex+1));
				int i = System.Convert.ToInt32(index);
				
				if (name.Length > 0)
				{
					value = GetMember(obj, name, memberAccessorFactory);
				}
				else
				{
					value = obj;
				}

				if (value is IList) 
				{
					value = ((IList) value)[i];
				} 
				else 
				{
					throw new ProbeException("The '" + name + "' member of the " + obj.GetType().Name + " class is not a List or Array.");
				}
			}
			catch (ProbeException pe) 
			{
				throw pe;
			} 
			catch(Exception e)
			{		
				throw new ProbeException("Error getting ordinal value from .net object. Cause" + e.Message, e);
			}

			return value;
		}




		private static void SetArrayMember(object obj, string indexedName, object value,
			IMemberAccessorFactory memberAccessorFactory)
		{
			try 
			{
				int startIndex  = indexedName.IndexOf("[");
				int length = indexedName.IndexOf("]");
				string name = indexedName.Substring(0, startIndex);
				string index = indexedName.Substring( startIndex+1, length-(startIndex+1));
				int i = System.Convert.ToInt32(index);
				
				object list = null;
				if (name.Length > 0)
				{
					list = GetMember(obj, name, memberAccessorFactory);
				}
				else
				{
					list = obj;
				}

				if (list is IList) 
				{
					((IList) list)[i] = value;
				} 
				else 
				{
					throw new ProbeException("The '" + name + "' member of the " + obj.GetType().Name + " class is not a List or Array.");
				}
			} 
			catch (ProbeException pe) 
			{
				throw pe;
			} 
			catch (Exception e) 
			{
				throw new ProbeException("Error getting ordinal value from .net object. Cause" + e.Message, e);
			}
		}


		/// <summary>
		/// Return the specified member on an object. 
		/// </summary>
		/// <param name="obj">The Object on which to invoke the specified property.</param>
		/// <param name="memberName">The name of the property.</param>
		/// <param name="memberAccessorFactory"></param>
		/// <returns>An Object representing the return value of the invoked property.</returns>
		public static object GetMemberValue(object obj, string memberName,
			IMemberAccessorFactory memberAccessorFactory)
		{
			if (memberName.IndexOf('.') > -1) 
			{
				StringTokenizer parser = new StringTokenizer(memberName, ".");
				IEnumerator enumerator = parser.GetEnumerator();
				object value = obj;
				string token = null;

				while (enumerator.MoveNext()) 
				{
					token = (string)enumerator.Current;
					value = GetMember(value, token, memberAccessorFactory);

					if (value == null) 
					{
						break;
					}
				}
				return value;
			} 
			else 
			{
				return GetMember(obj, memberName, memberAccessorFactory);
			}
		}


		/// <summary>
		/// 
		/// </summary>
		/// <param name="obj"></param>
		/// <param name="memberName"></param>
		/// <param name="memberAccessorFactory"></param>
		/// <returns></returns>
		protected static object GetMember(object obj, string memberName,
			IMemberAccessorFactory memberAccessorFactory)
		{
			try 
			{
				object value = null;

				if (memberName.IndexOf("[") > -1) 
				{
					value = GetArrayMember(obj, memberName, memberAccessorFactory);
				} 
				else 
				{
					if (obj is IDictionary) 
					{
						value = ((IDictionary) obj)[memberName];
					} 
					else 
					{
						Type targetType = obj.GetType();
						IMemberAccessor memberAccessor = memberAccessorFactory.CreateMemberAccessor(targetType, memberName);

						if (memberAccessor == null) 
						{
							throw new ProbeException("No Get method for member " + memberName + " on instance of " + obj.GetType().Name);
						}
						try 
						{
							value = memberAccessor.Get(obj);
						} 
						catch (Exception ae) 
						{
							throw new ProbeException(ae);
						}					
					}
				}
				return value;
			} 
			catch (ProbeException pe) 
			{
				throw pe;
			} 
			catch(Exception e)
			{
				throw new ProbeException("Could not Set property '" + memberName + "' for " + obj.GetType().Name + ".  Cause: " + e.Message, e);
			}
		}


		/// <summary>
		/// Set the specified member on an object 
		/// </summary>
		/// <param name="obj">The Object on which to invoke the specified property.</param>
		/// <param name="memberName">The name of the property to set.</param>
		/// <param name="memberValue">The new value to set.</param>
		/// <param name="memberAccessorFactory"></param>
		/// <param name="objectFactory"></param>
		public static void SetMemberValue(object obj, string memberName, object memberValue,
			IObjectFactory objectFactory,
			IMemberAccessorFactory memberAccessorFactory)
		{
			if (memberName.IndexOf('.') > -1) 
			{
				StringTokenizer parser = new StringTokenizer(memberName, ".");
				IEnumerator enumerator = parser.GetEnumerator();
				enumerator.MoveNext();

				string currentPropertyName = (string)enumerator.Current;
				object child = obj;
      
				while (enumerator.MoveNext()) 
				{
					Type type = GetMemberTypeForSetter(child, currentPropertyName);
					object parent = child;
					child = GetMember(parent, currentPropertyName, memberAccessorFactory);
					if (child == null) 
					{
						try 
						{
							IFactory factory = objectFactory.CreateFactory(type);
							child = factory.CreateInstance();

							SetMemberValue(parent, currentPropertyName, child, objectFactory, memberAccessorFactory);
						} 
						catch (Exception e) 
						{
							throw new ProbeException("Cannot set value of property '" + memberName + "' because '" + currentPropertyName + "' is null and cannot be instantiated on instance of " + type.Name + ". Cause:" + e.Message, e);
						}
					}
					currentPropertyName = (string)enumerator.Current;
				}
				SetMember(child, currentPropertyName, memberValue, memberAccessorFactory);
			} 
			else 
			{
				SetMember(obj, memberName, memberValue, memberAccessorFactory);
			}
		}


		/// <summary>
		/// 
		/// </summary>
		/// <param name="obj"></param>
		/// <param name="memberName"></param>
		/// <param name="memberValue"></param>
		/// <param name="memberAccessorFactory"></param>
		protected static void SetMember(object obj, string memberName, object memberValue,
			IMemberAccessorFactory memberAccessorFactory)
		{
			try 
			{
				if (memberName.IndexOf("[") > -1) 
				{
					SetArrayMember(obj, memberName, memberValue, memberAccessorFactory);
				} 
				else 
				{
					if (obj is IDictionary) 
					{
						((IDictionary) obj)[memberName] = memberValue;
					} 
					else 
					{
						Type targetType = obj.GetType();
						IMemberAccessor memberAccessor = memberAccessorFactory.CreateMemberAccessor(targetType, memberName);
						
						if (memberAccessor == null) 
						{
							throw new ProbeException("No Set method for member " + memberName + " on instance of " + obj.GetType().Name);
						}
						try 
						{
							memberAccessorFactory.CreateMemberAccessor(targetType, memberName).Set(obj, memberValue);
						}
						catch (Exception ex) 
						{
							throw new ProbeException(ex);
						}					
					}
				}
			}
			catch (ProbeException pe) 
			{
				throw pe;
			} 
			catch (Exception e) 
			{
				throw new ProbeException("Could not Get property '" + memberName + "' for " + obj.GetType().Name + ".  Cause: " + e.Message, e);
			}
		}


		/// <summary>
		/// Checks to see if a Object has a writable property/field be a given name
		/// </summary>
		/// <param name="obj"> The object to check</param>
		/// <param name="propertyName">The property to check for</param>
		/// <returns>True if the property exists and is writable</returns>
		public static bool HasWritableProperty(object obj, string propertyName) 
		{
			bool hasProperty = false;
			if (obj is IDictionary) 
			{
				hasProperty = ((IDictionary) obj).Contains(propertyName);
			} 
			else 
			{
				if (propertyName.IndexOf('.') > -1) 
				{
					StringTokenizer parser = new StringTokenizer(propertyName, ".");
					IEnumerator enumerator = parser.GetEnumerator();
					Type type = obj.GetType();

					while (enumerator.MoveNext()) 
					{ 
						propertyName = (string)enumerator.Current;
						type = ReflectionInfo.GetInstance(type).GetGetterType(propertyName);
						hasProperty = ReflectionInfo.GetInstance(type).HasWritableMember(propertyName);
					}
				} 
				else 
				{
					hasProperty = ReflectionInfo.GetInstance(obj.GetType()).HasWritableMember(propertyName);
				}
			}
			return hasProperty;
		}


		/// <summary>
		/// Checks to see if the Object have a property/field be a given name.
		/// </summary>
		/// <param name="obj">The Object on which to invoke the specified property.</param>
		/// <param name="propertyName">The name of the property to check for.</param>
		/// <returns>
		/// True or false if the property exists and is readable.
		/// </returns>
		public static bool HasReadableProperty(object obj, string propertyName) 
		{
			bool hasProperty = false;

			if (obj is IDictionary) 
			{
				hasProperty = ((IDictionary) obj).Contains(propertyName);
			} 
			else 
			{
				if (propertyName.IndexOf('.') > -1) 
				{
					StringTokenizer parser = new StringTokenizer(propertyName, ".");
					IEnumerator enumerator = parser.GetEnumerator();
					Type type = obj.GetType();

					while (enumerator.MoveNext()) 
					{ 
						propertyName = (string)enumerator.Current;
						type = ReflectionInfo.GetInstance(type).GetGetterType(propertyName);
						hasProperty = ReflectionInfo.GetInstance(type).HasReadableMember(propertyName);
					}
				} 
				else 
				{
					hasProperty = ReflectionInfo.GetInstance(obj.GetType()).HasReadableMember(propertyName);
				}
			}
			
			return hasProperty;
		}

		/// <summary>
		/// 
		/// </summary>
		/// <param name="type"></param>
		/// <returns></returns>
		public static bool IsSimpleType(Type type) 
		{
			if (_simpleTypeMap.Contains(type)) 
			{
				return true;
			} 
			else if (typeof(ICollection).IsAssignableFrom(type))
			{
				return true;
			} 
			else if (typeof(IDictionary).IsAssignableFrom(type)) 
			{
				return true;
			} 
			else if (typeof(IList).IsAssignableFrom(type)) 
			{
				return true;
			} 
			else if (typeof(IEnumerable).IsAssignableFrom(type)) 
			{
				return true;
			} 
			else
			{
				return false;
			}
		}


//		/// <summary>
//		///  Calculates a hash code for all readable properties of a object.
//		/// </summary>
//		/// <param name="obj">The object to calculate the hash code for.</param>
//		/// <returns>The hash code.</returns>
//		public static int ObjectHashCode(object obj) 
//		{
//			return ObjectHashCode(obj, GetReadablePropertyNames(obj));
//		}
//
//
//		/// <summary>
//		/// Calculates a hash code for a subset of the readable properties of a object.
//		/// </summary>
//		/// <param name="obj">The object to calculate the hash code for.</param>
//		/// <param name="properties">A list of the properties to hash.</param>
//		/// <returns>The hash code.</returns>
//		public static int ObjectHashCode(object obj, string[] properties ) 
//		{
//			ArrayList alreadyDigested = new ArrayList();
//
//			int hashcode = obj.GetType().FullName.GetHashCode();
//			int length = properties.Length;
//			for (int i = 0; i < length; i++) 
//			{
//				object value = GetProperty(obj, properties[i]);
//				if (value != null) 
//				{
//					if (IsSimpleType(value.GetType())) 
//					{
//						hashcode += value.GetHashCode();
//						hashcode += value.ToString().GetHashCode()*37;
//					} 
//					else 
//					{
//						// It's a Object 
//						// Check to avoid endless loop (circular dependency)
//						if (value != obj) 
//						{
//							if (!alreadyDigested.Contains(value)) 
//							{
//								alreadyDigested.Add(value);
//								hashcode += ObjectHashCode(value);
//							}
//						}
//					}
//					hashcode *= 29;
//				}
//			}
//			return hashcode;
//		}

	}
}
