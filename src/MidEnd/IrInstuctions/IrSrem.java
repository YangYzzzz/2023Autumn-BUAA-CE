package MidEnd.IrInstuctions;

import MidEnd.IrCore.IrInstruction;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

public class IrSrem extends IrInstruction {
    public IrSrem(String value, Value op1, Value op2) {
        super(value, 0);
        super.addUsedValue(op1);
        super.addUsedValue(op2);
    }

    public String toString() {
        ArrayList<Value> ops = usedValues;
        return super.value + " = srem i32 " + ops.get(0).value + ", " + ops.get(1).value + "\n";
    }
}
