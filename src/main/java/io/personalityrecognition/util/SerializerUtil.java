package io.personalityrecognition.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * This class handles the store and load of serialized objects, such as three layer maps, for faster loading
 *
 * @param
 * @return
 * @throws
 */
public class SerializerUtil {
	/**
	 * This method stores serialized trained map
	 *
	 * @param HashMap<String,Map<String, Double>>, Path
	 * @return
	 * @throws IOException
	 */
	public static void storeSerial(
			HashMap<String,Map<String, Double>> map, Path filePath)
		throws IOException{

		FileOutputStream fos = new FileOutputStream(filePath.toFile());

        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(map);
        oos.flush();
        oos.close();
        fos.close();
	}

	/**
	 * This method stores serialized test map
	 *
	 * @param HashMap<String,Map<String, Map<String, Double>>>, Path
	 * @return
	 * @throws IOException
	 */
	public static void storeSerialTest(
			HashMap<String,Map<String, Map<String, Double>>> map, Path filePath)
		throws IOException{

		FileOutputStream fos = new FileOutputStream(filePath.toFile());

        ObjectOutputStream oos = new ObjectOutputStream(fos);

        oos.writeObject(map);
        oos.flush();
        oos.close();
        fos.close();
	}

	/**
	 * This method loads serialized trained map
	 *
	 * @param Path
	 * @return HashMap<String,Map<String, Double>>
	 * @throws Exception
	 */
	public static HashMap<String,Map<String, Double>> loadSerial(Path filePath)
		throws Exception {

		FileInputStream fis = new FileInputStream(filePath.toFile());
        ObjectInputStream ois = new ObjectInputStream(fis);

        HashMap<String,Map<String, Double>> mapInFile =
			(HashMap<String,Map<String, Double>>)ois.readObject();

        ois.close();
        fis.close();

		return mapInFile;
	}

	/**
	 * This method loads serialized test map
	 *
	 * @param Path
	 * @return HashMap<String,Map<String, Map<String, Double>>>
	 * @throws Exception
	 */
	public static HashMap<String,Map<String, Map<String, Double>>> loadSerialTest(Path filePath)
		throws Exception {

		FileInputStream fis = new FileInputStream(filePath.toFile());
        ObjectInputStream ois = new ObjectInputStream(fis);

        HashMap<String,Map<String, Map<String, Double>>> mapInFile =
			(HashMap<String,Map<String, Map<String, Double>>>)ois.readObject();

        ois.close();
        fis.close();

		return mapInFile;
	}
}
