package com.ibatis.dao.iface;

import testdomain.Account;

/**
 * <p/>
 * Date: Feb 29, 2004 12:29:33 PM
 * 
 * @author Clinton Begin
 */
public interface AccountDao {

  public void createAccount (Account account);

  public void saveAccount (Account account);

  public void removeAccount (Account account);

  public Account findAccount (int id);

}
