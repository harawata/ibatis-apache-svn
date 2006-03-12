using System;
using System.Reflection;
using IBatisNet.Common.Test.Domain;
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
		/// Test integer property access performance
		/// </summary>
        [Test]
        public void TestGetIntegerPerformance()
        {
            const int TEST_ITERATIONS = 1000000;
        	Account account = new Account();
            int test = -1;

			#region Direct access (fastest)
			long time = DateTime.Now.Ticks;
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				test = -1;
				test = account.Id;
				Assert.AreEqual(0, test);
			}
			long directAccessMs = DateTime.Now.Ticks - time;
			#endregion

			#region IL Property accessor
        	IPropertyAccessor propertyAccessor = ILPropertyAccessor.CreatePropertyAccessor(typeof(Account), "Id");
			time = DateTime.Now.Ticks;
            for (int i = 0; i < TEST_ITERATIONS; i++)
            {
                test = -1;
                test = (int)propertyAccessor.Get(account);
                Assert.AreEqual(0, test);
            }
            long propertyAccessorMs = DateTime.Now.Ticks - time;
			float propertyAccessorRatio = (float)propertyAccessorMs / directAccessMs;
			#endregion

			#region IBatisNet.Common.Utilities.Object.ReflectionInfo
			ReflectionInfo reflectionInfo = ReflectionInfo.GetInstance(account.GetType());
			time = DateTime.Now.Ticks;
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				test = -1;
				PropertyInfo propertyInfo = reflectionInfo.GetGetter("Id");
				test = (int)propertyInfo.GetValue(account, null);
				Assert.AreEqual(0, test);
			}
			long reflectionInfoMs = DateTime.Now.Ticks - time;
			float reflectionInfoRatio = (float)reflectionInfoMs / directAccessMs;
			#endregion

			#region Reflection
			Type type = account.GetType();
			time = DateTime.Now.Ticks;
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				test = -1;
				PropertyInfo propertyInfo = type.GetProperty("Id", BindingFlags.Public | BindingFlags.SetProperty | BindingFlags.Instance);
				test = (int)propertyInfo.GetValue(account, null);
				Assert.AreEqual(0, test);
			}
			long reflectionMs = DateTime.Now.Ticks - time;
			float reflectionRatio = (float)reflectionMs / directAccessMs;
			#endregion

			#region ReflectionInvokeMember (slowest)
			type = account.GetType();
			time = DateTime.Now.Ticks;
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				test = -1;
				test = (int)type.InvokeMember("Id",
					BindingFlags.Public | BindingFlags.GetProperty | BindingFlags.Instance,
					null, account, null);
				Assert.AreEqual(0, test);
			}
			long reflectionInvokeMemberMs = DateTime.Now.Ticks - time;
			float reflectionInvokeMemberRatio = (float)reflectionInvokeMemberMs / directAccessMs;
			#endregion

			// Print results
			Console.WriteLine("{0} property gets on integer...", TEST_ITERATIONS);
			Console.WriteLine("Direct access: \t\t{0} ms", directAccessMs);
			Console.WriteLine("IPropertyAccessor: \t\t{0} ms Ratio: {1}", propertyAccessorMs, propertyAccessorRatio);
			Console.WriteLine("IBatisNet ReflectionInfo: \t{0} ms Ratio: {1}", reflectionInfoMs, reflectionInfoRatio);
			Console.WriteLine("ReflectionInvokeMember: \t{0} ms Ratio: {1}", reflectionInvokeMemberMs, reflectionInvokeMemberRatio);
			Console.WriteLine("Reflection: \t\t\t{0} ms Ratio: {1}", reflectionMs, reflectionRatio);
        }
        
		/// <summary>
        /// Test the performance of getting an integer property.
        /// </summary>
        [Test]
        public void TestSetIntegerPerformance()
        {
            const int TEST_ITERATIONS = 1000000;
            Account account = new Account();
            int value = 123;

			#region Direct access (fastest)
			long start = DateTime.Now.Ticks;
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				account.Id = value;
			}
			long directAccessMs = DateTime.Now.Ticks - start;
			#endregion

			#region Property accessor
            IPropertyAccessor propertyAccessor = ILPropertyAccessor.CreatePropertyAccessor(typeof(Account), "Id");
            start = DateTime.Now.Ticks;
            for (int i = 0; i < TEST_ITERATIONS; i++)
            {
                propertyAccessor.Set(account, value);
            }
            long propertyAccessorMs = DateTime.Now.Ticks - start;
			float propertyAccessorRatio = (float)propertyAccessorMs / directAccessMs;
			#endregion

			#region IBatisNet.Common.Utilities.Object.ReflectionInfo
			Type type = account.GetType();
			ReflectionInfo reflectionInfo = ReflectionInfo.GetInstance(type);
			start = DateTime.Now.Ticks;
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				PropertyInfo propertyInfo = reflectionInfo.GetSetter("Id");
				propertyInfo.SetValue(account, value, null);
			}
			long reflectionInfoMs = DateTime.Now.Ticks - start;
			float reflectionInfoRatio = (float)reflectionInfoMs / directAccessMs;
			#endregion

			#region Reflection
			type = account.GetType();
			start = DateTime.Now.Ticks;
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				PropertyInfo propertyInfo = type.GetProperty("Id", BindingFlags.Public | BindingFlags.SetProperty | BindingFlags.Instance);
				propertyInfo.SetValue(account, value, null);
			}
			long reflectionMs = DateTime.Now.Ticks - start;
			float reflectionRatio = (float)reflectionMs / directAccessMs;
			#endregion

			#region ReflectionInvokeMember (slowest)
            type = account.GetType();
            start = DateTime.Now.Ticks;
            for (int i = 0; i < TEST_ITERATIONS; i++)
            {
                type.InvokeMember("Id",
                    BindingFlags.Public | BindingFlags.SetProperty | BindingFlags.Instance,
                    null, account, new object[] { value });
            }
            long reflectionInvokeMemberMs = DateTime.Now.Ticks - start;
			float reflectionInvokeMemberRatio = (float)reflectionInvokeMemberMs / directAccessMs;
			#endregion
            
			// Print results
			Console.WriteLine("{0} property sets on integer...", TEST_ITERATIONS);
			Console.WriteLine("Direct access: \t\t{0} ms", directAccessMs);
			Console.WriteLine("IPropertyAccessor: \t\t{0} ms Ratio: {1}", propertyAccessorMs, propertyAccessorRatio);
			Console.WriteLine("IBatisNet ReflectionInfo: \t{0} ms Ratio: {1}", reflectionInfoMs, reflectionInfoRatio);
			Console.WriteLine("ReflectionInvokeMember: \t{0} ms Ratio: {1}", reflectionInvokeMemberMs, reflectionInvokeMemberRatio);
			Console.WriteLine("Reflection: \t\t\t{0} ms Ratio: {1}", reflectionMs, reflectionRatio);
        }
       
		/// <summary>
        /// Test the performance of getting an integer property.
        /// </summary>
        [Test]
        public void TestSetNullOnIntegerProperty()
        {
            Account account = new Account();
            account.Id = -99;

            // Property accessor
            IPropertyAccessor propertyAccessor = ILPropertyAccessor.CreatePropertyAccessor(typeof(Account), "Id");
            propertyAccessor.Set(account, null);
            Assert.IsTrue(account.Id == 0);
        }
    }
}
