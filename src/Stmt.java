import java.util.List;

abstract class Stmt {
 interface Visitor<R> {
 R visitBlockStmt(Block stmt);
 R visitExpressionStmt(Expression stmt);
 R visitIFStmt(IF stmt);
 R visitDISPLAYStmt(DISPLAY stmt);
 R visitSCANStmt(SCAN stmt);
 R visitVarStmt(Var stmt);
 R visitWhileStmt(While stmt);
 R visitBeginCodeStmt(BeginCode stmt);
 }
 static class Block extends Stmt {
 Block(List<Stmt> statements) {
 this.statements = statements;
 }

 @Override
 <R> R accept(Visitor<R> visitor) {
 return visitor.visitBlockStmt(this);
 }

 final List<Stmt> statements;
 }
 
 static class BeginCode extends Stmt {
    BeginCode(List<Stmt> statements) {
        this.statements = statements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitBeginCodeStmt(this);
    }

    final List<Stmt> statements;
}

 static class Expression extends Stmt {
 Expression(Expr expression) {
 this.expression = expression;
 }

 @Override
 <R> R accept(Visitor<R> visitor) {
 return visitor.visitExpressionStmt(this);
 }

 final Expr expression;
 }
 static class IF extends Stmt {
 IF(Expr condition, Stmt thenBranch, Stmt elseBranch) {
 this.condition = condition;
 this.thenBranch = thenBranch;
 this.elseBranch = elseBranch;
 }

 @Override
 <R> R accept(Visitor<R> visitor) {
 return visitor.visitIFStmt(this);
 }

 final Expr condition;
 final Stmt thenBranch;
 final Stmt elseBranch;
 }
 /*static class DISPLAY extends Stmt {
 DISPLAY(Expr expression) {
 this.expression = expression;
 }

 @Override
 <R> R accept(Visitor<R> visitor) {
 return visitor.visitDISPLAYStmt(this);
 }

 final Expr expression;
 }*/
 static class DISPLAY extends Stmt {
    DISPLAY(List<Expr> expressions) {
        this.expressions = expressions;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitDISPLAYStmt(this);
    }

    final List<Expr> expressions;
}

 static class SCAN extends Stmt {
    SCAN(List<Token> names) {
        this.names = names;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitSCANStmt(this);
    }

    final List<Token> names;
}
 static class Var extends Stmt {
    Var(Token type,  List<Token> names, List<Expr> initializers) {
        this.type = type;
        this.names = names;
        this.initializers = initializers;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
        return visitor.visitVarStmt(this);
    }

    final Token type;
    final List<Token> names;
    final List<Expr> initializers;
}
 static class While extends Stmt {
 While(Expr condition, Stmt body) {
 this.condition = condition;
 this.body = body;
 }

 @Override
 <R> R accept(Visitor<R> visitor) {
 return visitor.visitWhileStmt(this);
 }

 final Expr condition;
 final Stmt body;
 }

 abstract <R> R accept(Visitor<R> visitor);
}
