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

            // IL Property accessor
        	IPropertyAccessor propertyAccessor = ILPropertyAccessor.CreatePropertyAccessor(typeof(Account), "Id");

			long time = DateTime.Now.Ticks;
            for (int i = 0; i < TEST_ITERATIONS; i++)
            {
                test = -1;
                test = (int)propertyAccessor.Get(account);
                Assert.AreEqual(0, test);
            }
            long propertyAccessorMs = DateTime.Now.Ticks - time;

            // Direct access
            time = DateTime.Now.Ticks;
            for (int i = 0; i < TEST_ITERATIONS; i++)
            {
                test = -1;
                test = account.Id;
                Assert.AreEqual(0, test);
            }
            long directAccessMs = DateTime.Now.Ticks - time;

            // Reflection
            Type type = account.GetType();
            time = DateTime.Now.Ticks;
            for (int i = 0; i < TEST_ITERATIONS; i++)
            {
                test = -1;
                test = (int)type.InvokeMember("Id",
                                              BindingFlags.Public | BindingFlags.GetProperty | BindingFlags.Instance,
                    null, account, null);
                Assert.AreEqual(0, test);
            }
            long reflectionMs = DateTime.Now.Ticks - time;

            // Print results
            Console.WriteLine(
                TEST_ITERATIONS.ToString() + " property gets on integer..."
                + "\nDirect access : \t\t" + directAccessMs.ToString() + " ms"
                + "\nIPropertyAccessor : \t\t" + propertyAccessorMs.ToString()+ " ms Ratio: " + (((float)propertyAccessorMs / directAccessMs)).ToString()
                + "\nReflection : \t\t\t" + reflectionMs.ToString() + " ms Ratio: " + (((float)reflectionMs / directAccessMs)).ToString());
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

            // Property accessor
            IPropertyAccessor propertyAccessor = ILPropertyAccessor.CreatePropertyAccessor(typeof(Account), "Id");
            long start = DateTime.Now.Ticks;
            for (int i = 0; i < TEST_ITERATIONS; i++)
            {
                propertyAccessor.Set(account, value);
            }
            long propertyAccessorMs = DateTime.Now.Ticks - start;

            // Direct access
            start = DateTime.Now.Ticks;
            for (int i = 0; i < TEST_ITERATIONS; i++)
            {
                account.Id = value;
            }
            long directAccessMs = DateTime.Now.Ticks - start;

            // Reflection
            Type type = account.GetType();
            start = DateTime.Now.Ticks;
            for (int i = 0; i < TEST_ITERATIONS; i++)
            {
                type.InvokeMember("Id",
                    BindingFlags.Public | BindingFlags.SetProperty | BindingFlags.Instance,
                    null, account, new object[] { value });
            }
            long reflectionMs = DateTime.Now.Ticks - start;
            
            // Print results
            Console.WriteLine(
                TEST_ITERATIONS.ToString() + " property sets on integer..."
                + "\nDirect access : \t\t" + directAccessMs.ToString() + " ms"
                + "\nPropertyAccessor : \t\t" + propertyAccessorMs.ToString() + " ms Ratio: " + (((float)propertyAccessorMs / directAccessMs)).ToString()
                + "\nReflection : \t\t\t" +  reflectionMs +" ms Ratio: " + (((float)reflectionMs / directAccessMs)).ToString());
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
