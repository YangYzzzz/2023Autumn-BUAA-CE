package FrontEnd.NonTerminal;

import FrontEnd.SymbolTable.Symbol;
import MidEnd.IrCore.IrInstruction;
import MidEnd.IrInstuctions.IrAdd;
import MidEnd.IrInstuctions.IrStore;
import MidEnd.IrCore.Value;

import static FrontEnd.SymbolTable.StaticSymbolTable.getSymbol;
import static MidEnd.IrCore.IrModule.MODE;
import static MidEnd.IrCore.IrModule.curIrBasicBlock;
import static MidEnd.IrCore.IrModule.curIrFunction;
import static MidEnd.SymbolTable.IrSymbolTable.irSymbolTable;

public class ForStmt {
    private LVal lVal;
    private Exp exp;

    public ForStmt() {
    }

    public LVal getlVal() {
        return lVal;
    }

    public void setlVal(LVal lVal) {
        this.lVal = lVal;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }

    public void genLLVM() {
        Value value = exp.genLLVM();
        Symbol symbol = getSymbol(lVal.getIdent().getName());
        if (MODE == 1 && symbol.getType() == 0 && symbol.getGlobal() == 0) {
            if (value.valueType == 9) {
                // 若返回是一个假标签，则
                String tempValue = curIrFunction.allocVirtualReg();
                IrInstruction irInstruction = new IrAdd(tempValue, value, new Value("0", 0));
                curIrBasicBlock.addInstruction(irInstruction);
                irSymbolTable.addBasicBlockDef(symbol, irInstruction);
            } else {
                irSymbolTable.addBasicBlockDef(symbol, value);
            }
            return;
        }
        Value addr = lVal.genGetAddrOnlyLLVM();
        curIrBasicBlock.addInstruction(new IrStore(value, addr));
    }
}
