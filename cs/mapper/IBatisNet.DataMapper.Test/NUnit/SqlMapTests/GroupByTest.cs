
using System.Collections;
#if dotnet2
using System.Collections.Generic;
#endif
using IBatisNet.DataMapper.Test.Domain;
using NUnit.Framework;

namespace IBatisNet.DataMapper.Test.NUnit.SqlMapTests
{
    /// <summary>
    /// Summary description for GroupByTest.
    /// </summary>
    [TestFixture]
    public class GroupByTest : BaseTest
    {
        #region SetUp & TearDown

        /// <summary>
        /// SetUp
        /// </summary>
        [TestFixtureSetUp]
        protected override void SetUpFixture()
        {
            base.SetUpFixture();

            InitScript(sqlMap.DataSource, ScriptDirectory + "petstore-drop.sql");
            InitScript(sqlMap.DataSource, ScriptDirectory + "petstore-schema.sql");
            InitScript(sqlMap.DataSource, ScriptDirectory + "petstore-init.sql");
        }


        /// <summary>
        /// Dispose the SqlMap
        /// </summary>
        [TestFixtureTearDown]
        protected override void TearDownFixture()
        {
            InitScript(sqlMap.DataSource, ScriptDirectory + "petstore-drop.sql");
            base.TearDownFixture();
        }
        #endregion

        [Test]
        public void TestGroupBy() 
        {
            IList list = sqlMap.QueryForList("GetAllCategories", null);
            Assert.AreEqual(5, list.Count);
        }
        
        [Test]
        public void TestGroupByExtended()  
        {
            IList list = sqlMap.QueryForList("GetAllCategoriesExtended", null);
            Assert.AreEqual(5, list.Count);
        }

        [Test]
        public void TestNestedProperties()
        {
            IList list = sqlMap.QueryForList("GetFish", null);
            Assert.AreEqual(1, list.Count);

            Domain.Petshop.Category cat = (Domain.Petshop.Category)list[0];
            Assert.AreEqual("FISH", cat.Id);
            Assert.AreEqual("Fish", cat.Name);
            Assert.IsNotNull(cat.Products, "Expected product list.");
            Assert.AreEqual(4, cat.Products.Count);

            Domain.Petshop.Product product = (Domain.Petshop.Product)cat.Products[0];
            Assert.AreEqual(2, product.Items.Count);
        }

        [Test]
        public void TestForQueryForObject()
        {
            Domain.Petshop.Category cat = (Domain.Petshop.Category)sqlMap.QueryForObject("GetFish", null);
            Assert.IsNotNull(cat);

            Assert.AreEqual("FISH", cat.Id);
            Assert.AreEqual("Fish", cat.Name);
            Assert.IsNotNull(cat.Products, "Expected product list.");
            Assert.AreEqual(4, cat.Products.Count);

            Domain.Petshop.Product product = (Domain.Petshop.Product)cat.Products[0];
            Assert.AreEqual(2, product.Items.Count);
        }

#if dotnet2

        [Test]
        public void TestGenericFish()
        {
            IList list = sqlMap.QueryForList("GetFishGeneric", null);
            Assert.AreEqual(1, list.Count);

            Domain.Petshop.Category cat = (Domain.Petshop.Category)list[0];
            Assert.AreEqual("FISH", cat.Id);
            Assert.AreEqual("Fish", cat.Name);
            Assert.IsNotNull(cat.GenericProducts, "Expected product list.");
            Assert.AreEqual(4, cat.GenericProducts.Count);

            Domain.Petshop.Product product = cat.GenericProducts[0];
            Assert.AreEqual(2, product.GenericItems.Count);
        }

        [Test]
        public void TestForQueryForObjectGeneric()
        {
            Domain.Petshop.Category cat = sqlMap.QueryForObject<Domain.Petshop.Category>("GetFishGeneric", null);
            Assert.IsNotNull(cat);

            Assert.AreEqual("FISH", cat.Id);
            Assert.AreEqual("Fish", cat.Name);
            Assert.IsNotNull(cat.GenericProducts, "Expected product list.");
            Assert.AreEqual(4, cat.GenericProducts.Count);

            Domain.Petshop.Product product = cat.GenericProducts[0];
            Assert.AreEqual(2, product.GenericItems.Count);
        }

        [Test]
        public void TestGenericList()
        {
            IList<Domain.Petshop.Category> list = sqlMap.QueryForList<Domain.Petshop.Category>("GetFishGeneric", null);
            Assert.AreEqual(1, list.Count);

            Domain.Petshop.Category cat = list[0];
            Assert.AreEqual("FISH", cat.Id);
            Assert.AreEqual("Fish", cat.Name);
            Assert.IsNotNull(cat.GenericProducts, "Expected product list.");
            Assert.AreEqual(4, cat.GenericProducts.Count);

            Domain.Petshop.Product product = cat.GenericProducts[0];
            Assert.AreEqual(2, product.GenericItems.Count);
        }
#endif
        
        [Test]
        public void TestGroupByNull()
        {
            IList list = sqlMap.QueryForList("GetAllProductCategoriesJIRA250", null);
            Domain.Petshop.Category cat = (Domain.Petshop.Category)list[0];
            Assert.AreEqual(0, cat.Products.Count);
        }
        
        /// <summary>
        /// Test Select N+1 on Order/LineItem
        /// </summary>
        [Test]
        public void TestOrderLineItemGroupBy()
        {
            InitScript(sqlMap.DataSource, ScriptDirectory + "petstore-drop.sql");
            InitScript(sqlMap.DataSource, ScriptDirectory + "account-init.sql");
            InitScript(sqlMap.DataSource, ScriptDirectory + "order-init.sql");
            InitScript(sqlMap.DataSource, ScriptDirectory + "line-item-init.sql");

            Order order = new Order();
            order.Id = 11;
            LineItem item = new LineItem();
            item.Id = 10;
            item.Code = "blah";
            item.Price = 44.00m;
            item.Quantity = 1;
            item.Order = order;

            sqlMap.Insert("InsertLineItemPostKey", item);

            
            IList list = sqlMap.QueryForList("GetOrderLineItem", null);

            Assert.AreEqual(11, list.Count);
            
            order = (Order)list[0];
            Assert.AreEqual(3, order.LineItemsIList.Count);
            Assert.IsNotNull(order.Account);
            AssertAccount1(order.Account);

            order = (Order)list[10];
            Assert.AreEqual(1, order.LineItemsIList.Count);
            Assert.IsNull(order.Account);
        }

#if dotnet2
        /// <summary>
        /// Test GroupBy With use of Inheritance
        /// </summary>
        [Test]
        public void GroupByWithInheritance()
        {
            InitScript(sqlMap.DataSource, ScriptDirectory + "petstore-drop.sql");
            InitScript(sqlMap.DataSource, ScriptDirectory + "account-init.sql");
            InitScript(sqlMap.DataSource, ScriptDirectory + "documents-init.sql");

            IList<Account> list = sqlMap.QueryForList<Account>("JIRA206", null);
            
            Assert.AreEqual(5, list.Count);
            Assert.AreEqual(0, list[0].Documents.Count);
            Assert.AreEqual(2, list[1].Documents.Count);
            Assert.AreEqual(1, list[2].Documents.Count);
            Assert.AreEqual(0, list[3].Documents.Count);
            Assert.AreEqual(2, list[4].Documents.Count);

            InitScript(sqlMap.DataSource, ScriptDirectory + "petstore-drop.sql");
            InitScript(sqlMap.DataSource, ScriptDirectory + "petstore-schema.sql");
            InitScript(sqlMap.DataSource, ScriptDirectory + "petstore-init.sql");
        }
#endif
    }
}
