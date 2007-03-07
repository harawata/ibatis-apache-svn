using System;
using System.Text;
using System.Collections.Generic;
using System.Collections;

using IBatisNet.Common.Test.Domain;
using IBatisNet.Common.Utilities.TypesResolver;
using NUnit.Framework;
using IBatisNet.Common.Utilities;

namespace IBatisNet.Common.Test.NUnit.CommonTests.Utilities
{
    [TestFixture] 
    public class TypeResolverTest
    {

        /// <summary>
        /// Test space on generic type
        /// </summary>
        [Test]
        public void TestTrimSpace()
        {
            Type genericType = TypeUtils.ResolveType("System.Collections.Generic.Dictionary`2[[System.String],[System.Int32]]");

            Assert.IsNotNull(genericType);
        }



        /// <summary>
        /// Test nullable resolver
        /// </summary>
        [Test]
        public void TestFullNameNullableType()
        {
            Type nullableType = typeof(bool?);

            Type nullableBooleanType = TypeUtils.ResolveType(nullableType.FullName);

            Assert.IsNotNull(nullableBooleanType);
        }

        /// <summary>
        /// Test nullable resolver
        /// </summary>
        [Test]
        public void TestAssemblyQualifiedNameNullableType()
        {
            Type nullableType = typeof(bool?);

            Type nullableBooleanType = TypeUtils.ResolveType(nullableType.AssemblyQualifiedName);

            Assert.IsNotNull(nullableBooleanType);
        }

        /// <summary>
        /// Test generic list resolver
        /// </summary>
        [Test]
        public void TestGeneicListType()
        {
            IList<Account> list = new List<Account>();
            string assemblyQualifiedName = list.GetType().AssemblyQualifiedName;
            Type listType = TypeUtils.ResolveType(assemblyQualifiedName);

            Assert.IsNotNull(listType);
        }

        /// <summary>
        /// Test generic dictionary resolver
        /// </summary>
        [Test]
        public void TestGenericDictionaryType()
        {
            IDictionary<string, int> dico = new Dictionary<string, int>();

            Console.WriteLine(typeof(IDictionary<,>).FullName);
            Console.WriteLine(dico.GetType().FullName);

            string assemblyQualifiedName = dico.GetType().AssemblyQualifiedName;
            Type listType = TypeUtils.ResolveType(assemblyQualifiedName);

            Assert.IsNotNull(listType);
        }
    }
}
