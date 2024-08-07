package FrontEnd.NonTerminal;

import FrontEnd.SymbolTable.Dimension1ArraySymbol;
import FrontEnd.SymbolTable.Dimension2ArraySymbol;
import FrontEnd.SymbolTable.Symbol;
import MidEnd.IrCore.IrGlobalDef;
import MidEnd.IrCore.IrInstruction;
import MidEnd.IrInstuctions.IrAlloc;
import MidEnd.IrInstuctions.IrGetElementPtr;
import MidEnd.IrInstuctions.IrStore;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

import static FrontEnd.SymbolTable.StaticSymbolTable.curSymbolId;
import static FrontEnd.SymbolTable.StaticSymbolTable.getSymbol;
import static MidEnd.IrCore.IrModule.MODE;
import static MidEnd.IrCore.IrModule.curIrBasicBlock;
import static MidEnd.IrCore.IrModule.curIrFunction;
import static MidEnd.IrCore.IrModule.irModule;
import static MidEnd.SymbolTable.IrSymbolTable.irSymbolTable;

public class ConstDef {
    private Ident ident;
    // 存放各维度大小
    private ArrayList<ConstExp> constExps;


    // 初值暂且直接等同于表达式 存放初值 顺序存入即可
    private ConstInitVal constInitVal;

    public ConstDef() {
        constExps = new ArrayList<>();
    }

    public Ident getIdent() {
        return ident;
    }

    public void setIdent(Ident ident) {
        this.ident = ident;
    }

    public ConstInitVal getConstInitVal() {
        return constInitVal;
    }

    public void setConstInitVal(ConstInitVal constInitVal) {
        this.constInitVal = constInitVal;
    }
    public int getArrDim() {
        return constExps.size();
    }
    public void addConstExp(ConstExp constExp) {
        constExps.add(constExp);
    }

    public void genGlobalLLVM() {
        // @a = dso_local global [10 x i32] [i32 1, i32 2, i32 3, i32 0, i32 0, i32 0, i32 0, i32 0, i32 0, i32 0]
        //@b = dso_local global [10 x [20 x i32]] zeroinitializer
        //@c = dso_local global [5 x [5 x i32]] [[5 x i32] [i32 1, i32 2, i32 3, i32 0, i32 0], [5 x i32] [i32 1, i32 2, i32 3, i32 4, i32 5], [5 x i32] zeroinitializer, [5 x i32] zeroinitializer, [5 x i32] zeroinitializer]
        //@a = dso_local global i32 1, align 4
        // 常量一定有初始化值存在！
        Symbol symbol = getSymbol(ident.getName());
        IrGlobalDef irGlobalDef = null;
        ArrayList<Integer> initials = new ArrayList<>();
        // 一维数组
        if (constExps.isEmpty()) {
            int initial = constInitVal.calConstInitVal();
            symbol.setInitVal(initial);
            initials.add(initial);
            irGlobalDef = new IrGlobalDef("@"+ident.getName(), initials);
            irModule.addGlobals(irGlobalDef);
        } else if (constExps.size() == 1) {
            int dim = constExps.get(0).calConstExp();
            ((Dimension1ArraySymbol) symbol).setDimSize(dim);
            initials = constInitVal.calArr1ConstInitVal();
            ((Dimension1ArraySymbol) symbol).setInitials(initials);
            irGlobalDef = new IrGlobalDef("@"+ident.getName(), dim, initials);
            irModule.addGlobals(irGlobalDef);
        } else if (constExps.size() == 2) {
            int dim1 = constExps.get(0).calConstExp();
            int dim2 = constExps.get(1).calConstExp();
            initials = constInitVal.calArr2ConstInitVal();
            ((Dimension2ArraySymbol) symbol).setInitials(initials);
            irGlobalDef = new IrGlobalDef("@"+ident.getName(), dim1, dim2, initials);
            irModule.addGlobals(irGlobalDef);
        }
        // 设置标识符
        symbol.setIdentifier(irGlobalDef);
    }

    // 和VarDef的实现极其相似，可继承
    public void genLLVM() {
        Symbol symbol = getSymbol(ident.getName(), 0);
        curSymbolId = symbol.getId();
        String addrStr = curIrFunction.allocVirtualReg();
        IrInstruction irInstruction;
        Value addr = null;
        int initial = 0;
        switch (symbol.getType()) {
            case 0: {
                if (MODE == 1) {
                    // 优化，无需alloc和store 需要记录真实值Value和定义的基
                    initial = constInitVal.calConstInitVal();
                    // 加入ir符号表
                    irSymbolTable.addIrSymbol(symbol);
                    irSymbolTable.addBasicBlockDef(symbol, new Value(String.valueOf(initial), 0));
                    symbol.setInitVal(initial);
                } else {
                    irInstruction = new IrAlloc(addrStr);
                    addr = irInstruction;
                    curIrBasicBlock.addInstruction(irInstruction);
                    initial = constInitVal.calConstInitVal();
                    irInstruction = new IrStore(new Value(String.valueOf(initial), 0), addr);
                    curIrBasicBlock.addInstruction(irInstruction);
                    symbol.setInitVal(initial);
                }
                break;
            }
            case 1: {
                int dim = constExps.get(0).calConstExp();
                irInstruction = new IrAlloc(addrStr, dim);
                curIrBasicBlock.addInstruction(irInstruction);
                addr = irInstruction;
                ArrayList<Integer> initials = new ArrayList<>();
                for (int i = 0; i < dim; i++) {
                    // 应当是和dim对齐的
                    initial = constInitVal.getConstInitVals().get(i).calConstInitVal();
                    initials.add(initial);
                    if (initial == 0) {
                        continue;
                    }
                    String tarAddr = curIrFunction.allocVirtualReg();
                    irInstruction = new IrGetElementPtr(tarAddr, addr, new Value("0", 0), new Value(String.valueOf(i), 0));
                    curIrBasicBlock.addInstruction(irInstruction);
                    irInstruction = new IrStore(new Value(String.valueOf(initial), 0), irInstruction);
                    curIrBasicBlock.addInstruction(irInstruction);
                }
                ((Dimension1ArraySymbol) symbol).setInitials(initials);
                break;
            }
            case 2: {
                int dim1 = constExps.get(0).calConstExp();
                int dim2 = constExps.get(1).calConstExp();
                irInstruction = new IrAlloc(addrStr, dim1, dim2);
                curIrBasicBlock.addInstruction(irInstruction);
                addr = irInstruction;
                ArrayList<Integer> initials = new ArrayList<>();
                for (int i = 0; i < dim1; i++) {
                    for (int j = 0; j < dim2; j++) {
                        initial = constInitVal.getConstInitVals().get(i).getConstInitVals().get(j).calConstInitVal();
                        initials.add(initial);
                        if (initial == 0) {
                            continue;
                        }
                        String tarAddr = curIrFunction.allocVirtualReg();
                        irInstruction = new IrGetElementPtr(tarAddr, addr, new Value("0", 0),new Value(String.valueOf(i), 0), new Value(String.valueOf(j), 0));
                        curIrBasicBlock.addInstruction(irInstruction);
                        irInstruction = new IrStore(new Value(String.valueOf(initial), 0), irInstruction);
                        curIrBasicBlock.addInstruction(irInstruction);
                    }
                }
                ((Dimension2ArraySymbol) symbol).setInitials(initials);
                break;
            }
        }
        // 将基地址保存至符号表 所有变量通过读存内存地址进行交互
        symbol.setIdentifier(addr);
    }
}
