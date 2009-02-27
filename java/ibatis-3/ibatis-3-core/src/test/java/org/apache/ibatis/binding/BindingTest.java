package org.apache.ibatis.binding;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSession;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import domain.blog.*;

import java.util.List;

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
      assertEquals(2,blogs.size());
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
      assertEquals(1,blog.getId());
      assertEquals("Jim Business",blog.getTitle());
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
      assertEquals(101,author.getId());
      assertEquals("jim",author.getUsername());
      assertEquals("********",author.getPassword());
      assertEquals("jim@ibatis.apache.org",author.getEmail());
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
      assertEquals(101,author.getId());
      assertEquals("jim",author.getUsername());
      assertEquals("********",author.getPassword());
      assertEquals("jim@ibatis.apache.org",author.getEmail());
      assertEquals("", author.getBio());
    } finally {
      session.close();
    }
  }


}
