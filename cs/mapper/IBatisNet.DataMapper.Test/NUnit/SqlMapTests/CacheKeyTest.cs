

using NUnit.Framework;

namespace IBatisNet.DataMapper.Test.NUnit.SqlMapTests
{
	/// <summary>
	/// Summary description for CacheKeyTest.
	/// </summary>
	[TestFixture]
	public class CacheKeyTest
	{
		[Test]
		public void ShouldNotConsider1LAndNegative9223372034707292159LToBeEqual()
		{
			// old version of ObjectProbe gave TestClass based on these longs the same HashCode
			DoTestClassEquals(1L, -9223372034707292159L);
		}

		[Test]
		public void ShouldNotConsider1LAndNegative9223372036524971138LToBeEqual()
		{
			// current version of ObjectProbe gives TestClass based on these longs the same HashCode
			DoTestClassEquals(1L, -9223372036524971138L);
		}

		private static void DoTestClassEquals(long firstLong, long secondLong)
		{
			// Two cache keys are equal except for the parameter.
			Configuration.Cache.CacheKey key = new Configuration.Cache.CacheKey();

			key.Update(firstLong);

			Configuration.Cache.CacheKey aDifferentKey = new Configuration.Cache.CacheKey();

			key.Update(secondLong);

			Assert.IsFalse(aDifferentKey.Equals(key)); // should not be equal.
		}

		[Test]
		public void CacheKeyWithSameHashcode() 
		{
			Configuration.Cache.CacheKey key1 = new Configuration.Cache.CacheKey();
			Configuration.Cache.CacheKey key2 = new Configuration.Cache.CacheKey();

			key1.Update("HS1CS001");
			key2.Update("HS1D4001");

			Assert.AreEqual( key1.GetHashCode(), key2.GetHashCode(), "Expect same hashcode.");
			Assert.IsFalse( key1.Equals(key2),"Expect not equal");
		}

		[Test]
		public void CacheKeyWithTwoParamsSameHashcode() 
		{
			Configuration.Cache.CacheKey key1 = new Configuration.Cache.CacheKey();
			Configuration.Cache.CacheKey key2 = new Configuration.Cache.CacheKey();

			key1.Update("HS1CS001");
			key1.Update("HS1D4001");

			key2.Update("HS1D4001");
			key2.Update("HS1CS001");

			Assert.AreEqual(key1.GetHashCode(), key2.GetHashCode(), "Expect same hashcode.");
			Assert.IsFalse(key1.Equals(key2), "Expect not equal");
		}

	}
}
