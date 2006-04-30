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
	/// The DelegateFieldAccessor class defines a field accessor and
	/// provides <c>Reflection.Emit</c>-generated <see cref="Get"/> and <see cref="Set"/> 
	/// via the new DynamicMethod (.NET V2).
	/// </summary>
    public sealed class DelegateFieldAccessor : BaseAccessor
	{
        private delegate void SetValue(object instance, object value);
        private delegate object GetValue(object instance);

        private SetValue _set = null;
        private GetValue _get = null;

        #region IMemberAccessor Members

        /// <summary>
        /// Gets the field value from the specified target.
        /// </summary>
        /// <param name="target">Target object.</param>
        /// <returns>Property value.</returns>
        public override object Get(object target)
        {
            return _get(target);
        }

        /// <summary>
        /// Sets the field for the specified target.
        /// </summary>
        /// <param name="target">Target object.</param>
        /// <param name="value">Value to set.</param>
        public override void Set(object target, object value)
        {
            object newValue = value;
            if (newValue == null)
            {
                // If the value to assign is null, assign null internal value
                newValue = nullInternal;
            }
            _set(target, newValue);
        }

        #endregion

        /// <summary>
        /// Initializes a new instance of the <see cref="T:DelegateFieldAccessor"/> class
        /// for field access via DynamicMethod.
        /// </summary>
        /// <param name="targetObjectType">Type of the target object.</param>
        /// <param name="fieldName">Name of the field.</param>
        public DelegateFieldAccessor(Type targetObjectType, string fieldName)
        {
            this.targetType = targetObjectType;
            this.memberName = fieldName;

            FieldInfo fieldInfo = targetType.GetField(fieldName, BindingFlags.NonPublic | BindingFlags.Instance | BindingFlags.Public);

            // Make sure the field exists
            if (fieldInfo == null)
            {
                throw new NotSupportedException(
                    string.Format("Field \"{0}\" does not exist for type "
                    + "{1}.", fieldName, targetObjectType));
            }
            else
            {
                this.baseMemberType = fieldInfo.FieldType;
                this.nullInternal = this.GetNullInternal(this.baseMemberType);

                DynamicMethod dynamicMethodGet = new DynamicMethod("GetImplementation", typeof(object), new Type[] { typeof(object) }, this.GetType().Module, false);
                ILGenerator ilgen = dynamicMethodGet.GetILGenerator();

                // Emit the IL for get access. 

                // We need a reference to the current instance (stored in local argument index 0) 
                // so Ldfld can load from the correct instance (this one).
                ilgen.Emit(OpCodes.Ldarg_0);
                ilgen.Emit(OpCodes.Ldfld, fieldInfo);
                if (baseMemberType.IsValueType)
                {
                    // Now, we execute the box opcode, which pops the value of field 'x',
                    // returning a reference to the filed value boxed as an object.
                    ilgen.Emit(OpCodes.Box, fieldInfo.FieldType);
                }
                ilgen.Emit(OpCodes.Ret);
                _get = (GetValue)dynamicMethodGet.CreateDelegate(typeof(GetValue));

                // Emit the IL for set access. 
                DynamicMethod dynamicMethodSet = new DynamicMethod("SetImplementation", null, new Type[] { typeof(object), typeof(object) }, this.GetType().Module, false);
                ilgen = dynamicMethodSet.GetILGenerator();

                ilgen.Emit(OpCodes.Ldarg_0);//Load the first argument (target object)
                ilgen.Emit(OpCodes.Castclass, targetType); //Cast to the source type
                ilgen.Emit(OpCodes.Ldarg_1);//Load the second argument (value object)
                if (baseMemberType.IsValueType)
                {
                    ilgen.Emit(OpCodes.Unbox, baseMemberType); //Unbox it 	
                    if (typeToOpcode[baseMemberType] != null)
                    {
                        OpCode load = (OpCode)typeToOpcode[baseMemberType];
                        ilgen.Emit(load); //and load
                    }
                    else
                    {
                        ilgen.Emit(OpCodes.Ldobj, baseMemberType);
                    }
                    ilgen.Emit(OpCodes.Stfld, fieldInfo); //Set the field value
                }
                else
                {
                    ilgen.Emit(OpCodes.Castclass, baseMemberType); //Cast class
                }

                ilgen.Emit(OpCodes.Ret);
                _set = (SetValue)dynamicMethodSet.CreateDelegate(typeof(SetValue));               
            }
		}

	}
}
