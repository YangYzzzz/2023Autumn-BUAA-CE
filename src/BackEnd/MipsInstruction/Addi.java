package BackEnd.MipsInstruction;

import BackEnd.MipsCore.MipsRegister;

/*
    加立即数指令addiu，忽略溢出，允许32位立即数
 */
public class Addi implements MipsInstruction {
    private int target;
    private int op1;
    private int immediate;

    public Addi(int target, int op1, int immediate) {
        this.target = target;
        this.op1 = op1;
        this.immediate = immediate;
    }

    public String toString() {
        return "addi " + MipsRegister.getRegister(target) + ", " +
                MipsRegister.getRegister(op1) + ", " +
                immediate + "\n";
    }
}
