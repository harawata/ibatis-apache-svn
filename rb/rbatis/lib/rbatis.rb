require 'rbatis/sanitizer'

def Fixnum.from_database(record, column)
  record[column].to_i
end

def String.from_database(record, column)
  record[column].to_s
end

def Time.from_database(record, column)
  Time.parse(record[column])
end

module RBatis
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
      params.each{|k,v| send("#{k}=", v)}
      @proc = proc
      reset_statistics
    end
    
    def connection
      connection_provider.connection
    end
    
    def execute(*args)
      @execution_count += 1
      sql = sanitize_sql(proc.call(*args))
      do_execute(sql)
    end
        
    def reset_statistics
      @execution_count = 0
    end
    
    def validate
    end
  end
  
  class SelectValue < Statement
    attr_accessor :result_type
    
    def do_execute(sql)
      raise "result_type must be specified" unless result_type
      record = connection.select_one(sql)
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
      raise 'resultmap has not been specified' unless resultmap
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
    def prefix(prefix)
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
    
    # Creates a new column mapping with the column name prefixed with +prefix+.
    def prefix(prefix)
      self.class.new(prefix + column, type)
    end
  end
  
  class LazyLoadProxy
    def initialize(loader, container)
      @loader = loader
      @container = container
    end
    
    def method_missing(name, *args, &proc)
      maybe_load
      @target.send(name, *args, &proc)
    end
    
    def to_s
      maybe_load
      @target.to_s
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
  
  class EagerAssociation
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

        def statements
          @statements
        end
        
        alias selects statements
        alias inserts statements
        alias updates statements
        
        def resultmaps
          @resultmaps
        end

        def maps(cls)
          @maps = cls
        end
        
        def mapped_class
          @maps || self
        end
        
        def boolean
          BooleanMapper.new
        end
        
        def get_or_allocate(recordmap, record)
          mapped_class.allocate
        end
        
        def resultmap(name = :default, fields = {})
          resultmaps[name] = ResultMap.new(self, fields)
        end

        def extend_resultmap(name, base, fields)
          resultmaps[name] = base.extend(fields)
        end
        
        def statement(statement_type, name = statement_type, params = {}, &proc)
          statement_type = Statement::SHORTCUTS[statement_type] unless statement_type.respond_to?(:new)
          statement = statement_type.new(params, &proc)
          statement.connection_provider = self
          statement.resultmap = resultmaps[:default] if statement.respond_to?(:resultmap=) && statement.resultmap.nil?
          statement.validate
          statements[name] = statement
          eval <<-EVAL
            def #{name}(*args)
              statements[:#{name}].execute(*args)
            end
          EVAL
        end

        def create(*args, &proc)
          mapped_class.new(*args, &proc)
        end
        
        def reset_statistics
          selects.each_value{|s| s.reset_statistics}
        end
      end
    end
  end
end
