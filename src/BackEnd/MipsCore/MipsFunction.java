package BackEnd.MipsCore;

import BackEnd.MipsInstruction.MipsInstruction;
import BackEnd.MipsInstruction.Move;
import BackEnd.MipsInstruction.Sw;
import MidEnd.IrCore.IrArgument;
import MidEnd.IrCore.IrBasicBlock;
import MidEnd.IrCore.IrFunction;

import java.util.ArrayList;

import static BackEnd.MipsCore.MipsRegister.A0;
import static BackEnd.MipsCore.MipsRegister.A1;
import static BackEnd.MipsCore.MipsRegister.A2;
import static BackEnd.MipsCore.MipsRegister.A3;
import static BackEnd.MipsCore.MipsRegister.FP;
import static MidEnd.IrCore.IrModule.MODE;

public class MipsFunction {
    public static MipsFunction curMipsFunction;
    public int fpOffset;
    public Boolean isMain;
    public MipsRegister mipsRegister;
    private IrFunction irFunction;
    private ArrayList<MipsBasicBlock> mipsBasicBlocks;
    private ArrayList<MipsInstruction> paramsInstrutions;
    static int cnt = -1;
    public MipsFunction(IrFunction irFunction) {
        isMain = irFunction.value.equals("@main");
        this.paramsInstrutions = new ArrayList<>();
        this.mipsRegister = new MipsRegister();
        this.irFunction = irFunction;
        this.mipsBasicBlocks = new ArrayList<>();
        for (IrBasicBlock irBasicBlock : irFunction.getIrBasicBlocks()) {
            MipsBasicBlock mipsBasicBlock = new MipsBasicBlock(irBasicBlock);
            mipsBasicBlocks.add(mipsBasicBlock);
        }
        if (MODE == 1) {
            this.fpOffset = irFunction.getIrFpOffset(); // 设置定义数组时的起始偏移
        } else {
            this.fpOffset = 0;
        }
    }

    public String allocDivLabel() {
        cnt++;
        return "divLabel" + cnt;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(irFunction.value.substring(1)).append(":\n");
        for (MipsInstruction instruction : paramsInstrutions) {
            sb.append(instruction.toString());
        }
        for (MipsBasicBlock mipsBasicBlock : mipsBasicBlocks) {
            sb.append(mipsBasicBlock.toString());
        }
        sb.append("\n\n");
        return sb.toString();
    }

    private void a2reg(IrArgument irArgument, Integer A) {
        if (irArgument.isInMem) {
            paramsInstrutions.add(new Sw(A, irArgument.offset, FP));
        } else {
            paramsInstrutions.add(new Move(irArgument.reg, A));
        }
    }

    public void genMips() {
        // System.out.println("当前函数为: " + irFunction.value);
        curMipsFunction = this;
        if (MODE == 1) {
            // 开启优化，此时都函数的参数所对应的寄存器都传好了值，直接翻译指令即可
            for (int i = 0; i < irFunction.getIrArguments().size() && i < 4; i++) {
                IrArgument irArgument = irFunction.getIrArguments().get(i);
                switch (i) {
                    case 0: {
                        a2reg(irArgument, A0);
                        break;
                    }
                    case 1: {
                        a2reg(irArgument, A1);
                        break;
                    }
                    case 2: {
                        a2reg(irArgument, A2);
                        break;
                    }
                    case 3: {
                        a2reg(irArgument, A3);
                        break;
                    }
                }
            }
            for (MipsBasicBlock mipsBasicBlock : mipsBasicBlocks) {
                // 传进去当前偏移 返回Block解析过的偏移
                mipsBasicBlock.genOptimizeMips();
            }
        } else {
            // 将参数传递，规定从左往右压栈
            // 前四个参数占用栈空间，但是里面不存东西
            for (int i = 0; i < irFunction.getIrArguments().size(); i++) {
                IrArgument irArgument = irFunction.getIrArguments().get(i);
                // 一定能申请到四个s寄存器
                int sTemp = curMipsFunction.mipsRegister.allocSReg();
                // 将参数先处理一遍
                switch (i) {
                    case 0: {
                        paramsInstrutions.add(new Move(sTemp, A0));
                        irArgument.setReg(sTemp);
                        fpOffset += 4;
                        break;
                    }
                    case 1: {
                        paramsInstrutions.add(new Move(sTemp, A1));
                        irArgument.setReg(sTemp);
                        fpOffset += 4;
                        break;
                    }
                    case 2: {
                        paramsInstrutions.add(new Move(sTemp, A2));
                        irArgument.setReg(sTemp);
                        fpOffset += 4;
                        break;
                    }
                    case 3: {
                        paramsInstrutions.add(new Move(sTemp, A3));
                        irArgument.setReg(sTemp);
                        fpOffset += 4;
                        break;
                    }
                    default: {
                        irArgument.offset = fpOffset;
                        irFunction.isFp = true;
                        fpOffset += 4;
                        break;
                    }
                }
            }
            for (MipsBasicBlock mipsBasicBlock : mipsBasicBlocks) {
                // 传进去当前偏移 返回Block解析过的偏移
                mipsBasicBlock.genMips();
            }
        }
    }
}
