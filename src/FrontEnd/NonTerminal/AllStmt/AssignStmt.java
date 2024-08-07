package FrontEnd.NonTerminal.AllStmt;

import FrontEnd.NonTerminal.Exp;
import FrontEnd.NonTerminal.LVal;
import FrontEnd.NonTerminal.Stmt;
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

public class AssignStmt implements Stmt {
    private LVal lVal;
    private Exp exp;

    public AssignStmt() {
    }

    public LVal getlVal() {
        return lVal;
    }

    public void setlVal(LVal lVal) {
        this.lVal = lVal;
    }

    public Exp getExp() {
        return exp;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }

    @Override
    public void genLLVM() {
        // 左值计算LLVM 左值有两种情况 一种需要读左值 一种不需要 该处不需要读左值 只用把地址计算出来即可
        // System.out.println("赋值语句进入");
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
