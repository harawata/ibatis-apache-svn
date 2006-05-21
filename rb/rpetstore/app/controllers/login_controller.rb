class LoginController < ApplicationController
  skip_before_filter :authenticate
  
  def index
    if request.get?
      session[:return_to_url] = request.env['HTTP_REFERER']
    else
      @user = Account.authenticate(params[:username], params[:password])
      if @user
        session[:user] = @user
        if session[:return_to_url]
          redirect_to session[:return_to_url]
        else
          redirect_to :controller => 'shop'
        end
        session[:return_to_url] = nil
      else
        flash[:error] = "Wrong username or password"
      end
    end
  end
  
  def logout
    reset_session
    redirect_to :controller => 'shop'
  end
  
  def register
    if request.get?
      @account = Account.new
    else
      @account = Account.new(params[:account])
      if @account.save
        redirect_to :action => :index
      end
    end
  end
end
