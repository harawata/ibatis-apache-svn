package org.apache.ibatis.binding;

import domain.blog.*;
import static org.apache.ibatis.annotations.Annotations.*;

import java.util.List;

@CacheDomain
public interface BoundBlogMapper {

//  @ConstructorArgs({
//      @Arg(id = true, column = ""),
//      @Arg(column = ""),
//      @Arg(column = "")
//      })
//  @Results({
//      @Result(column = "A",property = ""),
//      @Result(column = "B",property = "", collectionSelect = "com.domain.Class.method"),
//      @Result(column = "C",property = "", collectionResults = @Results({
//        @Result(column = "X",property = ""),
//        @Result(column = "Y",property = "")
//      }))
//  })
  @Select({
      "SELECT *",
      "FROM blog"
      })
  List<Blog> selectBlogs();

  @Select("SELECT * FROM " +
      "blog WHERE id = #{id}")
  Blog selectBlog(int id);

}
