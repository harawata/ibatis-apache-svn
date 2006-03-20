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
        private MemberAccessorFactory _factory = null;

        #region SetUp & TearDown

        /// <summary>
        /// SetUp
        /// </summary>
        [SetUp]
        public void SetUp()
        {
            _factory = new MemberAccessorFactory(true);
        }


        /// <summary>
        /// TearDown
        /// </summary>
        [TearDown]
        public void Dispose()
        {
        }

        #endregion

        /// <summary>
        /// Test setting null on integer public field.
        /// </summary>
        [Test]
        public void TestSetNullOnIntegerField()
        {
            Property prop = new Property();
            prop.publicInt = -99;

            // Property accessor
            IMemberAccessor memberAccessor = _factory.CreateMemberAccessor(typeof(Property), "publicInt");
            memberAccessor.Set(prop, null);
            Assert.AreEqual(0, prop.publicInt);
        }

        /// <summary>
        /// Test setting an integer public field.
        /// </summary>
        [Test]
        public void TestSetPublicFieldInteger()
        {
            Property prop = new Property();
            prop.publicInt = -99;

            // Property accessor
            int test = 57;
            IMemberAccessor memberAccessor = _factory.CreateMemberAccessor(typeof(Property), "publicInt");
            memberAccessor.Set(prop, test);
            Assert.AreEqual(test, prop.publicInt);
        }

        /// <summary>
        /// Test getting an integer public field.
        /// </summary>
        [Test]
        public void TestGetPublicFieldInteger()
        {
            int test = -99;
            Property prop = new Property();
            prop.publicInt = test;

            // Property accessor
            IMemberAccessor memberAccessor = _factory.CreateMemberAccessor(typeof(Property), "publicInt");
            Assert.AreEqual(test, memberAccessor.Get(prop));
        }

        /// <summary>
        /// Test setting an integer private field.
        /// </summary>
        [Test]
        [ExpectedException(typeof(FieldAccessException))]
        public void TestSetPrivateFieldInteger()
        {
            Property prop = new Property();
            prop.Int = -99;

            // Property accessor
            int test = 57;
            IMemberAccessor memberAccessor = _factory.CreateMemberAccessor(typeof(Property), "_int");
            memberAccessor.Set(prop, test);
            Assert.AreEqual(test, prop.Int);
        }

        /// <summary>
        /// Test getting an integer private field.
        /// </summary>
        [Test]
        [ExpectedException(typeof(FieldAccessException))]
        public void TestGetPrivateFieldInteger()
        {
            int test = -99;
            Property prop = new Property();
            prop.Int = test;

            // Property accessor
            IMemberAccessor memberAccessor = _factory.CreateMemberAccessor(typeof(Property), "_int");
            Assert.AreEqual(test, memberAccessor.Get(prop));
        }
	}
}
