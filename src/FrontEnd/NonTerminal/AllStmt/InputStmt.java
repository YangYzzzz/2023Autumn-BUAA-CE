package FrontEnd.NonTerminal.AllStmt;

import FrontEnd.NonTerminal.LVal;
import FrontEnd.NonTerminal.Stmt;
import FrontEnd.SymbolTable.Symbol;
import MidEnd.IrCore.IrInstruction;
import MidEnd.IrInstuctions.IrCall;
import MidEnd.IrInstuctions.IrStore;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

import static FrontEnd.SymbolTable.StaticSymbolTable.getSymbol;
import static MidEnd.IrCore.IrModule.MODE;
import static MidEnd.IrCore.IrModule.curIrBasicBlock;
import static MidEnd.IrCore.IrModule.curIrFunction;
import static MidEnd.IrCore.IrModule.irModule;
import static MidEnd.SymbolTable.IrSymbolTable.irSymbolTable;

public class InputStmt implements Stmt {
    private LVal lVal;

    public InputStmt() {
    }

    public LVal getlVal() {
        return lVal;
    }

    public void setlVal(LVal lVal) {
        this.lVal = lVal;
    }

    @Override
    public void genLLVM() {
        Symbol symbol = getSymbol(lVal.getIdent().getName());
        String str = curIrFunction.allocVirtualReg();
        Value funcValue = irModule.getGetIntValue();
        IrInstruction irInstruction = new IrCall(str, funcValue, new ArrayList<>());
        curIrBasicBlock.addInstruction(irInstruction);
        if (MODE == 1 && symbol.getType() == 0 && symbol.getGlobal() == 0) {
            irSymbolTable.addBasicBlockDef(symbol, irInstruction);
        } else {
            Value addr = lVal.genGetAddrOnlyLLVM();
            curIrBasicBlock.addInstruction(new IrStore(irInstruction, addr));
        }
    }
}
