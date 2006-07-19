# Integration of RBatis with the Ruby on Rails framework.
# 
# Author::    Jon Tirsen  (mailto:jtirsen@apache.org)
# Copyright:: Copyright (c) 2006 Apache Software Foundation
# License::   Apache Version 2.0 (see http://www.apache.org/licenses/)

module RBatis

  # This is class should be used as a base-class when using RBatis with 
  # the Ruby on Rails framework.
  class Base
    include Repository
    include ::Reloadable::Subclasses
  
    cattr_accessor :logger
    
    # Creates new instance can optionally pass Hash to initialize all attributes.
    def initialize(attributes={})
      self.attributes = attributes
    end
    
    # Updates attributes in passed Hash.
    def attributes=(attributes)
      attributes.each do |key, value|
        send("#{key}=", value)
      end
    end
  
    def self.inherited(inherited_by)
      RBatis::Repository.included(inherited_by)
      class <<inherited_by
        def connection
          ActiveRecord::Base.connection
        end

        def human_attribute_name(field)
          field
        end
      end
    end
    
    # Just calls #save, hook for Ruby on Rails.
    def save!
      save
    end
    
    # If new_record? returns true it calls the +insert+ statement defined 
    # on this class, otherwise it calls the +update+ statement.
    def save
      if new_record?
        id = self.class.insert(self)
        @new_record = false
        id
      else
        self.class.update(self)
      end
    end
    
    # Calls name= with new value.
    def update_attribute(name, value)
      send(name.to_s + '=', value)
      save
    end
    
    # Called by the RBatis framework when loaded, sets new_record? to false so that #save
    # works properly.
    def on_load
      @new_record = false
    end
    
    def new_record?
      return true if not defined?(@new_record)
      @new_record
    end

    include ActiveRecord::Validations
  end
end

RBatis::Base.logger = ActiveRecord::Base.logger