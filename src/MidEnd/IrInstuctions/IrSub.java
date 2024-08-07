package MidEnd.IrInstuctions;

import MidEnd.IrCore.IrInstruction;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

public class IrSub extends IrInstruction {
    public IrSub(String value, Value op1, Value op2) {
        super(value, 0);
        super.addUsedValue(op1);
        super.addUsedValue(op2);
    }

    public String toString() {
        return value + " = sub i32 " + usedValues.get(0).value + ", " + usedValues.get(1).value + "\n";
    }

    @Override
    public ArrayList<String> toHashCode() {
        ArrayList<String> hashCode = new ArrayList<>();
        hashCode.add("&-&" + usedValues.get(0).value + usedValues.get(1).value);
        return hashCode;
    }
}
