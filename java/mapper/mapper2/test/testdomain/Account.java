/**
 * User: Clinton Begin
 * Date: May 19, 2003
 * Time: 1:11:15 PM
 */
package testdomain;

public class Account {

  private int id;
  private String firstName;
  private String lastName;
  private String emailAddress;
  private int[] ids;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int[] getIds() {
    return ids;
  }

  public void setIds(int[] ids) {
    this.ids = ids;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

}
