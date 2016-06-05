package io.personalityrecognition.util;

/**
 * This class processes the raw results into the data type we want to output.
 *
 * @param
 * @return
 * @throws
 */
public class TestResults {

	private Double truePositives;
	private Double falsePositives;
	private Double trueNegatives;
	private Double falseNegatives;
	private Double accuracy;
	private Double precision;
	private Double recall;
	private Double npv;
	private Double specificity;
	private Double fMeasure;
	private String model;
	private String trait;

	public TestResults() {}

	public TestResults setModel(String model) {
		this.model = model;
		return this;
	}

	public String getModel() {
		return model;
	}

	public TestResults setTrait(String trait) {
		this.trait = trait;
		return this;
	}

	public String getTrait() {
		return trait;
	}

	public TestResults(String trait, String model, Double truePositives, Double falsePositives, Double trueNegatives, Double falseNegatives) {
		this.trait = trait;
		this.model = model;
		this.truePositives = truePositives;
		this.falsePositives = falsePositives;
		this.trueNegatives = trueNegatives;
		this.falseNegatives = falseNegatives;
	}

	public TestResults setTruePositives(Double truePositives) {
		this.truePositives = truePositives;
		return this;
	}

	public TestResults setFalsePositives(Double falsePositives) {
		this.falsePositives = falsePositives;
		return this;
	}

	public TestResults setTrueNegatives(Double trueNegatives) {
		this.trueNegatives = trueNegatives;
		return this;
	}

	public TestResults setFalseNegatives(Double falseNegatives) {
		this.falseNegatives = falseNegatives;
		return this;
	}

	public double getAccuracy() {
		checkArgumentsAndThrow();

		if(accuracy == null)
			accuracy = (truePositives + trueNegatives) / (truePositives + trueNegatives + falsePositives + falseNegatives);

		return accuracy;
	}

	public double getPrecision() {
		checkArgumentsAndThrow();

		if(precision == null)
			precision = truePositives / (truePositives + falsePositives);

		return precision;
	}

	public double getRecall() {
		checkArgumentsAndThrow();

		if(recall == null)
			recall = truePositives / (truePositives + falseNegatives);

		return recall;
	}

	public double getSpecificity() {
		checkArgumentsAndThrow();

		if(specificity == null)
			specificity = trueNegatives / (trueNegatives + falsePositives);

		return specificity;
	}

	public double getNPV() {
		checkArgumentsAndThrow();

		if(npv == null)
			npv = trueNegatives / (trueNegatives + falseNegatives);

		return npv;
	}

	public double getFMeasure() {
		checkArgumentsAndThrow();

		if(fMeasure == null) {
			double p = getPrecision();
			double r = getRecall();
			fMeasure = 2 * p * r / (p + r);
		}

		return fMeasure;
	}

	private void checkArgumentsAndThrow() {
		if(!argumentsAreValid())
			throw new IllegalArgumentException();
	}

	private boolean argumentsAreValid() {
		return truePositives != null && trueNegatives != null && falsePositives != null && falseNegatives != null;
	}
}
