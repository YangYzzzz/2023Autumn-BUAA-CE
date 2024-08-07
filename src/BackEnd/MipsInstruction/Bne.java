package BackEnd.MipsInstruction;

import BackEnd.MipsCore.MipsRegister;

public class Bne implements MipsInstruction {
    private int cmp1;
    private int cmp2; // 按常理来说 应当全是0
    private String label;

    public Bne(int cmp1, int cmp2, String label) {
        this.cmp1 = cmp1;
        this.cmp2 = cmp2;
        this.label = label;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("bne ").append(MipsRegister.getRegister(cmp1)).append(", ").
                append(MipsRegister.getRegister(cmp2)).append(", ").
                append(label.substring(1)).append("\n");
        return sb.toString();
    }
}
