package MidEnd.IrInstuctions;

import MidEnd.IrCore.IrInstruction;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

public class IrCall extends IrInstruction {

    // IrCall 命令 返回值value，使用的值为一个函数和数个实参 规定第一个Value是函数，后面的Value是实参
    public IrCall(String value, Value funcValue, ArrayList<Value> ops) {
        super(value, 0, ops);
        super.addUsedValue(funcValue);
    }

    // void返回值函数
    public IrCall(Value funcValue, ArrayList<Value> ops) {
        super("!noreturn", 0, ops);
        super.addUsedValue(funcValue);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!value.equals("!noreturn")) {
            sb.append(value).append(" = call i32 ");
        } else {
            sb.append("call void ");
        }
        sb.append(usedValues.get(usedValues.size() - 1).value).append("(");
        for (int i = 0; i < usedValues.size() - 1; i++) {
            Value usedValue = usedValues.get(i);
            switch (usedValue.valueType) {
                // 根据实参的类型 分为 0 i32 ；1 i32*；2 [n x i32]*
                case 0: {
                    sb.append("i32 ");
                    sb.append(usedValue.value);
                    break;
                }
                case 1: {
                    sb.append("i32* ");
                    sb.append(usedValue.value);
                    break;
                }
                case 2: {
                    sb.append("[").append(usedValue.firstSize).append(" x i32]* ");
                    sb.append(usedValue.value);
                    break;
                }
                case 10: {
                    sb.append("i32 with offset ");
                    sb.append(usedValue.offset);
                    break;
                }
            }
            if (i != usedValues.size() - 2) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb + "\n";
    }
}
