using System;
using NPetshop.Persistence.Interfaces.Accounts;
using NUnit.Framework;


namespace NPetshop.Test.Persistence
{
	/// <summary>
	/// Description résumée de DaoTest.
	/// </summary>
	public class DaoTest : BaseTest
	{

		[Test] 						
		public void TestGetDao()
		{
			Type type = typeof(IAccountDao);

			IAccountDao accountDao = (IAccountDao)daoManager[typeof(IAccountDao)];

			Assert.IsNotNull(accountDao);
			Assert.IsTrue(type.IsInstanceOfType(accountDao));
		}
	}
}
