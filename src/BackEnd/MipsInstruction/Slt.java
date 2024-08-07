package BackEnd.MipsInstruction;

import BackEnd.MipsCore.MipsRegister;

public class Slt implements MipsInstruction {
    private int target;
    private int cmp1;
    private int cmp2;

    public Slt(int target, int cmp1, int cmp2) {
        this.target = target;
        this.cmp1 = cmp1;
        this.cmp2 = cmp2;
    }

    public String toString() {
        return "slt " + MipsRegister.getRegister(target) + ", " +
                MipsRegister.getRegister(cmp1) + ", " +
                MipsRegister.getRegister(cmp2) + "\n";
    }
}
