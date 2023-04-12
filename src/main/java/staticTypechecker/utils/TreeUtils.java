package staticTypechecker.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.Map.Entry;

import jolie.lang.NativeType;
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.util.Pair;
import staticTypechecker.entities.Path;
import staticTypechecker.typeStructures.ChoiceType;
import staticTypechecker.typeStructures.InlineType;
import staticTypechecker.typeStructures.Type;

/**
 * Utily functions for working with type trees
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class TreeUtils {
	/**
	 * Finds the exact nodes at the specified path. That is, if it is a choice node at the end of the path, the choice node is returned, rather than the different choices as in the findNodes function.
	 * @param path the path to follow
	 * @param root the root of the tree to search
	 * @param createPath whether the path should be created with void nodes if it is not present
	 * @return an arraylist of the nodes found at the end of the path
	 */
	public static ArrayList<Type> findNodesExact(Path path, Type root, boolean createPath, boolean unfold){
		ArrayList<Type> ret = new ArrayList<>();
		ArrayList<Pair<InlineType, String>> parents = TreeUtils.findParentAndName(path, root, createPath, unfold);

		for(Pair<InlineType, String> pair : parents){
			InlineType parent = pair.key();
			Type child = parent.getChild(pair.value());
			ret.add(child);
		}

		return ret;
	}

	/**
	 * Finds the parent nodes and child names of the nodes at the specified path. I.e. if the provided path is a.b.c, then it would return the pair (b, "c"), since b is the parent node and the childname is c.
	 * @param path the path to follow
	 * @param root the root node of the tree
	 * @param createPath whether or not the path should be created with void instances, if it does not exist
	 * @param unfold whether or not to unfold the structure in case of a recursive type structure
	 * @return an ArrayList of Pairs of InlineTypes and Strings, indicating the parent nodes and the child names
	 */
	public static ArrayList<Pair<InlineType, String>> findParentAndName(Path path, Type root, boolean createPath, boolean unfold){
		if(root instanceof InlineType){
			return TreeUtils.findParentAndNameRec(path, (InlineType)root, createPath, unfold, Collections.newSetFromMap(new IdentityHashMap<>()));
		}
		else{ // root is ChoiceType
			ArrayList<Pair<InlineType, String>> result = new ArrayList<>();
			for(InlineType choice : ((ChoiceType)root).choices()){
				result.addAll(TreeUtils.findParentAndNameRec(path, choice, createPath, unfold, Collections.newSetFromMap(new IdentityHashMap<>())));
			}

			return result;
		}
	}

	private static ArrayList<Pair<InlineType, String>> findParentAndNameRec(Path path, InlineType root, boolean createPath, boolean unfold, Set<InlineType> seenNodes){
		ArrayList<Pair<InlineType, String>> ret = new ArrayList<>();
		seenNodes.add(root);
				
		if(path.isEmpty()){
			return ret;
		}

		String childToLookFor = path.get(0);
		Type childNode;

		if(root.contains(childToLookFor)){
			childNode = root.getChild(childToLookFor);

			if(unfold && seenNodes.contains(childNode)){
				TreeUtils.unfold(path, root, root.copy(), Collections.newSetFromMap(new IdentityHashMap<>()));
				childNode = root.getChild(childToLookFor); // reassign the child to the child in the unfolded version
			}

			if(path.size() == 1){ // it was the child to look for
				ret.add(new Pair<InlineType, String>(root, childToLookFor));
				return ret;
			}
			
			if(childNode instanceof InlineType){ 
				InlineType parsedChild = (InlineType)childNode;
				ret.addAll(TreeUtils.findParentAndNameRec(path.remainder(), parsedChild, createPath, unfold, seenNodes));
			}
			else{ // child is choice node, continue search in all choices
				ChoiceType parsedChild = (ChoiceType)childNode;
				parsedChild.choices().forEach(c -> ret.addAll(TreeUtils.findParentAndNameRec(path.remainder(), c, createPath, unfold, seenNodes)));
			}
		}
		else if(createPath){
			InlineType newChild = new InlineType(BasicTypeDefinition.of(NativeType.VOID), null, null, false); // TODO ask Marco if this should be open or not, I think it should not
			root.addChildUnsafe(childToLookFor, newChild);
			
			if(path.size() == 1){
				ret.add(new Pair<InlineType,String>(root, childToLookFor));
			}
			else{
				ret.addAll(TreeUtils.findParentAndNameRec(path.remainder(), newChild, createPath, unfold, seenNodes));
			}
		}

		return ret;
	}

	/**
	 * Unfolds the Type root in place
	 * @param root the Type to unfold
	 * @param rootCopy a deep copy of root 
	 * @param seenNodes a Set<Type> of the nodes that already have been processed
	 */
	private static void unfold(Path path, Type root, Type rootCopy, Set<Type> seenNodes){
		seenNodes.add(root);
		
		if(root instanceof InlineType){
			InlineType parsedRoot = (InlineType)root;
			InlineType parsedRootCopy = (InlineType)rootCopy;

			if(path.isEmpty()){ // we do not need to unfold anymore, only update the children
				for(Entry<String, Type> ent : parsedRootCopy.children().entrySet()){
					parsedRoot.addChildUnsafe(ent.getKey(), ent.getValue());
				}
				return;
			}
			else{
				String childToLookFor = path.get(0);

				if(parsedRoot.contains(childToLookFor)){
					for(String childName : parsedRoot.children().keySet()){
						Type child = parsedRoot.getChild(childName);
						Type childCopy = parsedRootCopy.getChild(childName);

						if(childName.equals(childToLookFor)){ // it is the next child on the path
							if(seenNodes.contains(child)){ // a recursive edge
								// unfold once
								child = childCopy.copy();
								parsedRoot.addChildUnsafe(childName, child);
							}
	
							// continue on the path
							TreeUtils.unfold(path.remainder(), child, childCopy, seenNodes);
						}
						else{ // it is not a child on the path
							if(seenNodes.contains(child)){ // a recursive edge
								// point to the copy structure
								parsedRoot.addChildUnsafe(childName, childCopy);
							}
						}
					}
				}
				else{
					InlineType newChild = new InlineType(BasicTypeDefinition.of(NativeType.VOID), null, null, false); // TODO ask Marco if this should be open or not, I think it should not
					parsedRoot.addChildUnsafe(childToLookFor, newChild);
					
					if(path.size() > 1){ // if this was not the last element on the path, continue
						TreeUtils.unfold(path.remainder(), newChild, null, seenNodes);
					}
				}
			}
		}
		else{ // choice type
			ChoiceType parsedRoot = (ChoiceType)root;
			ChoiceType parsedRootCopy = (ChoiceType)rootCopy;

			for(InlineType choice : parsedRoot.choices()){
				for(InlineType choiceCopy : parsedRootCopy.choices()){
					if(choice.equals(choiceCopy)){
						TreeUtils.unfold(path, choice, choiceCopy, seenNodes);
						break;
					}
				}
			}
		}
	}

	/**
	 * Sets the type of the nodes at the end of the specified path by the given type.
	 * @param path the path to follow
	 * @param type the type to update all nodes at the end of the path to
	 * @param tree the tree in which the nodes reside
	 */
	public static void setTypeOfNodeByPath(Path path, Type type, Type tree){
		ArrayList<Pair<InlineType,String>> nodesToUpdate = TreeUtils.findParentAndName(path, tree, true, false);
		
		for(Pair<InlineType,String> pair : nodesToUpdate){
			pair.key().addChildUnsafe(pair.value(), type);
		}
	}

	/**
	 * Updates the basic type of the nodes specified by the given path. NOTE: unfolds the type in case of a recursive type
	 * @param path the path to follow
	 * @param basicTypes the new basic types
	 * @param tree the root of the tree in which to follow the path
	 */
	public static void setBasicTypeOfNodeByPath(Path path, ArrayList<BasicTypeDefinition> basicTypes, Type tree){
		TreeUtils.unfold(path, tree, tree.copy(), Collections.newSetFromMap(new IdentityHashMap<>()));

		ArrayList<Pair<InlineType,String>> nodesToUpdate = TreeUtils.findParentAndName(path, tree, true, false);
		
		for(Pair<InlineType,String> pair : nodesToUpdate){
			InlineType parent = pair.key();
			String childName = pair.value();

			if(basicTypes.size() == 1){ // update the child in the parent with a copy of the old child with a new basic type
				if(parent.getChild(childName) instanceof InlineType){
					parent.addChildUnsafe(childName, ((InlineType)parent.getChild(childName)).setBasicType(basicTypes.get(0)));
				}
				else{
					parent.addChildUnsafe(childName, ((ChoiceType)parent.getChild(childName)).updateBasicTypeOfChoices(basicTypes.get(0)));
				}
			}
			else{ // 
				ChoiceType newChild = new ChoiceType();

				if(parent.getChild(childName) instanceof InlineType){
					InlineType oldChild = (InlineType)parent.getChild(childName);

					for(BasicTypeDefinition b : basicTypes){
						newChild.addChoiceUnsafe(oldChild.setBasicType(b));
					}
				}
				else{
					ChoiceType oldChild = (ChoiceType)parent.getChild(childName);
					
					for(BasicTypeDefinition b : basicTypes){
						for(InlineType c : oldChild.choices()){
							newChild.addChoiceUnsafe(c.setBasicType(b));
						}
					}
				}

				parent.addChildUnsafe(childName, newChild);
			}
		}
	}

}
