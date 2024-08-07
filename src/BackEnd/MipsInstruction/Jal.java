package BackEnd.MipsInstruction;

/*
    函数调用使用
 */
public class Jal implements MipsInstruction {
    private String label;

    public Jal(String label) {
        this.label = label;
    }

    public String toString() {
        return "jal " + label.substring(1) + "\n";
    }
}
