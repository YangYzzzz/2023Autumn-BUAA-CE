package BackEnd.MipsInstruction;

import BackEnd.MipsCore.MipsRegister;

public class Lw implements MipsInstruction {
    private int target;
    private int offset; // 相较于Base的偏移量
    private int base; // 一般是 $gp, $fp, $sp

    public Lw(int target, int offset, int base) {
        this.target = target;
        this.offset = offset;
        this.base = base;
    }

    public String toString() {
        return "lw " + MipsRegister.getRegister(target) + ", " +
                offset + "(" +
                MipsRegister.getRegister(base) + ")\n";
    }
}
