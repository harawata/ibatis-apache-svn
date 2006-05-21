using System.Configuration;
using IBatisNet.DataAccess.Configuration;
using NUnit.Framework;

namespace IBatisNet.DataAccess.Test.NUnit.DaoTests.Ado.MySql
{
	/// <summary>
	/// Summary description for AdoDaoTest.
	/// </summary>
	[Category("MySql")]
	public class AdoDaoTest : BaseDaoTest
	{
		/// <summary>
		/// Initialisation
		/// </summary>
		[TestFixtureSetUp] 
		public void FixtureSetUp() 
		{
			DomDaoManagerBuilder builder = new DomDaoManagerBuilder();
			builder.Configure( "dao_MySql_"
				 + ConfigurationSettings.AppSettings["providerType"] + ".config" );
			daoManager = DaoManager.GetInstance();

		}

		/// <summary>
		/// Initialisation
		/// </summary>
		[SetUp] 
		public void SetUp() 
		{			
			InitScript( daoManager.LocalDataSource, ScriptDirectory + "account-init.sql" );
		}
	}
}
