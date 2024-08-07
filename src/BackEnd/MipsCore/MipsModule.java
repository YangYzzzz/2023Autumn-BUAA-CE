package BackEnd.MipsCore;

import BackEnd.MipsInstruction.Asciiz;
import MidEnd.IrCore.IrFunction;
import MidEnd.IrCore.IrModule;

import java.util.ArrayList;

import static MidEnd.IrCore.IrModule.MODE;

public class MipsModule {
    static public MipsModule mipsModule = new MipsModule(); // 单例模式 启动！
    // 用于Mips代码生成的基类
    private ArrayList<Asciiz> globalString; // 全局字符串 需要让全局看到 因为
    private MipsGlobal mipsGlobals;
    private ArrayList<MipsFunction> mipsFunctions;

    public MipsModule() {
    }

    public void setMipsModule(IrModule irModule) {
        globalString = new ArrayList<>();
        mipsFunctions = new ArrayList<>();
        mipsGlobals = new MipsGlobal(irModule.getGlobals());
        for (IrFunction irFunction : irModule.getIrFunctions()) {
            MipsFunction mipsFunction = new MipsFunction(irFunction);
            mipsFunctions.add(mipsFunction);
        }
    }
    public void addGlobalString(Asciiz asciiz) {
        globalString.add(asciiz);
    }
    // 开始生成Mips代码
    public void genMips() {
        mipsGlobals.genMips();
        for (MipsFunction mipsFunction : mipsFunctions) {
            mipsFunction.genMips();
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("# data代码段\n.data\n");
        for (Asciiz asciiz : globalString) {
            sb.append(asciiz.toString());
        }
        sb.append("# text代码段\n.text\n\n");
        sb.append("# 写入函数运行栈基地址\nli $fp, 0x10040000\nli $sp, 0x7ffffffc\nli $gp, 0x10000000\n# 全局变量段\n");
        sb.append(mipsGlobals.toString()).append("\n\n");
        sb.append("j main\nnop\n\n");

        for (MipsFunction mipsFunction : mipsFunctions) {
            sb.append(mipsFunction.toString()).append("\n\n");
        }
        // 主函数结束 终止程序
        if (MODE == 0) {
            sb.append("li $v0, 10\nsyscall\n");
        }
        return sb.toString();
    }
}
