package ILP;

import edu.asu.emit.qyan.alg.model.Path;
import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class IlpCstr5 {
	TrafficNodes tn;
	Path p;
	BaseVertex v;
	int funcID;
	
	public IlpCstr5(TrafficNodes tn, Path p, BaseVertex v, int funcID){
		this.tn = tn;
		this.p = p;
		this.v = v;
		this.funcID = funcID;
	}

	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			IlpCstr5 o = (IlpCstr5) obj;
			if( this.tn.equals(o.tn) && this.p.equals(o.p) && (this.v.get_id()==o.v.get_id()) && (this.funcID==o.funcID) ){
				result = true;
			}
		}
	    return result;
	}	
	
	@Override
	public int hashCode()
	{
	    return this.tn.hashCode() + this.p.hashCode() + this.v.hashCode() + this.funcID;
	} 
}
