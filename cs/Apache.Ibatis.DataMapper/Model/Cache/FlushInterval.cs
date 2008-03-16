
#region Apache Notice
/*****************************************************************************
 * $Header: $
 * $Revision: 383115 $
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

#region Imports
using System;
using System.Xml.Serialization;
#endregion

namespace Apache.Ibatis.DataMapper.Model.Cache
{
	/// <summary>
	/// Summary description for FlushInterval.
	/// </summary>
	[Serializable]
	public class FlushInterval
	{
		
		#region Fields 

		private int hours = 0;
		private int minutes= 0;
		private int seconds = 0;
		private int milliseconds = 0;
		private long interval = CacheModel.NO_FLUSH_INTERVAL;

		#endregion

		#region Properties
		/// <summary>
		/// Flush interval in hours
		/// </summary>
		public int Hours
		{
			get { return hours; }
		}

		/// <summary>
		/// Flush interval in minutes
		/// </summary>
		public int Minutes
		{
			get { return minutes; }
		}

		/// <summary>
		/// Flush interval in seconds
		/// </summary>
		public int Seconds
		{
			get { return seconds; }

		}

		/// <summary>
		/// Flush interval in milliseconds
		/// </summary>
		public int Milliseconds
		{
			get {return milliseconds;}

		}

		/// <summary>
		/// Get the flush interval value
		/// </summary>
		public long Interval
		{
			get { return interval; }
		}

		#endregion



        /// <summary>
        /// Initializes a new instance of the <see cref="FlushInterval"/> class.
        /// </summary>
        /// <param name="h">The hours.</param>
        /// <param name="mn">The minutes.</param>
        /// <param name="sec">The seconds.</param>
        /// <param name="ms">The milliseconds.</param>
        public FlushInterval(int h, int mn, int sec, int ms)
		{
            milliseconds = ms;
            seconds = sec;
            minutes = mn;
            hours = h;

            if (milliseconds != 0) 
			{
                interval += (milliseconds * TimeSpan.TicksPerMillisecond);
			}
			if (seconds != 0) 
			{
                interval += (seconds * TimeSpan.TicksPerSecond);
			}
			if (minutes != 0) 
			{
                interval += (minutes * TimeSpan.TicksPerMinute);
			}
			if (hours != 0) 
			{
                interval += (hours * TimeSpan.TicksPerHour);
			}

            if (interval == 0)
			{
                interval = CacheModel.NO_FLUSH_INTERVAL;
			}
		}

	}
}
