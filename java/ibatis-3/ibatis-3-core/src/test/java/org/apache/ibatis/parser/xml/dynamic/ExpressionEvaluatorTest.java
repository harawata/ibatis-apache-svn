package org.apache.ibatis.parser.xml.dynamic;

import static org.junit.Assert.*;
import org.junit.Test;
import domain.blog.*;

import java.util.HashMap;

public class ExpressionEvaluatorTest {

  private ExpressionEvaluator evaluator = new ExpressionEvaluator();

  @Test
  public void shouldCompareStringsReturnTrue() {
    boolean value = evaluator.isTrue("username == 'cbegin'", new Author(1,"cbegin","******","cbegin@apache.org","N/A", Section.NEWS));
    assertEquals(true, value);
  }

  @Test
  public void shouldCompareStringsReturnFalse() {
    boolean value = evaluator.isTrue("username == 'norm'", new Author(1,"cbegin","******","cbegin@apache.org","N/A", Section.NEWS));
    assertEquals(false, value);
  }

  @Test
  public void shouldReturnTrueIfNotNull() {
    boolean value = evaluator.isTrue("username", new Author(1,"cbegin","******","cbegin@apache.org","N/A", Section.NEWS));
    assertEquals(true, value);
  }

  @Test
  public void shouldReturnFalseIfNull() {
    boolean value = evaluator.isTrue("password", new Author(1,"cbegin",null,"cbegin@apache.org","N/A", Section.NEWS));
    assertEquals(false, value);
  }

  @Test
  public void shouldReturnTrueIfNotZero() {
    boolean value = evaluator.isTrue("id", new Author(1,"cbegin",null,"cbegin@apache.org","N/A", Section.NEWS));
    assertEquals(true, value);
  }

  @Test
  public void shouldReturnFalseIfZero() {
    boolean value = evaluator.isTrue("id", new Author(0,"cbegin",null,"cbegin@apache.org","N/A", Section.NEWS));
    assertEquals(false, value);
  }

  @Test
  public void shouldIterateOverIterable() {
    final HashMap<String,String[]> parameterObject = new HashMap() {{
        put("array", new String[]{"1", "2", "3"});
      }};
    final Iterable iterable = evaluator.getIterable("array", parameterObject);
    int i = 0;
    for(Object o: iterable) {
      assertEquals(String.valueOf(++i),o);
    }
  }


}
