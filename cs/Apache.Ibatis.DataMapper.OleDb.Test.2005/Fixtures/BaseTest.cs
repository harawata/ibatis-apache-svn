using System;
using System.Collections;
using System.Configuration;
using System.IO;
using System.Reflection;
using Apache.Ibatis.Common.Data;
using Apache.Ibatis.Common.Logging;
using Apache.Ibatis.Common.Resources;
using Apache.Ibatis.Common.Utilities;
using Apache.Ibatis.DataMapper.Configuration;
using Apache.Ibatis.DataMapper.Configuration.Interpreters.Config.Xml;
using Apache.Ibatis.DataMapper.OleDb.Test.Fixtures.Modules;
using Apache.Ibatis.DataMapper.Session;
using NUnit.Framework;

namespace Apache.Ibatis.DataMapper.OleDb.Test.Fixtures
{
    [TestFixture]
    public abstract class BaseTest
    {
        protected static IDataMapper dataMapper = null;
        private static readonly ILog _logger = LogManager.GetLogger(MethodBase.GetCurrentMethod().DeclaringType);

        protected static string ScriptDirectory = null;
        protected static ISessionFactory sessionFactory = null;

        /// <summary>
        /// Constructor
        /// </summary>
        static BaseTest()
        {
            ScriptDirectory = Path.Combine(Path.Combine(Path.Combine(Resources.ApplicationBase, ".."), ".."), "Scripts") + Path.DirectorySeparatorChar;

        }

        /// <summary>
        /// Initialize an sqlMap
        /// </summary>
        [TestFixtureSetUp]
        protected virtual void SetUpFixture()
        {
            //DateTime start = DateTime.Now;

            ConfigurationSetting configurationSetting = new ConfigurationSetting();
            configurationSetting.Properties.Add("collection2Namespace", "Apache.Ibatis.DataMapper.SqlClient.Test.Domain.LineItemCollection2, Apache.Ibatis.DataMapper.SqlClient.Test");
            configurationSetting.Properties.Add("nullableInt", "int?");

            string resource = "sqlmap.config";

            try
            {
                IConfigurationEngine engine = new DefaultConfigurationEngine(configurationSetting);
                engine.RegisterInterpreter(new XmlConfigurationInterpreter(resource));
                engine.RegisterModule(new AliasModule());

                IMapperFactory mapperFactory = engine.BuildMapperFactory();
                dataMapper = ((IDataMapperAccessor)mapperFactory).DataMapper;

                sessionFactory = engine.ModelStore.SessionFactory;
            }
            catch (Exception ex)
            {
                Exception e = ex;
                while (e != null)
                {
                    Console.WriteLine(e.Message);
                    Console.WriteLine(e.StackTrace);
                    e = e.InnerException;

                }
                throw;
            }
        }

        /// <summary>
        /// Dispose the SqlMap
        /// </summary>
        [TestFixtureTearDown]
        protected virtual void TearDownFixture()
        {
            dataMapper = null;
        }
    }
}
