package ILP;

import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;


public class IlpCstr9 {
	public TrafficNodes tn;	
	public int funcID;
	public int nxtFuncID;
	
	public IlpCstr9(TrafficNodes tn, int funcID, int nxtFuncID){
		this.tn = tn;	
		this.funcID = funcID;
		this.nxtFuncID = nxtFuncID;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			IlpCstr9 o = (IlpCstr9) obj;
			if( this.tn.equals(o.tn)  && (this.funcID==o.funcID) && (this.nxtFuncID==o.nxtFuncID) ){
				result = true;
			}
		}
	    return result;
	}	
	
	@Override
	public int hashCode()
	{
	    return this.tn.hashCode() + this.funcID + this.nxtFuncID;
	}
}
