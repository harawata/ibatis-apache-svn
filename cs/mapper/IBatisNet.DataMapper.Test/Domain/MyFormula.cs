using System.Data;
using IBatisNet.DataMapper.Configuration.ResultMapping;

namespace IBatisNet.DataMapper.Test.Domain
{
	/// <summary>
	/// Summary description for MyFormula.
	/// </summary>
	public class MyFormula : IDiscriminatorFormula
	{

		#region IDiscriminatorFormula Members

		public string GetDiscriminatorValue(IDataReader dataReader)
		{
			string type = dataReader.GetString(dataReader.GetOrdinal("Document_Type"));

			if (type=="Monograph" || type=="Book")
			{
				return "Book";
			}
			else if (type=="Tabloid" || type=="Broadsheet" || type=="Newspaper")
			{

				return "Newspaper";
			}
			else
			{
				return "Document";
			}
		}

		#endregion
	}
}
