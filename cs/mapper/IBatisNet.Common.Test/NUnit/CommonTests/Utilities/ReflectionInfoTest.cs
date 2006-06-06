using System;
using IBatisNet.Common.Utilities.Objects;
using NUnit.Framework;


using IBatisNet.Common.Test.Domain;
namespace IBatisNet.Common.Test.NUnit.CommonTests.Utilities
{
	/// <summary>
	/// Summary description for ReflectionInfoTest.
	/// </summary>
	[TestFixture]
	public class ReflectionInfoTest
	{
		/// <summary>
		/// Test multiple call to factory
		/// </summary>
		[Test]
		public void TestReflectionInfo()
		{

			ReflectionInfo info = ReflectionInfo.GetInstance(typeof (Document));
			
			Type type = info.GetGetterType("PageNumber");
		}
	}
}
