package org.apache.ibatis.xml;

import org.junit.*;

import java.util.*;

public class GenericTokenParserTest {

  public static class VariableTokenHandler implements GenericTokenParser.TokenHandler {
    private Map<String,String> variables = new HashMap<String,String>();
    public VariableTokenHandler(Map<String, String> variables) {
      this.variables = variables;
    }
    public String handleToken(String content) {
      return variables.get(content);
    }
  }

  @Test
  public void shouldDemonstrateGenericTokenReplacement() {
    GenericTokenParser parser = new GenericTokenParser("${","}",new VariableTokenHandler(new HashMap<String,String>() {{
      put("first_name","James");
      put("initial","T");
      put("last_name","Kirk");
    }}));

    Assert.assertEquals("James T Kirk reporting.",parser.parse("${first_name} ${initial} ${last_name} reporting."));
    Assert.assertEquals("Hello captain James T Kirk",parser.parse("Hello captain ${first_name} ${initial} ${last_name}"));
    Assert.assertEquals("James T Kirk",parser.parse("${first_name} ${initial} ${last_name}"));
    Assert.assertEquals("JamesTKirk",parser.parse("${first_name}${initial}${last_name}"));
    Assert.assertEquals("${",parser.parse("${"));
    Assert.assertEquals("}",parser.parse("}"));
    Assert.assertEquals("Hello ${ this is a test.",parser.parse("Hello ${ this is a test."));
    Assert.assertEquals("Hello } this is a test.",parser.parse("Hello } this is a test."));
    Assert.assertEquals("Hello } ${ this is a test.",parser.parse("Hello } ${ this is a test."));
  }

}
