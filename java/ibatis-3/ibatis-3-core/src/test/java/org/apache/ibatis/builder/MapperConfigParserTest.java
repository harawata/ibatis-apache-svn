package org.apache.ibatis.builder;

import org.junit.Test;
import org.junit.Assert;
import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.mapping.Configuration;
import org.apache.ibatis.io.Resources;

import javax.sql.DataSource;
import java.io.Reader;

public class MapperConfigParserTest extends BaseDataTest {

  @Test
  public void shouldBuildBlogMappers() throws Exception {
    DataSource ds = createBlogDataSource();
    final String resource = "org/apache/ibatis/builder/MapperConfig.xml";
    final Reader reader = Resources.getResourceAsReader(resource);
    MapperConfigParser parser = new MapperConfigParser(reader,null);

    parser.parse();
    Configuration config = parser.getConfiguration();

    Assert.assertEquals(1,config.getCaches().size());
  }

}
