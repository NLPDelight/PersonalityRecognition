package io.personalityrecognition.util;

import static io.personalityrecognition.util.DatasetKeys.TRAIT_CLASSES;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

	/**
	 * This class takes in a dataset and returns processed PCA vectors.
	 *
	 * @param
	 * @return
	 * @throws
	 */
public class PCADataReader {

	public static final String INDEPENDENT_VARIABLES = "independent";
	public static final String DEPENDENT_VARIABLES = "dependent";

	/**
	 * This method reads the raw dataset and converts to vectors
	 *
	 * @param String
	 * @returnList<Map<String, double[]>>
	 * @throws
	 */
	public static List<Map<String, double[]>> readPCAFile(String fileName)
			throws FileNotFoundException, UnsupportedEncodingException, IOException {
		List<Map<String, String>> data = CSVMapper.mapCSV(new File(fileName));
		List<Map<String, double[]>> vectors = new ArrayList<>();

		for(Map<String, String> row : data) {
			HashMap<String, double[]> newRow = new HashMap<String, double[]>();
			newRow.put(INDEPENDENT_VARIABLES, getDimensions(row));
			newRow.put(DEPENDENT_VARIABLES, getTraits(row));
			vectors.add(newRow);
		}

		return vectors;
	}

	/**
	 * This method gets the traits from the raw data row
	 *
	 * @param Map<String, String>
	 * @return double[]
	 * @throws
	 */
	private static double[] getTraits(Map<String, String> row) {
		double[] traits = new double[TRAIT_CLASSES.size()];
		int i = 0;
		for(String trait : TRAIT_CLASSES) {
			traits[i] = Double.parseDouble(row.get(trait));
			i++;
		}
		return traits;
	}

	/**
	 * This method gets the dimension of the row
	 *
	 * @param Map<String, String>
	 * @return double[]
	 * @throws
	 */
	private static double[] getDimensions(Map<String, String> row) {
		int dimensionCount = row.size() - TRAIT_CLASSES.size() - 1; // -1 for authid column
		double[] dimensions = new double[dimensionCount];
		for(int i = 0; i < dimensionCount; i++) {
			String columnName = "D_" + (i + 1);
			dimensions[i] = Double.parseDouble(row.get(columnName));
		}
		return dimensions;
	}

}
