using System;

namespace IBatisNet.DataMapper.Test.Domain
{
	/// <summary>
	/// Description r�sum�e de Account.
	/// </summary>
	[Serializable]
	public class Account
	{
		private int id;
		private string _firstName;
		private string _lastName;
		private string _emailAddress;
		private int[] _ids = null;
		private bool _bannerOption = false;
		private bool _cartOption = false;
	    private Document _document = null;

		public Account()
		{}

        public Account(int identifiant, string firstName, string lastName)
		{
            id = identifiant;
			_firstName = firstName;
			_lastName = lastName;
		}

		public int Id
		{
			get { return id; }
			set { id = value; }
		}

		public string FirstName
		{
			get { return _firstName; }
			set { _firstName = value; }
		}

		public string LastName
		{
			get { return _lastName; }
			set { _lastName = value; }
		}

		public string EmailAddress
		{
			get { return _emailAddress; }
			set { _emailAddress = value; }
		}

		public int[] Ids
		{
			get { return _ids; }
			set { _ids = value; }
		}

		public bool BannerOption
		{
			get { return _bannerOption; }
			set { _bannerOption = value; }
		}

		public bool CartOption
		{
			get { return _cartOption; }
			set { _cartOption = value; }
		}

        public Document Document
        {
            get { return _document; }
            set { _document = value; }
        }
	}
}
