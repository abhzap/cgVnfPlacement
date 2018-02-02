package ILP;

import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class IlpCstr7 {
	public TrafficNodes tn;
	public Path p;
	public BaseVertex v;
	public int funcID;
	public int nxtFuncID;
	
	public IlpCstr7(TrafficNodes tn, Path p, BaseVertex v, int funcID, int nxtFuncID){
		this.tn = tn;
		this.p = p;
		this.v = v;
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
			IlpCstr7 o = (IlpCstr7) obj;
			if( this.tn.equals(o.tn) && this.p.equals(o.p) && (this.v.get_id()==o.v.get_id()) && (this.funcID==o.funcID) && (this.nxtFuncID==o.nxtFuncID) ){
				result = true;
			}
		}
	    return result;
	}	
	
	@Override
	public int hashCode()
	{
	    return this.tn.hashCode() + this.p.hashCode() + this.v.hashCode() + this.funcID + this.nxtFuncID;
	}
}
