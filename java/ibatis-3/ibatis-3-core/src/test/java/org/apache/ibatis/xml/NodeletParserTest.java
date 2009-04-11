package org.apache.ibatis.xml;

import domain.misc.Employee;
import org.apache.ibatis.io.Resources;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.io.Reader;
import java.util.*;

public class NodeletParserTest {

  @Test
  public void shouldParseAttribute() throws Exception {
    NodeletParser parser = new NodeletParser();
    NodeHandler handler = new NodeHandler();
    parser.addNodeletHandler(handler);
    parser.setVariables(new Properties() {
      {
        setProperty("id_var", "1234567890");
      }
    });
    Reader resource = Resources.getResourceAsReader("resources/nodelet_test.xml");
    parser.parse(resource);
    Employee emp = handler.getEmployee();
    assertEquals(1234567890, emp.getId());
    assertEquals("Jim", emp.getFirstName());
    assertEquals("Smith", emp.getLastName());
    assertEquals(new Date(1970 - 1900, 6 - 1, 15), emp.getBirthDate());
    assertEquals(5.8, emp.getHeight());
    assertEquals("ft", emp.getHeightUnits());
    assertEquals(200, emp.getWeight());
    assertEquals("lbs", emp.getWeightUnits());
  }


  public static class NodeHandler {

    private Employee employee = new Employee();
    private int year;
    private int month;
    private int day;

    public Employee getEmployee() {
      return employee;
    }

    @Nodelet("/employee")
    public void id(NodeletContext node) {
      employee.setId(node.getIntAttribute("id", 0));
    }

    @Nodelet("/employee/first_name")
    public void firstName(NodeletContext node) {
      employee.setFirstName(node.getStringBody(""));
    }

    @Nodelet("/employee/last_name")
    public void lastName(NodeletContext node) {
      employee.setLastName(node.getStringBody(""));
    }

    @Nodelet("/employee/birth_date/year")
    public void year(NodeletContext node) {
      year = node.getIntBody(0);
    }

    @Nodelet("/employee/birth_date/month")
    public void month(NodeletContext node) {
      month = node.getIntBody(0);
    }

    @Nodelet("/employee/birth_date/day")
    public void day(NodeletContext node) {
      day = node.getIntBody(0);
    }

    @Nodelet("/employee/birth_date/end()")
    public void birth_date(NodeletContext node) {
      employee.setBirthDate(new Date(year - 1900, month - 1, day));
    }

    @Nodelet("/employee/height")
    public void height(NodeletContext node) {
      employee.setHeight(node.getDoubleBody(0.0));
      employee.setHeightUnits(node.getStringAttribute("units", ""));
    }

    @Nodelet("/employee/weight")
    public void weight(NodeletContext node) {
      employee.setWeight(node.getDoubleBody(0.0));
      employee.setWeightUnits(node.getStringAttribute("units", ""));
    }

  }

}
