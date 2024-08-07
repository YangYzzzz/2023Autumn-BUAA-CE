package FrontEnd.NonTerminal.AllStmt;

import FrontEnd.NonTerminal.Cond;
import FrontEnd.NonTerminal.ForStmt;
import FrontEnd.NonTerminal.Stmt;
import MidEnd.IrInstuctions.IrBr;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

import static MidEnd.IrCore.IrModule.curIrBasicBlock;
import static MidEnd.IrCore.IrModule.curIrFunction;
import static MidEnd.Tools.LLVMCode.backFillBrLLVMCode;

public class LoopStmt implements Stmt {
    private ForStmt initForStmt;
    private Cond cond;
    private ForStmt forStmt;
    private Stmt stmt;
    private ArrayList<IrBr> irBrLLVMCode;
    public LoopStmt() {
        irBrLLVMCode = new ArrayList<>();
    }

    public ArrayList<IrBr> getBrLLVMCode() {
        return irBrLLVMCode;
    }

    public ForStmt getInitForStmt() {
        return initForStmt;
    }

    public void setInitForStmt(ForStmt initForStmt) {
        this.initForStmt = initForStmt;
    }

    public Cond getCond() {
        return cond;
    }

    public void setCond(Cond cond) {
        this.cond = cond;
    }

    public ForStmt getForStmt() {
        return forStmt;
    }

    public void setForStmt(ForStmt forStmt) {
        this.forStmt = forStmt;
    }

    public Stmt getStmt() {
        return stmt;
    }

    public void setStmt(Stmt stmt) {
        this.stmt = stmt;
    }

    @Override
    public void genLLVM() {
        IrBr irBr;
        if (initForStmt != null) {
            initForStmt.genLLVM();
        }
        // 条件语句开头设置标签
        curIrFunction.allocBasicBlock();
        Value condLabel = curIrBasicBlock;
        if (cond != null) {
            cond.genLLVM(irBrLLVMCode, "!stmtLabel", "!forEndLabel");
        }
        // 设置Stmt开头的Label并重填 解析Stmt
        curIrFunction.allocBasicBlock();
        backFillBrLLVMCode(irBrLLVMCode, curIrBasicBlock, "!stmtLabel");
        // 针对continue,break做出处理
        stmt.genLLVM();
        // 设置forStmt的Label并重填，解析forStmt
        curIrFunction.allocBasicBlock();
        backFillBrLLVMCode(irBrLLVMCode, curIrBasicBlock, "!forStmtLabel");
        if (forStmt != null) {
            forStmt.genLLVM();
        }
        // 跳转到CondLabel
        irBr = new IrBr(condLabel);
        curIrBasicBlock.addInstruction(irBr);
        irBrLLVMCode.add(irBr);
        // 循环语句结束
        curIrFunction.allocBasicBlock();
        backFillBrLLVMCode(irBrLLVMCode, curIrBasicBlock, "!forEndLabel");
    }
}
