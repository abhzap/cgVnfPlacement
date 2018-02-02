package ILP;

import edu.asu.emit.qyan.alg.model.Path;

public class IlpCstr11 {
	TrafficNodes tn;
	Path p;	
	int funcID;
	int nxtFuncID;	
	
	public IlpCstr11(TrafficNodes tn, Path p, int funcID, int nxtFuncID){
		this.tn = tn;
		this.p = p;		
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
			IlpCstr11 o = (IlpCstr11) obj;
			if( this.tn.equals(o.tn) && this.p.equals(o.p) && (this.funcID==o.funcID) && (this.nxtFuncID==o.nxtFuncID) ){
				result = true;
			}
		}
	    return result;
	}	
	
	@Override
	public int hashCode()
	{
	    return this.tn.hashCode() + this.p.hashCode() + this.funcID + this.nxtFuncID;
	}
}
