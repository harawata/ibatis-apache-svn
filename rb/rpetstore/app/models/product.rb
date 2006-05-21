class Product < RBatis::Base
  attr_reader :product_id
  attr_reader :name
  attr_reader :items
  attr_reader :description

  resultmap :default,
    :product_id => ["productid", String],
    :name => ["name", String],
    :description => ["descn", String],
    :items => RBatis::LazyAssociation.new(:to => Item, :select => :find_by_product, :key => :product_id)
    
  statement :select_one, :find do |productid|
    ["SELECT * FROM product WHERE productid = ?", productid]
  end
  statement :select, :find_by_category_with_limit_and_offset do |category, limit, offset|
    ["SELECT * FROM product WHERE category = ? LIMIT ?, ?", category, offset, limit]
  end
  statement :select, :find_by_category do |category|
    ["SELECT * FROM product WHERE category = ?", category]
  end
  statement :select_value, :count_in_category, :result_type => Fixnum do |category|
    ["SELECT COUNT(*) FROM product WHERE category = ?", category]
  end
end