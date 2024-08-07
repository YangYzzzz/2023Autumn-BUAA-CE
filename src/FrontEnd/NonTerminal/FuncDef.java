package FrontEnd.NonTerminal;

import FrontEnd.SymbolTable.FuncSymbol;
import MidEnd.IrInstuctions.IrRet;

import static FrontEnd.SymbolTable.StaticSymbolTable.curSymbolId;
import static FrontEnd.SymbolTable.StaticSymbolTable.getSymbol;
import static MidEnd.IrCore.IrModule.curIrBasicBlock;
import static MidEnd.IrCore.IrModule.curIrFunction;
import static MidEnd.IrCore.IrModule.irModule;
import static MidEnd.SymbolTable.IrSymbolTable.irSymbolTable;

public class FuncDef {
    private FuncType funcType;
    private Ident ident;
    private FuncFParams funcFParams;
    private Block block;

    public FuncDef() {
    }

    public FuncType getFuncType() {
        return funcType;
    }

    public void setFuncType(FuncType funcType) {
        this.funcType = funcType;
    }

    public Ident getIdent() {
        return ident;
    }

    public void setIdent(Ident ident) {
        this.ident = ident;
    }

    public FuncFParams getFuncFParams() {
        return funcFParams != null ? funcFParams : null;
    }

    public void setFuncFParams(FuncFParams funcFParams) {
        this.funcFParams = funcFParams;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public void genLLVM() {
        FuncSymbol funcSymbol = (FuncSymbol) getSymbol(ident.getName(), 0);
        curSymbolId = funcSymbol.getId();
        irModule.addFunction("@" + ident.getName(), funcSymbol.getRetype());
        irSymbolTable.addFunctionSymbolTable();
        // 为函数的入口空申请一个
        funcSymbol.setIdentifier(curIrFunction);
        block.genParamsLLVM(funcFParams);
        if (funcSymbol.getRetype() == 1) {
            curIrBasicBlock.addInstruction(new IrRet());
        }
    }
}
