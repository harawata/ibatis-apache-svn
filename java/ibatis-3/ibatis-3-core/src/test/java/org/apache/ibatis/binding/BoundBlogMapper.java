package org.apache.ibatis.binding;

import domain.blog.*;
import org.apache.ibatis.annotations.*;

import java.util.*;

@CacheDomain
public interface BoundBlogMapper {

  //======================================================

  Blog selectBlogWithPostsUsingSubSelect(int id);

  //======================================================

  int selectRandom();

  //======================================================

  @Select({
      "SELECT *",
      "FROM blog"
      })
  List<Blog> selectBlogs();

  //======================================================

  List<Blog> selectBlogsFromXML();

  //======================================================

  @Select({
      "SELECT *",
      "FROM blog"
      })
  List<Map> selectBlogsAsMaps();

  //======================================================

  @SelectProvider(type = BoundBlogSql.class, method = "selectBlogsSql")
  List<Blog> selectBlogsUsingProvider();

  //======================================================

  @Select("SELECT * FROM post ORDER BY id")
  @TypeDiscriminator(
      column = "draft",
      javaType = String.class,
      cases = {@Case(value = "1", type = DraftPost.class)}
  )
  List<Post> selectPosts();

  //======================================================

  @Select("SELECT * FROM post ORDER BY id")
  @Results({
    @Result(id = true, property = "id", column = "id")
      })
  @TypeDiscriminator(
      column = "draft",
      javaType = int.class,
      cases = {@Case(value = "1", type = DraftPost.class,
          results = {@Result(id = true, property = "id", column = "id")})}
  )
  List<Post> selectPostsWithResultMap();

  //======================================================

  @Select("SELECT * FROM " +
      "blog WHERE id = #{id}")
  Blog selectBlog(int id);

  //======================================================

  @Select("SELECT * FROM " +
      "blog WHERE id = #{id}")
  Map selectBlogAsMap(Map params);

  //======================================================

//  @Results({
//    @Result(id = true, property = "id", column = "blog_id"),
//    @Result(property = "title", column = "blog_title"),
//    @Result(property = "author", column = "author_id", one = @One(
//        results = @Results({
//          @Result(id = true, property = "id", column = "author_id"),
//          @Result(property = "username", column = "author_username"),
//          @Result(property = "email", column = "author_email")
//            }))),
//    @Result(property = "posts", column = "post_id", many = @Many(
//        javaType = Post.class,
//        results = @Results({
//          @Result(id = true, property = "id", column = "post_id"),
//          @Result(property = "subject", column = "post_subject"),
//          @Result(property = "body", column = "post_body"),
//          @Result(property = "section", column = "post_section"),
//          @Result(id = true, property = "author.id", column = "author_id"),
//          @Result(property = "author.username", column = "author_username"),
//          @Result(property = "author.email", column = "author_email"),
//          @Result(property = "createdOn", column = "post_created_on")
//            , @Result(property = "tags", column = "tag_id", many = @Many(
//            javaType = Tag.class,
//            results = @Results({
//              @Result(id = true, property = "id", column = "tag_id"),
//              @Result(property = "name", column = "tag_name")
//                })))
//            , @Result(property = "comments", column = "comment_id", many = @Many(
//            javaType = Comment.class,
//            results = @Results({
//              @Result(id = true, property = "id", column = "comment_id"),
//              @Result(property = "name", column = "comment_name"),
//              @Result(property = "comment", column = "comment_text")
//                })))
//            })))
//      })
//  @Select("select" +
//      "    B.id as blog_id," +
//      "    B.title as blog_title," +
//      "    B.author_id as blog_author_id," +
//      "    A.id as author_id," +
//      "    A.username as author_username," +
//      "    A.password as author_password," +
//      "    A.email as author_email," +
//      "    A.bio as author_bio," +
//      "    A.favourite_section as author_favourite_section," +
//      "    P.id as post_id," +
//      "    P.blog_id as post_blog_id," +
//      "    P.author_id as post_author_id," +
//      "    P.created_on as post_created_on," +
//      "    P.section as post_section," +
//      "    P.subject as post_subject," +
//      "    P.draft as draft," +
//      "    P.body as post_body," +
//      "    C.id as comment_id," +
//      "    C.post_id as comment_post_id," +
//      "    C.name as comment_name," +
//      "    C.comment as comment_text," +
//      "    T.id as tag_id," +
//      "    T.name as tag_name" +
//      "    from Blog B" +
//      "    left outer join Author A on B.author_id = A.id" +
//      "    left outer join Post P on B.id = P.blog_id" +
//      "    left outer join Comment C on P.id = C.post_id" +
//      "    left outer join Post_Tag PT on PT.post_id = P.id" +
//      "    left outer join Tag T on PT.tag_id = T.id" +
//      "    where B.id = #{id}")
//  List<Blog> selectBlogWithAssociations(int id);

  //======================================================

}
