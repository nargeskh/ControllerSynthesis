package graphTransformation;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ShapeNode extends Node implements Comparable<ShapeNode> {
	Map<String, Integer> insm = new HashMap<String, Integer>();
	Map<String, Integer> outsm = new HashMap<String, Integer>();
	int count;
	public ShapeNode(Node node){
		super(node);
		count = 1;
	}
	public ShapeNode(ShapeNode node) {
		super(node);
		count = node.count;
		insm = new HashMap<String, Integer>(node.insm);
		outsm = new HashMap<String, Integer>(node.outsm);
	}
	@Override
	public int compareTo(ShapeNode o2) {
		if(this == o2)
			return 0;
		int result = name.compareTo(o2.name);
		if(result != 0)
			return result;
		
		result = insm.size() - o2.insm.size();
		if(result != 0)
			return result;
		result = outsm.size() - o2.outsm.size();
		if(result != 0)
			return result;
		
		for(Entry<String, Integer> entry  :  insm.entrySet()){
			String key = entry.getKey();
			Integer value = entry.getValue();
			Integer value1 = o2.insm.get(key);
			if(null == value1)
				return -value;
			result = value - value1;
			if(result != 0)
				return result;
		}
		for(Entry<String, Integer> entry  :  outsm.entrySet()){
			String key = entry.getKey();
			Integer value = entry.getValue();
			Integer value1 = o2.outsm.get(key);
			if(null == value1)
				return -value;
			result = value - value1;
			if(result != 0)
				return result;
		}
		return 0;
	}
}
