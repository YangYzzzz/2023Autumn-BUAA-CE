package FrontEnd.NonTerminal;

import MidEnd.IrCore.IrInstruction;
import MidEnd.IrInstuctions.IrCmp;
import MidEnd.IrInstuctions.IrZext;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

import static MidEnd.IrCore.IrModule.curIrBasicBlock;
import static MidEnd.IrCore.IrModule.curIrFunction;

public class RelExp {
    private ArrayList<AddExp> addExps;
    // > < >= <=
    private ArrayList<Sign> signs;

    public RelExp() {
        addExps = new ArrayList<>();
        signs = new ArrayList<>();
    }


    public void addAddExpAndSign(AddExp addExp, Sign sign) {
        addExps.add(addExp);
        signs.add(sign);
    }

    public void addAddExp(AddExp addExp) {
        addExps.add(addExp);
    }

    // 返回i32值
    public Value genLLVM() {
        // 仅一个元素的时候 需要特殊考虑?
        Value cmp1 = addExps.get(0).genLLVM();
        // System.out.println("等号两边" + cmp1.value);
        String zextValue;
        IrInstruction irInstruction;
        for (int i = 1; i < addExps.size(); i++) {
            Value cmp2 = addExps.get(i).genLLVM();
            if (cmp1.isNum() && cmp2.isNum()) {
                int num1 = Integer.parseInt(cmp1.value);
                int num2 = Integer.parseInt(cmp2.value);
                switch (signs.get(i - 1)) {
                    case GEQ: {
                        if (num1 >= num2) {
                            cmp1 = new Value("1", 0);
                        } else {
                            cmp1 = new Value("0", 0);
                        }
                        break;
                    }
                    case LEQ: {
                        if (num1 <= num2) {
                            cmp1 = new Value("1", 0);
                        } else {
                            cmp1 = new Value("0", 0);
                        }
                        break;
                    }
                    case GRE: {
                        if (num1 > num2) {
                            cmp1 = new Value("1", 0);
                        } else {
                            cmp1 = new Value("0", 0);
                        }
                        break;
                    }
                    case LSS: {
                        if (num1 < num2) {
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
                zextValue = curIrFunction.allocVirtualReg();
                // 将value转化为i32当作下一轮循环的cmp1 i1 -> i32
                irInstruction = new IrZext(zextValue, irInstruction);
                curIrBasicBlock.addInstruction(irInstruction);
                cmp1 = irInstruction;
            }
        }
        return cmp1;
    }
}
