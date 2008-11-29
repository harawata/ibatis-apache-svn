package org.apache.ibatis.builder;

import org.junit.Test;
import org.junit.Assert;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.api.*;
import org.apache.ibatis.io.Resources;

import java.io.Reader;
import java.util.List;

import domain.blog.Author;

public class SqlMapperBuilderTest extends BaseDataTest {

  @Test
  public void shouldBuildBlogMappers() throws Exception {
    createBlogDataSource();
    final String resource = "org/apache/ibatis/builder/MapperConfig.xml";
    final Reader reader = Resources.getResourceAsReader(resource);
    SqlMapper sqlMapper = new SqlMapperBuilder().build(reader);
    SqlSession session = sqlMapper.openSession();
    try {
      List<Author> authors = session.selectList("com.domain.AuthorMapper.selectAllAuthors");
      Assert.assertEquals(2,authors.size());
    } finally {
      session.close();
    }
  }

}
