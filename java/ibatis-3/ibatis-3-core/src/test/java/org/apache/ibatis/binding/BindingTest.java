package org.apache.ibatis.binding;

import org.apache.ibatis.api.SqlSessionFactory;
import org.junit.BeforeClass;
import org.junit.Test;

public class BindingTest {
  private static SqlSessionFactory sqlSessionFactory;

  @BeforeClass
  public static void setup() throws Exception {
    sqlSessionFactory = new IbatisConfig().getSqlSessionFactory();
  }

  @Test
  public void foo() {
  
  }


}
