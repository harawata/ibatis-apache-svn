using System;
using System.Text;
using IBatisNet.Common.Utilities.TypesResolver;
using NUnit.Framework;

namespace IBatisNet.Common.Test.NUnit.CommonTests.Utilities
{
    [TestFixture] 
    public class TypeResolverTest
    {
        /// <summary>
        /// Test nullable resolver
        /// </summary>
        [Test]
        public void TestFullNameNullableType()
        {
            Type nullableType = typeof(bool?);

            TypeResolver resolver = new TypeResolver();

            Type nullableBooleanType = resolver.Resolve(nullableType.FullName);

            Assert.IsNotNull(nullableBooleanType);
        }

        /// <summary>
        /// Test nullable resolver
        /// </summary>
        [Test]
        public void TestAssemblyQualifiedNameNullableType()
        {
            Type nullableType = typeof(bool?);

            TypeResolver resolver = new TypeResolver();

            Type nullableBooleanType = resolver.Resolve(nullableType.AssemblyQualifiedName);

            Assert.IsNotNull(nullableBooleanType);
        }
    }
}
