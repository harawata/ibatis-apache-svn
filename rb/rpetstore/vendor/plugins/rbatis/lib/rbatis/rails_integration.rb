class Dispatcher
  def reset_application!
    Controllers.clear!
    Dependencies.clear
    ActiveRecord::Base.reset_subclasses
    Dependencies.remove_subclasses_for(ActiveRecord::Base, ActiveRecord::Observer, ActionController::Base)
    Dependencies.remove_subclasses_for(RBatis::Base)
    Dependencies.remove_subclasses_for(ActionMailer::Base) if defined?(ActionMailer::Base)
  end
end

module RBatis
  class Base
    include Repository
    include ::Reloadable::Subclasses
  
    cattr_accessor :logger
    
    def initialize(attributes={})
      self.attributes = attributes
    end
    
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
    
    def save!
      save
    end
    
    def save
      if new_record?
        id = self.class.insert(self)
        @new_record = false
        id
      else
        self.class.update(self)
      end
    end
    
    def update_attribute(name, value)
      send(name.to_s + '=', value)
      save
    end
    
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