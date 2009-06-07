package org.apache.ibatis.submitted.includes;

import java.io.Reader;

import junit.framework.TestCase;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class IncludeTest extends TestCase {

    public void testIncludes() throws Exception {
        String resource = "org/apache/ibatis/submitted/includes/MapperConfig.xml";
        Reader reader = Resources.getResourceAsReader(resource);
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        SqlSessionFactory sqlMapper = builder.build(reader);
        assertNotNull(sqlMapper);
    }
}
