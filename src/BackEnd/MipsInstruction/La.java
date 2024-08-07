package BackEnd.MipsInstruction;

import BackEnd.MipsCore.MipsRegister;

public class La implements MipsInstruction {
    private int target;
    private String strLabel; // 输出字符串的首地址 asciiz分配的strLabel

    public La(int target, String strLabel) {
        this.target = target;
        this.strLabel = strLabel;
    }

    public String toString() {
        return "la " + MipsRegister.getRegister(target) + ", " +
                strLabel + "\n";
    }
}
