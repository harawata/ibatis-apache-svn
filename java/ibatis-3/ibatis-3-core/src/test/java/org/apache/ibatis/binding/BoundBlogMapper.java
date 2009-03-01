package org.apache.ibatis.binding;

import domain.blog.*;
import static org.apache.ibatis.annotations.Annotations.*;

import java.util.*;

@CacheDomain
public interface BoundBlogMapper {

  @Select({
      "SELECT *",
      "FROM blog"
      })
  List<Blog> selectBlogs();

  @Select({
      "SELECT *",
      "FROM blog"
      })
  List<Map> selectBlogsAsMaps();

  @Select("SELECT * FROM " +
      "blog WHERE id = #{id}")
  Blog selectBlog(int id);

  @Select("SELECT * FROM " +
      "blog WHERE id = #{id}")
  Map selectBlogAsMap(Map params);

}
