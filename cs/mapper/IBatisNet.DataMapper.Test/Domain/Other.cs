using System;

namespace IBatisNet.DataMapper.Test.Domain
{
	/// <summary>
	/// Description résumée de Other.
	/// </summary>
	public class Other
	{
		private int _int;
		private long _long;
		private bool _bool = false;

		public bool Bool
		{
			get
			{
				return _bool; 
			}
			set
			{ 
				_bool = value; 
			}
		}

		public int Int
		{
			get
			{
				return _int; 
			}
			set
			{ 
				_int = value; 
			}
		}

		public long Long
		{
			get
			{
				return _long; 
			}
			set
			{ 
				_long = value; 
			}
		}
	}
}
