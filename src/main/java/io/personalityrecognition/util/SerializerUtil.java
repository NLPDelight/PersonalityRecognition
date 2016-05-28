package io.personalityrecognition.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tom
 */
public class SerializerUtil {
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
