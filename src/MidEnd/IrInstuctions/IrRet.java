package MidEnd.IrInstuctions;

import MidEnd.IrCore.IrInstruction;
import MidEnd.IrCore.Value;

public class IrRet extends IrInstruction {
    public IrRet() {
        super(null, 0);
    }

    public IrRet(Value reValue) {
        super(null, 0);
        super.addUsedValue(reValue);
    }
    public String toString() {
        if (usedValues.isEmpty()) {
            return "ret void\n";
        } else {
            return "ret i32 " + usedValues.get(0).value + "\n";
        }
    }
}
