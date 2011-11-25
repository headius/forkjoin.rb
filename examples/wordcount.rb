require 'forkjoin'
require 'benchmark'

file = ARGV.shift

5.times do
  Benchmark.bm do |bm|
    bm.report do
      pool = ForkJoin::Pool.new
      map_futures = nil
      File.open(file) do |f|
        map_futures = pool.invoke_all(
          f.each_line.map { |line| -> { line.split.map { |word| [word,1] } } }
        )
      end
      counts = map_futures.map(&:get).inject({}) { |map, value|
        value.each { |k, v| (map[k] ||= []) << v }
        map
      }
      reduced_futures = pool.invoke_all(
        counts.map { |k, vs| -> { [k, vs.size] } }
      )
      # reduced_futures.map(&:get).each{|value|
      #   puts "%s %d\n" % value
      # }
    end
  end
end
