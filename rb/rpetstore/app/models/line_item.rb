class LineItem < RBatis::Base
  attr_accessor :order
  attr_accessor :line_num
  attr_accessor :item
  attr_accessor :quantity
  attr_accessor :unit_price
  
  def initialize(item, unit_price, quantity)
    @item = item
    @unit_price = unit_price
    @quantity = quantity
  end
  
  def total_price
    @unit_price * @quantity
  end
  
  resultmap :default,
    :order => RBatis::LazyAssociation.new(:to => Order, :select => :find_by_order_id, :key => :orderid),
    :orderid => ['orderid', String],
    :line_num => ['linenum', Fixnum],
    :item => RBatis::LazyAssociation.new(:to => Item, :select => :find, :key => :itemid),
    :itemid => ['itemid', String],
    :quantity => ['quantity', Fixnum],
    :unit_price => ['unitprice', Fixnum]
  
  statement :insert do |line_item|
    [%{
      INSERT INTO lineitem
      (orderid, linenum, itemid, quantity, unitprice)
      VALUES
      (?, ?, ?, ?, ?)
    }, line_item.order.order_id, line_item.line_num, line_item.item.item_id, line_item.quantity, line_item.unit_price]
  end
  
  statement :select, :find_by_order_id do |order_id|
    ["SELECT * FROM lineitem WHERE orderid = ?", order_id]
  end
end