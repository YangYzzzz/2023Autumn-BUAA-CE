package BackEnd.MipsCore;

import MidEnd.IrCore.Value;

import java.util.ArrayList;
import java.util.List;

/*
    记录寄存器的使用情况，每个函数拥有一张MipsRegister，可申请没有使用过的寄存器，也可以释放已经使用过的寄存器
 */
public class MipsRegister {
    public static final List<String> REGISTERNAMES = List.of("zero", "at", "v0", "v1",
            "a0", "a1", "a2", "a3",
            "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
            "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "t8", "t9",
            "k0", "k1", "gp", "sp", "fp", "ra");

    public static String getRegister(int cnt) {
//        if (cnt < 0 || cnt > 31) {
//            cnt = 0;
//        }
        return "$" + REGISTERNAMES.get(cnt);
    }
    public static int V0 = 2;
    public static int V1 = 3;
    public static int ZERO = 0;
    public static int A0 = 4;
    public static int A1 = 5;
    public static int A2 = 6;
    public static int A3 = 7;
    public static int RA = 31;
    public static int FP = 30;
    public static int SP = 29;
    public static int GP = 28;
    public static int T0 = 8;
    public static int T8 = 24;
    public static int T9 = 25;
    public static int regNum = 32; // 0 ~ 31
    public ArrayList<Boolean> regIsUsed;
    // 是否是返回值寄存器
    public Boolean isVReg(int index) {
        return index == 2 || index == 3;
    }

    // 是否是传参寄存器
    public Boolean isAReg(int index) {
        return index == 4 || index == 5 || index == 6 || index == 7;
    }

    // 是否是临时寄存器 将T9看作是机动寄存器了
    public Boolean isTReg(int index) {
        return (index >= 8 && index <= 15);
    }

    // 是否是保存寄存器
    public Boolean isSReg(int index) {
        return index >= 16 && index <= 23;
    }
    public ArrayList<Integer> getUsingReg() {
        // T0 ~ T7, S0 ~ S7
        ArrayList<Integer> regs = new ArrayList<>();
        for (int i = 0; i < regNum; i++) {
            if ((isSReg(i) || isTReg(i)) && regIsUsed.get(i)) {
                regs.add(i);
            }
        }
        return regs;
    }
    public MipsRegister() {
        this.regIsUsed = new ArrayList<>();
        for (int i = 0; i < regNum; i++) {
            if (isSReg(i) || isAReg(i) || isVReg(i) || isTReg(i)) {
                regIsUsed.add(false);
            } else {
                regIsUsed.add(true); // 其他的寄存器不会被申请到
            }
        }
    }

    public void freeReg(Value value) {
        // System.out.println("释放了 " + value.reg + "号寄存器");
        regIsUsed.set(value.reg, false);
        value.isInReg = false;
    }

    // T是临时寄存器，用于存放临时值，如长串表达式的子式等，在一个基本块内使用，使用后应当消亡
    public int allocTReg() {
        for (int i = 0; i < regNum; i++) {
            if (isTReg(i) && !regIsUsed.get(i)) {
                regIsUsed.set(i, true);
                // System.out.println("申请了" + i + "寄存器");
                return i;
            }
        }
        return -1;
    }
    public int allocSReg() {
        for (int i = 0; i < regNum; i++) {
            if (isSReg(i) && !regIsUsed.get(i)) {
                regIsUsed.set(i, true);
                return i;
            }
        }
        return -1;
    }

    // 直接申请 无实际意义
    public void alloc(int index) {
        regIsUsed.set(index, true);
    }

    public int allocTempReg() {
        for (int i = 0; i < regNum; i++) {
            if ((isAReg(i) || isVReg(i)) && !regIsUsed.get(i)) {
                regIsUsed.set(i, true);
                return i;
            }
        }
        // System.out.println("找不到机动寄存器了，出错！");
        return -1;
    }
}
