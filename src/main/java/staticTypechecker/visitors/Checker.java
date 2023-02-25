package staticTypechecker.visitors;

import java.util.HashMap;
import java.util.Map;

import jolie.lang.parse.OLVisitor;
import jolie.lang.parse.ast.AddAssignStatement;
import jolie.lang.parse.ast.AssignStatement;
import jolie.lang.parse.ast.CompareConditionNode;
import jolie.lang.parse.ast.CompensateStatement;
import jolie.lang.parse.ast.CorrelationSetInfo;
import jolie.lang.parse.ast.CurrentHandlerStatement;
import jolie.lang.parse.ast.DeepCopyStatement;
import jolie.lang.parse.ast.DefinitionCallStatement;
import jolie.lang.parse.ast.DefinitionNode;
import jolie.lang.parse.ast.DivideAssignStatement;
import jolie.lang.parse.ast.DocumentationComment;
import jolie.lang.parse.ast.EmbedServiceNode;
import jolie.lang.parse.ast.EmbeddedServiceNode;
import jolie.lang.parse.ast.ExecutionInfo;
import jolie.lang.parse.ast.ExitStatement;
import jolie.lang.parse.ast.ForEachArrayItemStatement;
import jolie.lang.parse.ast.ForEachSubNodeStatement;
import jolie.lang.parse.ast.ForStatement;
import jolie.lang.parse.ast.IfStatement;
import jolie.lang.parse.ast.ImportStatement;
import jolie.lang.parse.ast.InputPortInfo;
import jolie.lang.parse.ast.InstallFixedVariableExpressionNode;
import jolie.lang.parse.ast.InstallStatement;
import jolie.lang.parse.ast.InterfaceDefinition;
import jolie.lang.parse.ast.InterfaceExtenderDefinition;
import jolie.lang.parse.ast.LinkInStatement;
import jolie.lang.parse.ast.LinkOutStatement;
import jolie.lang.parse.ast.MultiplyAssignStatement;
import jolie.lang.parse.ast.NDChoiceStatement;
import jolie.lang.parse.ast.NotificationOperationStatement;
import jolie.lang.parse.ast.NullProcessStatement;
import jolie.lang.parse.ast.OLSyntaxNode;
import jolie.lang.parse.ast.OneWayOperationDeclaration;
import jolie.lang.parse.ast.OneWayOperationStatement;
import jolie.lang.parse.ast.OutputPortInfo;
import jolie.lang.parse.ast.ParallelStatement;
import jolie.lang.parse.ast.PointerStatement;
import jolie.lang.parse.ast.PostDecrementStatement;
import jolie.lang.parse.ast.PostIncrementStatement;
import jolie.lang.parse.ast.PreDecrementStatement;
import jolie.lang.parse.ast.PreIncrementStatement;
import jolie.lang.parse.ast.Program;
import jolie.lang.parse.ast.ProvideUntilStatement;
import jolie.lang.parse.ast.RequestResponseOperationDeclaration;
import jolie.lang.parse.ast.RequestResponseOperationStatement;
import jolie.lang.parse.ast.RunStatement;
import jolie.lang.parse.ast.Scope;
import jolie.lang.parse.ast.SequenceStatement;
import jolie.lang.parse.ast.ServiceNode;
import jolie.lang.parse.ast.SolicitResponseOperationStatement;
import jolie.lang.parse.ast.SpawnStatement;
import jolie.lang.parse.ast.SubtractAssignStatement;
import jolie.lang.parse.ast.SynchronizedStatement;
import jolie.lang.parse.ast.ThrowStatement;
import jolie.lang.parse.ast.TypeCastExpressionNode;
import jolie.lang.parse.ast.UndefStatement;
import jolie.lang.parse.ast.ValueVectorSizeExpressionNode;
import jolie.lang.parse.ast.VariablePathNode;
import jolie.lang.parse.ast.WhileStatement;
import jolie.lang.parse.ast.courier.CourierChoiceStatement;
import jolie.lang.parse.ast.courier.CourierDefinitionNode;
import jolie.lang.parse.ast.courier.NotificationForwardStatement;
import jolie.lang.parse.ast.courier.SolicitResponseForwardStatement;
import jolie.lang.parse.ast.expression.AndConditionNode;
import jolie.lang.parse.ast.expression.ConstantBoolExpression;
import jolie.lang.parse.ast.expression.ConstantDoubleExpression;
import jolie.lang.parse.ast.expression.ConstantIntegerExpression;
import jolie.lang.parse.ast.expression.ConstantLongExpression;
import jolie.lang.parse.ast.expression.ConstantStringExpression;
import jolie.lang.parse.ast.expression.FreshValueExpressionNode;
import jolie.lang.parse.ast.expression.InlineTreeExpressionNode;
import jolie.lang.parse.ast.expression.InstanceOfExpressionNode;
import jolie.lang.parse.ast.expression.IsTypeExpressionNode;
import jolie.lang.parse.ast.expression.NotExpressionNode;
import jolie.lang.parse.ast.expression.OrConditionNode;
import jolie.lang.parse.ast.expression.ProductExpressionNode;
import jolie.lang.parse.ast.expression.SumExpressionNode;
import jolie.lang.parse.ast.expression.VariableExpressionNode;
import jolie.lang.parse.ast.expression.VoidExpressionNode;
import jolie.lang.parse.ast.types.TypeChoiceDefinition;
import jolie.lang.parse.ast.types.TypeDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import jolie.util.Pair;
import staticTypechecker.typeStructures.InlineType;
import staticTypechecker.typeStructures.Type;
import staticTypechecker.utils.Bisimulator;
import staticTypechecker.entities.Module;
import staticTypechecker.faults.Fault;
import staticTypechecker.faults.FaultHandler;;

/**
 * Typechecker for a parsed Jolie abstract syntax tree. Works as a visitor and will visit each node in the provided tree.
 * 
 * @author Kasper Bergstedt, kberg18@student.sdu.dk
 */
public class Checker implements OLVisitor<Pair<Type, Type>, Void> {
	private static HashMap<String, Checker> checkers = new HashMap<>(); // maps module name to checker
	
	public static Checker get(Module module){
		Checker ret = Checker.checkers.get(module.name());
		
		if(ret == null){
			ret = new Checker(module);
			Checker.checkers.put(module.name(), ret);
		}
	
		return ret;
	}
	
	private Module module;
	
	private Checker(Module module){
		this.module = module;
	}

	/**
	 * Checks that the given node in the given tree is a subtype of the given type
	 * @param T the tree in which node resides
	 * @param node the node to check the type of
	 * @param type the type to check against
	 */
	public void check(Type T, OLSyntaxNode node, Type type){
		node.accept(this, new Pair<Type, Type>(T, type));
	} 
	
	/**
	 * @param T
	 * @param node
	 * @param type
	 */
	public void check(Type type1, Type type2){
		if(!Bisimulator.isSubtypeOf(type1, type2)){
			FaultHandler.throwFault("\nType:\n" + type1.prettyString() + "\nis not a subtype of:\n" + type2.prettyString());
		};
	} 

	public Void visit(Program p, Pair<Type, Type> treeAndType){
		for(OLSyntaxNode n : p.children()){
			n.accept(this, null);
		}

		return null;
	}

	public Void visit(TypeInlineDefinition t, Pair<Type, Type> treeAndType){
		System.out.println("Basic type: " + t.basicType().nativeType() + "[" + t.cardinality().min() + ", " + t.cardinality().max() + "]");

		if(t.subTypes() != null){ // has subtypes
			for(Map.Entry<String, TypeDefinition> key : t.subTypes()){
				key.getValue().accept(this, null);
			}
		}

		return null;
	}

	public Void visit(Type t, Pair<Type, Type> treeAndType){
		return null;
	}

	public Void visit( OneWayOperationDeclaration decl, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( RequestResponseOperationDeclaration decl, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( DefinitionNode n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( ParallelStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( SequenceStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( NDChoiceStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( OneWayOperationStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( RequestResponseOperationStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( NotificationOperationStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( SolicitResponseOperationStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( LinkInStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( LinkOutStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( AssignStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( AddAssignStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( SubtractAssignStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( MultiplyAssignStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( DivideAssignStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( IfStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( DefinitionCallStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( WhileStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( OrConditionNode n, Pair<Type, Type> treeAndType ){
		for(OLSyntaxNode child : n.children()){
			child.accept(this, treeAndType);
		}
		return null;
	};

	public Void visit( AndConditionNode n, Pair<Type, Type> treeAndType ){
		for(OLSyntaxNode child : n.children()){
			child.accept(this, treeAndType);
		}
		return null;
	};

	public Void visit( NotExpressionNode n, Pair<Type, Type> treeAndType ){
		n.expression().accept(this, treeAndType);
		return null;
	};

	public Void visit( CompareConditionNode n, Pair<Type, Type> treeAndType ){
		if(!treeAndType.value().isSubtypeOf(Type.BOOL)){
			FaultHandler.throwFault("the expresssion: " + n.leftExpression() + " " + n.opType().toString() + " " + n.rightExpression() + " is not a subtype of:\n" + treeAndType.value().prettyString());
		}
		return null;
	};

	public Void visit( ConstantIntegerExpression n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( ConstantDoubleExpression n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( ConstantBoolExpression n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( ConstantLongExpression n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( ConstantStringExpression n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( ProductExpressionNode n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( SumExpressionNode n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( VariableExpressionNode n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( NullProcessStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( Scope n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( InstallStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( CompensateStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( ThrowStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( ExitStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( ExecutionInfo n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( CorrelationSetInfo n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( InputPortInfo n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( OutputPortInfo n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( PointerStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( DeepCopyStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( RunStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( UndefStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( ValueVectorSizeExpressionNode n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( PreIncrementStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( PostIncrementStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( PreDecrementStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( PostDecrementStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( ForStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( ForEachSubNodeStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( ForEachArrayItemStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( SpawnStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( IsTypeExpressionNode n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( InstanceOfExpressionNode n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( TypeCastExpressionNode n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( SynchronizedStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( CurrentHandlerStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( EmbeddedServiceNode n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( InstallFixedVariableExpressionNode n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( VariablePathNode n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( TypeDefinitionLink n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( InterfaceDefinition n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( DocumentationComment n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( FreshValueExpressionNode n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( CourierDefinitionNode n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( CourierChoiceStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( NotificationForwardStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( SolicitResponseForwardStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( InterfaceExtenderDefinition n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( InlineTreeExpressionNode n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( VoidExpressionNode n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( ProvideUntilStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( TypeChoiceDefinition n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( ImportStatement n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( ServiceNode n, Pair<Type, Type> treeAndType ){
		return null;
	};

	public Void visit( EmbedServiceNode n, Pair<Type, Type> treeAndType ){
		return null;
	};
}
