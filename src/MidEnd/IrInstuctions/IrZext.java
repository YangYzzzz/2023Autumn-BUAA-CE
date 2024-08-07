package MidEnd.IrInstuctions;

import MidEnd.IrCore.IrInstruction;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

public class IrZext extends IrInstruction {

    public IrZext(String zextValue, Value value) {
        // 返回值i32类型
        super(zextValue, 0);
        super.addUsedValue(value);
    }

    public String toString() {
        return value + " = zext i1 " + usedValues.get(0).value + " to i32\n";
    }

    @Override
    public ArrayList<String> toHashCode() {
        ArrayList<String> hashCode = new ArrayList<>();
        hashCode.add("&->&" + usedValues.get(0).value);
        return hashCode;
    }
}
