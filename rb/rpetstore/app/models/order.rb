class Order < RBatis::Base
  CARD_TYPES = ['Visa', 'MasterCard', 'American Express']
  
  validates_presence_of :card_type
  validates_presence_of :credit_card
  validates_presence_of :expiry_date
  validates_format_of :expiry_date, :with => /^\d{2}\/\d{4}$/
  validates_presence_of :bill_to_first_name
  validates_presence_of :bill_to_last_name
  validates_presence_of :bill_address1
  validates_presence_of :bill_address2
  validates_presence_of :bill_city
  validates_presence_of :bill_state
  validates_presence_of :bill_zip
  validates_presence_of :bill_country
  validates_presence_of :ship_address1
  validates_presence_of :ship_address2
  validates_presence_of :ship_city
  validates_presence_of :ship_state
  validates_presence_of :ship_zip
  validates_presence_of :ship_country
  
  attr_reader :order_id
  attr_accessor :user
  attr_reader :order_date
  attr_accessor :card_type
  attr_accessor :credit_card
  attr_accessor :expiry_date
  attr_accessor :bill_to_first_name
  attr_accessor :bill_to_last_name
  attr_accessor :bill_address1
  attr_accessor :bill_address2
  attr_accessor :bill_city
  attr_accessor :bill_state
  attr_accessor :bill_zip
  attr_accessor :bill_country
  attr_accessor :ship_to_first_name
  attr_accessor :ship_to_last_name
  attr_accessor :ship_address1
  attr_accessor :ship_address2
  attr_accessor :ship_city
  attr_accessor :ship_state
  attr_accessor :ship_zip
  attr_accessor :ship_country
  attr_reader :line_items
  attr_reader :total_price
  attr_reader :locale
  attr_reader :courier
  attr_reader :status
  
  def initialize(attributes={})
    super(attributes)
    @line_items = []
    @total_price = 0
    @order_date = Time.now
    @credit_card = "999 9999 9999 9999";
    @expiry_date = "12/03";
    @card_type = "Visa";
    @courier = "UPS";
    @locale = "CA";
    @status = "P";
  end
  
  def use_billing_address_as_shipping_address
    self.ship_address1 = bill_address1
    self.ship_address2 = bill_address2
    self.ship_city = bill_city
    self.ship_state = bill_state
    self.ship_zip = bill_zip
    self.ship_country = bill_country
    self.ship_to_first_name = bill_to_first_name
    self.ship_to_last_name = bill_to_last_name
  end
  
  def add_item(item)
    @line_items<<item
    item.order = self
    item.line_num = @line_items.size
    @total_price += item.total_price
  end
  
  resultmap :default,
    :order_id => ['orderid', Fixnum],
    :user => RBatis::LazyAssociation.new(:to => Account, :select => :find_by_username, :key => :username),
    :username => ['userid', String],
    :order_date => ['orderdate', Time],
    :ship_address1 => ['shipaddr1', String],
    :ship_address2 => ['shipaddr2', String],
    :ship_city => ['shipcity', String], 
    :ship_state => ['shipstate', String], 
    :ship_zip => ['shipzip', String], 
    :ship_country => ['shipcountry', String], 
    :courier => ['courier', String], 
    :bill_address1 => ['billaddr1', String], 
    :bill_address2 => ['billaddr2', String], 
    :bill_city => ['billcity', String], 
    :bill_state => ['billstate', String], 
    :bill_zip => ['billzip', String], 
    :bill_country => ['billcountry', String], 
    :total_price => ['totalprice', Fixnum], 
    :bill_to_first_name => ['billtofirstname', String], 
    :bill_to_last_name => ['billtolastname', String], 
    :ship_to_first_name => ['shiptofirstname', String], 
    :ship_to_last_name => ['shiptolastname', String], 
    :credit_card => ['creditcard', String], 
    :expiry_date => ['exprdate', String], 
    :card_type => ['cardtype', String], 
    :locale => ['locale', String],
    :line_items => RBatis::LazyAssociation.new(:to => LineItem, :select => :find_by_order_id, :key => :order_id)
  
  statement :select_one, :find do |id|
    ["SELECT * FROM orders WHERE orderid = ?", id]
  end
  
  def self.insert(order)
    order.instance_variable_set(:@order_id, find_next_order_id)
    insert_into_orders(order)
    (order.line_items || []).each{|li| li.save!}
  end
  
  statement :select_value, :find_next_order_id, :result_type => Fixnum do
    "SELECT MAX(orderid) + 1 FROM orders"
  end

  statement :insert, :insert_into_orders do |order|
    [%{
      INSERT INTO orders 
        (orderid, userid, orderdate, shipaddr1, shipaddr2, shipcity, shipstate, shipzip, shipcountry, billaddr1, billaddr2, billcity, billstate, billzip, billcountry, courier, totalprice, billtofirstname, billtolastname, shiptofirstname, shiptolastname, creditcard, exprdate, cardtype, locale)
        VALUES
        (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    }, order.instance_variable_get(:@order_id), order.user.username, order.order_date, order.ship_address1, order.ship_address2, order.ship_city, order.ship_state, order.ship_zip, order.ship_country, order.bill_address1, order.bill_address2, order.bill_city, order.bill_state, order.bill_zip, order.bill_country, order.courier, order.total_price, order.bill_to_first_name, order.bill_to_last_name, order.ship_to_first_name, order.ship_to_last_name, order.credit_card, order.expiry_date, order.card_type, order.locale]
  end
  
  statement :insert, :insert_into_orderstatus do |order|
    [%{
      INSERT INTO orders 
        (orderid, userid, orderdate, shipaddr1, shipaddr2, shipcity, shipstate, shipzip, shipcountry, billaddr1, billaddr2, billcity, billstate, billzip, billcountry, courier, totalprice, billtofirstname, billtolastname, shiptofirstname, shiptolastname, creditcard, exprdate, cardtype, locale)
        VALUES
        (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    }, order.instance_variable_get(:@order_id), order.user.username, order.order_date, order.ship_address1, order.ship_address2, order.ship_city, order.ship_state, order.ship_zip, order.ship_country, order.courier, order.bill_address1, order.bill_address2, order.bill_city, order.bill_state, order.bill_zip, order.bill_country, order.total_price, order.bill_to_first_name, order.bill_to_last_name, order.ship_to_first_name, order.ship_to_last_name, order.credit_card, order.expiry_date, order.card_type, order.locale]
  end
  
  def self.delete_all
    delete_all_from_orders
    delete_all_from_lineitem
  end
  
  statement :delete, :delete_all_from_orders do
    "DELETE FROM orders"
  end
  
  statement :delete, :delete_all_from_lineitem do
    "DELETE FROM lineitem"
  end
end