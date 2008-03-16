using System;
using Apache.Ibatis.DataMapper.OleDb.Test.Domain;
using Apache.Ibatis.DataMapper.OleDb.Test.Fixtures;
using NUnit.Framework;

namespace Apache.Ibatis.DataMapper.OleDb.Test.Fixtures.Mapping
{
    [TestFixture] 
    public class StatementTest : BaseTest
    {
        /// <summary>
        /// Test guid column/field.
        /// </summary>
        [Test]
        public void TestGuidColumn()
        {
            Category category = new Category();
            category.Name = "toto";
            category.Guid = Guid.NewGuid();

            int key = (int)dataMapper.Insert("InsertCategory", category);

            Category categoryTest = (Category)dataMapper.QueryForObject("GetCategory", key);
            Assert.AreEqual(key, categoryTest.Id);
            Assert.AreEqual(category.Name, categoryTest.Name);
            Assert.AreEqual(category.Guid, categoryTest.Guid);
        }
    }
}
