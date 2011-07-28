// 0.12 or later required.
// You need to remove $GROOVY_HOME/lib/gpars-0.11.jar to run this script.
@Grab('org.codehaus.gpars:gpars:0.12')
import static groovyx.gpars.GParsPool.*

def fibonacci(num) {
  withPool { pool ->
    runForkJoin(num) { n ->
      if (n <= 1) return n
      forkOffChild(n - 1)
      return runChildDirectly(n - 2) + childrenResults.sum()
    }
  }
}

n = Integer.parseInt(args[0])

start = System.nanoTime()
println String.format("fib(%d) = %d", n, fibonacci(n))
println String.format("%f [msec]", (System.nanoTime() - start) / 1000000.0)
