class CartItem
  attr_accessor :quantity
  
  def initialize(item)
    @item = item
    @quantity = 1
  end
  
  def product
    @item.product
  end
  
  def item_id
    @item.item_id
  end
  
  def description
    @item.description
  end
  
  def in_stock?
    @item.quantity > 0
  end
  
  def list_price
    @item.list_price
  end
  
  def total
    list_price * quantity
  end
  
  def to_line_item
    LineItem.new(@item, list_price, @quantity)
  end
end

class Cart
  attr_reader :items
  
  def initialize
    @items = []
  end
  
  def add_item(item)
    @items << CartItem.new(item)
  end
  
  def empty?
    @items.empty?
  end
  
  def subtotal
    @items.inject(0) {|sum,i|sum+i.total}
  end
  
  def to_order
    order = Order.new
    add_line_items(order)
    order
  end
  
  def add_line_items(order)
    items.each{|item| order.add_item(item.to_line_item) }
  end
end
