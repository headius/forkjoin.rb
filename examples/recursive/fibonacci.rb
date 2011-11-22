require 'forkjoin'

class Fibonacci < ForkJoin::Task
  def initialize(n)
    @n = n
  end

  def call
    return @n if @n <= 1
    (f = Fibonacci.new(@n - 1)).fork
    Fibonacci.new(@n - 2).call + f.join
  end
end

times = ARGV.shift.to_i
n = ARGV.shift.to_i

times.times do
  start = Time.now.to_f
  pool = ForkJoin::Pool.new
  puts "fib(%d) = %d" % [n, pool.invoke(Fibonacci.new(n))]
  puts "%f [msec]" % ((Time.now.to_f - start) * 1000)
end
