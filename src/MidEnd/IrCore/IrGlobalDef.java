package MidEnd.IrCore;

import java.util.ArrayList;

public class IrGlobalDef extends User {
    private ArrayList<Integer> initials;

    public IrGlobalDef(String value, ArrayList<Integer> init) {
        super(value, 1);
        initials = init;
    }

    // 一维数组首地址
    public IrGlobalDef(String value, int lineSize, ArrayList<Integer> initials) {
        super(value, 2, lineSize, 0);
        // // System.out.println(value + " !!!!!!!!!!!!!!!!!!!1 " + valueType);
        this.initials = initials;
    }

    // 二维数组首地址
    public IrGlobalDef(String value, int lineSize, int rowSize, ArrayList<Integer> initials) {
        super(value, 3, lineSize, rowSize);
        this.initials = initials;
    }

    public ArrayList<Integer> getInitials() {
        return initials;
    }

    public void setInitials(ArrayList<Integer> initials) {
        this.initials = initials;
    }

    public String toString() {
        switch (valueType) {
            case 1: {
                if (initials != null) {
                    return genGlobalDim0LLVMCode(value, initials.get(0)) + "\n";
                } else {
                    return genGlobalDim0LLVMCode(value, 0) + "\n";
                }
            }
            case 2: {
                if (initials != null) {
                    return genGlobalDim1LLVMCode(value, initials, firstSize) + "\n";
                } else {
                    return genGlobalVarZeroLLVMCode(value, firstSize) + "\n";
                }
            }
            case 3: {
                if (initials != null) {
                    return genGlobalDim2LLVMCode(value, initials, firstSize, secondSize) + "\n";
                } else {
                    return genGlobalVarZeroLLVMCode(value, firstSize, secondSize) + "\n";
                }
            }
            default: return null;
        }
    }

    public String genGlobalDim0LLVMCode(String name, int initValue) {
        return name + " = dso_local global i32 " + initValue;
    }

    public String genGlobalLineCode(int start, int end, ArrayList<Integer> initValues) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int flag = 0;
        // 左闭右开
        for (int i = start; i < end; i++) {
            Integer initValue = initValues.get(i);
            if (initValue != 0) {
                flag = 1;
            }
            sb.append("i32 ").append(initValue);
            if (i != end - 1) {
                sb.append(", ");
            } else {
                sb.append("]");
            }
        }
        if (flag == 0) {
            return "zeroinitializer";
        } else {
            return sb.toString();
        }
    }

    public String genGlobalDim1LLVMCode(String name, ArrayList<Integer> initValues, int dim) {
        return name + " = dso_local global [" + dim + " x i32] " +
                genGlobalLineCode(0, initValues.size(), initValues);
    }

    public String genGlobalVarZeroLLVMCode(String name, int dim) {
        return "@" + name + " = dso_local global [" + dim + " x i32] zeroinitializer";
    }

    public String genGlobalVarZeroLLVMCode(String name, int dim1, int dim2) {
        return "@" + name + " = dso_local global [" + dim1 + " x [" + dim2 + " x i32]] zeroinitializer";
    }

    public String genGlobalDim2LLVMCode(String name, ArrayList<Integer> initials, int dim1, int dim2) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" = dso_local global [").append(dim1).append(" x [").append(dim2).append(" x i32]] ");
        // 都为零特殊处理
        sb.append("[");
        // System.out.println(initials.size());
        for (int i = 0; i < initials.size(); i = i + dim2) {
            sb.append("[").append(dim2).append(" x i32] ");
            sb.append(genGlobalLineCode(i, i + dim2, initials));
            if (i + dim2 == initials.size()) {
                sb.append("]");
            } else {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
