package FrontEnd.NonTerminal.AllStmt;

import FrontEnd.NonTerminal.Exp;
import FrontEnd.NonTerminal.Stmt;
import MidEnd.IrCore.IrInstruction;
import MidEnd.IrInstuctions.IrRet;
import MidEnd.IrCore.Value;

import static MidEnd.IrCore.IrModule.curIrBasicBlock;

public class ReturnStmt implements Stmt {
    private Exp exp;
    private int line;

    public ReturnStmt() {
    }

    public Exp getExp() {
        return exp;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    @Override
    public void genLLVM() {
        // 涉及到返回值的问题 两个符号运算得到的结果用 % 承接
        IrInstruction irInstruction;
        if (exp != null) {
            Value value = exp.genLLVM();
            irInstruction = new IrRet(value);
            curIrBasicBlock.addInstruction(irInstruction);
        } else {
            curIrBasicBlock.addInstruction(new IrRet());
        }
    }
}
