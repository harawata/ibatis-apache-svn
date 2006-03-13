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
			Timer timer = new Timer();

			#region Direct access (fastest)
			GC.Collect();
			GC.WaitForPendingFinalizers();

			timer.Start();
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				test = -1;
				test = account.Id;
				Assert.AreEqual(0, test);
			}
			timer.Stop();
			double directAccessDuration = 1000000 * (timer.Duration / (double)TEST_ITERATIONS);
			#endregion

			#region IL Property accessor
			GC.Collect();
			GC.WaitForPendingFinalizers();

			IPropertyAccessor propertyAccessor = ILPropertyAccessor.CreatePropertyAccessor(typeof(Account), "Id");
			timer.Start();
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				test = -1;
				test = (int)propertyAccessor.Get(account);
				Assert.AreEqual(0, test);
			}
			timer.Stop();
			double propertyAccessorDuration = 1000000 * (timer.Duration / (double)TEST_ITERATIONS);
			double propertyAccessorRatio = propertyAccessorDuration / directAccessDuration;
			#endregion

			#region IBatisNet.Common.Utilities.Object.ReflectionInfo
			GC.Collect();
			GC.WaitForPendingFinalizers();

			ReflectionInfo reflectionInfo = ReflectionInfo.GetInstance(account.GetType());
			timer.Start();
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				test = -1;
				PropertyInfo propertyInfo = reflectionInfo.GetGetter("Id");
				test = (int)propertyInfo.GetValue(account, null);
				Assert.AreEqual(0, test);
			}
			timer.Stop();
			double reflectionInfoDuration = 1000000 * (timer.Duration / (double)TEST_ITERATIONS);
			double reflectionInfoRatio = (float)reflectionInfoDuration / directAccessDuration;
			#endregion

			#region Reflection
			GC.Collect();
			GC.WaitForPendingFinalizers();

			Type type = account.GetType();
			timer.Start();
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				test = -1;
				PropertyInfo propertyInfo = type.GetProperty("Id", BindingFlags.Public | BindingFlags.SetProperty | BindingFlags.Instance);
				test = (int)propertyInfo.GetValue(account, null);
				Assert.AreEqual(0, test);
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
				test = (int)type.InvokeMember("Id",
					BindingFlags.Public | BindingFlags.GetProperty | BindingFlags.Instance,
					null, account, null);
				Assert.AreEqual(0, test);
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
            Account account = new Account();
            int value = 123;
            Timer timer = new Timer();

			#region Direct access (fastest)
			GC.Collect();
			GC.WaitForPendingFinalizers();

            timer.Start();
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				account.Id = value;
			}
            timer.Stop();
            double directAccessDuration = 1000000 * (timer.Duration / (double)TEST_ITERATIONS);
			#endregion

			#region Property accessor
			GC.Collect();
			GC.WaitForPendingFinalizers();

            IPropertyAccessor propertyAccessor = ILPropertyAccessor.CreatePropertyAccessor(typeof(Account), "Id");
            timer.Start();
            for (int i = 0; i < TEST_ITERATIONS; i++)
            {
                propertyAccessor.Set(account, value);
            }
            timer.Stop();
            double propertyAccessorDuration = 1000000 * (timer.Duration / (double)TEST_ITERATIONS);
            double propertyAccessorRatio = propertyAccessorDuration / directAccessDuration;
			#endregion

			#region IBatisNet.Common.Utilities.Object.ReflectionInfo
			GC.Collect();
			GC.WaitForPendingFinalizers();

			Type type = account.GetType();
			ReflectionInfo reflectionInfo = ReflectionInfo.GetInstance(type);
            timer.Start();
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				PropertyInfo propertyInfo = reflectionInfo.GetSetter("Id");
				propertyInfo.SetValue(account, value, null);
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
				PropertyInfo propertyInfo = type.GetProperty("Id", BindingFlags.Public | BindingFlags.SetProperty | BindingFlags.Instance);
				propertyInfo.SetValue(account, value, null);
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
                type.InvokeMember("Id",
                    BindingFlags.Public | BindingFlags.SetProperty | BindingFlags.Instance,
                    null, account, new object[] { value });
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
            Account account = new Account();
            account.Id = -99;

            // Property accessor
            IPropertyAccessor propertyAccessor = ILPropertyAccessor.CreatePropertyAccessor(typeof(Account), "Id");
            propertyAccessor.Set(account, null);
            Assert.IsTrue(account.Id == 0);
        }
    }
}
