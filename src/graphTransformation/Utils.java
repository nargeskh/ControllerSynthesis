package graphTransformation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class Utils {

	public static void putIntoFile(String content, String fileName){
		try {			
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName + ".dot")));
			writer.write(content);
			writer.close();
//			String command = "dot -Tjpg -o " + fileName + ".jpg " + fileName + ".dot";
//			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static <T, S> Map<T, List<S>> reverseMap(Map<S, T> map) {
		Map<T, List<S>> reverse = null;
		if(map != null){
			reverse = new HashMap<T, List<S>>();
			for(Entry<S, T>  entry : map.entrySet()){
				S key = entry.getKey();
				T value = entry.getValue();
				List<S> result = reverse.get(value);
				if(result == null){
					result = new ArrayList<S>();
					reverse.put(value, result);
				}
				result.add(key);
			}
		}
		return reverse;
	}
}
