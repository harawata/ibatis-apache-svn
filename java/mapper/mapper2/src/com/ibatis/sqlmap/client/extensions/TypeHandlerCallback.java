package com.ibatis.sqlmap.client.extensions;

import java.sql.SQLException;

/**
 * A simple interface for implementing custom type handlers.
 * <p/>
 * Using this interface, you can implement a type handler that
 * will perform customized processing before parameters are set
 * on a PreparedStatement and after values are retrieved from
 * a ResultSet.  Using a custom type handler you can extend
 * the framework to handle types that are not supported, or
 * handle supported types in a different way.  For example,
 * you might use a custom type handler to implement proprietary
 * BLOB support (e.g. Oracle), or you might use it to handle
 * booleans using "Y" and "N" instead of the more typical 0/1.
 * <p/>
 */
public interface TypeHandlerCallback {

  /**
   * Performs processing on a value before it is used to set
   * the parameter of a PreparedStatement.
   *
   * @param setter The interface for setting the value on the PreparedStatement.
   * @param parameter The value to be set.
   * @throws SQLException If any error occurs.
   */
  public void setParameter(ParameterSetter setter, Object parameter)
      throws SQLException;

  /**
   * Performs processing on a value before after it has been retrieved
   * from a ResultSet.
   *
   * @param getter The interface for getting the value from the ResultSet.
   * @return The processed value.
   * @throws SQLException If any error occurs.
   */
  public Object getResult(ResultGetter getter)
      throws SQLException;

  /**
   * Casts the string representation of a value into a type recognized by
   * this type handler.  This method is used to translate nullValue values
   * into types that can be appropriately compared.  If your custom type handler
   * cannot support nullValues, or if there is no reasonable string representation
   * for this type (e.g. File type), you can simply return the String representation
   * as it was passed in.  It is not recommended to return null, unless null was passed
   * in.
   *
   * @param s A string representation of a valid value for this type.
   * @return One of the following:
   * <ol>
   *   <li>the casted repersentation of the String value,</li>
   *   <li>the string as is,</li>
   *   <li>null, only if null was passed in.</li> 
   * </ol>
   */
  public Object valueOf(String s);

}
