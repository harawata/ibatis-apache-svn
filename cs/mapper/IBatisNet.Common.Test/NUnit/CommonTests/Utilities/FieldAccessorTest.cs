using System;
using System.Reflection;
using IBatisNet.Common.Test.Domain;
using IBatisNet.Common.Utilities;
using IBatisNet.Common.Utilities.Objects.Members;
using NUnit.Framework;

namespace IBatisNet.Common.Test.NUnit.CommonTests.Utilities
{
	/// <summary>
	/// Summary description for FieldAccessorTest.
	/// </summary>
	[TestFixture] 
	public class FieldAccessorTest
	{
		/// <summary>
		/// Test setting an integer property.
		/// </summary>
		[Test]
		public void TestSetInteger()
		{
			Property prop = new Property();
			prop.Int = -99;

			// Property accessor
			MemberAccessorFactory factory = new MemberAccessorFactory(true);
			IMemberAccessor propertyAccessor = factory.CreateMemberAccessor(typeof(Property), "_int");
			int test = 57;
			propertyAccessor.Set(prop, test);
			Assert.AreEqual(test, prop.Int);
		}

		/// <summary>
		/// Test getting an integer property.
		/// </summary>
		[Test]
		public void TestGetInteger()
		{
			int test = -99;
			Property prop = new Property();
			prop.Int = test;

			// Property accessor
			MemberAccessorFactory factory = new MemberAccessorFactory(true);
			IMemberAccessor propertyAccessor = factory.CreateMemberAccessor(typeof(Property), "_int");
			Assert.AreEqual(test, propertyAccessor.Get(prop));
		}
	}
}
