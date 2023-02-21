package staticTypechecker.utils;

import java.util.ArrayList;
import java.util.Map.Entry;

import staticTypechecker.entities.Path;
import staticTypechecker.faults.Warning;
import staticTypechecker.faults.WarningHandler;
import staticTypechecker.typeStructures.TypeChoiceStructure;
import staticTypechecker.typeStructures.TypeInlineStructure;
import staticTypechecker.typeStructures.TypeStructure;

public class Bisimulator {
	private static class Edge{
		public TypeStructure src;
		public TypeStructure tar;
		public Path srcPath;
		public Path tarPath;
		public String childName;
		public boolean used;

		public Edge(TypeStructure src, TypeStructure tar, Path srcPath, Path tarPath, String childName){
			this.src = src;
			this.tar = tar;
			this.srcPath = srcPath;
			this.tarPath = tarPath;
			this.childName = childName;
			this.used = false;
		}

		public int hashCode(){
			return this.src.hashCode() + this.tar.hashCode() + this.childName.hashCode() + (this.used ? 1 : 0 * 31);
		}

		public String toString(){
			return this.srcPath + " --> " + this.tarPath;
		}

		public boolean equals(Object other){
			if(!(other instanceof Edge)){
				return false;
			}
			
			Edge parsedOther = (Edge)other;
			// System.out.println("\nis " + this +  " equal to " + parsedOther + "?\nsrcPaths: " + this.srcPath.equals(parsedOther.srcPath) + "\ntarPaths: " + this.tarPath.equals(parsedOther.tarPath) + "\nchildnames: " + this.childName.equals(parsedOther.childName) + "\nsrc basic types: " + Bisimulator.compareBasicTypes(this.src, parsedOther.src, true) + "\ntar basic types: " + Bisimulator.compareBasicTypes(this.tar, parsedOther.tar, true));

			return 	this.srcPath.equals(parsedOther.srcPath) && 
					this.tarPath.equals(parsedOther.tarPath) && 
					this.childName.equals(parsedOther.childName) &&
					Bisimulator.compareBasicTypes(this.src, parsedOther.src, true, false) &&
					Bisimulator.compareBasicTypes(this.tar, parsedOther.tar, true, false);

			
		}
	}

	public static boolean isSubtypeOf(TypeStructure type1, TypeStructure type2){
		ArrayList<Edge> attackerEdges = new ArrayList<>();
		ArrayList<Edge> defenderEdges = new ArrayList<>();

		Bisimulator.findAllEdges(type1, attackerEdges, new Path("root"));
		Bisimulator.findAllEdges(type2, defenderEdges, new Path("root"));

		System.out.println("attacker edges: " + attackerEdges);
		System.out.println("defender edges: " + defenderEdges);

		if(attackerEdges.isEmpty() || defenderEdges.isEmpty()){ // one of them is empty, we do not need to run the algorithm
			if(!defenderEdges.isEmpty()){ // attacker is empty, and defender is not, thus it can only be a subtype, if the defender is a choice with no more edges than to its choices

				if(type2 instanceof TypeChoiceStructure && defenderEdges.size() == ((TypeChoiceStructure)type2).choices().size()){
					return Bisimulator.compareBasicTypes(type1, type2, false, true);
				}
				else{
					return false;
				}
			}
			
			return Bisimulator.compareBasicTypes(type1, type2, false, true);
		}

		// none of them is empty, continue with the algorithm
		if(defenderEdges.containsAll(attackerEdges)){
			if(defenderEdges.size() != attackerEdges.size()){
				WarningHandler.addWarning(new Warning("given type may not be compatible with expected type in all cases:\n\nExpected type:\n" + type2.prettyString() + "\n\nGiven type:\n" + type1.prettyString()));
			}
			return true;
		}

		return false;
	}

	public static boolean isEquivalent(TypeStructure type1, TypeStructure type2){
		ArrayList<Edge> attackerEdges = new ArrayList<>();
		ArrayList<Edge> defenderEdges = new ArrayList<>();

		Bisimulator.findAllEdges(type1, attackerEdges, new Path("root"));
		Bisimulator.findAllEdges(type2, defenderEdges, new Path("root"));

		System.out.println("attacker edges: " + attackerEdges);
		System.out.println("defender edges: " + defenderEdges);

		return defenderEdges.containsAll(attackerEdges) && attackerEdges.containsAll(defenderEdges);
	}

	private static void findAllEdges(TypeStructure type, ArrayList<Edge> list, Path currPath){
		if(type instanceof TypeInlineStructure){
			TypeInlineStructure parsedType = (TypeInlineStructure)type;
			
			// run through each child 
			for(Entry<String, TypeStructure> ent : parsedType.children().entrySet()){
				String childName = ent.getKey();
				TypeStructure child = ent.getValue();
				Path newPath = currPath.append(childName);
				Edge newEdge = new Edge(type, child, currPath, newPath, childName);
				
				if(!list.contains(newEdge)){ // we have not visited this child before
					list.add(newEdge);
	
					// find edges of the children
					Bisimulator.findAllEdges(child, list, newPath);
				}
			}
		}
		else{
			TypeChoiceStructure parsedType = (TypeChoiceStructure)type;

			// run through each child of each choice 
			for(int i = 0; i < parsedType.choices().size(); i++){
				TypeInlineStructure choice = parsedType.choices().get(i);
				Path newPath = currPath.append("choice");
				Edge newEdge = new Edge(type, choice, currPath, newPath, "");
	
				if(!list.contains(newEdge)){ // we have not visited this choice before
					list.add(newEdge);
	
					Bisimulator.findAllEdges(choice, list, newPath);
				}
			}
		}
	}

	/**
	 * 
	 * @param type1
	 * @param type2
	 * @param allChoices if both type1 and type2 are choice types, should all choices of type1 be present in type2 or not?
	 * @return
	 */
	private static boolean compareBasicTypes(TypeStructure type1, TypeStructure type2, boolean allChoices, boolean showWarning){
		if(type1 instanceof TypeInlineStructure){
			if(type2 instanceof TypeInlineStructure){
				return Bisimulator.compareBasicTypes((TypeInlineStructure)type1, (TypeInlineStructure)type2);
			}
			else{
				return Bisimulator.compareBasicTypes((TypeInlineStructure)type1, (TypeChoiceStructure)type2);
			}
		}
		else{
			if(type2 instanceof TypeInlineStructure){
				return Bisimulator.compareBasicTypes((TypeChoiceStructure)type1, (TypeInlineStructure)type2);

			}
			else{
				return Bisimulator.compareBasicTypes((TypeChoiceStructure)type1, (TypeChoiceStructure)type2, allChoices, showWarning);
			}
		}
	}

	private static boolean compareBasicTypes(TypeInlineStructure type1, TypeInlineStructure type2){
		return type1.basicType().equals(type2.basicType());
	}

	private static boolean compareBasicTypes(TypeInlineStructure type1, TypeChoiceStructure type2){
		boolean type1IsAChoice = false;

		for(TypeInlineStructure choice : type2.choices()){
			if(type1.basicType().equals(choice.basicType())){
				type1IsAChoice = true;
				break;
			}
		}
		
		return type1IsAChoice;
	}

	private static boolean compareBasicTypes(TypeChoiceStructure type1, TypeInlineStructure type2){
		boolean compatible = Bisimulator.compareBasicTypes(type2, type1);
		WarningHandler.addWarning(new Warning("given type may not be compatible with expected type in all cases:\n\nExpected type:\n" + type2.prettyString() + "\n\nGiven type:\n" + type1.prettyString()));
		return compatible;
	}

	private static boolean compareBasicTypes(TypeChoiceStructure type1, TypeChoiceStructure type2, boolean allChoicesMustBePresent, boolean showWarning){
		boolean allPresent = true; // if all choices of type1 are present
		boolean anyPresent = false; // if any choice of type1 is present
		
		for(TypeInlineStructure choice1 : type1.choices()){
			boolean choice1IsPresent = false;

			for(TypeInlineStructure choice2 : type2.choices()){
				if(choice1.basicType().equals(choice2.basicType())){
					choice1IsPresent = true;
					break;
				}
			}

			if(choice1IsPresent){
				anyPresent = true;
			}
			else{
				allPresent = false;
			}
		}

		if(showWarning && type1.choices().size() != type2.choices().size()){
			WarningHandler.addWarning(new Warning("given type may not be compatible with expected type in all cases:\n\nExpected type:\n" + type2.prettyString() + "\n\nGiven type:\n" + type1.prettyString()));
		}
		
		if(allChoicesMustBePresent){
			return allPresent;
		}

		return anyPresent;
	}
}
