using IBatisNet.Common.Test.Domain;
using IBatisNet.Common.Utilities.Objects;
using NUnit.Framework;
using System;

namespace IBatisNet.Common.Test.NUnit.CommonTests.Utilities
{

    [TestFixture] 
	public class ObjectFactoryTest
	{

		[Test]
		public void DynamicFactoryCreatesTypes()
		{
			IObjectFactory objectFactory = new ObjectFactory(true);

			IFactory factory = objectFactory.CreateFactory(typeof (Account));
			object obj = factory.CreateInstance();
			Assert.IsTrue(obj is Account);

			factory = objectFactory.CreateFactory(typeof (Account));
			obj = factory.CreateInstance();
			Assert.IsTrue(obj is Account);

			factory = objectFactory.CreateFactory(typeof (Simple));
			obj = factory.CreateInstance();
			Assert.IsTrue(obj is Simple);
		}

		[Test]
		public void CreateInstanceWithDifferentFactories()
		{
			const int TEST_ITERATIONS = 1000000;
			IFactory factory = null;

			#region new
			factory = new NewAccountFactory();

			// create an instance so that Activators can
			// cache the type/constructor/whatever
			factory.CreateInstance();

			GC.Collect();
			GC.WaitForPendingFinalizers();

			Timer timer = new Timer();
			timer.Start();
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				factory.CreateInstance();
			}
			timer.Stop();
			double newFactoryResult = 1000000 * (timer.Duration / (double)TEST_ITERATIONS);
			#endregion

			#region activator
			factory = new ActivatorObjectFactory().CreateFactory(typeof(Account));

			// create an instance so that Activators can
			// cache the type/constructor/whatever
			factory.CreateInstance();

			GC.Collect();
			GC.WaitForPendingFinalizers();

			timer.Start();
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				factory.CreateInstance();
			}
			timer.Stop();
			double activatorFactoryResult = 1000000 * (timer.Duration / (double)TEST_ITERATIONS);
			#endregion

			#region Emit
			factory = new EmitObjectFactory().CreateFactory(typeof(Account));

			// create an instance so that Activators can
			// cache the type/constructor/whatever
			factory.CreateInstance();

			GC.Collect();
			GC.WaitForPendingFinalizers();

			timer.Start();
			for (int i = 0; i < TEST_ITERATIONS; i++)
			{
				factory.CreateInstance();
			}
			timer.Stop();
			double emitFactoryResult = 1000000 * (timer.Duration / (double)TEST_ITERATIONS);
			#endregion

			// Print results
			Console.WriteLine(
				"Create " + TEST_ITERATIONS.ToString() + " objects via factory :"
				+ "\nNew : \t\t\t" + newFactoryResult.ToString("F3")
				+ "\nActivator : \t\t" + activatorFactoryResult.ToString("F3")+ " Ratio : " + ((activatorFactoryResult / newFactoryResult)).ToString("F3")
				+ "\nEmit IL : \t\t\t" + emitFactoryResult.ToString("F3") + " Ratio : " + ((emitFactoryResult / newFactoryResult)).ToString("F3"));

		}

		internal class NewAccountFactory : IFactory
		{
			public object CreateInstance()
			{
				return new Account();
			}
		}

	}
}
