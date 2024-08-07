package FrontEnd.NonTerminal;

import FrontEnd.SymbolTable.FuncSymbol;
import MidEnd.IrCore.IrFunction;
import MidEnd.IrCore.IrInstruction;
import MidEnd.IrInstuctions.IrCall;
import MidEnd.IrInstuctions.IrCmp;
import MidEnd.IrInstuctions.IrSub;
import MidEnd.IrInstuctions.IrZext;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

import static FrontEnd.SymbolTable.StaticSymbolTable.getSymbol;
import static MidEnd.IrCore.IrModule.curIrBasicBlock;
import static MidEnd.IrCore.IrModule.curIrFunction;

public class UnaryExp {
    // 标识该类是属于第几个分支 总共三个分支
    private int flag;
    // 1
    private PrimaryExp primaryExp_0;
    private Ident ident_1;
    // 2
    private FuncRParams funcRParams_1;
    // 3
    private UnaryExp unaryExp_2;
    private UnaryOp unaryOp_2;

    public UnaryExp() {
        // 可能会删除
        funcRParams_1 = new FuncRParams();
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public PrimaryExp getPrimaryExp_0() {
        return primaryExp_0;
    }

    public void setPrimaryExp_0(PrimaryExp primaryExp_0) {
        this.primaryExp_0 = primaryExp_0;
    }

    public Ident getIdent_1() {
        return ident_1;
    }

    public void setIdent_1(Ident ident_1) {
        this.ident_1 = ident_1;
    }

    public FuncRParams getFuncFParams_1() {
        return funcRParams_1;
    }

    public void setFuncFParams_1(FuncRParams funcRParams_1) {
        this.funcRParams_1 = funcRParams_1;
    }

    public UnaryExp getUnaryExp_2() {
        return unaryExp_2;
    }

    public void setUnaryExp_2(UnaryExp unaryExp_2) {
        this.unaryExp_2 = unaryExp_2;
    }

    public UnaryOp getUnaryOp_2() {
        return unaryOp_2;
    }

    public void setUnaryOp_2(UnaryOp unaryOp_2) {
        this.unaryOp_2 = unaryOp_2;
    }

    public boolean hasSingleLVal() {
        return switch (flag) {
            case 0 -> primaryExp_0.hasSingleLVal();
//            case 2 -> unaryExp_2.hasSingleLVal();
            default -> false;
        };
    }

    public LVal getSingleLVal() {
        return switch (flag) {
            case 0 -> primaryExp_0.getSingleLVal();
            case 2 -> unaryExp_2.getSingleLVal();
            default -> null;
        };
    }

    public Ident getFuncIdent() {
        if (flag == 1) {
            return ident_1;
        } else {
            return null;
        }
    }

    public int calUnaryExp() {
        int constValue = 0;
        if (flag == 0) {
            // primary
            constValue = primaryExp_0.calPrimaryExp();
        } else if (flag == 2) {
            if (unaryOp_2.getSign() == Sign.MINU) {
                constValue = - unaryExp_2.calUnaryExp();
            } else {
                constValue = unaryExp_2.calUnaryExp();
            }
        }
        return constValue;
    }

    public Value genLLVM() {
        IrInstruction irInstruction;
        switch (flag) {
            case 0: {
                // primaryExp
                return primaryExp_0.genLLVM();
            }
            case 1: {
                // 函数调用
                FuncSymbol symbol = (FuncSymbol) getSymbol(ident_1.getName());
                curIrFunction.addCallFunctions((IrFunction) symbol.getIdentifier());
                // 参数分两种 一种是数值型参数 一种是地址型参数 地址型参数又分为两类 * 和 [3 x i32]*
                ArrayList<Value> params = funcRParams_1.genLLVM();
                switch (symbol.getRetype()) {
                    case 0: {
                        // int
                        String re = curIrFunction.allocVirtualReg();
                        irInstruction = new IrCall(re, symbol.getIdentifier(), params);
                        curIrBasicBlock.addInstruction(irInstruction);
                        return irInstruction;
                    }
                    case 1: {
                        // void
                        irInstruction = new IrCall(symbol.getIdentifier(), params);
                        curIrBasicBlock.addInstruction(irInstruction);
                        return null;
                    }
                }
            }
            case 2: {
                // 皆返回 i32
                if (unaryOp_2.getSign() == Sign.MINU) {
                    Value op = unaryExp_2.genLLVM();
                    if (op.isNum()) {
                        return new Value(String.valueOf(-Integer.parseInt(op.value)), 0);
                    } else {
                        String value = curIrFunction.allocVirtualReg();
                        irInstruction = new IrSub(value, new Value("0", 0), op);
                        curIrBasicBlock.addInstruction(irInstruction);
                        return irInstruction;
                    }
                } else if (unaryOp_2.getSign() == Sign.PLUS) {
                    return unaryExp_2.genLLVM();
                } else {
                    // 在Cond里会出现 ! 的情况 !%x ===
                    Value cmp = unaryExp_2.genLLVM();
                    String value = curIrFunction.allocVirtualReg();
                    irInstruction = new IrCmp(value, cmp, new Value("0", 0), Sign.EQL);
                    curIrBasicBlock.addInstruction(irInstruction);
                    // i1 --> i32
                    String zextedValue = curIrFunction.allocVirtualReg();
                    irInstruction = new IrZext(zextedValue, irInstruction);
                    curIrBasicBlock.addInstruction(irInstruction);
                    return irInstruction;
                }
            }
            default: {
                return null;
            }
        }
    }
}
