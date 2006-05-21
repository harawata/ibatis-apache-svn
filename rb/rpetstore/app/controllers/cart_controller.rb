class CartController < ApplicationController
  skip_before_filter :authenticate
  before_filter :create_or_get_cart
  
  def add
    @item = Item.find(params[:item])
    @cart.add_item(@item)
    redirect_to :action => :index
  end
  
  def index
    @item_pages = Paginator.new self, @cart.items.size, 5, @params['page']
    @items =
      @cart.items[@item_pages.current.offset..@item_pages.current.offset + @item_pages.items_per_page]
    @items = @cart.items
  end
  
  def checkout
    @item_pages = Paginator.new self, @cart.items.size, 5, @params['page']
    @items =
      @cart.items[@item_pages.current.offset..@item_pages.current.offset + @item_pages.items_per_page]
    @items = @cart.items
  end
  
  private
  
  def create_or_get_cart
    @cart = session[:cart]
    unless @cart
      @cart = Cart.new
      session[:cart] = @cart
    end
    @cart
  end
end
