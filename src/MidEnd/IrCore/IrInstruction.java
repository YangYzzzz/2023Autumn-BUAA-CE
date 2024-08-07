package MidEnd.IrCore;

import MidEnd.IrInstuctions.IrMove;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static MidEnd.IrCore.IrFunction.ACTIVE_VARIABLE_FLAG;

public class IrInstruction extends User {
    public Set<Value> in;
    public Set<Value> out;


    // 创建一条指令 准备好返回值和使用值 先不考虑Use互相关联
    public IrInstruction(String value, int valueType) {
        super(value, valueType);
        in = new HashSet<>();
        out = new HashSet<>();
    }

    public IrInstruction(String value, int valueType, ArrayList<Value> ops) {
        super(value, valueType, ops);
        in = new HashSet<>();
        out = new HashSet<>();
    }

    public IrInstruction(String value, int valueType, int firstSize, int secondSize) {
        super(value, valueType, firstSize, secondSize);
        in = new HashSet<>();
        out = new HashSet<>();
    }

    /*
        当前指令的Out集来自于后继的in集
        是否需要修改 %3 = %1 + %2 此时3无法分配寄存器 则
        %3 = %1 + %2
        store %3 offset xx

     */
    public void genActiveVariable(ArrayList<IrInstruction> ins) {
        for (IrInstruction instruction : ins) {
            for (Value in : instruction.in) {
                if (!out.contains(in)) {
                    ACTIVE_VARIABLE_FLAG = 1;
                    out.add(in);
                }
            }
        }

        // in = use 交 (out - def)
        // eg: %3 = %1 + %2
        in.addAll(out);
        if (this instanceof IrMove) {
            in.remove(((IrMove) this).target);
            if (((IrMove) this).source.isVirtualReg()) { // 排除A0~A3形参
                in.add(((IrMove) this).source);
            }
        } else {
            in.remove(this); // 如果定义的变量在集合内 则删除

            for (Value usedValue : usedValues) {
                if (usedValue.isVirtualReg()) {
                    in.add(usedValue); // 若usedValue为虚拟变量 则
                }
            }
        }
    }

    public String printActiveVariable() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n该指令in如下:\n");
        for (Value in : in) {
            sb.append(in.value).append(" ");
        }
        sb.append("\n该指令out如下:\n");
        for (Value out : out) {
            sb.append(out.value).append(" ");
        }
        return sb + "\n";
    }

    public ArrayList<String> toHashCode() {
        return new ArrayList<>();
    }
}
