using System;
using System.Reflection;
using IBatisNet.Common.Test.Domain;
using IBatisNet.Common.Utilities;
using IBatisNet.Common.Utilities.Objects;
using NUnit.Framework;

namespace IBatisNet.Common.Test.NUnit.CommonTests.Utilities
{
    [TestFixture] 
    public class PropertyAccessorTest
    {
        #region SetUp & TearDown

        /// <summary>
        /// SetUp
        /// </summary>
        [SetUp]
        public void SetUp()
        {
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
		/// Test PropertyAccessorFactory
		/// </summary>
		[Test]
		public void TestPropertyAccessorFactory()
		{
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor1 = factory.CreatePropertyAccessor(typeof(Property), "Int");
			IPropertyAccessor propertyAccessor2 = factory.CreatePropertyAccessor(typeof(Property), "Int");

			Assert.AreEqual(HashCodeProvider.GetIdentityHashCode(propertyAccessor1), HashCodeProvider.GetIdentityHashCode(propertyAccessor2) );
		}

		/// <summary>
		/// Test multiple PropertyAccessorFactory
		/// </summary>
		[Test]
		public void TestMultiplePropertyAccessorFactory()
		{
			Property prop = new Property();
			PropertyAccessorFactory factory1 = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor1 = factory1.CreatePropertyAccessor(typeof(Property), "Int");

			PropertyAccessorFactory factory2 = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor2 = factory2.CreatePropertyAccessor(typeof(Property), "Int");

			Assert.AreEqual(int.MinValue, propertyAccessor1.Get(prop));
			Assert.AreEqual(int.MinValue, propertyAccessor2.Get(prop));
		}

       

		/// <summary>
        /// Test the performance of getting an integer property.
        /// </summary>
        [Test]
        public void TestSetNullOnIntegerProperty()
        {
            Property prop = new Property();
            prop.Int = -99;

            // Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Int");
            propertyAccessor.Set(prop, null);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Int");
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Int");
			Assert.AreEqual(test, propertyAccessor.Get(prop));
		}


		/// <summary>
		/// Test the performance of getting an Long property.
		/// </summary>
		[Test]
		public void TestSetNullOnLongProperty()
		{
			Property prop = new Property();
			prop.Long = 78945566664213223;

			// Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Long");
			propertyAccessor.Set(prop, null);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Long");
			long test = 123456789987456;
			propertyAccessor.Set(prop, test);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Long");
			Assert.AreEqual(test, propertyAccessor.Get(prop));
		}


		/// <summary>
		/// Test the performance of getting an sbyte property.
		/// </summary>
		[Test]
		public void TestSetNullOnSbyteProperty()
		{
			Property prop = new Property();
			prop.SByte = 78;

			// Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "SByte");
			propertyAccessor.Set(prop, null);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "SByte");
			sbyte test = 19;
			propertyAccessor.Set(prop, test);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "SByte");
			Assert.AreEqual(test, propertyAccessor.Get(prop));
		}


		/// <summary>
		/// Test the performance of getting an String property.
		/// </summary>
		[Test]
		public void TestSetNullOnStringProperty()
		{
			Property prop = new Property();
			prop.String = "abc";

			// Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "String");
			propertyAccessor.Set(prop, null);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "String");
			string test = "wxc";
			propertyAccessor.Set(prop, test);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "String");
			Assert.AreEqual(test, propertyAccessor.Get(prop));
		}


		/// <summary>
		/// Test the performance of getting an DateTime property.
		/// </summary>
		[Test]
		public void TestSetNullOnDateTimeProperty()
		{
			Property prop = new Property();
			prop.DateTime = DateTime.Now;
			
//			PropertyInfo propertyInfo = typeof(Property).GetProperty("DateTime", BindingFlags.Public | BindingFlags.SetProperty | BindingFlags.Instance);
//			propertyInfo.SetValue(prop, null, null);

			// Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "DateTime");
			propertyAccessor.Set(prop, null);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "DateTime");
			DateTime test = new DateTime(1987,11,25);
			propertyAccessor.Set(prop, test);
			Assert.AreEqual(test, prop.DateTime);
		}

		/// <summary>
		/// Test getting an DateTime property.
		/// </summary>
		[Test]
		public void TestGetDateTime()
		{
			DateTime test = new DateTime(1987,11,25);
			Property prop = new Property();
			prop.DateTime = test;

			// Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "DateTime");
			Assert.AreEqual(test, propertyAccessor.Get(prop));
		}

		
		/// <summary>
		/// Test the performance of getting an decimal property.
		/// </summary>
		[Test]
		public void TestSetNullOnDecimalProperty()
		{
			Property prop = new Property();
			prop.Decimal = 45.187M;
			
			// Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Decimal");
			propertyAccessor.Set(prop, null);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Decimal");
			Decimal test = 789456.141516M;
			propertyAccessor.Set(prop, test);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Decimal");
			Assert.AreEqual(test, propertyAccessor.Get(prop));
		}

		
		/// <summary>
		/// Test the performance of getting an byte property.
		/// </summary>
		[Test]
		public void TestSetNullOnByteProperty()
		{
			Property prop = new Property();
			prop.Byte = 78;
			
			// Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Byte");
			propertyAccessor.Set(prop, null);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Byte");
			byte test = 94;
			propertyAccessor.Set(prop, test);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Byte");
			Assert.AreEqual(test, propertyAccessor.Get(prop));
		}


		/// <summary>
		/// Test the performance of getting an char property.
		/// </summary>
		[Test]
		public void TestSetNullOnCharProperty()
		{
			Property prop = new Property();
			prop.Char = 'r';
			
			// Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Char");
			propertyAccessor.Set(prop, null);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Char");
			char test = 'j';
			propertyAccessor.Set(prop, test);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Char");
			Assert.AreEqual(test, propertyAccessor.Get(prop));
		}


		/// <summary>
		/// Test the performance of getting an short property.
		/// </summary>
		[Test]
		public void TestSetNullOnShortProperty()
		{
			Property prop = new Property();
			prop.Short = 5;
			
			// Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Short");
			propertyAccessor.Set(prop, null);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Short");
			short test = 45;
			propertyAccessor.Set(prop, test);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Short");
			Assert.AreEqual(test, propertyAccessor.Get(prop));
		}


		/// <summary>
		/// Test the performance of getting an ushort property.
		/// </summary>
		[Test]
		public void TestSetNullOnUShortProperty()
		{
			Property prop = new Property();
			prop.UShort = 5;
			
			// Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "UShort");
			propertyAccessor.Set(prop, null);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "UShort");
			ushort test = 45;
			propertyAccessor.Set(prop, test);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "UShort");
			Assert.AreEqual(test, propertyAccessor.Get(prop));
		}


		/// <summary>
		/// Test the performance of getting an uint property.
		/// </summary>
		[Test]
		public void TestSetNullOnUIntProperty()
		{
			Property prop = new Property();
			prop.UInt = 5;
			
			// Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "UInt");
			propertyAccessor.Set(prop, null);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "UInt");
			uint test = 45;
			propertyAccessor.Set(prop, test);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "UInt");
			Assert.AreEqual(test, propertyAccessor.Get(prop));
		}

		
		/// <summary>
		/// Test the performance of getting an ulong property.
		/// </summary>
		[Test]
		public void TestSetNullOnULongProperty()
		{
			Property prop = new Property();
			prop.ULong = 5L;
			
			// Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "ULong");
			propertyAccessor.Set(prop, null);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "ULong");
			ulong test = 45;
			propertyAccessor.Set(prop, test);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "ULong");
			Assert.AreEqual(test, propertyAccessor.Get(prop));
		}


		/// <summary>
		/// Test the performance of getting an bool property.
		/// </summary>
		[Test]
		public void TestSetNullOnBoolProperty()
		{
			Property prop = new Property();
			prop.Bool = true;
			
			// Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Bool");
			propertyAccessor.Set(prop, null);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Bool");
			bool test = true;
			propertyAccessor.Set(prop, test);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Bool");
			Assert.AreEqual(test, propertyAccessor.Get(prop));
		}


		/// <summary>
		/// Test the performance of getting an double property.
		/// </summary>
		[Test]
		public void TestSetNullOnDoubleProperty()
		{
			Property prop = new Property();
			prop.Double = 788956.56D;
			
			// Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Double");
			propertyAccessor.Set(prop, null);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Double");
			double test = 788956.56D;
			propertyAccessor.Set(prop, test);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Double");
			Assert.AreEqual(test, propertyAccessor.Get(prop));
		}


		/// <summary>
		/// Test the performance of getting an float property.
		/// </summary>
		[Test]
		public void TestSetNullOnFloatProperty()
		{
			Property prop = new Property();
			prop.Float = 565.45F;
			
			// Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Float");
			propertyAccessor.Set(prop, null);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Float");
			float test = 4567.45F;
			propertyAccessor.Set(prop, test);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Float");
			Assert.AreEqual(test, propertyAccessor.Get(prop));
		}


		/// <summary>
		/// Test the performance of getting an Guid property.
		/// </summary>
		[Test]
		public void TestSetNullOnGuidProperty()
		{
			Property prop = new Property();
			prop.Guid = Guid.NewGuid();
			
			// Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Guid");
			propertyAccessor.Set(prop, null);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Guid");
			Guid test = Guid.NewGuid();
			propertyAccessor.Set(prop, test);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Guid");
			Assert.AreEqual(test, propertyAccessor.Get(prop));
		}


		/// <summary>
		/// Test the setting null on a TimeSpan property.
		/// </summary>
		[Test]
		public void TestSetNullOnTimeSpanProperty()
		{
			Property prop = new Property();
			prop.TimeSpan = new TimeSpan(5,12,57,21,13) ;
			
			// Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "TimeSpan");
			propertyAccessor.Set(prop, null);
			Assert.AreEqual(TimeSpan.MinValue, prop.TimeSpan);
		}

		/// <summary>
		/// Test setting an TimeSpan property.
		/// </summary>
		[Test]
		public void TestSetTimeSpan()
		{
			Property prop = new Property();
			prop.TimeSpan = new TimeSpan(5,12,57,21,13) ;

			// Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "TimeSpan");
			TimeSpan test =  new TimeSpan(15,5,21,45,35) ;
			propertyAccessor.Set(prop, test);
			Assert.AreEqual(test, prop.TimeSpan);
		}

		/// <summary>
		/// Test getting an TimeSpan property.
		/// </summary>
		[Test]
		public void TestGetTimeSpan()
		{
			TimeSpan test = new TimeSpan(5,12,57,21,13) ;
			Property prop = new Property();
			prop.TimeSpan = test;

			// Property accessor
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "TimeSpan");
			Assert.AreEqual(test, propertyAccessor.Get(prop));
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Account");
			propertyAccessor.Set(prop, null);
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Account");

			Assert.AreEqual(HashCodeProvider.GetIdentityHashCode(test), HashCodeProvider.GetIdentityHashCode(prop.Account));

			Assert.AreEqual(test.FirstName, ((Account)propertyAccessor.Get(prop)).FirstName );
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
			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Account");
			Account test = new Account();
			test.FirstName = "Gilles";
			propertyAccessor.Set(prop, test);
			Assert.AreEqual(test.FirstName, prop.Account.FirstName);
		
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
            PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
            IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Day");
            propertyAccessor.Set(prop, null);
            //Assert.AreEqual(TimeSpan.MinValue, prop.TimeSpan);
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
            PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
            IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Day");
            Days test = Days.Wed;
            propertyAccessor.Set(prop, test);
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
            PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
            IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Day");
            Assert.AreEqual(test, propertyAccessor.Get(prop));
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
            PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
            IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "IntNullable");
            propertyAccessor.Set(prop, null);
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
            PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
            IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "IntNullable");

            Assert.AreEqual(test, propertyAccessor.Get(prop));
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
            PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
            IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "IntNullable");
            Int32? test = 55;
            propertyAccessor.Set(prop, test);
            Assert.AreEqual(test, prop.IntNullable);

        }
#endif
    }
}
