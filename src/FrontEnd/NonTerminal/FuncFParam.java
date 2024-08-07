package FrontEnd.NonTerminal;

import FrontEnd.SymbolTable.Symbol;
import MidEnd.IrCore.IrInstruction;
import MidEnd.IrInstuctions.IrAlloc;
import MidEnd.IrInstuctions.IrStore;

import java.util.ArrayList;

import static FrontEnd.SymbolTable.StaticSymbolTable.curSymbolId;
import static FrontEnd.SymbolTable.StaticSymbolTable.getSymbol;
import static MidEnd.IrCore.IrModule.MODE;
import static MidEnd.IrCore.IrModule.curIrBasicBlock;
import static MidEnd.IrCore.IrModule.curIrFunction;
import static MidEnd.SymbolTable.IrSymbolTable.irSymbolTable;

public class FuncFParam {
    private BType bType; // int
    private Ident ident;
    private int dim;
    // 传递参数时 第一维[]是空 是否需要记录? 暂时不记录 只记录第二维(和更高维)的大小
    private ArrayList<ConstExp> constExps;

    public ArrayList<ConstExp> getConstExps() {
        return constExps;
    }

    public FuncFParam() {
        constExps = new ArrayList<>();
    }

    public BType getbType() {
        return bType;
    }

    public void setbType(BType bType) {
        this.bType = bType;
    }

    public Ident getIdent() {
        return ident;
    }

    public void setIdent(Ident ident) {
        this.ident = ident;
    }

    public void addFirstDim() {
        dim++;
    }
    public void addConstExp(ConstExp constExp) {
        constExps.add(constExp);
        dim++;
    }

    public int getDim() {
        return dim;
    }

    public void genLLVM() {
        Symbol symbol = getSymbol(ident.getName(), 0);
        // 什么时候应当考虑符号表顺序，而什么时间不需要考虑顺序，考虑清楚!
        curSymbolId = symbol.getId();
        IrInstruction irInstruction;
        // 先只考虑0维
        if (dim == 0) {
            if (MODE == 1) {
                irSymbolTable.addIrSymbol(symbol);
                irSymbolTable.addBasicBlockDef(symbol, symbol.getIdentifier());
            } else {
                String addr = curIrFunction.allocVirtualReg();
                // 将参数复制一份放入当前栈中
                irInstruction = new IrAlloc(addr);
                IrInstruction addrIns = irInstruction;
                curIrBasicBlock.addInstruction(irInstruction);
                // 不太对感觉 传int时 传的是值
                irInstruction = new IrStore(symbol.getIdentifier(), irInstruction);
                curIrBasicBlock.addInstruction(irInstruction);
                symbol.setIdentifier(addrIns);
            }
        } else if (dim == 1) {
            if (MODE == 1) {
                irSymbolTable.addIrSymbol(symbol);
                irSymbolTable.addBasicBlockDef(symbol, symbol.getIdentifier());
            } else {
                String addr = curIrFunction.allocVirtualReg();
                irInstruction = new IrAlloc(addr, true);
                IrInstruction addrIns = irInstruction;
                curIrBasicBlock.addInstruction(irInstruction);
                irInstruction = new IrStore(symbol.getIdentifier(), irInstruction);
                curIrBasicBlock.addInstruction(irInstruction);
                symbol.setIdentifier(addrIns);
            }
        } else {
            if (MODE == 1) {
                irSymbolTable.addIrSymbol(symbol);
                irSymbolTable.addBasicBlockDef(symbol, symbol.getIdentifier());
            } else {
                String addr = curIrFunction.allocVirtualReg();
                int dimSize = constExps.get(0).calConstExp();
                irInstruction = new IrAlloc(addr, dimSize, true);
                IrInstruction addrIns = irInstruction;
                // 为指针标识符备注维度大小
                curIrBasicBlock.addInstruction(irInstruction);
                irInstruction = new IrStore(symbol.getIdentifier(), irInstruction);
                curIrBasicBlock.addInstruction(irInstruction);
                symbol.setIdentifier(addrIns);
            }
        }
    }
}
