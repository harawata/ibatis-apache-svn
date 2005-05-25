using System.IO;
using System.Xml;
using IBatisNet.Common.Utilities;
using NUnit.Framework;

namespace IBatisNet.Common.Test.NUnit.CommonTests.Utilities
{
	/// <summary>
	/// Description résumée de ResourcesTest.
	/// </summary>
	[TestFixture] 
	public class ResourcesTest
	{
		#region SetUp & TearDown

		/// <summary>
		/// SetUp
		/// </summary>
		[SetUp] 
		public void SetUp() 
		{
		}


		/// <summary>
		/// TearDown
		/// </summary>
		[TearDown] 
		public void Dispose()
		{ 
		} 

		#endregion

		#region Test ResourcesTest

		/// <summary>
		/// Test loading Embeded Resource
		/// </summary>
		[Test] 
		public void TestEmbededResource() 
		{
			XmlDocument doc = null;

			doc = Resources.GetEmbeddedResourceAsXmlDocument("properties.xml, IBatisNet.Common.Test");

			Assert.IsNotNull(doc);
			Assert.IsTrue(doc.HasChildNodes);
			Assert.AreEqual(doc.ChildNodes.Count,2);
			Assert.AreEqual(doc.SelectNodes("/settings/add").Count, 4);
		}
		#endregion

		#region GetFileInfo Tests

		[Test] 
		public void GetFileInfoWithAbsolute() 
		{ 
			FileInfo fileInfo = Resources.GetFileInfo(Resources.ApplicationBase+Path.DirectorySeparatorChar+"IBatisNet.Common.Test.dll");
			Assert.IsNotNull(fileInfo);
		}

		[Test] 
		public void GetFileInfoWithRelative() 
		{ 
			FileInfo fileInfo = Resources.GetFileInfo("IBatisNet.Common.Test.dll");
			Assert.IsNotNull(fileInfo);
		}

		[Test] 
		public void GetFileInfoWithRelativeProtocole() 
		{ 
			FileInfo fileInfo = Resources.GetFileInfo("file://IBatisNet.Common.Test.dll");
			Assert.IsNotNull(fileInfo);
		}

		[Test] 
		public void GetFileInfoWithAbsoluteProtocole() 
		{ 
			FileInfo fileInfo = Resources.GetFileInfo("file://"+Resources.ApplicationBase+Path.DirectorySeparatorChar+"IBatisNet.Common.Test.dll");
			Assert.IsNotNull(fileInfo);
		}

		[Test] 
		public void GetFileInfoWithRelativeProtocolePlusSlash() 
		{ 
			FileInfo fileInfo = Resources.GetFileInfo("file:///IBatisNet.Common.Test.dll");
			Assert.IsNotNull(fileInfo);
		}



		[Test] 
		public void GetFileInfoWithAbsoluteProtocolePlusSlash() 
		{ 
			FileInfo fileInfo = Resources.GetFileInfo("file:///"+Resources.ApplicationBase+Path.DirectorySeparatorChar+"IBatisNet.Common.Test.dll");
			Assert.IsNotNull(fileInfo);
		}
		#endregion 

		#region GetConfigAsXmlDocument Tests

		[Test] 
		public void GetConfigAsXmlDocumentWithAbsolute() 
		{ 
			XmlDocument doc = Resources.GetConfigAsXmlDocument(Resources.ApplicationBase+Path.DirectorySeparatorChar+"SqlMap_MSSQL_SqlClient.config");
			Assert.IsNotNull(doc);
		}

		[Test] 
		public void GetConfigAsXmlDocumentWithRelative() 
		{ 
			XmlDocument doc = Resources.GetConfigAsXmlDocument("SqlMap_MSSQL_SqlClient.config");
			Assert.IsNotNull(doc);
		}

		[Test] 
		public void GetConfigAsXmlDocumentWithRelativeProtocole() 
		{ 
			XmlDocument doc = Resources.GetConfigAsXmlDocument("file://SqlMap_MSSQL_SqlClient.config");
			Assert.IsNotNull(doc);
		}

		[Test] 
		public void GetConfigAsXmlDocumentWithAbsoluteProtocole() 
		{ 
			XmlDocument doc = Resources.GetConfigAsXmlDocument("file://"+Resources.ApplicationBase+Path.DirectorySeparatorChar+"SqlMap_MSSQL_SqlClient.config");
			Assert.IsNotNull(doc);
		}

		[Test] 
		public void GetConfigAsXmlDocumentWithRelativeProtocolePlusSlash() 
		{ 
			XmlDocument doc = Resources.GetConfigAsXmlDocument("file:///SqlMap_MSSQL_SqlClient.config");
			Assert.IsNotNull(doc);
		}



		[Test] 
		public void GetConfigAsXmlDocumentWithAbsoluteProtocolePlusSlash() 
		{ 
			XmlDocument doc = Resources.GetConfigAsXmlDocument("file:///"+Resources.ApplicationBase+Path.DirectorySeparatorChar+"SqlMap_MSSQL_SqlClient.config");
			Assert.IsNotNull(doc);
		}
		#endregion 
	}
}
