using System;
using System.Collections;
using System.Configuration;

using NUnit.Framework;

using IBatisNet.DataMapper; //<-- To access the definition of the deleagte RowDelegate
using IBatisNet.DataMapper.MappedStatements; //<-- To access the definition of the PageinatedList
using IBatisNet.Common;
using IBatisNet.Common.Exceptions;

using IBatisNet.DataMapper.Test;
using IBatisNet.DataMapper.Test.Domain;

namespace IBatisNet.DataMapper.Test.NUnit.SqlMapTests
{
	/// <summary>
	/// Summary description for InheritanceTest.
	/// </summary>
	[TestFixture] 
	public class InheritanceTest: BaseTest
	{

		#region SetUp & TearDown

		/// <summary>
		/// SetUp
		/// </summary>
		[SetUp] 
		public void Init() 
		{
			InitSqlMap();
			InitScript( sqlMap.DataSource, ScriptDirectory + "documents-init.sql" );
		}


		/// <summary>
		/// TearDown
		/// </summary>
		[TearDown] 
		public void Dispose()
		{ /* ... */ } 

		#endregion

		/// <summary>
		/// </summary>
		[Test] 
		public void GetAllDocument() 
		{
			IList list = sqlMap.QueryForList("GetAllDocument", null);

			Assert.AreEqual(3, list.Count);
			Book book = (Book) list[0];
			Assert.AreEqual(1, book.Id);
			Assert.AreEqual("The World of Null-A", book.Title);
			Assert.AreEqual(55, book.PageNumber);

			book = (Book) list[1];
			Assert.AreEqual(3, book.Id);
			Assert.AreEqual("Lord of the Rings", book.Title);
			Assert.AreEqual(3587, book.PageNumber);

			Newspaper news = (Newspaper) list[2];
			Assert.AreEqual(2, news.Id);
			Assert.AreEqual("Le Progres de Lyon", news.Title);
			Assert.AreEqual("Lyon", news.City);
		}
	}
}
