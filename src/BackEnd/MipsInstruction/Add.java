package BackEnd.MipsInstruction;

import BackEnd.MipsCore.MipsRegister;
/*
    有符号 or 无符号？
 */
public class Add implements MipsInstruction {
    private int target;
    private int op1;
    private int op2;

    public Add(int target, int op1, int op2) {
        this.target = target;
        this.op1 = op1;
        this.op2 = op2;
    }

    public String toString() {
        if (target == -1) {
            return "";
        }
        return "addu " + MipsRegister.getRegister(target) + ", " +
                MipsRegister.getRegister(op1) + ", " +
                MipsRegister.getRegister(op2) + "\n";
    }

}
