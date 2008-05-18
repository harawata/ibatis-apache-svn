using NUnit.Framework;
using Apache.Ibatis.DataMapper.Model.Cache;
using Apache.Ibatis.DataMapper.Model.Cache.Fifo;

namespace Apache.Ibatis.DataMapper.SqlClient.Test.Fixtures.Mapping.CacheController
{
    /// <summary>
    /// Description résumée de FifoCacheControllerTest.
    /// </summary>
    [TestFixture]
    public class FifoCacheControllerTest : LruCacheControllerTest
    {

        protected override ICacheController GetController() 
        {
            return new FifoCacheController();
        }
    }
}