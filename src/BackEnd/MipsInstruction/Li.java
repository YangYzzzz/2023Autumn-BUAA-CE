package BackEnd.MipsInstruction;

import BackEnd.MipsCore.MipsRegister;

/*
    加载立即数，是一条伪指令，最多可加载32位
 */
public class Li implements MipsInstruction {
    private int target;
    private int immediate;

    public Li(int target, int immediate) {
        this.target = target;
        this.immediate = immediate;
    }

    public String toString() {
        return "li " + MipsRegister.getRegister(target) + ", " +
                immediate + "\n";
    }
}
