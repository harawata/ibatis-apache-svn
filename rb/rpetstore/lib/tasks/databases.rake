desc "Create and load MySQL databases"
task :setup_railspetstore_databases_mysql => :environment do
  ['railspetstore_development', 'railspetstore_test'].each do |database|
    exec "cd vendor/JPetStore-5.0/src/ddl/mysql && /usr/local/mysql/bin/mysql -u root -e \"drop database #{database}; create database #{database}; use #{database}; source jpetstore-mysql-schema.sql; source jpetstore-mysql-dataload.sql;\""
  end
end

task :clone_structure_to_test do
end