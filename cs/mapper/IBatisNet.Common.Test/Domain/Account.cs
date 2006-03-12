using System;

namespace IBatisNet.Common.Test.Domain
{
	/// <summary>
	/// Description résumée de Account.
	/// </summary>
	[Serializable]
	public class Account
	{
		private int _id = 0;
		private string _firstName = string.Empty;
        private string _lastName = string.Empty;
        private string _emailAddress = string.Empty;
		private int[] _ids = null;

		public int Id
		{
			get
			{
				return _id; 
			}
			set
			{ 
				_id = value; 
			}
		}

		public string FirstName
		{
			get
			{
				return _firstName; 
			}
			set
			{ 
				_firstName = value; 
			}
		}

		public string LastName
		{
			get
			{
				return _lastName; 
			}
			set
			{ 
				_lastName = value; 
			}
		}

		public string EmailAddress
		{
			get
			{
				return _emailAddress; 
			}
			set
			{ 
				_emailAddress = value; 
			}
		}

		public int[] Ids
		{
			get
			{
				return _ids; 
			}
			set
			{ 
				_ids = value; 
			}
		}
	}
}
