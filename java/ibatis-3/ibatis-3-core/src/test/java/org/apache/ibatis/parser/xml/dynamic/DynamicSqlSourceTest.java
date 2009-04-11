package org.apache.ibatis.parser.xml.dynamic;

import static org.junit.Assert.*;
import org.junit.Test;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.ognl.Ognl;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.session.*;
import org.apache.ibatis.io.Resources;

import java.util.*;
import java.io.*;
import java.sql.SQLException;

public class DynamicSqlSourceTest extends BaseDataTest {

  @Test
  public void shouldDemonstrateSimpleExpectedTextWithNoLoopsOrConditionals() throws Exception {
    final String expected = "SELECT * FROM BLOG";
    final MixedSqlNode sqlNode = mixedContents(new TextSqlNode(expected));
    DynamicSqlSource source = createDynamicSqlSource(sqlNode);
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldDemonstrateMultipartExpectedTextWithNoLoopsOrConditionals() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new TextSqlNode("WHERE ID = ?"));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldConditionallyIncludeWhere() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new IfSqlNode("true",
            mixedContents(new TextSqlNode("WHERE ID = ?"))));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldConditionallyExcludeWhere() throws Exception {
    final String expected = "SELECT * FROM BLOG";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new IfSqlNode("false",
            mixedContents(new TextSqlNode("WHERE ID = ?"))));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldConditionallyDefault() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE CATEGORY = 'DEFAULT'";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"), 
        new ChooseSqlNode(new ArrayList() {{
          add(new IfSqlNode("false",
            mixedContents(new TextSqlNode("WHERE CATEGORY = ?"))));
          add(new IfSqlNode("false",
            mixedContents(new TextSqlNode("WHERE CATEGORY = 'NONE'"))));
        }},mixedContents(new TextSqlNode("WHERE CATEGORY = 'DEFAULT'"))));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldConditionallyChooseFirst() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE CATEGORY = ?";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new ChooseSqlNode(new ArrayList() {{
          add(new IfSqlNode("true",
            mixedContents(new TextSqlNode("WHERE CATEGORY = ?"))));
          add(new IfSqlNode("false",
            mixedContents(new TextSqlNode("WHERE CATEGORY = 'NONE'"))));
        }},mixedContents(new TextSqlNode("WHERE CATEGORY = 'DEFAULT'"))));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldConditionallyChooseSecond() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE CATEGORY = 'NONE'";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG"),
        new ChooseSqlNode(new ArrayList() {{
          add(new IfSqlNode("false",
            mixedContents(new TextSqlNode("WHERE CATEGORY = ?"))));
          add(new IfSqlNode("true",
            mixedContents(new TextSqlNode("WHERE CATEGORY = 'NONE'"))));
        }},mixedContents(new TextSqlNode("WHERE CATEGORY = 'DEFAULT'"))));
    BoundSql boundSql = source.getBoundSql(null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  public void shouldIterateOnceForEachItemInCollection() throws Exception {
    final HashMap<String,String[]> parameterObject = new HashMap() {{
        put("array", new String[]{"1", "2", "3"});
      }};
    final String expected = "SELECT * FROM BLOG WHERE ID in [ ? , ? , ? ]";
    DynamicSqlSource source = createDynamicSqlSource(
        new TextSqlNode("SELECT * FROM BLOG WHERE ID in"),
        new ForEachSqlNode("array","[","]",",",mixedContents(new TextSqlNode("?"))));
    BoundSql boundSql = source.getBoundSql(parameterObject);
    assertEquals(expected, boundSql.getSql());
  }


  private DynamicSqlSource createDynamicSqlSource(SqlNode... contents) throws IOException, SQLException {
    createBlogDataSource();
    final String resource = "org/apache/ibatis/parser/MapperConfig.xml";
    final Reader reader = Resources.getResourceAsReader(resource);
    SqlSessionFactory sqlMapper = new SqlSessionFactoryBuilder().build(reader);
    Configuration configuration = sqlMapper.getConfiguration();
    MixedSqlNode sqlNode = mixedContents(contents);
    return new DynamicSqlSource(configuration, sqlNode);
  }

  private MixedSqlNode mixedContents(SqlNode... contents) {
    return new MixedSqlNode(Arrays.asList(contents));
  }

}
