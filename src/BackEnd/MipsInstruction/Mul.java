package BackEnd.MipsInstruction;

import BackEnd.MipsCore.MipsRegister;

public class Mul implements MipsInstruction {
    private int target;
    private int op1;
    private int immediate;

    public Mul(int target, int op1, int immediate) {
        this.target = target;
        this.op1 = op1;
        this.immediate = immediate;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("mul ").append(MipsRegister.getRegister(target)).append(", ").
                append(MipsRegister.getRegister(op1)).append(", ").
                append(immediate).append("\n");
        return sb.toString();
    }
}
