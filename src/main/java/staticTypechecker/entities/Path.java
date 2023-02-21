package staticTypechecker.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jolie.lang.parse.ast.OLSyntaxNode;
import jolie.util.Pair;

public class Path {
	private ArrayList<String> pathElems;

	public Path(String path){
		this.pathElems = new ArrayList<>();
		String[] elems = path.split("\\.");

		for(String pathElem : elems){
			this.pathElems.add(pathElem);
		}
	}

	public Path(List<Pair<OLSyntaxNode, OLSyntaxNode>> path){
		this.pathElems = new ArrayList<>();
		path.forEach(pair -> this.pathElems.add(pair.key().toString()));
	}

	public Path(ArrayList<String> path){
		this.pathElems = path;
	}

	public Path(Path other){
		this.pathElems = new ArrayList<>(other.path());
	}

	public Path append(String next){
		Path tmp = new Path(this);
		tmp.pathElems.add(next);
		return tmp;
	}

	/**
	 * Retrieves the path element at the specified index. Note: negative indices are allowed, they will simply start from the back
	 * @param index the index
	 * @return the path element at the specified index
	 */
	public String get(int index){
		int i = (this.pathElems.size() + index) % this.pathElems.size();
		return this.pathElems.get(i);
	}

	public ArrayList<String> path(){
		return this.pathElems;
	}

	public boolean isEmpty(){
		return this.pathElems.isEmpty();
	}

	public int size(){
		return this.pathElems.size();
	}

	public Path subPath(int start, int end){
		return new Path((ArrayList<String>)this.pathElems.subList(start, end));
	}

	/**
	 * @return the remaining of this Path when the first element has been removed. If this Path is empty, return itself.
	 */
	public Path remainder(){
		if(this.isEmpty()){
			return this;
		}
		return new Path( new ArrayList<String>(this.pathElems.subList(1, this.size())) );
	}

	public String toString(){
		return this.pathElems.stream().collect(Collectors.joining("."));
	}

	public int hashCode(){
		return this.pathElems.hashCode();
	}

	public boolean equals(Object other){
		if(!(other instanceof Path)){
			return false;
		}

		Path parsedOther = (Path)other;

		return this.pathElems.equals(parsedOther.pathElems);
	}
}
