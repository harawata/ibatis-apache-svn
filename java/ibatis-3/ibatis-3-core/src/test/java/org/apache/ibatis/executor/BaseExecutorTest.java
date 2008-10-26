package org.apache.ibatis.executor;

import domain.blog.*;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.jdbc.PooledDataSource;
import org.apache.ibatis.mapping.*;
import org.junit.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public abstract class BaseExecutorTest extends BaseDataTest {
  private final Configuration config;

  protected BaseExecutorTest() {
    config = new Configuration();
    config.setEnhancementEnabled(true);
    config.setLazyLoadingEnabled(true);
    config.setGeneratedKeysEnabled(false);
    config.setMultipleResultSetsEnabled(true);
    config.setUseColumnLabel(true);
    config.setDefaultStatementTimeout(5000);
  }

  @Test
  public void shouldInsertNewAuthor() throws Exception {
    DataSource ds = createBlogDataSource();
    Connection connection = ds.getConnection();
    Executor executor = createExecutor(connection);
    Author author = new Author(99, "someone", "******", "someone@apache.org", null, Section.NEWS);
    MappedStatement insertStatement = ExecutorTestHelper.prepareInsertAuthorMappedStatement(config);
    MappedStatement selectStatement = ExecutorTestHelper.prepareSelectOneAuthorMappedStatement(config);
    int rows = executor.update(insertStatement, author);
    List<Author> authors = executor.query(selectStatement, 99, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, Executor.NO_RESULT_HANDLER);
    executor.flushStatements();
    executor.rollback(true);
    Assert.assertEquals(1, authors.size());
    Assert.assertEquals(author.toString(), authors.get(0).toString());
    Assert.assertTrue(1 == rows || BatchExecutor.BATCH_UPDATE_RETURN_VALUE == rows);
  }

  @Test
  public void shouldSelectAllAuthorsAutoMapped() throws Exception {
    DataSource ds = createBlogDataSource();
    Connection connection = ds.getConnection();
    Executor executor = createExecutor(connection);
    MappedStatement selectStatement = ExecutorTestHelper.prepareSelectAllAuthorsAutoMappedStatement(config);
    List<Author> authors = executor.query(selectStatement, null, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, Executor.NO_RESULT_HANDLER);
    Assert.assertEquals(2, authors.size());
    Author author = authors.get(0);
    // id,username, password, email, bio, favourite_section
    // (101,'jim','********','jim@ibatis.apache.org','','NEWS');
    Assert.assertEquals(101, author.getId());
    Assert.assertEquals("jim", author.getUsername());
    Assert.assertEquals("jim@ibatis.apache.org", author.getEmail());
    Assert.assertEquals("", author.getBio());
    Assert.assertEquals(Section.NEWS, author.getFavouriteSection());
  }

  @Test
  public void shouldInsertNewAuthorWithAutoKey() throws Exception {
    DataSource ds = createBlogDataSource();
    Connection connection = ds.getConnection();
    config.setGeneratedKeysEnabled(true);
    try {
      Executor executor = createExecutor(connection);
      Author author = new Author(-1, "someone", "******", "someone@apache.org", null, Section.NEWS);
      MappedStatement insertStatement = ExecutorTestHelper.prepareInsertAuthorMappedStatementWithAutoKey(config);
      MappedStatement selectStatement = ExecutorTestHelper.prepareSelectOneAuthorMappedStatement(config);
      int id = executor.update(insertStatement, author);
      if (id != BatchExecutor.BATCH_UPDATE_RETURN_VALUE) {
        author.setId(id);
        List<Author> authors = executor.query(selectStatement, id, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, Executor.NO_RESULT_HANDLER);
        executor.flushStatements();
        executor.rollback(true);
        Assert.assertEquals(1, authors.size());
        Assert.assertEquals(author.toString(), authors.get(0).toString());
        Assert.assertTrue(id >= 10000);
      }
    } finally {
      config.setGeneratedKeysEnabled(false);
    }
  }

  @Test
  public void shouldInsertNewAuthorByProc() throws Exception {
    DataSource ds = createBlogDataSource();
    Connection connection = ds.getConnection();
    Executor executor = createExecutor(connection);
    Author author = new Author(97, "someone", "******", "someone@apache.org", null, null);
    MappedStatement insertStatement = ExecutorTestHelper.prepareInsertAuthorProc(config);
    MappedStatement selectStatement = ExecutorTestHelper.prepareSelectOneAuthorMappedStatement(config);
    int rows = executor.update(insertStatement, author);
    List<Author> authors = executor.query(selectStatement, 97, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, Executor.NO_RESULT_HANDLER);
    executor.flushStatements();
    executor.rollback(true);
    Assert.assertEquals(1, authors.size());
    Assert.assertEquals(author.toString(), authors.get(0).toString());
  }

  @Test
  public void shouldInsertNewAuthorUsingSimpleNonPreparedStatements() throws Exception {
    DataSource ds = createBlogDataSource();
    Connection connection = ds.getConnection();
    Executor executor = createExecutor(connection);
    Author author = new Author(99, "someone", "******", "someone@apache.org", null, null);
    MappedStatement insertStatement = ExecutorTestHelper.createInsertAuthorWithIDof99MappedStatement(config);
    MappedStatement selectStatement = ExecutorTestHelper.createSelectAuthorWithIDof99MappedStatement(config);
    int rows = executor.update(insertStatement, null);
    List<Author> authors = executor.query(selectStatement, 99, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, Executor.NO_RESULT_HANDLER);
    executor.flushStatements();
    executor.rollback(true);
    Assert.assertEquals(1, authors.size());
    Assert.assertEquals(author.toString(), authors.get(0).toString());
    Assert.assertTrue(1 == rows || BatchExecutor.BATCH_UPDATE_RETURN_VALUE == rows);
  }

  @Test
  public void shouldUpdateAuthor() throws Exception {
    DataSource ds = createBlogDataSource();
    Connection connection = ds.getConnection();
    Executor executor = createExecutor(connection);
    Author author = new Author(101, "someone", "******", "someone@apache.org", null, Section.NEWS);
    MappedStatement updateStatement = ExecutorTestHelper.prepareUpdateAuthorMappedStatement(config);
    MappedStatement selectStatement = ExecutorTestHelper.prepareSelectOneAuthorMappedStatement(config);
    int rows = executor.update(updateStatement, author);
    List<Author> authors = executor.query(selectStatement, 101, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, Executor.NO_RESULT_HANDLER);
    executor.flushStatements();
    executor.rollback(true);
    Assert.assertEquals(1, authors.size());
    Assert.assertEquals(author.toString(), authors.get(0).toString());
    Assert.assertTrue(1 == rows || BatchExecutor.BATCH_UPDATE_RETURN_VALUE == rows);
  }

  @Test
  public void shouldDeleteAuthor() throws Exception {
    DataSource ds = createBlogDataSource();
    Connection connection = ds.getConnection();
    Executor executor = createExecutor(connection);
    Author author = new Author(101, null, null, null, null, null);
    MappedStatement deleteStatement = ExecutorTestHelper.prepareDeleteAuthorMappedStatement(config);
    MappedStatement selectStatement = ExecutorTestHelper.prepareSelectOneAuthorMappedStatement(config);
    int rows = executor.update(deleteStatement, author);
    List<Author> authors = executor.query(selectStatement, 101, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, Executor.NO_RESULT_HANDLER);
    executor.flushStatements();
    executor.rollback(true);
    Assert.assertEquals(0, authors.size());
    Assert.assertTrue(1 == rows || BatchExecutor.BATCH_UPDATE_RETURN_VALUE == rows);
  }

  @Test
  public void shouldSelectDiscriminatedProduct() throws Exception {
    DataSource ds = createJPetstoreDataSource();
    Connection connection = ds.getConnection();
    Executor executor = createExecutor(connection);
    MappedStatement selectStatement = ExecutorTestHelper.prepareSelectDiscriminatedProduct(config);
    List<Map> products = executor.query(selectStatement, null, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, Executor.NO_RESULT_HANDLER);
    connection.rollback();
    Assert.assertEquals(16, products.size());
    for (Map m : products) {
      if ("REPTILES".equals(m.get("category"))) {
        Assert.assertNull(m.get("name"));
      } else {
        Assert.assertNotNull(m.get("name"));
      }
    }
  }

  @Test
  public void shouldSelect10DiscriminatedProducts() throws Exception {
    DataSource ds = createJPetstoreDataSource();
    Connection connection = ds.getConnection();
    Executor executor = createExecutor(connection);
    MappedStatement selectStatement = ExecutorTestHelper.prepareSelectDiscriminatedProduct(config);
    List<Map> products = executor.query(selectStatement, null, 4, 10, Executor.NO_RESULT_HANDLER);
    connection.rollback();
    Assert.assertEquals(10, products.size());
    for (Map m : products) {
      if ("REPTILES".equals(m.get("category"))) {
        Assert.assertNull(m.get("name"));
      } else {
        Assert.assertNotNull(m.get("name"));
      }
    }
  }

  @Test
  public void shouldSelectTwoSetsOfAuthorsViaProc() throws Exception {
    DataSource ds = createBlogDataSource();
    Connection connection = ds.getConnection();
    connection.setAutoCommit(false);
    Executor executor = createExecutor(connection);
    MappedStatement selectStatement = ExecutorTestHelper.prepareSelectTwoSetsOfAuthorsProc(config);
    List<List> authorSets = executor.query(selectStatement, new HashMap() {
      {
        put("id1", 101);
        put("id2", 102);
      }
    }, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, Executor.NO_RESULT_HANDLER);
    connection.rollback();
    Assert.assertEquals(2, authorSets.size());
    for (List authors : authorSets) {
      Assert.assertEquals(2, authors.size());
      for (Object author : authors) {
        Assert.assertTrue(author instanceof Author);
      }
    }
  }

  @Test
  public void shouldSelectAuthorViaOutParams() throws Exception {
    DataSource ds = createBlogDataSource();
    Connection connection = ds.getConnection();
    connection.setAutoCommit(false);
    Executor executor = createExecutor(connection);
    MappedStatement selectStatement = ExecutorTestHelper.prepareSelectAuthorViaOutParams(config);
    Author author = new Author(102, null, null, null, null, null);
    executor.query(selectStatement, author, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, Executor.NO_RESULT_HANDLER);
    connection.rollback();

    Assert.assertEquals("sally", author.getUsername());
    Assert.assertEquals("********", author.getPassword());
    Assert.assertEquals("sally@ibatis.apache.org", author.getEmail());
    Assert.assertEquals(null, author.getBio());
  }

  @Test
  public void shouldFetchPostsForBlog() throws Exception {
    DataSource ds = createBlogDataSource();
    Connection connection = ds.getConnection();
    Executor executor = createExecutor(connection);
    MappedStatement selectBlog = ExecutorTestHelper.prepareComplexSelectBlogMappedStatement(config);
    MappedStatement selectPosts = ExecutorTestHelper.prepareSelectPostsForBlogMappedStatement(config);
    config.addMappedStatement(selectBlog);
    config.addMappedStatement(selectPosts);
    List<Post> posts = executor.query(selectPosts, 1, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, Executor.NO_RESULT_HANDLER);
    executor.flushStatements();
    Assert.assertEquals(2, posts.size());
    Assert.assertNotNull(posts.get(1).getBlog());
    Assert.assertEquals(1, posts.get(1).getBlog().getId());
    executor.rollback(true);
  }

  @Test
  public void shouldFetchOneOrphanedPostWithNoBlog() throws Exception {
    DataSource ds = createBlogDataSource();
    Connection connection = ds.getConnection();
    Executor executor = createExecutor(connection);
    MappedStatement selectBlog = ExecutorTestHelper.prepareComplexSelectBlogMappedStatement(config);
    MappedStatement selectPost = ExecutorTestHelper.prepareSelectPostMappedStatement(config);
    config.addMappedStatement(selectBlog);
    config.addMappedStatement(selectPost);
    List<Post> posts = executor.query(selectPost, 5, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, Executor.NO_RESULT_HANDLER);
    executor.flushStatements();
    executor.rollback(true);
    Assert.assertEquals(1, posts.size());
    Post post = posts.get(0);
    Assert.assertNull(post.getBlog());
  }

  @Test
  public void shouldFetchPostWithBlogWithCompositeKey() throws Exception {
    DataSource ds = createBlogDataSource();
    Connection connection = ds.getConnection();
    Executor executor = createExecutor(connection);
    MappedStatement selectBlog = ExecutorTestHelper.prepareSelectBlogByIdAndAuthor(config);
    MappedStatement selectPost = ExecutorTestHelper.prepareSelectPostWithBlogByAuthorMappedStatement(config);
    config.addMappedStatement(selectBlog);
    config.addMappedStatement(selectPost);
    List<Post> posts = executor.query(selectPost, 2, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, Executor.NO_RESULT_HANDLER);
    executor.flushStatements();
    Assert.assertEquals(1, posts.size());
    Post post = posts.get(0);
    Assert.assertNotNull(post.getBlog());
    Assert.assertEquals(101, post.getBlog().getAuthor().getId());
    executor.rollback(true);
  }


  @Test
  public void shouldFetchComplexBlogs() throws Exception {
    DataSource ds = createBlogDataSource();
    Connection connection = ds.getConnection();
    try {
      Executor executor = createExecutor(connection);
      MappedStatement selectBlog = ExecutorTestHelper.prepareComplexSelectBlogMappedStatement(config);
      MappedStatement selectPosts = ExecutorTestHelper.prepareSelectPostsForBlogMappedStatement(config);
      config.addMappedStatement(selectBlog);
      config.addMappedStatement(selectPosts);
      config.setLazyLoadingEnabled(false);
      List<Blog> blogs = executor.query(selectBlog, 1, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, Executor.NO_RESULT_HANDLER);
      executor.flushStatements();
      executor.rollback(true);
      Assert.assertEquals(1, blogs.size());
      Assert.assertEquals(2, blogs.get(0).getPosts().size());
      Assert.assertEquals(1, blogs.get(0).getPosts().get(1).getBlog().getPosts().get(1).getBlog().getId());
    } finally {
      config.setLazyLoadingEnabled(true);
    }
  }

  @Test
  public void shouldMapConstructorResults() throws Exception {
    DataSource ds = createBlogDataSource();
    Connection connection = ds.getConnection();
    Executor executor = createExecutor(connection);
    MappedStatement selectStatement = ExecutorTestHelper.prepareSelectOneAuthorMappedStatementWithConstructorResults(config);
    List<Author> authors = executor.query(selectStatement, 102, Executor.NO_ROW_OFFSET, Executor.NO_ROW_LIMIT, Executor.NO_RESULT_HANDLER);
    executor.flushStatements();
    executor.rollback(true);
    Assert.assertEquals(1, authors.size());

    Author author = authors.get(0);
    Assert.assertEquals(102, author.getId());
  }

  protected abstract Executor createExecutor(Connection connection);

}
