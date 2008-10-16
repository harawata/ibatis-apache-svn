using System;
using System.Reflection;
using System.Threading;
using Apache.Ibatis.Common.Logging;
using Apache.Ibatis.DataMapper.Session;
using Apache.Ibatis.DataMapper.SqlClient.Test.Domain;
using Apache.Ibatis.DataMapper.SqlClient.Test.Fixtures;
using NUnit.Framework;

namespace Apache.Ibatis.DataMapper.SqlClient.Test.Fixtures.Mapping
{
    /// <summary>
    /// Summary description for TransactionTest.
    /// </summary>
    [TestFixture] 
    public class ThreadTest: BaseTest
    {
        private static readonly ILog _logger = LogManager.GetLogger( MethodBase.GetCurrentMethod().DeclaringType );

        private static readonly int numberOfThreads = 10;
        private readonly ManualResetEvent startEvent = new ManualResetEvent(false);
        private readonly ManualResetEvent stopEvent = new ManualResetEvent(false);

        #region SetUp & TearDown

        /// <summary>
        /// SetUp 
        /// </summary>
        [SetUp] 
        public void Init() 
        {
            InitScript(sessionFactory.DataSource, scriptDirectory + "account-init.sql");
        }

        /// <summary>
        /// TearDown
        /// </summary>
        [TearDown] 
        public void Dispose()
        { /* ... */ } 

        #endregion

        #region Thread test

        [Test]
        public void TestCommonUsageMultiThread()
        {
            const int threadCount = 10;

            Thread[] threads = new Thread[threadCount];
			
            for(int i = 0; i < threadCount; i++)
            {
                threads[i] = new Thread(new ThreadStart(ExecuteMethodUntilSignal));
                threads[i].Start();
            }

            startEvent.Set();

            Thread.CurrentThread.Join(1 * 2000);

            stopEvent.Set();
        }

        public void ExecuteMethodUntilSignal()
        {
            startEvent.WaitOne(int.MaxValue, false);

            while (!stopEvent.WaitOne(1, false))
            {
                Assert.IsNull(sessionStore.CurrentSession);

                Console.WriteLine("Begin Thread : " + Thread.CurrentThread.GetHashCode());

                Account account = (Account)dataMapper.QueryForObject("GetAccountViaColumnIndex", 1);

                Assert.IsNull(sessionStore.CurrentSession);

                Assert.AreEqual(1, account.Id, "account.Id");
                Assert.AreEqual("Joe", account.FirstName, "account.FirstName");
                Assert.AreEqual("Dalton", account.LastName, "account.LastName");

                Console.WriteLine("End Thread : " + Thread.CurrentThread.GetHashCode());
            }
        }

        /// <summary>
        /// Test BeginTransaction, CommitTransaction
        /// </summary>
        [Test] 
        public void TestThread() 
        {
            Account account = NewAccount6();

            try 
            {
                Thread[] threads = new Thread[numberOfThreads];

                AccessTest accessTest = new AccessTest();

                for (int i = 0; i < numberOfThreads; i++) 
                {
                    Thread thread = new Thread(new ThreadStart(accessTest.GetAccount));
                    threads[i] = thread;
                }
                for (int i = 0; i < numberOfThreads; i++) 
                {
                    threads[i].Start();
                }
            } 
            finally 
            {
            }

        }

        #endregion

        /// <summary>
        /// Summary description for AccessTest.
        /// </summary>
        private class AccessTest
        {
		
            /// <summary>
            /// Get an account
            /// </summary>
            public void GetAccount()
            {
                ISessionStore sessionStore = ((IModelStoreAccessor)dataMapper).ModelStore.SessionStore;

                Assert.IsNull(sessionStore.CurrentSession);

                Account account = (Account)dataMapper.QueryForObject("GetAccountViaColumnIndex", 1);

                Assert.IsNull(sessionStore.CurrentSession);

                Assert.AreEqual(1, account.Id, "account.Id");
                Assert.AreEqual("Joe", account.FirstName, "account.FirstName");
                Assert.AreEqual("Dalton", account.LastName, "account.LastName");
            }
        }	
    }
}