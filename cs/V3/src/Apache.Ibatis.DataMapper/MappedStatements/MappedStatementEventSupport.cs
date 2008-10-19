#region Apache Notice
/*****************************************************************************
 * $Revision: 476843 $
 * $LastChangedDate: 2008-06-08 20:20:44 +0200 (dim., 08 juin 2008) $
 * $LastChangedBy: gbayon $
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
using System.ComponentModel;
using Apache.Ibatis.DataMapper.Model.Events;

namespace Apache.Ibatis.DataMapper.MappedStatements
{
    /// <summary>
    /// Base implementation for <see cref="IMappedStatementEvents"/>
    /// </summary>
    [Serializable]
    public abstract class MappedStatementEventSupport : IMappedStatementEvents
    {
        protected static readonly object PreInsertEvent = new object();
        protected static readonly object PreSelectEvent = new object();
        protected static readonly object PreUpdateOrDeleteEvent = new object();
        protected static readonly object PostInsertEvent = new object();
        protected static readonly object PostSelectEvent = new object();
        protected static readonly object PostUpdateOrDeleteEvent = new object();

        private readonly EventHandlerList events = new EventHandlerList();

        #region IMappedStatementEvents Members

        /// <summary>
        /// Will handle an <see cref="PreInsertEventArgs"/>. 
        /// </summary>
        public event EventHandler<PreInsertEventArgs> PreInsert
        {
            add { events.AddHandler(PreInsertEvent, value); }
            remove { events.RemoveHandler(PreInsertEvent, value); }
        }

        /// <summary>
        /// Will handle an <see cref="PreSelectEventArgs"/>. 
        /// </summary>
        public event EventHandler<PreSelectEventArgs> PreSelect
        {
            add { events.AddHandler(PreSelectEvent, value); }
            remove { events.RemoveHandler(PreSelectEvent, value); }
        }

        /// <summary>
        /// Will handle an <see cref="PreUpdateOrDeleteEventArgs"/>. 
        /// </summary>
        public event EventHandler<PreUpdateOrDeleteEventArgs> PreUpdateOrDelete
        {
            add { events.AddHandler(PreUpdateOrDeleteEvent, value); }
            remove { events.RemoveHandler(PreUpdateOrDeleteEvent, value); }
        }

        /// <summary>
        /// Will handle an <see cref="PostInsertEventArgs"/>. 
        /// </summary>
        public event EventHandler<PostInsertEventArgs> PostInsert
        {
            add { events.AddHandler(PostInsertEvent, value); }
            remove { events.RemoveHandler(PostInsertEvent, value); }
        }

        /// <summary>
        /// Will handle an <see cref="PostSelectEventArgs"/>. 
        /// </summary>
        public event EventHandler<PostSelectEventArgs> PostSelect
        {
            add { events.AddHandler(PostSelectEvent, value); }
            remove { events.RemoveHandler(PostSelectEvent, value); }
        }

        /// <summary>
        /// Will handle an <see cref="PostUpdateOrDeleteEventArgs"/>. 
        /// </summary>
        public event EventHandler<PostUpdateOrDeleteEventArgs> PostUpdateOrDelete
        {
            add { events.AddHandler(PostUpdateOrDeleteEvent, value); }
            remove { events.RemoveHandler(PostUpdateOrDeleteEvent, value); }
        }

        #endregion

        /// <summary>
        /// Raises the pre event.
        /// </summary>
        /// <param name="key">The key.</param>
        /// <param name="parameterObject">The parameter object.</param>
        /// <returns>Returns is used as the parameter object</returns>
        protected object RaisePreEvent<TEvent>(object key,  object parameterObject)
            where TEvent : PreStatementEventArgs, new()
        {
            EventHandler<TEvent> handlers = (EventHandler<TEvent>)events[key];

            if (handlers != null)
            {
                TEvent eventArgs = new TEvent();
                eventArgs.ParameterObject = parameterObject;
                handlers(this, eventArgs);            
                return eventArgs.ParameterObject;
            }
            return parameterObject;
        }

        /// <summary>
        /// Raises the post event.
        /// </summary>
        /// <param name="key">The key.</param>
        /// <param name="parameterObject">The parameter object.</param>
        /// <param name="resultObject">The result object.</param>
        /// <returns>Returns is used as the result object</returns>
        protected TType RaisePostEvent<TType, TEvent>(object key, object parameterObject, TType resultObject)
            where TEvent : PostStatementEventArgs, new()
        {
            EventHandler<TEvent> handlers = (EventHandler<TEvent>)events[key];

            if (handlers != null)
            {
                TEvent eventArgs = new TEvent();
                eventArgs.ParameterObject = parameterObject;
                eventArgs.ResultObject = resultObject;
                handlers(this, eventArgs);
                return (TType)eventArgs.ResultObject;
            }
            return resultObject;
        }
    }
}
