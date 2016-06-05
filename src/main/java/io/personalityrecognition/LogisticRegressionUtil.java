package io.personalityrecognition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.mahout.classifier.evaluation.Auc;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.ConstantValueEncoder;
import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder;

public class LogisticRegressionUtil {

	/**
	 * This method converts the N-gram maps to List<Observations> so they can be put into mahout.
	 *
	 * @param Map<String, Double>, Map<String, Double>
	 * @return List<Observation>
	 * @throws
	 */
	public List<Observation> parseInput(Map<String, Double> map, Map<String, Double> mapB) {
		List<Observation> result = new ArrayList<>();

		for (Entry<String, Double> entry : map.entrySet()) {
			result.add(new Observation(1, entry.getKey(), entry.getValue()));
		}

		for (Entry<String, Double> entry : mapB.entrySet()) {
			result.add(new Observation(0, entry.getKey(), entry.getValue()));
		}

		return result;
	}

	/**
	 * This method converts the N-gram maps for test to List<Observations> so they can be put into mahout.
	 *
	 * @param Map<String, Double>
	 * @return List<Observation>
	 * @throws
	 */
	public List<Observation> parseTest(Map<String, Double> list) {
		List<Observation> result = new ArrayList<>();

		for (Entry<String, Double> s : list.entrySet()) {
			result.add(new Observation(1, s.getKey(), s.getValue()));
		}

		return result;
	}

	/**
	 * This method trains Logistic Regression model using Mahout
	 *
	 * @param List<Observation>
	 * @return OnlineLogisticRegression
	 * @throws
	 */
	public OnlineLogisticRegression train(List<Observation> trainData) {
		OnlineLogisticRegression olr = new OnlineLogisticRegression(2, 3,
			new L1());
		// Train the model using 30 passes
		for (int pass = 0; pass < 30; pass++) {
			for (Observation observation : trainData) {
				olr.train(observation.getActual(), observation.getVector());
			}
			// Every 10 passes check the accuracy of the trained model
			if (pass % 1 == 0) {
				Auc eval = new Auc(0.5);
				for (Observation observation : trainData) {
					eval.add(observation.getActual(),
						olr.classifyScalar(observation.getVector()));
				}
//				System.out.format(
//					"Pass: %2d, Learning rate: %2.4f, Accuracy: %2.4f\n",
//					pass, olr.currentLearningRate(), eval.auc());
			}
		}
		return olr;
	}

	/**
	 * This method tests Logistic Regression model using Mahout
	 *
	 * @param Map<String, Double>, OnlineLogisticRegression
	 * @return double
	 * @throws
	 */
	public double testModel(Map<String, Double> testMap, OnlineLogisticRegression olr) {
		List<Observation> testData = parseTest(testMap);

		List<Double> results = new ArrayList<>();

		double sum = 0;
		for (Observation o : testData) {
			Vector result = olr.classifyFull(o.getVector());

			results.add(result.get(1));

			sum += result.get(1);
//			System.out.println("------------- Testing -------------");
//			System.out.format("Probability of not fraud (0) = %.3f\n",
//			result.get(0));
//			System.out.format("Probability of fraud (1)     = %.3f\n",
//			result.get(1));
			}
		return sum/testData.size();
		}

	/**
	 * This is the class type needed to process data into Mahout
	 *
	 */
	class Observation {

		private DenseVector vector = new DenseVector(3);
		private int actual;

		public Observation(int actual, String gram, double value) {
			ConstantValueEncoder interceptEncoder = new ConstantValueEncoder(
				"intercept");
			StaticWordValueEncoder encoder = new StaticWordValueEncoder(
				"feature");

			interceptEncoder.addToVector("1", vector);
			encoder.addToVector(gram, vector);
			vector.set(0, value);
			this.actual = actual;
		}

		public Vector getVector() {
			return vector;
		}

		public int getActual() {
			return actual;
		}
	}
}
