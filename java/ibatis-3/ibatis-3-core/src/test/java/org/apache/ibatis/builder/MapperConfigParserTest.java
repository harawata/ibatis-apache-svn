package org.apache.ibatis.builder;

import org.junit.Test;
import org.junit.Assert;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.api.SqlMapper;
import org.apache.ibatis.api.defaults.DefaultSqlMapper;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.io.Resources;

import javax.sql.DataSource;
import java.io.Reader;
import java.util.List;

import domain.blog.Author;

public class MapperConfigParserTest extends BaseDataTest {

  @Test
  public void shouldBuildBlogMappers() throws Exception {
    createBlogDataSource();
    final String resource = "org/apache/ibatis/builder/MapperConfig.xml";
    final Reader reader = Resources.getResourceAsReader(resource);
    MapperConfigParser parser = new MapperConfigParser(reader,null);
    parser.parse();
    Configuration config = parser.getConfiguration();

    SqlMapper sqlMapper = new DefaultSqlMapper(config);

    Environment environment = config.getEnvironment();
    DataSource ds = environment.getDataSource();
    TransactionFactory tf = environment.getTransactionFactory();
    Transaction tx = tf.newTransaction(ds.getConnection());
    Executor exec = config.newExecutor(tx);
    MappedStatement ms = config.getMappedStatement("com.domain.AuthorMapper.selectAllAuthors");
    List<Author> authors = exec.query(ms,null,Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, Executor.NO_RESULT_HANDLER);

    for (Author author : authors) {
      System.out.println(author);
    }

    Assert.assertEquals(2,config.getCaches().size());
  }

}
