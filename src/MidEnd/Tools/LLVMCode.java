package MidEnd.Tools;

import FrontEnd.NonTerminal.Sign;
import MidEnd.IrInstuctions.IrBr;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

public class LLVMCode {
    static public void backFillBrLLVMCode(ArrayList<IrBr> irBrs, Value label, String fakeLabel) {
        for (IrBr irBr : irBrs) {
            irBr.backFill(label, fakeLabel);
        }
    }

    static public String genGlobalDim0LLVMCode(String name, int initValue) {
        return "@" + name + " = dso_local global i32 " + initValue;
    }

    static public String genGlobalLineCode(int start, int end, ArrayList<Integer> initValues) {
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

    static public String genGlobalDim1LLVMCode(String name, ArrayList<Integer> initValues, int dim) {
        return "@" + name + " = dso_local global [" + dim + " x i32] " +
                genGlobalLineCode(0, initValues.size(), initValues);
    }

    static public String genGlobalVarZeroLLVMCode(String name, int dim) {
        return "@" + name + " = dso_local global [" + dim + " x i32] zeroinitializer";
    }

    static public String genGlobalVarZeroLLVMCode(String name, int dim1, int dim2) {
        return "@" + name + " = dso_local global [" + dim1 + " x [" + dim2 + " x i32]] zeroinitializer";
    }

    static public String genGlobalDim2LLVMCode(String name, ArrayList<Integer> initials, int dim1, int dim2) {
        StringBuilder sb = new StringBuilder();
        sb.append("@").append(name).append(" = dso_local global [").append(dim1).append(" x [").append(dim2).append(" x i32]] ");
        // 都为零特殊处理
        sb.append("[");
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

    static public String genReturnLLVMCode(String value) {
        return "ret i32 " + value;
    }

    static public String genReturnLLVMCode() {
        return "ret void";
    }

    static public String genAddLLVMCode(String re, String op1, String op2) {
        // <result> = add <ty> <op1>, <op2>
        return re + " = add i32 " + op1 + ", " + op2;
    }

    static public String genSubLLVMCode(String re, String op1, String op2) {
        // <result> = sub <ty> <op1>, <op2>
        return re + " = sub i32 " + op1 + ", " + op2;
    }

    static public String genMulLLVMCode(String re, String op1, String op2) {
        // <result> = mul <ty> <op1>, <op2>
        return re + " = mul i32 " + op1 + ", " + op2;
    }

    static public String genDivLLVMCode(String re, String op1, String op2) {
        // <result> = sdiv <ty> <op1>, <op2> 有符号除法
        return re + " = sdiv i32 " + op1 + ", " + op2;
    }

    static public String genAllocLLVMCode(String addr) {
        return addr + " = alloca i32";
    }
    static public String genAllocPtrLLVMCode(String addr) {
        return addr + " = alloca i32*";
    }
    static public String genAllocPtrLLVMCode(String addr, int dim) {
        return addr + " = alloca [" + dim + " x i32]*";
    }
    static public String genAllocLLVMCode(String addr, int dim) {
        return addr + " = alloca [" + dim + " x i32]";
    }

    static public String genAllocLLVMCode(String addr, int dim1, int dim2) {
        return addr + " = alloca [" + dim1 + " x [" + dim2 + " x i32]]";
    }

    // 目前只在非中 需要处理i1到i32的转换 其他转换目前不需要
    static public String genZextLLVMCode(String zextedValue, String value) {
        return zextedValue + " = zext i1 " + value + " to i32";
    }
    // 一维数组首地址 寻一维数组内pos位置地址
    static public String genGetElementPtrLLVMCode(String targetAddr, String baseAddr, int dim, String pos) {
        return targetAddr + " = getelementptr [" + dim + " x i32], [" + dim + " x i32]* " + baseAddr + ", i32 0, i32 " + pos;
    }

    // 传二维数组 即一维数组的指针
    static public String genGetElementPtrLLVMCode(String targetAddr, String baseAddr, int dim1, int dim2) {
        return targetAddr + " = getelementptr [" + dim1 + " x [" + dim2 + " x i32]], [" + dim1 +
                " x [" + dim2 + " x i32]]* " + baseAddr + ", i32 0, i32 0";
    }

    static public String genGetElementPtrLLVMCode(String targetAddr, String baseAddr, int dim2) {
        return targetAddr + " = getelementptr [" + dim2 + " x i32], ["  + dim2 + " x i32]* " + baseAddr + ", i32 0";
    }
    // 二维数组首地址 寻二维数组内(pos1, pos2)处地址
    static public String genGetElementPtrLLVMCode(String targetAddr, String baseAddr, int dim1, int dim2, String pos1, String pos2) {
        return targetAddr + " = getelementptr [" + dim1 + " x [" + dim2 + " x i32]], [" + dim1 +
                " x [" + dim2 + " x i32]]* " + baseAddr + ", i32 0, i32 " + pos1 + ", i32 " + pos2;
    }

    // 一维指针类型 寻址 传进来a[]，寻a[1]
    static public String genGetElementPtrLLVMCode(String targetAddr, String baseAddr, String pos) {
        return targetAddr + " = getelementptr i32, i32* " + baseAddr + ", i32 " + pos;
    }

    // 二维指针类型寻址 传进来a[][1] 寻a[1][1]
    static public String genGetElementPtrLLVMCode(String targetAddr, String baseAddr, int dim2, String pos1, String pos2) {
        return targetAddr + " = getelementptr [" + dim2 + " x i32], [" + dim2 + " x i32]* " + baseAddr + ", i32 " + pos1 + ", i32 " + pos2;
    }
    static public String genStoreLLVMCode(String value, String addr) {
        return "store i32 " + value + ", i32* " + addr;
    }

    static public String genStorePtrLLVMCode(String value, String addr) {
        return "store i32* " + value + ", i32* * " + addr;
    }

    static public String genStorePtrLLVMCode(String value, String addr, int dimSize) {
        return "store [" + dimSize + " x i32]* " + value + ", [" + dimSize + " x i32]* * " + addr;
    }
    public static String genLoadLLVMCode(String value, String addr) {
        return value + " = load i32, i32* " + addr;
    }

    // 用于
    public static String genLoadPtrLLVMCode(String value, String addr) {
        return value + " = load i32*, i32* * " + addr;
    }
    public static String genLoadPtrLLVMCode(String value, String addr, int dimSize) {
        return value + " = load [" + dimSize + " x i32]*, [" + dimSize + " x i32]* * " + addr;
    }
    public static String genCallLLVMCode(String value, String funcName, ArrayList<String> params, ArrayList<Integer> paramsTypes, ArrayList<Integer> dimSizes) {
        StringBuilder sb = new StringBuilder();
        sb.append(value).append(" = call i32 ").append(funcName).append("(");
        return genCallParamsLLVMCode(params, sb, paramsTypes, dimSizes) + ")";
    }
    public static String genCallLLVMCode(String value, String funcName, ArrayList<String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append(value).append(" = call i32 ").append(funcName).append("(");
        return genCallParamsLLVMCode(params, sb) + ")";
    }
    private static String genCallParamsLLVMCode(ArrayList<String> params, StringBuilder sb, ArrayList<Integer> paramsTypes, ArrayList<Integer> dimSizes) {
        for (int i = 0; i < params.size(); i++) {
            if (paramsTypes.get(i) == 0) {
                sb.append("i32 ");
            } else if (paramsTypes.get(i) == 4) {
                sb.append("i32* ");
            } else {
                sb.append("[").append(dimSizes.get(i)).append(" x i32]* ");
            }
            sb.append(params.get(i));
            if (i != params.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
    private static String genCallParamsLLVMCode(ArrayList<String> params, StringBuilder sb) {
        for (int i = 0; i < params.size(); i++) {
            sb.append("i32 ");
            sb.append(params.get(i));
            if (i != params.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
    public static String genCallLLVMCode(String funcName, ArrayList<String> params, ArrayList<Integer> paramsTypes, ArrayList<Integer> dimSizes) {
        StringBuilder sb = new StringBuilder();
        sb.append("call void ").append(funcName).append("(");
        return genCallParamsLLVMCode(params, sb, paramsTypes, dimSizes) + ")";
    }

    public static String genCallLLVMCode(String funcName, ArrayList<String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("call void ").append(funcName).append("(");
        return genCallParamsLLVMCode(params, sb) + ")";
    }
    public static String genCmpLLVMCode(String value, Sign sign, String cmp1, String cmp2) {
        String cond = null;
        switch (sign) {
            case EQL -> cond = "eq";
            case NEQ -> cond = "ne";
            case GEQ -> cond = "sge";
            case LEQ -> cond = "sle";
            case GRE -> cond = "sgt";
            case LSS -> cond = "slt";
        }
        return value + " = icmp " + cond + " i32 " + cmp1 + ", " + cmp2;
    }

    public static String genBrLLVMCode(String value, String fakeLabel1, String fakeLabel2) {
        // 需要回填 在外面回填即可
        return "br i1 " + value + ", label " + fakeLabel1 + ", label " + fakeLabel2;
    }

    public static String genBrLLVMCode(String label) {
        return "br label " + label;
    }
}
