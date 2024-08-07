package BackEnd.MipsInstruction;

import BackEnd.MipsCore.MipsRegister;

/*
    无符号乘法，适用于除法优化中
 */
public class Multu implements MipsInstruction{
    private int op1;
    private int op2;

    public Multu(int op1, int op2) {
        this.op1 = op1;
        this.op2 = op2;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("multu ").
                append(MipsRegister.getRegister(op1)).append(", ").
                append(MipsRegister.getRegister(op2)).append("\n");
        return sb.toString();
    }
}
