package FPGrowth;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * FP���Ĺ���
 * �Ӱٶ��ϳ���
 */
public class FPTree {
	private int minSup; // ��С֧�ֶ�
	// ��ʼ�����׼�¼
	List<List<String>> records = new LinkedList<List<String>>();
	public int getMinSup() {
		return minSup;
	}

	public void setMinSup(int minSup) {
		this.minSup = minSup;
	}

	/**
	 * 1.���������¼���ļ�����
	 * 
	 */
	public List<List<String>> readFromFile() {
		List<String> record;
		String csv = "D:\\Groceries.csv";
//		String csv="D:\\1.txt";
		BufferedReader br = null;
		String line = "";
		try {
			br = new BufferedReader(new FileReader(csv));
			while ((line = br.readLine()) != null) {
				record = new LinkedList<String>();
				String[] major = line.split("[{,}]");
				for (int i = 2; i < major.length-1; i++)
					record.add(major[i]);
				records.add(record);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return records;
	}
	/**
	 * 2.����Ƶ��1�
	 * 
	 */
	public ArrayList<TreeNode> find_frequent_1_itemsets(List<List<String>> transRecords) {
		ArrayList<TreeNode> F1 = null;
		if (transRecords.size() > 0) {
			F1 = new ArrayList<TreeNode>();
			Map<String, TreeNode> map = new HashMap<String, TreeNode>();
			// ����֧�ֶ�
			for (List<String> record : transRecords) {
				for (String item : record) {
					if (!map.keySet().contains(item)) {
						TreeNode node = new TreeNode(item);
						node.setCount(1);
						map.put(item, node);
					} else {
						TreeNode node=new TreeNode(item);
						node.setCount(map.get(item).getCount()+1);
						map.put(item,node);
					}
				}
			}
			Set<String> names = map.keySet();
			for (String name : names) {
				TreeNode tnode = map.get(name);
				if (tnode.getCount() >= minSup) {
					tnode.setName(name);
					F1.add(tnode);
				}
			}
			Collections.sort(F1);
//			for(TreeNode node:F1){
//				System.out.println(node.getName()+":"+node.getCount());
//			}
			return F1;
		} else {
			return null;
		}
	}

	/**
	 * 3����FP��
	 */
	public TreeNode buildFPTree(List<List<String>> transRecords, ArrayList<TreeNode> F1) {
		TreeNode root = new TreeNode(); // �������ĸ��ڵ�
		for (List<String> transRecord : transRecords) {
			LinkedList<String> record = sortByF1(transRecord, F1);
			TreeNode subTreeRoot = root;
			TreeNode tmpRoot = null;
			if (root.getChildren() != null) {
				while (!record.isEmpty() && (tmpRoot = subTreeRoot.findChild(record.peek())) != null) {
					tmpRoot.countIncrement(1);
					subTreeRoot = tmpRoot;
					record.poll();
				}
			}
			addNodes(subTreeRoot, record, F1);
		}
		return root;
	}

	/**
	 * 3.1���������ݿ��е�һ����¼����F1��Ƶ��1����е�˳������
	 * 
	 */
	public LinkedList<String> sortByF1(List<String> transRecord, ArrayList<TreeNode> F1) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (String item : transRecord) {
			for (int i = 0; i < F1.size(); i++) {
				TreeNode tnode = F1.get(i);
				if (item.equals(tnode.getName())) {
					map.put(item, i);
				}
			}
		}
		ArrayList<Entry<String, Integer>> al = new ArrayList<Entry<String, Integer>>(map.entrySet());
		Collections.sort(al, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> arg0, Entry<String, Integer> arg1) {
				return arg0.getValue() - arg1.getValue();
			}
		});
		LinkedList<String> rest = new LinkedList<String>();
		for (Entry<String, Integer> entry : al) {
			rest.add(entry.getKey());
		}
		return rest;
	}

	/**
	 * 3.2 �����ɸ��ڵ���Ϊָ��ָ���ڵ�ĺ����������
	 * 
	 */
	public void addNodes(TreeNode ancestor, LinkedList<String> record, ArrayList<TreeNode> F1) {
		if (record.size() > 0) {
			while (record.size() > 0) {
				String item = record.poll();
				TreeNode leafnode = new TreeNode(item);
				leafnode.setName(item);
				leafnode.setCount(1);
				leafnode.setParent(ancestor);
				ancestor.addChild(leafnode);
				for (TreeNode f1 : F1) {
					if (f1.getName().equals(item)) {
						while (f1.getNextSameNode() != null) {
							f1 = f1.getNextSameNode();
						}
						f1.setNextSameNode(leafnode);
						break;
					}
				}
				addNodes(leafnode, record, F1);
			}
		}
	}

	/**
	 * 4. ��FPTree���ҵ����е�Ƶ��ģʽ
	 * 
	 */
	public Map<List<String>, Integer> findFP(TreeNode root, ArrayList<TreeNode> F1) {
		Map<List<String>, Integer> fp = new HashMap<List<String>, Integer>();
		Iterator<TreeNode> iter = F1.iterator();
		while (iter.hasNext()) {
			TreeNode curr = iter.next();
			// Ѱ��cur������ģʽ��CPB������transRecords��
			List<List<String>> transRecords = new LinkedList<List<String>>();
			TreeNode backnode = curr.getNextSameNode();
			while (backnode != null) {
				int counter = backnode.getCount();
				List<String> prenodes = new ArrayList<String>();
				TreeNode parent = backnode;
				// ����backnode�����Ƚڵ㣬�ŵ�prenodes��
				while ((parent = parent.getParent()).getName() != null) {
					prenodes.add(parent.getName());
				}
				while (counter-- > 0) {
					transRecords.add(prenodes);
				}
				backnode = backnode.getNextSameNode();
			}

			// ��������Ƶ��1�
			ArrayList<TreeNode> subF1 = find_frequent_1_itemsets(transRecords);
			// ��������ģʽ���ľֲ�FP-tree
			TreeNode subRoot = buildFPTree(transRecords, subF1);

			// ������FP-Tree��Ѱ��Ƶ��ģʽ
			if (subRoot != null) {
				Map<List<String>, Integer> prePatterns = findPrePattern(subRoot);
				if (prePatterns != null) {
					Set<Entry<List<String>, Integer>> ss = prePatterns.entrySet();
					for (Entry<List<String>, Integer> entry : ss) {
						entry.getKey().add(curr.getName());
						fp.put(entry.getKey(), entry.getValue());
					}
				}
			}
		}
		return fp;
	}

	/**
	 * 4.1 ��һ��FP-Tree���ҵ����е�ǰ׺ģʽ
	 */
	public Map<List<String>, Integer> findPrePattern(TreeNode root) {
		Map<List<String>, Integer> patterns = null;
		List<TreeNode> children = root.getChildren();
		if (children != null) {
			patterns = new HashMap<List<String>, Integer>();
			for (TreeNode child : children) {
				// �ҵ���childΪ���ڵ�������е����г�·������ν��·��ָ�����������κ�·������·����
				LinkedList<LinkedList<TreeNode>> paths = buildPaths(child);
				if (paths != null) {
					for (List<TreeNode> path : paths) {
						Map<List<String>, Integer> backPatterns = combination(path);
						Set<Entry<List<String>, Integer>> entryset = backPatterns.entrySet();
						for (Entry<List<String>, Integer> entry : entryset) {
							List<String> key = entry.getKey();
							int c1 = entry.getValue();
							int c0 = 0;
							if (patterns.containsKey(key)) {
								c0 = patterns.get(key).byteValue();
							}
							patterns.put(key, c0 + c1);
						}
					}
				}
			}
		}

		// ���˵���ЩС��MinSup��ģʽ
		Map<List<String>, Integer> rect = null;
		if (patterns != null) {
			rect = new HashMap<List<String>, Integer>();
			Set<Entry<List<String>, Integer>> ss = patterns.entrySet();
			for (Entry<List<String>, Integer> entry : ss) {
				if (entry.getValue() >= minSup) {
					rect.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return rect;
	}

	/**
	 * 4.1.1 �ҵ���ָ���ڵ㣨root�������пɴ�Ҷ�ӽڵ��·��
	 */
	public LinkedList<LinkedList<TreeNode>> buildPaths(TreeNode root) {
		LinkedList<LinkedList<TreeNode>> paths = null;
		if (root != null) {
			paths = new LinkedList<LinkedList<TreeNode>>();
			List<TreeNode> children = root.getChildren();
			if (children != null) {
				// �ڴ����Ϸ��뵥��·��ʱ���Էֲ�ڵĽڵ㣬��countҲҪ�ֵ�����·����ȥ
				// ����FP-Tree�Ƕ�֦�����
				if (children.size() > 1) {
					for (TreeNode child : children) {
						int count = child.getCount();
						LinkedList<LinkedList<TreeNode>> ll = buildPaths(child);
						for (LinkedList<TreeNode> lp : ll) {
							TreeNode prenode = new TreeNode(root.getName());
							prenode.setCount(count);
							lp.addFirst(prenode);
							paths.add(lp);
						}
					}
				}
				// ����FP-Tree�ǵ�֦�����
				else {
					for (TreeNode child : children) {
						LinkedList<LinkedList<TreeNode>> ll = buildPaths(child);
						for (LinkedList<TreeNode> lp : ll) {
							lp.addFirst(root);
							paths.add(lp);
						}
					}
				}
			} else {
				LinkedList<TreeNode> lp = new LinkedList<TreeNode>();
				lp.add(root);
				paths.add(lp);
			}
		}
		return paths;
	}

	/**
	 * 4.1.2 ����·��path������Ԫ�ص�������ϣ�������ÿһ����ϵ�count--��ʵ������������һ��Ԫ�ص�count��
	 * 
	 */
	public Map<List<String>, Integer> combination(List<TreeNode> path) {
		if (path.size() > 0) {
			// ��path���Ƴ��׽ڵ�
			TreeNode start = path.remove(0);
			// �׽ڵ��Լ����Գ�Ϊһ����ϣ�����rect��
			Map<List<String>, Integer> rect = new HashMap<List<String>, Integer>();
			List<String> li = new ArrayList<String>();
			li.add(start.getName());
			rect.put(li, start.getCount());

			Map<List<String>, Integer> postCombination = combination(path);
			if (postCombination != null) {
				Set<Entry<List<String>, Integer>> set = postCombination.entrySet();
				for (Entry<List<String>, Integer> entry : set) {
					// ���׽ڵ�֮��Ԫ�ص�������Ϸ���rect��
					rect.put(entry.getKey(), entry.getValue());
					// �׽ڵ㲢�����Ԫ�صĸ�����Ϸ���rect��
					List<String> ll = new ArrayList<String>();
					ll.addAll(entry.getKey());
					ll.add(start.getName());
					rect.put(ll, entry.getValue());
				}
			}

			return rect;
		} else {
			return null;
		}
	}

	/**
	 * ��ӡƵ��1�
	 * 
	 */
	public void printF1(List<TreeNode> F1) {
		System.out.println("F-1 set: ");
		for (TreeNode item : F1) {
			System.out.print(item.getName() + ":" + item.getCount() + "\t");
		}
		System.out.println();
//		System.out.println();
	}

	/**
	 * ��ӡFP-Tree
	 */
	public void printFPTree(TreeNode root) {
		printNode(root);
		List<TreeNode> children = root.getChildren();
		if (children != null && children.size() > 0) {
			for (TreeNode child : children) {
				printFPTree(child);
			}
		}
	}

	/**
	 * ��ӡ���ϵ����ڵ����Ϣ
	 */
	public void printNode(TreeNode node) {
		if (node.getName() != null) {
			System.out.print(
					"Name:" + node.getName() + "\tCount:" + node.getCount() + "\tParent:" + node.getParent().getName());
			if (node.getNextSameNode() != null)
				System.out.print("\tNextSameNode:" + node.getNextSameNode().getName());
			System.out.print("\tChildren:");
			node.printChildrenName();
			System.out.println();
		} else {
			System.out.println("FPTreeRoot");
		}
	}

	/**
	 * ��ӡ�����ҵ�������Ƶ��ģʽ��
	 * 
	 * @param patterns
	 */
	public void printFreqPatterns(Map<List<String>, Integer> patterns) {
//		System.out.println();
		System.out.println("��С֧�ֶȣ�" + this.getMinSup());
		System.out.println("������С֧�ֶȵ�����Ƶ��ģʽ�����£�");
		Set<Entry<List<String>, Integer>> ss = patterns.entrySet();
		for (Entry<List<String>, Integer> entry : ss) {
			List<String> list = entry.getKey();
			for (String item : list) {
				System.out.print(item + " ");
			}
			System.out.print("\t" + entry.getValue());
			System.out.println();
		}
	}
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		FPTree fptree = new FPTree();
        fptree.setMinSup(5);
        List<List<String>> transRecords = fptree.readFromFile();
        for(List<String> record:transRecords){
        	for(String s:record)
       		System.out.print(s+";");
        	System.out.println();
        }
        System.out.println("��ȡ������ϣ�--------------------------------------------");
        ArrayList<TreeNode> F1 = fptree.find_frequent_1_itemsets(transRecords);
        System.out.println("�ҳ�Ƶ��1���ϣ�----------------------------------------");
        fptree.printF1(F1);
        TreeNode treeroot = fptree.buildFPTree(transRecords, F1);
        System.out.println("��������ϣ�----------------------------------------------");
        Map<List<String>, Integer> patterns = fptree.findFP(treeroot, F1);
        fptree.printFreqPatterns(patterns);
        long endTime = System.currentTimeMillis();
        System.out.println("�ҳ�Ƶ�������---------------------------------------------��");
        System.out.println("����ʱ��"+(endTime-startTime)+"ms");
	}

}