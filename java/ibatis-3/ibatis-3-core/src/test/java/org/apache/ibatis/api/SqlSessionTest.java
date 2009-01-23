package org.apache.ibatis.api;

import org.junit.*;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.io.Resources;

import java.io.Reader;
import java.util.List;

import domain.blog.*;

import javax.sql.DataSource;

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
      Assert.assertEquals(Section.NEWS, author.getFavouriteSection());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectOneAuthorAsList() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      List<Author> authors= session.selectList(
          "com.domain.AuthorMapper.selectAuthor", new Author(101));
      Assert.assertEquals(101, authors.get(0).getId());
      Assert.assertEquals(Section.NEWS, authors.get(0).getFavouriteSection());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectOneImmutableAuthor() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      ImmutableAuthor author = (ImmutableAuthor) session.selectOne(
          "com.domain.AuthorMapper.selectImmutableAuthor", new Author(101));
      Assert.assertEquals(101, author.getId());
      Assert.assertEquals(Section.NEWS, author.getFavouriteSection());
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
      Assert.assertNotNull(actual);
      Assert.assertEquals(expected.getId(), actual.getId());
      Assert.assertEquals(expected.getUsername(), actual.getUsername());
      Assert.assertEquals(expected.getPassword(), actual.getPassword());
      Assert.assertEquals(expected.getEmail(), actual.getEmail());
      Assert.assertEquals(expected.getBio(), actual.getBio());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldUpdateAuthorImplicitRollback() throws Exception {
    SqlSession session = sqlMapper.openSession();
    Author original;
    Author updated;
    try {
      original = (Author) session.selectOne("com.domain.AuthorMapper.selectAuthor", 101);
      original.setEmail("new@email.com");
      session.update("com.domain.AuthorMapper.updateAuthor", original);

      updated = (Author) session.selectOne("com.domain.AuthorMapper.selectAuthor", 101);
      Assert.assertEquals(original.getEmail(), updated.getEmail());
    } finally {
      session.close();
    }
    try {
      session = sqlMapper.openSession();
      updated = (Author) session.selectOne("com.domain.AuthorMapper.selectAuthor", 101);
      Assert.assertEquals("jim@ibatis.apache.org", updated.getEmail());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldUpdateAuthorCommit() throws Exception {
    SqlSession session = sqlMapper.openSession();
    Author original;
    Author updated;
    try {
      original = (Author) session.selectOne("com.domain.AuthorMapper.selectAuthor", 101);
      original.setEmail("new@email.com");
      session.update("com.domain.AuthorMapper.updateAuthor", original);

      updated = (Author) session.selectOne("com.domain.AuthorMapper.selectAuthor", 101);
      Assert.assertEquals(original.getEmail(), updated.getEmail());
      session.commit();
    } finally {
      session.close();
    }
    try {
      session = sqlMapper.openSession();
      updated = (Author) session.selectOne("com.domain.AuthorMapper.selectAuthor", 101);
      Assert.assertEquals(original.getEmail(), updated.getEmail());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldDeleteAuthor() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      final int id = 102;

      List<Author> authors = session.selectList("com.domain.AuthorMapper.selectAuthor", id);
      Assert.assertEquals(1,authors.size());

      session.delete("com.domain.AuthorMapper.deleteAuthor", id);
      authors = session.selectList("com.domain.AuthorMapper.selectAuthor", id);
      Assert.assertEquals(0,authors.size());

      session.rollback();
      authors = session.selectList("com.domain.AuthorMapper.selectAuthor", id);
      Assert.assertEquals(1,authors.size());

    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectBlogWithPostsAndAuthorUsingSubSelects() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      Blog blog = (Blog) session.selectOne("com.domain.BlogMapper.selectBlogWithPostsUsingSubSelect", 1);
      Assert.assertEquals("Jim Business", blog.getTitle());
      Assert.assertEquals(2, blog.getPosts().size());
      Assert.assertEquals("Corn nuts",blog.getPosts().get(0).getSubject());
      Assert.assertEquals(101,blog.getAuthor().getId());
      Assert.assertEquals("jim",blog.getAuthor().getUsername());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectBlogWithPostsAndAuthorUsingJoin() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      Blog blog = (Blog) session.selectOne("com.domain.BlogMapper.selectBlogJoinedWithPostsAndAuthor", 1);
      Assert.assertEquals("Jim Business", blog.getTitle());

      final Author author = blog.getAuthor();
      Assert.assertEquals(101, author.getId());
      Assert.assertEquals("jim",author.getUsername());

      final List<Post> posts = blog.getPosts();
      Assert.assertEquals(2, posts.size());

      final Post post = blog.getPosts().get(0);
      Assert.assertEquals(1, post.getId());
      Assert.assertEquals("Corn nuts", post.getSubject());

      final List<Comment> comments = post.getComments();
      Assert.assertEquals(1, comments.size());

      final Comment comment = comments.get(0);
      Assert.assertEquals(1, comment.getId());

      Assert.assertEquals(DraftPost.class, blog.getPosts().get(0).getClass());
      Assert.assertEquals(Post.class, blog.getPosts().get(1).getClass());

    } finally {
      session.close();
    }
  }

  @Test
  public void shouldThrowExceptionIfMappedStatementDoesNotExist() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      session.selectList("ThisStatementDoesNotExist");
      Assert.fail("Expected exception to be thrown due to statement that does not exist.");
    } catch (Exception e) {
      Assert.assertTrue(e.getMessage().contains("does not contain value for ThisStatementDoesNotExist"));
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldThrowExceptionIfTryingToAddStatementWithSameName() throws Exception {
    Configuration config = sqlMapper.getConfiguration();
    try {
      config.addMappedStatement(config.getMappedStatement("com.domain.BlogMapper.selectBlogWithPostsUsingSubSelect"));
      Assert.fail("Expected exception to be thrown due to statement that already exists.");
    } catch (Exception e) {
      Assert.assertTrue(e.getMessage().contains("already contains value for com.domain.BlogMapper.selectBlogWithPostsUsingSubSelect"));
    }
  }

}
