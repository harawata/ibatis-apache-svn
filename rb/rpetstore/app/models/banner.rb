class Banner < RBatis::Base
  attr_accessor :bannerdata
  
  resultmap :default,
    :bannerdata => ['bannerdata', String]
  
  def to_s
    bannerdata
  end
  
  statement :select_one, :find_by_favcategory do |favcategory|
    ['SELECT * FROM bannerdata WHERE favcategory = ?', favcategory]
  end
end