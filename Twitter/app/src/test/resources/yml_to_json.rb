require "json"
require "yaml"

raise "no ARGV[0] (yml file)" unless ARGV[0]
raise "no ARGV[1] (json file)" unless ARGV[1]

data = YAML.load File.read(ARGV[0])
File.open(ARGV[1], "w") { |f| f.puts data.to_json }
