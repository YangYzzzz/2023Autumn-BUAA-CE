package BackEnd.MipsInstruction;

import BackEnd.MipsCore.MipsRegister;

public class Sub implements MipsInstruction {
    private int target;
    private int op1;
    private int op2;

    public Sub(int target, int op1, int op2) {
        this.target = target;
        this.op1 = op1;
        this.op2 = op2;
    }

    public String toString() {
        return "subu " + MipsRegister.getRegister(target) + ", " +
                MipsRegister.getRegister(op1) + ", " +
                MipsRegister.getRegister(op2) + "\n";
    }

}
