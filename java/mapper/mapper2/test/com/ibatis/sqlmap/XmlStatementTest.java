package com.ibatis.sqlmap;

import com.ibatis.common.minixml.MiniDom;
import com.ibatis.common.minixml.MiniParser;

import java.sql.SQLException;

/**
 * <p/>
 * Date: May 4, 2004 10:04:28 PM
 * 
 * @author Clinton Begin
 */
public class XmlStatementTest extends BaseSqlMapTest {

  // SETUP & TEARDOWN

  protected void setUp() throws Exception {
    initSqlMap("com/ibatis/sqlmap/maps/SqlMapConfig.xml", null);
    initScript("scripts/account-init.sql");
    initScript("scripts/order-init.sql");
    initScript("scripts/line_item-init.sql");
  }

  protected void tearDown() throws Exception {
  }


  public void testExecuteQueryForXml() throws SQLException {
    String account = (String) sqlMap.queryForObject("getAccountXml", "<parameter><id>1</id></parameter>");
    assertNotNull(account);
    MiniDom dom = new MiniParser(account).getDom();
    assertEquals("1", dom.getValue("ID"));
    assertEquals("Clinton", dom.getValue("FIRSTNAME"));
    assertEquals("Begin", dom.getValue("LASTNAME"));
    assertEquals("clinton.begin@ibatis.com", dom.getValue("EMAILADDRESS"));
  }


  public void testExecuteQueryForXmlExternalMaps() throws SQLException {
    String account = (String) sqlMap.queryForObject("getAccountXmlExternalMaps", "<parameter><id>1</id></parameter>");
    assertNotNull(account);
    MiniDom dom = new MiniParser(account).getDom();
    assertEquals("1", dom.getValue("id"));
    assertEquals("Clinton", dom.getValue("firstName"));
    assertEquals("Begin", dom.getValue("lastName"));
    assertEquals("clinton.begin@ibatis.com", dom.getValue("emailAddress"));
    assertEquals("1", dom.getValue("account.ID"));
    assertEquals("Clinton", dom.getValue("account.FIRSTNAME"));
    assertEquals("Begin", dom.getValue("account.LASTNAME"));
    assertEquals("clinton.begin@ibatis.com", dom.getValue("account.EMAILADDRESS"));
  }

  public void testExecuteQueryForOrderXml() throws SQLException {
    String order = (String) sqlMap.queryForObject("getOrderXml", "<parameter><id>1</id></parameter>");
    assertNotNull(order);
    MiniDom dom = new MiniParser(order).getDom();
    assertEquals("1", dom.getValue("id"));
    assertEquals("2", dom.getValue("lineItems.lineItem.ID"));
  }

}
