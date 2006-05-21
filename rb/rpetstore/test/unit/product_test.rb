require File.dirname(__FILE__) + '/../test_helper'

class ProductTest < Test::Unit::TestCase
  def test_find
    @product = Product.find('FI-SW-01')
    assert_equal('Angelfish', @product.name)
    assert_equal(2, @product.items.size)
  end
end
