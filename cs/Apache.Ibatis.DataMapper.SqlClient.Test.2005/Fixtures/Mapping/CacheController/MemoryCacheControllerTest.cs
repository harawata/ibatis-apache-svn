using Apache.Ibatis.DataMapper.SqlClient.Test.Fixtures.Mapping.CacheController;
using NUnit.Framework;
using Apache.Ibatis.DataMapper.Model.Cache;
using Apache.Ibatis.DataMapper.Model.Cache.Memory;

namespace Apache.Ibatis.DataMapper.SqlClient.Test.Fixtures.Mapping.CacheController
{
    /// <summary>
    /// Description résumée de MemoryCacheControllerTest.
    /// </summary>
    [TestFixture]
    public class MemoryCacheControllerTest: LruCacheControllerTest
    {

        protected override ICacheController GetController() 
        {
            return new MemoryCacheControler();
        }

        [Test]
        public override void TestSizeOne() 
        {
            // This is not relevant for this model
        }
    }
}