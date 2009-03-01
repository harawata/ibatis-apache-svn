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

  @Results({
      @Result(id=true,property="id",column="blog_id"),
      @Result(property="title",column="blog_title"),
      @Result(property="author.id",column="author_id"),
      @Result(property="author.username",column="author_username"),
      @Result(property="author.email",column="author_email")
      })
  @Select("select" +
      "    B.id as blog_id," +
      "    B.title as blog_title," +
      "    B.author_id as blog_author_id," +
      "    A.id as author_id," +
      "    A.username as author_username," +
      "    A.password as author_password," +
      "    A.email as author_email," +
      "    A.bio as author_bio," +
      "    A.favourite_section as author_favourite_section," +
      "    P.id as post_id," +
      "    P.blog_id as post_blog_id," +
      "    P.author_id as post_author_id," +
      "    P.created_on as post_created_on," +
      "    P.section as post_section," +
      "    P.subject as post_subject," +
      "    P.draft as draft," +
      "    P.body as post_body," +
      "    C.id as comment_id," +
      "    C.post_id as comment_post_id," +
      "    C.name as comment_name," +
      "    C.comment as comment_text," +
      "    T.id as tag_id," +
      "    T.name as tag_name" +
      "    from Blog B" +
      "    left outer join Author A on B.author_id = A.id" +
      "    left outer join Post P on B.id = P.blog_id" +
      "    left outer join Comment C on P.id = C.post_id" +
      "    left outer join Post_Tag PT on PT.post_id = P.id" +
      "    left outer join Tag T on PT.tag_id = T.id" +
      "    where B.id = #{id}")
  List<Blog> selectBlogWithAssociations(int id);

}
