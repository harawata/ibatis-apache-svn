package com.ibatis.jpetstore.presentation;

import com.ibatis.common.util.PaginatedList;
import com.ibatis.jpetstore.domain.Account;
import com.ibatis.jpetstore.service.AccountService;
import com.ibatis.jpetstore.service.CatalogService;
import com.ibatis.struts.ActionContext;
import com.ibatis.struts.BaseBean;
import com.ibatis.struts.BeanActionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class AccountBean extends BaseBean {

  /* Constants */

  private static final AccountService accountService = AccountService.getInstance();
  private static final CatalogService catalogService = CatalogService.getInstance();

  private static final String VALIDATE_NEW_ACCOUNT = "new";
  private static final String VALIDATE_EDIT_ACCOUNT = "edit";

  private static final List LANGUAGE_LIST;
  private static final List CATEGORY_LIST;

  /* Private Fields */

  private Account account;
  private String repeatedPassword;
  private String pageDirection;
  private String validation;
  private PaginatedList myList;
  private boolean authenticated;

  /* Static Initializer */

  static {
    List langList = new ArrayList();
    langList.add("english");
    langList.add("japanese");
    LANGUAGE_LIST = Collections.unmodifiableList(langList);

    List catList = new ArrayList();
    catList.add("FISH");
    catList.add("DOGS");
    catList.add("REPTILES");
    catList.add("CATS");
    catList.add("BIRDS");
    CATEGORY_LIST = Collections.unmodifiableList(catList);
  }

  /* Constructors */

  public AccountBean() {
    account = new Account();
  }

  /* JavaBeans Properties */

  public String getUsername() {
    return account.getUsername();
  }

  public void setUsername(String username) {
    account.setUsername(username);
  }

  public String getPassword() {
    return account.getPassword();
  }

  public void setPassword(String password) {
    account.setPassword(password);
  }

  public PaginatedList getMyList() {
    return myList;
  }

  public void setMyList(PaginatedList myList) {
    this.myList = myList;
  }

  public String getRepeatedPassword() {
    return repeatedPassword;
  }

  public void setRepeatedPassword(String repeatedPassword) {
    this.repeatedPassword = repeatedPassword;
  }

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account account) {
    this.account = account;
  }


  public List getLanguages() {
    return LANGUAGE_LIST;
  }

  public List getCategories() {
    return CATEGORY_LIST;
  }

  public String getPageDirection() {
    return pageDirection;
  }

  public void setPageDirection(String pageDirection) {
    this.pageDirection = pageDirection;
  }

  public String getValidation() {
    return validation;
  }

  public void setValidation(String validation) {
    this.validation = validation;
  }

  /* Public Methods */

  public String newAccount() {
    try {
      accountService.insertAccount(account);
      account = accountService.getAccount(account.getUsername());
      myList = catalogService.getProductListByCategory(account.getFavouriteCategoryId());
      authenticated = true;
      repeatedPassword = null;
      return "success";
    } catch (Exception e) {
      throw new BeanActionException ("There was a problem creating your Account Information.  Cause: " + e, e);
    }
  }

  public String editAccountForm() {
    try {
      account = accountService.getAccount(account.getUsername());
      return "success";
    } catch (Exception e) {
      throw new BeanActionException ("There was a problem retrieving your Account Information. Cause: "+e, e);
    }
  }

  public String editAccount() {
    try {
      accountService.updateAccount(account);
      account = accountService.getAccount(account.getUsername());
      myList = catalogService.getProductListByCategory(account.getFavouriteCategoryId());
      return "success";
    } catch (Exception e) {
      throw new BeanActionException ("There was a problem updating your Account Information. Cause: "+e, e);
    }
  }

  public String switchMyListPage () {
    if ("next".equals(pageDirection)) {
      myList.nextPage();
    } else if ("previous".equals(pageDirection)) {
      myList.previousPage();
    }
    return "success";
  }

  public String signon() {

    account = accountService.getAccount(account.getUsername(), account.getPassword());

    if (account == null || account == null) {
      ActionContext.getActionContext().setSimpleMessage("Invalid username or password.  Signon failed.");
      clear();
      return "failure";
    } else {
      account.setPassword(null);

      myList = catalogService.getProductListByCategory(account.getFavouriteCategoryId());

      authenticated = true;

      return "success";
    }
  }

  public String signoff() {
    ActionContext.getActionContext().getRequest().getSession().invalidate();
    clear();
    return "success";
  }

  public boolean isAuthenticated() {
    return authenticated && account != null && account.getUsername() != null;
  }

  public void reset() {
    if (account != null) {
      account.setBannerOption(false);
      account.setListOption(false);
    }
  }

  public void clear() {
    account = new Account();
    repeatedPassword = null;
    pageDirection = null;
    myList = null;
    authenticated = false;
  }

  public void validate() {
    ActionContext ctx = ActionContext.getActionContext();
    if (validation != null) {
      if (VALIDATE_EDIT_ACCOUNT.equals(validation) || VALIDATE_NEW_ACCOUNT.equals(validation)) {
        if (VALIDATE_NEW_ACCOUNT.equals(validation)) {
          account.setStatus("OK");
          validateRequiredField(account.getUsername(), "User ID is required.");
          if (account.getPassword() == null || account.getPassword().length() < 1 || !account.getPassword().equals(repeatedPassword)) {
            ctx.addSimpleError("Passwords did not match or were not provided.  Matching passwords are required.");
          }
        }
        if (account.getPassword() != null && account.getPassword().length() > 0) {
          if (!account.getPassword().equals(repeatedPassword)) {
            ctx.addSimpleError("Passwords did not match.");
          }
        }
        validateRequiredField(account.getFirstName(), "First name is required.");
        validateRequiredField(account.getLastName(), "Last name is required.");
        validateRequiredField(account.getEmail(), "Email address is required.");
        validateRequiredField(account.getPhone(), "Phone number is required.");
        validateRequiredField(account.getAddress1(), "Address (1) is required.");
        validateRequiredField(account.getCity(), "City is required.");
        validateRequiredField(account.getState(), "State is required.");
        validateRequiredField(account.getZip(), "ZIP is required.");
        validateRequiredField(account.getCountry(), "Country is required.");
      }
    }

  }

}
