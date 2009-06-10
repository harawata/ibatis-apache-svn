package org.apache.ibatis.submitted.selectkey;

import java.io.Reader;

import junit.framework.TestCase;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class SelectKeyTest extends TestCase {

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
