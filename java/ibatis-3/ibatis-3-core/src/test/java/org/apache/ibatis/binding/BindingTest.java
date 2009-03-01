package org.apache.ibatis.binding;

import domain.blog.*;
import org.apache.ibatis.session.*;
import static org.junit.Assert.assertEquals;
import org.junit.*;

import java.util.*;

public class BindingTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setup() throws Exception {
    sqlSessionFactory = new IbatisConfig().getSqlSessionFactory();
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
      for (Blog blog : blogs) {
        System.out.println(blog);
        for (Post post : blog.getPosts()) {
          System.out.println("    " + post);
          if (post.getComments() != null) {
            for (Comment comment : post.getComments()) {
              System.out.println("        " + comment);
            }
          }
          if (post.getTags() != null) {
            for (Tag tag : post.getTags()) {
              System.out.println("        " + tag);
            }
          }
        }
      }
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


}
