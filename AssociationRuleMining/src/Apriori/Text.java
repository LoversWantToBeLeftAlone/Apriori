package Apriori;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
//这个类用不到，就是我在编程时候用来测试函数用的
public class Text {
	public static ArrayList<Set<String>> getAllSubsets(Set<String> set){  
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
	public static void print(Set<String> set) {
		System.out.print("[");
		for (String str : set)
			System.out.print(str + ",");
		System.out.println("]");
	}

	public static void main(String[]args){
		Set<String> set=new TreeSet<>();
		set.add("i1");
		set.add("i2");
		set.add("i3");
		set.add("i5");
		Set<String>s=new TreeSet<>();
		s.add("i1");
		s.add("i2");
		set.removeAll(s);
		print(set);
	}
}
