package FrontEnd.NonTerminal;

import FrontEnd.SymbolTable.FuncSymbol;

import static FrontEnd.SymbolTable.StaticSymbolTable.curSymbolId;
import static FrontEnd.SymbolTable.StaticSymbolTable.getSymbol;
import static MidEnd.IrCore.IrModule.curIrFunction;
import static MidEnd.IrCore.IrModule.irModule;
import static MidEnd.SymbolTable.IrSymbolTable.irSymbolTable;

public class MainFuncDef {
    private Block block;

    public MainFuncDef() {
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public void genLLVM() {
        FuncSymbol funcSymbol = (FuncSymbol) getSymbol("main", 0);
        curSymbolId = funcSymbol.getId();
        irModule.addFunction("@main", 0);
        irSymbolTable.addFunctionSymbolTable();
        funcSymbol.setIdentifier(curIrFunction); // 将该函数对应的Value赋值到符号表中
        block.genLLVM();
    }
}
