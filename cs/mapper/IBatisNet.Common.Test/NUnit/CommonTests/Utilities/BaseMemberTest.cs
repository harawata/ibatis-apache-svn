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
	public abstract class BaseMemberTest
    {
        protected MemberAccessorFactory factory = null;
        protected IMemberAccessor intAccessor = null;
        protected IMemberAccessor longAccessor = null;
        protected IMemberAccessor sbyteAccessor = null;
        protected IMemberAccessor datetimeAccessor = null;
        protected IMemberAccessor decimalAccessor = null;
        protected IMemberAccessor byteAccessor = null;
        protected IMemberAccessor stringAccessor = null;
        protected IMemberAccessor charAccessor = null;
        protected IMemberAccessor shortAccessor = null;
        protected IMemberAccessor ushortAccessor = null;
        protected IMemberAccessor uintAccessor = null;
        protected IMemberAccessor ulongAccessor = null;
        protected IMemberAccessor boolAccessor = null;
        protected IMemberAccessor doubleAccessor = null;
        protected IMemberAccessor floatAccessor = null;
        protected IMemberAccessor guidAccessor = null;
        protected IMemberAccessor timespanAccessor = null;
        protected IMemberAccessor accountAccessor = null;
        protected IMemberAccessor enumAccessor = null;
#if dotnet2
        protected IMemberAccessor nullableAccessor = null;
#endif


        /// <summary>
		/// Initialize an sqlMap
		/// </summary>
        [TestFixtureSetUp]
        protected virtual void SetUpFixture()
        {
            factory = new MemberAccessorFactory(true);
        }

        /// <summary>
        /// Dispose the SqlMap
        /// </summary>
        [TestFixtureTearDown]
        protected virtual void TearDownFixture()
        {
            factory = null;
        }

        /// <summary>
        /// Test setting null on integer property.
        /// </summary>
        [Test]
        public void TestSetNullOnIntegerProperty()
        {
            Property prop = new Property();
            prop.Int = -99;

            // Property accessor
            intAccessor.Set(prop, null);
            Assert.AreEqual(0, prop.Int);
        }

        /// <summary>
        /// Test setting an integer property.
        /// </summary>
        [Test]
        public void TestSetInteger()
        {
            Property prop = new Property();
            prop.Int = -99;

            // Property accessor
            int test = 57;
            intAccessor.Set(prop, test);
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
            Assert.AreEqual(test, intAccessor.Get(prop));
        }

        /// <summary>
        /// Test setting null on Long property.
        /// </summary>
        [Test]
        public void TestSetNullOnLongProperty()
        {
            Property prop = new Property();
            prop.Long = 78945566664213223;

            // Property accessor
            longAccessor.Set(prop, null);
            Assert.AreEqual((long)0, prop.Long);
        }

        /// <summary>
        /// Test setting an Long property.
        /// </summary>
        [Test]
        public void TestSetLong()
        {
            Property prop = new Property();
            prop.Long = 78945566664213223;

            // Property accessor
            long test = 123456789987456;
            longAccessor.Set(prop, test);
            Assert.AreEqual(test, prop.Long);
        }

        /// <summary>
        /// Test getting an long property.
        /// </summary>
        [Test]
        public void TestGetLong()
        {
            long test = 78945566664213223;
            Property prop = new Property();
            prop.Long = test;

            // Property accessor
            Assert.AreEqual(test, longAccessor.Get(prop));
        }

        /// <summary>
        /// Test setting null on sbyte property.
        /// </summary>
        [Test]
        public void TestSetNullOnSbyteProperty()
        {
            Property prop = new Property();
            prop.SByte = 78;

            // Property accessor
            sbyteAccessor.Set(prop, null);
            Assert.AreEqual((sbyte)0, prop.SByte);
        }

        /// <summary>
        /// Test setting an sbyte property.
        /// </summary>
        [Test]
        public void TestSetSbyte()
        {
            Property prop = new Property();
            prop.SByte = 78;

            // Property accessor
            sbyte test = 19;
            sbyteAccessor.Set(prop, test);
            Assert.AreEqual(test, prop.SByte);
        }

        /// <summary>
        /// Test getting an sbyte property.
        /// </summary>
        [Test]
        public void TestGetSbyte()
        {
            sbyte test = 78;
            Property prop = new Property();
            prop.SByte = test;

            // Property accessor
            Assert.AreEqual(test, sbyteAccessor.Get(prop));
        }

        /// <summary>
		/// Test setting null on String property.
		/// </summary>
		[Test]
		public void TestSetNullOnStringProperty()
		{
			Property prop = new Property();
			prop.String = "abc";

			// Property accessor
            stringAccessor.Set(prop, null);
			Assert.IsNull(prop.String);
		}

		/// <summary>
		/// Test setting an String property.
		/// </summary>
		[Test]
		public void TestSetString()
		{
			Property prop = new Property();
			prop.String = "abc";

			// Property accessor
			string test = "wxc";
            stringAccessor.Set(prop, test);
			Assert.AreEqual(test, prop.String);
		}

		/// <summary>
		/// Test getting an String property.
		/// </summary>
        [Test]
        public void TestGetString()
        {
            string test = "abc";
            Property prop = new Property();
            prop.String = test;

            // Property accessor
            Assert.AreEqual(test, stringAccessor.Get(prop));
        }

        		/// <summary>
		/// Test setting null on DateTime property.
		/// </summary>
		[Test]
		public void TestSetNullOnDateTimeProperty()
		{
			Property prop = new Property();
			prop.DateTime = DateTime.Now;
			
			// Property accessor
			datetimeAccessor.Set(prop, null);
			Assert.AreEqual(DateTime.MinValue, prop.DateTime);
		}

		/// <summary>
		/// Test setting an DateTime property.
		/// </summary>
		[Test]
		public void TestSetDateTime()
		{
			Property prop = new Property();
			prop.DateTime = DateTime.Now;

			// Property accessor
			DateTime test = new DateTime(1987,11,25);
			datetimeAccessor.Set(prop, test);
			Assert.AreEqual(test, prop.DateTime);
		}

		/// <summary>
		/// Test getting an DateTime property.
		/// </summary>
        [Test]
        public void TestGetDateTime()
        {
            DateTime test = new DateTime(1987, 11, 25);
            Property prop = new Property();
            prop.DateTime = test;

            // Property accessor
            Assert.AreEqual(test, datetimeAccessor.Get(prop));
        }

        /// <summary>
        /// Test setting null on decimal property.
        /// </summary>
        [Test]
        public void TestSetNullOnDecimalProperty()
        {
            Property prop = new Property();
            prop.Decimal = 45.187M;

            // Property accessor
            decimalAccessor.Set(prop, null);
            Assert.AreEqual(0.0M, prop.Decimal);
        }

        /// <summary>
        /// Test setting an decimal property.
        /// </summary>
        [Test]
        public void TestSetDecimal()
        {
            Property prop = new Property();
            prop.Decimal = 45.187M;

            // Property accessor
            Decimal test = 789456.141516M;
            decimalAccessor.Set(prop, test);
            Assert.AreEqual(test, prop.Decimal);
        }

        /// <summary>
        /// Test getting an decimal property.
        /// </summary>
        [Test]
        public void TestGetDecimal()
        {
            Decimal test = 789456.141516M;
            Property prop = new Property();
            prop.Decimal = test;

            // Property accessor
            Assert.AreEqual(test, decimalAccessor.Get(prop));
        }

        /// <summary>
        /// Test setting null on byte property.
        /// </summary>
        [Test]
        public void TestSetNullOnByteProperty()
        {
            Property prop = new Property();
            prop.Byte = 78;

            // Property accessor
            byteAccessor.Set(prop, null);
            Assert.AreEqual((byte)0, prop.Byte);
        }

        /// <summary>
        /// Test setting an byte property.
        /// </summary>
        [Test]
        public void TestSetByte()
        {
            Property prop = new Property();
            prop.Byte = 15;

            // Property accessor
            byte test = 94;
            byteAccessor.Set(prop, test);
            Assert.AreEqual(test, prop.Byte);
        }

        /// <summary>
        /// Test getting an byte property.
        /// </summary>
        [Test]
        public void TestGetByte()
        {
            byte test = 78;
            Property prop = new Property();
            prop.Byte = test;

            // Property accessor
            Assert.AreEqual(test, byteAccessor.Get(prop));
        }

        /// <summary>
        /// Test setting null on char property.
        /// </summary>
        [Test]
        public void TestSetNullOnCharProperty()
        {
            Property prop = new Property();
            prop.Char = 'r';

            // Property accessor
            charAccessor.Set(prop, null);
            Assert.AreEqual('\0', prop.Char);
        }

        /// <summary>
        /// Test setting an char property.
        /// </summary>
        [Test]
        public void TestSetChar()
        {
            Property prop = new Property();
            prop.Char = 'b';

            // Property accessor
            char test = 'j';
            charAccessor.Set(prop, test);
            Assert.AreEqual(test, prop.Char);
        }

        /// <summary>
        /// Test getting an char property.
        /// </summary>
        [Test]
        public void TestGetChar()
        {
            char test = 'z';
            Property prop = new Property();
            prop.Char = test;

            // Property accessor
            Assert.AreEqual(test, charAccessor.Get(prop));
        }

        /// <summary>
        /// Test setting null on short property.
        /// </summary>
        [Test]
        public void TestSetNullOnShortProperty()
        {
            Property prop = new Property();
            prop.Short = 5;

            // Property accessor
            shortAccessor.Set(prop, null);
            Assert.AreEqual((short)0, prop.Short);
        }

        /// <summary>
        /// Test setting an short property.
        /// </summary>
        [Test]
        public void TestSetShort()
        {
            Property prop = new Property();
            prop.Short = 9;

            // Property accessor
            short test = 45;
            shortAccessor.Set(prop, test);
            Assert.AreEqual(test, prop.Short);
        }

        /// <summary>
        /// Test getting an short property.
        /// </summary>
        [Test]
        public void TestGetShort()
        {
            short test = 99;
            Property prop = new Property();
            prop.Short = test;

            // Property accessor
            Assert.AreEqual(test, shortAccessor.Get(prop));
        }

        /// <summary>
        /// Test setting null on ushort property.
        /// </summary>
        [Test]
        public void TestSetNullOnUShortProperty()
        {
            Property prop = new Property();
            prop.UShort = 5;

            // Property accessor
            ushortAccessor.Set(prop, null);
            Assert.AreEqual((ushort)0, prop.UShort);
        }

        /// <summary>
        /// Test setting an ushort property.
        /// </summary>
        [Test]
        public void TestSetUShort()
        {
            Property prop = new Property();
            prop.UShort = 9;

            // Property accessor
            ushort test = 45;
            ushortAccessor.Set(prop, test);
            Assert.AreEqual(test, prop.UShort);
        }

        /// <summary>
        /// Test getting an ushort property.
        /// </summary>
        [Test]
        public void TestGetUShort()
        {
            ushort test = 99;
            Property prop = new Property();
            prop.UShort = test;

            // Property accessor
            Assert.AreEqual(test, ushortAccessor.Get(prop));
        }

        /// <summary>
        /// Test setting null on uint property.
        /// </summary>
        [Test]
        public void TestSetNullOnUIntProperty()
        {
            Property prop = new Property();
            prop.UInt = 5;

            // Property accessor
            uintAccessor.Set(prop, null);
            Assert.AreEqual((uint)0, prop.UInt);
        }

        /// <summary>
        /// Test setting an uint property.
        /// </summary>
        [Test]
        public void TestSetUInt()
        {
            Property prop = new Property();
            prop.UInt = 9;

            // Property accessor
            uint test = 45;
            uintAccessor.Set(prop, test);
            Assert.AreEqual(test, prop.UInt);
        }

        /// <summary>
        /// Test getting an uint property.
        /// </summary>
        [Test]
        public void TestGetUInt()
        {
            uint test = 99;
            Property prop = new Property();
            prop.UInt = test;

            // Property accessor
            Assert.AreEqual(test, uintAccessor.Get(prop));
        }

        /// <summary>
        /// Test setting null on ulong property.
        /// </summary>
        [Test]
        public void TestSetNullOnULongProperty()
        {
            Property prop = new Property();
            prop.ULong = 5L;

            // Property accessor
            ulongAccessor.Set(prop, null);
            Assert.AreEqual((ulong)0, prop.ULong);
        }

        /// <summary>
        /// Test setting an ulong property.
        /// </summary>
        [Test]
        public void TestSetULong()
        {
            Property prop = new Property();
            prop.ULong = 45464646578;

            // Property accessor
            ulong test = 45;
            ulongAccessor.Set(prop, test);
            Assert.AreEqual(test, prop.ULong);
        }

        /// <summary>
        /// Test getting an ulong property.
        /// </summary>
        [Test]
        public void TestGetULong()
        {
            ulong test = 99;
            Property prop = new Property();
            prop.ULong = test;

            // Property accessor
            Assert.AreEqual(test, ulongAccessor.Get(prop));
        }

        /// <summary>
        /// Test setting null on bool property.
        /// </summary>
        [Test]
        public void TestSetNullOnBoolProperty()
        {
            Property prop = new Property();
            prop.Bool = true;

            // Property accessor
            boolAccessor.Set(prop, null);
            Assert.AreEqual(false, prop.Bool);
        }

        /// <summary>
        /// Test setting an bool property.
        /// </summary>
        [Test]
        public void TestSetBool()
        {
            Property prop = new Property();
            prop.Bool = false;

            // Property accessor
            bool test = true;
            boolAccessor.Set(prop, test);
            Assert.AreEqual(test, prop.Bool);
        }

        /// <summary>
        /// Test getting an bool property.
        /// </summary>
        [Test]
        public void TestGetBool()
        {
            bool test = false;
            Property prop = new Property();
            prop.Bool = test;

            // Property accessor
            Assert.AreEqual(test, boolAccessor.Get(prop));
        }

        		/// <summary>
		/// Test setting null on double property.
		/// </summary>
		[Test]
		public void TestSetNullOnDoubleProperty()
		{
			Property prop = new Property();
			prop.Double = 788956.56D;
			
			// Property accessor
            doubleAccessor.Set(prop, null);
			Assert.AreEqual(0.0D, prop.Double);
		}

		/// <summary>
		/// Test setting an double property.
		/// </summary>
		[Test]
		public void TestSetDouble()
		{
			Property prop = new Property();
			prop.Double = 56789123.45888D;

			// Property accessor
			double test = 788956.56D;
            doubleAccessor.Set(prop, test);
			Assert.AreEqual(test, prop.Double);
		}

		/// <summary>
		/// Test getting an double property.
		/// </summary>
        [Test]
        public void TestGetDouble()
        {
            double test = 788956.56D;
            Property prop = new Property();
            prop.Double = test;

            // Property accessor
            Assert.AreEqual(test, doubleAccessor.Get(prop));
        }

        /// <summary>
        /// Test setting null on float property.
        /// </summary>
        [Test]
        public void TestSetNullOnFloatProperty()
        {
            Property prop = new Property();
            prop.Float = 565.45F;

            // Property accessor
            floatAccessor.Set(prop, null);
            Assert.AreEqual(0.0D, prop.Float);
        }

        /// <summary>
        /// Test setting an float property.
        /// </summary>
        [Test]
        public void TestSetFloat()
        {
            Property prop = new Property();
            prop.Float = 565.45F;

            // Property accessor
            float test = 4567.45F;
            floatAccessor.Set(prop, test);
            Assert.AreEqual(test, prop.Float);
        }

        /// <summary>
        /// Test getting an float property.
        /// </summary>
        [Test]
        public void TestGetFloat()
        {
            float test = 565.45F;
            Property prop = new Property();
            prop.Float = test;

            // Property accessor
            Assert.AreEqual(test, floatAccessor.Get(prop));
        }


        /// <summary>
        /// Test setting null on Guid property.
        /// </summary>
        [Test]
        public void TestSetNullOnGuidProperty()
        {
            Property prop = new Property();
            prop.Guid = Guid.NewGuid();

            // Property accessor
            guidAccessor.Set(prop, null);
            Assert.AreEqual(Guid.Empty, prop.Guid);
        }

        /// <summary>
        /// Test setting an Guid property.
        /// </summary>
        [Test]
        public void TestSetGuid()
        {
            Property prop = new Property();
            prop.Guid = Guid.NewGuid();

            // Property accessor
            Guid test = Guid.NewGuid();
            guidAccessor.Set(prop, test);
            Assert.AreEqual(test, prop.Guid);
        }

        /// <summary>
        /// Test getting an Guid property.
        /// </summary>
        [Test]
        public void TestGetGuid()
        {
            Guid test = Guid.NewGuid();
            Property prop = new Property();
            prop.Guid = test;

            // Property accessor
            Assert.AreEqual(test, guidAccessor.Get(prop));
        }


        /// <summary>
        /// Test the setting null on a TimeSpan property.
        /// </summary>
        [Test]
        public void TestSetNullOnTimeSpanProperty()
        {
            Property prop = new Property();
            prop.TimeSpan = new TimeSpan(5, 12, 57, 21, 13);

            // Property accessor
            timespanAccessor.Set(prop, null);
            Assert.AreEqual(TimeSpan.MinValue, prop.TimeSpan);
        }

        /// <summary>
        /// Test setting an TimeSpan property.
        /// </summary>
        [Test]
        public void TestSetTimeSpan()
        {
            Property prop = new Property();
            prop.TimeSpan = new TimeSpan(5, 12, 57, 21, 13);

            // Property accessor
            TimeSpan test = new TimeSpan(15, 5, 21, 45, 35);
            timespanAccessor.Set(prop, test);
            Assert.AreEqual(test, prop.TimeSpan);
        }

        /// <summary>
        /// Test getting an TimeSpan property.
        /// </summary>
        [Test]
        public void TestGetTimeSpan()
        {
            TimeSpan test = new TimeSpan(5, 12, 57, 21, 13);
            Property prop = new Property();
            prop.TimeSpan = test;

            // Property accessor
            Assert.AreEqual(test, timespanAccessor.Get(prop));
        }

        		/// <summary>
		/// Test the setting null on a object property.
		/// </summary>
		[Test]
		public void TestSetNullOnAccountProperty()
		{
			Property prop = new Property();
			prop.Account = new Account() ;
			prop.Account.FirstName = "test";
			
			// Property accessor
            accountAccessor.Set(prop, null);
			Assert.AreEqual(null, prop.Account);
		}

		/// <summary>
		/// Test getting an object property.
		/// </summary>
		[Test]
		public void TestGetAccount()
		{
			Account test = new Account();
			test.FirstName = "Gilles";

			Property prop = new Property();
			prop.Account = test;

			// Property accessor
			Assert.AreEqual(HashCodeProvider.GetIdentityHashCode(test), HashCodeProvider.GetIdentityHashCode(prop.Account));
            Assert.AreEqual(test.FirstName, ((Account)accountAccessor.Get(prop)).FirstName);
		}
		
		/// <summary>
		/// Test setting an object property.
		/// </summary>
		[Test]
		public void TestSetAccount()
		{
			Property prop = new Property();
			prop.Account = new Account() ;
			prop.Account.FirstName = "test";

			// Property accessor
            string firstName = "Gilles";
			Account test = new Account();
            test.FirstName = firstName;
            accountAccessor.Set(prop, test);

            Assert.AreEqual(firstName, prop.Account.FirstName);
        }

        /// <summary>
        /// Test the setting null on a Enum property.
        /// </summary>
        [Test]
        public void TestSetNullOnEnumProperty()
        {
            Property prop = new Property();
            prop.Day = Days.Thu;

            PropertyInfo propertyInfo = typeof(Property).GetProperty("Day", BindingFlags.Public | BindingFlags.SetProperty | BindingFlags.Instance);
            propertyInfo.SetValue(prop, null, null);

            // Property accessor
            enumAccessor.Set(prop, null);
            Assert.AreEqual(TimeSpan.MinValue, prop.TimeSpan);
        }

        /// <summary>
        /// Test setting an Enum property.
        /// </summary>
        [Test]
        public void TestSetEnum()
        {
            Property prop = new Property();
            prop.Day = Days.Thu;

            // Property accessor
            Days test = Days.Wed;
            enumAccessor.Set(prop, test);
            Assert.AreEqual(test, prop.Day);
        }

        /// <summary>
        /// Test getting an Enum property.
        /// </summary>
        [Test]
        public void TestGetEnum()
        {
            Days test = Days.Wed;
            Property prop = new Property();
            prop.Day = test;

            // Property accessor
            Assert.AreEqual(test, enumAccessor.Get(prop));
        }

#if dotnet2
        /// <summary>
        /// Test the setting null on a nullable int property.
        /// </summary>
        [Test]
        public void TestSetNullOnNullableIntProperty()
        {
            Property prop = new Property();
            prop.IntNullable = 85;

            // Property accessor
            nullableAccessor.Set(prop, null);
            Assert.AreEqual(null, prop.IntNullable);
        }

        /// <summary>
        /// Test getting an nullable int property.
        /// </summary>
        [Test]
        public void TestGetNullableInt()
        {
            Int32? test = 55;
            Property prop = new Property();
            prop.IntNullable = test;

            // Property accessor
            Assert.AreEqual(test, nullableAccessor.Get(prop));
        }

        /// <summary>
        /// Test setting an nullable int property.
        /// </summary>
        [Test]
        public void TestSetNullableInt()
        {
            Property prop = new Property();
            prop.IntNullable = 99;

            // Property accessor
            Int32? test = 55;
            nullableAccessor.Set(prop, test);
            Assert.AreEqual(test, prop.IntNullable);

        }
#endif
    }
}
