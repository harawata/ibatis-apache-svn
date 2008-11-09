package org.apache.ibatis.builder;

import org.junit.Test;
import org.junit.Assert;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.io.Resources;

import javax.sql.DataSource;
import java.io.Reader;

public class MapperConfigParserTest extends BaseDataTest {

  @Test
  public void shouldBuildBlogMappers() throws Exception {
    createBlogDataSource();
    final String resource = "org/apache/ibatis/builder/MapperConfig.xml";
    final Reader reader = Resources.getResourceAsReader(resource);
    MapperConfigParser parser = new MapperConfigParser(reader,null);

    parser.parse();
    Configuration config = parser.getConfiguration();

    DataSource ds = config.getEnvironment().getDataSource();
    TransactionFactory tf = config.getEnvironment().getTransactionFactory();
    MappedStatement ms = config.getMappedStatement("selectAllAuthors");
    Transaction tx = tf.newTransaction(ds.getConnection());


//    Assert.assertEquals(1,config.getCaches().size());
  }

}
