package Apriori;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 功能：Apriori算法的实现
 * author：131220044查鹏
 */
public class Apriori {
	private int minSup;
	private double minConf;
	private static List<String> data;
	private static List<Set<String>> dataSet;
	Map<Set<String>, Integer> DATA = new HashMap<Set<String>, Integer>();


	public void setMinSup(int minSup) {
		this.minSup = minSup;
	}

	public void setMinConf(double minConf) {
		this.minConf = minConf;
	}

	/**
	 * 数据集
	 */
	List<String> buildData() {
		String csv = "D:\\Groceries.csv";
		BufferedReader br = null;
		String line = "";
		List<String> data = new ArrayList<String>();
		dataSet = new ArrayList<Set<String>>();
		Set<String> dset;
		try {
			br = new BufferedReader(new FileReader(csv));
			while ((line = br.readLine()) != null) {// 读取一行
				dset = new TreeSet<String>();
				data.add(line);
				String[] major = line.split("[},{]");// 分割字符串
				for (int i = 2; i < major.length - 1; i++)// 获取数据
					dset.add(major[i]);		
				dataSet.add(dset);// 将数据加入事务集中
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
		return data;

	}

	/**
	 * 找出候选1项集
	 * 
	 */
	List<Set<String>> find_frequent_1_itemsets(List<String> data) {
		List<Set<String>> result = new ArrayList<Set<String>>();
		Map<String, Integer> dc = new HashMap<String, Integer>();
		for (String d : data) {
			String[] items = d.split("[{,}]");

			for (int i = 2; i < items.length - 1; i++) {
				if (dc.containsKey(items[i])) {
					dc.put(items[i], dc.get(items[i]) + 1);
				} else {
					dc.put(items[i], 1);
				}
			}
		}
		Set<String> itemKeys = dc.keySet();
		Set<String> tempKeys = new TreeSet<String>();
		for (String str : itemKeys) {
			tempKeys.add(str);
		}
		for (String item : tempKeys) {// 集合中每一个元素
			if (dc.get(item) >= minSup) {// 删除不满足最小支持度的
				Set<String> f1Set = new TreeSet<String>();
				f1Set.add(item);
				DATA.put(f1Set, dc.get(item));
				result.add(f1Set);
			}
		}
		return result;
	}

	/**
	 * 利用arioriGen方法由k-1项集生成k项集
	 */
	List<Set<String>> apriori_gen(List<Set<String>> preSet) {
		
		List<Set<String>> result = new ArrayList<Set<String>>();
		int preSetSize = preSet.size();
		for (int i = 0; i < preSetSize - 1; i++) {
			for (int j = i + 1; j < preSetSize; j++) {
				String[] strA1 = preSet.get(i).toArray(new String[0]);
				String[] strA2 = preSet.get(j).toArray(new String[0]);
				if (isCanLink(strA1, strA2)) {// 判断两个k-1项集是否符合连接成K项集的条件
					Set<String> set = new TreeSet<String>();
					for (String str : strA1) {
						set.add(str);// 将strA1加入set中连成前K-1项集
					}
					set.add((String) strA2[strA2.length - 1]);// 连接成K项集
					// 判断K项集是否需要剪切掉，如果不需要被cut掉，则加入到k项集的列表中
					if (!isNeedCut(preSet, set)) {
						result.add(set);
					}
				}
			}
		}
		return checkSupport(result);// 返回的都是频繁K项集
	}

	/**
	 * 把set中的项集与数量集比较并进行计算，求出支持度大于要求的项集
	 * 
	 */
	List<Set<String>> checkSupport(List<Set<String>> setList) {
		List<Set<String>> result = new ArrayList<Set<String>>();
		boolean flag = true;
		int[] counter = new int[setList.size()];
		for (int i = 0; i < setList.size(); i++) {
			for (Set<String> dSets : dataSet) {
				if (setList.get(i).size() > dSets.size()) {
					flag = true;
				} else {
					for (String str : setList.get(i)) {
						if (!dSets.contains(str)) {
							flag = false;
							break;
						}
					}
					if (flag) {
						counter[i] += 1;
					} else {
						flag = true;
					}
				}
			}
		}
		for (int i = 0; i < setList.size(); i++) {
			if (counter[i] >= minSup) {
				result.add(setList.get(i));
				DATA.put(setList.get(i), counter[i]);
			}
		}
		return result;
	}

	/**
	 * 判断两个项集能否执行连接操作
	 */
	boolean isCanLink(String[] s1, String[] s2) {
		boolean flag = true;
		if (s1.length == s2.length) {
			for (int i = 0; i < s1.length - 1; i++) {
				if (!s1[i].equals(s2[i])) {
					flag = false;
					break;
				}
			}
			if (s1[s1.length - 1].equals(s2[s2.length - 1])) {
				flag = false;
			}
		} else {
			flag = true;
		}
		return flag;
	}

	/**
	 * 剪枝
	 */
	boolean isNeedCut(List<Set<String>> setList, Set<String> set) {// setList指频繁K-1项集，set指候选K项集
		boolean flag = false;
		List<Set<String>> subSets = subset(set);// 获得K项集的所有k-1项集
		for (Set<String> subSet : subSets) {
			// 判断当前的k-1项集set是否在频繁k-1项集中出现，如果出现，则不需要cut
			// 若没有出现，则需要被cut
			if (!has_infrequent_subset(setList, subSet)) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	/**
	 * 功能:判断k项集的某k-1项集是否包含在频繁k-1项集列表中
	 * 
	 */
	boolean has_infrequent_subset(List<Set<String>> setList, Set<String> set) {
		boolean flag = false;
		int position = 0;
		for (Set<String> s : setList) {
			String[] sArr = s.toArray(new String[0]);
			String[] setArr = set.toArray(new String[0]);
			for (int i = 0; i < sArr.length; i++) {
				if (sArr[i].equals(setArr[i])) {
					// 如果对应位置的元素相同，则position为当前位置的值
					position = i;
				} else {
					break;
				}
			}
			// 如果position等于数组的长度，说明已经找到某个setList中的集合与
			// set集合相同了，退出循环，返回包含
			// 否则，把position置为0进入下一个比较
			if (position == sArr.length - 1) {
				flag = true;
				break;
			} else {
				flag = false;
				position = 0;
			}
		}
		return flag;
	}

	/**
	 * 获得k项集的所有k-1项子集
	 */
	List<Set<String>> subset(Set<String> set) {
		List<Set<String>> result = new ArrayList<Set<String>>();
		String[] setArr = set.toArray(new String[0]);
		for (int i = 0; i < setArr.length; i++) {
			Set<String> subSet = new TreeSet<String>();
			for (int j = 0; j < setArr.length; j++) {
				if (i != j) {
					subSet.add((String) setArr[j]);
				}
			}
			result.add(subSet);
		}
		return result;
	}

	/**
	 * 功能：打印频繁项集
	 */
	void printSet(List<Set<String>> setList, int i) {
		System.out.print("频繁" + i + "项集： 共" + setList.size() + "项： {");
		for (Set<String> set : setList) {
			System.out.print("[");
			for (String str : set) {
				System.out.print(str + ",");
			}
			System.out.print("], ");
		}
		System.out.println("}");
	}

	/**
	 * 
	 * @param set
	 * @return
	 */
	public ArrayList<Set<String>> getAllSubsets(Set<String> set){  
        ArrayList<Set<String>> allsubsets = new ArrayList<Set<String>>();  
        int max = 1 << set.size(); //how many sub sets  
        String[] major=set.toArray(new String[0]);
        for(int i=0; i<max; i++){  
            int index = 0;  
            int k = i;  
            Set<String> s = new TreeSet<String>();  
            while(k > 0){  
                if((k&1) > 0){  
                    s.add(major[index]);  
                }  
                k>>=1;  
                index++;  
            }  
            allsubsets.add(s);  
        }  
        for(int i=0;i<allsubsets.size();i++){
        	if(allsubsets.get(i).isEmpty())
        		allsubsets.remove(i);
        }
        return allsubsets;  
    }  
	
	/**
	 * @param set
	 * 
	 */
	public void print(Set<String> set) {
		System.out.print("[");
		for (String str : set)
			System.out.print(str + ",");
		System.out.print("]");
	}

	/**
	 * 给一个频繁项集，输出满足置信度的规则
	 */

	public void printRules(Set<String> set) {// 对一个项集求其规则		
		int sup_AB = DATA.get(set);
		Set<String> stCopy = new TreeSet<>();
		ArrayList<Set<String>> subSets = getAllSubsets(set);// 获取子集
		for (Set<String> s:subSets) {// 对每一个子集
//			stCopy = new TreeSet<>();
			stCopy = set;
			stCopy.removeAll(s);// 这样得到的stCopy就是t=l-s
			boolean flag = true;
			if (stCopy.isEmpty() || s.isEmpty())
				flag = false;
			if (flag) {
				print(stCopy);
				double Conf1 = (double) sup_AB / DATA.get(s);
				double Conf2=(double)sup_AB/DATA.get(stCopy);
				if (Conf1 > minConf) {
					print(s);
					System.out.print("=>");
					print(stCopy);
					System.out.print(Conf1);
					System.out.println();
				}
				if (Conf2 > minConf) {
					print(stCopy);
					System.out.print("=>");
					print(s);
					System.out.print(Conf2);
					System.out.println();
				}
			}
		}
	}

	public void apriori() {
		// 构造频繁1项集
		List<Set<String>> f1Set = find_frequent_1_itemsets(data);
		printSet(f1Set, 1);
		List<Set<String>> C = f1Set;
		int i = 2;
		do {
			C = apriori_gen(C);// 迭代
			printSet(C, i);
			i++;
//下面注释掉的是用来输出association	 relus的		
//			List<Set<String>>cTemp=C;
//			for(int index=0;index<cTemp.size();index++)
//				printRules(cTemp.get(index));			
		} while (C.size() != 0);
	}

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		Apriori apriori = new Apriori();
		data = apriori.buildData();
		// 设置最小支持度
		apriori.setMinSup(70);
		apriori.setMinConf(0.5);
		// 构造数据集
		data = apriori.buildData();
		apriori.apriori();
		long endTime = System.currentTimeMillis();
		System.out.println("共用时： " + (endTime - startTime) + "ms");
	}
}