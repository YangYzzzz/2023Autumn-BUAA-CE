package MidEnd.IrInstuctions;

import MidEnd.IrCore.IrInstruction;
import MidEnd.IrCore.Value;

public class IrBr extends IrInstruction {
    private int type; // 0 -> 无条件跳转
    private String fakeLabel1;
    private String fakeLabel2;
    public IrBr(String fakeLabel) {
        super(null, 0);
        type = 0;
        this.fakeLabel1 = fakeLabel;
        this.fakeLabel2 = "";
        // 用作后续替换
        super.addUsedValue(new Value("假Value", 9));
    }


    public IrBr(Value labelValue) {
        super(null, 0);
        this.fakeLabel1 = "";
        this.fakeLabel2 = "";
        type = 0;
        super.addUsedValue(labelValue);
    }
    public IrBr(Value cmp, String fakeLabel1, String fakeLabel2) {
        super(null, 0);
        type = 1;
        this.fakeLabel1 = fakeLabel1;
        this.fakeLabel2 = fakeLabel2;
        super.addUsedValue(cmp);
        super.addUsedValue(new Value("假Value", 9));
        super.addUsedValue(new Value("假Value", 9));
    }

    public int getType() {
        return type;
    }

    public void backFill(Value label, String fakeLabel) {
        if (fakeLabel1.equals(fakeLabel)) {
            if (type == 0) {
                usedValues.set(0, label);
            } else {
                usedValues.set(1, label);
            }
            fakeLabel1 = "";
        }
        if (fakeLabel2.equals(fakeLabel)) {
            usedValues.set(2, label);
            fakeLabel2 = "";
        }
    }
    public String toString() {
        if (type == 1) {
            Value value = usedValues.get(0);
            Value label1 = usedValues.get(1);
            Value label2 = usedValues.get(2);
            return "br i1 " + value.value + ", label " + label1.value + ", label " + label2.value + "\n";
        } else {
            return "br label " + usedValues.get(0).value + "\n";
        }
    }
}
