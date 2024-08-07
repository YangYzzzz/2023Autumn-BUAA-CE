package MidEnd.IrInstuctions;

import MidEnd.IrCore.IrInstruction;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

public class IrAdd extends IrInstruction {

    public IrAdd(String value, Value op1, Value op2) {
        super(value, 0);
        super.addUsedValue(op1);
        super.addUsedValue(op2);
    }

    public String toString() {
        // System.out.println(usedValues.size());
        return super.value + " = add i32 " + usedValues.get(0).value + ", " + usedValues.get(1).value + "\n";
    }

    @Override
    public ArrayList<String> toHashCode() {
        ArrayList<String> hashCode = new ArrayList<>(); // 滿足結合律
        hashCode.add("&+&" + usedValues.get(0).value + usedValues.get(1).value);
        hashCode.add("&+&" + usedValues.get(1).value + usedValues.get(0).value);
        return hashCode;
    }
}
