require 'java'
org.jruby.ext.ForkJoin.new.load(JRuby.runtime, false)

pool = ForkJoin::Pool.new

map_futures = pool.invoke_all(
  ARGF.each_line.map{|line| ->{line.split.map{|word| [word,1]}}}
)
counts = map_futures.map(&:get).inject({}) {|map, value|
  value.each {|k,v| (map[k] ||= []) << v}
  map
}
reduced_futures = pool.invoke_all(
  counts.map{|k, vs| ->{[k, vs.size]}}
)
reduced_futures.map(&:get).each{|value|
  puts "%s %d\n" % value
}
