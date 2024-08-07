package FrontEnd.NonTerminal.AllStmt;

import FrontEnd.NonTerminal.Sign;
import FrontEnd.NonTerminal.Stmt;
import MidEnd.IrInstuctions.IrBr;

import static MidEnd.IrCore.IrModule.curIrBasicBlock;

public class BreakOrContinueStmt implements Stmt {
    private Sign sign;
    private LoopStmt loopStmt;

    public BreakOrContinueStmt() {
    }

    public Sign getSign() {
        return sign;
    }

    public void setSign(Sign sign) {
        this.sign = sign;
    }

    public void setLoopStmt(LoopStmt loopStmt) {
        this.loopStmt = loopStmt;
    }

    @Override
    public void genLLVM() {
        IrBr instruction;
        if (sign == Sign.BREAKTK) {
            instruction = new IrBr("!forEndLabel");
        } else {
            instruction = new IrBr("!forStmtLabel");
        }
        curIrBasicBlock.addInstruction(instruction);
        loopStmt.getBrLLVMCode().add(instruction);
    }
}
