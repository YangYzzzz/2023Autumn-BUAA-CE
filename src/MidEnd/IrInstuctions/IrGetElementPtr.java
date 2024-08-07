package MidEnd.IrInstuctions;

import MidEnd.IrCore.IrInstruction;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

public class IrGetElementPtr extends IrInstruction {
    // 返回值为 i32* / [n x i32]*
    // value是提前申请好的一个虚拟变量，valueType是期望得到的地址类型 有两种情况 1和2

    // 返回 [n x i32]*

    public IrGetElementPtr(String value, Value baseAddr, Value index1, Value index2, Boolean bigPtrFlag) {
        super(value, 2, baseAddr.secondSize, 0);
        super.addUsedValue(baseAddr);
        super.addUsedValue(index1);
        super.addUsedValue(index2);
    }

    // 返回 i32*
    public IrGetElementPtr(String value, Value baseAddr, Value index1) {
        super(value, 1);
        super.addUsedValue(baseAddr);
        super.addUsedValue(index1);
    }

    public IrGetElementPtr(String value, Value baseAddr, Value index1, Value index2) {
        super(value, 1);
        super.addUsedValue(baseAddr);
        super.addUsedValue(index1);
        super.addUsedValue(index2);
    }

    public IrGetElementPtr(String value, Value baseAddr, Value index1, Value index2, Value index3) {
        super(value, 1);
        super.addUsedValue(baseAddr);
        super.addUsedValue(index1);
        super.addUsedValue(index2);
        super.addUsedValue(index3);
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(value).append(" = getelementptr ");
        Value baseAddr = usedValues.get(0);
        switch (baseAddr.valueType) {
            case 1: {
                sb.append("i32, i32* ").append(baseAddr.value);
                break;
            }
            case 2: {
                sb.append("[").append(baseAddr.firstSize).append(" x i32], [").append(baseAddr.firstSize).append(" x i32]* ").append(baseAddr.value);
                break;
            }
            case 3: {
                sb.append("[").append(baseAddr.firstSize).append(" x [").append(baseAddr.secondSize).append(" x i32]], [").append(baseAddr.firstSize).append(" x [").append(baseAddr.secondSize).append(" x i32]]* ").append(baseAddr.value);
                break;
            }
            default: {
                sb.append(baseAddr.value);
                break;
            }
        }
        // 索引需要提前给好 第一个索引不改变维度 从第二个索引开始 每个降一维
        // 可以推出 输入的baseAddr Type === index.size - 1 + valueType
        // baseAddr 1 ~ 3 ; valueType 1 ~ 2 ; 因此 Indexsize最大为4
        for (int i = 1; i < usedValues.size(); i++) {
            sb.append(", i32 ").append(usedValues.get(i).value);
        }
        return sb + "\n";
    }

    @Override
    public ArrayList<String> toHashCode() {
        ArrayList<String> hashCode = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("&gtr&");
        for (Value usedValue : usedValues) {
            sb.append(usedValue.value);
        }
        hashCode.add(sb.toString());
        return hashCode;
    }
}
