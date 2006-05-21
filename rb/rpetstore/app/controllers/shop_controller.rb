class ShopController < ApplicationController
  skip_before_filter :authenticate

  def index
  end
  
  def category
    @category = Category.find_by_name(params[:category])
    @product_pages = Paginator.new self, Product.count_in_category(params[:category]), 5, @params['page']
    @products = Product.find_by_category_with_limit_and_offset(
                      @params['category'],
                      @product_pages.items_per_page,
                      @product_pages.current.offset)
  end
  
  def product
    @product = Product.find(params[:product])
    @item_pages = Paginator.new self, Item.count_in_product(params[:product]), 5, @params['page']
    @items = Item.find_by_product_with_limit_and_offset(
                      @params['product'],
                      @item_pages.items_per_page,
                      @item_pages.current.offset)
  end
  
  def item
    @item = Item.find(params[:item])
  end
end
