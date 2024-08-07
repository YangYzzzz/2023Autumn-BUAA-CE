package BackEnd.MipsInstruction;

import BackEnd.MipsCore.MipsRegister;

public class Sll implements MipsInstruction {
    private int target;
    private int source;
    private int offset; // 左移几位，一般是2

    public Sll(int target, int source, int offset) {
        this.target = target;
        this.source = source;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "sll " + MipsRegister.getRegister(target) + ", " +
                MipsRegister.getRegister(source) + ", " +
                offset + "\n";
    }
}
