package org.apache.ibatis.parser.xml.dynamic;

import static org.junit.Assert.*;
import org.junit.Test;
import domain.blog.*;

public class ExpressionEvaluatorTest {

  private ExpressionEvaluator evaluator = new ExpressionEvaluator();

  @Test
  public void shouldCompareStringsReturnTrue() {
    boolean value = evaluator.evaluate("username == 'cbegin'", new Author(1,"cbegin","******","cbegin@apache.org","N/A", Section.NEWS));
    assertEquals(true, value);
  }

  @Test
  public void shouldCompareStringsReturnFalse() {
    boolean value = evaluator.evaluate("username == 'norm'", new Author(1,"cbegin","******","cbegin@apache.org","N/A", Section.NEWS));
    assertEquals(false, value);
  }

  @Test
  public void shouldReturnTrueIfNotNull() {
    boolean value = evaluator.evaluate("username", new Author(1,"cbegin","******","cbegin@apache.org","N/A", Section.NEWS));
    assertEquals(true, value);
  }

  @Test
  public void shouldReturnFalseIfNull() {
    boolean value = evaluator.evaluate("password", new Author(1,"cbegin",null,"cbegin@apache.org","N/A", Section.NEWS));
    assertEquals(false, value);
  }

  @Test
  public void shouldReturnTrueIfNotZero() {
    boolean value = evaluator.evaluate("id", new Author(1,"cbegin",null,"cbegin@apache.org","N/A", Section.NEWS));
    assertEquals(true, value);
  }

  @Test
  public void shouldReturnFalseIfZero() {
    boolean value = evaluator.evaluate("id", new Author(0,"cbegin",null,"cbegin@apache.org","N/A", Section.NEWS));
    assertEquals(false, value);
  }

}
