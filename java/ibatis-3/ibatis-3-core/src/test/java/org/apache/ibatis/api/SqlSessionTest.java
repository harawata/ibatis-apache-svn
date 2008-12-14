package org.apache.ibatis.api;

import org.junit.*;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.api.*;
import org.apache.ibatis.io.Resources;

import java.io.Reader;
import java.util.List;

import domain.blog.Author;

public class SqlSessionTest extends BaseDataTest {
  private static SqlSessionFactory sqlMapper;

  @BeforeClass
  public static void setup() throws Exception {
    createBlogDataSource();
    final String resource = "org/apache/ibatis/parser/MapperConfig.xml";
    final Reader reader = Resources.getResourceAsReader(resource);
    sqlMapper = new SqlSessionFactoryBuilder().build(reader);
  }

  @Test
  public void shouldSelectAllAuthors() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      List<Author> authors = session.selectList("com.domain.AuthorMapper.selectAllAuthors");
      Assert.assertEquals(2,authors.size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectOneAuthor() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      Author author = (Author) session.selectOne(
          "com.domain.AuthorMapper.selectAuthor", new Author(101));
      Assert.assertEquals(101, author.getId());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectOneAuthorWithInlineParams() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      Author author = (Author) session.selectOne(
          "com.domain.AuthorMapper.selectAuthorWithInlineParams", new Author(101));
      Assert.assertEquals(101, author.getId());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldInsertAuthor() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      Author expected = new Author(500, "cbegin", "******", "cbegin@somewhere.com", "Something...", null);
      session.insert("com.domain.AuthorMapper.insertAuthor", expected);
      Author actual = (Author) session.selectOne("com.domain.AuthorMapper.selectAuthor", new Author(500));
      Assert.assertEquals(expected.getId(), actual.getId());
      Assert.assertEquals(expected.getUsername(), actual.getUsername());
      Assert.assertEquals(expected.getPassword(), actual.getPassword());
      Assert.assertEquals(expected.getEmail(), actual.getEmail());
      Assert.assertEquals(expected.getBio(), actual.getBio());
    } finally {
      session.close();
    }
  }

}
