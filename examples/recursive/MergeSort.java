import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.*;

/**
 * This program is based on MergeSort.java in OpenJDK 7 example.
 */
public class MergeSort extends RecursiveAction {

    public static void main(String[] args) {
	int n = Integer.parseInt(args[0]);
	Random random = new Random(0);
	int[] array = new int[n];
	for (int i = 0; i < n; ++i) {
	    array[i] = random.nextInt();
	}
	long start = System.nanoTime();
	ForkJoinPool pool = new ForkJoinPool();
	pool.submit(new MergeSort(array, 0, array.length)).join();
	System.out.println(String.format("%f [msec]", (System.nanoTime() - start) / 1000000.0));
    }

    private final int[] array;
    private final int low;
    private final int high;

    MergeSort(int[] array, int low, int high) {
	this.array = array;
	this.low = low;
	this.high = high;
    }

    public void compute() {
	int size = high - low;
	if (size <= 8) {
	    Arrays.sort(array, low, high);
	}
	else {
	    int middle = low + (size >> 1);
	    invokeAll(new MergeSort(array, low, middle), new MergeSort(array, middle, high));
	    merge(middle);
	}
    }

    private void merge(int middle) {
	if (array[middle - 1] < array[middle]) {
	    return;
	}
	int copySize = high - low;
	int copyMiddle = middle - low;
	int[] copy = new int[copySize];
	System.arraycopy(array, low, copy, 0, copy.length);
	int p = 0;
	int q = copyMiddle;
	for (int i = low; i < high; ++i) {
	    if (q >= copySize || (p < copyMiddle && copy[p] < copy[q])) {
		array[i] = copy[p++];
	    }
	    else {
		array[i] = copy[q++];
	    }
	}
    }
}
