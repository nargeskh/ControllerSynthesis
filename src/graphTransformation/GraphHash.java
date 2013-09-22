package graphTransformation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class GraphHash implements Comparable<GraphHash>{
	public class Bi implements Comparable<Bi>{
		int ni;
		int no;
		public Bi(int size, int size2) {
			ni = size;
			no = size2;
		}
		@Override
		public int compareTo(Bi o2) {
			if(ni != o2.ni)
				return ni - o2.ni;
			return no - o2.no;
		}
	}
	int[][] array;
	Map<Bi, List<Node>> back = new TreeMap<Bi, List<Node>>();
	int ne;
	int nv;
	public GraphHash(Graph graph) {
		nv = graph.nodes.size();
		ne = graph.edges.size();
		array = new int[nv][2];
		int i = 0;
		for(Node iter : graph.nodes){
			Bi cur = new Bi(iter.ins.size(), iter.outs.size());
			List<Node> node = back.get(cur);
			if(node == null)
				node = new ArrayList<Node>();
			node.add(iter);
			if(node.size() == 1)
				back.put(cur, node);
			array[i][0] = cur.ni;
			array[i][1] = cur.no;
			++i;
		}
		Arrays.sort(array, new Comparator<int[]>() {

			@Override
			public int compare(int[] o1, int[] o2) {
				if(o1[0] != o2[0])
					return o1[0] - o2[0];
				return o1[1] - o2[1];
			}
		});
	}
	@Override
	public int compareTo(GraphHash o2) {
		if(nv != o2.nv)
			return nv - o2.nv;
		if(ne != o2.ne)
			return ne - o2.ne;
		for (int i = 0; i < nv; i++) {
			if(array[i][0] != o2.array[i][0])
				return array[i][0] - o2.array[i][0];
			if(array[i][1] != o2.array[i][1])
				return array[i][1] - o2.array[i][1];
		}
		return 0;
	}
}
