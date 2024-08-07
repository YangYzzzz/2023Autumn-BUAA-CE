package BackEnd.MipsCore;

import BackEnd.MipsInstruction.Add;
import BackEnd.MipsInstruction.Addi;
import BackEnd.MipsInstruction.Asciiz;
import BackEnd.MipsInstruction.Beq;
import BackEnd.MipsInstruction.Bne;
import BackEnd.MipsInstruction.Div;
import BackEnd.MipsInstruction.J;
import BackEnd.MipsInstruction.Jal;
import BackEnd.MipsInstruction.Jr;
import BackEnd.MipsInstruction.La;
import BackEnd.MipsInstruction.Li;
import BackEnd.MipsInstruction.Lw;
import BackEnd.MipsInstruction.Mfhi;
import BackEnd.MipsInstruction.Mflo;
import BackEnd.MipsInstruction.MipsInstruction;
import BackEnd.MipsInstruction.Move;
import BackEnd.MipsInstruction.Mul;
import BackEnd.MipsInstruction.Mult;
import BackEnd.MipsInstruction.Multu;
import BackEnd.MipsInstruction.Seq;
import BackEnd.MipsInstruction.Sge;
import BackEnd.MipsInstruction.Sgt;
import BackEnd.MipsInstruction.Sle;
import BackEnd.MipsInstruction.Sll;
import BackEnd.MipsInstruction.Slt;
import BackEnd.MipsInstruction.Sne;
import BackEnd.MipsInstruction.Srl;
import BackEnd.MipsInstruction.Sub;
import BackEnd.MipsInstruction.Sw;
import BackEnd.MipsInstruction.Syscall;
import MidEnd.IrCore.IrArgument;
import MidEnd.IrCore.IrFunction;
import MidEnd.IrCore.IrInstruction;
import MidEnd.IrInstuctions.IrAdd;
import MidEnd.IrInstuctions.IrAlloc;
import MidEnd.IrInstuctions.IrBr;
import MidEnd.IrInstuctions.IrCall;
import MidEnd.IrInstuctions.IrCmp;
import MidEnd.IrInstuctions.IrGetElementPtr;
import MidEnd.IrInstuctions.IrLoad;
import MidEnd.IrInstuctions.IrMove;
import MidEnd.IrInstuctions.IrMul;
import MidEnd.IrInstuctions.IrRet;
import MidEnd.IrInstuctions.IrSdiv;
import MidEnd.IrInstuctions.IrSrem;
import MidEnd.IrInstuctions.IrStore;
import MidEnd.IrInstuctions.IrSub;
import MidEnd.IrInstuctions.IrZext;
import MidEnd.IrCore.Value;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static BackEnd.MipsCore.MipsFunction.curMipsFunction;
import static BackEnd.MipsCore.MipsRegister.A0;
import static BackEnd.MipsCore.MipsRegister.A1;
import static BackEnd.MipsCore.MipsRegister.A2;
import static BackEnd.MipsCore.MipsRegister.A3;
import static BackEnd.MipsCore.MipsRegister.FP;
import static BackEnd.MipsCore.MipsRegister.GP;
import static BackEnd.MipsCore.MipsRegister.RA;
import static BackEnd.MipsCore.MipsRegister.SP;
import static BackEnd.MipsCore.MipsRegister.T8;
import static BackEnd.MipsCore.MipsRegister.T9;
import static BackEnd.MipsCore.MipsRegister.V0;
import static BackEnd.MipsCore.MipsRegister.V1;
import static BackEnd.MipsCore.MipsRegister.ZERO;

/*
    一个MipsInstruction
 */
public class MipsInstructionBlock {
    private IrInstruction irInstruction;

    private ArrayList<MipsInstruction> mipsInstructions;
    private String divLabel1;
    private String divLabel2;
    private int reg1;

    public MipsInstructionBlock(IrInstruction irInstruction) {
        this.irInstruction = irInstruction;
        mipsInstructions = new ArrayList<>();
        this.divLabel1 = null;
    }

    public MipsInstructionBlock(Asciiz str) {
        irInstruction = null;
        mipsInstructions = new ArrayList<>();
        // 打印字符串
        // A0可能会用到 需不需要转移
        mipsInstructions.add(new La(A0, str.getName()));
        mipsInstructions.add(new Li(V0, 4));
        mipsInstructions.add(new Syscall());
        this.divLabel1 = null;
    }

    public Boolean isTransA0(Set<Value> outs) {
        for (Value value : outs) {
            if (value.reg == A0) {
                return true;
            }
        }
        return false;
    }

    public MipsInstructionBlock(Asciiz str, Set<Value> outs) {
        irInstruction = null;
        mipsInstructions = new ArrayList<>();
        // 打印字符串
        // A0可能会用到 需不需要转移
        if (isTransA0(outs)) {
            mipsInstructions.add(new Move(V1, A0));
        }
        mipsInstructions.add(new La(A0, str.getName()));
        mipsInstructions.add(new Li(V0, 4));
        mipsInstructions.add(new Syscall());
        if (isTransA0(outs)) {
            mipsInstructions.add(new Move(A0, V1));
        }
        this.divLabel1 = null;
    }

    public void genMips() {
        if (irInstruction != null) {
            if (irInstruction instanceof IrAlloc) {
                genAllocToMips();
            } else if (irInstruction instanceof IrGetElementPtr) {
                genGetElementPtrToMips();
            } else if (irInstruction instanceof IrStore) {
                genStoreToMips();
            } else if (irInstruction instanceof IrAdd) {
                genAddToMips();
            } else if (irInstruction instanceof IrSub) {
                genSubToMips();
            } else if (irInstruction instanceof IrBr) {
                genBrToMips();
            } else if (irInstruction instanceof IrCall) {
                genCallToMips();
            } else if (irInstruction instanceof IrCmp) {
                genCmpToMips();
            } else if (irInstruction instanceof IrLoad) {
                genLoadToMips();
            } else if (irInstruction instanceof IrMul) {
                genMulToMips();
            } else if (irInstruction instanceof IrRet) {
                genRetToMips();
            } else if (irInstruction instanceof IrSdiv) {
                genDivToMips();
            } else if (irInstruction instanceof IrZext) {
                genZextToMips();
            } else if (irInstruction instanceof IrSrem) {
                genModToMips();
            }
        }
    }

    private void genModToMips() {
        Value op1 = irInstruction.usedValues.get(0);
        Value op2 = irInstruction.usedValues.get(1);
        // 若不在 则装填上
        loadValueInReg(op1, V0);
        loadValueInReg(op2, T9);
        mipsInstructions.add(new Div(op1.reg, op2.reg));
        // 释放操作数的寄存器
        curMipsFunction.mipsRegister.freeReg(op1);
        curMipsFunction.mipsRegister.freeReg(op2);
        int t3Temp = curMipsFunction.mipsRegister.allocTReg();
        if (t3Temp != -1) {
            mipsInstructions.add(new Mfhi(t3Temp));
            irInstruction.setReg(t3Temp);
        } else {
            mipsInstructions.add(new Mfhi(V0));
            mipsInstructions.add(new Sw(V0, curMipsFunction.fpOffset, FP));
            irInstruction.offset = curMipsFunction.fpOffset;
            curMipsFunction.fpOffset += 4;
        }
    }

    private void genZextToMips() {
        // i1 -> i32 无需其他变动 寄存器不在乎i32和i1的区别
        Value value = irInstruction.usedValues.get(0);
        if (value.isInReg) {
            irInstruction.setReg(value.reg);
        } else {
            irInstruction.offset = value.offset;
        }
    }

    // 取模也可以这么算，但需要将LLVM转化一下
    private void genDivToMips() {
        Value op1 = irInstruction.usedValues.get(0);
        Value op2 = irInstruction.usedValues.get(1);
        // 若不在 则装填上
        loadValueInReg(op1, V0);
        loadValueInReg(op2, T9);
        mipsInstructions.add(new Div(op1.reg, op2.reg));
        // 释放操作数的寄存器
        mulAndDivFun(op1, op2);
    }

    private void loadValueInReg(Value op, int reg) {
        if (op.isNum()) {
            curMipsFunction.mipsRegister.alloc(reg);
            mipsInstructions.add(new Li(reg, Integer.parseInt(op.value)));
            op.setReg(reg);
        } else if (!op.isInReg) {
            curMipsFunction.mipsRegister.alloc(reg);
            mipsInstructions.add(new Lw(reg, op.offset, FP));
            op.setReg(reg);
        }
    }

    private void genRetToMips() {
        if (irInstruction.usedValues.size() == 1) {
            Value reValue = irInstruction.usedValues.get(0);
            // 有返回值
            if (reValue.isNum()) {
                mipsInstructions.add(new Li(V0, Integer.parseInt(reValue.value)));
            } else if (!reValue.isInReg) {
                mipsInstructions.add(new Lw(V0, reValue.offset, FP));
            } else {
                mipsInstructions.add(new Move(V0, reValue.reg));
                curMipsFunction.mipsRegister.freeReg(reValue);
            }
        }
        if (!curMipsFunction.isMain) {
            mipsInstructions.add(new Jr());
        }
    }

    private void genMulToMips() {
        Value op1 = irInstruction.usedValues.get(0);
        Value op2 = irInstruction.usedValues.get(1);
        // 若不在 则装填上
        loadValueInReg(op1, V0);
        loadValueInReg(op2, T9);
        mipsInstructions.add(new Mult(op1.reg, op2.reg));
        // 释放操作数的寄存器
        mulAndDivFun(op1, op2);
    }

    private void mulAndDivFun(Value op1, Value op2) {
        curMipsFunction.mipsRegister.freeReg(op1);
        curMipsFunction.mipsRegister.freeReg(op2);
        int t3Temp = curMipsFunction.mipsRegister.allocTReg();
        if (t3Temp != -1) {
            mipsInstructions.add(new Mflo(t3Temp));
            irInstruction.setReg(t3Temp);
        } else {
            mipsInstructions.add(new Mflo(T9));
            mipsInstructions.add(new Sw(T9, curMipsFunction.fpOffset, FP));
            irInstruction.offset = curMipsFunction.fpOffset;
            curMipsFunction.fpOffset += 4;
        }
    }

    /*
        核心：两种情况，load值和load地址
     */
    private void genLoadToMips() {
        Value addr = irInstruction.usedValues.get(0);
        int sTemp = curMipsFunction.mipsRegister.allocSReg();
        if (sTemp != -1) {
            loadFun(addr, sTemp);
            irInstruction.setReg(sTemp);
        } else {
            // 若此刻无可用寄存器
            loadFun(addr, T9);
            mipsInstructions.add(new Sw(T9, curMipsFunction.fpOffset, FP));
            irInstruction.offset = curMipsFunction.fpOffset;
            curMipsFunction.fpOffset += 4;
        }
    }

    // addr 作为一个Value 可能在内存中，也可能在寄存器中，也可能offset是记录了地址 这种情况一定不在寄存器中
    // 所以要将不在寄存器中的情况 和 在内存中 看作两种情况
    private void loadFun(Value addr, int reg) {
        // 一维取值的情况? 取数时无需经过gep，直接从地址中去
        if (addr.isGp) {
            mipsInstructions.add(new Lw(reg, addr.offset, GP));
        } else if (addr.isFp) {
            mipsInstructions.add(new Lw(reg, addr.offset, FP));
        } else if (!addr.isInReg) {
            // 地址不在寄存器中，
            mipsInstructions.add(new Lw(reg, addr.offset, FP));
            mipsInstructions.add(new Lw(reg, 0, reg));
        } else {
            // 地址在寄存器内，一定是绝对地址
            mipsInstructions.add(new Lw(reg, 0, addr.reg));
            curMipsFunction.mipsRegister.freeReg(addr);
        }
    }

    private void genCmpToMips() {
        Value cmp1 = irInstruction.usedValues.get(0);
        loadValueInReg(cmp1, V0);
        Value cmp2 = irInstruction.usedValues.get(1);
        loadValueInReg(cmp2, T9);
        int tTemp = curMipsFunction.mipsRegister.allocTReg();
        if (tTemp != -1) {
            cmpFun(cmp1, cmp2, tTemp);
            irInstruction.setReg(tTemp);
        } else {
            cmpFun(cmp1, cmp2, T8);
            mipsInstructions.add(new Sw(T8, curMipsFunction.fpOffset, FP));
            irInstruction.offset = curMipsFunction.fpOffset;
            curMipsFunction.fpOffset += 4;
        }
        curMipsFunction.mipsRegister.freeReg(cmp1);
        curMipsFunction.mipsRegister.freeReg(cmp2);
    }

    private void cmpFun(Value cmp1, Value cmp2, int targetReg) {
        switch (((IrCmp) irInstruction).getCmpOp()) {
            case EQL -> mipsInstructions.add(new Seq(targetReg, cmp1.reg, cmp2.reg));
            case NEQ -> mipsInstructions.add(new Sne(targetReg, cmp1.reg, cmp2.reg));
            case LEQ -> mipsInstructions.add(new Sle(targetReg, cmp1.reg, cmp2.reg));
            case LSS -> mipsInstructions.add(new Slt(targetReg, cmp1.reg, cmp2.reg));
            case GEQ -> mipsInstructions.add(new Sge(targetReg, cmp1.reg, cmp2.reg));
            case GRE -> mipsInstructions.add(new Sgt(targetReg, cmp1.reg, cmp2.reg));
        }
    }

    private void genCallToMips() {
        ArrayList<Value> values = irInstruction.usedValues;
        Value funcValue = values.get(values.size() - 1);
        switch (funcValue.value) {
            case "@putint": {
                Value value = values.get(0);
                mipsInstructions.add(new Li(V0, 1));
                if (value.isNum()) {
                    mipsInstructions.add(new Li(A0, Integer.parseInt(value.value)));
                } else if (value.isInReg) {
                    mipsInstructions.add(new Move(A0, value.reg));
                    curMipsFunction.mipsRegister.freeReg(value);
                } else {
                    mipsInstructions.add(new Lw(A0, value.offset, FP));
                }
                mipsInstructions.add(new Syscall());
                break;
            }
            case "@getint": {
                mipsInstructions.add(new Li(V0, 5));
                mipsInstructions.add(new Syscall());
                int tTemp = curMipsFunction.mipsRegister.allocTReg();
                // 此时v0中保存整数的值
                if (tTemp != -1) {
                    mipsInstructions.add(new Move(tTemp, V0));
                    irInstruction.setReg(tTemp);
                } else {
                    mipsInstructions.add(new Sw(V0, curMipsFunction.fpOffset, FP));
                    irInstruction.offset = curMipsFunction.fpOffset;
                    curMipsFunction.fpOffset += 4;
                }
                break;
            }
            default: {
                /*
                    正经的函数
                    1. 将前四个参数放入A中
                    2. 将当前FP，RA保存到SP中 还有哪些需要保存 目前其他的不需要 实际上需要保存上下文现场!
                    3. 将FP增长至被调用函数的栈帧处
                    4. 将剩余参数存入FP首地址的位置
                    5. 执行跳转 jal xxx
                    6. 返回后首先将FP,RA从SP中取回 SP = SP + 8
                    7. 将返回值转移到其他寄存器，继续陨星
                 */
                for (int i = 0; i < values.size() - 1; i++) {
                    Value value = values.get(i);
                    switch (i) {
                        case 0: {
                            fillRParam(value, A0);
                            break;
                        }
                        case 1: {
                            fillRParam(value, A1);
                            break;
                        }
                        case 2: {
                            fillRParam(value, A2);
                            break;
                        }
                        case 3: {
                            fillRParam(value, A3);
                            break;
                        }
                    }
                    if (i > 3) {
                        break; // 多的后面打包
                    }
                }
                int spOffset = 0;
                mipsInstructions.add(new Sw(RA, -spOffset, SP));
                spOffset += 4;
                mipsInstructions.add(new Sw(FP, -spOffset, SP));
                spOffset += 4;
                // 保存上下文查看现在哪些在使用中
                ArrayList<Integer> usingRegs = curMipsFunction.mipsRegister.getUsingReg();
                for (Integer i : usingRegs) {
                    mipsInstructions.add(new Sw(i, -spOffset, SP));
                    spOffset += 4;
                }
                mipsInstructions.add(new Addi(SP, SP, -spOffset));
//                mipsInstructions.add(new Li(T8, curMipsFunction.fpOffset));
                // TODO:
                mipsInstructions.add(new Addi(FP, FP, curMipsFunction.fpOffset)); // 将栈帧调整为被调用函数的基地址
                int newFpOffset = 16; // 前四个参数跳过
                // 若传参过多，寄存器记录不下那么多的参数，目前没考虑，需要存入活动变量中
                for (int i = 4; i < values.size() - 1; i++) {
                    Value value = values.get(i);
                    if (value.isNum()) {
                        mipsInstructions.add(new Li(V0, Integer.parseInt(value.value)));
                        value.setReg(V0);
                    } else if (value.isGp) {
                        mipsInstructions.add(new Lw(V0, value.offset, GP));
                        value.setReg(V0);
                    } else if (value.isFp) {
                        mipsInstructions.add(new Lw(V0, value.offset - curMipsFunction.fpOffset, FP));
                        value.setReg(V0);
                    } else if (!value.isInReg) {
                        mipsInstructions.add(new Lw(V0, value.offset - curMipsFunction.fpOffset, FP));
                        value.setReg(V0);
                    }
                    mipsInstructions.add(new Sw(value.reg, newFpOffset, FP));
                    newFpOffset += 4;
                    curMipsFunction.mipsRegister.freeReg(value);
                }
                // 执行跳转
                mipsInstructions.add(new Jal(funcValue.value));
                // 返回后
                mipsInstructions.add(new Addi(SP, SP, spOffset));
                spOffset = 0;
                mipsInstructions.add(new Lw(RA, spOffset, SP));
                spOffset -= 4;
                mipsInstructions.add(new Lw(FP, spOffset, SP));
                spOffset -= 4;
                for (Integer i : usingRegs) {
                    mipsInstructions.add(new Lw(i, spOffset, SP));
                    spOffset -= 4;
                }
                if (irInstruction.value != null) {
                    int tTemp = curMipsFunction.mipsRegister.allocTReg();
                    if (tTemp != -1) {
                        // System.out.println("函数的返回值存放在： " + tTemp + " 中, 调用函数为: " + funcValue.value);
                        mipsInstructions.add(new Move(tTemp, V0));
                        irInstruction.setReg(tTemp);
                    } else {
                        // 此时函数的返回值被保存在内存中
                        mipsInstructions.add(new Sw(V0, curMipsFunction.fpOffset, FP));
                        irInstruction.offset = curMipsFunction.fpOffset;
                        curMipsFunction.fpOffset += 4;
                    }
                }
                break;
            }
        }
    }

    private void fillRParam(Value value, int a) {
        if (value.isNum()) {
            mipsInstructions.add(new Li(a, Integer.parseInt(value.value)));
        } else if (value.isGp) {
            mipsInstructions.add(new Lw(a, value.offset, GP));
        } else if (value.isFp) {
            mipsInstructions.add(new Lw(a, value.offset, FP));
        } else if (value.isInReg) {
            mipsInstructions.add(new Move(a, value.reg));
            curMipsFunction.mipsRegister.freeReg(value);
        } else {
            // 和上面的情况有何不同，
            mipsInstructions.add(new Lw(a, value.offset, FP));
        }
    }

    private void genBrToMips() {
        switch (((IrBr) irInstruction).getType()) {
            case 0: {
                // 直接跳转
                mipsInstructions.add(new J(irInstruction.usedValues.get(0).value));
                break;
            }
            case 1: {
                // i1 型的值一定不是个常数
                Value cmp = irInstruction.usedValues.get(0);
                String label1 = irInstruction.usedValues.get(1).value;
                String label2 = irInstruction.usedValues.get(2).value;
                if (cmp.isInReg) {
                    mipsInstructions.add(new Bne(cmp.reg, ZERO, label1));
                    mipsInstructions.add(new Beq(cmp.reg, ZERO, label2));
                    curMipsFunction.mipsRegister.freeReg(cmp);
                } else if (cmp.isNum()) {
                    mipsInstructions.add(new Li(V0, Integer.parseInt(cmp.value)));
                    mipsInstructions.add(new Bne(V0, ZERO, label1));
                    mipsInstructions.add(new Beq(V0, ZERO, label2));
                } else {
                    mipsInstructions.add(new Lw(V0, cmp.offset, FP));
                    mipsInstructions.add(new Bne(V0, ZERO, label1));
                    mipsInstructions.add(new Beq(V0, ZERO, label2));
                }
                break;
            }
            default: {
                // System.out.println("Br Error!");
                break;
            }
        }
    }

    private void genSubToMips() {
        Value op1 = irInstruction.usedValues.get(0);
        Value op2 = irInstruction.usedValues.get(1);
        loadValueInReg(op1, V0);
        loadValueInReg(op2, T9);
        int t3Temp = curMipsFunction.mipsRegister.allocTReg();
        if (t3Temp != -1) {
            mipsInstructions.add(new Sub(t3Temp, op1.reg, op2.reg));
            irInstruction.setReg(t3Temp);
        } else {
            mipsInstructions.add(new Sub(T8, op1.reg, op2.reg));
            mipsInstructions.add(new Sw(T8, curMipsFunction.fpOffset, FP));
            irInstruction.offset = curMipsFunction.fpOffset;
            curMipsFunction.fpOffset += 4;
        }
        // 释放操作数的寄存器
        curMipsFunction.mipsRegister.freeReg(op1);
        curMipsFunction.mipsRegister.freeReg(op2);
    }

    private void genAddToMips() {
        Value op1 = irInstruction.usedValues.get(0);
        Value op2 = irInstruction.usedValues.get(1);
        loadValueInReg(op1, V0);
        loadValueInReg(op2, T9);
        int t3Temp = curMipsFunction.mipsRegister.allocTReg();
        if (t3Temp != -1) {
            mipsInstructions.add(new Add(t3Temp, op1.reg, op2.reg));
            irInstruction.setReg(t3Temp);
        } else {
            mipsInstructions.add(new Add(T8, op1.reg, op2.reg));
            mipsInstructions.add(new Sw(T8, curMipsFunction.fpOffset, FP));
            irInstruction.offset = curMipsFunction.fpOffset;
            curMipsFunction.fpOffset += 4;
        }
        // 释放操作数的寄存器
        curMipsFunction.mipsRegister.freeReg(op1);
        curMipsFunction.mipsRegister.freeReg(op2);
    }

    // 涉及到地址时，一律通过FP / GP寻址 或者是 V1 寻址
    private void genStoreToMips() {
        Value value = irInstruction.usedValues.get(0);
        Value addr = irInstruction.usedValues.get(1);
        loadValueInReg(value, V0);
        // 函数传参
        // addr的基地址一定被一寄存器存着或者是记录了偏移量
        if (addr.isGp) {
            mipsInstructions.add(new Sw(value.reg, addr.offset, GP));
        } else if (addr.isFp) {
            mipsInstructions.add(new Sw(value.reg, addr.offset, FP));
        } else if (!addr.isInReg) {
            mipsInstructions.add(new Lw(T9, addr.offset, FP));
            mipsInstructions.add(new Sw(value.reg, 0, T9));
        } else {
            // 待保存的值，有两种情况，一种情况是该值在寄存器内，此时正常即可，另一种是参数多于四个时，寄存器放不下了，被保存在栈帧中，此时需要先从栈帧中取出
            mipsInstructions.add(new Sw(value.reg, 0, addr.reg));
            curMipsFunction.mipsRegister.freeReg(addr); // 释放掉V1
        }
        curMipsFunction.mipsRegister.freeReg(value);
    }

    /*
        算出来相对于baseAddr的偏移量，多少个Int32
        究竟是否需要计算 直接算出来值Offset即可，通过基地址＋偏移计算
        此条指令一定和后续存取直接相关(原子操作)，且用过后不会再次使用
        首先 结果Value一定要被保存为绝对地址，因为一旦这个地址作为函数参数传递，便不知道
     */
    private void genGetElementPtrToMips() {
        // baseAddr i32*, [n x i32]*, [m x [n x i32]*
        // targetAddr i32*, [n x i32]*
        // 只有地址会反复使用，其他的虚拟寄存器使用后就会释放 区分Gp还是Fp
        Value baseAddr = irInstruction.usedValues.get(0);
        // 基地址有可能已经被load出来，比如传递指针时从地址的地址取出，这个时候从寄存器中存的是基地址的值
        // 查找 a[2][2] 传 a 出现的问题，get后得到的应该是value中存着其地址才对
        // 此时V1中装着baseAddr的绝对基地址
        if (baseAddr.isInReg) { // 绝对基地址
            mipsInstructions.add(new Move(V1, baseAddr.reg));
        } else if (baseAddr.isGp) {
            mipsInstructions.add(new Addi(V1, GP, baseAddr.offset));
        } else if (baseAddr.isFp) {
            mipsInstructions.add(new Addi(V1, FP, baseAddr.offset));
        } else {
            mipsInstructions.add(new Lw(V1, baseAddr.offset, FP));
        }
        int tTemp = curMipsFunction.mipsRegister.allocTReg();
        switch (baseAddr.valueType) {
            case 1: {
                // i32*
                if (irInstruction.valueType == 1) {
                    Value index = irInstruction.usedValues.get(1);
                    if (index.isNum()) {
                        // 偏移量是数
//                        irInstruction.offset = Integer.parseInt(index.value) * 4 + baseAddr.offset; //理解的不到位 这条的Value的Offset如果被推进内存内距离基地址FP的偏移量，而这个算的是相对于目标地址的偏移量
                        mipsInstructions.add(new Addi(V1, V1, Integer.parseInt(index.value) * 4));
                    } else {
                        if (!index.isInReg) {
                            curMipsFunction.mipsRegister.alloc(T8);
                            index.setReg(T8);
                            mipsInstructions.add(new Lw(T8, index.offset, FP));
                        }
                        // 偏移量存储的寄存器已经存进去了 此时该地址不再通过fp寻址，而是通过fp+fpoffset+reg内存的值寻址
                        // 优化位置，一定要在下一个V1被顶替前用出该Value，事实上这是对的，因为
                        // V1用作机动，使用完光速释放
                        mipsInstructions.add(new Sll(index.reg, index.reg, 2));
                        mipsInstructions.add(new Add(V1, V1, index.reg));
                        curMipsFunction.mipsRegister.freeReg(index);
                    }
                    irInstruction.setReg(V1);
                } else {
                    // System.out.println("Index Error! 请检查");
                }
                break;
            }
            case 2: {
                int lineSize = baseAddr.firstSize;
                switch (irInstruction.valueType) {
                    case 2: {
                        // 1个index
                        Value index = irInstruction.usedValues.get(1);
                        if (index.isNum()) {
                            mipsInstructions.add(new Addi(V1, V1, Integer.parseInt(index.value) * lineSize * 4));
                        } else {
                            if (!index.isInReg) {
                                curMipsFunction.mipsRegister.alloc(T8);
                                index.setReg(T8);
                                mipsInstructions.add(new Lw(T8, index.offset, FP));
                            }
                            // fp + offset + linesize * 4 * reg
                            mipsInstructions.add(new Sll(index.reg, index.reg, 2));
                            mipsInstructions.add(new Mul(index.reg, index.reg, lineSize));
                            mipsInstructions.add(new Add(V1, V1, index.reg));
                            curMipsFunction.mipsRegister.freeReg(index);
                        }
                        irInstruction.setReg(V1);
                        break;
                    }
                    case 1: {
                        // 两个index (index1 * linesize + index2) * 4
                        Value index1 = irInstruction.usedValues.get(1);
                        Value index2 = irInstruction.usedValues.get(2);
                        if (index1.isNum() && index2.isNum()) {
                            mipsInstructions.add(new Addi(V1, V1, (Integer.parseInt(index1.value) * lineSize + Integer.parseInt(index2.value)) * 4));
                        } else {
                            if (!index1.isNum()) {
                                if (!index1.isInReg) {
                                    curMipsFunction.mipsRegister.alloc(T8);
                                    index1.setReg(T8);
                                    mipsInstructions.add(new Lw(T8, index1.offset, FP));
                                }
                                mipsInstructions.add(new Mul(index1.reg, index1.reg, lineSize));
                                if (!index2.isNum()) {
                                    if (!index2.isInReg) {
                                        curMipsFunction.mipsRegister.alloc(T9);
                                        index2.setReg(T9);
                                        mipsInstructions.add(new Lw(T9, index2.offset, FP));
                                    }
                                    mipsInstructions.add(new Add(index1.reg, index1.reg, index2.reg));
                                    curMipsFunction.mipsRegister.freeReg(index2);
                                } else {
                                    mipsInstructions.add(new Addi(index1.reg, index1.reg, Integer.parseInt(index2.value)));
                                }
                                mipsInstructions.add(new Sll(index1.reg, index1.reg, 2));
                                mipsInstructions.add(new Add(V1, V1, index1.reg));
                                curMipsFunction.mipsRegister.freeReg(index1);
                            } else {
                                if (!index2.isInReg) {
                                    curMipsFunction.mipsRegister.alloc(T8);
                                    index2.setReg(T8);
                                    mipsInstructions.add(new Lw(T8, index2.offset, FP));
                                }
                                mipsInstructions.add(new Addi(index2.reg, index2.reg, lineSize * Integer.parseInt(index1.value)));
                                mipsInstructions.add(new Sll(index2.reg, index2.reg, 2));
                                mipsInstructions.add(new Add(V1, V1, index2.reg));
                                curMipsFunction.mipsRegister.freeReg(index2);
                            }
                            irInstruction.setReg(V1);
                        }
                        break;
                    }
                    default: {
                        // System.out.println("Index Error! 请检查");
                        break;
                    }
                }
                break;
            }
            case 3: {
                // 和上一种情况非常类似可提取
                int dim2 = baseAddr.secondSize;
                switch (irInstruction.valueType) {
                    // 第一个Index一定是0，只需求二三个即可
                    case 1: {
                        // 三个index
                        Value index1 = irInstruction.usedValues.get(2);
                        Value index2 = irInstruction.usedValues.get(3);
                        if (index1.isNum() && index2.isNum()) {
                            mipsInstructions.add(new Addi(V1, V1, (Integer.parseInt(index1.value) * dim2 + Integer.parseInt(index2.value)) * 4));
                        } else {
                            if (!index1.isNum()) {
                                if (!index1.isInReg) {
                                    curMipsFunction.mipsRegister.alloc(T8);
                                    index1.setReg(T8);
                                    mipsInstructions.add(new Lw(T8, index1.offset, FP));
                                }
                                mipsInstructions.add(new Mul(index1.reg, index1.reg, dim2));
                                if (!index2.isNum()) {
                                    if (!index2.isInReg) {
                                        curMipsFunction.mipsRegister.alloc(T9);
                                        index2.setReg(T9);
                                        mipsInstructions.add(new Lw(T9, index2.offset, FP));
                                    }
                                    mipsInstructions.add(new Add(index1.reg, index1.reg, index2.reg));
                                    curMipsFunction.mipsRegister.freeReg(index2);
                                } else {
                                    mipsInstructions.add(new Addi(index1.reg, index1.reg, Integer.parseInt(index2.value)));
                                }
                                mipsInstructions.add(new Sll(index1.reg, index1.reg, 2));
                                mipsInstructions.add(new Add(V1, V1, index1.reg));
                                curMipsFunction.mipsRegister.freeReg(index1);
                            } else {
                                if (!index2.isInReg) {
                                    curMipsFunction.mipsRegister.alloc(T8);
                                    index2.setReg(T8);
                                    mipsInstructions.add(new Lw(T8, index2.offset, FP));
                                }
                                mipsInstructions.add(new Addi(index2.reg, index2.reg, dim2 * Integer.parseInt(index1.value)));
                                mipsInstructions.add(new Sll(index2.reg, index2.reg, 2));
                                mipsInstructions.add(new Add(V1, V1, index2.reg));
                                curMipsFunction.mipsRegister.freeReg(index2);
                            }
                            irInstruction.setReg(V1);
                        }
                        break;
                    }
                    case 2: {
                        Value index = irInstruction.usedValues.get(2);
                        if (index.isNum()) {
                            mipsInstructions.add(new Addi(V1, V1, Integer.parseInt(index.value) * dim2 * 4));
                        } else {
                            if (!index.isInReg) {
                                curMipsFunction.mipsRegister.alloc(T8);
                                index.setReg(T8);
                                mipsInstructions.add(new Lw(T8, index.offset, FP));
                            }
                            // fp + offset + linesize * 4 * reg
                            mipsInstructions.add(new Sll(index.reg, index.reg, 2));
                            mipsInstructions.add(new Mul(index.reg, index.reg, dim2));
                            mipsInstructions.add(new Add(V1, V1, index.reg));
                            curMipsFunction.mipsRegister.freeReg(index);
                        }
                        irInstruction.setReg(V1);
                        break;
                    }
                    default: {
                        // System.out.println("Index Error! 请检查");
                        break;
                    }
                }
            }
        }
        if (tTemp == -1) {
            mipsInstructions.add(new Sw(V1, curMipsFunction.fpOffset, FP));
            irInstruction.offset = curMipsFunction.fpOffset;
            curMipsFunction.fpOffset += 4;
            curMipsFunction.mipsRegister.freeReg(irInstruction);
        } else {
            irInstruction.setReg(tTemp); // V1是方便的一个过度
            mipsInstructions.add(new Move(tTemp, V1));
        }
        // 用过一次就扔，这个可有可无
//        if (baseAddr.isInReg) {
//            curMipsFunction.mipsRegister.freeReg(baseAddr);
//        }
    }

    private void genAllocToMips() {
        // 地址不用分配寄存器，用fp偏移量就可以表示
        irInstruction.isFp = true;
        // 偏移量记录的是，该地址的值距离基活动记录的偏移，而alloc申请的是地址！
        irInstruction.offset = curMipsFunction.fpOffset;
//        switch (irInstruction.valueType) {
//            case 1: {
//                // 一个指向int的int*
//            }
//            case 2: {
//                // 指向一维数组的int*
//            }
//            case 3: {
//                // 指向二维数组的int*
//            }
//        }
        switch (irInstruction.valueType) {
            case 1, 7, 8: {
                curMipsFunction.fpOffset += 4;
                break;
            }
            case 2: {
                curMipsFunction.fpOffset += 4 * irInstruction.firstSize;
                break;
            }
            case 3: {
                curMipsFunction.fpOffset += 4 * irInstruction.firstSize * irInstruction.secondSize;
                break;
            }
            default: {
                // System.out.println("Alloc Error!");
                break;
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
//        if (irInstruction != null) {
//            sb.append("\n").append(irInstruction.toString());
//        }
        if (this.divLabel1 != null) {
            sb.append(mipsInstructions.get(0).toString());
            sb.append("bgez ").append(MipsRegister.getRegister(V0)).append(", ").append(divLabel1).append("\n");
            sb.append("subu ").append(MipsRegister.getRegister(V0)).append(", $zero, ").append(MipsRegister.getRegister(V0)).append("\n");
            for (int i = 1; i < mipsInstructions.size(); i++) {
                sb.append(mipsInstructions.get(i).toString());
            }
            sb.append("sub ").append(MipsRegister.getRegister(this.reg1)).append(", $zero, ").append(MipsRegister.getRegister(this.reg1)).append("\n");
            sb.append("j ").append(divLabel2).append("\n");
            sb.append(divLabel1).append(":\n");
            for (int i = 1; i < mipsInstructions.size(); i++) {
                sb.append(mipsInstructions.get(i).toString());
            }
            sb.append(divLabel2).append(":\n");
        } else {
            for (MipsInstruction mipsInstruction : mipsInstructions) {
                sb.append(mipsInstruction.toString());
            }
        }
        return sb.toString();
    }


    /**
     * 优化后MIPS生成
     * 若存在立即数的情况，第一个立即数用V0，第二个立即数用V1
     */
    public void genOptimizeMips() {
        if (irInstruction != null) {
            if (irInstruction instanceof IrAlloc) {
                genOptimizeAllocToMips();
            } else if (irInstruction instanceof IrGetElementPtr) {
                genOptimizeGetElementPtrToMips();
            } else if (irInstruction instanceof IrStore) {
                genOptimizeStoreToMips();
            } else if (irInstruction instanceof IrAdd) {
                genOptimizeAddToMips();
            } else if (irInstruction instanceof IrSub) {
                genOptimizeSubToMips();
            } else if (irInstruction instanceof IrBr) {
                genOptimizeBrToMips();
            } else if (irInstruction instanceof IrCall) {
                genOptimizeCallToMips();
            } else if (irInstruction instanceof IrCmp) {
                genOptimizeCmpToMips();
            } else if (irInstruction instanceof IrLoad) {
                genOptimizeLoadToMips();
            } else if (irInstruction instanceof IrMul) {
                genOptimizeMulToMips();
            } else if (irInstruction instanceof IrRet) {
                genOptimizeRetToMips();
            } else if (irInstruction instanceof IrSdiv) {
                genOptimizeDivToMips();
            } else if (irInstruction instanceof IrSrem) {
                genOptimizeModToMips();
            } else if (irInstruction instanceof IrMove) {
                genOptimizeMoveToMips();
            } else if (irInstruction instanceof IrZext) {
                genOptimizeZextToMips();
            }
        }
    }

    private void genOptimizeZextToMips() {
        if (irInstruction.isInMem) {
            irInstruction.reg = V1;
        }
        Value op = irInstruction.usedValues.get(0);
        op2ToReg(op);
        if (irInstruction.reg != op.reg) {
            mipsInstructions.add(new Move(irInstruction.reg, op.reg));
        }
    }

    private void genOptimizeMoveToMips() {
        Value target = ((IrMove) irInstruction).target;
        Value source = ((IrMove) irInstruction).source;
        op2ToReg(source);
        if (target.isInMem) {
            target.reg = V1;
        }

        if (source.reg != target.reg) {
            mipsInstructions.add(new Move(target.reg, source.reg));
        }
    }

    private void genOptimizeModToMips() {
        Value op1 = irInstruction.usedValues.get(0);
        op1ToReg(op1);
        Value op2 = irInstruction.usedValues.get(1);
        op2ToReg(op2);
        mipsInstructions.add(new Div(op1.reg, op2.reg));
        if (irInstruction.isInMem) {
            irInstruction.reg = V1;
        }
        mipsInstructions.add(new Mfhi(irInstruction.reg)); // Hi取模
    }

    private void genOptimizeDivToMips() {
        Value op1 = irInstruction.usedValues.get(0);
        Value op2 = irInstruction.usedValues.get(1);
        if (irInstruction.isInMem) {
            irInstruction.reg = V1;
        }
        // 除法优化，需要我们保证除数已知
        if (op2.isNum()) {
            // 还需要判断被除数是否小于0，得手动添加基本块
            /*
                bgez op1.reg, tempLabel1
                sub op1.reg, $0, op1.reg
                ...
                sub ir.reg, $0, ir.reg
                tempLabel1:
                ... 重复
             */
            int d = Integer.parseInt(op2.value), cnt;
            int flag = 0, l; // 标记最后是否需要做取负数
            if (d < 0) {
                flag = 1;
                d = -d;
            }
//            if ((cnt = getLeftShiftNum(d)) != -1) {
//                op1ToReg(op1);
//                mipsInstructions.add(new Srl(irInstruction.reg, op1.reg, cnt));
//                if (flag == 1) {
//                    mipsInstructions.add(new Sub(irInstruction.reg, ZERO, irInstruction.reg));
//                }
//                return;
//            }

            BigDecimal left, right, m = BigDecimal.valueOf(0);
            for (l = 0; l < 32; l++) {
                left = BigDecimal.valueOf(2).pow(32 + l);
                right = BigDecimal.valueOf(2).pow(32 + l).add(BigDecimal.valueOf(2).pow(l));
                m = left.divide(BigDecimal.valueOf(d), 0, 0); // 直接进位
                if (m.multiply(BigDecimal.valueOf(d)).compareTo(right) <= 0) {
                    break;
                }
            }
            // 得到了m和l
            if (l < 32 && m.compareTo(BigDecimal.valueOf(2).pow(32)) < 0) {
                System.out.println("进行除法优化！！！！！！！");
                this.divLabel1 = curMipsFunction.allocDivLabel();
                this.divLabel2 = curMipsFunction.allocDivLabel();
                this.reg1 = irInstruction.reg;
                if (op1.isInMem()) {
                    mipsInstructions.add(new Lw(V0, op1.offset, FP));
                } else {
                    mipsInstructions.add(new Move(V0, op1.reg));
                }
                mipsInstructions.add(new Li(V1, m.intValue()));
                mipsInstructions.add(new Multu(V0, V1));
                mipsInstructions.add(new Mfhi(V0));
                mipsInstructions.add(new Srl(irInstruction.reg, V0, l));
                if (flag == 1) {
                    mipsInstructions.add(new Sub(irInstruction.reg, ZERO, irInstruction.reg));
                }
                return;
            }
        }
        op1ToReg(op1);
        op2ToReg(op2);
        mipsInstructions.add(new Div(op1.reg, op2.reg));
        if (irInstruction.reg == -1) {
            throw new RuntimeException();
        }
        mipsInstructions.add(new Mflo(irInstruction.reg)); // Lo取商
    }

    private void genOptimizeRetToMips() {
        if (!irInstruction.usedValues.isEmpty()) {
            Value ret = irInstruction.usedValues.get(0);
            op1ToReg(ret);
            if (ret.reg != V0 && !curMipsFunction.isMain) {
                mipsInstructions.add(new Move(V0, ret.reg));
            }
            if (!curMipsFunction.isMain) {
                mipsInstructions.add(new Jr());
            } else {
                mipsInstructions.add(new Li(V0, 10));
                mipsInstructions.add(new Syscall());
            }
        } else {
            mipsInstructions.add(new Jr());
        }
    }

    // 能左移返回左移位数，不能左移返回-1
    private int getLeftShiftNum(int num) {
        int shiftNum = 0;
        while (num % 2 == 0) {
            num /= 2;
            shiftNum++;
        }
        if (num == 1) {
            return shiftNum;
        } else {
            return -1;
        }
    }

    private void genOptimizeMulToMips() {
        // 可以保证不会同时两个Num
        Value op1 = irInstruction.usedValues.get(0);
        Value op2 = irInstruction.usedValues.get(1);
        if (irInstruction.isInMem) {
            irInstruction.reg = V1;
        }
        int num, cnt, flag = 0;
        if (op1.isNum()) {
            num = Integer.parseInt(op1.value);
            if (num < 0) {
                flag = 1;
                num = -num;
            }
            if (num == 0) {
                mipsInstructions.add(new Li(irInstruction.reg, 0));
                return;
            } else if (num == 1) {
                op2ToReg(op2);
                mipsInstructions.add(new Move(irInstruction.reg, op2.reg));
                if (flag == 1) {
                    mipsInstructions.add(new Sub(irInstruction.reg, ZERO, irInstruction.reg));
                }
                return;
            } else if ((cnt = getLeftShiftNum(num)) != -1) {
                op2ToReg(op2);
                mipsInstructions.add(new Sll(irInstruction.reg, op2.reg, cnt));
                if (flag == 1) {
                    mipsInstructions.add(new Sub(irInstruction.reg, ZERO, irInstruction.reg));
                }
                return;
            }
        } else if (op2.isNum()) {
            num = Integer.parseInt(op2.value);
            if (num < 0) {
                flag = 1;
                num = -num;
            }
            if (num == 0) {
                mipsInstructions.add(new Li(irInstruction.reg, 0));
                return;
            } else if (num == 1) {
                op1ToReg(op1);
                mipsInstructions.add(new Move(irInstruction.reg, op1.reg));
                if (flag == 1) {
                    mipsInstructions.add(new Sub(irInstruction.reg, ZERO, irInstruction.reg));
                }
                return;
            } else if ((cnt = getLeftShiftNum(num)) != -1) {
                op1ToReg(op1);
                mipsInstructions.add(new Sll(irInstruction.reg, op1.reg, cnt));
                if (flag == 1) {
                    mipsInstructions.add(new Sub(irInstruction.reg, ZERO, irInstruction.reg));
                }
                return;
            }
        }
        op1ToReg(op1);
        op2ToReg(op2);
        mipsInstructions.add(new Mult(op1.reg, op2.reg));
        if (irInstruction.reg == -1) {
            System.out.println(irInstruction);
            throw new RuntimeException();
        }
        mipsInstructions.add(new Mflo(irInstruction.reg)); // Lo取积
    }

    private void op1ToReg(Value op1) {
        if (op1.isNum()) {
            if (Integer.parseInt(op1.value) == 0) {
                op1.reg = ZERO;
            } else {
                mipsInstructions.add(new Li(V0, Integer.parseInt(op1.value)));
                op1.reg = V0;
            }
            return;
        } else if (op1.isInMem()) {
            mipsInstructions.add(new Lw(V0, op1.offset, FP));
            op1.reg = V0;
            return;
        }
    }

    private void op2ToReg(Value op2) {
        if (op2.isNum()) {
            if (Integer.parseInt(op2.value) == 0) {
                op2.reg = ZERO;
            } else {
                mipsInstructions.add(new Li(V1, Integer.parseInt(op2.value)));
                op2.reg = V1;
            }
            return;
        } else if (op2.isInMem()) {
            mipsInstructions.add(new Lw(V1, op2.offset, FP));
            op2.reg = V1;
            return;
        }
    }

    private void genOptimizeCmpToMips() {
        // 可能存在立即数的情况，可后续使用常量传播
        Value cmp1 = irInstruction.usedValues.get(0);
        op1ToReg(cmp1);
        Value cmp2 = irInstruction.usedValues.get(1);
        op2ToReg(cmp2);
        if (irInstruction.isInMem) {
            irInstruction.reg = V1;
        }
        switch (((IrCmp) irInstruction).getCmpOp()) {
            case EQL -> mipsInstructions.add(new Seq(irInstruction.reg, cmp1.reg, cmp2.reg));
            case NEQ -> mipsInstructions.add(new Sne(irInstruction.reg, cmp1.reg, cmp2.reg));
            case LEQ -> mipsInstructions.add(new Sle(irInstruction.reg, cmp1.reg, cmp2.reg));
            case LSS -> mipsInstructions.add(new Slt(irInstruction.reg, cmp1.reg, cmp2.reg));
            case GEQ -> mipsInstructions.add(new Sge(irInstruction.reg, cmp1.reg, cmp2.reg));
            case GRE -> mipsInstructions.add(new Sgt(irInstruction.reg, cmp1.reg, cmp2.reg));
        }
    }

    private void f2p(Value fparam, Integer A) {
        if (fparam.isNum()) {
            mipsInstructions.add(new Li(A, Integer.parseInt(fparam.value)));
        } else if (fparam.isInMem) {
            mipsInstructions.add(new Lw(A, fparam.offset, FP));
        } else {
            mipsInstructions.add(new Move(A, fparam.reg));
        }
    }

    private void genOptimizeCallToMips() {
        ArrayList<Value> values = irInstruction.usedValues;
        if (irInstruction.isInMem) {
            irInstruction.reg = V1;
        }
        Value funcValue = values.get(values.size() - 1);
        switch (funcValue.value) {
            case "@putint": {
                Value value = values.get(0);
                // 此时可能会产生A0冲突
                if (value.isNum()) {
                    mipsInstructions.add(new Li(A0, Integer.parseInt(value.value)));
                    mipsInstructions.add(new Li(V0, 1));
                    mipsInstructions.add(new Syscall());
                } else if (value.isInMem) {
                    mipsInstructions.add(new Lw(A0, value.offset, FP));
                    mipsInstructions.add(new Li(V0, 1));
                    mipsInstructions.add(new Syscall());
                } else {
                    mipsInstructions.add(new Move(A0, value.reg));
                    mipsInstructions.add(new Li(V0, 1));
                    mipsInstructions.add(new Syscall());
                }
                break;
            }
            case "@getint": {
                mipsInstructions.add(new Li(V0, 5));
                mipsInstructions.add(new Syscall());
                mipsInstructions.add(new Move(irInstruction.reg, V0));
                break;
            }
            default: {
                /*
                    正经的函数, 优化后的版本
                    1. 将out的所有活跃变量入FP
                    2. 参数的传递需要考量，可能会出现很多冲突，重点
                    3. 将当前FP, RA保存到SP中
                    4. 跳转
                    5. 将FP,RA从SP中返回
                    6. 从FP中返回所有活跃变量
                 */
                Set<Value> actives = irInstruction.out;
                HashMap<Value, Integer> activeValueInMem = new HashMap<>(); // 记录Value(Reg)到内存偏移的映射
                int spOffset = 0;
                mipsInstructions.add(new Sw(RA, -spOffset, SP));
                spOffset += 4;
                mipsInstructions.add(new Sw(FP, -spOffset, SP));
                spOffset += 4;
                // 将活跃变量存入内存
                for (Value value : actives) {
                    if (!value.equal(irInstruction)) {
                        activeValueInMem.put(value, spOffset);
                        mipsInstructions.add(new Sw(value.reg, -spOffset, SP));
                        spOffset += 4;
                    }
                }
                mipsInstructions.add(new Addi(SP, SP, -spOffset));

                // 将实参传给调用函数的形参 存在 寄存器 -> 寄存器 的关系
                ArrayList<IrArgument> pParams = ((IrFunction) funcValue).getIrArguments();
                int tempSpOffset = 0;
                for (int i = 0; i < values.size() - 1; i++) {
                    Value fparam = values.get(i);
                    if (i < 4) {
                        switch (i) {
                            case 0: {
                                f2p(fparam, A0);
                                break;
                            }
                            case 1: {
                                f2p(fparam, A1);
                                break;
                            }
                            case 2: {
                                f2p(fparam, A2);
                                break;
                            }
                            case 3: {
                                f2p(fparam, A3);
                                break;
                            }
                        }
                    } else {
                        op1ToReg(fparam);
                        if (!pParams.get(i).isInMem() && pParams.get(i).reg == -1) {
                            continue;
                        } else if (pParams.get(i).isInMem()) {
                            mipsInstructions.add(new Sw(values.get(i).reg, pParams.get(i).offset + curMipsFunction.fpOffset, FP));
                        } else {
                            // 剩下的是形参也在mem里的情况
                            // 双方都不在mem里
                            if (values.get(i).reg == pParams.get(i).reg) {
                                continue;
                            } else if (canDirectMove(values, i, pParams.get(i).reg)) {
                                // 能直接移动
                                mipsInstructions.add(new Move(pParams.get(i).reg, values.get(i).reg));
                            } else {
                                // 直接移动会冲突，在后面的实参有用到reg这个寄存器的 借用栈空间
                                mipsInstructions.add(new Sw(values.get(i).reg, -tempSpOffset, SP));
                                pParams.get(i).isInTempStack = true;
                                tempSpOffset += 4;
                            }
                        }
                    }
                }

                mipsInstructions.add(new Addi(FP, FP, curMipsFunction.fpOffset)); // 将栈帧调整为被调用函数的基地址
                tempSpOffset = 0;
                for (IrArgument irArgument : pParams) {
                    if (irArgument.isInTempStack) {
                        mipsInstructions.add(new Lw(irArgument.reg, -tempSpOffset, SP));
                        tempSpOffset += 4;
                        irArgument.isInTempStack = false;
                    }
                }
                // 执行跳转
                mipsInstructions.add(new Jal(funcValue.value));
                // 返回后将现场返回
                mipsInstructions.add(new Addi(SP, SP, spOffset));
                mipsInstructions.add(new Lw(RA, 0, SP));
                mipsInstructions.add(new Lw(FP, -4, SP));
                for (Value value : actives) {
                    if (!value.equal(irInstruction)) {
                        mipsInstructions.add(new Lw(value.reg, -activeValueInMem.get(value), SP));
                    }
                }
                // 记录返回值
                if (!irInstruction.value.equals("!noreturn")) {
                    mipsInstructions.add(new Move(irInstruction.reg, V0));
                }
                break;
            }
        }
    }

    // 在位置i之后 values中不存在使用reg的寄存器
    private boolean canDirectMove(ArrayList<Value> values, int pos, int reg) {
        for (int i = pos + 1; i < values.size() - 1; i++) {
            if (values.get(i).reg == reg) {
                return false;
            }
        }
        return true;
    }

    private void genOptimizeBrToMips() {
        switch (((IrBr) irInstruction).getType()) {
            case 0: {
                // 直接跳转
                mipsInstructions.add(new J(irInstruction.usedValues.get(0).value));
                break;
            }
            case 1: {
                Value cmp = irInstruction.usedValues.get(0);
                op1ToReg(cmp);
                String label1 = irInstruction.usedValues.get(1).value;
                String label2 = irInstruction.usedValues.get(2).value;
                mipsInstructions.add(new Bne(cmp.reg, ZERO, label1));
                mipsInstructions.add(new Beq(cmp.reg, ZERO, label2));
                break;
            }
        }
    }

    private void genOptimizeSubToMips() {
        if (irInstruction.isInMem) {
            irInstruction.reg = V1;
        }
        Value op1 = irInstruction.usedValues.get(0);
        op1ToReg(op1);
        Value op2 = irInstruction.usedValues.get(1);
        op2ToReg(op2);
        mipsInstructions.add(new Sub(irInstruction.reg, op1.reg, op2.reg));
    }

    private void genOptimizeAddToMips() {
        if (irInstruction.isInMem) {
            irInstruction.reg = V1;
        }
        Value op1 = irInstruction.usedValues.get(0);
        op1ToReg(op1);
        Value op2 = irInstruction.usedValues.get(1);
        op2ToReg(op2);
        mipsInstructions.add(new Add(irInstruction.reg, op1.reg, op2.reg));
    }

    private void genOptimizeLoadToMips() {
        if (irInstruction.isInMem) {
            irInstruction.reg = V1;
        }
        Value addr = irInstruction.usedValues.get(0);
        op1ToReg(addr);
        if (addr.isGlobal()) {
            mipsInstructions.add(new Lw(irInstruction.reg, addr.offset, GP));
        } else {
            mipsInstructions.add(new Lw(irInstruction.reg, 0, addr.reg));
        }
    }

    private void genOptimizeStoreToMips() {
        Value value = irInstruction.usedValues.get(0);
        Value addr = irInstruction.usedValues.get(1);
        if (irInstruction.valueType == 11) {
            // 特殊情况，将v1存入即可
            mipsInstructions.add(new Sw(V1, value.offset, FP));
        } else {
            op1ToReg(value); // value有可能是常数
            if (addr.isGlobal()) {
                mipsInstructions.add(new Sw(value.reg, addr.offset, GP));
            } else {
                op2ToReg(addr);
                mipsInstructions.add(new Sw(value.reg, 0, addr.reg));
            }
        }
    }

    private void genOptimizeGetElementPtrToMips() {
        if (irInstruction.isInMem) {
            irInstruction.reg = V1;
        }
        int tarAddrReg = irInstruction.reg;
        Value baseAddr = irInstruction.usedValues.get(0);
        int shiftCnt = 0;
        // 先计算偏移，最后的结果用V1保存
        switch (baseAddr.valueType) {
            case 1: {
                // i32*
                if (irInstruction.valueType == 1) {
                    Value index = irInstruction.usedValues.get(1);
                    if (index.isNum()) {
                        // 偏移量是数
                        mipsInstructions.add(new Li(V1, Integer.parseInt(index.value) * 4));
                    } else {
                        if (index.isInMem()) {
                            mipsInstructions.add(new Lw(V1, index.offset, FP));
                            index.reg = V1;
                        }
                        mipsInstructions.add(new Sll(V1, index.reg, 2));
                    }
                }
                break;
            }
            case 2: {
                int lineSize = baseAddr.firstSize;
                switch (irInstruction.valueType) {
                    case 2: {
                        // 1个index
                        Value index = irInstruction.usedValues.get(1);
                        if (index.isNum()) {
                            mipsInstructions.add(new Li(V1, Integer.parseInt(index.value) * lineSize * 4));
                        } else {
                            // fp + offset + linesize * 4 * reg
                            if (index.isInMem()) {
                                mipsInstructions.add(new Lw(V1, index.offset, FP));
                                index.reg = V1;
                            }
                            mipsInstructions.add(new Sll(V1, index.reg, 2));
                            if ((shiftCnt = getLeftShiftNum(lineSize)) != -1) {
                                mipsInstructions.add(new Sll(V1, V1, shiftCnt));
                            } else {
                                mipsInstructions.add(new Mul(V1, V1, lineSize));
                            }
                        }
                        break;
                    }
                    case 1: {
                        // 两个index (index1 * linesize + index2) * 4
                        Value index1 = irInstruction.usedValues.get(1);
                        Value index2 = irInstruction.usedValues.get(2);
                        if (index1.isNum() && index2.isNum()) {
                            mipsInstructions.add(new Li(V1, (Integer.parseInt(index1.value) * lineSize + Integer.parseInt(index2.value)) * 4));
                        } else {
                            if (!index1.isNum()) {
                                // 不能改变index1.reg的值，用V1作一步承接
                                if (index1.isInMem()) {
                                    mipsInstructions.add(new Lw(V1, index1.offset, FP));
                                    index1.reg = V1;
                                }
                                if ((shiftCnt = getLeftShiftNum(lineSize)) != -1) {
                                    mipsInstructions.add(new Sll(V1, index1.reg, shiftCnt));
                                } else {
                                    mipsInstructions.add(new Mul(V1, index1.reg, lineSize));
                                }
                                if (!index2.isNum()) {
                                    if (index2.isInMem()) {
                                        mipsInstructions.add(new Lw(V0, index2.offset, FP));
                                        index2.reg = V0;
                                    }
                                    mipsInstructions.add(new Add(V1, V1, index2.reg));
                                } else {
                                    mipsInstructions.add(new Addi(V1, V1, Integer.parseInt(index2.value)));
                                }
                            } else {
                                if (index2.isInMem()) {
                                    mipsInstructions.add(new Lw(V0, index2.offset, FP));
                                    index2.reg = V0;
                                }
                                mipsInstructions.add(new Addi(V1, index2.reg, lineSize * Integer.parseInt(index1.value)));
                            }
                            mipsInstructions.add(new Sll(V1, V1, 2));
                        }
                        break;
                    }
                }
                break;
            }
            case 3: {
                // 和上一种情况非常类似可提取
                int dim2 = baseAddr.secondSize;
                switch (irInstruction.valueType) {
                    // 第一个Index一定是0，只需求二三个即可
                    case 1: {
                        // 三个index
                        Value index1 = irInstruction.usedValues.get(2);
                        Value index2 = irInstruction.usedValues.get(3);
                        if (index1.isNum() && index2.isNum()) {
                            mipsInstructions.add(new Li(V1, (Integer.parseInt(index1.value) * dim2 + Integer.parseInt(index2.value)) * 4));
                        } else {
                            if (!index1.isNum()) {
                                if (index1.isInMem()) {
                                    mipsInstructions.add(new Lw(V1, index1.offset, FP));
                                    index1.reg = V1;
                                }
                                if ((shiftCnt = getLeftShiftNum(dim2)) != -1) {
                                    mipsInstructions.add(new Sll(V1, index1.reg, shiftCnt));
                                } else {
                                    mipsInstructions.add(new Mul(V1, index1.reg, dim2));
                                }
                                if (!index2.isNum()) {
                                    if (index2.isInMem()) {
                                        mipsInstructions.add(new Lw(V0, index2.offset, FP));
                                        index2.reg = V0;
                                    }
                                    mipsInstructions.add(new Add(V1, V1, index2.reg));
                                } else {
                                    mipsInstructions.add(new Addi(V1, V1, Integer.parseInt(index2.value)));
                                }
                            } else {
                                if (index2.isInMem()) {
                                    mipsInstructions.add(new Lw(V0, index2.offset, FP));
                                    index2.reg = V0;
                                }
                                mipsInstructions.add(new Addi(V1, index2.reg, dim2 * Integer.parseInt(index1.value)));
                            }
                            mipsInstructions.add(new Sll(V1, V1, 2));
                        }
                        break;
                    }
                    case 2: {
                        Value index = irInstruction.usedValues.get(2);
                        if (index.isNum()) {
                            mipsInstructions.add(new Li(V1, Integer.parseInt(index.value) * dim2 * 4));
                        } else {
                            // fp + offset + linesize * 4 * reg
                            if (index.isInMem()) {
                                mipsInstructions.add(new Lw(V1, index.offset, FP));
                                index.reg = V1;
                            }
                            mipsInstructions.add(new Sll(V1, index.reg, 2));

                            if ((shiftCnt = getLeftShiftNum(dim2)) != -1) {
                                mipsInstructions.add(new Sll(V1, V1, shiftCnt));
                            } else {
                                mipsInstructions.add(new Mul(V1, V1, dim2));
                            }
                        }
                        break;
                    }
                }
            }
        }

        // 计算目标地址

        // 此时V0中记录基地址, V1记录偏移，baseAddr也许后续需要使用，先计算偏移也许可行
        if (baseAddr.isGlobal()) {
            // 涉及全局变量基地址如 %VirtualReg1 = getelementptr [1 x i32], [1 x i32]* @a, i32 0, i32 0
            mipsInstructions.add(new Addi(V0, GP, baseAddr.offset));
            mipsInstructions.add(new Add(tarAddrReg, V0, V1));
        } else if (baseAddr.isInMem()) {
            mipsInstructions.add(new Lw(V0, baseAddr.offset, FP));
            mipsInstructions.add(new Add(tarAddrReg, V0, V1));
        } else {
            // 涉及局部变量基地址且寄存器不同
            mipsInstructions.add(new Add(tarAddrReg, baseAddr.reg, V1));
        }
    }

    private void genOptimizeAllocToMips() {
        if (irInstruction.isInMem) {
            irInstruction.reg = V1;
        }
        mipsInstructions.add(new Addi(irInstruction.reg, FP, curMipsFunction.fpOffset));
        switch (irInstruction.valueType) {
            // offset后移
            case 2: {
                curMipsFunction.fpOffset += 4 * irInstruction.firstSize;
                break;
            }
            case 3: {
                curMipsFunction.fpOffset += 4 * irInstruction.firstSize * irInstruction.secondSize;
                break;
            }
        }
    }
}
