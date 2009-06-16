package org.apache.ibatis.submitted.selectkey;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.*;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import java.io.Reader;

public class SelectKeyTest {

  @Test
  public void testSelectKey() throws Exception {
    // this test checks to make sure that we can have select keys with the same
    // insert id in different namespaces
    String resource = "org/apache/ibatis/submitted/selectkey/MapperConfig.xml";
    Reader reader = Resources.getResourceAsReader(resource);
    SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
    SqlSessionFactory sqlMapper = builder.build(reader);
    assertNotNull(sqlMapper);
  }
}
