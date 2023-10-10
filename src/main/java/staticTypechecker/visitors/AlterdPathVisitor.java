package staticTypechecker.visitors;

import java.util.ArrayList;

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
import jolie.lang.parse.ast.InstallFunctionNode;
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
import jolie.util.Pair;
import staticTypechecker.entities.Path;

public class AlterdPathVisitor implements OLVisitor<ArrayList<Path>, Void>{

    @Override
    public Void visit(Program n, ArrayList<Path> ctx) {
        for(OLSyntaxNode child : n.children()) {
            go(child, ctx);
        }
        return null;
    }

    @Override
    public Void visit(OneWayOperationDeclaration decl, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(RequestResponseOperationDeclaration decl, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(DefinitionNode n, ArrayList<Path> ctx) {
        go(n.body(), ctx);
        return null;
    }

    @Override
    public Void visit(ParallelStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(SequenceStatement n, ArrayList<Path> ctx) {
        for(OLSyntaxNode child : n.children()) {
            go(child, ctx);
        }
        return null;
    }

    @Override
    public Void visit(NDChoiceStatement n, ArrayList<Path> ctx) {
        for(Pair<OLSyntaxNode, OLSyntaxNode> child : n.children()) {
            go(child.key(), ctx);
            go(child.value(), ctx);
        }
        return null;
    }

    @Override
    public Void visit(OneWayOperationStatement n, ArrayList<Path> ctx) {
        ctx.add(new Path(n.inputVarPath().path()));
        return null;
    }

    @Override
    public Void visit(RequestResponseOperationStatement n, ArrayList<Path> ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(NotificationOperationStatement n, ArrayList<Path> ctx) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visit'");
    }

    @Override
    public Void visit(SolicitResponseOperationStatement n, ArrayList<Path> ctx) {
        Path inPath;
        if(n.inputVarPath() == null){
            inPath = new Path();
        } else {
            inPath = new Path(n.inputVarPath().path());
        }
        ctx.add(inPath);
        return null;
    }

    @Override
    public Void visit(LinkInStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(LinkOutStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(AssignStatement n, ArrayList<Path> ctx) {
        ctx.add(new Path(n.variablePath().path()));
        return null;
    }

    @Override
    public Void visit(AddAssignStatement n, ArrayList<Path> ctx) {
        ctx.add(new Path(n.variablePath().path()));
        return null;
    }

    @Override
    public Void visit(SubtractAssignStatement n, ArrayList<Path> ctx) {
        ctx.add(new Path(n.variablePath().path()));
        return null;
    }

    @Override
    public Void visit(MultiplyAssignStatement n, ArrayList<Path> ctx) {
        ctx.add(new Path(n.variablePath().path()));
        return null;
    }

    @Override
    public Void visit(DivideAssignStatement n, ArrayList<Path> ctx) {
        ctx.add(new Path(n.variablePath().path()));
        return null;
    }

    @Override
    public Void visit(IfStatement n, ArrayList<Path> ctx) {
        for(Pair<OLSyntaxNode, OLSyntaxNode> child : n.children()) {
            go(child.value(), ctx);
        }
        return null;
    }

    @Override
    public Void visit(DefinitionCallStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(WhileStatement n, ArrayList<Path> ctx) {
        go(n.body(), ctx);
        return null;
    }

    @Override
    public Void visit(OrConditionNode n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(AndConditionNode n, ArrayList<Path> ctx) {
       return null;
    }

    @Override
    public Void visit(NotExpressionNode n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(CompareConditionNode n, ArrayList<Path> ctx) {
       return null;
    }

    @Override
    public Void visit(ConstantIntegerExpression n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(ConstantDoubleExpression n, ArrayList<Path> ctx) {
       return null;
    }

    @Override
    public Void visit(ConstantBoolExpression n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(ConstantLongExpression n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(ConstantStringExpression n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(ProductExpressionNode n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(SumExpressionNode n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(VariableExpressionNode n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(NullProcessStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(Scope n, ArrayList<Path> ctx) {
        go(n.body(), ctx);
        return null;
    }

    @Override
    public Void visit(InstallStatement n, ArrayList<Path> ctx) {
        InstallFunctionNode handlerFunction = n.handlersFunction();
        for(Pair<String, OLSyntaxNode> pair : handlerFunction.pairs()) {
            go(pair.value(), ctx);
        }
        return null;
    }

    @Override
    public Void visit(CompensateStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(ThrowStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(ExitStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(ExecutionInfo n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(CorrelationSetInfo n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(InputPortInfo n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(OutputPortInfo n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(PointerStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(DeepCopyStatement n, ArrayList<Path> ctx) {
        ctx.add(new Path(n.leftPath().path()));
        return null;
    }

    @Override
    public Void visit(RunStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(UndefStatement n, ArrayList<Path> ctx) {
        ctx.add(new Path(n.variablePath().path()));
        return null;
    }

    @Override
    public Void visit(ValueVectorSizeExpressionNode n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(PreIncrementStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(PostIncrementStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(PreDecrementStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(PostDecrementStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(ForStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(ForEachSubNodeStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(ForEachArrayItemStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(SpawnStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(IsTypeExpressionNode n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(InstanceOfExpressionNode n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(TypeCastExpressionNode n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(SynchronizedStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(CurrentHandlerStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(EmbeddedServiceNode n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(InstallFixedVariableExpressionNode n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(VariablePathNode n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(TypeInlineDefinition n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(TypeDefinitionLink n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(InterfaceDefinition n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(DocumentationComment n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(FreshValueExpressionNode n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(CourierDefinitionNode n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(CourierChoiceStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(NotificationForwardStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(SolicitResponseForwardStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(InterfaceExtenderDefinition n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(InlineTreeExpressionNode n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(VoidExpressionNode n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(ProvideUntilStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(TypeChoiceDefinition n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(ImportStatement n, ArrayList<Path> ctx) {
        return null;
    }

    @Override
    public Void visit(ServiceNode n, ArrayList<Path> ctx) {
        go(n.program(), ctx);
        return null;
    }

    @Override
    public Void visit(EmbedServiceNode n, ArrayList<Path> ctx) {
        return null;
    }
    
}
