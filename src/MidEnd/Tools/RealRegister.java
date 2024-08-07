package MidEnd.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RealRegister {
    public static final List<String> REGISTERNAMES = List.of("zero", "at", "v0", "v1",
            "a0", "a1", "a2", "a3",
            "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
            "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "t8", "t9",
            "k0", "k1", "gp", "sp", "fp", "ra");
    // 暂且将a也考虑在分配的寄存器内
    public static final List<Integer> regs = List.of(8, 9, 10, 11, 12, 13, 14, 15,16,17,18, 19,20, 21, 22, 23, 24, 25, 26);
    public static int regNum = regs.size();
    public static int getRealReg(Set<Integer> adjRegs) {
        for (int i = 0; i < regNum; i++) {
            if (!adjRegs.contains(regs.get(i))) {
                return regs.get(i);
            }
        }
        System.out.println("没有寄存器可以分配了!");
        return -1;
    }
}
