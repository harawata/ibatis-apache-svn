using IBatisNet.DataMapper;
using IBatisNet.DataMapper.TypeHandlers;
using NUnit.Framework;


namespace IBatisNet.DataMapper.Test.NUnit.SqlMapTests
{
	/// <summary>
	/// Summary description for CacheKeyTest.
	/// </summary>
	[TestFixture]
	public class CacheKeyTest
	{
		private const long A_LONG = 1L;
		private const long ANOTHER_LONG_WITH_SAME_HASHCODE = -9223372034707292159;

		[Test]
		public void ShouldNotBeConsideredEqualWhenParametersHaveTheSameHashCodeButAreNotEqual()
		{
			TypeHandlerFactory factory = new TypeHandlerFactory();

			// Two cache keys are equal except for the parameter.
			CacheKey key = new CacheKey(factory, "STATEMENT", "SQL", new TestClass(A_LONG), new string[] {"AProperty"}, 0, 0, CacheKeyType.Object);
			CacheKey aDifferentKey = new CacheKey(factory, "STATEMENT", "SQL", new TestClass(ANOTHER_LONG_WITH_SAME_HASHCODE), new string[] {"AProperty"}, 0, 0, CacheKeyType.Object);

			Assert.IsFalse(aDifferentKey.Equals(key)); // should not be equal.
		}

		private class TestClass
		{
			private long _property = long.MinValue;

			public TestClass(long aProperty)
			{
				_property = aProperty;
			}

			public long AProperty
			{
				get { return _property; }
				set { _property = value; }
			}
		}

	}
}
