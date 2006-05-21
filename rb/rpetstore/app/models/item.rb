class Item < RBatis::Base
  attr_reader :item_id
  attr_reader :productid
  attr_reader :list_price
  attr_reader :unitcost
  attr_reader :supplier
  attr_reader :status
  attr_reader :attribute1
  attr_reader :attribute2
  attr_reader :attribute3
  attr_reader :attribute4
  attr_reader :attribute5
  attr_reader :product
  attr_reader :inventory
  attr_reader :quantity
  
  resultmap :default,
    :item_id => ['itemid', String],
    :productid => ['productid', String],
    :list_price => ['listprice', Fixnum],
    :unitcost => ['unitcost', Fixnum],
    :supplier => ['supplier', Fixnum],
    :status => ['status', String],
    :attribute1 => ['attr1', String],
    :attribute2 => ['attr2', String],
    :attribute3 => ['attr3', String],
    :attribute4 => ['attr4', String],
    :attribute5 => ['attr5', String],
    :product => RBatis::LazyAssociation.new(:to => Product, :select => :find, :key => :productid),
    :quantity => ['qty', Fixnum]
    
  statement :select, :find_by_product do |productid|
    [%{
      SELECT *, inventory.qty FROM item
      INNER JOIN inventory ON inventory.itemid = item.itemid
      WHERE productid = ?
    }, productid]
  end

  statement :select, :find_by_product_with_limit_and_offset do |productid, limit, offset|
    [%{
      SELECT *, inventory.qty FROM item
      INNER JOIN inventory ON inventory.itemid = item.itemid
      WHERE productid = ?
      LIMIT ?,?
    }, productid, offset, limit]
  end

  statement :select_value, :count_in_product, :result_type => Fixnum do |product|
    ["SELECT COUNT(*) FROM item WHERE productid = ?", product]
  end
  
  statement :select_one, :find do |itemid|
    [%{
      SELECT *, inventory.qty FROM item
      INNER JOIN inventory ON inventory.itemid = item.itemid
      WHERE item.itemid = ?
    }, itemid]
  end
  
  def description
    [attribute1, attribute2, attribute3, attribute4, attribute5, product.name].join(' ')
  end
end
