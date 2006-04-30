#region Apache Notice
/*****************************************************************************
 * $Revision: 374175 $
 * $LastChangedDate: 2006-04-09 20:24:53 +0200 (dim., 09 avr. 2006) $
 * $LastChangedBy: gbayon $
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
using System.Reflection;
using System.Reflection.Emit;

namespace IBatisNet.Common.Utilities.Objects.Members
{
	/// <summary>
	/// The DelegateFieldAccessor class defines a property accessor and
	/// provides <c>Reflection.Emit</c>-generated <see cref="Get"/> and <see cref="Set"/> 
	/// via the new DynamicMethod (.NET V2).
	/// </summary>
    public sealed class DelegatePropertyAccessor : BaseAccessor, IPropertyAccessor
	{
		private delegate void SetValue(object instance, object value);
		private delegate object GetValue(object instance);

		private SetValue _set = null;
		private GetValue _get = null;

		private bool _canRead = false;
		private bool _canWrite = false;

		#region IPropertyAccessor Members
		/// <summary>
		/// Gets a value indicating whether the property can be read. 
		/// </summary>
		public bool CanRead
		{
			get { return _canRead; }
		}

		/// <summary>
		/// Gets a value indicating whether the property can be written to. 
		/// </summary>
		public bool CanWrite
		{
			get { return _canWrite; }
		}
		#endregion

		#region IMemberAccessor Members

        /// <summary>
        /// Gets the field value from the specified target.
        /// </summary>
        /// <param name="target">Target object.</param>
        /// <returns>Property value.</returns>
        public override object Get(object target)
		{
			if (_canRead)
			{
				return _get( target );
			}
			else
			{
				throw new NotSupportedException(
					string.Format("Property \"{0}\" on type "
					+ "{1} doesn't have a get method.", memberName, targetType));
			}
		}

        /// <summary>
        /// Sets the field for the specified target.
        /// </summary>
        /// <param name="target">Target object.</param>
        /// <param name="value">Value to set.</param>
        public override void Set(object target, object value)
		{
			if (_canWrite)
			{
				object newValue = value;
				if (newValue == null)
				{
					// If the value to assign is null, assign null internal value
					newValue = nullInternal;
				}

				_set(target, newValue);
			}
			else
			{
				throw new NotSupportedException(
					string.Format("Property \"{0}\" on type "
					+ "{1} doesn't have a set method.", memberName, targetType));
			}		
		}

		#endregion

        /// <summary>
        /// Initializes a new instance of the <see cref="T:DelegatePropertyAccessor"/> class
        /// for property access via DynamicMethod.
        /// </summary>
        /// <param name="targetObjectType">Type of the target object.</param>
        /// <param name="propertyName">Name of the property.</param>
        public DelegatePropertyAccessor(Type targetObjectType, string propertyName)
		{
            this.targetType = targetObjectType;
            this.memberName = propertyName;

			PropertyInfo propertyInfo = targetType.GetProperty(propertyName);

			// Make sure the property exists
			if(propertyInfo == null)
			{
				throw new NotSupportedException(
					string.Format("Property \"{0}\" does not exist for type "
					+ "{1}.", propertyName, targetType));
			}
			else
			{
                this.baseMemberType = propertyInfo.PropertyType;
                _canRead = propertyInfo.CanRead;
                _canWrite = propertyInfo.CanWrite;

                this.nullInternal = this.GetNullInternal(this.baseMemberType);

				if (propertyInfo.CanWrite)
				{
					DynamicMethod dynamicMethod = new DynamicMethod("SetImplementation", null, new Type[] { typeof(object), typeof(object) }, this.GetType().Module, false);
					ILGenerator ilgen = dynamicMethod.GetILGenerator();
                    
                    // Emit the IL for set access. 
                    MethodInfo targetSetMethod = propertyInfo.GetSetMethod();

                    Type paramType = targetSetMethod.GetParameters()[0].ParameterType;
                    ilgen.DeclareLocal(paramType);
                    ilgen.Emit(OpCodes.Ldarg_0); //Load the first argument (target object)
                    ilgen.Emit(OpCodes.Castclass, targetType); //Cast to the source type
                    ilgen.Emit(OpCodes.Ldarg_1); //Load the second argument (value object)
                    if (paramType.IsValueType)
                    {
                        ilgen.Emit(OpCodes.Unbox, paramType); //Unbox it 	
                        if (typeToOpcode[paramType] != null)
                        {
                            OpCode load = (OpCode)typeToOpcode[paramType];
                            ilgen.Emit(load); //and load
                        }
                        else
                        {
                            ilgen.Emit(OpCodes.Ldobj, paramType);
                        }
                    }
                    else
                    {
                        ilgen.Emit(OpCodes.Castclass, paramType); //Cast class
                    }
                    ilgen.EmitCall(OpCodes.Callvirt, targetSetMethod, null); //Set the property value
                    ilgen.Emit(OpCodes.Ret);
				
					_set = (SetValue)dynamicMethod.CreateDelegate(typeof(SetValue));
				}

				if (propertyInfo.CanRead)
				{
					DynamicMethod dynamicMethod = new DynamicMethod("GetImplementation", typeof(object), new Type[] { typeof(object) }, this.GetType().Module, false);
					ILGenerator ilgen = dynamicMethod.GetILGenerator();

                    // Emit the IL for get access. 
                    MethodInfo targetGetMethod = propertyInfo.GetGetMethod();

                    ilgen.DeclareLocal(typeof(object));
                    ilgen.Emit(OpCodes.Ldarg_0);	//Load the first argument,(target object)
                    ilgen.Emit(OpCodes.Castclass, targetType);	//Cast to the source type
                    ilgen.EmitCall(OpCodes.Callvirt, targetGetMethod, null); //Get the property value
                    if (targetGetMethod.ReturnType.IsValueType)
                    {
                        ilgen.Emit(OpCodes.Box, targetGetMethod.ReturnType); //Box if necessary
                    }
                    ilgen.Emit(OpCodes.Stloc_0); //Store it
                    ilgen.Emit(OpCodes.Ldloc_0);
                    ilgen.Emit(OpCodes.Ret);
  
					_get = (GetValue)dynamicMethod.CreateDelegate(typeof(GetValue));
				}
			}
		}

	}
}
