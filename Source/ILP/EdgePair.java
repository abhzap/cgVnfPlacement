package ILP;

import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class EdgePair {
     
	public BaseVertex source;
	public BaseVertex sink;
	
	
	public EdgePair(BaseVertex source, BaseVertex sink){
		this.source = source;
		this.sink = sink;		
	}	
	
	
	@Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			EdgePair o = (EdgePair) obj;
			if( this.source.get_id()==o.source.get_id() && this.sink.get_id()==o.sink.get_id() ){
				result = true;
			}
		}
	    return result;
	}	
	
	@Override
	public int hashCode()
	{
	    return this.source.get_id() + this.sink.get_id();
	}
}

