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
 * ���ܣ�Apriori�㷨��ʵ��
 * author��131220044����
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
	 * ���ݼ�
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
			while ((line = br.readLine()) != null) {// ��ȡһ��
				dset = new TreeSet<String>();
				data.add(line);
				String[] major = line.split("[},{]");// �ָ��ַ���
				for (int i = 2; i < major.length - 1; i++)// ��ȡ����
					dset.add(major[i]);		
				dataSet.add(dset);// �����ݼ���������
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
	 * �ҳ���ѡ1�
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
		for (String item : tempKeys) {// ������ÿһ��Ԫ��
			if (dc.get(item) >= minSup) {// ɾ����������С֧�ֶȵ�
				Set<String> f1Set = new TreeSet<String>();
				f1Set.add(item);
				DATA.put(f1Set, dc.get(item));
				result.add(f1Set);
			}
		}
		return result;
	}

	/**
	 * ����arioriGen������k-1�����k�
	 */
	List<Set<String>> apriori_gen(List<Set<String>> preSet) {
		
		List<Set<String>> result = new ArrayList<Set<String>>();
		int preSetSize = preSet.size();
		for (int i = 0; i < preSetSize - 1; i++) {
			for (int j = i + 1; j < preSetSize; j++) {
				String[] strA1 = preSet.get(i).toArray(new String[0]);
				String[] strA2 = preSet.get(j).toArray(new String[0]);
				if (isCanLink(strA1, strA2)) {// �ж�����k-1��Ƿ�������ӳ�K�������
					Set<String> set = new TreeSet<String>();
					for (String str : strA1) {
						set.add(str);// ��strA1����set������ǰK-1�
					}
					set.add((String) strA2[strA2.length - 1]);// ���ӳ�K�
					// �ж�K��Ƿ���Ҫ���е����������Ҫ��cut��������뵽k����б���
					if (!isNeedCut(preSet, set)) {
						result.add(set);
					}
				}
			}
		}
		return checkSupport(result);// ���صĶ���Ƶ��K�
	}

	/**
	 * ��set�е�����������Ƚϲ����м��㣬���֧�ֶȴ���Ҫ����
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
	 * �ж�������ܷ�ִ�����Ӳ���
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
	 * ��֦
	 */
	boolean isNeedCut(List<Set<String>> setList, Set<String> set) {// setListָƵ��K-1���setָ��ѡK�
		boolean flag = false;
		List<Set<String>> subSets = subset(set);// ���K�������k-1�
		for (Set<String> subSet : subSets) {
			// �жϵ�ǰ��k-1�set�Ƿ���Ƶ��k-1��г��֣�������֣�����Ҫcut
			// ��û�г��֣�����Ҫ��cut
			if (!has_infrequent_subset(setList, subSet)) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	/**
	 * ����:�ж�k���ĳk-1��Ƿ������Ƶ��k-1��б���
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
					// �����Ӧλ�õ�Ԫ����ͬ����positionΪ��ǰλ�õ�ֵ
					position = i;
				} else {
					break;
				}
			}
			// ���position��������ĳ��ȣ�˵���Ѿ��ҵ�ĳ��setList�еļ�����
			// set������ͬ�ˣ��˳�ѭ�������ذ���
			// ���򣬰�position��Ϊ0������һ���Ƚ�
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
	 * ���k�������k-1���Ӽ�
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
	 * ���ܣ���ӡƵ���
	 */
	void printSet(List<Set<String>> setList, int i) {
		System.out.print("Ƶ��" + i + "��� ��" + setList.size() + "� {");
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
	 * ��һ��Ƶ���������������ŶȵĹ���
	 */

	public void printRules(Set<String> set) {// ��һ����������		
		int sup_AB = DATA.get(set);
		Set<String> stCopy = new TreeSet<>();
		ArrayList<Set<String>> subSets = getAllSubsets(set);// ��ȡ�Ӽ�
		for (Set<String> s:subSets) {// ��ÿһ���Ӽ�
//			stCopy = new TreeSet<>();
			stCopy = set;
			stCopy.removeAll(s);// �����õ���stCopy����t=l-s
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
		// ����Ƶ��1�
		List<Set<String>> f1Set = find_frequent_1_itemsets(data);
		printSet(f1Set, 1);
		List<Set<String>> C = f1Set;
		int i = 2;
		do {
			C = apriori_gen(C);// ����
			printSet(C, i);
			i++;
//����ע�͵������������association	 relus��		
//			List<Set<String>>cTemp=C;
//			for(int index=0;index<cTemp.size();index++)
//				printRules(cTemp.get(index));			
		} while (C.size() != 0);
	}

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		Apriori apriori = new Apriori();
		data = apriori.buildData();
		// ������С֧�ֶ�
		apriori.setMinSup(70);
		apriori.setMinConf(0.5);
		// �������ݼ�
		data = apriori.buildData();
		apriori.apriori();
		long endTime = System.currentTimeMillis();
		System.out.println("����ʱ�� " + (endTime - startTime) + "ms");
	}
}