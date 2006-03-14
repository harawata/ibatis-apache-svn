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
		/// Test integer property access performance
		/// </summary>
        [Test]
        public void TestGetIntegerPerformance()
        {
            const int TEST_ITERATIONS = 1000000;
			Property prop = new Property();
			int test = -1;
			Timer timer = new Timer();

			#region Direct access (fastest)
			GC.Collect();
			GC.WaitForPendingFinalizers();

			timer.Start();
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				test = -1;
				test = prop.Int;
				Assert.AreEqual(int.MinValue, test);
			}
			timer.Stop();
			double directAccessDuration = 1000000 * (timer.Duration / (double)TEST_ITERATIONS);
			#endregion

			#region IL Property accessor
			GC.Collect();
			GC.WaitForPendingFinalizers();

			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Int");
			timer.Start();
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				test = -1;
				test = (int)propertyAccessor.Get(prop);
				Assert.AreEqual(int.MinValue, test);
			}
			timer.Stop();
			double propertyAccessorDuration = 1000000 * (timer.Duration / (double)TEST_ITERATIONS);
			double propertyAccessorRatio = propertyAccessorDuration / directAccessDuration;
			#endregion

			#region IBatisNet.Common.Utilities.Object.ReflectionInfo
			GC.Collect();
			GC.WaitForPendingFinalizers();

			ReflectionInfo reflectionInfo = ReflectionInfo.GetInstance(prop.GetType());
			timer.Start();
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				test = -1;
				PropertyInfo propertyInfo = reflectionInfo.GetGetter("Int");
				test = (int)propertyInfo.GetValue(prop, null);
				Assert.AreEqual(int.MinValue, test);
			}
			timer.Stop();
			double reflectionInfoDuration = 1000000 * (timer.Duration / (double)TEST_ITERATIONS);
			double reflectionInfoRatio = (float)reflectionInfoDuration / directAccessDuration;
			#endregion

			#region Reflection
			GC.Collect();
			GC.WaitForPendingFinalizers();

			Type type = prop.GetType();
			timer.Start();
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				test = -1;
				PropertyInfo propertyInfo = type.GetProperty("Int", BindingFlags.Public | BindingFlags.SetProperty | BindingFlags.Instance);
				test = (int)propertyInfo.GetValue(prop, null);
				Assert.AreEqual(int.MinValue, test);
			}
			timer.Stop();
			double reflectionDuration = 1000000 * (timer.Duration / (double)TEST_ITERATIONS);
			double reflectionRatio = reflectionDuration / directAccessDuration;
			#endregion

			#region ReflectionInvokeMember (slowest)
			GC.Collect();
			GC.WaitForPendingFinalizers();

			timer.Start();
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				test = -1;
				test = (int)type.InvokeMember("Int",
					BindingFlags.Public | BindingFlags.GetProperty | BindingFlags.Instance,
					null, prop, null);
				Assert.AreEqual(int.MinValue, test);
			}
			timer.Stop();
			double reflectionInvokeMemberDuration = 1000000 * (timer.Duration / (double)TEST_ITERATIONS);
			double reflectionInvokeMemberRatio = reflectionInvokeMemberDuration / directAccessDuration;
			#endregion

			// Print results
			Console.WriteLine("{0} property gets on integer...", TEST_ITERATIONS);
			Console.WriteLine("Direct access: \t\t{0} ", directAccessDuration.ToString("F3"));
			Console.WriteLine("IPropertyAccessor: \t\t{0} Ratio: {1}", propertyAccessorDuration.ToString("F3"), propertyAccessorRatio.ToString("F3"));
			Console.WriteLine("IBatisNet ReflectionInfo: \t{0} Ratio: {1}", reflectionInfoDuration.ToString("F3"), reflectionInfoRatio.ToString("F3"));
			Console.WriteLine("ReflectionInvokeMember: \t{0} Ratio: {1}", reflectionInvokeMemberDuration.ToString("F3"), reflectionInvokeMemberRatio.ToString("F3"));
			Console.WriteLine("Reflection: \t\t\t{0} Ratio: {1}", reflectionDuration.ToString("F3"), reflectionRatio.ToString("F3"));
        }
        

		/// <summary>
        /// Test the performance of getting an integer property.
        /// </summary>
        [Test]
        public void TestSetIntegerPerformance()
        {
            const int TEST_ITERATIONS = 1000000;
			Property prop = new Property();
			int value = 123;
            Timer timer = new Timer();

			#region Direct access (fastest)
			GC.Collect();
			GC.WaitForPendingFinalizers();

            timer.Start();
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				prop.Int = value;
			}
            timer.Stop();
            double directAccessDuration = 1000000 * (timer.Duration / (double)TEST_ITERATIONS);
			#endregion

			#region Property accessor
			GC.Collect();
			GC.WaitForPendingFinalizers();

			PropertyAccessorFactory factory = new PropertyAccessorFactory(true);
			IPropertyAccessor propertyAccessor = factory.CreatePropertyAccessor(typeof(Property), "Int");
			timer.Start();
            for (int i = 0; i < TEST_ITERATIONS; i++)
            {
                propertyAccessor.Set(prop, value);
            }
            timer.Stop();
            double propertyAccessorDuration = 1000000 * (timer.Duration / (double)TEST_ITERATIONS);
            double propertyAccessorRatio = propertyAccessorDuration / directAccessDuration;
			#endregion

			#region IBatisNet.Common.Utilities.Object.ReflectionInfo
			GC.Collect();
			GC.WaitForPendingFinalizers();

			Type type = prop.GetType();
			ReflectionInfo reflectionInfo = ReflectionInfo.GetInstance(type);
            timer.Start();
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				PropertyInfo propertyInfo = reflectionInfo.GetSetter("Int");
				propertyInfo.SetValue(prop, value, null);
			}
            timer.Stop();
            double reflectionInfoDuration = 1000000 * (timer.Duration / (double)TEST_ITERATIONS);
            double reflectionInfoRatio = reflectionInfoDuration / directAccessDuration;
			#endregion

			#region Reflection
			GC.Collect();
			GC.WaitForPendingFinalizers();

            timer.Start();
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				PropertyInfo propertyInfo = type.GetProperty("Int", BindingFlags.Public | BindingFlags.SetProperty | BindingFlags.Instance);
				propertyInfo.SetValue(prop, value, null);
			}
            timer.Stop();
            double reflectionDuration = 1000000 * (timer.Duration / (double)TEST_ITERATIONS);
            double reflectionRatio = reflectionDuration / directAccessDuration;
			#endregion

			#region ReflectionInvokeMember (slowest)
			GC.Collect();
			GC.WaitForPendingFinalizers();

            timer.Start();
            for (int i = 0; i < TEST_ITERATIONS; i++)
            {
                type.InvokeMember("Int",
                    BindingFlags.Public | BindingFlags.SetProperty | BindingFlags.Instance,
                    null, prop, new object[] { value });
            }
            timer.Stop();
            double reflectionInvokeMemberDuration = 1000000 * (timer.Duration / (double)TEST_ITERATIONS);
            double reflectionInvokeMemberRatio = reflectionInvokeMemberDuration / directAccessDuration;
			#endregion
            
			// Print results
			Console.WriteLine("{0} property sets on integer...", TEST_ITERATIONS);
            Console.WriteLine("Direct access: \t\t{0} ", directAccessDuration.ToString("F3"));
            Console.WriteLine("IPropertyAccessor: \t\t{0} Ratio: {1}", propertyAccessorDuration.ToString("F3"), propertyAccessorRatio.ToString("F3"));
            Console.WriteLine("IBatisNet ReflectionInfo: \t{0} Ratio: {1}", reflectionInfoDuration.ToString("F3"), reflectionInfoRatio.ToString("F3"));
            Console.WriteLine("ReflectionInvokeMember: \t{0} Ratio: {1}", reflectionInvokeMemberDuration.ToString("F3"), reflectionInvokeMemberRatio.ToString("F3"));
            Console.WriteLine("Reflection: \t\t\t{0} Ratio: {1}", reflectionDuration.ToString("F3"), reflectionRatio.ToString("F3"));
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
    }
}
