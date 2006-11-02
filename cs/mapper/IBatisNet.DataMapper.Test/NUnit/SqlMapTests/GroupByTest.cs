
using System.Collections;
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
        [SetUp]
        public void Init()
        {
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
        /// Test Select N+1 on Order/LineItem
        /// </summary>
        [Test]
        public void TestOrderLineItemGroupBy()
        {
            IList list = sqlMap.QueryForList("GetOrderLineItem", null);

            Assert.AreEqual(11, list.Count);
            
            Order order = (Order)list[0];
            Assert.AreEqual(3, order.LineItemsIList.Count);
            Assert.IsNotNull(order.Account);
            AssertAccount1(order.Account);

            order = (Order)list[10];
            Assert.AreEqual(1, order.LineItemsIList.Count);
            Assert.IsNull(order.Account);
        }
    }
}
