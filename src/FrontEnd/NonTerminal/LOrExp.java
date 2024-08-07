package FrontEnd.NonTerminal;

import MidEnd.IrInstuctions.IrBr;

import java.util.ArrayList;

import static MidEnd.IrCore.IrModule.curIrBasicBlock;
import static MidEnd.IrCore.IrModule.curIrFunction;
import static MidEnd.Tools.LLVMCode.backFillBrLLVMCode;

public class LOrExp {
    private ArrayList<LAndExp> lAndExps;

    public LOrExp() {
        lAndExps = new ArrayList<>();
    }

    public void addLAndExp(LAndExp lAndExp) {
        lAndExps.add(lAndExp);
    }

    public void genLLVM(ArrayList<IrBr> irBrLLVMCode, String fakeLabel1, String fakeLabel2) {
        /*
            共需要记录
            1. StmtLabel: !1
            2. ElseStmtLabel: !2
            3. IfEndLabel: !3
            4.
            全局记录
            1. 后一条 &&: 真 -> 后一条 && 的 Label; 假 -> 下一个 || 的Label （如果有 需要记录）
            2. 后一条 ||: 真 -> Stmt 的 Label; 假 -> 后一条 || 的Label
            3. 后一条 无: 真 -> Stmt 的 Label; 假 -> elseStmt 的Label
         */
        for (int i = 0; i < lAndExps.size(); i++) {
            LAndExp lAndExp = lAndExps.get(i);
            if (i != 0) {
                curIrFunction.allocBasicBlock();
                backFillBrLLVMCode(irBrLLVMCode, curIrBasicBlock, "!nextOrLabel");
            }
            if (i != lAndExps.size() - 1) {
                // 最后一条有下一个||
                lAndExp.genLLVM(irBrLLVMCode, 1, fakeLabel1, fakeLabel2);
            } else {
                // 没有有下一个||
                lAndExp.genLLVM(irBrLLVMCode, 0, fakeLabel1, fakeLabel2);
            }
        }
    }

}
