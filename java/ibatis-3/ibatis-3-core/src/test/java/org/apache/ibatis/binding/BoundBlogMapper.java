package org.apache.ibatis.binding;

import domain.blog.Blog;
import static org.apache.ibatis.annotations.Annotations.*;

@Cache
public interface BoundBlogMapper {

  @ConstructorArgs({
      @Arg(id = true, column = ""),
      @Arg(column = ""),
      @Arg(column = "")
      })
  @Results({
      @Result(column = "A",property = ""),
      @Result(column = "B",property = "", collectionSelect = "com.domain.Class.method"),
      @Result(column = "C",property = "", collectionResults = @Results({
        @Result(column = "X",property = ""),
        @Result(column = "Y",property = "")
      }))
  })
  @Select({
      "SELECT *",
      "FROM BLOG",
      "where id = ${id}"
      })
  Blog selectBlog(int id);

}
