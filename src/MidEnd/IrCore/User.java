package MidEnd.IrCore;

import java.util.ArrayList;

public class User extends Value {
    // 一条指令作为一个user使用过的Value, 通常指两个操作数
    public ArrayList<Value> usedValues;

    public User(String value, int valueType) {
        super(value, valueType);
        usedValues = new ArrayList<>();
    }

    // 适用于函数指令，无法提前判断参数大小
    public User(String value, int valueType, ArrayList<Value> ops) {
        super(value, valueType);
        usedValues = ops;
    }

    public void addUsedValue(Value value) {
        usedValues.add(value); // 这一步删除死代码时再做，由于新增的phi定义无法触发这个函数
    }

    public void addUsedValue(int pos, Value value) {
        usedValues.add(pos, value);
    }
    public User(String value, int valueType, int lineSize, int rowSize) {
        super(value, valueType, lineSize, rowSize);
        usedValues = new ArrayList<>();
    }
}
