//Parser Generator
//before running, you'd better to check all tokens and symbols matching the rules

package Parser;

import java.util.*;
import java.io.*;

public class ParserGenerator {	
	
	private ArrayList<PGGrammar> gen_PGGrammars=new ArrayList<PGGrammar>();
	private Map<String,PGSymbol> gen_symbols=new HashMap<String,PGSymbol>();
	private Set<PGSymbol> gen_tokens=new HashSet<PGSymbol>();
	private Set<PGSymbol> gen_NTs=new HashSet<PGSymbol>();
	private ArrayList<PGGrammarRule> gen_gr_tables=new ArrayList<PGGrammarRule>();
	private ArrayList<CC> gen_CCs=new ArrayList<CC>();
	private LinkedList<CC> cc_list=new LinkedList<CC>();
	private Map<PGSymbol,HashSet<Item>> gen_items=new HashMap<PGSymbol,HashSet<Item>>();
	private ArrayList<ActionTable> gen_action_tables=new ArrayList<ActionTable>();
	private ArrayList<String> sr_conflicts=new ArrayList<String>();
	private ArrayList<String> rr_conflicts=new ArrayList<String>();
	
	static public void main(String[] args){
		
		ParserGenerator pg=new ParserGenerator(); System.out.println("start");
		pg.input("."+File.separator+"src"+File.separator+"Parser"+File.separator+"PGGrammar.txt");       
		pg.generatePGGrammarTable();     System.out.println("read PGGrammar");
		pg.getFirst();                 
		pg.getFollow();                System.out.println("first and follow");
		pg.getCCs();                   System.out.println("CCs");
		pg.generateActionTable();	   System.out.println("action table");
		pg.outCC("."+File.separator+"src"+File.separator+"Parser"+File.separator+"out_cc.txt");        
		pg.output("out_PGGrammar.txt");  System.out.println("output");System.out.println("end");
		
	}
		
	private boolean input(String filename) {
		Scanner in = null;
		String word;
		try {			
			in=new Scanner(new BufferedReader(new FileReader(filename)));
			//in=new Scanner(new FileReader(filename));
			if(in.hasNext())
				word=in.next();
			else return false;
			if(word.equals("//symbols")){
				word=in.next();
				while(!word.equals("//tokens")){
					PGSymbol symbol=new PGSymbol(word,false);
					if(gen_symbols.containsKey(word)){
						System.out.println("existing symbol "+symbol.name);
					}
					gen_symbols.put(word,symbol);		//a table recording all symbols
					gen_NTs.add(symbol);				//non terminal
					word=in.next();
				}
			}else return false;
			if(word.equals("//tokens")){
				word=in.next();
				while(!word.equals("//PGGrammars")){					
					PGSymbol symbol=new PGSymbol(word,true);
					if(gen_symbols.containsKey(word)){
						System.out.println("existing symbol "+symbol.name);
					}
					gen_symbols.put(word,symbol);		//symbols table
					gen_tokens.add(symbol);				//token, terminal
					if(gen_NTs.contains(symbol))
						System.out.println("token in NT "+ symbol.name);
					word=in.next();
				}
				gen_NTs.removeAll(gen_tokens);
			}else return false;
			if(word.equals("//PGGrammars")){
				inputPGGrammars(in);		//record PGGrammar in table
				return true;
			}else return false;					
		} catch (FileNotFoundException e) {
			e.printStackTrace();			
		}finally{
			in.close();
		}
		return false;
	}
	
	private boolean inputPGGrammars(Scanner in){
		String word=null;
		String head=null;
		LinkedList<String> list=new LinkedList<String>();
		while(in.hasNext()){
			word=in.next();
			if(word.equals("//end")){
				gen_PGGrammars.add(buildPGGrammar(list));
				in.close();
				return true;
			}
			if(word.equals("|>")){
				head=list.removeLast();				
				if(!list.isEmpty())
					gen_PGGrammars.add(buildPGGrammar(list));
				list.addLast(head);
			}else								//if(word.equals("->"))
				list.addLast(word);
			
		}
		return false;
	}
	
	private PGGrammar buildPGGrammar(LinkedList<String> list){
		PGGrammar PGGrammar = new PGGrammar();
		Production production = new Production();
		String word=list.removeFirst();
		PGSymbol head = gen_symbols.get(word);
		if(head==null){
			System.out.println("null head "+word);
		}
		PGGrammar.head=head;
		PGGrammar.productions.add(production);
		while(!list.isEmpty()){
			word=list.removeFirst();
			if(word.equals(">|")){
				production=new Production();
				PGGrammar.productions.add(production);
				continue;
			}
			PGSymbol s_t=gen_symbols.get(word);
			if(s_t==null){
				System.out.println("null symbol "+word);
			}
			production.symbols.add(s_t);						
		}
		head.head_PGGrammar.addAll(PGGrammar.productions);
		return PGGrammar;
	}

	private void generatePGGrammarTable(){		//generate PGGrammar-table from input PGGrammars
		for(PGGrammar grr:gen_PGGrammars){
			for(Production prd:grr.productions){
				PGGrammarRule grt=new PGGrammarRule();
				grt.head=grr.head.name;
				for(PGSymbol sym:prd.symbols){					
					grt.symbols.add(sym.name);
				}
				gen_gr_tables.add(grt);
				prd.index_gr_tb=gen_gr_tables.size()-1;
			}
		}
	}
	
	private boolean getFirst(){	//first token set of each symbol
		PGSymbol sym_e=gen_symbols.get("e");
		for(PGSymbol sym : gen_tokens){
			sym.First.add(sym);
		}
		int count=gen_PGGrammars.size();
		boolean loop=true;
		while(loop){
			loop=false;
			for(int i=count-1;i>=0;i--){
				PGGrammar PGGrammar=gen_PGGrammars.get(i);
				PGSymbol head=PGGrammar.head;
				for(Production production : PGGrammar.productions){
					ArrayList<PGSymbol> symbols=production.symbols;
					Set<PGSymbol> first_set=new HashSet<PGSymbol>();
					int count1=symbols.size();
					for(int j=0;j<count1;j++){
						PGSymbol sym=symbols.get(j);				
						first_set.addAll(sym.First);
						if(j==count1-1&&sym.First.contains(sym_e)){
							break;
						}
						if(sym.First.contains(sym_e)){
							continue;
						}else{
							break;
						}						
					}//for(int j=0;j<count1;j++)
					if(!head.First.containsAll(first_set)){
						head.First.addAll(first_set);
						loop=true;
					}
				}//for(Production production : PGGrammar.productions)				
			}//for(int i=count-1;i>=0;i--)
		}//while(loop)
		return true;
	}
	
	private boolean getFollow(){	//follow token set of each symbol, but not used currently.
		PGSymbol sym_e=gen_symbols.get("e");
		PGSymbol sym_eof=gen_symbols.get("eof");
		gen_PGGrammars.get(0).head.Follow.add(sym_eof);
		int count=gen_PGGrammars.size();
		boolean loop=true;
		while(loop){
			loop=false;
			for(int i=count-1;i>=0;i--){
				PGGrammar PGGrammar=gen_PGGrammars.get(i);
				PGSymbol head=PGGrammar.head;
				for(Production production : PGGrammar.productions){
					Set<PGSymbol> follow_set=new HashSet<PGSymbol>();
					follow_set.addAll(head.Follow);
					ArrayList<PGSymbol> symbols=production.symbols;
					int count1=symbols.size();
					for(int j=count1-1;j>=0;j--){
						PGSymbol sym=symbols.get(j);
						if(!sym.isFinal){
							if(!sym.Follow.containsAll(follow_set)){
								sym.Follow.addAll(follow_set);
								loop=true;
							}
							if(sym.First.contains(sym_e)){
								follow_set.addAll(sym.First);
								follow_set.remove(sym_e);
							}else{
								follow_set.clear();
								follow_set.addAll(sym.First);
							}	
						}//if(!sym.isFinal)
						else{
							follow_set.clear();
							follow_set.add(sym);
						}
					}//for(int j=count1-1;j>=0;j--)
				}
			}
		}//while(loop)
		return true;
	}
	private boolean addItemTable(Item item){
		if(!gen_items.containsKey(item.head)){
			gen_items.put(item.head, new HashSet<Item>());
		}
		return gen_items.get(item.head).add(item);
	}
	private boolean getClosure(CC cc){ 		//items closure of the canonical collection				
		PGSymbol sym_e=gen_symbols.get("e");
		ArrayList<Item> cl_items=cc.items;		//items in closure 
		int index_cl=0;
		while(true){
			if(index_cl>=cl_items.size())
				break;
			Item k_item=cl_items.get(index_cl++);
			ArrayList<PGSymbol> symbols=k_item.symbols;
			int position=k_item.position;
			if(position==symbols.size()){//position is at end of item, then reduce				
				if(cc.item_reduce.contains(k_item)){
					cc.token_reduce.addAll(k_item.look_ahead);	
					continue;
				}else{
					cc.item_reduce.add(k_item);
					cc.token_reduce.addAll(k_item.look_ahead);
					if(cc.is_reduce){
						rr_conflicts.add("rr "+cc.index_cc+" "+k_item.head.name);
					}else{
						cc.is_reduce=true;
					}					
					continue;
				}
			}
			PGSymbol sym_head=symbols.get(position);
			HashSet<PGSymbol> follow_set=new HashSet<PGSymbol>();			//get follow_set of the symbol sym_head
			int rest_count=symbols.size()-position;
			if(rest_count>1){
				int i=1;
				follow_set.addAll(symbols.get(position+i).First);
				while(follow_set.contains(sym_e)){
					follow_set.remove(sym_e);
					if(++i>=rest_count){
						follow_set.addAll(k_item.look_ahead);
						break;
					}
					follow_set.addAll(symbols.get(position+i).First);
				}//while(follow_set.contains(sym_e))
			}//if(rest_count>0)
			else{
				follow_set.addAll(k_item.look_ahead);			//get follow_set
			}
						
			//look for a item with sym as head
			for(Production production:sym_head.head_PGGrammar){
				boolean need_new=true;
				for(Item item_in:cl_items){//check if item existed
					if(item_in.index_gr_tb==production.index_gr_tb && item_in.position==0){
						item_in.look_ahead.addAll(follow_set);
						need_new=false;
						break;
					}
				}
				if(need_new){
					Item item_1=new Item(sym_head, production.symbols, 0, follow_set, production.index_gr_tb);
					cl_items.add(item_1);
					addItemTable(item_1);
					item_1.cc_in=cc;
				}
			}
		}
		//cc.items.addAll(cl_items);
		return true;
	}
	
	private CC getGoto(Item item){
		Item kr_item=new Item(item);
		kr_item.position++;
		HashSet<Item> its=gen_items.get(kr_item.head);//find if core is already exist
		if(its!=null){
			for(Item it:its){
				if(kr_item.eqItem(it)){	
					return it.cc_in;
				}
			}
		}
		CC cc =new CC();
		cc.items.add(kr_item);
		gen_CCs.add(cc);
		cc_list.add(cc);
		cc.in_table=true;
		cc.index_cc=gen_CCs.size()-1;
		kr_item.cc_in=cc;
		addItemTable(kr_item);
		return cc;
	}
	
	private boolean getCCs(){//from first cc spread all others and get their token-action map 
		PGGrammar PGGrammar0=gen_PGGrammars.get(0);
		CC cc0=new CC();
		Item item0=new Item();
		item0.head=PGGrammar0.head;
		item0.symbols=PGGrammar0.productions.get(0).symbols;
		item0.position=0;
		item0.cc_in=cc0;
		item0.look_ahead.add(gen_symbols.get("eof"));
		item0.index_gr_tb=PGGrammar0.productions.get(0).index_gr_tb;
		cc0.items.add(item0);
		cc0.index_cc=0;
		gen_CCs.add(cc0);		
		cc_list.add(cc0);
		cc0.in_table=true;
		getClosure(cc0);
		while(!cc_list.isEmpty()){			
			CC cc=cc_list.removeFirst();
			if(cc.got_Goto){
				//continue;
			}
			cc.got_Goto=true;
			
			for(Item item:cc.items){
				if(item.position==item.symbols.size()){
					continue;
				}
				PGSymbol sym=item.symbols.get(item.position);
				if(cc.goto_tb.containsKey(sym)){
					Item item_tmp=new Item(item);
					item_tmp.position++;
					CC cc_tmp=cc.goto_tb.get(sym);
					boolean isInCC=false;
					for(Item item1:cc_tmp.items){
						if(item_tmp.inItem(item1)){//find if item in cc
							item1.look_ahead.addAll(item_tmp.look_ahead);
							isInCC=true;
							break;
						}
					}
					if(!isInCC){//not in cc, add it
						cc_tmp.items.add(item_tmp);
						addItemTable(item_tmp);
						item_tmp.cc_in=cc_tmp;
						cc_list.add(cc_tmp);
					}
				}else{
					CC new_cc=getGoto(item);
					cc.goto_tb.put(sym, new_cc);
				}
				//System.out.println(cc.index_cc+" "+sym.name+" "+cc.goto_tb.get(sym).index_cc);
			}//for(Item item:cc.items)
			for(PGSymbol smb:cc.goto_tb.keySet()){
				CC cc1=cc.goto_tb.get(smb);
				getClosure(cc1);
			}
		}
		return true;
	}

	private void generateActionTable(){		//action table and goto table
		for(CC cc:gen_CCs){
			ActionTable table=new ActionTable();
			gen_action_tables.add(table);
			for(PGSymbol token:gen_tokens){
				createActionTable(table,cc,token); //action table
			}
			for(PGSymbol sym:gen_NTs){
				createGotoTable(table,cc,sym);  //goto table
			}
		}
		gen_action_tables.get(0).goto_t.put("Goal", "g0");
	}

	private void createActionTable(ActionTable table,CC cc,PGSymbol token){//action table
		String key=token.name;
		String value="";
		if(cc.goto_tb.containsKey(token)){
			int obj_cc=cc.goto_tb.get(token).index_cc;
			value="s"+obj_cc;
			if(cc.token_reduce.contains(token)){
				sr_conflicts.add("sr "+cc.index_cc+" token "+token.name);
			}
		}
		if(cc.is_reduce && cc.token_reduce.contains(token)){
			Set<Item> its_gr=cc.item_reduce;
			for(Item it:its_gr){
				value=value+"r"+it.index_gr_tb;
			}			
		}
		if(!(cc.goto_tb.containsKey(token) || cc.token_reduce.contains(token))){
			value="/";
		}
		table.action_t.put(key, value);
	}

	private void createGotoTable(ActionTable table,CC cc,PGSymbol sym){//goto table
		String key=sym.name;
		String value="";
		if(cc.goto_tb.containsKey(sym)){
			int obj_cc=cc.goto_tb.get(sym).index_cc;
			value="g"+obj_cc;	
		}else{
			value="/";
		}		
		table.goto_t.put(key, value);
	}
	
	private void output(String filename){
		PrintWriter out=null;
		try {
			out=new PrintWriter(new BufferedWriter(new FileWriter(filename)));
			String line="";
			out.println("//tokens");
			for(PGSymbol sym:gen_tokens){
				line=line+sym.name+" ";
			}
			out.println(line);
			line="";out.println();
			out.println("//symbols");
			for(PGSymbol sym:gen_NTs){
				line=line+sym.name+" ";
			}
			out.println(line);
			line="";
			out.println();
			out.println("//PGGrammars");
			int i=0;
			for(PGGrammarRule table:gen_gr_tables){
				line="";
				for(String str:table.symbols){
					line=line+str+" ";
				}
				out.println(i+" "+table.head+" "+line);
				i++;
			}
			line="";
			out.println();
			out.println("//actions");
			i=0;
			for(ActionTable table:gen_action_tables){				
				for(PGSymbol token:gen_tokens){
					line=line+table.action_t.get(token.name)+" ";
				}
				out.println(i+" "+line);
				line="";
				
				i++;
			}
			line="";
			out.println();
			out.println("//gotos");
			i=0;
			for(ActionTable table:gen_action_tables){				
				
				for(PGSymbol sym:gen_NTs){
					line=line+table.goto_t.get(sym.name)+" ";	
				}
				out.println(i+" "+line);
				line="";
				i++;
			}
			out.println();
			out.println("//end");
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			out.close();
		}
	}
	
	private void outCC(String filename){
		PrintWriter out=null;
		try {
			out=new PrintWriter(new BufferedWriter(new FileWriter(filename)));
			String line="";
			out.println("//first set");out.println();
			//print all first set
			for(PGSymbol sym:gen_NTs){
				line=sym.name+" :";				
				out.println(line);
				line="";
				for(PGSymbol fs:sym.First){
					line=line+fs.name+" ";
				}
				out.println(line);
				if(sym.First.isEmpty()){
					line="error empty firset set of "+sym.name;
					System.out.println(line);
				}
			}
			out.println();out.println("//shift-reduece conflicts");
			for(String rsline:sr_conflicts){
				out.println(rsline);
			}
			out.println();out.println("//reduce-reduece conflicts");
			for(String rrline:rr_conflicts){
				out.println(rrline);
			}
			out.println();out.println("//all CCs");out.println();
			//print all CCs
			for(CC cc:gen_CCs){
				line=cc.index_cc+" : ";	out.println(line);
				if(cc.is_reduce){					
					line="reduce: "; out.println(line);
					for(Item it_r:cc.item_reduce){
						line=it_r.index_gr_tb+" "+it_r.head.name;
						out.println(line);
					}
				}
				line="items: "; out.println(line);
				for(Item item:cc.items){
					line=item.head.name+" ::= ";
					for(PGSymbol sym:item.symbols){
						line=line+sym.name+" ";
					}
					line=line+"    p: "+String.valueOf(item.position)+"    lok: ";					
					for(PGSymbol sym:item.look_ahead){
						line=line+sym.name+" ";
					}
					out.println(line);
				}
				out.println();
			}
			out.println("//end");
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			out.close();
		}
	}		
}

class PGSymbol{
	PGSymbol(){}
	PGSymbol(String name,boolean isFinal){
		this.name=name;
		this.isFinal=isFinal;
	}
	Set<PGSymbol> First=new HashSet<PGSymbol>();
	Set<PGSymbol> Follow=new HashSet<PGSymbol>();
	ArrayList<Production> head_PGGrammar=new ArrayList<Production>();
	Boolean isFinal;
	String name;
}

class PGGrammar{
	PGGrammar(){}
	PGGrammar(PGSymbol head, ArrayList<Production> productions){}
	PGSymbol head;
	ArrayList<Production> productions=new ArrayList<Production>();
}

class Production{
	ArrayList<PGSymbol> symbols = new ArrayList<PGSymbol>();
	int index_gr_tb;
}

class Item{
	PGSymbol head;
	ArrayList<PGSymbol> symbols=new ArrayList<PGSymbol>();
	int position;
	HashSet<PGSymbol> look_ahead=new HashSet<PGSymbol>();
	int index_gr_tb;
	CC cc_in;
	Item(){}
	Item(PGSymbol head,ArrayList<PGSymbol> symbols, int position, HashSet<PGSymbol> look_ahead, int index_gr_tb){
		this.head=head;
		this.symbols.addAll(symbols);
		this.position=position;
		this.look_ahead.addAll(look_ahead);
		this.index_gr_tb=index_gr_tb;
	}
	Item(Item old){
		this.head=old.head;
		this.symbols.addAll(old.symbols);
		this.position=old.position;
		this.look_ahead.addAll(old.look_ahead);
		this.index_gr_tb=old.index_gr_tb;
	}
	boolean eqItem(Item item){
		if(!this.head.equals(item.head))
			return false;
		if(this.position!=item.position)
			return false;
		if(this.symbols.size()!=item.symbols.size())
			return false;
		for(int i=0;i<this.symbols.size();i++){
			if(this.symbols.get(i)!=item.symbols.get(i))
				return false;
		}
		if(this.look_ahead.containsAll(item.look_ahead) && item.look_ahead.containsAll(this.look_ahead) )
			return true;
		return false;
	}
	boolean inItem(Item item){
		if(!this.head.equals(item.head))
			return false;
		if(this.position!=item.position)
			return false;
		if(this.symbols.size()!=item.symbols.size())
			return false;
		for(int i=0;i<this.symbols.size();i++){
			if(this.symbols.get(i)!=item.symbols.get(i))
				return false;
		}
		return true;
	}
}

class CC{//need to edit as look_ahead is wrong
	//Item core_item;
	ArrayList<Item> items=new ArrayList<Item>();
	HashMap<PGSymbol,CC> goto_tb=new HashMap<PGSymbol,CC>();
	boolean got_Closure=false;
	boolean got_Goto=false;
	boolean in_table=false;
	boolean is_reduce=false;
	int index_cc=-1;
	//int index_gr=-1;
	HashSet<PGSymbol> token_reduce=new HashSet<PGSymbol>();
	HashSet<Item> item_reduce=new HashSet<Item>();
}

class ActionTable{
	Map<String, String> action_t=new HashMap<String,String>();
	Map<String, String> goto_t=new HashMap<String,String>();
}

class PGGrammarRule{
	String head;
	ArrayList<String> symbols=new ArrayList<String>();
}
