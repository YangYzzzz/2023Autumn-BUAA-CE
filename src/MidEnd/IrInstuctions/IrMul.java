package MidEnd.IrInstuctions;

import MidEnd.IrCore.IrInstruction;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

public class IrMul extends IrInstruction {
    public IrMul(String value, Value op1, Value op2) {
        super(value, 0);
        super.addUsedValue(op1);
        super.addUsedValue(op2);
    }

    public String toString() {
        ArrayList<Value> ops = usedValues;
        return value + " = mul i32 " + ops.get(0).value + ", " + ops.get(1).value + "\n";
    }

    @Override
    public ArrayList<String> toHashCode() {
        ArrayList<String> hashCode = new ArrayList<>();
        hashCode.add("&*&" + usedValues.get(0).value + usedValues.get(1).value);
        hashCode.add("&*&" + usedValues.get(1).value + usedValues.get(0).value);
        return hashCode;
    }
}
