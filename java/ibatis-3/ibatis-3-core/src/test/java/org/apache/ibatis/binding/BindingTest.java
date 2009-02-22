package org.apache.ibatis.binding;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSession;
import org.junit.BeforeClass;
import org.junit.Test;
import domain.blog.Blog;

import java.util.List;

public class BindingTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setup() throws Exception {
    sqlSessionFactory = new IbatisConfig().getSqlSessionFactory();
  }

  @Test
  public void shouldExecuteBoundSelectStatement() {
    SqlSession session = sqlSessionFactory.openSession();
    try {
      BoundBlogMapper mapper = session.getMapper(BoundBlogMapper.class);
      List<Blog> blogs = mapper.selectBlogs();
      System.out.println(blogs.size());
    } finally {
      session.close();
    }

  }


}
