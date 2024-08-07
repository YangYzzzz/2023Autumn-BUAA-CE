package MidEnd.Tools;

import MidEnd.IrCore.Value;

/*
    用来记录两个value的move/zext关系

 */
public class MoveConnect {
    public Value value1;
    public Value value2;

    public MoveConnect(Value value1, Value value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public Boolean equal(Value value1, Value value2) {
        return (this.value1.equal(value1) && this.value2.equal(value2))
                || (this.value1.equal(value2) && this.value2.equal(value1));
    }

    public void changeValueConnect(Value combineValue, Value combinedValue) {
        if (this.value1.equal(combinedValue)) {
            System.out.println(this.value1.value + " 被合并走了 需要被替换为" + combineValue.value + " 另外一條鞭為: " + value2.value);
            this.value1 = combineValue;
        } else if (this.value2.equal(combinedValue)) {
            System.out.println(this.value2.value + " 被合并走了 需要被替换为" + combineValue.value + " 另外一條鞭為: " + value1.value);
            this.value2 = combineValue;
        }
    }
}
