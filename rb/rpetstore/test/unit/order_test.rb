require File.dirname(__FILE__) + '/../test_helper'

class OrderTest < Test::Unit::TestCase
  def setup
    Order.delete_all
  end
  
  def create_account
    account = Account.new
    account.username = "blah"
    account.password = "password"
    account.password_confirmation = "password"
    account.email = "email@email.com"
    account.first_name = "Jon"
    account.last_name = "Tirsen"
    account.phone = "12345"
    account.address1 = "Address 1"
    account.address2 = "Address 2"
    account.city = "City"
    account.state = "State"
    account.zip = "12345"
    account.country = "Australia"
    account.language_preference = "english"
    account
  end
  
  def create_item(itemid)
    item = Item.new
    item.instance_variable_set(:@item_id, itemid)
    item.instance_variable_set(:@list_price, itemid * 100)
    item.instance_variable_set(:@quantity, 1)
    item
  end
  
  def create_cart
    cart = Cart.new
    cart.add_item(create_item(1))
    cart.add_item(create_item(2))
    cart.add_item(create_item(3))
    cart
  end

  def test_create_and_save_order
    account = create_account
    cart = create_cart
    order = cart.to_order
    account.fill_in(order)
    order.use_billing_address_as_shipping_address
    
    order.card_type = "Visa"
    order.credit_card = "1234"
    order.expiry_date = "12/2006"
    
    assert order.save, order.errors.full_messages.join("\n")
    
    order = Order.find(order.order_id)
    assert_equal 3, order.line_items.size
    assert_equal 'Address 1', order.bill_address1
    assert_equal 'Address 1', order.ship_address1
    assert_equal 'Address 2', order.bill_address2
    assert_equal 'Address 2', order.ship_address2
    assert_equal 'Australia', order.bill_country
    assert_equal 'Australia', order.ship_country
  end
end
