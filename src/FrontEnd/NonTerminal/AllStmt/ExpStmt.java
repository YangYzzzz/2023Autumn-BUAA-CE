package FrontEnd.NonTerminal.AllStmt;

import FrontEnd.NonTerminal.Exp;
import FrontEnd.NonTerminal.Stmt;

public class ExpStmt implements Stmt {
    private Exp exp;

    public ExpStmt() {
    }

    public Exp getExp() {
        return exp;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }

    @Override
    public void genLLVM() {
        // 是有意义的 可能会调用函数影响变量
        if (exp != null) {
            exp.genLLVM();
        }
    }
}
