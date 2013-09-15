forkjoin for JRuby
==================

This is a small extension that wraps the JSR166y "Fork/Join"
framework in an efficient way for JRuby.

Example
-------

```ruby
require 'forkjoin'

pool = ForkJoin::Pool.new

# FORK

# Add a job (a proc) to the pool for each line
map_futures = pool.invoke_all(
  ARGF.each_line.map{|line| ->{line.split.map{|word| [word,1]}}}
)

# Get aggregate results
counts = map_futures.map(&:get).inject({}) {|map, value|
  value.each {|k,v| (map[k] ||= []) << v}
  map
}

# JOIN

# Add a job to the pool for each count in the map
reduced_futures = pool.invoke_all(
  counts.map{|k, vs| ->{[k, vs.size]}}
)

# Print out results (or you could "reduce" some other way)
reduced_futures.map(&:get).each{|value|
  puts "%s %d\n" % value
}
```
