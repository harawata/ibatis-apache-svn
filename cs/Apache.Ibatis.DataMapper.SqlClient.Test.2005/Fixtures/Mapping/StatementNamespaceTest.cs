using System;
using Apache.Ibatis.DataMapper.SqlClient.Test.Domain;
using NUnit.Framework;

namespace Apache.Ibatis.DataMapper.SqlClient.Test.Fixtures.Mapping
{
    /// <summary>
    /// To test statement namespaces,
    /// set your SqlMap config settings attribute 
    /// useStatementNamespaces="true" 
    /// before running Namespace Tests.
    /// </summary>
    [TestFixture] 
    [Category("StatementNamespaces")]
    public class StatementNamespaceTest : BaseTest
    {
        #region SetUp & TearDown

        /// <summary>
        /// SetUp
        /// </summary>
        [SetUp] 
        public void Init() 
        {
            InitScript( sessionFactory.DataSource, ScriptDirectory + "account-init.sql" );
            InitScript( sessionFactory.DataSource, ScriptDirectory + "order-init.sql" );
            InitScript( sessionFactory.DataSource, ScriptDirectory + "line-item-init.sql" );
            InitScript( sessionFactory.DataSource, ScriptDirectory + "category-init.sql" );
        }

        /// <summary>
        /// TearDown
        /// </summary>
        [TearDown] 
        public void Dispose()
        { /* ... */ } 

        #endregion

        #region Test statement namespaces

        /// <summary>
        /// Test QueryForObject
        /// </summary>
        [Test] 
        public void TestQueryForObject() {
            Account account = dataMapper.QueryForObject("Account.GetAccountViaResultClass", 1) as Account;
            AssertAccount1(account);
        }

        /// <summary>
        /// Test collection mapping: Ilist collection 
        /// order.LineItemsIList 
        /// with statement namespaces enabled
        /// </summary>
        [Test]
        public void TestListMapping() {
            Order order = (Order) dataMapper.QueryForObject("Order.GetOrderWithLineItemsUsingStatementNamespaces", 1);

            AssertOrder1(order);

            // Check IList collection
            Assert.IsNotNull(order.LineItemsIList);
            Assert.AreEqual(2, order.LineItemsIList.Count);

        }

        /// <summary>
        /// Test Insert Via Insert Statement
        /// for support request 1050704:
        /// Unable to use selectKey with 
        /// useStatementNamespaces=true
        /// </summary>
        [Test] 
        public void TestInsertSelectKey() {
            Category category = new Category();
            category.Name = "toto";
            category.Guid = Guid.NewGuid();

            int key = (int)dataMapper.Insert("Category.InsertCategoryViaInsertStatement", category);
            Assert.AreEqual(1, key);
        }

        #endregion
    }
}