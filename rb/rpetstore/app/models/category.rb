class Category < RBatis::Base
  attr_reader :name
  attr_reader :products

  resultmap :default,
    :name => ["name", String],
    :products => RBatis::LazyAssociation.new(:to => Product, :select => :find_by_category, :key => :name)
    
  statement :select_one, :find_by_name do |category|
    ["SELECT * FROM category WHERE name = ?", category]
  end
end