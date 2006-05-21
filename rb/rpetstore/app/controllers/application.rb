# Filters added to this controller will be run for all controllers in the application.
# Likewise, all the methods added will be available for all controllers.
class ApplicationController < ActionController::Base
  before_filter :load_user
  before_filter :authenticate
  
  def load_user
    @user = session[:user]
  end
  
  def authenticate
    unless @user
      redirect_to :controller => 'login'
      false
    end
  end
end