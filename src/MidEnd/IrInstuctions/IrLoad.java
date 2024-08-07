package MidEnd.IrInstuctions;

import MidEnd.IrCore.IrInstruction;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

public class IrLoad extends IrInstruction {
    // return value + " = load i32, i32* " + addr;

    /*
        [n x i32]** ---> [n x i32]* 需要继承addr的firstSize
        i32** ---> i32*
        i32* ---> i32
     */
    public IrLoad(String value, int valueType, Value addr) {
        // 构造前推导 0 / 1 / 2
        super(value, valueType, addr.firstSize, addr.secondSize);
        super.addUsedValue(addr);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(value).append(" = load ");
        Value addr = usedValues.get(0);
        switch (valueType) {
            case 0: {
                sb.append("i32, i32* ").append(addr.value);
                break;
            }
            case 1: {
                sb.append("i32*, i32* * ").append(addr.value);
                break;
            }
            case 2: {
                sb.append("[").append(addr.firstSize).append(" x i32]* ").append(", [").append(addr.firstSize).append(" x i32]* * ").append(addr.value);
                break;
            }
        }
        return sb + "\n";
    }

    @Override
    public ArrayList<String> toHashCode() {
        ArrayList<String> hashCode = new ArrayList<>();
        hashCode.add("&load&" + usedValues.get(0).value);
        return hashCode;
    }
}
