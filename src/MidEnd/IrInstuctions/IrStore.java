package MidEnd.IrInstuctions;

import MidEnd.IrCore.IrInstruction;
import MidEnd.IrCore.Value;

public class IrStore extends IrInstruction {
    // return "store i32 " + value + ", i32* " + addr;
    public IrStore(Value value, Value addr) {
        super("!store", -1);
        super.addUsedValue(value);
        super.addUsedValue(addr);
    }

    public IrStore(Value value, Value addr, int valueType) {
        super("!store", valueType);
        super.addUsedValue(value);
        super.addUsedValue(addr);
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Value value = usedValues.get(0);
        Value addr = usedValues.get(1);
        sb.append("store ");
        // 申请的地址的Value类型 是这块地址指向的值的类型!
        switch (value.valueType) {
            case 0: {
                sb.append("i32 ").append(value.value).append(", i32* ").append(addr.value);
                break;
            }
            case 1: {
                sb.append("i32* ").append(value.value).append(", i32* * ").append(addr.value);
                break;
            }
            case 2: {
                sb.append("[").append(addr.firstSize).append(" x i32]* ").append(value.value).append(", [").append(addr.firstSize).append(" x i32]* * ").append(addr.value);
                break;
            }
            default: {
                sb.append(value.value).append(" with offset ").append(value.offset);
            }
        }
        return sb + "\n";
    }
}
