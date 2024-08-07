package BackEnd.MipsInstruction;

import BackEnd.MipsCore.MipsRegister;
/*
    规定只用$ra返回
 */
public class Jr implements MipsInstruction {
    public Jr() {
    }

    public String toString() {
        return "jr " + MipsRegister.getRegister(31) + "\n";
    }
}
