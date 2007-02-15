using System;
using System.Collections.Generic;

namespace IBatisNet.DataMapper.Test.Domain
{
	/// <summary>
	/// Summary description for User.
	/// </summary>
	public class User
	{
        protected Nullable<int> id = null;
        protected string name;
	    protected IList<Document> documents = new List<Document>();

        public IList<Document> Documents
        {
            get { return documents; }
        }

        public Nullable<int> Id 
		{
			get { return id; }
		}

		public string Name 
		{
            get { return name; }
		}


	}
}
