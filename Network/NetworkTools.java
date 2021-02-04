// this file is heavily inspired by the tutorial series fully connected on youTube by Finn Eggers

package Network;


public class NetworkTools {
	public static double[] create_array(int size, double init_value) {
		if (size < 1) {
			return null;
		}
		double[] array = new double[size];
		for (int i = 0; i < array.length; i ++) {
			array[i] = init_value;
		}
		return array;
	}


	public static double[] create_random_array(int size, double lower_bound, double upper_bound) {
		if (size < 1) {
			return null;
		}
		double[] array = new double[size];
		for (int i = 0; i < array.length; i ++) {
			array[i] = random_value(lower_bound, upper_bound);
		}
		return array;
	}


	public static double[][] create_random_array(int size_x, int size_y, double lower_bound, double upper_bound) {
		if (size_x < 1 || size_y < 1) {
			return null;
		}
		double[][] array = new double[size_x][size_y];
		for (int i = 0; i < array.length; i ++) {
			array[i] = create_random_array(size_y, lower_bound, upper_bound);
		}
		return array;
	}


	public static double random_value(double lower_bound, double upper_bound) {
		return Math.random() * (upper_bound - lower_bound) + lower_bound;
	}


	public static Integer[] random_values(int lower_bound, int upper_bound, int amount) {
		lower_bound --;
		if (amount > (upper_bound - lower_bound)) {
			return null;
		}
		Integer[] values = new Integer[amount];
		for (int i = 0; i < amount; i ++) {
			int n = (int) Math.random() * (upper_bound - lower_bound + 1) + lower_bound;
			while (contains_value(values, n)) {
				n = (int) Math.random() * (upper_bound - lower_bound + 1) + lower_bound;
			}
		}
		return values;
	}


	public static <T extends Comparable<T>> boolean contains_value(T[] array, T value) {
		for (int i = 0; i < array.length; i ++) {
			if (array[i] != null) {
				if (value.compareTo(array[i]) == 0) {
					return true;
				}
			}
		}
		return false;
	}


	public static int index_of_highest_value(double[] values) {
		int index = 0;
		for (int i = 0; i < values.length; i ++) {
			if (values[i] > values[index]) {
				index = i;
			}
		}
		return index;
	}
}