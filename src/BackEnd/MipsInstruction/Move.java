package BackEnd.MipsInstruction;

import BackEnd.MipsCore.MipsRegister;

public class Move implements MipsInstruction {
    private int target;
    private int source;

    public Move(int target, int source) {
        this.target = target;
        this.source = source;
    }

    public String toString() {
        if (target == -1 || source == -1) {
            return "";
        }
        return "move " + MipsRegister.getRegister(target) + ", " +
                MipsRegister.getRegister(source) + "\n";
    }
}
