require File.expand_path(File.dirname(__FILE__) + '/../../../rails/activerecord/lib/active_record')
require 'test/unit'
require 'test/unit/ui/console/testrunner'
$: << File.dirname(__FILE__) + '/../lib/'
require 'rbatis'
require 'rbatis/rails_integration'

RAILS_ENV = "development"
ActiveRecord::Base.configurations = {
  RAILS_ENV => {
    'adapter' => 'sqlite3',
    'database' => ":memory:"
  }
}
ActiveRecord::Base.establish_connection

ActiveRecord::Base.connection.create_table :cars do |t|
  t.column :make, :string
  t.column :owner_id, :integer
end
ActiveRecord::Base.connection.create_table :people do |t|
  t.column :name, :string
end
ActiveRecord::Base.connection.insert("INSERT INTO people (id, name) VALUES (1, 'Jon Tirsen')")
ActiveRecord::Base.connection.insert("INSERT INTO people (id, name) VALUES (2, 'Asa Holmstrom')")
ActiveRecord::Base.connection.insert("INSERT INTO people (id, name) VALUES (3, 'Ben Hogan')")
ActiveRecord::Base.connection.insert("INSERT INTO cars (id, make, owner_id) VALUES (1, 'Honda', 1)")
ActiveRecord::Base.connection.insert("INSERT INTO cars (id, make, owner_id) VALUES (2, 'Audi', 1)")
ActiveRecord::Base.connection.insert("INSERT INTO cars (id, make, owner_id) VALUES (3, 'Hyundai', 3)")


class Car < RBatis::Base
  attr_reader :make

  def to_s
    @make
  end
  
  resultmap :default, 
    :id => ["id", Fixnum],
    :make => ["make", String]
  statement :select, :find_by_owner_id do |person_id|
    ["SELECT * FROM cars WHERE owner_id = ?", person_id]
  end
end

class Person < RBatis::Base
  attr_reader :person_id
  attr_accessor :name
  attr_accessor :cars
  
  def initialize(name)
    @name = name
  end
  
  def to_s
    "#{@name}(#{@cars.join(',')})"
  end
  
  resultmap :default,
    :person_id => ["id", Fixnum],
    :name => ["name", String],
    :cars => RBatis::LazyAssociation.new(:to => Car, :select => :find_by_owner_id, :key => :person_id)
  
  extend_resultmap :fetch_cars, resultmaps[:default],
    :cars => RBatis::EagerAssociation.new(Car.resultmaps[:default].prefix("car_"))
  
  statement :select, :find_all do 
    "SELECT * FROM people ORDER BY id"
  end

  statement :select_one, :find_by_id do |id|
    ["SELECT * FROM people WHERE id = ?", id]
  end

  statement :select, :find_all_fetch_cars, :resultmap => Car.resultmaps[:fetch_cars] do %{
    SELECT p.*, c.id car_id, c.make car_make
    FROM people p
    LEFT OUTER JOIN cars c ON p.id = c.owner_id
  } end
  
  statement :insert do |person|
    ["INSERT INTO people (name) VALUES (?)", person.name]
  end

  statement :update do |person|
    ["UPDATE people SET name = ? WHERE id = ?", person.name, person.person_id]
  end
  
  statement :delete do |person|
    ["DELETE FROM people WHERE id = ?", person.person_id]
  end

  statement :delete, :delete_temporary_data do
    ["DELETE FROM people WHERE id > ?", 3]
  end
end

class RBatisTest < Test::Unit::TestCase
  def setup
    Person.reset_statistics
    Car.reset_statistics
  end

  def test_lazy_load
    assert_correct_people_and_cars(Person.find_all)
    assert_equal(1, Person.selects[:find_all].execution_count)
    assert_equal(3, Car.selects[:find_by_owner_id].execution_count)
  end

  def doesnt_work_yet_test_eager_load
    assert_correct_people_and_cars(Person.find_all_fetch_cars)    
  
    assert_equal(1, Person.selects[:find_all_fetch_cars].execution_count)
    assert_equal(0, Car.selects[:find_by_owner_id].execution_count)
  end

  def doesnt_work_yet_test_load_twice_gives_same_object
    RBatis::Base.transaction do
      assert_all_same(PersonRepository.find_all, PersonRepository.find_all)
    end
  end
  
  def test_create_update_and_delete
    person = Person.new("Jon Tirsen")
    id = person.save
    assert(!person.new_record?)
    assert(id > 0)

    person = Person.find_by_id(id)
    assert_equal("Jon Tirsen", person.name)
    person.name = "Julian Boot"
    person.save

    person = Person.find_by_id(id)
    assert_equal("Julian Boot", person.name)
    Person.delete(person)

    assert_nil(Person.find_by_id(id))
  end

  def assert_all_same(ary1, ary2)
    ary1.each_with_index do |el1, index|
      assert_same(el1, ary2[index])
    end
  end
  
  def assert_correct_people_and_cars(all)
    assert_equal('Jon Tirsen', all[0].name)
    assert_equal(2, all[0].cars.size)
    assert_equal('Honda', all[0].cars[0].make)
    assert_equal('Audi', all[0].cars[1].make)
    assert_equal('Asa Holmstrom', all[1].name)
    assert_equal(0, all[1].cars.size)
    assert_equal('Ben Hogan', all[2].name)
    assert_equal(1, all[2].cars.size)
    assert_equal('Hyundai', all[2].cars[0].make)
    p all.collect{|p| p.name}
    assert_equal(3, all.size)
  end
end
