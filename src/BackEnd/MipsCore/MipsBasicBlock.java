package BackEnd.MipsCore;

import BackEnd.MipsInstruction.Asciiz;
import MidEnd.IrCore.IrBasicBlock;
import MidEnd.IrCore.IrInstruction;
import MidEnd.IrInstuctions.IrCall;
import MidEnd.IrCore.Value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static BackEnd.MipsCore.MipsModule.mipsModule;
import static MidEnd.IrCore.IrModule.MODE;

public class MipsBasicBlock {
    private String blockLabel;
    private ArrayList<MipsInstructionBlock> instructionBlocks; // 每条LLVM可能对应多条Mips!

    public MipsBasicBlock(IrBasicBlock irBasicBlock) {
        this.blockLabel = irBasicBlock.value;
        instructionBlocks = new ArrayList<>();
        ArrayList<IrInstruction> irInstructions = irBasicBlock.getInstructions();
        for (int i = 0; i < irInstructions.size(); i++) {
            // 输出字符特殊处理
            StringBuilder sb = new StringBuilder();
            Set<Value> outs = new HashSet<>();
            while (i < irInstructions.size() && irInstructions.get(i) instanceof IrCall && irInstructions.get(i).usedValues.size() == 2
                    && irInstructions.get(i).usedValues.get(1).value.equals("@putch")) {
                sb.append((char)Integer.parseInt(irInstructions.get(i).usedValues.get(0).value));
                outs = irInstructions.get(i).out; // 判断是否需要转移A0
                i++;
            }
            if (!sb.isEmpty()) {
                Asciiz asciiz = new Asciiz(sb.toString());
                mipsModule.addGlobalString(asciiz);
                if (MODE == 1) {
                    instructionBlocks.add(new MipsInstructionBlock(asciiz, outs));
                } else {
                    instructionBlocks.add(new MipsInstructionBlock(asciiz));
                }
            }
            if (i < irInstructions.size()) {
                instructionBlocks.add(new MipsInstructionBlock(irInstructions.get(i)));
            }
        }
    }

    public void genMips() {
        for (MipsInstructionBlock mipsInstructionBlock : instructionBlocks) {
            mipsInstructionBlock.genMips(); // 返回的应该是一个寄存器，即返回值
        }
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!blockLabel.equals("delete")) {
            sb.append(blockLabel.substring(1)).append(":\n");
        }
        sb.append("# New Basic Block\n");
        for (MipsInstructionBlock mipsInstructionBlock : instructionBlocks) {
            sb.append(mipsInstructionBlock.toString());
        }
        return sb + "\n";
    }

    public void genOptimizeMips() {
        for (MipsInstructionBlock mipsInstructionBlock : instructionBlocks) {
            mipsInstructionBlock.genOptimizeMips(); // 返回的应该是一个寄存器，即返回值
        }
    }
}
