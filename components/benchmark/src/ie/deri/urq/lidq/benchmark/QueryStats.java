package ie.deri.urq.lidq.benchmark;

import ie.deri.urq.lidaq.benchmark.QueryExecutionBenchmark;
import ie.deri.urq.lidq.benchmark.eval.StabilityStats.STABILITYCODE;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

public class QueryStats{


	public Map<File,Integer[]> setupValues = new TreeMap<File, Integer[]>();
	private String _queryID;
	private boolean error =false;
	private int _setups;
	String [] fixes;
	
	/**
	 * @param ww
	 * @param acceptedQueries 
	 * @param length 
	 */
	public QueryStats(String ww, ArrayList<String> acceptedQueries) {
		_queryID = ww;
		_setups = acceptedQueries.size();
		fixes = new String[acceptedQueries.size()];
		fixes = acceptedQueries.toArray(fixes);
		
	}

	
	
	public String getQueryID(){
		return _queryID;
	}

	public String getStatusArrayHeaderString() {
		String s = _queryID;
		for(Entry<File,Integer[]> ent : setupValues.entrySet()){
			s+=" "+ent.getKey().getName();
		}
		return s;
	}

//	public String getStatusArrayString() {
//		String s = _queryID;
//		for(Entry<File,Integer[]> ent : setupValues.entrySet()){
//			s+=" "+getStatus(ent.getKey());
//		}
//		return s;
//	}
//
//	public int[] getAggStatus(){
//		int [] st = new int[setupValues.size()];
//		Arrays.fill(st, -1);
//		for(File s: setupValues.keySet()){
//			st[getStatus(s)]++;
//		}
//		return st;
//	}
//
//	public String getQueryName(){
//		return _queryID;
//	}
//
//	public STABILITYCODE getOnlyOverlapingStats() {
//		int resp = 6;
//		boolean consistent = true;
//		for(Entry<File,Integer[]> ent : setupValues.entrySet()){
//			int status = getStatus(ent.getKey());
//			if(resp==6) resp = status;
//			else if(status != resp){
//				if((status==4 || status ==0) && (resp == 0 || resp ==4)){
//					resp = 4;
//				}else{
//					consistent = false;
//				}
//			}
//		}
//		if(consistent) return resp;
//		else return 6;
//	}


	
	private HashSet<String>[][][] urlSet = null;
	private int baseIdx;
	private String _query;

	/**
	 * @param fw2 
	 * @return true if the query is defined as stable, 
	 *     
	 */
	public boolean isStable(FileWriter fw2,File file) {
		try{
			/**
			 * Try to find the most stable combination
			 */
			Map<File,Integer[]>[] confs = new HashMap[fixes.length];
			for(int i =0;i <fixes.length;i++){
				confs[i] = new HashMap<File, Integer[]>();
			}
			for(Entry<File,Integer[]> ent : setupValues.entrySet()){
				for(int i =0; i< fixes.length;i++){
					if(ent.getKey().getName().endsWith(fixes[i])){
						confs[i].put(ent.getKey(), ent.getValue());
					}
				}
			}
			boolean missing = false;

			for(int i =0;i <fixes.length;i++){
				if(confs[i].size() == 0){
					if(fw2!=null){
						fw2.write("echo \""+_queryID+" "+i+"\" >> missing.txt \n");fw2.flush();
					}
					
					missing = true;
					System.out.println("no configuration for "+confs[i].keySet());
				}
			}
			if(missing ) return false;

			if(urlSet == null){
				urlSet = new HashSet[fixes.length][5][2];
				baseIdx= 0;
				for(int i =0;i<fixes.length;i++){
					int a=0;
					for(Entry<File,Integer[]> ent : confs[i].entrySet()){
						urlSet[i][a]= getURLsSets(ent.getKey());
						a++;
						if(ent.getKey().getName().contains("smart")){
							baseIdx = i;
						}
					}
				}
			}
			
			

			int a=0;
			String s = null;
			int cnt =0;
//			System.out.print("#");
			TreeMap<File,Integer[]> map = new TreeMap<File, Integer[]>();

			for(Entry<File,Integer[]> ent : confs[baseIdx].entrySet()){
				HashSet<String> all = new HashSet<String>();
				HashSet<String>[] base = urlSet[baseIdx][a];
//				System.out.print(".");
				all.addAll(base[0]);all.addAll(base[1]);
				HashSet<String> cur[][] = new HashSet[fixes.length][2];

				int b=0;
				for(Entry<File,Integer[]> ent1 : confs[0].entrySet()){
					if(urlSet[0][b] != null){
						cur[0]= urlSet[0][b];
						map.put(ent1.getKey(),ent1.getValue());
						//						if(ent1.getValue()[0]!=0){
						s = analyse(1,cur,urlSet,all,base,confs,map,baseIdx,b);
						if(s!=null){
							break;
						}
						else{
							map.remove(ent1.getKey());
							s=null;
						}
						//						}
						//						else{
						//							map.remove(ent1.getKey());
						//							s=null;
						//						}
					}
					b++;
				}

			}

			if(map.size()!=fixes.length){
				System.out.println(map.size()+ " != "+fixes.length);
				return false;
			}
			boolean stable = (s!=null);

//			if(stable && fw2 != null){
//				int c =0;
//				for(File folder: map.keySet()){
//					File toDir = null;
//					String name = folder.getName();
//					for(String st: fixes){
//						if(name.contains(st)){
//							toDir = new File(file,name.substring(0, name.indexOf(st)+st.length()));
//							break;
//						}
//					}
//					for(File quDir: folder.listFiles()){
//						if(quDir.getName().startsWith(_query)){
//							fw2.write("mv "+quDir+" "+new File(toDir,quDir.getName())+"\n");
//							fw2.flush();
//							if(!Copy.copyDirectory(quDir, new File(toDir,quDir.getName()))){
//								System.out.println(quDir+" to "+toDir);
//							}
//							else{
//								c++;
//							}		
//						}
//					}
//				}
//
//			}
			return stable;
			//
			//			HashSet<String> set[][] = new HashSet[10][2];
			//			int c = 0;
			//			int baseIdx= 0;
			//			HashSet<String> all = new HashSet<String>();
			//			HashSet<String>[] base = set[2];
			//			for(Entry<File,Integer[]> ent : setupValues.entrySet()){
			//				System.out.println(ent.getKey());
			//				set[c]= getURLsSets(ent.getKey());
			//				if(ent.getKey().getName().contains("smart")){
			//					base = set[c];
			//					all.addAll(base[0]);all.addAll(base[1]);
			//					baseIdx = c;
			//				}
			//				c++;
			//			}
			//
			//
			//
			//
			//			//in theory , all the URIs in smart should appear 
			//
			//			boolean stable = true;
			//			for(int i=0;i<10;i++){
			//				for(String s : all){
			//					if(base[0].contains(s) && !set[i][0].contains(s)) stable = false;
			//					if(base[1].contains(s) && !set[i][1].contains(s)) stable = false;
			//				}
			//
			//				if(!stable){
			//					int a=0;
			//					//				for(Entry<File,Integer[]> ent : setupValues.entrySet()){
			//					//					if(a==i){
			//					//						System.out.println(ent.getKey()+"/"+this.getQueryID());
			//					//						System.out.println("bad:"+base[0]);
			//					//						System.out.println("bad:"+set[i][0]);
			//					//						System.out.println("good:"+base[1]);
			//					//						System.out.println("good:"+set[i][1]);
			//					//					}
			//					//					a++;
			//					//					
			//					//				}
			//
			//				}
			//			}
			//
			//			boolean stable2 = stable;
			//			if(stable){
			//
			//
			//				int [] results = new int[10];
			//				int b=0;
			//				for(Entry<File,Integer[]> ent : setupValues.entrySet()){
			//					results[b++]= ent.getValue()[0];
			//					if(ent.getValue()[0]==0) stable2=false;
			//				}
			//				for( int i=0;i< 10;i++){
			//					if(i!=2 && results[2]>results[i])
			//						stable2 = false;
			//				}
			//
			//				//			combination always better
			//				for( int i=0; i< 10;i++){
			//					if(i!=1 && i!=8 && i!=0 && i!=6 && i!=7 && i!=9){
			//						if(i!=0 && results[0]<results[i]){
			//							System.out.println("0 "+i);
			//							stable2 = false;
			//						}
			//						if(i!=9 &&results[9]<results[i]) {
			//							System.out.println("9 "+i);
			//							stable2 = false;
			//						}
			//						if(i!=8 &&results[8]<results[i]) {
			//							System.out.println("8 "+i);
			//							stable2 = false;
			//						}
			//					}
			//				}
			//
			//				//			if(!stable2){
			//				//				for(Entry<File,Integer[]> ent : setupValues.entrySet()){
			//				//					System.out.println(ent.getKey()+"/"+_query);
			//				//					System.out.println(Arrays.toString(ent.getValue()));
			//				//				}
			//				//				
			//				//			}
			//			}
			//
			//			return stable2;

		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
			return false;
		}
	}	




	public boolean analyseStable(HashSet<String>[][] set,
			HashSet<String> all,
			HashSet<String>[] base, Map<File,Integer[]> map){
		//in theory , all the URIs in smart should appear 
		boolean stable = true;
		for(int i=0;i<fixes.length;i++){
			for(String s : all){
				if(base[0].contains(s) && !set[i][0].contains(s)){
					stable = false;
				}
				if(base[1].contains(s) && !set[i][1].contains(s)){
					stable = false;
				}
			}
		}
		boolean stable2 = stable;
		if(stable){
			int [] results = new int[fixes.length];

			if(map.size() != results.length){
				System.out.println("Non matching map size for "+_queryID +" "+map.size());
				return false;
			}
			int b=0;
			
			/**
			 * check for empty results 
			 **/
			for(Integer[] ent : map.values()){
				results[b++]= ent[0];
				if(ent[0]==0){ 
					stable2=false;
				}
			}
			for( int i=0;i< fixes.length;i++){
				if(i!=2 && results[2]>results[i])
					stable2 = false;
			}
		}
		if(stable2){
			boolean nonNull= false;
			boolean print =false;
			for(Integer[] ent : map.values()){
				if(ent[0]==0 && nonNull){
					print = true;
				}else if(ent[0]!=0){
					nonNull=true;
				}
			}
			if(print){
				System.out.println("Some empty results: "+_queryID);

				//	for( int i=0;i< 10;i++){
				//		if(i!=2 && results[2]>results[i])
				//			stable2 = false;
				//		}
				//	for(Integer[] ent : map.values()){
				//		System.out.println(Arrays.toString(ent));
				//	}
			}
		}

		return stable2;
	}
	
	public boolean emptyResults() {
		for(Integer [] resVal: setupValues.values()){
			if(resVal[0]==0) return true;
		}
		return false;
	}


	private String analyse(int i, HashSet<String>[][] cur,
			HashSet<String>[][][] set, HashSet<String> all,
			HashSet<String>[] base, Map<File, Integer[]>[] confs, Map<File,Integer[]> map, int baseIdx1, int b2) {

		String s = null;
		int b=0;
		int next = i+1;
		String res = "";
		boolean stable = false;
		for(Entry<File,Integer[]> ent : confs[i].entrySet()){
			if(baseIdx1!=i || (baseIdx1==i && b2 == b)){
				cur[i]= set[i][b];
				if(cur[i]!=null){
					map.put(ent.getKey(), ent.getValue());
					if(map.size()!=next){
						System.out.println(i+" "+map.size()+" "+next);
					}
					if(i!=fixes.length-1){
						String t = analyse(next,cur,set,all,base,confs,map,baseIdx1,b2);
						if( t != null){
							res += t+""+b;
							break;
						}else{
							map.remove(ent.getKey());
							res=null;
						}
					}else{
						if(analyseStable(cur, all, base,map)){
							res =""+b;
							stable = true;
							break;
						}
						else{
							res = null;
							map.remove(ent.getKey());
						}
					}

				}
			}
			b++;
		}
		//		if(stable)
		//			System.out.println(res);

		return res;
	}


	//		//we have the same errors in every query
	//		int status = -1;
	//		boolean consistent = true;
	//		boolean inconsistentStatus = false;
	//		boolean inconsistentError = false;
	//		boolean abox = false;
	//		Integer[] prev = null;
	//		String statusCodes = "";
	//		
	//		int [] results = new int[10];
	//		int c=0;
	//		for(Entry<File,Integer[]> ent : setupValues.entrySet()){
	//			results[c++]= ent.getValue()[0];
	//			if(ent.getValue()[0]==0)return false;
	//		}
	//		
	////		base <= rest
	//		for( int i=0;i< 10;i++){
	//			if(i!=2 && results[2]>results[i])
	//				return false;
	//		}
	//		
	////		combination always better
	//		for( int i=0; i< 10;i++){
	//			if(i!=0 && results[0]<results[i]) return false;
	//			if(i!=9 &&results[9]<results[i]) return false;
	//			if(i!=8 &&results[8]<results[i]) return false;
	//		}
	//		return true;



	//		allcl
	//		base
	//		smart
	//		seealso
	//		rdfs
	//		sameAs
	//		rdfsd
	//		rdfsc
	//		all 
	//		alldir
	//		
	//		
	//		
	//		
	//		for(Entry<File,Integer[]> ent : setupValues.entrySet()){
	//			int cur = getStatus(ent.getKey());
	//			statusCodes+=""+cur;
	//			if(status == -1){ status=cur;}
	//			else {
	//				if(status != cur){
	//					//what went wrong? 
	//					if(prev[7] !=ent.getValue()[7] || prev[8]!=ent.getValue()[8]){
	//						//ok we have a different number of abox lookups
	//						inconsistentError= true;
	//						consistent=false;
	//					}
	//					else if(!inconsistentError){
	//						//same number of errors but still different status code
	//						inconsistentStatus=true;
	//						consistent=false;	
	//					}
	//					if(prev[7] !=0 || prev[8]!=0){
	//						abox=true;
	//						//we have an abox error
	//					}
	//					status=cur;
	//				}
	//			}
	//			prev = ent.getValue();
	//		}
	//		if(!consistent){
	//			
	//			if(!inconsistentError && abox){
	//				return true;
	//				//we have abox errors but still 
	//			}
	//			else{
	//				int c =0;
	//				int base = 0;
	//				for(Entry<File,Integer[]> ent : setupValues.entrySet()){
	//					//				System.out.println(ent.getValue()[0]);
	//					if(c==1)
	//						base = ent.getValue()[0];
	//					else if(c>1 && base != ent.getValue()[0]){
	//						return false;
	//
	//					}
	//				}
	//				return true;
	//			}
	//			//			System.out.println("___________");
	//
	//			//			return false;
	//
	//		}else{
	//			return true;
	//		}

	//	}

	private HashSet<String>[] getURLsSets(File qu) {
		HashSet<String>[] set = new HashSet[2];
		set[0]= new HashSet<String>();
		set[1]= new HashSet<String>();

		for(File f: qu.listFiles()){
			if(f.getName().startsWith(_queryID)){
				Scanner s;
				try {
					String fname = f.getName();
					fname = fname.substring(0,fname.indexOf(".sparql")+7);

					for(File fs: f.getParentFile().listFiles()){
						if(fs.getName().endsWith("-abox.access.log") && fs.getName().startsWith(fname)){
							s = new Scanner(fs);
							while(s.hasNextLine()){
								String []tt=s.nextLine().split(" ");
								String url = tt[3];
								String resp = tt[5];
								if(resp.startsWith("5") || resp.startsWith("6")|| resp.startsWith("4"))
									set[0].add(url);
								else if(resp.startsWith("2")||resp.startsWith("3"))
									set[1].add(url);
							}
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		return set;
	}


	/**
	 * @return
	 */
	public String csvHeader() {
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		int c =0;
		for(String s : QueryExecutionBenchmark.summaryOrder){
			if(c!=0){
				sb1.append(",").append(s);
				for(File t: setupValues.keySet()){
					sb1.append(",");
					sb2.append(",").append(t.getName());
				}
			}else
				sb1.append("Query");
			c++;
		}
		return sb1.toString()+"\n"+sb2.toString();
	}

	/**
	 * @return
	 */
	public String csvLine() {
		StringBuilder sb1 = new StringBuilder(_queryID);

		for(int i=0; i< QueryExecutionBenchmark.summaryOrder.size()-1;i++){
			for(File t: setupValues.keySet()){
				sb1.append(",").append(setupValues.get(t)[i]);
			}
		}
		return sb1.toString();
	}


	/**
	 * 
	 * @param qu
	 * @param tt
	 * @return - status code see getStatus method
	 */
//	public int analyse(File qu, String[] tt){
//		Integer [] v = new Integer[QueryExecutionBenchmark.summaryOrder.size()-1];
//		for(int i=0; i< QueryExecutionBenchmark.summaryOrder.size()-1; i++){
//			try{
//				if(tt[i+1] !=null){
//					v[i] = Integer.valueOf(tt[i+1]);
//				}
//				else if(i!=2) v[i]= -1;
//				else v[i]=0;
//			}catch(Exception e){
//				v[i] = 0;	
//			}
//		}
//		setupValues.put(qu, v);
//		int status = getStatus(qu);
//
//
//		return status;
//	}
	public int analyse(File qu, QueryExecutionBenchmark qeb) {
		Integer [] v = new Integer[QueryExecutionBenchmark.summaryOrder.size()-1];
		for(int i=0; i< QueryExecutionBenchmark.summaryOrder.size()-1; i++){
			try{
				if(qeb.getProperty(QueryExecutionBenchmark.summaryOrder.get(i+1)) != null)
					v[i] = Integer.valueOf(qeb.getProperty(QueryExecutionBenchmark.summaryOrder.get(i+1)));
				else if(i!=2) v[i]= -1;
				else v[i]=0;
			}catch(Exception e){
				v[i] = 0;	
			}
		}

		
		
		setupValues.put(qu, v);
//		int status = getStatus(qu);
//		return status;
		return 1;
	}



	/**
	 * 
	 * @param querySetup
	 * @param tt
	 * @return - status code about the current line 
	 *   0 - means everything was ok and non-empty result
	 *   1 - means everything was ok, but empty results
	 *   2 - null as result value, which indicates some error
	 *   3 - we have an unstable abox query 
	 *   4 - we have an unstable tbox query
	 *   5 - multiple failures
	 */
//	STABILITYCODE getStatus(File querySetup) {
//
//		Integer [] v = setupValues.get(querySetup);
//
////		//check the number of lookups 
////		int total = v[3];
////		boolean ignore = false;
////		for(int i=4; i<9;i++){
////			total-=v[i];
////			if(total <0 && i==8){
////				
////				//				ignore = true;
////			}
////		}
//
//		if(v[0]==0){
//			NON_EMPTY_RESULT
//		}
//		
//		int resp = 0;
//		
//			if(resp == 0){
//				resp = 1;
//			}else if(resp!=3){resp =5;}
//			error = true;
//		}	
//		if(v[0]==-1){
//			if(resp == 0 ){
//				resp = 2;
//			}else if(resp!=3){resp =5;}
//			error = true;
//		}
//
//		if(v[7] !=0 || (!ignore && v[8] !=0)){
//			if(resp == 0 || resp == 3){
//				resp = 3;
//			}else if(resp!=3){resp = 5;}
//		}
//		if(v[13] !=0 || v[14] !=0){
//			if(resp == 0 || resp ==4){
//				resp = 4;
//			}else if(resp!=3){resp =5;}
//		}
//		//		if(resp !=3 && ignore)System.out.println(resp+" "+Arrays.toString(v));
//		//		if((resp==5 || resp==3) && ignore)System.out.println(Arrays.toString(v));
//
//		return resp;
//	}


	public int entries() {
		return setupValues.size();
	}


	public boolean isStable() {
		return isStable(null,null);
	}

	public HashMap<String, Integer[]> getInconsistentURIs() {
		HashSet<String>[][] urlSetLoc = new HashSet[setupValues.size()][2];
		HashSet<String>[] all = new HashSet[2];
		all[0]= new HashSet<String>();
		all[1]= new HashSet<String>();
		int a =0;
		for(Entry<File,Integer[]> ent : setupValues.entrySet()){
			urlSetLoc[a]= getURLsSets(ent.getKey());
			all[0].addAll(urlSetLoc[a][0]);
			all[1].addAll(urlSetLoc[a][1]);
			a++;
		}
		HashMap<String, Integer[]> map = new HashMap<String, Integer[]>();
		for(String s: urlSetLoc[2][0]){
			if(map.containsKey(s)) continue;
			Integer [] inte = new Integer[urlSetLoc.length];
			for(int j =0; j < urlSetLoc.length; j++){
				if(urlSetLoc[j][1].contains(s)){
					inte[j]=1;
				}
				else if(urlSetLoc[j][0].contains(s)){
					inte[j]=0;
				}
				else 
					inte[j]=-1;
			}
			map.put(s, inte);
		}
		for(String s: urlSetLoc[2][1]){
			if(map.containsKey(s)) continue;
			Integer [] inte = new Integer[urlSetLoc.length];
			for(int j =0; j < urlSetLoc.length; j++){
				if(urlSetLoc[j][0].contains(s)){
					inte[j]=1;
				}
				else if(urlSetLoc[j][1].contains(s)){
					inte[j]=0;
				}
				else 
					inte[j]=-1;
			}
			map.put(s, inte);
		}

		return map;
	}

	public void setQuery(String query) {
		_query = query;
		
	}
	
	public String getQuery(){
		return _query;
	}



}

