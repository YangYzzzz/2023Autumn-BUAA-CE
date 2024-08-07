package BackEnd.MipsInstruction;

import BackEnd.MipsCore.MipsRegister;

public class Mfhi implements MipsInstruction{
    private int target;

    public Mfhi(int target) {
        this.target = target;
    }

    public String toString() {
        return "mfhi " + MipsRegister.getRegister(target) + "\n";
    }
}
