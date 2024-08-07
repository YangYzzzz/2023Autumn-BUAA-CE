package MidEnd.IrInstuctions;

import MidEnd.IrCore.IrInstruction;
import MidEnd.IrCore.Value;

/**
 * 记录着消PHI后产生的假LLVM指令MOVE
 */
public class IrMove extends IrInstruction {
    public Value target;
    public Value source;
    public IrMove(Value target, Value source) {
        super("!move", -1);
        this.target = target;
        this.source = source;
    }

    public String toString() {
        return target.value + " = move " + source.value + "\n";
    }
}
