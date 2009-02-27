package org.apache.ibatis.binding;

import static org.apache.ibatis.annotations.Annotations.*;
import domain.blog.Author;

public interface BoundAuthorMapper {

  @ConstructorArgs({
      @Arg(column = "AUTHOR_ID",javaType = int.class)
      })
  @Results({
//    @Result(property="id",column = "AUTHOR_ID"),
      @Result(property = "username",column = "AUTHOR_USERNAME"),
      @Result(property = "password",column = "AUTHOR_PASSWORD"),
      @Result(property = "email",column = "AUTHOR_EMAIL"),
      @Result(property = "bio",column = "AUTHOR_BIO")
      })
  @Select({
      "SELECT ",
      "  ID as AUTHOR_ID,",
      "  USERNAME as AUTHOR_USERNAME,",
      "  PASSWORD as AUTHOR_PASSWORD,",
      "  EMAIL as AUTHOR_EMAIL,",
      "  BIO as AUTHOR_BIO",
      "FROM AUTHOR WHERE ID = #{id}"})
  Author selectAuthor(int id);

}
