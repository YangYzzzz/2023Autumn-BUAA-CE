package BackEnd.MipsCore;

import BackEnd.MipsInstruction.Li;
import BackEnd.MipsInstruction.MipsInstruction;
import BackEnd.MipsInstruction.Sw;
import MidEnd.IrCore.IrGlobalDef;

import java.util.ArrayList;

import static BackEnd.MipsCore.MipsRegister.GP;
import static BackEnd.MipsCore.MipsRegister.T0;

public class MipsGlobal {
    private ArrayList<IrGlobalDef> irGlobalDefs;
    private ArrayList<MipsInstruction> mipsInstructions;
    private int gpOffset;
    public MipsGlobal(ArrayList<IrGlobalDef> irGlobalDefs) {
        this.irGlobalDefs = irGlobalDefs;
        mipsInstructions = new ArrayList<>();
        gpOffset = 0;
    }

    public void genMips() {
        // 仅获取全局符号表
        MipsInstruction mipsInstruction;
        ArrayList<Integer> initials;
        for (IrGlobalDef irGlobalDef : irGlobalDefs) {
            // 得到符号表对应符号
            irGlobalDef.offset = gpOffset;
            irGlobalDef.isGp = true;
            initials = irGlobalDef.getInitials();
            switch (irGlobalDef.valueType) {
                case 1: {
                    // 零维常数
                    mipsInstruction = new Li(T0, initials.get(0));
                    mipsInstructions.add(mipsInstruction);
                    mipsInstruction = new Sw(T0, gpOffset, GP);
                    mipsInstructions.add(mipsInstruction);
                    gpOffset += 4;
                    break;
                }
                case 2: {
                    // 一维数组
                    int dim = irGlobalDef.firstSize;
                    int flag = 0;
                    for (int i = 0; i < dim; i++) {
                        if (initials.get(i) != 0) {
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 1) {
                        for (int i = 0; i < dim; i++) {
                            if (initials.get(i) != 0) {
                                mipsInstruction = new Li(T0, initials.get(i));
                                mipsInstructions.add(mipsInstruction);
                                mipsInstruction = new Sw(T0, gpOffset, GP);
                                mipsInstructions.add(mipsInstruction);
                            }
                            gpOffset += 4;
                        }
                    } else {
                        gpOffset += 4 * dim;
                    }
                    break;
                }
                case 3: {
                    // 二维数组
                    int dim1 = irGlobalDef.firstSize;
                    int dim2 = irGlobalDef.secondSize;
                    int flag = 0;
                    for (int i = 0; i < dim1 * dim2; i++) {
                        if (initials.get(i) != 0) {
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 1) {
                        for (int i = 0; i < dim1 * dim2; i++) {
                            if (initials.get(i) != 0) {
                                mipsInstruction = new Li(T0, initials.get(i));
                                mipsInstructions.add(mipsInstruction);
                                mipsInstruction = new Sw(T0, gpOffset, GP);
                                mipsInstructions.add(mipsInstruction);
                            }
                            gpOffset += 4;
                        }
                    } else {
                        gpOffset += 4 * dim1 * dim2;
                    }
                    break;
                }
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (MipsInstruction mipsInstruction : mipsInstructions) {
            sb.append(mipsInstruction.toString());
        }
        return sb.toString();
    }
}
