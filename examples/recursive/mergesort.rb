require 'forkjoin'

class MergeSort < ForkJoin::Task
  def initialize(array, low, high)
    @array, @low, @high = array, low, high
  end

  def call
    size = @high - @low
    if size <= 8
      @array[@low, size] = @array[@low, size].sort
    else
      middle = @low + (size >> 1)
      (f = MergeSort.new(@array, middle, @high)).fork
      MergeSort.new(@array, @low, middle).call
      f.join
      merge(middle)
    end
  end

private

  def merge(middle)
    return if @array[middle - 1] < @array[middle]
    copy_size = @high - @low
    copy_middle = middle - @low
    copy = @array[@low, copy_size]
    p = 0
    q = copy_middle
    @low.upto(@high - 1) do |i|
      if q >= copy_size || (p < copy_middle && copy[p] < copy[q])
        @array[i] = copy[p]
        p += 1
      else
        @array[i] = copy[q]
        q += 1
      end
    end
  end
end

n = ARGV.shift.to_i

array = Array.new(n)
0.upto(n - 1) do |idx|
  array[idx] = rand(2**32) - 2**31
end

org = array.dup

start = Time.now.to_f
pool = ForkJoin::Pool.new
pool.submit(MergeSort.new(array, 0, array.size)).join
puts "%f [msec]" % ((Time.now.to_f - start) * 1000)

if array != org.sort
  # Huh? On my 4 core machine, it *sometimes* fail.
  # Runs fine on my 1 core machine.
  raise "Merge-sort failed!"
end
