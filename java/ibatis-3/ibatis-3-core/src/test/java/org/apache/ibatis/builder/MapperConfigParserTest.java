package org.apache.ibatis.builder;

import org.junit.Test;
import org.apache.ibatis.BaseDataTest;

import javax.sql.DataSource;

public class MapperConfigParserTest extends BaseDataTest {

  @Test
  public void shouldBuildBlogMappers() throws Exception {
    DataSource ds = createBlogDataSource();

  }

}
