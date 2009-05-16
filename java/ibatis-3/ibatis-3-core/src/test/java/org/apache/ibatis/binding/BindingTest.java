package org.apache.ibatis.binding;

import domain.blog.*;
import org.apache.ibatis.session.*;
import static org.junit.Assert.*;
import org.junit.*;

import java.util.*;

public class BindingTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setup() throws Exception {
    sqlSessionFactory = new IbatisConfig().getSqlSessionFactory();
  }

  @Test
  public void shouldSelectRandom() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Integer x = mapper.selectRandom();
      assertNotNull(x);
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldExecuteBoundSelectListOfBlogsStatement() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      List<Blog> blogs = mapper.selectBlogs();
      assertEquals(2, blogs.size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectListOfBlogsUsingXMLConfig() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      List<Blog> blogs = mapper.selectBlogsFromXML();
      assertEquals(2, blogs.size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldExecuteBoundSelectListOfBlogsStatementUsingProvider() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      List<Blog> blogs = mapper.selectBlogsUsingProvider();
      assertEquals(2, blogs.size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldExecuteBoundSelectListOfBlogsAsMaps() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      List<Map> blogs = mapper.selectBlogsAsMaps();
      assertEquals(2, blogs.size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectBlogWithAssociations() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      List<Blog> blogs = mapper.selectBlogWithAssociations(1);
      assertEquals(1, blogs.size());
      Blog blog = blogs.get(0);
      assertEquals(2, blog.getPosts().size());
      Post firstPost = blog.getPosts().get(0);
      assertEquals(3, firstPost.getTags().size());
      assertEquals(2, firstPost.getComments().size());
      Post secondPost = blog.getPosts().get(1);
      assertEquals(1, secondPost.getTags().size());
      assertEquals(0, secondPost.getComments().size());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldExecuteBoundSelectOneBlogStatement() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Blog blog = mapper.selectBlog(1);
      assertEquals(1, blog.getId());
      assertEquals("Jim Business", blog.getTitle());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectOneBlogAsMap() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      Map blog = mapper.selectBlogAsMap(new HashMap() {
        {
          put("id", 1);
        }
      });
      assertEquals(1, blog.get("ID"));
      assertEquals("Jim Business", blog.get("TITLE"));
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectOneAuthor() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundAuthorMapper mapper = session.getMapper(BoundAuthorMapper.class);
      Author author = mapper.selectAuthor(101);
      assertEquals(101, author.getId());
      assertEquals("jim", author.getUsername());
      assertEquals("********", author.getPassword());
      assertEquals("jim@ibatis.apache.org", author.getEmail());
      assertEquals("", author.getBio());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectOneAuthorByConstructor() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundAuthorMapper mapper = session.getMapper(BoundAuthorMapper.class);
      Author author = mapper.selectAuthorConstructor(101);
      assertEquals(101, author.getId());
      assertEquals("jim", author.getUsername());
      assertEquals("********", author.getPassword());
      assertEquals("jim@ibatis.apache.org", author.getEmail());
      assertEquals("", author.getBio());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectDraftTypedPosts() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      List<Post> posts = mapper.selectPosts();
      assertEquals(5, posts.size());
      assertEquals(DraftPost.class, posts.get(0).getClass());
      assertEquals(Post.class, posts.get(1).getClass());
      assertEquals(DraftPost.class, posts.get(2).getClass());
      assertEquals(Post.class, posts.get(3).getClass());
      assertEquals(Post.class, posts.get(4).getClass());
    } finally {
      session.close();
    }
  }

  @Test
  public void shouldSelectDraftTypedPostsWithResultMap() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      List<Post> posts = mapper.selectPostsWithResultMap();
      assertEquals(5, posts.size());
      assertEquals(DraftPost.class, posts.get(0).getClass());
      assertEquals(Post.class, posts.get(1).getClass());
      assertEquals(DraftPost.class, posts.get(2).getClass());
      assertEquals(Post.class, posts.get(3).getClass());
      assertEquals(Post.class, posts.get(4).getClass());
    } finally {
      session.close();
    }
  }


}
