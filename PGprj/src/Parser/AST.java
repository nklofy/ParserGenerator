package Parser;
import java.util.*;

//import Interpreter.*;
//import Parser.TypeSys.*;

public class AST {
	private int line;
	private String ast_type;
	private AST ast_deMrg;	//de-merge, choose correct one
	HashSet<AST> merged_asts;
	private boolean isMerged=false;
	//HashMap<String,R_Variable> var_table=new HashMap<String,R_Variable>();
	//HashMap<String,T_Type> type_table=new HashMap<String,T_Type>();
	//HashMap<String,R_Function> func_table = new HashMap<String,R_Function>();
	protected int scope=1;
	public int getScope() {
		return scope;
	}
	public void setScope(int scope) {
		this.scope = scope;
	}
	public String getASTType() {
		return ast_type;
	}
	public void setASTType(String type) {
		String[] ss=type.split("\\.");
		this.ast_type = ss[ss.length-1];
	}
	/*	public boolean genCode(CodeGenerator codegen) throws GenCodeException{
		return true;
	}
	public boolean genSymTb(CodeGenerator codegen) throws GenSymTblException{
		return true;
	}
	public boolean checkType(CodeGenerator codegen) throws TypeCheckException{
		return true;
	}
	HashMap<String, R_Variable> getVarTb() {
		return var_table;
	}
	void setVarTb(HashMap<String, R_Variable> t) {
		this.var_table=t;
	}
	void putVarTb(String name, R_Variable r) {
		if(this.var_table != null){
			this.var_table.put(name, r);
		}else{
			this.var_table=new HashMap<String, R_Variable>();
			this.var_table.put(name, r);
		}
	}
	HashMap<String, T_Type> getTypeTb() {
		return type_table;
	}
	void setTypeTb(HashMap<String, T_Type> t) {
		this.type_table=t;
	}
	void putTypeTb(String name, T_Type r) {
		if(this.type_table != null){
			this.type_table.put(name, r);
		}else{
			this.type_table=new HashMap<String, T_Type>();
			this.type_table.put(name, r);
		}
	}
	HashMap<String, R_Function> getFuncTb() {
		return func_table;
	}
	void setFuncTb(HashMap<String, R_Function> t) {
		this.func_table=t;
	}
	void putFuncTb(String name, R_Function r) {
		if(this.func_table != null){
			if(this.func_table.containsKey(name)){
				if(this.func_table.get(name).isMulti()){
					this.func_table.get(name).addFuncR(r);
				}else{
					this.func_table.get(name).setMulti();
					this.func_table.get(name).addFuncR(r);
				}
			}else{
				this.func_table.put(name, r);
			}			
		}else{
			this.func_table=new HashMap<String, R_Function>();
			this.func_table.put(name, r);
		}
	}*/
	public boolean isMerged() {
		return isMerged;
	}
	public void setMerged() {
		this.isMerged = true;
	}
//	public AST getDeMrg(CodeGenerator codegen) {
		//de-merge and return 
//		return ast_deMrg;
//	}
	public HashSet<AST> getMergedAsts() {
		return merged_asts;
	}
	public void setMergedAsts(HashSet<AST> merged_asts) {
		this.merged_asts = merged_asts;
	}
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
}