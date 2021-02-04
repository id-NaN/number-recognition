// this file is heavily inspired by the tutorial series fully connected on youTube by Finn Eggers

package Network;


import java.util.Arrays;
import java.io.Serializable;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ClassNotFoundException;


public class Network implements Serializable {
	private double[][]   output;
	private double[][][] weights;
	private double[][]   bias;

	private double[][]   error_signal;
	private double[][]   output_derivative;

	public final int[] NETWORK_LAYER_SIZES;
	public final int   INPUT_SIZE;
	public final int   OUTPUT_SIZE;
	public final int   NETWORK_SIZE;


	public Network(int... NETWORK_LAYER_SIZES) {
		this.NETWORK_LAYER_SIZES = NETWORK_LAYER_SIZES;
		this.NETWORK_SIZE = NETWORK_LAYER_SIZES.length;
		this.INPUT_SIZE = NETWORK_LAYER_SIZES[0];
		this.OUTPUT_SIZE = NETWORK_LAYER_SIZES[NETWORK_SIZE - 1];

		this.output =  new double[NETWORK_SIZE][];
		this.weights = new double[NETWORK_SIZE][][];
		this.bias =    new double[NETWORK_SIZE][];

		this.error_signal      =  new double[NETWORK_SIZE][];
		this.output_derivative =  new double[NETWORK_SIZE][];

		for (int i = 0; i < NETWORK_SIZE; i ++) {
			this.output [i] = new double[NETWORK_LAYER_SIZES[i]];
			this.bias   [i] = NetworkTools.create_random_array(NETWORK_LAYER_SIZES[i], 0.3, 0.7);

			this.error_signal      [i] = new double[NETWORK_LAYER_SIZES[i]];
			this.output_derivative [i] = new double[NETWORK_LAYER_SIZES[i]];

			if (i > 0) {
				this.weights[i] = NetworkTools.create_random_array(NETWORK_LAYER_SIZES[i], NETWORK_LAYER_SIZES[i - 1], -0.3, 0.5);
			}
		}
	}


	public double[] calculate(double... input) {
		if (input.length != this.INPUT_SIZE) {
			return null;
		}
		this.output[0] = input;
		for (int layer = 1; layer < NETWORK_SIZE; layer ++) {
			for (int neuron = 0; neuron < NETWORK_LAYER_SIZES[layer]; neuron ++) {
				double sum = bias[layer][neuron];
				for (int prev_neuron = 0; prev_neuron < NETWORK_LAYER_SIZES[layer - 1]; prev_neuron ++) {
					sum += output[layer - 1][prev_neuron] * weights[layer][neuron][prev_neuron];
				}
				output[layer][neuron] = sigmoid(sum);
				output_derivative[layer][neuron] = (output[layer][neuron] * (1 - output[layer][neuron]));
			}
		}
		return output[NETWORK_SIZE - 1];
	}


	public double MSE(double[] input, double[] target) {
		if (input.length != INPUT_SIZE || target.length != OUTPUT_SIZE) {
			return 0;
		}
		calculate(input);
		double v = 0;
		for (int i = 0; i < target.length; i ++) {
			v += Math.pow(target[i] - output[NETWORK_SIZE - 1][i], 2);
		}
		return v / (2d * target.length);
	}


	public double MSE(DataSet set) {
		double v = 0;
		for (int i = 0; i < set.size(); i ++) {
			v += MSE(set.get_input(i), set.get_output(i));
		}
		return v / set.size();
	}


	public void train(DataSet set, int loops, int batch_size, double eta) {
		if (set.INPUT_SIZE != INPUT_SIZE || set.OUTPUT_SIZE != OUTPUT_SIZE) {
			return;
		}
		for (int i = 0; i < loops; i ++) {
			DataSet batch = set.extract_batch(batch_size);
			for (int b = 0; b < batch_size; b ++) {
				this.train(batch.get_input(b), batch.get_output(b), eta);
			}
		}
		System.out.println(MSE(set.extract_batch(batch_size * 10)));
	}


	public void train(double[] input, double[] target, double eta) {
		if (input.length != INPUT_SIZE || target.length != OUTPUT_SIZE) {
		}
		calculate(input);
		backprop_error(target);
		update_weights(eta);
	}


	public void backprop_error(double[] target) {
		for (int neuron = 0; neuron < NETWORK_LAYER_SIZES[NETWORK_SIZE - 1]; neuron ++) {
			error_signal[NETWORK_SIZE - 1][neuron] = (output[NETWORK_SIZE - 1][neuron] - target[neuron]) *
				output_derivative[NETWORK_SIZE - 1][neuron];
		}
		for (int layer = NETWORK_SIZE - 2; layer > 0; layer --) {
			for (int neuron = 0; neuron < NETWORK_LAYER_SIZES[layer]; neuron ++) {
				double sum = 0;
				for (int next_neuron = 0; next_neuron < NETWORK_LAYER_SIZES[layer + 1]; next_neuron ++) {
					sum += weights[layer + 1][next_neuron][neuron] * error_signal[layer + 1][next_neuron];
				}
				this.error_signal[layer][neuron] = sum * output_derivative[layer][neuron];
			}
		}
	}


	public void update_weights(double eta) {
		for (int layer = 1; layer < NETWORK_SIZE; layer ++) {
			for (int neuron = 0; neuron < NETWORK_LAYER_SIZES[layer]; neuron ++) {

				double delta = - eta * error_signal[layer][neuron];
				bias[layer][neuron] += delta;

				for (int previous_neuron = 0; previous_neuron < NETWORK_LAYER_SIZES[layer - 1]; previous_neuron ++) {
					weights[layer][neuron][previous_neuron] += delta * output[layer - 1][previous_neuron];
				}
			}
		}
	}


	private double sigmoid(double x) {
		return 1d / (1 + Math.exp(-x));
	}


	public void save_network(String file_name) throws Exception{
		File file = new File(file_name);
		ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file));
		output.writeObject(this);
		output.flush();
		output.close();
	}


	public static Network load_network(String file_name) throws FileNotFoundException, IOException, ClassNotFoundException {
		File file = new File(file_name);
		ObjectInputStream output = new ObjectInputStream(new FileInputStream(file));
		Network net = (Network)output.readObject();
		output.close();
		return net;
	}
}