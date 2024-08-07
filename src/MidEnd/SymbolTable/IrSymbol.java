package MidEnd.SymbolTable;

import FrontEnd.SymbolTable.Symbol;
import MidEnd.IrCore.IrBasicBlock;
import MidEnd.IrCore.Value;

import java.util.HashMap;
import java.util.Map;

import static MidEnd.IrCore.IrModule.curIrBasicBlock;

/**
 * 仅针对单值优化设立的符号记录
 */
public class IrSymbol {
    private Symbol symbol; // 应当还需要记录额外的信息
    /* 记录该值的定义与赋值块 在一个基本块内可能会被赋值多次，记录最后一次赋值即可
        不对 仍需要记录树状结构信息，如
        若使用该值，则现在当前基本块最近一次定义，若有，直接用；若无，该基本块内无定义，说明来自之前，此时需要phi函数
     */
    private HashMap<IrBasicBlock, Value> blockDefs;

    public IrSymbol(Symbol symbol) {
        this.symbol = symbol;
        this.blockDefs = new HashMap<>();
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public Value getValue(IrBasicBlock irBasicBlock) {
        return blockDefs.get(irBasicBlock);
    }

    public HashMap<IrBasicBlock, Value> getBlockDefs() {
        return blockDefs;
    }

    public void setBlockDefs(HashMap<IrBasicBlock, Value> blockDefs) {
        this.blockDefs = blockDefs;
    }

    public void addBlockDef(Value value) {
        blockDefs.put(curIrBasicBlock, value);
    }

    // 避免抢占末尾定义，仅有该基本块无定义时才会插入
    public void addBlockDef(IrBasicBlock irBasicBlock, Value value) {
        if (!blockDefs.containsKey(irBasicBlock)) {
            blockDefs.put(irBasicBlock, value);
        }
    }

    public void printIrSymbol() {
        for (Map.Entry<IrBasicBlock, Value> entry : blockDefs.entrySet()) {
            // System.out.println("基本块为：" + entry.getKey().value + " 符号值为: " + entry.getValue().value);
        }
    }

    public Boolean isBlockHasDef(IrBasicBlock irBasicBlock) {
        return blockDefs.containsKey(irBasicBlock);
    }

    public void updateIrSymbol(IrBasicBlock curBasicBlock, IrBasicBlock frontBlock) {
        if (blockDefs.containsKey(curBasicBlock)) {
            blockDefs.put(frontBlock, blockDefs.get(curBasicBlock));
            blockDefs.remove(curBasicBlock);
        }
    }
}
