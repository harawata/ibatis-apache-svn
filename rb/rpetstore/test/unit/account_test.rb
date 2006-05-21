require File.dirname(__FILE__) + '/../test_helper'

class AccountTest < Test::Unit::TestCase
  def setup
    Account.delete_all
  end

  def test_save_with_validations
    account = Account.new
    assert !account.save
    account.errors
    
    account.username = "blah"
    account.password = "password"
    account.password_confirmation = "password"
    account.email = "email@email.com"
    account.first_name = "Jon"
    account.last_name = "Tirsen"
    account.phone = "12345"
    account.address1 = "abcd"
    account.address2 = "efgh"
    account.city = "City"
    account.state = "State"
    account.zip = "12345"
    account.country = "Australia"
    account.language_preference = "english"
    account.favourite_category = "FISH"
    account.list_option = true
    account.banner_option = false
    assert account.save, account.errors.full_messages.join("\n")
    
    account = Account.authenticate('blah', 'password')
    assert !account.nil?
    assert_equal true, account.list_option
    assert_equal false, account.banner_option
    assert_equal '', account.banner.bannerdata
    
    # test update
    assert account.save, account.errors.full_messages.join("\n")
  end
end
