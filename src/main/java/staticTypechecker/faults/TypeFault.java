package staticTypechecker.faults;

import staticTypechecker.entities.Type;

public class TypeFault implements Fault {
	private Type t1;
	private Type t2;

	private String preamble;

	public TypeFault(Type t1, Type t2, String preamble){
		this.t1 = t1;
		this.t2 = t2;
		this.preamble = preamble;
	}
	
	public String getMessage(){
		String res = this.preamble == null ? "" : this.preamble + ":\n"; // begin the fault with the preabmle if it exists
		res += this.t1.prettyString() + "\n\nis not a subtype of\n" + this.t2.prettyString();
		return res;
	}
}
