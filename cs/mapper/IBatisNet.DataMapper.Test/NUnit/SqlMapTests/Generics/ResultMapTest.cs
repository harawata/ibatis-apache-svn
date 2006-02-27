using System;
using System.Collections;
using System.Configuration;
using System.Collections.Generic;

using NUnit.Framework;

using IBatisNet.DataMapper.Test.Domain;

namespace IBatisNet.DataMapper.Test.NUnit.SqlMapTests.Generics
{
    [TestFixture] 
    public class ResultMapTest : BaseTest
    {
        #region SetUp & TearDown

        /// <summary>
        /// SetUp
        /// </summary>
        [SetUp]
        public void Init()
        {
            InitScript(sqlMap.DataSource, ScriptDirectory + "account-init.sql");
            InitScript(sqlMap.DataSource, ScriptDirectory + "order-init.sql");
            InitScript(sqlMap.DataSource, ScriptDirectory + "line-item-init.sql");
            InitScript(sqlMap.DataSource, ScriptDirectory + "enumeration-init.sql");
        }

        /// <summary>
        /// TearDown
        /// </summary>
        [TearDown]
        public void Dispose()
        { /* ... */ }

        #endregion

        #region Result Map test

        /// <summary>
        /// Test generic Ilist  
        /// </summary>
        [Test]
        public void TestGenricListMapping()
        {
            Order order = sqlMap.QueryForObject<Order>("GetOrderWithGenericListLineItem", 1);

            AssertOrder1(order);

            // Check generic IList collection
            Assert.IsNotNull(order.LineItemsGenericList);
            Assert.AreEqual(2, order.LineItemsGenericList.Count);

        }

        /// <summary>
        /// Test generic Collection  
        /// </summary>
        [Test]
        public void TestGenricCollectionMapping()
        {
            Order order = sqlMap.QueryForObject<Order>("GetOrderWithGenericLineItemCollection", 1);

            AssertOrder1(order);

            // Check generic collection
            Assert.IsNotNull(order.LineItemsCollection);
            Assert.AreEqual(2, order.LineItemsCollection.Count);
        }

        #endregion
    }
}
