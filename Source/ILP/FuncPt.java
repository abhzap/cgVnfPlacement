package ILP;

public class FuncPt{
	private int func_id;   
    private double core_count;  
    
    public FuncPt(int func_id, double core_count){
    	this.func_id = func_id;
    	this.core_count = core_count;    	
    }    
   
    public int getid(){
    	return this.func_id;
    }
    
    public double getcore(){
    	return this.core_count;
    }
}
