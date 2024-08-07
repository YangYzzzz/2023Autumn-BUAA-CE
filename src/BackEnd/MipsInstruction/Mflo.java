package BackEnd.MipsInstruction;

import BackEnd.MipsCore.MipsRegister;

public class Mflo implements MipsInstruction{
    private int target;

    public Mflo(int target) {
        this.target = target;
    }

    public String toString() {
        return "mflo " + MipsRegister.getRegister(target) + "\n";
    }
}
