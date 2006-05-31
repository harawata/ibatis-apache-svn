using System;

namespace IBatisNet.Common.Test.Domain
{
	/// <summary>
	/// Summary description for Document.
	/// </summary>
	public abstract class Document
	{
		private DateTime _date = DateTime.MinValue;
		
		public DateTime Creation
		{
			get { return _date;}
			set { _date = value;}
		}
	}
}
