// this file is heavily inspired by the tutorial series fully connected on youTube by Finn Eggers

package Network;


import java.util.ArrayList;
import java.util.Arrays;


public class DataSet {
	public final int INPUT_SIZE;
	public final int OUTPUT_SIZE;

	public ArrayList<double[][]> data = new ArrayList<>();


	public DataSet(int INPUT_SIZE, int OUTPUT_SIZE) {
		this.INPUT_SIZE = INPUT_SIZE;
		this.OUTPUT_SIZE = OUTPUT_SIZE;
	}


	public void add_data(double[] in, double[] expected) {
		if (in.length != INPUT_SIZE || expected.length != OUTPUT_SIZE) {
			return;
		}
		data.add(new double[][]{in, expected});
	}


	public String toString() {
		String output = "DataSet [" + INPUT_SIZE + " ; " + OUTPUT_SIZE + "] ";
		output += this.size() + "\n";
		if (size() < 1) {
			output += "\n[empty]";
		} else {
			for (int i = 0; i < Math.min(100, this.size()); i ++) {
				output += i + ":\t" + Arrays.toString(data.get(i)[0]) + "  >-||-<  " + Arrays.toString(data.get(i)[1]) + "\n";
			}
		}
		if (size() > 100) {
			output += "...";
		}
		return output + "\n";
	}


	public DataSet extract_batch(int amount) {
		DataSet set = new DataSet(INPUT_SIZE, OUTPUT_SIZE);
		ArrayList<double[][]> original_data = new ArrayList<>(this.data);
		for (int i = 0; i < amount; i ++) {
			int index = (int)(Math.random() * original_data.size());
			set.data.add(original_data.get(index));
			original_data.remove(index);
		}
		return set;
	}


	public int size() {
		return data.size();
	}


	public double[] get_input(int index) {
		return data.get(index)[0];
	}


	public double[] get_output(int index) {
		return data.get(index)[1];
	}


	public int getINPUT_SIZE() {
		return INPUT_SIZE;
	}


	public int getOUTPUT_SIZE() {
		return OUTPUT_SIZE;
	}
}