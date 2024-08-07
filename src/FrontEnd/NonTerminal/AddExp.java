package FrontEnd.NonTerminal;

import MidEnd.IrCore.IrInstruction;
import MidEnd.IrInstuctions.IrAdd;
import MidEnd.IrInstuctions.IrSub;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

import static MidEnd.IrCore.IrModule.curIrBasicBlock;
import static MidEnd.IrCore.IrModule.curIrFunction;

public class AddExp {
    // 等价于Exp 优化掉Exp
    private ArrayList<MulExp> mulExps;
    // 后续可优化为枚举类型
    private ArrayList<Sign> signs;

    public AddExp() {
        this.mulExps = new ArrayList<>();
        this.signs = new ArrayList<>();
    }

    public int calAddExp() {
        int constValue = mulExps.get(0).calMulExp();
        for (int i = 1; i < mulExps.size(); i++) {
            switch (signs.get(i - 1)) {
                case PLUS -> constValue += mulExps.get(i).calMulExp();
                case MINU -> constValue -= mulExps.get(i).calMulExp();
                default -> System.out.println("符号出错");
            }
        }
        return constValue;
    }

    public void addMulAndSign(MulExp mulExp, Sign sign) {
        mulExps.add(mulExp);
        signs.add(sign);
    }

    public void addMulExp(MulExp mulExp) {
        mulExps.add(mulExp);
    }

    public boolean hasSingleLVal() {
        if (mulExps.size() != 1) {
            return false;
        } else {
            return mulExps.get(0).hasSingleLVal();
        }
    }

    public LVal getSingleLVal() {
        return mulExps.get(0).getSingleLVal();
    }

    public ArrayList<Ident> getAllFuncIdent() {
        ArrayList<Ident> funcIdents = new ArrayList<>();
        for (MulExp mulExp : mulExps) {
            funcIdents.addAll(mulExp.getAllFuncIdent());
        }
        return funcIdents;
    }

    public Value genLLVM() {
        // 所有层皆两种可能 返回Value或者返回一个int 该如何选择 返回一个字符串就可以
        // 3 + 3 * 2 - 3 / 4
        // 文法转化 value1 是初值
        Value op1 = mulExps.get(0).genLLVM();
        IrInstruction irInstruction = null;
        for (int i = 1; i < mulExps.size(); i++) {
            Value op2 = mulExps.get(i).genLLVM();
            // 结果的返回值
            String re = curIrFunction.allocVirtualReg();
            switch (signs.get(i - 1)) {
                case PLUS: {
                    if (op1.isNum() && op2.isNum()) {
                        int result = Integer.parseInt(op1.value) + Integer.parseInt(op2.value);
                        op1 = new Value(String.valueOf(result), 0);
                    } else if (op1.isNum() && Integer.parseInt(op1.value) == 0) {
                        op1 = op2;
                    } else if (op2.isNum() && Integer.parseInt(op2.value) == 0) {
                        op1 = op1;
                    } else {
                        irInstruction = new IrAdd(re, op1, op2);
                        curIrBasicBlock.addInstruction(irInstruction);
                        op1 = irInstruction;
                    }
                    break;
                }
                case MINU: {
                    if (op1.isNum() && op2.isNum()) {
                        int result = Integer.parseInt(op1.value) - Integer.parseInt(op2.value);
                        op1 = new Value(String.valueOf(result), 0);
                    } else if (op2.isNum() && Integer.parseInt(op2.value) == 0) {
                        op1 = op1;
                    }  else {
                        irInstruction = new IrSub(re, op1, op2);
                        curIrBasicBlock.addInstruction(irInstruction);
                        op1 = irInstruction;
                    }
                    break;
                }
            }
        }
        return op1;
    }
}
