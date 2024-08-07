package MidEnd.IrCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import static MidEnd.IrCore.IrFunction.RESTART_FLAG;
import static MidEnd.SymbolTable.IrSymbolTable.irSymbolTable;

// 单例模式
public class IrModule {
    /*
        保存CompUnit的LLVM，包含
     */
    static public IrModule irModule = new IrModule();
    static public IrFunction curIrFunction;
    static public IrBasicBlock curIrBasicBlock;
    static public int MODE = 1; // 1是开启LLVM优化 0是关闭LLVM优化
    static public int COMMON_SUBEXPRESSION_AND_UNUSED_CODE_DELETE_FLAG = 0;
    private ArrayList<IrGlobalDef> globals;
    private ArrayList<IrFunction> irFunctions;
    private Value getIntValue;
    private Value putChValue;
    private Value putIntValue;

    public IrModule() {
        this.globals = new ArrayList<>();
        this.irFunctions = new ArrayList<>();
        getIntValue = new Value("@getint", 5);
        putChValue = new Value("@putch", 5);
        putIntValue = new Value("@putint", 5);
    }

    public Value getGetIntValue() {
        return getIntValue;
    }

    public Value getPutChValue() {
        return putChValue;
    }

    public Value getPutIntValue() {
        return putIntValue;
    }

    public void addGlobals(IrGlobalDef irGlobalDef) {
        globals.add(irGlobalDef);
//        // System.out.println(instruction);
    }
    public void addFunction(String value, int reType) {
        curIrFunction = new IrFunction(value, reType);
        irFunctions.add(curIrFunction);
    }

    public ArrayList<IrGlobalDef> getGlobals() {
        return globals;
    }

    public void setGlobals(ArrayList<IrGlobalDef> globals) {
        this.globals = globals;
    }

    public ArrayList<IrFunction> getIrFunctions() {
        return irFunctions;
    }

    public void setIrFunctions(ArrayList<IrFunction> irFunctions) {
        this.irFunctions = irFunctions;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("declare i32 @getint()\ndeclare void @putint(i32)\ndeclare void @putch(i32)\ndeclare void @putstr(i8*)\n");
        for (IrGlobalDef irGlobalDef : globals) {
            sb.append(irGlobalDef.toString());
        }
        for (IrFunction irFunction : irFunctions) {
            sb.append(irFunction.toString());
        }
        return sb.toString();
    }

    // 生成基本块结点树
    public void genCFG() {
        for (IrFunction irFunction : irFunctions) {
            irFunction.genCFG();
        }
    }

    // 生成支配树 采用节点删除法，依次将
    public void genDOMTree() {
        for (IrFunction irFunction : irFunctions) {
            irFunction.genDOMTree();
            irFunction.printDOMTree();
            irFunction.printIdom();
        }
    }

    // 计算支配边界
    public void genDomFrontier() {
        for (IrFunction irFunction : irFunctions) {
            irFunction.genDomFrontier();
            irFunction.printDomFrontier();
        }
    }

    // 插入phi指令
    public void genPhiIns() {
        irSymbolTable.printIrSymbolTable();
        for (IrFunction irFunction : irFunctions) {
            irFunction.genPhiIns();
        }
    }
    public void optimizeLLVM() {
        // 内存2寄存器
        genMemToReg();
        // 死函数删除
        unusedFunctionDelete();

        do {
            COMMON_SUBEXPRESSION_AND_UNUSED_CODE_DELETE_FLAG = 0;
            // 死代码删除
            unusedCodeDelete();
            // 局部公共子表达式删除
            commonSubexpressionDeletion();
        } while (COMMON_SUBEXPRESSION_AND_UNUSED_CODE_DELETE_FLAG == 1);

        // load store删除
        unusedLoadAndStoreDelete();

        do {
            COMMON_SUBEXPRESSION_AND_UNUSED_CODE_DELETE_FLAG = 0;
            // 死代码删除
            unusedCodeDelete();
            // 局部公共子表达式删除
            commonSubexpressionDeletion();
        } while (COMMON_SUBEXPRESSION_AND_UNUSED_CODE_DELETE_FLAG == 1);
        // 全局变量删除
        unusedGlobalDelete();

        // 简单代码前提
        ptrCommandForward();

        // 消PHI
        declarePhi();

        // 图着色寄存器分配
        figureColoringRegAllocation();

        // 基本块再次合并
//        basicBlockCombine2();
    }

    private void ptrCommandForward() {
        for (IrFunction irFunction : irFunctions) {
            irFunction.ptrCommandForward();
        }
    }

    private void unusedGlobalDelete() {
        HashMap<IrGlobalDef, Boolean> globalDefBooleanHashMap = new HashMap<>();
        for (IrGlobalDef irGlobalDef : this.globals) {
            globalDefBooleanHashMap.put(irGlobalDef, false);
        }

        // 查找用过的全局变量
        for (IrFunction irFunction : irFunctions) {
            for (IrBasicBlock irBasicBlock : irFunction.getIrBasicBlocks()) {
                for (IrInstruction instruction : irBasicBlock.getInstructions()) {
                    for (Value usedValue : instruction.usedValues) {
                        if (usedValue instanceof IrGlobalDef irGlobalDef) {
                            globalDefBooleanHashMap.put(irGlobalDef, true);
                        }
                    }
                }
            }
        }

        // 删除
        globals.removeIf(irGlobalDef -> !globalDefBooleanHashMap.get(irGlobalDef));
    }


    private void unusedLoadAndStoreDelete() {
        for (IrFunction irFunction : irFunctions) {
            irFunction.unusedLoadAndStoreDelete();
        }
    }

    private void commonSubexpressionDeletion() {
        for (IrFunction irFunction : irFunctions) {
            irFunction.commonSubexpressionDeletion();
        }
    }

    // TODO:
    private void figureColoringRegAllocation() {
        int flag = 0;
        do {
            flag++;
            RESTART_FLAG = 0;
            calConflictGraph();
            distributeRealReg();
        } while (RESTART_FLAG == 1);
    }

    private void distributeRealReg() {
        for (IrFunction irFunction : irFunctions) {
            irFunction.distributeRealReg();
        }
    }

    private void calConflictGraph() {
        for (IrFunction irFunction : irFunctions) {
            irFunction.calConflictGraph();
        }
    }


    private void declarePhi() {
        for (IrFunction irFunction : irFunctions) {
            irFunction.declarePhi();
        }
    }

    private void unusedCodeDelete() {
        for (IrFunction irFunction : irFunctions) {
            irFunction.unusedCodeDelete();
        }
    }

    private void unusedFunctionDelete() {
        HashMap<IrFunction, Boolean> irFunctionBooleanHashMap = new HashMap<>();
        IrFunction mainFunction = irFunctions.get(irFunctions.size() - 1);
        for (IrFunction irFunction : irFunctions) {
            if (irFunction.value.equals("@main")) {
                irFunctionBooleanHashMap.put(irFunction, true);
            } else {
                irFunctionBooleanHashMap.put(irFunction, false);
            }
        }
        ArrayBlockingQueue<IrFunction> irFunctionQueue = new ArrayBlockingQueue<>(1000);
        irFunctionQueue.add(mainFunction);
        // BFS
        while (!irFunctionQueue.isEmpty()) {
            IrFunction irFunction = irFunctionQueue.poll();
            irFunctionBooleanHashMap.put(irFunction, true);
            for (IrFunction callFunction : irFunction.getCallFunctions()) {
                if (!irFunctionBooleanHashMap.get(callFunction)) {
                    irFunctionQueue.add(callFunction);
                }
            }
        }

        irFunctions.removeIf(irFunction -> !irFunctionBooleanHashMap.get(irFunction));
    }

    private void genMemToReg() {
        genCFG();
        // 基本块合并，在生成前驱后继后立即进行
        basicBlockCombine();
        genDOMTree();
        genDomFrontier();
        genPhiIns();
        renameIns();
    }
    private void basicBlockCombine2() {
        for (IrFunction irFunction : irFunctions) {
            irFunction.basicBlockCombine2();
        }
    }
    private void basicBlockCombine() {
        for (IrFunction irFunction : irFunctions) {
            irFunction.basicBlockCombine();
        }
    }

    // DFS
    private void renameIns() {
        for (IrFunction irFunction : irFunctions) {
            irFunction.renameIns();
        }
    }

}
