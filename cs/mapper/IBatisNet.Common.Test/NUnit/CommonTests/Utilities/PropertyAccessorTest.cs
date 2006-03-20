using System;
using System.Reflection;
using IBatisNet.Common.Test.Domain;
using IBatisNet.Common.Utilities;
using IBatisNet.Common.Utilities.Objects.Members;
using NUnit.Framework;

namespace IBatisNet.Common.Test.NUnit.CommonTests.Utilities
{
    [TestFixture]
    public class PropertyAccessorTest : BaseMemberTest
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

        ///// <summary>
        ///// Test MemberAccessorFactory
        ///// </summary>
        //[Test]
        //public void TestMemberAccessorFactory()
        //{
        //    IMemberAccessor propertyAccessor1 = factory.CreateMemberAccessor(typeof(Property), "Int");
        //    IMemberAccessor propertyAccessor2 = factory.CreateMemberAccessor(typeof(Property), "Int");

        //    Assert.AreEqual(HashCodeProvider.GetIdentityHashCode(propertyAccessor1), HashCodeProvider.GetIdentityHashCode(propertyAccessor2) );
        //}

        ///// <summary>
        ///// Test multiple MemberAccessorFactory
        ///// </summary>
        //[Test]
        //public void TestMultipleMemberAccessorFactory()
        //{
        //    Property prop = new Property();
        //    IMemberAccessor propertyAccessor1 = factory1.CreateMemberAccessor(typeof(Property), "Int");

        //    MemberAccessorFactory factory2 = new MemberAccessorFactory(true);
        //    IMemberAccessor propertyAccessor2 = factory2.CreateMemberAccessor(typeof(Property), "Int");

        //    Assert.AreEqual(int.MinValue, propertyAccessor1.Get(prop));
        //    Assert.AreEqual(int.MinValue, propertyAccessor2.Get(prop));
        //}






    }
}
