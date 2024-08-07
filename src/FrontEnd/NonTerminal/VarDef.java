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

public class VarDef {
    private Ident ident;
    // 0维 到 2维
    private ArrayList<ConstExp> constExps;
    private InitVal initVal;

    public VarDef() {
        constExps = new ArrayList<>();
    }

    public Ident getIdent() {
        return ident;
    }

    public void setIdent(Ident ident) {
        this.ident = ident;
    }

    public void addConstExp(ConstExp constExp) {
        constExps.add(constExp);
    }

    public InitVal getInitVal() {
        return initVal;
    }

    public int getArrDim() {
        return constExps.size();
    }

    public void setInitVal(InitVal initVal) {
        this.initVal = initVal;
    }

    public void genGlobalLLVM() {
        Symbol symbol = getSymbol(ident.getName(), 0);
        symbol.setGlobal(1);
        IrGlobalDef irGlobalDef = null;
        ArrayList<Integer> initials = new ArrayList<>();
        // 一维数组
        if (constExps.isEmpty()) {
            if (initVal != null) {
                initials.add(initVal.calInitVal());
                irGlobalDef = new IrGlobalDef("@"+ident.getName(), initials);
                irModule.addGlobals(irGlobalDef);
            } else {
                initials.add(0);
                irGlobalDef = new IrGlobalDef("@"+ident.getName(), initials);
                irModule.addGlobals(irGlobalDef);
            }
        } else if (constExps.size() == 1) {
            int dim = constExps.get(0).calConstExp();
            ((Dimension1ArraySymbol) symbol).setDimSize(dim);
            if (initVal != null) {
                initials = initVal.calArr1InitVal();
            } else {
                initials = new ArrayList<>();
                for (int i = 0; i < dim; i++) {
                    initials.add(0);
                }
            }
            ((Dimension1ArraySymbol) symbol).setInitials(initials);
            irGlobalDef = new IrGlobalDef("@"+ident.getName(), dim, initials);
            irModule.addGlobals(irGlobalDef);
        } else if (constExps.size() == 2) {
            int dim1 = constExps.get(0).calConstExp();
            int dim2 = constExps.get(1).calConstExp();
            if (initVal != null) {
                initials = initVal.calArr2InitVal();
            } else {
                initials = new ArrayList<>();
                for (int i = 0; i < dim1 * dim2; i++) {
                    initials.add(0);
                }
            }
            ((Dimension2ArraySymbol) symbol).setInitials(initials);
            irGlobalDef = new IrGlobalDef("@"+ident.getName(), dim1, dim2, initials);
            irModule.addGlobals(irGlobalDef);
        }
        // 设置标识符
        symbol.setIdentifier(irGlobalDef);
    }


    public void genLLVM() {
        /*
            int a=1+2;
            %1 = alloca i32 地址
            %2 = add i32 1, 2 值
            store i32 %2, i32* %1 将值存入地址
         */
        Symbol symbol = getSymbol(ident.getName(), 0);
        curSymbolId = symbol.getId();
        String addrStr = curIrFunction.allocVirtualReg();
        IrInstruction irInstruction;
        Value addr = null;
        Value value;
        switch (symbol.getType()) {
            case 0: {
                if (MODE == 1) {
                    // 优化，无需alloc和store 需要记录真实值Value和定义的基本块
                    if (initVal != null) {
                        value = initVal.genLLVM();
                    } else {
                        value = new Value("0", 0);
                    }
                    // 加入ir符号表
                    irSymbolTable.addIrSymbol(symbol);
                    irSymbolTable.addBasicBlockDef(symbol, value);
                } else {
                    irInstruction = new IrAlloc(addrStr);
                    addr = irInstruction;
                    curIrBasicBlock.addInstruction(irInstruction);
                    if (initVal != null) {
                        value = initVal.genLLVM();
                        curIrBasicBlock.addInstruction(new IrStore(value, addr));
                    }
                }
                break;
            }
            case 1: {
                int dim = constExps.get(0).calConstExp();
                ((Dimension1ArraySymbol) symbol).setDimSize(dim);
                irInstruction = new IrAlloc(addrStr, dim);
                addr = irInstruction;
                curIrBasicBlock.addInstruction(irInstruction);
                if (initVal == null) {
                    break;
                }
                for (int i = 0; i < dim; i++) {
                    // 应当是和dim对齐的
                    value = initVal.getInitVals().get(i).genLLVM();
                    String tarAddrStr = curIrFunction.allocVirtualReg();
                    irInstruction = new IrGetElementPtr(tarAddrStr, addr, new Value("0", 0), new Value(String.valueOf(i), 0));
                    curIrBasicBlock.addInstruction(irInstruction);
                    curIrBasicBlock.addInstruction(new IrStore(value, irInstruction));
                }
                break;
            }
            case 2: {
                int dim1 = constExps.get(0).calConstExp();
                int dim2 = constExps.get(1).calConstExp();
                irInstruction = new IrAlloc(addrStr, dim1, dim2);
                addr = irInstruction;
                curIrBasicBlock.addInstruction(irInstruction);
                if (initVal == null) {
                    break;
                }
                for (int i = 0; i < dim1; i++) {
                    for (int j = 0; j < dim2; j++) {
                        // 要么不填 要么都填满 暂且认为这样 后续可优化
                        value = initVal.getInitVals().get(i).getInitVals().get(j).genLLVM();
                        String tarAddr = curIrFunction.allocVirtualReg();
                        irInstruction = new IrGetElementPtr(tarAddr, addr, new Value("0", 0), new Value(String.valueOf(i), 0), new Value(String.valueOf(j
                        ), 0));
                        curIrBasicBlock.addInstruction(irInstruction);
                        curIrBasicBlock.addInstruction(new IrStore(value, irInstruction));
                    }
                }
                break;
            }
        }
        // 将基地址保存至符号表 所有变量通过读存内存地址进行交互
        symbol.setIdentifier(addr);
    }
}
