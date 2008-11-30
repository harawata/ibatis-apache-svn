package org.apache.ibatis.builder;

import org.junit.*;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.api.*;
import org.apache.ibatis.io.Resources;

import java.io.Reader;
import java.util.List;

import domain.blog.Author;

public class SqlMapperBuilderTest extends BaseDataTest {
  private static SqlMapper sqlMapper;

  @BeforeClass
  public static void setup() throws Exception {
    createBlogDataSource();
    final String resource = "org/apache/ibatis/builder/MapperConfig.xml";
    final Reader reader = Resources.getResourceAsReader(resource);
    sqlMapper = new SqlMapperBuilder().build(reader);
  }

  @Test
  public void shouldBuildBlogMappers() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      List<Author> authors = session.selectList("com.domain.AuthorMapper.selectAllAuthors");
      Assert.assertEquals(2,authors.size());

    } finally {
      session.close();
    }
  }

}
