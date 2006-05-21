require File.dirname(__FILE__) + '/../test_helper'

class CategoryTest < Test::Unit::TestCase
  def test_find_fish
    @category = Category.find_by_name('FISH')
    assert_equal('Fish', @category.name)
    assert_equal(4, @category.products.size)
  end
end
