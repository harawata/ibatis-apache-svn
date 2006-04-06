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
	public class FieldAccessorTest : BaseMemberTest
	{

        #region SetUp & TearDown

        /// <summary>
        /// SetUp
        /// </summary>
		[SetUp]
		public void SetUp()
		{
			intAccessor = factory.CreateMemberAccessor(typeof(Property), "Int");
			longAccessor = factory.CreateMemberAccessor(typeof(Property), "Long");
			sbyteAccessor = factory.CreateMemberAccessor(typeof(Property), "SByte");
			stringAccessor = factory.CreateMemberAccessor(typeof(Property), "String");
			datetimeAccessor = factory.CreateMemberAccessor(typeof(Property), "DateTime");
			decimalAccessor = factory.CreateMemberAccessor(typeof(Property), "Decimal");
			byteAccessor = factory.CreateMemberAccessor(typeof(Property), "Byte");
			charAccessor = factory.CreateMemberAccessor(typeof(Property), "Char");
			shortAccessor = factory.CreateMemberAccessor(typeof(Property), "Short");
			ushortAccessor = factory.CreateMemberAccessor(typeof(Property), "UShort");
			uintAccessor = factory.CreateMemberAccessor(typeof(Property), "UInt");
			ulongAccessor = factory.CreateMemberAccessor(typeof(Property), "ULong");
			boolAccessor = factory.CreateMemberAccessor(typeof(Property), "Bool");
			doubleAccessor = factory.CreateMemberAccessor(typeof(Property), "Double");
			floatAccessor = factory.CreateMemberAccessor(typeof(Property), "Float");
			guidAccessor = factory.CreateMemberAccessor(typeof(Property), "Guid");
			timespanAccessor = factory.CreateMemberAccessor(typeof(Property), "TimeSpan");
			accountAccessor = factory.CreateMemberAccessor(typeof(Property), "Account");
			enumAccessor = factory.CreateMemberAccessor(typeof(Property), "Day");
#if dotnet2
            nullableAccessor = factory.CreateMemberAccessor(typeof(Property), "IntNullable");
#endif
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
            IMemberAccessor memberAccessor = factory.CreateMemberAccessor(typeof(Property), "publicInt");
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
            IMemberAccessor memberAccessor = factory.CreateMemberAccessor(typeof(Property), "publicInt");
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
            IMemberAccessor memberAccessor = factory.CreateMemberAccessor(typeof(Property), "publicInt");
            Assert.AreEqual(test, memberAccessor.Get(prop));
        }

        /// <summary>
        /// Test setting an integer private field.
        /// </summary>
        [Test]
        public void TestSetPrivateFieldInteger()
        {
            Property prop = new Property();
            prop.Int = -99;

            // Property accessor
            int test = 57;
            IMemberAccessor memberAccessor = factory.CreateMemberAccessor(typeof(Property), "_int");
            memberAccessor.Set(prop, test);
            Assert.AreEqual(test, prop.Int);
        }

        /// <summary>
        /// Test getting an integer private field.
        /// </summary>
        [Test]
        public void TestGetPrivateFieldInteger()
        {
            int test = -99;
            Property prop = new Property();
            prop.Int = test;

            // Property accessor
            IMemberAccessor memberAccessor = factory.CreateMemberAccessor(typeof(Property), "_int");
            Assert.AreEqual(test, memberAccessor.Get(prop));
        }
	}
}
