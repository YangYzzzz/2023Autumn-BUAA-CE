package MidEnd.SymbolTable;

import FrontEnd.SymbolTable.Symbol;
import MidEnd.IrCore.IrBasicBlock;
import MidEnd.IrCore.IrFunction;
import MidEnd.IrCore.Value;

import java.util.HashMap;
import java.util.Map;

import static MidEnd.IrCore.IrModule.curIrBasicBlock;
import static MidEnd.IrCore.IrModule.curIrFunction;

/**
 * 用于LLVM优化，将单量的Value按每个函数提出，计算每个value的def基本块,
 */
public class IrSymbolTable {
    static public IrSymbolTable irSymbolTable = new IrSymbolTable();
    private HashMap<IrFunction, HashMap<Symbol, IrSymbol>> irSymbolTableHashMap; // 可以优化为HashMap<Symbol, IrSymbol>

    public IrSymbolTable() {
        this.irSymbolTableHashMap = new HashMap<>();
    }

    public void addFunctionSymbolTable() {
        irSymbolTableHashMap.put(curIrFunction, new HashMap<>());
    }

    public void addIrSymbol(Symbol symbol) {
        irSymbolTableHashMap.get(curIrFunction).put(symbol, new IrSymbol(symbol));
    }

    public void addBasicBlockDef(Symbol symbol, Value value) {
        irSymbolTableHashMap.get(curIrFunction).get(symbol).addBlockDef(value);
    }

    public void addBasicBlockDef(IrBasicBlock irBasicBlock, Symbol symbol, Value value) {
        irSymbolTableHashMap.get(curIrFunction).get(symbol).addBlockDef(irBasicBlock, value);
    }
    public Value getValue(Symbol symbol) {
        // 可能返回 null or Value
        return irSymbolTableHashMap.get(curIrFunction).get(symbol).getValue(curIrBasicBlock);
    }

    // 合并基本块后需要将被合并的基本块中定义的部分也转移到合并的基本块内
    public void updateIrSymbol(IrFunction irFunction, IrBasicBlock curBasicBlock, IrBasicBlock frontBlock) {
        HashMap<Symbol, IrSymbol> irSymbolHashMap = irSymbolTableHashMap.get(irFunction);
        for (Map.Entry<Symbol, IrSymbol> entry : irSymbolHashMap.entrySet()) {
            entry.getValue().updateIrSymbol(curBasicBlock, frontBlock);
        }
    }
    public HashMap<Symbol, IrSymbol> getFunctionIrSymbolTable(IrFunction irFunction) {
        return irSymbolTableHashMap.get(irFunction);
    }
    public void printIrSymbolTable() {
        // System.out.println("中间代码优化符号表：");
        for (Map.Entry<IrFunction, HashMap<Symbol, IrSymbol>> entry : irSymbolTableHashMap.entrySet()) {
            // System.out.println("函数名称：" + entry.getKey().value);
            for (Map.Entry<Symbol, IrSymbol> entry1 : entry.getValue().entrySet()) {
                // System.out.println("符号名为: " + entry1.getKey().getName());
                entry1.getValue().printIrSymbol();
            }
        }
    }
}
