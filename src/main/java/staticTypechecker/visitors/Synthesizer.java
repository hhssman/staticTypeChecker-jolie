package staticTypechecker.visitors;

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
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import staticTypechecker.typeStructures.TypeInlineStructure;
import staticTypechecker.typeStructures.TypeStructure;
import staticTypechecker.utils.TreeUtils;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.Operation;
import staticTypechecker.entities.Operation.OperationType;;

/**
 * Synthesizer for a parsed Jolie abstract syntax tree. Works as a visitor and will visit each node in the provided tree.
 * 
 * @author Kasper Bergstedt, kberg18@student.sdu.dk
 */
public class Synthesizer implements OLVisitor<TypeInlineStructure, Void> {
	private Module module;
	
	public Synthesizer(Module module){
		this.module = module;
	}

	public void synthesize(OLSyntaxNode node, TypeInlineStructure tree){
		node.accept(this, tree);
	}

	public Void visit(Program p, TypeInlineStructure tree){
		for(OLSyntaxNode n : p.children()){
			n.accept(this, null);
		}

		return null;
	}

	public Void visit(TypeInlineDefinition t, TypeInlineStructure tree){
		return null;
	}

	public Void visit( OneWayOperationDeclaration decl, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( RequestResponseOperationDeclaration decl, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( DefinitionNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( ParallelStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( SequenceStatement n, TypeInlineStructure tree ){
		for(OLSyntaxNode child : n.children()){
			 child.accept(this, tree);
		}

		return null;
	};

	public Void visit( NDChoiceStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( OneWayOperationStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( RequestResponseOperationStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( NotificationOperationStatement n, TypeInlineStructure tree ){
		Operation op = (Operation)this.module.symbols().get(n.id());

		if(op == null){ // operation does not exist, throw error? TODO
			return null;
		}

		if(op.type() != OperationType.ONEWAY){
			// TODO error
			return null;
		}

		TypeStructure opInputType = (TypeStructure)this.module.symbols().get(op.requestType());
		TypeStructure givenInputType = TreeUtils.getTypeOfExpression(n.outputExpression(), tree);

		System.out.println("operation takes:\n" + opInputType.prettyString() + "\n\nand we are given:\n" + givenInputType.prettyString());

		if(!givenInputType.isSubtypeOf(opInputType)){
			// TODO error
			return null;
		}

		return null;
	};

	public Void visit( SolicitResponseOperationStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( LinkInStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( LinkOutStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( AssignStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( AddAssignStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( SubtractAssignStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( MultiplyAssignStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( DivideAssignStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( IfStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( DefinitionCallStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( WhileStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( OrConditionNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( AndConditionNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( NotExpressionNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( CompareConditionNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( ConstantIntegerExpression n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( ConstantDoubleExpression n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( ConstantBoolExpression n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( ConstantLongExpression n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( ConstantStringExpression n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( ProductExpressionNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( SumExpressionNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( VariableExpressionNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( NullProcessStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( Scope n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( InstallStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( CompensateStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( ThrowStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( ExitStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( ExecutionInfo n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( CorrelationSetInfo n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( InputPortInfo n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( OutputPortInfo n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( PointerStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( DeepCopyStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( RunStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( UndefStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( ValueVectorSizeExpressionNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( PreIncrementStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( PostIncrementStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( PreDecrementStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( PostDecrementStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( ForStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( ForEachSubNodeStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( ForEachArrayItemStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( SpawnStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( IsTypeExpressionNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( InstanceOfExpressionNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( TypeCastExpressionNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( SynchronizedStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( CurrentHandlerStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( EmbeddedServiceNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( InstallFixedVariableExpressionNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( VariablePathNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( TypeDefinitionLink n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( InterfaceDefinition n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( DocumentationComment n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( FreshValueExpressionNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( CourierDefinitionNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( CourierChoiceStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( NotificationForwardStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( SolicitResponseForwardStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( InterfaceExtenderDefinition n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( InlineTreeExpressionNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( VoidExpressionNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( ProvideUntilStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( TypeChoiceDefinition n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( ImportStatement n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( ServiceNode n, TypeInlineStructure tree ){
		return null;
	};

	public Void visit( EmbedServiceNode n, TypeInlineStructure tree ){
		return null;
	};
}
