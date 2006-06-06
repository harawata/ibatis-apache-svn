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
    /// The <see cref="DelegatePropertySetAccessor"/> class defines a set property accessor and
    /// provides <c>Reflection.Emit</c>-generated <see cref="ISet"/> 
    /// via the new DynamicMethod (.NET V2).
    /// </summary>
    public sealed class DelegatePropertySetAccessor : BaseAccessor, ISetAccessor
    {
        private delegate void SetValue(object instance, object value);

        private SetValue _set = null;

        /// <summary>
        /// The property name
        /// </summary>
        private string _propertyName = string.Empty;
        /// <summary>
        /// The property type
        /// </summary>
        private Type _propertyType = null;
        private bool _canWrite = false;
        private Type _targetType = null;

                /// <summary>
        /// Initializes a new instance of the <see cref="T:DelegatePropertySetAccessor"/> class
        /// for set property access via DynamicMethod.
        /// </summary>
        /// <param name="targetObjectType">Type of the target object.</param>
        /// <param name="propertyName">Name of the property.</param>
        public DelegatePropertySetAccessor(Type targetObjectType, string propertyName)
		{
            _targetType = targetObjectType;
            _propertyName = propertyName;

            // deals with Overriding a property using new and reflection
            // http://blogs.msdn.com/thottams/archive/2006/03/17/553376.aspx
            PropertyInfo propertyInfo = _targetType.GetProperty(propertyName, BindingFlags.Public | BindingFlags.Instance | BindingFlags.DeclaredOnly);
            if (propertyInfo == null)
            {
                propertyInfo = _targetType.GetProperty(propertyName);
            }
                    
			// Make sure the property exists
			if(propertyInfo == null)
			{
				throw new NotSupportedException(
					string.Format("Property \"{0}\" does not exist for type "
                    + "{1}.", propertyName, _targetType));
			}
			else
			{
                _propertyType = propertyInfo.PropertyType;
                _canWrite = propertyInfo.CanWrite;

                this.nullInternal = this.GetNullInternal(_propertyType);

				if (propertyInfo.CanWrite)
				{
					DynamicMethod dynamicMethod = new DynamicMethod("SetImplementation", null, new Type[] { typeof(object), typeof(object) }, this.GetType().Module, false);
					ILGenerator ilgen = dynamicMethod.GetILGenerator();
                    
                    // Emit the IL for set access. 
                    MethodInfo targetSetMethod = propertyInfo.GetSetMethod();

                    Type paramType = targetSetMethod.GetParameters()[0].ParameterType;
                    ilgen.DeclareLocal(paramType);
                    ilgen.Emit(OpCodes.Ldarg_0); //Load the first argument (target object)
                    ilgen.Emit(OpCodes.Castclass, _targetType); //Cast to the source type
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
			}
		}

        #region IAccessor Members

        /// <summary>
        /// Gets the property's name.
        /// </summary>
        /// <value></value>
        public string Name
        {
            get { return _propertyName; }
        }

        /// <summary>
        /// Gets the property's type.
        /// </summary>
        /// <value></value>
        public Type MemberType
        {
            get { return _propertyType; }
        }

        #endregion

        #region ISet Members

        /// <summary>
        /// Sets the field for the specified target.
        /// </summary>
        /// <param name="target">Target object.</param>
        /// <param name="value">Value to set.</param>
        public void Set(object target, object value)
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
                    + "{1} doesn't have a set method.", _propertyName, _targetType));
            }
        }

        #endregion
    }
}
