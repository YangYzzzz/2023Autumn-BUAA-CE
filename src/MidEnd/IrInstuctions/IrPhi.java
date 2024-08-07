package MidEnd.IrInstuctions;

import FrontEnd.SymbolTable.Symbol;
import MidEnd.IrCore.IrBasicBlock;
import MidEnd.IrCore.IrInstruction;
import MidEnd.IrCore.Value;

/**
 * 聚合点的新Value，一定只有两个基本块聚合进来么？？
 * 是否存在大于两个基本块聚合进一个基本块的情况 待议
 */
public class IrPhi extends IrInstruction {
    public Symbol symbol; // 记录该phi是应用于哪一个Symbol的
    public IrPhi(String value, Symbol symbol) {
        super(value, 0);
        this.symbol = symbol;
    }

    public void addPhiStream(IrBasicBlock basicBlock, Value value) {
        super.addUsedValue(0, value);
        super.addUsedValue(1, basicBlock);
    }
    @Override
    public String toString() {
        // %v155 = phi i32 [ 0, %b1 ], [ %v131, %b3 ]
        StringBuilder sb = new StringBuilder();
        sb.append(value).append(" = phi i32 ");
        for (int i = 0; i < usedValues.size(); i = i + 2) {
            sb.append("[ ").append(usedValues.get(i).value).append(", ").append(usedValues.get(i+1).value).append(" ]");
            if (i != usedValues.size() - 2) {
                sb.append(", ");
            }
        }
//        sb.append(" ").append(symbol.getName()).append("\n");
        sb.append("\n");
        return sb.toString();
    }
}
