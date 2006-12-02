#
# 
# Author::    Jon Tirsen  (mailto:jtirsen@apache.org)
# Copyright:: Copyright (c) 2006 Apache Software Foundation
# License::   Apache Version 2.0 (see http://www.apache.org/licenses/)

require 'rbatis/sanitizer'

# Converts Fixnum from a database record.
def Fixnum.from_database(record, column)
  record[column].to_i
end

# Converts String from a database record.
def String.from_database(record, column)
  record[column].to_s
end

# Converts Time from a database record.
def Time.from_database(record, column)
  Time.parse(record[column])
end

module RBatis
  VERBOSE = false
  
  class BooleanMapper
    def from_database(record, column)
      return null if record[column].nil?
      return true if record[column] == '1'
      return false if record[column] == '0'
      
      raise "can't parse boolean value for column " + column
    end
  end

  class Statement
    attr_accessor :connection_provider
    attr_reader :proc
    attr_reader :execution_count
    
    include Sanitizer
    
    def initialize(params, &proc)
      params.each do |k,v|
        setter = "#{k}="
        raise "property #{k} is not support by statement #{self.class.name}" unless self.respond_to?(setter)
        self.send(setter, v)
      end
      @proc = proc
      reset_statistics
    end
    
    def connection
      connection_provider.connection
    end
    
    def execute(*args)
      @execution_count += 1
      sql = sanitize_sql(proc.call(*args))
      puts sql if VERBOSE
      result = do_execute(sql)
      p result if VERBOSE
      result
    end
        
    def reset_statistics
      @execution_count = 0
    end
    
    def validate
    end
  end
  
  class SelectValue < Statement
    attr_accessor :result_type
    alias type= result_type=
    
    def do_execute(sql)
      raise "result_type must be specified" unless result_type
      record = connection.select_one(sql)
      return nil unless record
      result_type.from_database(record, record.keys.first)
    end
  end
  
  class Insert < Statement
    def do_execute(sql)
      connection.insert(sql)
    end
  end

  class Delete < Statement
    def do_execute(sql)
      connection.delete(sql)
    end
  end
  
  class Update < Statement
    def do_execute(sql)
      connection.update(sql)
    end
  end

  class Select < Statement
    attr_accessor :resultmap
    
    def do_execute(sql)
      connection.select_all(sql).collect{|record| resultmap.map(record)}.uniq
    end

    def validate
      raise 'resultmap has not been specified, you need at least a :default resultmap declared before the statements' unless resultmap
    end
  end

  class SelectOne < Statement
    attr_accessor :resultmap
    
    def do_execute(sql)
      record = connection.select_one(sql)
      return nil unless record
      resultmap.map(record)
    end
  end
  
  class Statement
    SHORTCUTS = {
      :select => Select,
      :select_one => SelectOne,
      :select_value => SelectValue,
      :insert => Insert,
      :delete => Delete,
      :update => Update,
    }
  end
  
  class ResultMap
    attr_reader :fields
    attr_reader :factory
    
    def initialize(factory, fields)
      @factory = factory
      @fields = {}
      fields.each do |name, field_spec|
        if field_spec.is_a?(Array)
          raise 'column name and type must be specified' unless field_spec.size >= 2
          @fields[name] = Column.new(*field_spec)
        else
          @fields[name] = field_spec
        end
        @fields[name].name = name
      end
    end
    
    def map(record)
      hydrate(factory.get_or_allocate(self, record), record)
    end
    
    def hydrate(result, record)
      fields.each_value{|f| f.map(record, result)}
      result.on_load if result.respond_to?(:on_load)
      result
    end
    
    def value_of(name, record)
      fields[name].value(record)
    end
    
    # Creates a new ResultMap that is identical to the previous one
    # except that all columns are prefixed with the specified +prefix+.
    # Use with EagerAssociation to fetch associated items from an OUTER JOIN fetch
    # to accomplish eager loading and avoiding the N+1 select problem.
    # TODO: Not implemented correctly yet.
    def prefix(prefix) # :nodoc:
      ResultMap.new(factory, fields.collect{|n,f| [n, f.prefix(prefix)]})
    end
    
    # Creates a new ResultMap containing all the same fields except those overriden
    # by +fields+.
    def extend(overriding_fields)
      ResultMap.new(factory, fields.merge(overriding_fields))
    end
    
    def all_nil?(record)
      fields.each_value{|f| return false if !f.value(record).nil?}
      return true
    end
  end
  
  class Column
    attr_accessor :name
    attr_reader :column
    attr_reader :type

    def initialize(column, type)
      @column = column
      @type = type
    end
    
    def map(record, result)
      result.instance_variable_set("@#{name}".to_sym, value(record))
    end
    
    def value(record)
      type.from_database(record, column)
    end
    
    # Creates a new column mapping with the column name prefixed with +prefix+. Useful when
    # doing eager associations.
    # TODO: not implemented correctly yet.
    def prefix(prefix)  # :nodoc:
      self.class.new(prefix + column, type)
    end
  end
  
  class LazyLoadProxy # :nodoc:
    def initialize(loader, container)
      @loader = loader
      @container = container
    end
    
    def target
      maybe_load
      @target
    end
    
    def method_missing(name, *args, &proc)
      self.target.send(name, *args, &proc)
    end
    
    def to_s
      self.target.to_s
    end
    
    private
    
    def maybe_load
      @target = load if !defined?(@target)
    end
    
    def load
      @loader.load(@container)
    end
  end

  class LazyAssociation
    attr_accessor :name

    def initialize(options={}, &loader)
      @options = options
      @options[:keys] = @options[:keys] || [@options[:key]]
      @loader = loader
    end
    
    def map(record, result)
      # association has already been loaded, don't overwrite with proxy
      return if result.instance_variable_get("@#{name}".to_sym)
      
      result.instance_variable_set("@#{name}".to_sym, LazyLoadProxy.new(self, result))
    end
    
    def load(container)
      return @loader.call if @loader
      
      keys = @options[:keys].collect{|key| container.instance_variable_get("@#{key}".to_sym)}
      @options[:to].send(@options[:select], *keys)
    end
  end
  
  # Implements loading of eager outer join associations.
  # TODO: Not implemented correctly yet.
  class EagerAssociation # :nodoc:
    attr_accessor :name
    attr_reader :resultmap
    
    def initialize(resultmap)
      @resultmap = resultmap
    end
    
    def map(record, result)
      ary = result.instance_variable_get("@#{name}".to_sym)
      ary = [] if ary.nil?
      ary << resultmap.map(record) unless resultmap.all_nil?(record)
      result.instance_variable_set("@#{name}".to_sym, ary)
    end
  end

  module Repository
    
    def self.included(included_into)
      included_into.instance_variable_set(:@resultmaps, {})
      included_into.instance_variable_set(:@statements, {})
      class <<included_into
        
        include ClassMethods

      end
    end
    
    module ClassMethods
      def statements
        @statements
      end
      
      alias selects statements # :nodoc:
      alias inserts statements # :nodoc:
      alias updates statements # :nodoc:
      
      # Returns Hash of all #resultmaps defined by #resultmap.
      def resultmaps
        @resultmaps
      end

      # Specify which class this Repository maps to.
      def maps(cls)
        @maps = cls
      end
      
      # Returns the mapped_class (specified with #maps) or +self+ if not specified.
      def mapped_class
        @maps || self
      end
      
      # Returns a BooleanMapper, useful for #resultmaps like this:
      #   resultmap :default,
      #     :username => ['userid', String],
      #     :email_offers => ['offersopt', boolean],
      def boolean
        BooleanMapper.new
      end
      
      def association(type, *args)
        type = case type
          when Class then type
          when :lazy then LazyAssociation
          when :eager then raise 'eager associations not yet implemented'
        end
        type.new(*args)
      end
      
      def get_or_allocate(recordmap, record) # :nodoc:
        mapped_class.allocate
      end
      
      # Defines a named ResultMap which is a map from field name to mapping specification.
      # For example:
      #   resultmap :default,
      #     :username => ['userid', String],
      #     :email => ['email', String],
      #     :first_name => ['firstname', String],
      #     :last_name => ['lastname', String],
      #     :address1 => ['addr1', String],
      #     :address2 => ['addr2', String],
      #     :city => ['city', String],
      #     :state => ['state', String],
      #     :zip => ['zip', String],
      #     :country => ['country', String],
      #     :phone => ['phone', String],
      #     :favourite_category_name => ['favcategory', String],
      #     :language_preference => ['langpref', String],
      #     :list_option => ['mylistopt', boolean],
      #     :banner_option => ['banneropt', boolean],
      #     :banner => RBatis::LazyAssociation.new(:to => Banner, 
      #                                           :select => :find_by_favcategory,
      #                                           :key => :favourite_category_name),
      #     :favourite_category => RBatis::LazyAssociation.new(:to => Category, 
      #                                                       :select => :find_by_name,
      #                                                       :key => :favourite_category_name)
      #
      def resultmap(name = :default, fields = {})
        resultmaps[name] = ResultMap.new(self, fields)
      end

      # Useful for eager loading.
      # TODO not implemented properly yet.
      def extend_resultmap(name, base, fields) # :nodoc:
        resultmaps[name] = base.extend(fields)
      end
      
      # Instantiates the statement and puts it into #statements also generates a class method with the same name that invokes the statement. For example:
      #
      #   class Product < RBatis::Base
      #     statement :select_one, :find do |productid|
  		#   	  ["SELECT * FROM product WHERE productid = ?", productid]
  		#     end
  		#   end
  		#
  		# Can be invoked with:
  		#   Product.select_one(id)
  		#
  		# Note: This also needs a +resultmap+ named +:default+:
      #  
      # +statement_type+ is one of:
      # :select:: Selects and maps multiple object (see Select)
      # :select_one:: Selects and maps one object (see SelectOne)
      # :select_value:: Selects a single value such as an integer or a string (see SelectValue)
      # :insert:: Inserts new records into the database (see Insert)
      # :update:: Updates the database (see Update)
      # :delete:: Deletes records from the database (see Delete)
      #
      def statement(statement_type, name = statement_type, params = {}, &proc)
        statement_type = Statement::SHORTCUTS[statement_type] unless statement_type.respond_to?(:new)
        statement = statement_type.new(params, &proc)
        statement.connection_provider = self
        statement.resultmap = resultmaps[:default] if statement.respond_to?(:resultmap=) && statement.resultmap.nil?
        statement.validate
        statements[name] = statement
        eval <<-EVAL
          def #{name}(*args)
            raise "no such statement: '#{name}'" unless statements[:#{name}]
            statements[:#{name}].execute(*args)
          end
        EVAL
      end

      # Creates a new instance of mapped_class.
      def create(*args, &proc)
        mapped_class.new(*args, &proc)
      end
      
      def reset_statistics # :nodoc:
        selects.each_value{|s| s.reset_statistics}
      end
    end
  end
end
