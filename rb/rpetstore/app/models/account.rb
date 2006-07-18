class Account < RBatis::Base
  LANGUAGES = ["english", "japanese"]
  CATEGORIES = ["FISH", "DOGS", "REPTILES", "CATS", "BIRDS"]
  
  attr_accessor :username
  attr_accessor :password
  attr_accessor :password_confirmation
  attr_accessor :first_name
  attr_accessor :last_name
  attr_accessor :email
  attr_accessor :phone
  attr_accessor :address1
  attr_accessor :address2
  attr_accessor :city
  attr_accessor :state
  attr_accessor :zip
  attr_accessor :country
  attr_accessor :favourite_category
  attr_accessor :language_preference
  attr_accessor :list_option
  attr_accessor :banner_option
  attr_reader :banner
  attr_reader :mylist
  
  validates_presence_of :username
  #validates_uniqueness_of :username
  validates_confirmation_of :password
  validates_presence_of :email
  validates_format_of :email, :with => /^([^@\s]+)@((?:[-a-z0-9]+\.)+[a-z]{2,})$/i, :on => :create
  validates_presence_of :first_name
  validates_presence_of :last_name
  validates_presence_of :email
  validates_presence_of :phone
  validates_presence_of :address1
  validates_presence_of :address2
  validates_presence_of :city
  validates_presence_of :state
  validates_presence_of :zip
  validates_presence_of :country
  validates_presence_of :phone
  validates_presence_of :language_preference
  validates_presence_of :favourite_category
  
  def fill_in(order)
    order.user = self
    order.bill_to_first_name = self.first_name
    order.bill_to_last_name = self.last_name
    order.bill_address1 = self.address1
    order.bill_address2 = self.address2
    order.bill_city = self.city
    order.bill_state = self.state
    order.bill_zip = self.zip
    order.bill_country = self.country
  end
  
  def mylist
    return nil unless favourite_category
    favourite_category.products
  end

  resultmap :default,
    :username => ['userid', String],
    :email => ['email', String],
    :first_name => ['firstname', String],
    :last_name => ['lastname', String],
    :address1 => ['addr1', String],
    :address2 => ['addr2', String],
    :city => ['city', String],
    :state => ['state', String],
    :zip => ['zip', String],
    :country => ['country', String],
    :phone => ['phone', String],
    :favourite_category_name => ['favcategory', String],
    :language_preference => ['langpref', String],
    :list_option => ['mylistopt', boolean],
    :banner_option => ['banneropt', boolean],
    :banner => RBatis::LazyAssociation.new(:to => Banner, :select => :find_by_favcategory, :key => :favourite_category_name),
    :favourite_category => RBatis::LazyAssociation.new(:to => Category, :select => :find_by_name, :key => :favourite_category_name)
  
  statement :select_one, :authenticate do |username, password|
    [%{
      SELECT 
        account.userid as userid,
        account.email as email,
        account.firstname as firstname,
        account.lastname as lastname,
        account.addr1 as addr1,
        account.addr2 as addr2,
        account.city as city,
        account.state as state,
        account.zip as zip,
        account.country as country,
        account.phone as phone,
        profile.langpref as langpref,
        profile.favcategory as favcategory,
        profile.mylistopt as mylistopt,
        profile.banneropt as banneropt
      FROM account
      INNER JOIN signon ON signon.username = account.userid
      INNER JOIN profile ON profile.userid = account.userid
      WHERE account.userid = ? AND signon.password = ?
    }, username, password]
  end
  
  def self.update(account)
    update_account(account)
    #update_signon(account)
    update_profile(account)
  end
  
  statement :update, :update_account do |account|
    [%{
      UPDATE account 
      SET
        email = ?, 
        firstname = ?, 
        lastname = ?, 
        addr1 = ?,
        addr2 = ?,
        city = ?, 
        state = ?, 
        zip = ?, 
        country = ?, 
        phone = ?
      WHERE userid = ?
    },  account.email,
        account.first_name,
        account.last_name,
        account.address1,
        account.address2,
        account.city,
        account.state,
        account.zip,
        account.country,
        account.phone,
        account.username]
  end

  statement :update, :update_profile do |account|
    [%{
      UPDATE profile 
      SET langpref = ?, favcategory = ?, mylistopt = ?, banneropt = ?
      WHERE userid = ?
    },  account.language_preference,
        account.favourite_category,
        account.list_option,
        account.banner_option,
        account.username]
  end
  
  statement :update, :update_signon do |account|
    [%{
      UPDATE signon
      SET password = ?
      WHERE username = ?
    },  account.password,
        account.username]
  end
  
  def self.insert(account)
    insert_into_account(account)
    insert_into_signon(account)
    insert_into_profile(account)
  end
  
  statement :insert, :insert_into_account do |account|
    [%{
      INSERT INTO account (
        userid, email, firstname, lastname, addr1, addr2, city, state, zip, country, phone
      ) VALUES (
        ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
      )
    }, account.username,
       account.email,
       account.first_name,
       account.last_name,
       account.address1,
       account.address2,
       account.city,
       account.state,
       account.zip,
       account.country,
       account.phone]
  end
  
  statement :insert, :insert_into_profile do |account|
    [%{
      INSERT INTO profile (
        userid, langpref, favcategory, mylistopt, banneropt
      ) VALUES (
        ?, ?, ?, ?, ?
      )
    }, account.username,
       account.language_preference,
       account.favourite_category,
       account.list_option,
       account.banner_option]
  end
  
  statement :insert, :insert_into_signon do |account|
    [%{
      INSERT INTO signon (
        username, password
      ) VALUES (
        ?, ?
      )
    }, account.username,
       account.password]
  end
  
  def self.delete_all
    delete_all_from_account
    delete_all_from_profile
    delete_all_from_signon
  end
  
  statement :delete, :delete_all_from_account do
    "DELETE FROM account"
  end
  
  statement :delete, :delete_all_from_profile do
    "DELETE FROM profile"
  end
  
  statement :delete, :delete_all_from_signon do
    "DELETE FROM signon"
  end
  
end
