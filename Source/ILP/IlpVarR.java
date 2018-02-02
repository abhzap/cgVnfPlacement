package ILP;


import edu.asu.emit.qyan.alg.model.Path;

public class IlpVarR {

	
	
	public TrafficNodes tn;
	public Path p;
	public int pathIndex;
	
	public IlpVarR(TrafficNodes tn, Path p, int pathIndex){
		this.tn = tn;
		this.p = p;
		this.pathIndex = pathIndex;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			IlpVarR o = (IlpVarR) obj;
			if( this.tn.equals(o.tn) && this.p.equals(o.p) && (this.pathIndex == o.pathIndex) ){
				result = true;
			}
		}
	    return result;
	}	
	
	@Override
	public int hashCode()
	{
	    return this.tn.hashCode() + this.p.hashCode() + this.pathIndex;
	}
	
	@Override
	public String toString(){
		return "R_s" + this.tn.v1.get_id() + "_d"  + this.tn.v2.get_id() + "_p" + this.pathIndex;
	}
	
}
