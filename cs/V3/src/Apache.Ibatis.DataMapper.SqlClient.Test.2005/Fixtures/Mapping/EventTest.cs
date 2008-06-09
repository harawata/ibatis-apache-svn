using System;
using Apache.Ibatis.Common.Resources;
using Apache.Ibatis.DataMapper.Configuration;
using Apache.Ibatis.DataMapper.Configuration.Interpreters.Config.Xml;
using Apache.Ibatis.DataMapper.MappedStatements;
using Apache.Ibatis.DataMapper.Model.Events;
using Apache.Ibatis.DataMapper.Model.Events.Listeners;
using Apache.Ibatis.DataMapper.Model.ResultMapping;
using Apache.Ibatis.DataMapper.Session;
using Apache.Ibatis.DataMapper.SqlClient.Test.Domain;
using Apache.Ibatis.DataMapper.SqlClient.Test.Fixtures.Modules;
using NUnit.Framework;
using NUnit.Framework.SyntaxHelpers;

namespace Apache.Ibatis.DataMapper.SqlClient.Test.Fixtures.Mapping
{
    [TestFixture]
    public class EventTest :ScriptBase 
    {
        private IDataMapper dataMapper = null;
        private ISessionFactory sessionFactory = null;

        [TestFixtureSetUp]
        public void SetUpFixture()
        {
            string uri = "file://~/SqlMap.event.config";
            IResource resource = ResourceLoaderRegistry.GetResource(uri);

            ConfigurationSetting setting = new ConfigurationSetting();
            IConfigurationEngine engine = new DefaultConfigurationEngine(setting);
            engine.RegisterInterpreter(new XmlConfigurationInterpreter(resource));
            engine.RegisterModule(new EventModule());

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
            InitScript(sessionFactory.DataSource, scriptDirectory + "account-init.sql");
            InitScript(sessionFactory.DataSource, scriptDirectory + "documents-init.sql");
        }


        private static void ReInitListeners(IResultMap resultMap)
        {
            resultMap.PreCreateEventListeners = new IResultMapEventListener<PreCreateEvent>[] { };
            resultMap.PostCreateEventListeners = new IResultMapEventListener<PostCreateEvent>[] { };
        }

        private static void ReInitListeners(ResultProperty resultProperty)
        {
            resultProperty.PrePropertyEventListeners = new IResultPropertyEventListener<PrePropertyEvent>[] { };
            resultProperty.PostPropertyEventListeners = new IResultPropertyEventListener<PostPropertyEvent>[] { };
        }


        [Test]
        public void PreSelectEvent_must_be_fired()
        {
            IMappedStatement statement = ((IModelStoreAccessor)dataMapper).ModelStore.GetMappedStatement("SelectAccount");
            Assert.That(statement, Is.Not.Null);
            statement.PreSelect+= PreSelectEventHandler;

            Account account = dataMapper.QueryForObject<Account>("SelectAccount", 1);
            Assert.That(account, Is.Not.Null);
            Assert.That(account.Id, Is.EqualTo(2));

            statement.PreSelect -= PreSelectEventHandler;
        }

        private static void PreSelectEventHandler(object src, PreSelectEventArgs evnt)
        {
            Assert.That(((IMappedStatement)src).Id, Is.EqualTo("SelectAccount"));
            evnt.ParameterObject = ((int)evnt.ParameterObject) +1;
        }


        [Test]
        public void PostSelectEventListener_must_be_fired()
        {
            IMappedStatement statement = ((IModelStoreAccessor)dataMapper).ModelStore.GetMappedStatement("SelectAccount");
            Assert.That(statement, Is.Not.Null);
            statement.PostSelect += PostSelectEventHandler;

            Account account = dataMapper.QueryForObject<Account>("SelectAccount", 1);
            Assert.That(account, Is.Not.Null);
            Assert.That(account.Id, Is.EqualTo(99));

            statement.PostSelect -= PostSelectEventHandler;
        }

        private static void PostSelectEventHandler(object src, PostSelectEventArgs evnt)
        {
            Assert.That(((IMappedStatement)src).Id, Is.EqualTo("SelectAccount"));
            Account account = (Account)evnt.ResultObject;
            account.Id = 99;
        }

        
        [Test]
        public void PreInsertEventListener_must_be_fired()
        {
            IMappedStatement statement = statement = ((IModelStoreAccessor)dataMapper).ModelStore.GetMappedStatement("InsertAccount");
            Assert.That(statement, Is.Not.Null);
            statement.PreInsert += PreInsertEventHandler;
            
            Account account = new Account();
            account.Id = 6;
            account.FirstName = "Calamity";
            account.LastName = "Jane";
            account.EmailAddress = "no_email@provided.com";

            dataMapper.Insert("InsertAccount", account);

            account = dataMapper.QueryForObject<Account>("SelectAccount", 6);

            Assert.That(account, Is.Not.Null);
            Assert.That(account.Id, Is.EqualTo(6));
            Assert.That(account.FirstName, Is.EqualTo("Calamity"));
            Assert.That(account.LastName, Is.EqualTo("Jane"));
            Assert.That(account.EmailAddress, Is.EqualTo("pre.insert.email@noname.org"));

            statement.PreInsert -= PreInsertEventHandler;
        }

        private static void PreInsertEventHandler(object src, PreInsertEventArgs evnt)
        {
            Assert.That(((IMappedStatement)src).Id, Is.EqualTo("InsertAccount"));
            Account account = (Account)evnt.ParameterObject;
            account.EmailAddress = "pre.insert.email@noname.org";
        }

        
        [Test]
        public void PostInsertEventListener_must_be_fired()
        {
            IMappedStatement statement = ((IModelStoreAccessor)dataMapper).ModelStore.GetMappedStatement("InsertAccount");
            Assert.That(statement, Is.Not.Null);
            statement.PostInsert += PostInsertEventHandler;

            Account account = new Account();
            account.Id = 6;
            account.FirstName = "Calamity";
            account.LastName = "Jane";
            account.EmailAddress = "no_email@provided.com";

            int id = (int)dataMapper.Insert("InsertAccount", account);

            Assert.That(id, Is.EqualTo(999));
            Assert.That(account.Id, Is.EqualTo(99));

            statement.PostInsert -= PostInsertEventHandler;

        }

        private static void PostInsertEventHandler(object src, PostInsertEventArgs evnt)
        {
            Assert.That(((IMappedStatement)src).Id, Is.EqualTo("InsertAccount"));
            Account account = (Account)evnt.ParameterObject;
            account.Id = 99;
            evnt.ResultObject = 999;
        }
      

        [Test]
        public void PreUpdateOrDeleteEventListener_must_be_fired()
        {
            IMappedStatement statement =  ((IModelStoreAccessor)dataMapper).ModelStore.GetMappedStatement("UpdateAccount");
            statement.PreUpdateOrDelete += PreUpdateOrDeleteEventHandler;
            
            Account account = dataMapper.QueryForObject<Account>("SelectAccount", 1);
            account.EmailAddress = "To.Be.Replace@noname.org";

            dataMapper.Update("UpdateAccount", account);

            account = dataMapper.QueryForObject<Account>("SelectAccount", 1);

            Assert.That(account, Is.Not.Null);
            Assert.That(account.Id, Is.EqualTo(1));
            Assert.That(account.EmailAddress, Is.EqualTo("Pre.Update.Or.Delete.Event@noname.org"));

            statement.PreUpdateOrDelete -= PreUpdateOrDeleteEventHandler;
        }

        private static void PreUpdateOrDeleteEventHandler(object src, PreUpdateOrDeleteEventArgs evnt)
        {
            Assert.That(((IMappedStatement)src).Id, Is.EqualTo("UpdateAccount"));
            Account account = (Account)evnt.ParameterObject;
            account.EmailAddress = "Pre.Update.Or.Delete.Event@noname.org";
        }

        [Test]
        public void PostUpdateOrDeleteEventListener_must_be_fired()
        {
            IMappedStatement statement = ((IModelStoreAccessor)dataMapper).ModelStore.GetMappedStatement("UpdateAccount");
            statement.PostUpdateOrDelete += PostUpdateOrDeleteEventHandler;

            Account account = dataMapper.QueryForObject<Account>("SelectAccount", 1);

            int id = dataMapper.Update("UpdateAccount", account);

            Assert.That(id, Is.EqualTo(999));
            Assert.That(account.Id, Is.EqualTo(99));

            statement.PostUpdateOrDelete -= PostUpdateOrDeleteEventHandler;

        }

        private static void PostUpdateOrDeleteEventHandler(object src, PostUpdateOrDeleteEventArgs evnt)
        {
            Assert.That(((IMappedStatement)src).Id, Is.EqualTo("UpdateAccount"));
            Account account = (Account)evnt.ParameterObject;
            account.Id = 99;
            evnt.ResultObject = 999;
        }

        /*
        [Test]
        public void PreCreateEventListener_must_be_fired()
        {
            IResultMap resultMap = ((IModelStoreAccessor)dataMapper).ModelStore.GetResultMap("Account.account-result-constructor");
            ReInitListeners(resultMap);
            resultMap.PreCreateEventListeners = new PreCreateEventListener[] { new MyPreCreateEventListener() };

            Account account = dataMapper.QueryForObject<Account>("SelectAccountViaConstructor", 1);

            Assert.That(account.Id, Is.EqualTo(1));
            Assert.That(account.LastName, Is.EqualTo("new lastName"));
        }

        [Test]
        public void PostCreateEventListener_must_be_fired()
        {
            IResultMap resultMap = ((IModelStoreAccessor)dataMapper).ModelStore.GetResultMap("Account.account-result-constructor");
            ReInitListeners(resultMap);
            resultMap.PostCreateEventListeners = new PostCreateEventListener[] { new MyPostCreateEventListener() };

            Account account = dataMapper.QueryForObject<Account>("SelectAccountViaConstructor", 1);

            Assert.That(account.Id, Is.EqualTo(1234));
            Assert.That(account.LastName, Is.EqualTo("New LastName"));
            Assert.That(account.FirstName, Is.EqualTo("New FirstName"));
        }

        [Test]
        public void PrePropertyEventListener_must_be_fired()
        {
            IResultMap resultMap = ((IModelStoreAccessor)dataMapper).ModelStore.GetResultMap("Account.account-result");
            ReInitListeners(resultMap);
            ResultProperty resultProperty = resultMap.Properties.FindByPropertyName("FirstName");
            ReInitListeners(resultProperty);

            resultProperty.PrePropertyEventListeners = new PrePropertyEventListener[] { new MyPrePropertyEventListener() };

            Account account = dataMapper.QueryForObject<Account>("SelectAccount", 1);

            Assert.That(account.Id, Is.EqualTo(1));
            Assert.That(account.FirstName, Is.EqualTo("No Name"));
            Assert.That(account.LastName, Is.EqualTo("Dalton"));

        }

        [Test]
        public void PostPropertyEventListener_must_be_fired()
        {
            IResultMap resultMap = ((IModelStoreAccessor)dataMapper).ModelStore.GetResultMap("Account.account-result-with-document");
            ReInitListeners(resultMap);
            ResultProperty resultProperty = resultMap.Properties.FindByPropertyName("Document");
            ReInitListeners(resultProperty);

            resultProperty.PostPropertyEventListeners = new PostPropertyEventListener[] { new MyPostPropertyEventListener() };

            Account account = dataMapper.QueryForObject<Account>("SelectAccountWithDocument", 1);
            Assert.That(account.Id, Is.EqualTo(1));
            Assert.That(account.Document, Is.Not.Null);
            Assert.That(account.Document.Id, Is.EqualTo(55));
        }
        

        private class MyPostSelectEventListener : PostSelectEventListener
        {

            /// <summary>
            /// Calls on the specified event.
            /// </summary>
            /// <param name="evnt">The event.</param>
            /// <returns>Returns is used as the result object</returns>
            public override object OnEvent(PostSelectEventArgs evnt)
            {
                Assert.That(evnt.MappedStatement.Id, Is.EqualTo("SelectAccount"));
                Account account = (Account)evnt.ResultObject;
                account.Id = 99;

                return account;
            }
        }

        private class MyPreInsertEventListener :PreInsertEventListener
        {

            /// <summary>
            /// Calls on the specified event.
            /// </summary>
            /// <param name="evnt">The event.</param>
            /// <returns>Returns is used as the parameter object</returns>
            public override object OnEvent(PreInsertEventArgs evnt)
            {
                Assert.That(evnt.MappedStatement.Id, Is.EqualTo("InsertAccount"));
                Account account = (Account)evnt.ParameterObject;
                account.EmailAddress = "pre.insert.email@noname.org";

                return account;
            }
        }

        private class MyPostInsertEventListener : PostInsertEventListener
        {

            /// <summary>
            /// Calls on the specified event.
            /// </summary>
            /// <param name="evnt">The event.</param>
            /// <returns>Returns is used as the result object</returns>
            public override object OnEvent(PostInsertEventArgs evnt)
            {
                Assert.That(evnt.MappedStatement.Id, Is.EqualTo("InsertAccount"));
                Account account = (Account)evnt.ParameterObject;
                account.Id = 99;
                return 999;
            }
        }

        private class MyPreUpdateOrDeleteEventListener : PreUpdateOrDeleteEventListener
        {

            /// <summary>
            /// Calls on the specified event.
            /// </summary>
            /// <param name="evnt">The event.</param>
            /// <returns>Returns is used as the parameter object</returns>
            public override object OnEvent(PreUpdateOrDeleteEventArgs evnt)
            {
                Assert.That(evnt.MappedStatement.Id, Is.EqualTo("UpdateAccount"));
                Account account = (Account)evnt.ParameterObject;
                account.EmailAddress = "Pre.Update.Or.Delete.Event@noname.org";

                return account;
            }
        }

        private class MyPostUpdateOrDeleteEventListener : PostUpdateOrDeleteEventListener
        {

            /// <summary>
            /// Calls on the specified event.
            /// </summary>
            /// <param name="evnt">The event.</param>
            /// <returns>Returns is used as the result object</returns>
            public override object OnEvent(PostUpdateOrDeleteEventArgs evnt)
            {
                Assert.That(evnt.MappedStatement.Id, Is.EqualTo("UpdateAccount"));
                Account account = (Account)evnt.ParameterObject;
                account.Id = 99;
                return 999;
            }
        }

        private class MyPreCreateEventListener : PreCreateEventListener
        {
            /// <summary>
            /// Calls before creating an instance of the <see cref="IResultMap"/> object.
            /// </summary>
            /// <param name="evnt">The event.</param>
            /// <returns>
            /// Returns is used as constructor arguments for the instance being created
            /// </returns>
            public override object OnEvent(PreCreateEvent evnt)
            {
                Assert.That(evnt.ResultMap.Id, Is.EqualTo("Account.account-result-constructor"));
                evnt.Parameters[evnt.Parameters.Length-1] = "new lastName";

                return evnt.Parameters;
            }
        }

        private class MyPostCreateEventListener : PostCreateEventListener
        {

            /// <summary>
            /// Calls after creating an instance of the <see cref="IResultMap"/> object.
            /// </summary>
            /// <param name="evnt">The event.</param>
            /// <returns>Returns is used as the instance object</returns>
            public override object OnEvent(PostCreateEvent evnt)
            {
                Assert.That(evnt.ResultMap.Id, Is.EqualTo("Account.account-result-constructor"));
                Account account = (Account)evnt.Instance;
                account.Id = 1234;
                account.FirstName = "New FirstName";
                account.LastName = "New LastName";

                return account;
            }
        }

        private class MyPrePropertyEventListener : PrePropertyEventListener
        {

            /// <summary>
            /// Calls before setting the property value in an instance of a <see cref="IResultMap"/> object.
            /// </summary>
            /// <param name="evnt">The event.</param>
            /// <returns>
            /// Returns is used as databse value, to be set on the property
            /// </returns>
            public override object OnEvent(PrePropertyEvent evnt)
            {
                Assert.That(evnt.ResultProperty.PropertyName, Is.EqualTo("FirstName"));
                return "No Name";
            }
        }

        private class MyPostPropertyEventListener : PostPropertyEventListener
        {
            /// <summary>
            /// Calls after creating an instance of the <see cref="IResultMap"/> object.
            /// </summary>
            /// <param name="evnt">The event.</param>
            /// <returns>Returns is not used</returns>
            public override object OnEvent(PostPropertyEvent evnt)
            {
                Assert.That(evnt.ResultProperty.PropertyName, Is.EqualTo("Document"));

                Account account = (Account) evnt.Target;

                Assert.That(account.Document, Is.Null);

                account.Document = new Document();
                account.Document.Id = 55;

                return null;
            }

        }
         */
    }
}
