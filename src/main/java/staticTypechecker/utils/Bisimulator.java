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

		public Edge(TypeStructure src, TypeStructure tar, Path srcPath, Path tarPath, String childName){
			this.src = src;
			this.tar = tar;
			this.srcPath = srcPath;
			this.tarPath = tarPath;
			this.childName = childName;
		}

		public int hashCode(){
			return this.src.hashCode() + this.tar.hashCode() + this.childName.hashCode();
		}

		public String toString(){
			return this.srcPath + " --> " + this.tarPath;
		}

		public boolean equals(Object other){
			if(!(other instanceof Edge)){
				return false;
			}
			
			Edge parsedOther = (Edge)other;
			
			// TODO: figure out a way to compare to edges
			return this.src == parsedOther.src && this.tar == parsedOther.tar && this.childName.equals(parsedOther.childName);
		}
	}

	/**
	 * NOTE DOES NOT WORK YET, TALK TO MARCO ABOUT SUBTYPING
	 * @param type1
	 * @param type2
	 * @return
	 */
	public static boolean isSubtypeOf(TypeStructure type1, TypeStructure type2){
		ArrayList<Edge> attackerEdges = new ArrayList<>();
		ArrayList<Edge> defenderEdges = new ArrayList<>();

		Bisimulator.findAllEdges(type1, attackerEdges, new Path("root"));
		Bisimulator.findAllEdges(type2, defenderEdges, new Path("root"));

		System.out.println("attacker edges: " + attackerEdges);
		System.out.println("defender edges: " + defenderEdges);

		if(attackerEdges.isEmpty() || defenderEdges.isEmpty()){ // one of them is empty, we do not need to run the algorithm
			if(!defenderEdges.isEmpty()){ // attacker is empty, and defender is not, thus it can only be a subtype, if the defender is a choice with each choice having no children. This we can check by comparing the number of defender edges and number of choices

				if(type2 instanceof TypeChoiceStructure && defenderEdges.size() == ((TypeChoiceStructure)type2).choices().size()){
					return Bisimulator.isBasicTypeSubtypeOf(type1, type2);
				}
				else{
					return false;
				}
			}
			
			return Bisimulator.isBasicTypeSubtypeOf(type1, type2);
		}

		// none of them is empty, continue with the algorithm
		if(defenderEdges.containsAll(attackerEdges)){
			if(defenderEdges.size() < attackerEdges.size()){
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

		return attackerEdges.size() == defenderEdges.size() && attackerEdges.containsAll(defenderEdges);
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
				
				// System.out.println("new edge: "  + newEdge);
				// System.out.println("old edges: " + list);

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

	// UTILS FOR SUBTYPING
	private static boolean isBasicTypeSubtypeOf(TypeStructure type1, TypeStructure type2){
		if(type1 instanceof TypeInlineStructure && type2 instanceof TypeInlineStructure){ // both are inline
			return ((TypeInlineStructure)type1).basicType().checkBasicTypeEqualness(((TypeInlineStructure)type2).basicType());
		}
		else if(type1 instanceof TypeInlineStructure && type2 instanceof TypeChoiceStructure){
			return Bisimulator.containsChoices((TypeChoiceStructure)type2,(TypeInlineStructure)type1);
		}
		else if(type1 instanceof TypeChoiceStructure && type2 instanceof TypeInlineStructure){
			return Bisimulator.containsChoices((TypeChoiceStructure)type1, (TypeInlineStructure)type2);
		}
		else{ // both are choice types
			return Bisimulator.containsChoices((TypeChoiceStructure)type1, (TypeChoiceStructure)type2);
		}
	}

	private static boolean containsChoices(TypeChoiceStructure type1, TypeInlineStructure type2){
		for(TypeInlineStructure choice : type1.choices()){
			if(choice.basicType().checkBasicTypeEqualness(type2.basicType())){
				return true;
			}
		}
		return false;
	}

	private static boolean containsChoices(TypeChoiceStructure type1, TypeChoiceStructure type2){
		for(TypeInlineStructure choice1 : type1.choices()){
			boolean foundChoice = false;

			for(TypeInlineStructure choice2 : type2.choices()){
				if(choice1.basicType().checkBasicTypeEqualness(choice2.basicType())){
					foundChoice = true;
					break;
				}
			}

			if(!foundChoice){
				return false;
			}
		}

		return true;
	}


	// UTILS FOR EQUIVALENCE
	private static boolean isBasicTypeEqual(TypeStructure type1, TypeStructure type2){
		if(!type1.getClass().isInstance(type2)){ // not same type
			return false;
		}

		if(type1 instanceof TypeInlineStructure){
			return ((TypeInlineStructure)type1).basicType().checkBasicTypeEqualness(((TypeInlineStructure)type2).basicType());
		}
		else{
			return Bisimulator.choicesAreEqual((TypeChoiceStructure)type1, (TypeChoiceStructure)type2);
		}
	}

	private static boolean choicesAreEqual(TypeChoiceStructure type1, TypeChoiceStructure type2){
		if(type1.choices().size() != type2.choices().size()){
			return false;
		}		
		
		for(TypeInlineStructure choice1 : type1.choices()){
			boolean foundChoice = false;

			for(TypeInlineStructure choice2 : type2.choices()){
				if(choice1.basicType().checkBasicTypeEqualness(choice2.basicType())){
					foundChoice = true;
					break;
				}
			}

			if(!foundChoice){
				return false;
			}
		}

		return true;
	}


}
