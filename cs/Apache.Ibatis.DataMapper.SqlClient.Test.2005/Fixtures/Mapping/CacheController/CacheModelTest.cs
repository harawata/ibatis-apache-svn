using System;
using System.Collections;
using System.Collections.Specialized;
using Apache.Ibatis.Common.Utilities;

using NUnit.Framework;

using Apache.Ibatis.DataMapper.SqlClient.Test.Domain;
using Apache.Ibatis.DataMapper.Model.Cache;
using Apache.Ibatis.DataMapper.Model.Cache.Lru;

namespace Apache.Ibatis.DataMapper.SqlClient.Test.Fixtures.Mapping.CacheController
{
    /// <summary>
    /// Summary description for CacheKeyTest.
    /// </summary>
    [TestFixture]
    public class CacheModelTest
    {
        [Test]
        /// <summary>
        /// Returns reference to same instance of cached object
        /// </summary>
        public void TestReturnInstanceOfCachedOject()
        {
            IDictionary props = new HybridDictionary();
            props.Add("CacheSize", "1");
            //cacheController.Configure(props);

            FlushInterval interval = new FlushInterval(1,0,0,0);

            CacheModel cacheModel = new CacheModel("test", typeof(LruCacheController).FullName, props);

            cacheModel.FlushInterval = interval;
            //cacheModel.IsReadOnly = true;
            //cacheModel.IsSerializable = false;

            Order order = new Order(); 
            order.CardNumber = "CardNumber";
            order.Date = DateTime.Now;
            order.LineItemsCollection = new LineItemCollection();
            LineItem item = new LineItem();
            item.Code = "Code1";
            order.LineItemsCollection.Add(item);
            item = new LineItem();
            item.Code = "Code2";
            order.LineItemsCollection.Add(item);

            CacheKey key = new CacheKey();
            key.Update(order);

            int firstId = HashCodeProvider.GetIdentityHashCode(order);
            cacheModel[ key ] = order;

            Order order2 = cacheModel[ key ] as Order;
            int secondId = HashCodeProvider.GetIdentityHashCode(order2);
            Assert.AreEqual(firstId, secondId, "hasCode different");
        }

        [Test]
        /// <summary>
        /// Returns copy of cached object
        /// </summary>
        public void TestReturnCopyOfCachedOject()
        {
            //ICacheController cacheController = new LruCacheController();
            IDictionary props = new HybridDictionary();
            props.Add("CacheSize", "1");

            FlushInterval interval = new FlushInterval(1,0,0,0);

            CacheModel cacheModel = new CacheModel("test", typeof(LruCacheController).FullName, props, false, true);
            cacheModel.FlushInterval = interval;
            //cacheModel.CacheController = cacheController;
            //cacheModel.IsReadOnly = false;
            //cacheModel.IsSerializable = true;

            Order order = new Order(); 
            order.CardNumber = "CardNumber";
            order.Date = DateTime.Now;
            order.LineItemsCollection = new LineItemCollection();

            LineItem item = new LineItem();
            item.Code = "Code1";
            order.LineItemsCollection.Add(item);

            item = new LineItem();
            item.Code = "Code2";
            order.LineItemsCollection.Add(item);

            CacheKey key = new CacheKey();
            key.Update(order);

            int firstId = HashCodeProvider.GetIdentityHashCode(order);
            cacheModel[ key ] = order;

            Order order2 = cacheModel[ key ] as Order;
            int secondId = HashCodeProvider.GetIdentityHashCode(order2);
            Assert.AreNotEqual(firstId, secondId, "hasCode equal");

        }
    }
}