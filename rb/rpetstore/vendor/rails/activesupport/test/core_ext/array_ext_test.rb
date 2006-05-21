require 'test/unit'
require File.dirname(__FILE__) + '/../../lib/active_support'

class ArrayExtToParamTests < Test::Unit::TestCase
  def test_string_array
    assert_equal '', %w().to_param
    assert_equal 'hello/world', %w(hello world).to_param
    assert_equal 'hello/10', %w(hello 10).to_param
  end
  
  def test_number_array
    assert_equal '10/20', [10, 20].to_param
  end
end

class ArrayExtConversionTests < Test::Unit::TestCase
  def test_plain_array_to_sentence
    assert_equal "", [].to_sentence
    assert_equal "one", ['one'].to_sentence
    assert_equal "one and two", ['one', 'two'].to_sentence
    assert_equal "one, two, and three", ['one', 'two', 'three'].to_sentence
    
  end
  
  def test_to_sentence_with_connector
    assert_equal "one, two, and also three", ['one', 'two', 'three'].to_sentence(:connector => 'and also')
  end
  
  def test_to_sentence_with_skip_last_comma
    assert_equal "one, two, and three", ['one', 'two', 'three'].to_sentence(:skip_last_comma => false)
  end

  def test_two_elements
    assert_equal "one and two", ['one', 'two'].to_sentence
  end
  
  def test_one_element
    assert_equal "one", ['one'].to_sentence
  end
end

class ArrayExtGroupingTests < Test::Unit::TestCase
  def test_group_by_with_perfect_fit
    groups = []
    ('a'..'i').to_a.in_groups_of(3) do |group|
      groups << group
    end

    assert_equal [%w(a b c), %w(d e f), %w(g h i)], groups
    assert_equal [%w(a b c), %w(d e f), %w(g h i)], ('a'..'i').to_a.in_groups_of(3)
  end

  def test_group_by_with_padding
    groups = []
    ('a'..'g').to_a.in_groups_of(3) do |group|
      groups << group
    end

    assert_equal [%w(a b c), %w(d e f), ['g', nil, nil]], groups
  end

  def test_group_by_pads_with_specified_values
    groups = []

    ('a'..'g').to_a.in_groups_of(3, false) do |group|
      groups << group
    end

    assert_equal [%w(a b c), %w(d e f), ['g', false, false]], groups
  end
end

class ArraySplitTests < Test::Unit::TestCase
  def test_split_with_empty_array
    assert_equal [[]], [].split(0)
  end
  
  def test_split_with_argument
    assert_equal [[1, 2], [4, 5]],  [1, 2, 3, 4, 5].split(3)
    assert_equal [[1, 2, 3, 4, 5]], [1, 2, 3, 4, 5].split(0)
  end
  
  def test_split_with_block
    assert_equal [[1, 2], [4, 5], [7, 8], [10]], (1..10).to_a.split { |i| i % 3 == 0 }
  end
  
  def test_split_with_edge_values
    assert_equal [[], [2, 3, 4, 5]],  [1, 2, 3, 4, 5].split(1)
    assert_equal [[1, 2, 3, 4], []],  [1, 2, 3, 4, 5].split(5)
    assert_equal [[], [2, 3, 4], []], [1, 2, 3, 4, 5].split { |i| i == 1 || i == 5 }
  end
end

class ArrayToXmlTests < Test::Unit::TestCase
  def test_to_xml
    xml = [
      { :name => "David", :age => 26 }, { :name => "Jason", :age => 31 }
    ].to_xml(:skip_instruct => true, :indent => 0)

    assert_equal "<records><record>", xml.first(17)
    assert xml.include?(%(<age type="integer">26</age>))
    assert xml.include?(%(<name>David</name>))
    assert xml.include?(%(<age type="integer">31</age>))
    assert xml.include?(%(<name>Jason</name>))
  end

  def test_to_xml_with_dedicated_name
    xml = [
      { :name => "David", :age => 26 }, { :name => "Jason", :age => 31 }
    ].to_xml(:skip_instruct => true, :indent => 0, :root => "people")

    assert_equal "<people><person>", xml.first(16)
  end
  
  def test_to_xml_with_options
    xml = [ 
      { :name => "David", :street_address => "Paulina" }, { :name => "Jason", :street_address => "Evergreen" }
    ].to_xml(:skip_instruct => true, :skip_types => true, :indent => 0)

    assert_equal "<records><record>", xml.first(17)
    assert xml.include?(%(<street-address>Paulina</street-address>))
    assert xml.include?(%(<name>David</name>))
    assert xml.include?(%(<street-address>Evergreen</street-address>))
    assert xml.include?(%(<name>Jason</name>))
  end
end
