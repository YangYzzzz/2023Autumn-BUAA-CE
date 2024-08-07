package FrontEnd.NonTerminal;

import MidEnd.IrCore.IrInstruction;
import MidEnd.IrInstuctions.IrMul;
import MidEnd.IrInstuctions.IrSdiv;
import MidEnd.IrInstuctions.IrSrem;
import MidEnd.IrInstuctions.IrSub;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

import static MidEnd.IrCore.IrModule.MODE;
import static MidEnd.IrCore.IrModule.curIrBasicBlock;
import static MidEnd.IrCore.IrModule.curIrFunction;

public class MulExp {
    private ArrayList<UnaryExp> unaryExps;
    private ArrayList<Sign> signs;

    public MulExp() {
        unaryExps = new ArrayList<>();
        signs = new ArrayList<>();
    }

    public void addUnaryAndSign(UnaryExp unaryExp, Sign sign) {
        unaryExps.add(unaryExp);
        signs.add(sign);
    }

    public void addUnaryExp(UnaryExp unaryExp) {
        unaryExps.add(unaryExp);
    }

    public boolean hasSingleLVal() {
        if (unaryExps.size() != 1) {
            return false;
        } else {
            return unaryExps.get(0).hasSingleLVal();
        }
    }

    public LVal getSingleLVal() {
        return unaryExps.get(0).getSingleLVal();
    }

    public ArrayList<Ident> getAllFuncIdent() {
        ArrayList<Ident> funcIdents = new ArrayList<>();
        for (UnaryExp unaryExp : unaryExps) {
            if (unaryExp.getFuncIdent() != null) {
                funcIdents.add(unaryExp.getFuncIdent());
            }
        }
        return funcIdents;
    }

    public int calMulExp() {
        int constValue = unaryExps.get(0).calUnaryExp();
        for (int i = 1; i < unaryExps.size(); i++) {
            // System.out.println("被除数 " + constValue+  ",除数 " + unaryExps.get(i).calUnaryExp());
            switch (signs.get(i - 1)) {
                case MULT -> constValue *= unaryExps.get(i).calUnaryExp();
                case DIV -> constValue /= unaryExps.get(i).calUnaryExp();
                case MOD -> constValue %= unaryExps.get(i).calUnaryExp();
                default -> System.out.println("计算符号出错");
            }
        }
        return constValue;
    }

    public Value genLLVM() {
        Value op1 = unaryExps.get(0).genLLVM();
        IrInstruction irInstruction = null;
        for (int i = 1; i < unaryExps.size(); i++) {
            Value op2 = unaryExps.get(i).genLLVM();
            // 结果的返回值
            String re = curIrFunction.allocVirtualReg();
            switch (signs.get(i - 1)) {
                case MULT: {
                    if (op1.isNum() && op2.isNum()) {
                        int result = Integer.parseInt(op1.value) * Integer.parseInt(op2.value);
                        op1 = new Value(String.valueOf(result), 0);
                    } else if ((op1.isNum() && Integer.parseInt(op1.value) == 0)
                            || (op2.isNum() && Integer.parseInt(op2.value) == 0)) {
                        op1 = new Value("0", 0);
                    } else if (op1.isNum() && Integer.parseInt(op1.value) == 1) {
                        op1 = op2;
                    } else {
                        irInstruction = new IrMul(re, op1, op2);
                        curIrBasicBlock.addInstruction(irInstruction);
                        op1 = irInstruction;
                    }
                    /*
                        这么优化会出现问题，为何？？？
                        else if (op2.isNum() && Integer.parseInt(op2.value) == 1) {
                            op1 = op1;
                        }
                     */
                    break;
                }
                case DIV: {
                    if (op1.isNum() && op2.isNum()) {
                        int result = Integer.parseInt(op1.value) / Integer.parseInt(op2.value);
                        op1 = new Value(String.valueOf(result), 0);
                    } else if (op1.isNum() && Integer.parseInt(op1.value) == 0) {
                        op1 = new Value("0", 0);
                    } else if (op2.isNum() && Integer.parseInt(op2.value) == 1) {
                        op1 = op1;
                    } else {
                        irInstruction = new IrSdiv(re, op1, op2);
                        curIrBasicBlock.addInstruction(irInstruction);
                        op1 = irInstruction;
                    }
                    break;
                }
                case MOD: {
                    if (op1.isNum() && op2.isNum()) {
                        int result = Integer.parseInt(op1.value) % Integer.parseInt(op2.value);
                        op1 = new Value(String.valueOf(result), 0);
                    } else {
                        // 模运算要拆成多个子式子 a % b === a - a / b * b 三个操作
                        if (MODE == 0) {
                            irInstruction = new IrSrem(re, op1, op2);
                            curIrBasicBlock.addInstruction(irInstruction);
                            op1 = irInstruction;
                        } else {
                            if (op1.isNum() && Integer.parseInt(op1.value) == 0) {
                                op1 = new Value("0", 0);
                            } else {
                                irInstruction = new IrSdiv(re, op1, op2);
                                curIrBasicBlock.addInstruction(irInstruction);
                                String re2 = curIrFunction.allocVirtualReg();
                                irInstruction = new IrMul(re2, irInstruction, op2);
                                curIrBasicBlock.addInstruction(irInstruction);
                                String re3 = curIrFunction.allocVirtualReg();
                                irInstruction = new IrSub(re3, op1, irInstruction);
                                curIrBasicBlock.addInstruction(irInstruction);
                                op1 = irInstruction;
                            }
                        }
                    }
                    break;
                }
            }
        }
        return op1;
    }
}
