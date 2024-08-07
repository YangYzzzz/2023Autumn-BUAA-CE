package FrontEnd.NonTerminal.AllStmt;

import FrontEnd.NonTerminal.Cond;
import FrontEnd.NonTerminal.Stmt;
import MidEnd.IrInstuctions.IrBr;

import java.util.ArrayList;

import static MidEnd.IrCore.IrModule.curIrBasicBlock;
import static MidEnd.IrCore.IrModule.curIrFunction;
import static MidEnd.Tools.LLVMCode.backFillBrLLVMCode;

public class JudgeStmt implements Stmt {
    private Cond cond;
    private Stmt stmt;
    private Stmt elseStmt;
    private ArrayList<IrBr> irBrLLVMCode;
    public JudgeStmt() {
        irBrLLVMCode = new ArrayList<>();
    }

    public Cond getCond() {
        return cond;
    }

    public void setCond(Cond cond) {
        this.cond = cond;
    }

    public Stmt getStmt() {
        return stmt;
    }

    public void setStmt(Stmt stmt) {
        this.stmt = stmt;
    }

    public Stmt getElseStmt() {
        return elseStmt;
    }

    public void setElseStmt(Stmt elseStmt) {
        this.elseStmt = elseStmt;
    }

    @Override
    public void genLLVM() {
        /*
            Label出现在几个位置
            1. Stmt之前
            2. elseStmt之前
            3. elseStmt之后
            4. 每个 && || 之后
         */
        // 初始化需要回填的指令列表
        cond.genLLVM(irBrLLVMCode, "!stmtLabel", "!elseStmtLabel");

        // if语句块
        curIrFunction.allocBasicBlock();
        backFillBrLLVMCode(irBrLLVMCode, curIrBasicBlock, "!stmtLabel");
        stmt.genLLVM();
        IrBr irBrLabel;
        irBrLabel = new IrBr("!ifEndLabel");
        curIrBasicBlock.addInstruction(irBrLabel);
        irBrLLVMCode.add(irBrLabel);
        if (elseStmt != null) {
            // else语句块
            curIrFunction.allocBasicBlock();
            backFillBrLLVMCode(irBrLLVMCode, curIrBasicBlock, "!elseStmtLabel");
            elseStmt.genLLVM();
            irBrLabel = new IrBr("!ifEndLabel");
            curIrBasicBlock.addInstruction(irBrLabel);
            irBrLLVMCode.add(irBrLabel);
        }
        curIrFunction.allocBasicBlock();
        backFillBrLLVMCode(irBrLLVMCode, curIrBasicBlock, "!ifEndLabel");
        // 若无else语句，则原本该跳到elseStmtLabel的标签直接跳到if结尾
        backFillBrLLVMCode(irBrLLVMCode, curIrBasicBlock, "!elseStmtLabel");
    }
}
