/**
 * User: Clinton Begin
 * Date: May 19, 2003
 * Time: 1:11:15 PM
 */
package testdomain;

import java.io.Serializable;
import java.util.List;

public class Account implements Serializable {

  private int id;
  private String firstName;
  private String lastName;
  private String emailAddress;
  private int[] ids;
  private Account account;
  private List accountList;

  public Account() {
  }

  public Account(int id) {
    this.id = id;
  }

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

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account account) {
    this.account = account;
  }

  public List getAccountList() {
    return accountList;
  }

  public void setAccountList(List accountList) {
    this.accountList = accountList;
  }

}
