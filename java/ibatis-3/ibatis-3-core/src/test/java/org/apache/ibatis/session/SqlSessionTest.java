package org.apache.ibatis.session;

import domain.blog.*;
import domain.blog.mappers.*;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Configuration;
import static org.junit.Assert.*;
import org.junit.*;

import java.io.Reader;
import java.util.*;

public class SqlSessionTest extends BaseDataTest {
  private static SqlSessionFactory sqlMapper;

  @BeforeClass
  public static void setup() throws Exception {
    createBlogDataSource();
    final String resource = "org/apache/ibatis/builder/MapperConfig.xml";
    final Reader reader = Resources.getResourceAsReader(resource);
    sqlMapper = new SqlSessionFactoryBuilder().build(reader);
  }

  @Test
  public void shouldSelectAllAuthors() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      List<Author> authors = session.selectList("domain.blog.mappers.AuthorMapper.selectAllAuthors");
      assertEquals(2, authors.size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectOneAuthor() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      Author author = (Author) session.selectOne(
          "domain.blog.mappers.AuthorMapper.selectAuthor", new Author(101));
      assertEquals(101, author.getId());
      assertEquals(Section.NEWS, author.getFavouriteSection());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectOneAuthorAsList() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      List<Author> authors = session.selectList(
          "domain.blog.mappers.AuthorMapper.selectAuthor", new Author(101));
      assertEquals(101, authors.get(0).getId());
      assertEquals(Section.NEWS, authors.get(0).getFavouriteSection());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectOneImmutableAuthor() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      ImmutableAuthor author = (ImmutableAuthor) session.selectOne(
          "domain.blog.mappers.AuthorMapper.selectImmutableAuthor", new Author(101));
      assertEquals(101, author.getId());
      assertEquals(Section.NEWS, author.getFavouriteSection());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectOneAuthorWithInlineParams() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      Author author = (Author) session.selectOne(
          "domain.blog.mappers.AuthorMapper.selectAuthorWithInlineParams", new Author(101));
      assertEquals(101, author.getId());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldInsertAuthor() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      Author expected = new Author(500, "cbegin", "******", "cbegin@somewhere.com", "Something...", null);
      session.insert("domain.blog.mappers.AuthorMapper.insertAuthor", expected);
      Author actual = (Author) session.selectOne("domain.blog.mappers.AuthorMapper.selectAuthor", new Author(500));
      assertNotNull(actual);
      assertEquals(expected.getId(), actual.getId());
      assertEquals(expected.getUsername(), actual.getUsername());
      assertEquals(expected.getPassword(), actual.getPassword());
      assertEquals(expected.getEmail(), actual.getEmail());
      assertEquals(expected.getBio(), actual.getBio());
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
      original = (Author) session.selectOne("domain.blog.mappers.AuthorMapper.selectAuthor", 101);
      original.setEmail("new@email.com");
      session.update("domain.blog.mappers.AuthorMapper.updateAuthor", original);

      updated = (Author) session.selectOne("domain.blog.mappers.AuthorMapper.selectAuthor", 101);
      assertEquals(original.getEmail(), updated.getEmail());
    } finally {
      session.close();
    }
    try {
      session = sqlMapper.openSession();
      updated = (Author) session.selectOne("domain.blog.mappers.AuthorMapper.selectAuthor", 101);
      assertEquals("jim@ibatis.apache.org", updated.getEmail());
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
      original = (Author) session.selectOne("domain.blog.mappers.AuthorMapper.selectAuthor", 101);
      original.setEmail("new@email.com");
      session.update("domain.blog.mappers.AuthorMapper.updateAuthor", original);

      updated = (Author) session.selectOne("domain.blog.mappers.AuthorMapper.selectAuthor", 101);
      assertEquals(original.getEmail(), updated.getEmail());
      session.commit();
    } finally {
      session.close();
    }
    try {
      session = sqlMapper.openSession();
      updated = (Author) session.selectOne("domain.blog.mappers.AuthorMapper.selectAuthor", 101);
      assertEquals(original.getEmail(), updated.getEmail());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldUpdateAuthorIfNecessary() throws Exception {
    SqlSession session = sqlMapper.openSession();
    Author original;
    Author updated;
    try {
      original = (Author) session.selectOne("domain.blog.mappers.AuthorMapper.selectAuthor", 101);
      original.setEmail("new@email.com");
      original.setBio(null);
      session.update("domain.blog.mappers.AuthorMapper.updateAuthorIfNecessary", original);

      updated = (Author) session.selectOne("domain.blog.mappers.AuthorMapper.selectAuthor", 101);
      assertEquals(original.getEmail(), updated.getEmail());
      session.commit();
    } finally {
      session.close();
    }
    try {
      session = sqlMapper.openSession();
      updated = (Author) session.selectOne("domain.blog.mappers.AuthorMapper.selectAuthor", 101);
      assertEquals(original.getEmail(), updated.getEmail());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldDeleteAuthor() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      final int id = 102;

      List<Author> authors = session.selectList("domain.blog.mappers.AuthorMapper.selectAuthor", id);
      assertEquals(1, authors.size());

      session.delete("domain.blog.mappers.AuthorMapper.deleteAuthor", id);
      authors = session.selectList("domain.blog.mappers.AuthorMapper.selectAuthor", id);
      assertEquals(0, authors.size());

      session.rollback();
      authors = session.selectList("domain.blog.mappers.AuthorMapper.selectAuthor", id);
      assertEquals(1, authors.size());

    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectBlogWithPostsAndAuthorUsingSubSelects() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      Blog blog = (Blog) session.selectOne("domain.blog.mappers.BlogMapper.selectBlogWithPostsUsingSubSelect", 1);
      assertEquals("Jim Business", blog.getTitle());
      assertEquals(2, blog.getPosts().size());
      assertEquals("Corn nuts", blog.getPosts().get(0).getSubject());
      assertEquals(101, blog.getAuthor().getId());
      assertEquals("jim", blog.getAuthor().getUsername());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectBlogWithPostsAndAuthorUsingJoin() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      Blog blog = (Blog) session.selectOne("domain.blog.mappers.BlogMapper.selectBlogJoinedWithPostsAndAuthor", 1);
      assertEquals("Jim Business", blog.getTitle());

      final Author author = blog.getAuthor();
      assertEquals(101, author.getId());
      assertEquals("jim", author.getUsername());

      final List<Post> posts = blog.getPosts();
      assertEquals(2, posts.size());

      final Post post = blog.getPosts().get(0);
      assertEquals(1, post.getId());
      assertEquals("Corn nuts", post.getSubject());

      final List<Comment> comments = post.getComments();
      assertEquals(1, comments.size());

      final Comment comment = comments.get(0);
      assertEquals(1, comment.getId());

      assertEquals(DraftPost.class, blog.getPosts().get(0).getClass());
      assertEquals(Post.class, blog.getPosts().get(1).getClass());

    } finally {
      session.close();
    }
  }

  @Test 
  public void shouldSelectNestedBlogWithPostsAndAuthorUsingJoin() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      Blog blog = (Blog) session.selectOne("domain.blog.mappers.NestedBlogMapper.selectBlogJoinedWithPostsAndAuthor", 1);
      assertEquals("Jim Business", blog.getTitle());

      final Author author = blog.getAuthor();
      assertEquals(101, author.getId());
      assertEquals("jim", author.getUsername());

      final List<Post> posts = blog.getPosts();
      assertEquals(2, posts.size());

      final Post post = blog.getPosts().get(0);
      assertEquals(1, post.getId());
      assertEquals("Corn nuts", post.getSubject());

      final List<Comment> comments = post.getComments();
      assertEquals(1, comments.size());

      final Comment comment = comments.get(0);
      assertEquals(1, comment.getId());

      assertEquals(DraftPost.class, blog.getPosts().get(0).getClass());
      assertEquals(Post.class, blog.getPosts().get(1).getClass());

    } finally {
      session.close();
    }
  }

  @Test
  public void shouldThrowExceptionIfMappedStatementDoesNotExist() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      session.selectList("ThisStatementDoesNotExist");
      fail("Expected exception to be thrown due to statement that does not exist.");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("does not contain value for ThisStatementDoesNotExist"));
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldThrowExceptionIfTryingToAddStatementWithSameName() throws Exception {
    Configuration config = sqlMapper.getConfiguration();
    try {
      config.addMappedStatement(config.getMappedStatement("domain.blog.mappers.BlogMapper.selectBlogWithPostsUsingSubSelect"));
      fail("Expected exception to be thrown due to statement that already exists.");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("already contains value for domain.blog.mappers.BlogMapper.selectBlogWithPostsUsingSubSelect"));
    }
  }

  @Test
  public void shouldCacheAllAuthors() throws Exception {
    int first = -1;
    int second = -1;
    SqlSession session = sqlMapper.openSession();
    try {
      List<Author> authors = session.selectList("com.domain.CachedAuthorMapper.selectAllAuthors");
      first = System.identityHashCode(authors);
      session.commit(); // commit should not be required for read/only activity.
    } finally {
      session.close();
    }
    session = sqlMapper.openSession();
    try {
      List<Author> authors = session.selectList("com.domain.CachedAuthorMapper.selectAllAuthors");
      second = System.identityHashCode(authors);
    } finally {
      session.close();
    }
    assertEquals(first, second);
  }

  @Test
  public void shouldNotCacheAllAuthors() throws Exception {
    int first = -1;
    int second = -1;
    SqlSession session = sqlMapper.openSession();
    try {
      List<Author> authors = session.selectList("domain.blog.mappers.AuthorMapper.selectAllAuthors");
      first = System.identityHashCode(authors);
    } finally {
      session.close();
    }
    session = sqlMapper.openSession();
    try {
      List<Author> authors = session.selectList("domain.blog.mappers.AuthorMapper.selectAllAuthors");
      second = System.identityHashCode(authors);
    } finally {
      session.close();
    }
    assertTrue(first != second);
  }

  @Test
  public void shouldSelectAuthorsUsingMapperClass() {
    SqlSession session = sqlMapper.openSession();
    try {
      AuthorMapper mapper = session.getMapper(AuthorMapper.class);
      List authors = mapper.selectAllAuthors();
      assertEquals(2, authors.size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldExecuteSelectOneAuthorUsingMapperClass() {
    SqlSession session = sqlMapper.openSession();
    try {
      AuthorMapper mapper = session.getMapper(AuthorMapper.class);
      Author author = mapper.selectAuthor(101);
      assertEquals(101, author.getId());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldInsertAuthorUsingMapperClass() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      AuthorMapper mapper = session.getMapper(AuthorMapper.class);
      Author expected = new Author(500, "cbegin", "******", "cbegin@somewhere.com", "Something...", null);
      mapper.insertAuthor(expected);
      Author actual = mapper.selectAuthor(500);
      assertNotNull(actual);
      assertEquals(expected.getId(), actual.getId());
      assertEquals(expected.getUsername(), actual.getUsername());
      assertEquals(expected.getPassword(), actual.getPassword());
      assertEquals(expected.getEmail(), actual.getEmail());
      assertEquals(expected.getBio(), actual.getBio());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldDeleteAuthorUsingMapperClass() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      AuthorMapper mapper = session.getMapper(AuthorMapper.class);
      int count = mapper.deleteAuthor(101);
      assertEquals(1, count);
      try {
        mapper.selectAuthor(101);
        fail("Expected exception.");
      } catch (Exception e) {
        assertEquals(SessionException.class, e.getClass());
      }
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldUpdateAuthorUsingMapperClass() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      AuthorMapper mapper = session.getMapper(AuthorMapper.class);
      Author expected = mapper.selectAuthor(101);
      expected.setUsername("NewUsername");
      int count = mapper.updateAuthor(expected);
      assertEquals(1, count);
      Author actual = mapper.selectAuthor(101);
      assertEquals(expected.getUsername(), actual.getUsername());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectAllPostsUsingMapperClass() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      BlogMapper mapper = session.getMapper(BlogMapper.class);
      List<Map> posts = mapper.selectAllPosts(null);
      assertEquals(5, posts.size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldLimitResultsUsingMapperClass() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      BlogMapper mapper = session.getMapper(BlogMapper.class);
      List<Map> posts = mapper.selectAllPosts(null, 0, 2);
      assertEquals(2, posts.size());
      assertEquals(1, posts.get(0).get("ID"));
      assertEquals(2, posts.get(1).get("ID"));
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldOffsetAndLimitResultsUsingMapperClass() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      BlogMapper mapper = session.getMapper(BlogMapper.class);
      List<Map> posts = mapper.selectAllPosts(2, 3);
      assertEquals(3, posts.size());
      assertEquals(3, posts.get(0).get("ID"));
      assertEquals(4, posts.get(1).get("ID"));
      assertEquals(5, posts.get(2).get("ID"));
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldFindPostsAllPostsWithDynamicSql() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      List<Post> posts = session.selectList("domain.blog.mappers.PostMapper.findPost");
      assertEquals(5,posts.size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldFindPostByIDWithDynamicSql() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      List<Post> posts = session.selectList("domain.blog.mappers.PostMapper.findPost",
          new HashMap() {{
            put("id",1);
          }});
      assertEquals(1, posts.size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldFindPostsInSetOfIDsWithDynamicSql() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      List<Post> posts = session.selectList("domain.blog.mappers.PostMapper.findPost",
          new HashMap() {{
            put("ids",new ArrayList(){{
              add(1);
              add(2);
              add(3);
            }});
          }});
      assertEquals(3, posts.size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldFindPostsWithBlogIdUsingDynamicSql() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      List<Post> posts = session.selectList("domain.blog.mappers.PostMapper.findPost",
          new HashMap() {{
            put("blog_id",1);
          }});
      assertEquals(2, posts.size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldFindPostsWithAuthorIdUsingDynamicSql() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      List<Post> posts = session.selectList("domain.blog.mappers.PostMapper.findPost",
          new HashMap() {{
            put("author_id",101);
          }});
      assertEquals(3, posts.size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldFindPostsWithAuthorAndBlogIdUsingDynamicSql() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      List<Post> posts = session.selectList("domain.blog.mappers.PostMapper.findPost",
          new HashMap() {{
            put("ids",new ArrayList(){{
              add(1);
              add(2);
              add(3);
            }});
            put("blog_id",1);
          }});
      assertEquals(2, posts.size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldFindPostsInList() throws Exception {
    SqlSession session = sqlMapper.openSession();
    try {
      List<Post> posts = session.selectList("domain.blog.mappers.PostMapper.selectPostIn",
          new ArrayList(){{
              add(1);
              add(3);
              add(5);
            }});
      assertEquals(3, posts.size());
    } finally {
      session.close();
    }
  }

}
