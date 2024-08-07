package BackEnd.MipsInstruction;

import BackEnd.MipsCore.MipsRegister;

public class Sw implements MipsInstruction {
    private int source;
    private int offset; // 相较于Base的偏移量
    private int base; // 一般是 $gp, $fp, $sp

    public Sw(int source, int offset, int base) {
        this.source = source;
        this.offset = offset;
        this.base = base;
    }

    public String toString() {
        return "sw " + MipsRegister.getRegister(source) + ", " +
                offset + "(" +
                MipsRegister.getRegister(base) + ")\n";
    }
}
