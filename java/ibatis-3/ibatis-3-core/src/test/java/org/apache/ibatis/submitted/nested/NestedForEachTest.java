package org.apache.ibatis.submitted.nested;

import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

public class NestedForEachTest {

    protected static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void setUp() throws Exception {
        Connection conn = null;

        try {
            Class.forName("org.hsqldb.jdbcDriver");
            conn = DriverManager.getConnection("jdbc:hsqldb:mem:cname", "sa",
                    "");

            Reader reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/nested/CreateDB.sql");

            ScriptRunner runner = new ScriptRunner(conn);
            runner.setLogWriter(null);
            runner.setErrorLogWriter(new PrintWriter(System.err));
            runner.runScript(reader);
            conn.commit();
            reader.close();

            reader = Resources.getResourceAsReader("org/apache/ibatis/submitted/nested/MapperConfig.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
            reader.close();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSimpleSelect() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            Name name = new Name();
            name.setLastName("Flintstone");
            Parameter parameter = new Parameter();
            parameter.addName(name);
            
            List<Map<String, Object>> answer =
                sqlSession.selectList("org.apache.ibatis.submitted.nested.simpleSelect", parameter);
            
            assertEquals(3, answer.size());
        } finally {
            sqlSession.close();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNestedSelect() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            Name name = new Name();
            name.setLastName("Flintstone");
            name.addFirstName("Fred");
            name.addFirstName("Wilma");
            
            Parameter parameter = new Parameter();
            parameter.addName(name);
            
            List<Map<String, Object>> answer =
                sqlSession.selectList("org.apache.ibatis.submitted.nested.nestedSelect", parameter);
            
            assertEquals(2, answer.size());
        } finally {
            sqlSession.close();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNestedSelect2() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            Name name = new Name();
            name.setLastName("Flintstone");
            name.addFirstName("Fred");
            name.addFirstName("Wilma");
            
            Parameter parameter = new Parameter();
            parameter.addName(name);
            
            name = new Name();
            name.setLastName("Rubble");
            name.addFirstName("Betty");
            parameter.addName(name);
            
            List<Map<String, Object>> answer =
                sqlSession.selectList("org.apache.ibatis.submitted.nested.nestedSelect", parameter);
            
            assertEquals(3, answer.size());
        } finally {
            sqlSession.close();
        }
    }
}
