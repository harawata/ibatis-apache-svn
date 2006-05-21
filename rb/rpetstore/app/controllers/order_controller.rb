module ActiveRecord
  class Errors
    def clear_for(attr)
      @errors.delete(attr.to_s)
    end
  end
end

class OrderController < ApplicationController
  def index
    if request.get?
      @order = session[:cart].to_order
      session[:user].fill_in(@order)
      session[:order_in_progress] = @order
    else
      @order = session[:order_in_progress]
      @order.attributes = params[:order]
      @order.valid?
      # don't validate shipping address at this stage
      @order.errors.clear_for(:ship_address1)
      @order.errors.clear_for(:ship_address2)
      @order.errors.clear_for(:ship_city)
      @order.errors.clear_for(:ship_state)
      @order.errors.clear_for(:ship_zip)
      @order.errors.clear_for(:ship_country)
      if @order.errors.empty?
        if params[:shipping_address_required]
          redirect_to :action => :shipping_address
        else
          @order.use_billing_address_as_shipping_address
          redirect_to :action => :confirmation
        end
      end
    end
  end
  
  def shipping_address
    @order = session[:order_in_progress]
    if request.post?
      @order.attributes = params[:order]
      @order.valid?
      if @order.errors.empty?
        redirect_to :action => :confirmation
      end
    else
      # start off with the billing address and let the user modify it
      @order.use_billing_address_as_shipping_address
    end
  end
  
  def confirmation
    @order = session[:order_in_progress]
  end
  
  def confirm
    @order = session[:order_in_progress]
    @order.save!
    
    flash[:message] = "Thank you, your order has been submitted."
    redirect_to :action => :view, :id => @order.order_id
  end
  
  def view
    @order = Order.find(params[:id])
  end
end
