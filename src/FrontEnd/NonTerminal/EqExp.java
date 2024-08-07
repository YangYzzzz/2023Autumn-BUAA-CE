package FrontEnd.NonTerminal;

import MidEnd.IrCore.IrInstruction;
import MidEnd.IrInstuctions.IrCmp;
import MidEnd.IrInstuctions.IrZext;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

import static MidEnd.IrCore.IrModule.curIrBasicBlock;
import static MidEnd.IrCore.IrModule.curIrFunction;

public class EqExp {
    private ArrayList<RelExp> relExps;
    private ArrayList<Sign> signs;

    public EqExp() {
        relExps = new ArrayList<>();
        signs = new ArrayList<>();
    }

    public void addRelAndSign(RelExp relExp, Sign sign) {
        relExps.add(relExp);
        signs.add(sign);
    }

    public void addRelExp(RelExp relExp) {
        relExps.add(relExp);
    }

    // 需要返回 i1 值
    public Value genLLVM() {
        // i32
        Value cmp1 = relExps.get(0).genLLVM();
        IrInstruction irInstruction;
        for (int i = 1; i < relExps.size(); i++) {
            // i32
            Value cmp2 = relExps.get(i).genLLVM();
            if (cmp1.isNum() && cmp2.isNum()) {
                int num1 = Integer.parseInt(cmp1.value);
                int num2 = Integer.parseInt(cmp2.value);
                switch (signs.get(i - 1)) {
                    case EQL: {
                        if (num1 == num2) {
                            cmp1 = new Value("1", 0);
                        } else {
                            cmp1 = new Value("0", 0);
                        }
                        break;
                    }
                    case NEQ: {
                        if (num1 != num2) {
                            cmp1 = new Value("1", 0);
                        } else {
                            cmp1 = new Value("0", 0);
                        }
                        break;
                    }
                }
            } else {
                String value = curIrFunction.allocVirtualReg();
                irInstruction = new IrCmp(value, cmp1, cmp2, signs.get(i - 1));
                curIrBasicBlock.addInstruction(irInstruction);
//                if (MODE == 0) {
                    // 消除zext
                    String zextValue = curIrFunction.allocVirtualReg();
                    // i1 -> i32
                    irInstruction = new IrZext(zextValue, irInstruction);
                    curIrBasicBlock.addInstruction(irInstruction);
//                }
                cmp1 = irInstruction;
            }
        }
        // 用cmp ne 0 --> 令所有非零的i32转化为i1 1; 零的i32转化为i1 0
        if (cmp1.isNum()) {
            int num = Integer.parseInt(cmp1.value);
            if (num != 0) {
                return new Value("1", 0);
            } else {
                return new Value("0", 0);
            }
        } else {
            String reValue = curIrFunction.allocVirtualReg();
            irInstruction = new IrCmp(reValue, cmp1, new Value("0", 0), Sign.NEQ);
            curIrBasicBlock.addInstruction(irInstruction);
            return irInstruction;
        }
    }
}
