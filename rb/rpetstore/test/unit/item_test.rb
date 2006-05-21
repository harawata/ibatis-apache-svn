require File.dirname(__FILE__) + '/../test_helper'

class ItemTest < Test::Unit::TestCase
  def test_find_by_product 
    Item.find_by_product('FI-SW-01')
  end
  
  def test_find
    Item.find('EST-1')
  end
end
