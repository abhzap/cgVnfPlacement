package ILP;

import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class IlpCstr8 {	
	TrafficNodes tn;
	Path p;
	BaseVertex vrt;
	BaseVertex nxtVrt;
	int funcID;
	int nxtFuncID;
	
	public IlpCstr8(TrafficNodes tn, Path p, BaseVertex vrt, BaseVertex nxtVrt, int funcID, int nxtFuncID){
		this.tn = tn;
		this.p = p;
		this.vrt = vrt;
		this.nxtVrt = nxtVrt;
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
			IlpCstr8 o = (IlpCstr8) obj;
			if( this.tn.equals(o.tn) && this.p.equals(o.p) && (this.vrt.get_id()==o.vrt.get_id()) && (this.nxtVrt.get_id()==o.nxtVrt.get_id()) && (this.funcID==o.funcID) && (this.nxtFuncID==o.nxtFuncID) ){
				result = true;
			}
		}
	    return result;
	}	
	
	@Override
	public int hashCode()
	{
	    return this.tn.hashCode() + this.p.hashCode() + this.vrt.hashCode() + this.nxtVrt.hashCode() +this.funcID + this.nxtFuncID;
	}
}
