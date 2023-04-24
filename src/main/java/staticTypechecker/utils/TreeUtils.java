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
import staticTypechecker.entities.ChoiceType;
import staticTypechecker.entities.InlineType;
import staticTypechecker.entities.Type;

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
		ArrayList<Pair<InlineType, String>> parents = TreeUtils.findParentAndName(path, root, createPath, true);

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
	public static ArrayList<Pair<InlineType, String>> findParentAndName(Path path, Type root, boolean createPath, boolean createChild){
		if(root instanceof InlineType){
			return TreeUtils.findParentAndNameRec(path, (InlineType)root, createPath, createChild, Collections.newSetFromMap(new IdentityHashMap<>()));
		}
		else{ // root is ChoiceType
			ArrayList<Pair<InlineType, String>> result = new ArrayList<>();
			for(InlineType choice : ((ChoiceType)root).choices()){
				result.addAll(TreeUtils.findParentAndNameRec(path, choice, createPath, createChild, Collections.newSetFromMap(new IdentityHashMap<>())));
			}

			return result;
		}
	}

	public static ArrayList<Pair<InlineType, String>> findParentAndName(Path path, Type root, boolean createPath){
		return TreeUtils.findParentAndName(path, root, createPath, true);
	}

	private static ArrayList<Pair<InlineType, String>> findParentAndNameRec(Path path, InlineType root, boolean createPath, boolean createChild, Set<InlineType> seenNodes){
		ArrayList<Pair<InlineType, String>> ret = new ArrayList<>();
		seenNodes.add(root);
				
		if(path.isEmpty()){
			return ret;
		}

		String childNameToLookFor = path.get(0);
		Type childNode;

		if(root.contains(childNameToLookFor)){
			childNode = root.getChild(childNameToLookFor);

			if(path.size() == 1){ // it was the child to look for
				ret.add(new Pair<InlineType, String>(root, childNameToLookFor));
				return ret;
			}
			
			if(childNode instanceof InlineType){ 
				InlineType parsedChild = (InlineType)childNode;
				ret.addAll(TreeUtils.findParentAndNameRec(path.remainder(), parsedChild, createPath, createChild, seenNodes));
			}
			else{ // child is choice node, continue search in all choices
				ChoiceType parsedChild = (ChoiceType)childNode;
				parsedChild.choices().forEach(c -> ret.addAll(TreeUtils.findParentAndNameRec(path.remainder(), c, createPath, createChild, seenNodes)));
			}
		}
		else if(createPath){
			InlineType newChild = new InlineType(BasicTypeDefinition.of(NativeType.VOID), null, null, false);
			
			if(path.size() == 1){ // this was the child to look for
				ret.add(new Pair<InlineType,String>(root, childNameToLookFor));

				if(createChild){
					root.addChildUnsafe(childNameToLookFor, newChild);
				}
			}
			else{
				root.addChildUnsafe(childNameToLookFor, newChild);
				ret.addAll(TreeUtils.findParentAndNameRec(path.remainder(), newChild, createPath, createChild, seenNodes));
			}
		}

		return ret;
	}

	/**
	 * Unfolds the Type root in place
	 * @param path the path to follow. The unfolding will stop when reaching the end of this path
	 * @param root the Type to unfold
	 * @param rootCopy a deep copy of root 
	 * @param seenNodes a Set<Type> of the nodes that already have been processed
	 */
	public static Type unfold(Path path, Type root){
		return TreeUtils.unfoldRec(path, root.copy(), root.copy(), Collections.newSetFromMap(new IdentityHashMap<>()));
	}
	
	public static Type unfoldRec(Path path, Type root, Type rootCopy, Set<Type> seenNodes){
		seenNodes.add(root);
		
		if(root instanceof InlineType){
			InlineType parsedRoot = (InlineType)root;
			InlineType parsedRootCopy = (InlineType)rootCopy;

			if(path.isEmpty()){ // we do not need to unfold anymore, only update the children
				for(Entry<String, Type> ent : parsedRootCopy.children().entrySet()){
					parsedRoot.addChildUnsafe(ent.getKey(), ent.getValue());
				}
				return root;
			}
			else{
				String childToLookFor = path.get(0);

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
						TreeUtils.unfoldRec(path.remainder(), child, childCopy, seenNodes);
					}
					else{ // it is not a child on the path
						if(seenNodes.contains(child)){ // a recursive edge
							// point to the copy structure
							parsedRoot.addChildUnsafe(childName, childCopy);
						}
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
						TreeUtils.unfoldRec(path, choice, choiceCopy, seenNodes);
						break;
					}
				}
			}
		}

		return root;
	}

	public static void fold(Type root){
		if(root == null){
			return;
		}

		if(root instanceof InlineType){
			InlineType parsedRoot = (InlineType)root;
			for(Entry<String, Type> ent : parsedRoot.children().entrySet()){
				String childName = ent.getKey();
				Type child = ent.getValue();

				if(child.equals(parsedRoot)){
					parsedRoot.addChildUnsafe(childName, parsedRoot);
				}
				else{
					TreeUtils.fold(child);
				}
			}
		}
		else{ // choice types
			ChoiceType parsedRoot = (ChoiceType)root;
			for(InlineType choice : parsedRoot.choices()){
				TreeUtils.fold(choice);
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
		ArrayList<Pair<InlineType,String>> nodesToUpdate = TreeUtils.findParentAndName(path, tree, true, true);
		
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
		TreeUtils.unfold(path, tree);

		ArrayList<Pair<InlineType,String>> nodesToUpdate = TreeUtils.findParentAndName(path, tree, true, true);
		
		for(Pair<InlineType,String> pair : nodesToUpdate){
			InlineType parent = pair.key();
			String childName = pair.value();

			if(basicTypes.size() == 1){ // only one basic type, update the child in the parent with a copy of the old child with a new basic type
				if(parent.getChild(childName) instanceof InlineType){
					parent.addChildUnsafe(childName, ((InlineType)parent.getChild(childName)).setBasicType(basicTypes.get(0)));
				}
				else{
					ChoiceType child = ((ChoiceType)parent.getChild(childName)).updateBasicTypeOfChoices(basicTypes.get(0));
					// this check is necessary, since changing the basic type of all choices, may make some of them equivalence, and hence they will be removed. We may be in a situation of a choice type with only one choice (which we convert to an InlineType below)
					if(child.choices().size() == 1){
						parent.addChildUnsafe(childName, new InlineType(basicTypes.get(0), null, child.context(), false));
					}
					else{
						parent.addChildUnsafe(childName, child);
					}
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

	/**
	 * Will go through the trees and any node which differs in basic type will be converted to an any{?} node 
	 * @param orignalType
	 * @param other
	 * @return a copy of originalType but with all nodes converted to any{?} if the corresponding node in other has a different basic type
	 */
	public static Type undefine(Type orignalType, Type other){
		Type copy = orignalType.copy();
		TreeUtils.undefineRec(copy, other, null, null, Collections.newSetFromMap(new IdentityHashMap<>()));
		return copy;
	}

	/**
	 * VERSION 2, discard the children, reset the parents
	 * Precondition: original and other must have the same basic type
	 */
	private static void undefineRec(Type original, Type other, String name, Type parent, Set<Type> seenNodes){
		if(original == null || other == null){
			return;
		}
		if(seenNodes.contains(original)){
			return;
		}
		seenNodes.add(original);

		if(original instanceof InlineType && other instanceof InlineType){
			InlineType parsedOG = (InlineType)original;
			InlineType parsedOther = (InlineType)other;

			if(!parsedOG.basicType().equals(parsedOther.basicType())){ // basic types are different, reset node and return
				TreeUtils.resetNodeType(parsedOG, parent, name);
				return;
			}

			// otherwise the basic types are equal, thus we must do the same check for the children
			for(Entry<String, Type> ent : parsedOG.children().entrySet()){
				String childName = ent.getKey();
				Type childOG = ent.getValue();
				Type childOther = parsedOther.getChild(childName);
				TreeUtils.undefineRec(childOG, childOther, childName, parsedOG, seenNodes);
			}
		}
		else if(original instanceof InlineType && other instanceof ChoiceType){ // not same type, we must reset
			TreeUtils.resetNodeType(original, parent, name);
		}
		else if(original instanceof ChoiceType && other instanceof InlineType){ // not same type, we must reset
			TreeUtils.resetNodeType(original, parent, name);
		}
		else{
			if(!original.equals(other)){ 
				TreeUtils.resetNodeType(original, parent, name);
			}
		}
	}

	private static void removeChildOrChoice(Type child, Type parent, String name){
		if(parent instanceof InlineType){
			((InlineType)parent).removeChildUnsafe(name);
		}
		else{
			((ChoiceType)parent).removeChoiceUnsafe(child);
		}
	}

	private static void addChildOrChoice(Type child, Type parent, String name){
		if(parent instanceof InlineType){
			((InlineType)parent).addChildUnsafe(name, child);
		}
		else{
			((ChoiceType)parent).addChoiceUnsafe(child);
		}
	}

	private static void resetNodeType(Type node, Type parent, String name){
		if(parent != null){
			TreeUtils.removeChildOrChoice(node, parent, name);
			TreeUtils.addChildOrChoice(Type.OPEN_RECORD(), parent, name);
		}
	}

}
