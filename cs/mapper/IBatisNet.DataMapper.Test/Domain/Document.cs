using System;

namespace IBatisNet.DataMapper.Test.Domain
{
	/// <summary>
	/// Description r�sum�e de Document.
	/// </summary>
	public class Document
	{
		private int _id = -1;
		private string _title = string.Empty;

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

		public string Title
		{
			get
			{
				return _title; 
			}
			set
			{ 
				_title = value; 
			}
		}

		public Document()
		{
			//
			// TODO�: ajoutez ici la logique du constructeur
			//
		}
	}
}
