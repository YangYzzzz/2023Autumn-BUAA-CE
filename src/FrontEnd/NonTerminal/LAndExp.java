package FrontEnd.NonTerminal;

import MidEnd.IrInstuctions.IrBr;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

import static MidEnd.IrCore.IrModule.curIrBasicBlock;
import static MidEnd.IrCore.IrModule.curIrFunction;
import static MidEnd.Tools.LLVMCode.backFillBrLLVMCode;

public class LAndExp {
    // 由于只有一种符号 因此只用一个列表即可
    private ArrayList<EqExp> eqExps;

    public LAndExp() {
        eqExps = new ArrayList<>();
    }

    public void addEqExp(EqExp eqExp) {
        eqExps.add(eqExp);
    }

    public void genLLVM(ArrayList<IrBr> irBrLLVMCode, int flag, String fakeLabel1, String fakeLabel2) {
        /*
            全局记录
            1. 后一条 &&: 真 -> 后一条 && 的 Label; 假 -> 下一个 || 的Label （如果有 需要记录）
            2. 后一条 ||: 真 -> Stmt 的 Label; 假 -> 后一条 || 的Label
            3. 后一条 无: 真 -> Stmt 的 Label; 假 -> elseStmt 的Label
            ---------------------------------------------------------
            对于If语句而言 fakeLabel1是!stmtLabel fakeLabel2是!elseStmtLabel
            对于For语句而言 fakeLabel1是!stmtLabel fakeLabel2是!forEndLabel
         */
        for (int i = 0; i < eqExps.size(); i++) {
            EqExp eqExp = eqExps.get(i);
            if (i != 0) {
                curIrFunction.allocBasicBlock();
                backFillBrLLVMCode(irBrLLVMCode, curIrBasicBlock, "!nextAndLabel");
            }
            // 考虑情况 1 == 0 == 1 多个等式的判断
            Value value = eqExp.genLLVM();
            IrBr instruction;
            if (flag == 0 && i == eqExps.size() - 1) {
                // System.out.println("只有一项Cond表达式 " + fakeLabel1 + " " + fakeLabel2);
                // 是||的最后一个也是&&的最后一个 即后继无人
                instruction = new IrBr(value, fakeLabel1, fakeLabel2);
            } else if (flag == 1 && i == eqExps.size() - 1) {
                // 不是||的最后一个 是&&的最后一个 后继是||
                instruction = new IrBr(value, fakeLabel1, "!nextOrLabel");
            } else if (flag == 1 && i != eqExps.size() - 1){
                // 不是||的最后一个 不是&&的最后一个 后继是&&
                instruction = new IrBr(value, "!nextAndLabel", "!nextOrLabel");
            } else {
                // 是||的最后一个 不是&&的最后一个 后继是&&
                instruction = new IrBr(value, "!nextAndLabel", fakeLabel2);
            }
            curIrBasicBlock.addInstruction(instruction);
            irBrLLVMCode.add(instruction);
        }
    }
}
