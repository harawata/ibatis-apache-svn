package org.apache.ibatis.executor;

import domain.blog.*;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.*;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.type.*;

import java.util.*;

public class ExecutorTestHelper {

  public static final Cache authorCache;

  static {
    PerpetualCache cache = new PerpetualCache();
    cache.setId("author_cache");
    authorCache =
        new SynchronizedCache(
            new SerializedCache(
                new LoggingCache(
                    new ScheduledCache(
                        cache, 5000))));

  }

  public static MappedStatement prepareInsertAuthorMappedStatement(final Configuration config) {
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    MappedStatement ms = new MappedStatement.Builder(config, "insertAuthor", new BasicSqlSource("INSERT INTO author (id,username,password,email,bio,favourite_section) values(?,?,?,?,?,?)"))
        .parameterMap(
            new ParameterMap.Builder(
                config, "defaultParameterMap", Author.class,
                new ArrayList<ParameterMapping>() {
                  {
                    add(new ParameterMapping.Builder(config, "id", registry.getTypeHandler(int.class)).build());
                    add(new ParameterMapping.Builder(config, "username", registry.getTypeHandler(String.class)).build());
                    add(new ParameterMapping.Builder(config, "password", registry.getTypeHandler(String.class)).build());
                    add(new ParameterMapping.Builder(config, "email", registry.getTypeHandler(String.class)).build());
                    add(new ParameterMapping.Builder(config, "bio", registry.getTypeHandler(String.class)).jdbcType(JdbcType.VARCHAR).build());
                    add(new ParameterMapping.Builder(config, "favouriteSection", registry.getTypeHandler(Section.class)).jdbcType(JdbcType.VARCHAR).build());
                  }
                }).build())
        .cache(authorCache).build();
    return ms;
  }

  public static MappedStatement prepareInsertAuthorMappedStatementWithAutoKey(final Configuration config) {
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    MappedStatement ms = new MappedStatement.Builder(config, "insertAuthor", new BasicSqlSource("INSERT INTO author (username,password,email,bio,favourite_section) values(?,?,?,?,?)"))
        .parameterMap(
            new ParameterMap.Builder(config, "defaultParameterMap", Author.class, new ArrayList<ParameterMapping>() {
              {
                add(new ParameterMapping.Builder(config, "username", registry.getTypeHandler(String.class)).build());
                add(new ParameterMapping.Builder(config, "password", registry.getTypeHandler(String.class)).build());
                add(new ParameterMapping.Builder(config, "email", registry.getTypeHandler(String.class)).build());
                add(new ParameterMapping.Builder(config, "bio", registry.getTypeHandler(String.class)).jdbcType(JdbcType.VARCHAR).build());
                add(new ParameterMapping.Builder(config, "favouriteSection", registry.getTypeHandler(Section.class)).jdbcType(JdbcType.VARCHAR).build());
              }
            }).build())
        .cache(authorCache)
        .build();
    return ms;
  }

  public static MappedStatement prepareInsertAuthorProc(final Configuration config) {
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    MappedStatement ms = new MappedStatement.Builder(config, "insertAuthorProc", new BasicSqlSource("{call insertAuthor(?,?,?,?)}"))
        .parameterMap(new ParameterMap.Builder(config, "defaultParameterMap", Author.class,
            new ArrayList<ParameterMapping>() {
              {
                add(new ParameterMapping.Builder(config, "id", registry.getTypeHandler(int.class)).build());
                add(new ParameterMapping.Builder(config, "username", registry.getTypeHandler(String.class)).build());
                add(new ParameterMapping.Builder(config, "password", registry.getTypeHandler(String.class)).build());
                add(new ParameterMapping.Builder(config, "email", registry.getTypeHandler(String.class)).build());
              }
            }).build())
        .cache(authorCache).build();
    return ms;
  }

  public static MappedStatement prepareUpdateAuthorMappedStatement(final Configuration config) {
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    MappedStatement ms = new MappedStatement.Builder(config, "updateAuthor", new BasicSqlSource("UPDATE author SET username = ?, password = ?, email = ?, bio = ? WHERE id = ?"))
        .parameterMap(new ParameterMap.Builder(config, "defaultParameterMap", Author.class,
            new ArrayList<ParameterMapping>() {
              {
                add(new ParameterMapping.Builder(config, "username", registry.getTypeHandler(String.class)).build());
                add(new ParameterMapping.Builder(config, "password", registry.getTypeHandler(String.class)).build());
                add(new ParameterMapping.Builder(config, "email", registry.getTypeHandler(String.class)).build());
                add(new ParameterMapping.Builder(config, "bio", registry.getTypeHandler(String.class)).jdbcType(JdbcType.VARCHAR).build());
                add(new ParameterMapping.Builder(config, "id", registry.getTypeHandler(int.class)).build());
              }
            }).build())
        .cache(authorCache).build();
    return ms;
  }

  public static MappedStatement prepareDeleteAuthorMappedStatement(final Configuration config) {
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    MappedStatement ms = new MappedStatement.Builder(config, "deleteAuthor", new BasicSqlSource("DELETE FROM author WHERE id = ?"))
        .parameterMap(new ParameterMap.Builder(config, "defaultParameterMap", Author.class,
            new ArrayList<ParameterMapping>() {
              {
                add(new ParameterMapping.Builder(config, "id", registry.getTypeHandler(int.class)).build());
              }
            }).build())
        .cache(authorCache)
        .build();
    return ms;
  }

  public static MappedStatement prepareSelectOneAuthorMappedStatement(final Configuration config) {
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();

    final ResultMap rm = new ResultMap.Builder(config, "defaultResultMap", Author.class, new
        ArrayList<ResultMapping>() {
          {
            add(new ResultMapping.Builder(config, "id", "id", registry.getTypeHandler(int.class)).build());
            add(new ResultMapping.Builder(config, "username", "username", registry.getTypeHandler(String.class)).build());
            add(new ResultMapping.Builder(config, "password", "password", registry.getTypeHandler(String.class)).build());
            add(new ResultMapping.Builder(config, "email", "email", registry.getTypeHandler(String.class)).build());
            add(new ResultMapping.Builder(config, "bio", "bio", registry.getTypeHandler(String.class)).build());
            add(new ResultMapping.Builder(config, "favouriteSection", "favourite_section", registry.getTypeHandler(Section.class)).build());
          }
        }).build();

    MappedStatement ms = new MappedStatement.Builder(config, "selectAuthor", new BasicSqlSource("SELECT * FROM author WHERE id = ?"))
        .parameterMap(new ParameterMap.Builder(config, "defaultParameterMap", Author.class,
            new ArrayList<ParameterMapping>() {
              {
                add(new ParameterMapping.Builder(config, "id", registry.getTypeHandler(int.class)).build());
              }
            }).build())
        .resultMaps(new ArrayList<ResultMap>() {
          {
            add(rm);
          }
        })
        .cache(authorCache).build();
    return ms;
  }

  public static MappedStatement prepareSelectAllAuthorsAutoMappedStatement(final Configuration config) {
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    return new MappedStatement.Builder(config, "selectAuthorAutoMap", new BasicSqlSource("SELECT * FROM author ORDER BY id"))
        .resultMaps(new ArrayList<ResultMap>() {
          {
            add(new ResultMap.Builder(config, "defaultResultMap", Author.class, new ArrayList() {
              {
                add(new ResultMapping.Builder(config, "favouriteSection", "favourite_section", registry.getTypeHandler(Section.class)).build());
                add(new ResultMapping.Builder(config, null, "not_exists",Object.class).build());
              }
            }).build());
          }
        }).build();
  }

  public static MappedStatement prepareSelectOneAuthorMappedStatementWithConstructorResults(final Configuration config) {
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    MappedStatement ms = new MappedStatement.Builder(config, "selectAuthor", new BasicSqlSource("SELECT * FROM author WHERE id = ?"))
        .parameterMap(new ParameterMap.Builder(config, "defaultParameterMap", Author.class,
            new ArrayList<ParameterMapping>() {
              {
                add(new ParameterMapping.Builder(config, "id", registry.getTypeHandler(int.class)).build());
              }
            }).build())
        .resultMaps(new ArrayList<ResultMap>() {
          {
            add(new ResultMap.Builder(config, "defaultResultMap", Author.class, new ArrayList<ResultMapping>() {
              {
                add(new ResultMapping.Builder(config, "id", "id", registry.getTypeHandler(int.class)).javaType(int.class).flags(new ArrayList<ResultFlag>() {
                  {
                    add(ResultFlag.CONSTRUCTOR);
                  }
                }).build());
                add(new ResultMapping.Builder(config, "username", "username", registry.getTypeHandler(String.class)).build());
                add(new ResultMapping.Builder(config, "password", "password", registry.getTypeHandler(String.class)).build());
                add(new ResultMapping.Builder(config, "email", "email", registry.getTypeHandler(String.class)).build());
                add(new ResultMapping.Builder(config, "bio", "bio", registry.getTypeHandler(String.class)).build());
                add(new ResultMapping.Builder(config, "favouriteSection", "favourite_section", registry.getTypeHandler(Section.class)).build());
              }
            }).build());
          }
        })
        .cache(authorCache)
        .build();
    return ms;
  }

  public static MappedStatement prepareSelectTwoSetsOfAuthorsProc(final Configuration config) {
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    MappedStatement ms = new MappedStatement.Builder(config, "selectTwoSetsOfAuthors", new BasicSqlSource("{call selectTwoSetsOfAuthors(?,?)}"))
        .statementType(StatementType.CALLABLE)
        .parameterMap(new ParameterMap.Builder(
            config, "defaultParameterMap", Author.class,
            new ArrayList<ParameterMapping>() {
              {
                add(new ParameterMapping.Builder(config, "id1", registry.getTypeHandler(int.class)).build());
                add(new ParameterMapping.Builder(config, "id2", registry.getTypeHandler(int.class)).build());
              }
            }).build())
        .resultMaps(new ArrayList<ResultMap>() {
          {
            ResultMap map = new ResultMap.Builder(config, "defaultResultMap", Author.class, new ArrayList<ResultMapping>() {
              {
                add(new ResultMapping.Builder(config, "id", "id", registry.getTypeHandler(int.class)).build());
                add(new ResultMapping.Builder(config, "username", "username", registry.getTypeHandler(String.class)).build());
                add(new ResultMapping.Builder(config, "password", "password", registry.getTypeHandler(String.class)).build());
                add(new ResultMapping.Builder(config, "email", "email", registry.getTypeHandler(String.class)).build());
                add(new ResultMapping.Builder(config, "bio", "bio", registry.getTypeHandler(String.class)).build());
              }
            }).build();
            add(map);
            add(map);
          }
        }).build();
    return ms;
  }

  public static MappedStatement prepareSelectAuthorViaOutParams(final Configuration config) {
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    MappedStatement ms = new MappedStatement.Builder(config, "selectAuthorViaOutParams", new BasicSqlSource("{call selectAuthorViaOutParams(?,?,?,?,?)}"))
        .statementType(StatementType.CALLABLE)
        .parameterMap(new ParameterMap.Builder(config, "defaultParameterMap", Author.class,
            new ArrayList<ParameterMapping>() {
              {
                add(new ParameterMapping.Builder(config, "id", registry.getTypeHandler(int.class)).build());
                add(new ParameterMapping.Builder(config, "username", registry.getTypeHandler(String.class)).jdbcType(JdbcType.VARCHAR).mode(ParameterMode.OUT).build());
                add(new ParameterMapping.Builder(config, "password", registry.getTypeHandler(String.class)).jdbcType(JdbcType.VARCHAR).mode(ParameterMode.OUT).build());
                add(new ParameterMapping.Builder(config, "email", registry.getTypeHandler(String.class)).jdbcType(JdbcType.VARCHAR).mode(ParameterMode.OUT).build());
                add(new ParameterMapping.Builder(config, "bio", registry.getTypeHandler(String.class)).jdbcType(JdbcType.VARCHAR).mode(ParameterMode.OUT).build());
              }
            }).build())
        .resultMaps(new ArrayList<ResultMap>())
        .cache(authorCache).build();
    return ms;
  }

  public static MappedStatement prepareSelectDiscriminatedProduct(final Configuration config) {
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    final ResultMap discriminatorResultMap = new ResultMap.Builder(config, "petResultMap", HashMap.class, new ArrayList<ResultMapping>() {
      {
        add(new ResultMapping.Builder(config, "name", "name", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "descn", "descn", registry.getTypeHandler(String.class)).build());
      }
    }).build();
    config.addResultMap(discriminatorResultMap);
    MappedStatement ms = new MappedStatement.Builder(config, "selectProducts", new BasicSqlSource("SELECT * FROM product"))
        .resultMaps(new ArrayList<ResultMap>() {
          {
            add(new ResultMap.Builder(config, "defaultResultMap", HashMap.class, new ArrayList<ResultMapping>() {
              {
                add(new ResultMapping.Builder(config, "productid", "productid", registry.getTypeHandler(String.class)).build());
                add(new ResultMapping.Builder(config, "category", "category", registry.getTypeHandler(String.class)).build());
              }
            })
                .discriminator(new Discriminator.Builder(
                    config, new ResultMapping.Builder(config, "category", "category", registry.getTypeHandler(String.class)).build(),
                    new HashMap<String, String>() {
                      {
                        put("CATS", discriminatorResultMap.getId());
                        put("DOGS", discriminatorResultMap.getId());
                        put("BIRDS", discriminatorResultMap.getId());
                        put("FISH", discriminatorResultMap.getId());
                        //Reptiles left out on purpose.
                      }
                    }).build()).build());

          }
        }).build();
    return ms;
  }

  public static MappedStatement createInsertAuthorWithIDof99MappedStatement(final Configuration config) {
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    MappedStatement ms = new MappedStatement.Builder(config, "insertAuthor", new BasicSqlSource("INSERT INTO author (id,username,password,email,bio) values(99,'someone','******','someone@apache.org',null)"))
        .statementType(StatementType.STATEMENT)
        .parameterMap(new ParameterMap.Builder(config, "defaultParameterMap", Author.class,
            new ArrayList<ParameterMapping>()).build())
        .cache(authorCache)
        .build();
    return ms;
  }

  public static MappedStatement createSelectAuthorWithIDof99MappedStatement(final Configuration config) {
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    MappedStatement ms = new MappedStatement.Builder(config, "selectAuthor", new BasicSqlSource("SELECT * FROM author WHERE id = 99"))
        .statementType(StatementType.STATEMENT)
        .parameterMap(new ParameterMap.Builder(config, "defaultParameterMap", Author.class, new ArrayList<ParameterMapping>()).build())
        .resultMaps(new ArrayList<ResultMap>() {
          {
            add(new ResultMap.Builder(config, "defaultResultMap", Author.class, new ArrayList<ResultMapping>() {
              {
                add(new ResultMapping.Builder(config, "id", "id", registry.getTypeHandler(int.class)).build());
                add(new ResultMapping.Builder(config, "username", "username", registry.getTypeHandler(String.class)).build());
                add(new ResultMapping.Builder(config, "password", "password", registry.getTypeHandler(String.class)).build());
                add(new ResultMapping.Builder(config, "email", "email", registry.getTypeHandler(String.class)).build());
                add(new ResultMapping.Builder(config, "bio", "bio", registry.getTypeHandler(String.class)).build());
              }
            }).build());
          }
        }).build();
    return ms;
  }

  public static MappedStatement prepareComplexSelectBlogMappedStatement(final Configuration config) {
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    final SqlSource sqlSource = new BasicSqlSource("SELECT b.id, b.author_id, b.title, a.username, a.password, a.email, a.bio" +
        " FROM blog b" +
        " INNER JOIN author a ON b.author_id = a.id" +
        " WHERE b.id = ?");
    final ParameterMap parameterMap = new ParameterMap.Builder(config, "defaultParameterMap", int.class,
        new ArrayList<ParameterMapping>() {
          {
            add(new ParameterMapping.Builder(config, "id", registry.getTypeHandler(int.class)).build());
          }
        }).build();
    final ResultMap resultMap = new ResultMap.Builder(config, "defaultResultMap", Blog.class, new ArrayList<ResultMapping>() {
      {
        add(new ResultMapping.Builder(config, "id", "id", registry.getTypeHandler(int.class))
            .flags(new ArrayList<ResultFlag>() {
              {
                add(ResultFlag.ID);
              }
            }).build());
        add(new ResultMapping.Builder(config, "title", "title", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "author.id", "author_id", registry.getTypeHandler(int.class)).build());
        add(new ResultMapping.Builder(config, "author.username", "username", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "author.password", "password", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "author.email", "email", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "author.bio", "bio", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "posts", "id", registry.getTypeHandler(int.class)).javaType(List.class).nestedQueryId("selectPostsForBlog").build());
      }
    }).build();

    return new MappedStatement.Builder(config, "selectBlogById", sqlSource)
        .parameterMap(parameterMap)
        .resultMaps(new ArrayList<ResultMap>() {
          {
            add(resultMap);
          }
        }).build();
  }

  public static MappedStatement prepareSelectBlogByIdAndAuthor(final Configuration config) {
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    final SqlSource sqlSource = new BasicSqlSource("SELECT b.id, b.author_id, b.title, a.username, a.password, a.email, a.bio" +
        " FROM blog b" +
        " INNER JOIN author a ON b.author_id = a.id" +
        " WHERE b.id = ? and a.id = ?");
    final ParameterMap parameterMap = new ParameterMap.Builder(config, "defaultParameterMap", Map.class,
        new ArrayList<ParameterMapping>() {
          {
            add(new ParameterMapping.Builder(config, "blogId", registry.getTypeHandler(int.class)).build());
            add(new ParameterMapping.Builder(config, "authorId", registry.getTypeHandler(int.class)).build());
          }
        }).build();
    final ResultMap resultMap = new ResultMap.Builder(config, "defaultResultMap", Blog.class, new ArrayList<ResultMapping>() {
      {
        add(new ResultMapping.Builder(config, "id", "id", registry.getTypeHandler(int.class))
            .flags(new ArrayList<ResultFlag>() {
              {
                add(ResultFlag.ID);
              }
            }).build());
        add(new ResultMapping.Builder(config, "title", "title", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "author.id", "author_id", registry.getTypeHandler(int.class)).build());
        add(new ResultMapping.Builder(config, "author.username", "username", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "author.password", "password", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "author.email", "email", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "author.bio", "bio", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "posts", "id", registry.getTypeHandler(int.class)).javaType(List.class).nestedQueryId("selectPostsForBlog").build());
      }
    }).build();

    return new MappedStatement.Builder(config, "selectBlogByIdAndAuthor", sqlSource)
        .parameterMap(parameterMap)
        .resultMaps(new ArrayList<ResultMap>() {
          {
            add(resultMap);
          }
        }).build();

  }

  public static MappedStatement prepareSelectPostsForBlogMappedStatement(final Configuration config) {
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    final SqlSource sqlSource = new BasicSqlSource("SELECT p.id, p.created_on, p.blog_id, p.section, p.subject, p.body, pt.tag_id," +
        " t.name as tag_name, c.id as comment_id, c.name as comment_name, c.comment" +
        " FROM post p" +
        " INNER JOIN post_tag pt ON pt.post_id = p.id" +
        " INNER JOIN tag t ON pt.tag_id = t.id" +
        " LEFT OUTER JOIN comment c ON c.post_id = p.id" +
        " WHERE p.blog_id = ?");
    final ParameterMap parameterMap = new ParameterMap.Builder(config, "defaultParameterMap", Author.class,
        new ArrayList<ParameterMapping>() {
          {
            add(new ParameterMapping.Builder(config, "id", registry.getTypeHandler(int.class)).build());
          }
        }).build();
    final ResultMap tagResultMap = new ResultMap.Builder(config, "tagResultMap", Tag.class, new ArrayList<ResultMapping>() {
      {
        add(new ResultMapping.Builder(config, "id", "tag_id", registry.getTypeHandler(int.class))
            .flags(new ArrayList() {
              {
                add(ResultFlag.ID);
              }
            }).build());
        add(new ResultMapping.Builder(config, "name", "tag_name", registry.getTypeHandler(String.class)).build());
      }
    }).build();
    final ResultMap commentResultMap = new ResultMap.Builder(config, "commentResultMap", Comment.class, new ArrayList<ResultMapping>() {
      {
        add(new ResultMapping.Builder(config, "id", "comment_id", registry.getTypeHandler(int.class))
            .flags(new ArrayList<ResultFlag>() {
              {
                add(ResultFlag.ID);
              }
            }).build());
        add(new ResultMapping.Builder(config, "name", "comment_name", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "comment", "comment", registry.getTypeHandler(String.class)).build());
      }
    }).build();
    config.addResultMap(tagResultMap);
    config.addResultMap(commentResultMap);
    final ResultMap postResultMap = new ResultMap.Builder(config, "defaultResultMap", Post.class, new ArrayList<ResultMapping>() {
      {
        add(new ResultMapping.Builder(config, "id", "id", registry.getTypeHandler(int.class))
            .flags(new ArrayList<ResultFlag>() {
              {
                add(ResultFlag.ID);
              }
            }).build());
        add(new ResultMapping.Builder(config, "blog", "blog_id", registry.getTypeHandler(int.class)).javaType(Blog.class).nestedQueryId("selectBlogById").build());
        add(new ResultMapping.Builder(config, "createdOn", "created_on", registry.getTypeHandler(Date.class)).build());
        add(new ResultMapping.Builder(config, "section", "section", registry.getTypeHandler(Section.class)).build());
        add(new ResultMapping.Builder(config, "subject", "subject", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "body", "body", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "tags").nestedResultMapId(tagResultMap.getId()).build());
        add(new ResultMapping.Builder(config, "comments").nestedResultMapId(commentResultMap.getId()).build());
      }
    }).build();
    return new MappedStatement.Builder(config, "selectPostsForBlog", sqlSource)
        .parameterMap(parameterMap)
        .resultMaps(new ArrayList<ResultMap>() {
          {
            add(postResultMap);
          }
        }).build();
  }

  public static MappedStatement prepareSelectPostMappedStatement(final Configuration config) {
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    final SqlSource sqlSource = new BasicSqlSource("SELECT p.id, p.created_on, p.blog_id, p.section, p.subject, p.body, pt.tag_id," +
        " t.name as tag_name, c.id as comment_id, c.name as comment_name, c.comment" +
        " FROM post p" +
        " LEFT OUTER JOIN post_tag pt ON pt.post_id = p.id" +
        " LEFT OUTER JOIN tag t ON pt.tag_id = t.id" +
        " LEFT OUTER JOIN comment c ON c.post_id = p.id" +
        " WHERE p.id = ?");
    final ParameterMap parameterMap = new ParameterMap.Builder(config, "defaultParameterMap", Author.class,
        new ArrayList<ParameterMapping>() {
          {
            add(new ParameterMapping.Builder(config, "id", registry.getTypeHandler(int.class)).build());
          }
        }).build();
    final ResultMap tagResultMap = new ResultMap.Builder(config, "tagResultMap", Tag.class, new ArrayList<ResultMapping>() {
      {
        add(new ResultMapping.Builder(config, "id", "tag_id", registry.getTypeHandler(int.class))
            .flags(new ArrayList() {
              {
                add(ResultFlag.ID);
              }
            }).build());
        add(new ResultMapping.Builder(config, "name", "tag_name", registry.getTypeHandler(String.class)).build());
      }
    }).build();
    final ResultMap commentResultMap = new ResultMap.Builder(config, "commentResultMap", Comment.class, new ArrayList<ResultMapping>() {
      {
        add(new ResultMapping.Builder(config, "id", "comment_id", registry.getTypeHandler(int.class))
            .flags(new ArrayList<ResultFlag>() {
              {
                add(ResultFlag.ID);
              }
            }).build());
        add(new ResultMapping.Builder(config, "name", "comment_name", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "comment", "comment", registry.getTypeHandler(String.class)).build());
      }
    }).build();
    config.addResultMap(tagResultMap);
    config.addResultMap(commentResultMap);
    final ResultMap postResultMap = new ResultMap.Builder(config, "", Post.class, new ArrayList<ResultMapping>() {
      {
        add(new ResultMapping.Builder(config, "id", "id", registry.getTypeHandler(int.class))
            .flags(new ArrayList<ResultFlag>() {
              {
                add(ResultFlag.ID);
              }
            }).build());
        add(new ResultMapping.Builder(config, "blog", "blog_id", registry.getTypeHandler(int.class)).javaType(Blog.class).nestedQueryId("selectBlogById").build());
        add(new ResultMapping.Builder(config, "createdOn", "created_on", registry.getTypeHandler(Date.class)).build());
        add(new ResultMapping.Builder(config, "section", "section", registry.getTypeHandler(Section.class)).build());
        add(new ResultMapping.Builder(config, "subject", "subject", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "body", "body", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "tags").nestedResultMapId(tagResultMap.getId()).build());
        add(new ResultMapping.Builder(config, "comments").nestedResultMapId(commentResultMap.getId()).build());
      }
    }).build();


    return new MappedStatement.Builder(config, "selectPostsForBlog", sqlSource)
        .parameterMap(parameterMap)
        .resultMaps(new ArrayList<ResultMap>() {
          {
            add(postResultMap);
          }
        }).build();
  }


  public static MappedStatement prepareSelectPostWithBlogByAuthorMappedStatement(final Configuration config) {
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    final SqlSource sqlSource = new BasicSqlSource("SELECT p.id, p.created_on, p.blog_id, p.author_id, p.section, p.subject, p.body, pt.tag_id," +
        " t.name as tag_name, c.id as comment_id, c.name as comment_name, c.comment" +
        " FROM post p" +
        " LEFT OUTER JOIN post_tag pt ON pt.post_id = p.id" +
        " LEFT OUTER JOIN tag t ON pt.tag_id = t.id" +
        " LEFT OUTER JOIN comment c ON c.post_id = p.id" +
        " WHERE p.id = ?");
    final ParameterMap parameterMap = new ParameterMap.Builder(config, "defaultParameterMap", Author.class,
        new ArrayList<ParameterMapping>() {
          {
            add(new ParameterMapping.Builder(config, "id", registry.getTypeHandler(int.class)).build());
          }
        }).build();
    final ResultMap tagResultMap = new ResultMap.Builder(config, "tagResultMap", Tag.class, new ArrayList<ResultMapping>() {
      {
        add(new ResultMapping.Builder(config, "id", "tag_id", registry.getTypeHandler(int.class))
            .flags(new ArrayList() {
              {
                add(ResultFlag.ID);
              }
            }).build());
        add(new ResultMapping.Builder(config, "name", "tag_name", registry.getTypeHandler(String.class)).build());
      }
    }).build();
    final ResultMap commentResultMap = new ResultMap.Builder(config, "commentResultMap", Comment.class, new ArrayList<ResultMapping>() {
      {
        add(new ResultMapping.Builder(config, "id", "comment_id", registry.getTypeHandler(int.class))
            .flags(new ArrayList<ResultFlag>() {
              {
                add(ResultFlag.ID);
              }
            }).build());
        add(new ResultMapping.Builder(config, "name", "comment_name", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "comment", "comment", registry.getTypeHandler(String.class)).build());
      }
    }).build();
    config.addResultMap(tagResultMap);
    config.addResultMap(commentResultMap);
    final ResultMap postResultMap = new ResultMap.Builder(config, "postResultMap", Post.class, new ArrayList<ResultMapping>() {
      {
        add(new ResultMapping.Builder(config, "id", "id", registry.getTypeHandler(int.class))
            .flags(new ArrayList<ResultFlag>() {
              {
                add(ResultFlag.ID);
              }
            }).build());

        add(new ResultMapping.Builder(config, "blog").nestedQueryId("selectBlogByIdAndAuthor").composites(new ArrayList<ResultMapping>() {
          {
            add(new ResultMapping.Builder(config, "authorId", "author_id", registry.getTypeHandler(int.class)).build());
            add(new ResultMapping.Builder(config, "blogId", "blog_id", registry.getTypeHandler(int.class)).build());
          }
        }).build());
        add(new ResultMapping.Builder(config, "createdOn", "created_on", registry.getTypeHandler(Date.class)).build());
        add(new ResultMapping.Builder(config, "section", "section", registry.getTypeHandler(Section.class)).build());
        add(new ResultMapping.Builder(config, "subject", "subject", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "body", "body", registry.getTypeHandler(String.class)).build());
        add(new ResultMapping.Builder(config, "tags").nestedResultMapId(tagResultMap.getId()).build());
        add(new ResultMapping.Builder(config, "comments").nestedResultMapId(commentResultMap.getId()).build());
      }
    }).build();


    return new MappedStatement.Builder(config, "selectPostsForBlog", sqlSource)
        .parameterMap(parameterMap)
        .resultMaps(new ArrayList<ResultMap>() {
          {
            add(postResultMap);
          }
        }).build();
  }

}
