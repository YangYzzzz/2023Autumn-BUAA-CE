package BackEnd.MipsInstruction;

import BackEnd.MipsCore.MipsRegister;

public class Div implements MipsInstruction {
    private int op1;
    private int op2;

    public Div(int op1, int op2) {
        this.op1 = op1;
        this.op2 = op2;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("div ").
                append(MipsRegister.getRegister(op1)).append(", ").
                append(MipsRegister.getRegister(op2)).append("\n");
        return sb.toString();
    }
}
