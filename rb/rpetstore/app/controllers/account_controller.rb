class AccountController < ApplicationController
  def index
    @account = @user
    
    if request.post?
      @account.attributes = params[:account]
      @account.save
    end
  end
end
