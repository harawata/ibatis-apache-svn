using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using Apache.Ibatis.Common.Data;
using Apache.Ibatis.Common.Resources;
using Apache.Ibatis.Common.Utilities;
using Apache.Ibatis.DataMapper.Configuration;
using Apache.Ibatis.DataMapper.Configuration.Interpreters.Config.Xml;
using Apache.Ibatis.DataMapper.Session;
using Apache.Ibatis.DataMapper.SqlClient.Test.Domain;
using Apache.Ibatis.DataMapper.SqlClient.Test.Fixtures.Modules;
using NUnit.Framework;
using NUnit.Framework.SyntaxHelpers;

namespace Apache.Ibatis.DataMapper.SqlClient.Test.Fixtures.Mapping
{
    [TestFixture]
    public class VelocityTest
    {
        private IDataMapper dataMapper = null;
        protected static ISessionFactory sessionFactory = null;

        [TestFixtureSetUp]
        public void SetUpFixture()
        {
            string uri = "file://~/SqlMap.velocity.config";
            IResource resource = ResourceLoaderRegistry.GetResource(uri);

            ConfigurationSetting setting = new ConfigurationSetting();

            IConfigurationEngine engine = new DefaultConfigurationEngine(setting);
            engine.RegisterInterpreter(new XmlConfigurationInterpreter(resource));
            engine.RegisterModule(new AliasModule());

            IMapperFactory mapperFactory = engine.BuildMapperFactory();
            sessionFactory = engine.ModelStore.SessionFactory;
            dataMapper = ((IDataMapperAccessor)mapperFactory).DataMapper;
        }

         /// <summary>
        /// SetUp
        /// </summary>
        [SetUp]
        public void SetUp()
        {
            string scriptDirectory = Path.Combine(Path.Combine(Path.Combine(Resources.ApplicationBase, ".."), ".."), "Scripts") + Path.DirectorySeparatorChar;

            InitScript(sessionFactory.DataSource, scriptDirectory + "account-init.sql");
            InitScript(sessionFactory.DataSource, scriptDirectory + "ps_InsertAccountWithDefault.sql");
        }

        [Test]
        public void Nvelocity_simple_sql_template_should_work()
        {
            Account paramAccount = new Account();
            paramAccount.Id = 1;

            IDictionary<string, object> parameters = new Dictionary<string, object>();
            parameters.Add("account", paramAccount);

            Account account = dataMapper.QueryForObject<Account>("NVelocity.Simple", parameters);
            Assert.That(account, Is.Not.Null);
            AssertAccount1(account);
        }

        [Test]
        public void Nvelocity_template_with_if_should_work()
        {
            Account paramAccount = new Account();
            paramAccount.FirstName = "Joe";
            paramAccount.Id = 1;

            IDictionary<string, object> parameters = new Dictionary<string, object>();
            parameters.Add("account", paramAccount);

            Account account = dataMapper.QueryForObject<Account>("NVelocity.If", parameters);
            Assert.That(account.FirstName, Is.EqualTo("Joe"));
            Assert.That(account.LastName, Is.Null);

            paramAccount = new Account();
            paramAccount.LastName = "Dalton";
            paramAccount.Id = 1;

            parameters = new Dictionary<string, object>();
            parameters.Add("account", paramAccount);

            account = dataMapper.QueryForObject<Account>("NVelocity.If", parameters);
            Assert.That(account.LastName, Is.EqualTo("Dalton"));
            Assert.That(account.FirstName, Is.Null);

        }

        [Test]
        public void Nvelocity_inline_parameter_should_work()
        {
            IDictionary<string, object> parameters = new Dictionary<string, object>();
            parameters.Add("LastName", "Dalton");

            IList<Account> accounts = dataMapper.QueryForList<Account>("NVelocity.InlineParameter", parameters);
            Assert.That(accounts.Count, Is.EqualTo(4));

            parameters = new Dictionary<string, object>();
            parameters.Add("LastName", "xxx");

            accounts = dataMapper.QueryForList<Account>("NVelocity.InlineParameter", parameters);
            Assert.That(accounts.Count, Is.EqualTo(5));
        }

        [Test]
        public void Nvelocity_Iterate_should_work()
        {
            List<int> integers = new List<int>();
            integers.Add(1);
            integers.Add(2);
            integers.Add(3);

            IDictionary<string, object> parameters = new Dictionary<string, object>();
            parameters.Add("integers", integers);

            IList<Account> accounts = dataMapper.QueryForList<Account>("NVelocity.For", parameters);
            Assert.That(accounts.Count, Is.EqualTo(3));
            AssertAccount1(accounts[0]);
        }

        [Test]
        public void Nvelocity_procedure_with_dynamic_parameter_should_work()
        {
            Account account = new Account();

            account.Id = 99;
            account.FirstName = "Achille";
            account.LastName = "Talon";
            account.NullBannerOption = false;

            IDictionary<string, object> parameters = new Dictionary<string, object>();
            parameters.Add("account", account);

            dataMapper.Insert("NVelocity.Procedure", parameters);

            Account testAccount = dataMapper.QueryForObject<Account>("NVelocity.Simple", parameters);

            Assert.IsNotNull(testAccount);
            Assert.That(testAccount.Id, Is.EqualTo(99));
            Assert.That(testAccount.EmailAddress, Is.EqualTo("no_email@provided.com"));
            Assert.That(testAccount.BannerOption, Is.False);
            Assert.That(testAccount.CartOption, Is.False);

            account.Id = 100;
            account.FirstName = "Achille";
            account.LastName = "Talon";
            account.NullBannerOption = true;
            account.CartOption = true;

            parameters = new Dictionary<string, object>();
            parameters.Add("account", account);

            dataMapper.Insert("NVelocity.Procedure", parameters);

            testAccount = dataMapper.QueryForObject<Account>("NVelocity.Simple", parameters);

            Assert.IsNotNull(testAccount);
            Assert.That(testAccount.Id, Is.EqualTo(100));
            Assert.That(testAccount.EmailAddress, Is.EqualTo("no_email@provided.com"));
            Assert.That(testAccount.BannerOption, Is.True);
            Assert.That(testAccount.CartOption, Is.True);
        }

        /// <summary>
        /// Verify that the input account is equal to the account(id=1).
        /// </summary>
        /// <param name="account">An account object</param>
        private void AssertAccount1(Account account)
        {
            Assert.AreEqual(1, account.Id, "account.Id");
            Assert.AreEqual("Joe", account.FirstName, "account.FirstName");
            Assert.AreEqual("Dalton", account.LastName, "account.LastName");
            Assert.AreEqual("Joe.Dalton@somewhere.com", account.EmailAddress, "account.EmailAddress");
        }

        /// <summary>
        /// Run a sql batch for the datasource.
        /// </summary>
        /// <param name="datasource">The datasource.</param>
        /// <param name="script">The sql batch</param>
        public static void InitScript(IDataSource datasource, string script)
        {
            InitScript(datasource, script, true);
        }

        /// <summary>
        /// Run a sql batch for the datasource.
        /// </summary>
        /// <param name="datasource">The datasource.</param>
        /// <param name="script">The sql batch</param>
        /// <param name="doParse">parse out the statements in the sql script file.</param>
        private static void InitScript(IDataSource datasource, string script, bool doParse)
        {
            ScriptRunner runner = new ScriptRunner();

            runner.RunScript(datasource, script, doParse);
        }

    }
}
