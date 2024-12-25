package MidEnd.IrCore;

import BackEnd.MipsCore.MipsRegister;
import FrontEnd.SymbolTable.Symbol;
import MidEnd.IrInstuctions.IrBr;
import MidEnd.IrInstuctions.IrCall;
import MidEnd.IrInstuctions.IrGetElementPtr;
import MidEnd.IrInstuctions.IrMove;
import MidEnd.IrInstuctions.IrPhi;
import MidEnd.IrInstuctions.IrRet;
import MidEnd.IrInstuctions.IrStore;
import MidEnd.IrInstuctions.IrZext;
import MidEnd.SymbolTable.IrSymbol;
import MidEnd.Tools.MoveConnect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;

import static MidEnd.IrCore.IrModule.curIrBasicBlock;
import static MidEnd.IrCore.IrModule.curIrFunction;
import static MidEnd.Tools.RealRegister.getRealReg;
import static MidEnd.Tools.RealRegister.regNum;
import static MidEnd.SymbolTable.IrSymbolTable.irSymbolTable;

public class IrFunction extends User {
    /*
        存储一个函数的LLVM信息
        保存两个部分的内容：函数定义信息 名字和参数 和 基本块信息
        同时每个函数对应一个新的User User维护Value列表
     */
    private int basicBlockCnt;
    private int virtualRegCnt;
    private int reType;
    private int symbolTableId;
    private ArrayList<IrArgument> irArguments; // 目前只考虑形参的标识符即可 类型以0维int处理？
    private ArrayList<IrBasicBlock> irBasicBlocks;
    private Set<IrFunction> callFunctions; // 记录该调用过哪些函数
    static int ACTIVE_VARIABLE_FLAG = 0;
    static int RESTART_FLAG = 0;

    private Set<Value> valuesInFunction; // 记录当前函数的全部Value
    private ArrayList<MoveConnect> sameValuesInMove; // 记录哈希对，每一个哈希对存在着合并结点的可能性
    private int irFpOffset;

    public IrFunction(String value, int reType) {
        // User应该是存形参 Value存函数名
        super(value, 5, new ArrayList<>());
        this.irBasicBlocks = new ArrayList<>();
        this.basicBlockCnt = -1;
        this.virtualRegCnt = -1;
        this.irArguments = new ArrayList<>();
        curIrBasicBlock = new IrBasicBlock(allocLabel());
        irBasicBlocks.add(curIrBasicBlock);
        this.reType = reType;
        this.callFunctions = new HashSet<>();
        this.valuesInFunction = new HashSet<>();
        this.sameValuesInMove = new ArrayList<>();
        this.irFpOffset = 0;
    }

    public int getIrFpOffset() {
        return irFpOffset;
    }

    public void setSymbolTableId(int symbolTableId) {
        this.symbolTableId = symbolTableId;
    }

    public void addCallFunctions(IrFunction irFunction) {
        callFunctions.add(irFunction);
    }

    public Set<IrFunction> getCallFunctions() {
        return callFunctions;
    }

    public int getSymbolTableId() {
        return symbolTableId;
    }

    public int getVirtualRegCnt() {
        return virtualRegCnt;
    }

    public void setVirtualRegCnt(int virtualRegCnt) {
        this.virtualRegCnt = virtualRegCnt;
    }

    public int getReType() {
        return reType;
    }

    public void setReType(int reType) {
        this.reType = reType;
    }

    public ArrayList<IrArgument> getIrArguments() {
        return irArguments;
    }

    public void setIrArguments(ArrayList<IrArgument> irArguments) {
        this.irArguments = irArguments;
    }

    public ArrayList<IrBasicBlock> getIrBasicBlocks() {
        return irBasicBlocks;
    }

    public void setIrBasicBlocks(ArrayList<IrBasicBlock> irBasicBlocks) {
        this.irBasicBlocks = irBasicBlocks;
    }

    public String allocVirtualReg() {
        virtualRegCnt++;
        return "%VirtualReg" + virtualRegCnt;
    }

    public String allocLabel() {
        basicBlockCnt++;
        return "%" + value.substring(1) + "Label" + basicBlockCnt;
    }

    public IrArgument addParam(String value, int type) {
        IrArgument irArgument = new IrArgument(value, type);
        // 使用于0，1维
        irArguments.add(irArgument);
        return irArgument;
    }

    public IrArgument addParam(String value, int type, int rowSize) {
        IrArgument irArgument = new IrArgument(value, type, rowSize);
        // 适用于二维
        irArguments.add(irArgument);
        return irArgument;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // 输出第一行函数定义
        sb.append("define dso_local ");
        if (reType == 0) {
            sb.append("i32 ");
        } else {
            sb.append("void ");
        }
        sb.append(value).append("(");
        for (int i = 0; i < irArguments.size(); i++) {
            IrArgument irArgument = irArguments.get(i);
            if (i != 0) {
                sb.append(", ");
            }
            switch (irArgument.valueType) {
                case 0: {
                    sb.append("i32 ");
                    break;
                }
                case 1: {
                    sb.append("i32* ");
                    break;
                }
                case 2: {
                    sb.append("[").append(irArgument.firstSize).append(" x i32]* ");
                    break;
                }
            }
            sb.append(irArgument.value);
        }
        sb.append(") {\n");
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            sb.append(irBasicBlock.toString());
        }
        sb.append("}\n");
        return sb.toString();
    }

    // 可能需要一个起始基本块，即使内部无内容
    public void allocBasicBlock() {
        String label = allocLabel();
        IrBasicBlock irBasicBlock = new IrBasicBlock(label);
            /*
                申请一个新bb，两个条件满足其一即可
                1. 当前bb已经有指令了
                2. 当前bb是第一块 根据短路求值 即 当前是第一块且没指令
                3. 好像不太对 再想想
             */
        if (curIrBasicBlock.isEmpty() || !(curIrBasicBlock.getInstructions().get(curIrBasicBlock.getInstructions().size() - 1) instanceof IrBr)) {
            // System.out.println("增加跳转: " + irBasicBlock.value);
            curIrBasicBlock.addInstruction(new IrBr(irBasicBlock));
        }
        irBasicBlocks.add(irBasicBlock);
        curIrBasicBlock = irBasicBlock;
    }

    public void allocMoveBasicBlock() {
        allocBasicBlock();
        curIrBasicBlock.moveFlag = true;
    }

    public ArrayList<Value> genMipsSymbolTable() {
        ArrayList<Value> mipsSymbolTable = new ArrayList<>(irArguments);
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            mipsSymbolTable.addAll(irBasicBlock.genMipsSymbolTable());
        }
        return mipsSymbolTable;
    }

    // 增加后继节点 对于该基本块 后继是i，对于i基本块，后继是this
    public void addBackNode(IrBasicBlock i) {
        curIrBasicBlock.addBackNode(i);
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            if (irBasicBlock.equal(i)) {
                i.addFrontNode(curIrBasicBlock);
            }
        }
    }

    public void printCFG() {
        // System.out.println("当前函数为: " + super.value);
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            // System.out.println(irBasicBlock.value);
            // System.out.println("前驱节点有：");
            for (IrBasicBlock front : irBasicBlock.getFrontNodes()) {
                // System.out.println(front.value);
            }
            // System.out.println("后继节点有：");
            for (IrBasicBlock back : irBasicBlock.getBackNodes()) {
                // System.out.println(back.value);
            }
            // System.out.println("\n");
        }
    }

    public void genCFG() {
        // 第一个基本块无前驱节点
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            Value value = null;
            for (IrInstruction irInstruction : irBasicBlock.getInstructions()) {
                if (irInstruction instanceof IrBr) {
                    value = irInstruction;
                    break;
                } else if (irInstruction instanceof IrRet) {
                    break;
                }
            }
            if (value != null) {
                // 应当一定满足
                IrBr brValue = ((IrBr) value);
                if (brValue.getType() == 0) {
                    // 无条件跳转基本块
                    IrBasicBlock backNode = (IrBasicBlock) brValue.usedValues.get(0);
                    irBasicBlock.addBackNode(backNode);
                    backNode.addFrontNode(irBasicBlock);
                } else {
                    IrBasicBlock backNode1 = (IrBasicBlock) brValue.usedValues.get(1);
                    IrBasicBlock backNode2 = (IrBasicBlock) brValue.usedValues.get(2);
                    irBasicBlock.addBackNode(backNode1);
                    irBasicBlock.addBackNode(backNode2);
                    backNode1.addFrontNode(irBasicBlock);
                    backNode2.addFrontNode(irBasicBlock);
                }
            }
        }
        printCFG();
    }

    public void genDOMTree() {
        // 初始化支配树 初始节点
        IrBasicBlock startBasicBlock = irBasicBlocks.get(0);
        HashMap<IrBasicBlock, Boolean> sNodeHashMap = new HashMap<>();
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            if (startBasicBlock.equal(irBasicBlock)) {
                sNodeHashMap.put(irBasicBlock, true);
            } else {
                sNodeHashMap.put(irBasicBlock, false);
            }
        }
        startBasicBlock.setDomedNodes(sNodeHashMap);
        // 其余节点
        HashMap<IrBasicBlock, Boolean> templateNodeHashMap = new HashMap<>(sNodeHashMap);
        for (Map.Entry<IrBasicBlock, Boolean> entry : templateNodeHashMap.entrySet()) {
            entry.setValue(true);
        }
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            if (!irBasicBlock.equal(startBasicBlock)) {
                irBasicBlock.setDomedNodes(new HashMap<>(templateNodeHashMap));
            }
        }
        // 开始迭代计算
        int flag; // 标注一轮中是否发生改变
        do {
            flag = 0;
            for (IrBasicBlock irBasicBlock : irBasicBlocks) {
                // 非首节点
                if (!irBasicBlock.equal(startBasicBlock)) {
                    ArrayList<IrBasicBlock> frontNodes = irBasicBlock.getFrontNodes();
                    HashMap<IrBasicBlock, Boolean> newDomedNodeHashMap = new HashMap<>();
                    for (IrBasicBlock node : irBasicBlocks) {
                        // 遍历到的节点是自己，一定被支配
                        if (node.equal(irBasicBlock)) {
                            newDomedNodeHashMap.put(node, true);
                        } else {
                            int flag1 = 0;
                            for (IrBasicBlock frontNode : frontNodes) {

                                if (!frontNode.getDomedNodes().get(node)) {
                                    flag1 = 1;
                                }
                            }
                            if (flag1 == 0) {
                                // 都是true则置true
                                newDomedNodeHashMap.put(node, true);
                            } else {
                                newDomedNodeHashMap.put(node, false);
                            }
                        }
                    }
                    // 比较前后两邻接图，若不同则继续
                    for (Map.Entry<IrBasicBlock, Boolean> entry1 : newDomedNodeHashMap.entrySet()) {
                        if (entry1.getValue() != irBasicBlock.getDomedNodes().get(entry1.getKey())) {
                            flag = 1;
                        }
                    }
                    irBasicBlock.setDomedNodes(newDomedNodeHashMap);
                }
            }
        } while (flag != 0);

        // 计算每个节点直接支配者
        // 严格支配n，且不严格支配任何严格支配 n 的节点的节点(直观理解就是所有严格支配n的节点中离n最近的那一个)，我们称其为n的直接支配者
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            if (irBasicBlock.equal(startBasicBlock)) {
                // 首节点无直接支配者
                irBasicBlock.setIdom(null);
            } else {
                // 非首节点，遍历全部支配者结点
                IrBasicBlock idom = null;
                int iflag;
                for (Map.Entry<IrBasicBlock, Boolean> entryTar : irBasicBlock.getDomedNodes().entrySet()) {
                    iflag = 0;
                    IrBasicBlock tarBlock = entryTar.getKey();
                    // 严格支配的该节点的节点
                    // 其他的全部节点都不被这个节点支配，即其他的全部节点的被支配哈希表中该节点对应false
                    if (entryTar.getValue() && !entryTar.getKey().equal(irBasicBlock)) {
                        // 再次遍历该节点的全部被支配节点
                        for (Map.Entry<IrBasicBlock, Boolean> entryCmp : irBasicBlock.getDomedNodes().entrySet()) {
                            if (entryCmp.getValue() && !entryCmp.getKey().equal(irBasicBlock) && !entryCmp.getKey().equal(tarBlock) && entryCmp.getKey().getDomedNodes().get(tarBlock)) {
                                // 该节点存在节点被目标节点支配，故目标节点不是Idom
                                iflag = 1;
                                break;
                            }
                        }
                        if (iflag == 0) {
                            idom = tarBlock;
                            break;
                        }
                    }
                }
                irBasicBlock.setIdom(idom);
            }
        }

        // 建立支配树
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            if (irBasicBlock.getIdom() != null) {
                irBasicBlock.getIdom().addDomTreeLeaf(irBasicBlock);
            }
        }
    }

    public void printDOMTree() {
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            irBasicBlock.printDomedTree();
        }
    }

    public void printIdom() {
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            if (irBasicBlock.getIdom() != null) {
                // System.out.println("基本块名为: " + irBasicBlock.value + " 其直接支配者是： " + irBasicBlock.getIdom().value);

            } else {
                // System.out.println("基本块名为: " + irBasicBlock.value + " 无直接支配");
            }
        }
    }

    // 计算支配边界 遍历所有的边 有向图，遍历节点和其所有后继节点即可 (a, b) 代表 a --> b
    public void genDomFrontier() {
        IrBasicBlock x;
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            ArrayList<IrBasicBlock> backNodes = irBasicBlock.getBackNodes();
            x = irBasicBlock;
            for (IrBasicBlock backBlock : backNodes) {
                // 满足三个条件 x不超出界限 后继边不被x严格支配
                while (x != null && !backBlock.strictDomedByX(x)) {
                    // System.out.println("前驱节点为： " + x.value + " 后继节点为: " + backBlock.value);
                    x.addDomFrontier(backBlock);
                    x = x.getIdom();
                }
            }
        }
    }

    public void printDomFrontier() {
        // System.out.println("函数名为: " + this.value);
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            irBasicBlock.printDF();
        }
    }

    public void genPhiIns() {
        curIrFunction = this;
        HashMap<Symbol, IrSymbol> irSymbolHashMap = irSymbolTable.getFunctionIrSymbolTable(this);
        for (Map.Entry<Symbol, IrSymbol> entry : irSymbolHashMap.entrySet()) {
            System.out.println("插入的Symbol是 " + entry.getKey().getName());
            IrSymbol irSymbol = entry.getValue();
            // 开始插入phi算法
            // 初始化待插入phi的基本块 一个基本块面临多个基本块流入的Value
            HashMap<IrBasicBlock, IrPhi> addPhiIrBasicBlock = new HashMap<>();
            // 初始化该变量定义块, 求闭包时需要此哈希表
            HashMap<IrBasicBlock, Value> defBlocks = irSymbol.getBlockDefs();
            // 司马队列
            ArrayBlockingQueue<IrBasicBlock> defBlocksQueue = new ArrayBlockingQueue<>(1000);
            for (Map.Entry<IrBasicBlock, Value> entry1 : defBlocks.entrySet()) {
                System.out.println("起始定义在: " + entry1.getKey().value);
                defBlocksQueue.add(entry1.getKey());
            }
            while (!defBlocksQueue.isEmpty()) {
                // 出队的一个定义基本块
                IrBasicBlock defBasicBlock = defBlocksQueue.poll();
                // 遍历该定义基本块的支配边界
                for (IrBasicBlock domFrontier : defBasicBlock.getDomFrontier()) {
                    // 遍历到支配边界未必代表要插入Phi指令，因为存在未定义的情况
                    if (!addPhiIrBasicBlock.containsKey(domFrontier)) {
                        String virtualReg = allocVirtualReg();
                        IrPhi irPhi = new IrPhi(virtualReg, entry.getKey());
                        addPhiIrBasicBlock.put(domFrontier, irPhi);
                        // 设置插入phi的位置
                        if (!defBlocks.containsKey(domFrontier)) {
                            // 构造闭包
                            defBlocksQueue.add(domFrontier);
                        }
                    }
                }
            }

            // 最后统一插入phi, 需要判断此处有无定义
            IrBasicBlock basicBlock;
            IrPhi irPhi;
            for (Map.Entry<IrBasicBlock, IrPhi> entry1 : addPhiIrBasicBlock.entrySet()) {
                basicBlock = entry1.getKey();
                irPhi = entry1.getValue();
                if (basicBlock.hasDef(irPhi.symbol)) {
                    irSymbolTable.addBasicBlockDef(basicBlock, irPhi.symbol, irPhi);
                    basicBlock.addPhiInstruction(irPhi);
                }
            }
        }
    }

    // DFS
    public void renameIns() {
        curIrFunction = this;
        HashMap<IrBasicBlock, Boolean> visit = new HashMap<>();
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            visit.put(irBasicBlock, false);
        }
        IrBasicBlock startBlock = irBasicBlocks.get(0);
        DFS(visit, startBlock);
    }

    private void DFS(HashMap<IrBasicBlock, Boolean> visit, IrBasicBlock block) {
        visit.put(block, true);
        block.renameIns();
        for (IrBasicBlock leafBlock : block.getDomTreeLeaves()) {
            if (!visit.get(leafBlock)) {
                DFS(visit, leafBlock);
            }
        }
    }

    /**
     * 合并基本块
     * 1. 将不可达的基本块去除
     * 1. 前驱节点为1且前驱节点的后继节点为1的可合并
     */
    public void basicBlockCombine() {
        curIrFunction = this;
        Iterator<IrBasicBlock> iterator = irBasicBlocks.iterator();
        // 读走第一个基本块
        iterator.next();
        while (iterator.hasNext()) {
            IrBasicBlock irBasicBlock = iterator.next();
            // 删除无前驱结点的基本块
            if (irBasicBlock.getFrontNodes().isEmpty()) {
                iterator.remove();
                for (IrBasicBlock backNode : irBasicBlock.getBackNodes()) {
                    backNode.deleteFrontNode(irBasicBlock);
                }
            }
        }

        // 合并基本块
        int flag;
        do {
            flag = 0;
            for (int i = 0; i < irBasicBlocks.size(); i++) {
                IrBasicBlock curBasicBlock = irBasicBlocks.get(i);
                if (curBasicBlock.getFrontNodes().size() == 1 && curBasicBlock.getFrontNodes().get(0).getBackNodes().size() == 1) {
                    // 可合并
                    IrBasicBlock frontBasicBlock = curBasicBlock.getFrontNodes().get(0);
                    frontBasicBlock.blockCombine(curBasicBlock);
                    // 将当前基本块所有的后继节点的前驱节点记录为其前驱节点
                    for (IrBasicBlock backNode : curBasicBlock.getBackNodes()) {
                        backNode.replaceFrontNode(frontBasicBlock, curBasicBlock); // 第一个参数为待替换的，第二个参数为被替换掉的
                    }
                    irSymbolTable.updateIrSymbol(this, curBasicBlock, frontBasicBlock);
                    flag = 1;
                    irBasicBlocks.remove(i); // 将当前基本块删除
                    break;
                }
            }
        } while (flag != 0);
    }

    public void unusedCodeDelete() {
        // 用来判断Value该不该删除
        HashMap<IrInstruction, Boolean> instructionBooleanHashMapBooleanHashMap = new HashMap<>();
        ArrayBlockingQueue<IrInstruction> instructionQueue = new ArrayBlockingQueue<>(10000);
//        HashSet<IrGlobalDef> irGlobalDefs = new HashSet<>();
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            for (IrInstruction irInstruction : irBasicBlock.getInstructions()) {
                if (irInstruction instanceof IrRet || irInstruction instanceof IrBr || irInstruction instanceof IrCall || irInstruction instanceof IrStore) {
                    instructionQueue.add(irInstruction);
                }
                instructionBooleanHashMapBooleanHashMap.put(irInstruction, false);
            }
        }

        // BFS
        while (!instructionQueue.isEmpty()) {
            IrInstruction irInstruction = instructionQueue.poll();
            instructionBooleanHashMapBooleanHashMap.put(irInstruction, true);
            for (Value usedValue : irInstruction.usedValues) {
                // Value需要排除 数字类型，基本块类型，函数参数, 全局变量类型，应当为指令的返回值才被纳入到use链中
                if (usedValue instanceof IrInstruction) {
//                    usedValue.addUser(irInstruction); // 设置mips
                    if (!instructionBooleanHashMapBooleanHashMap.get(usedValue)) {
                        instructionQueue.add((IrInstruction) usedValue);
                    }
                }
            }
        }

        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            irBasicBlock.getInstructions().removeIf(irInstruction -> !instructionBooleanHashMapBooleanHashMap.get(irInstruction));
        }
    }

    public void declarePhi() {
        // 遍历全部基本块,由于动态存在加入，则
        ArrayList<IrBasicBlock> copyOfIrBasicBlock = new ArrayList<>(irBasicBlocks);
        for (IrBasicBlock irBasicBlock : copyOfIrBasicBlock) {
            // 含PHI指令的基本块前驱一定超过两个
            if (!irBasicBlock.getInstructions().isEmpty() && irBasicBlock.getInstructions().get(0) instanceof IrPhi) {
                // 记录前驱节点到新创建的基本块的映射，方便后续加入move指令，若映射到当前节点，代表无需创建基本块，此时Move指令加在前驱节点末尾即可
                HashMap<IrBasicBlock, IrBasicBlock> frontNode2TmpNodeHashMap = new HashMap<>();
                for (IrBasicBlock frontNode : irBasicBlock.getFrontNodes()) {
                    // 若前驱节点的后继节点大于1，则需要创建新的基本块
                    if (frontNode.getBackNodes().size() > 1) {
                        allocMoveBasicBlock();
                        curIrBasicBlock.addInstruction(new IrBr(irBasicBlock)); // move块前驱一个节点，后继一个节点
                        curIrBasicBlock.getFrontNodes().add(frontNode);
                        curIrBasicBlock.getBackNodes().add(irBasicBlock);
                        frontNode.replaceBackNode(curIrBasicBlock, irBasicBlock);
                        irBasicBlock.replaceFrontNode(curIrBasicBlock, frontNode);
                        frontNode2TmpNodeHashMap.put(frontNode, curIrBasicBlock);
                    } else {
                        // 无需创建新的
                        frontNode2TmpNodeHashMap.put(frontNode, irBasicBlock);
                    }
                }

                // 遍历全部Phi指令，将数据流拆成数个Move指令
                Iterator<IrInstruction> iterator = irBasicBlock.getInstructions().iterator();
                while (iterator.hasNext()) {
                    IrInstruction irInstruction = iterator.next();
                    if (irInstruction instanceof IrPhi) {
                        iterator.remove();
                        // 跳转没改
                        for (int i = 0; i < irInstruction.usedValues.size(); i = i + 2) {
                            IrBasicBlock frontNode = (IrBasicBlock) irInstruction.usedValues.get(i + 1);
                            Value value = irInstruction.usedValues.get(i);
                            if (frontNode2TmpNodeHashMap.get(frontNode).equals(irBasicBlock)) {
                                // 在前驱基本块末尾添加move
                                frontNode.addInstruction(new IrMove(irInstruction, value));
                            } else {
                                // 在新基本块中添加Move
                                frontNode2TmpNodeHashMap.get(frontNode).addInstruction(new IrMove(irInstruction, value));
                            }
                        }
                    } else {
                        break;
                    }
                }
            }
        }
    }

    /**
     * 首先计算每个指令的活跃变量
     * 每条指令内记录 in 和 out
     * 自底向上计算，一个Block or Ins 的 in 依赖于后继的 out
     * 存在环，无法拓扑排序，需要迭代求解
     */
    public void calConflictGraph() {
        // 0. 清空全部活跃变量
        valuesInFunction.clear();
        sameValuesInMove.clear();
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            irBasicBlock.delConflictGraph();
        }

        // 1. 计算活跃变量
        do {
            ACTIVE_VARIABLE_FLAG = 0;
            for (IrBasicBlock irBasicBlock : irBasicBlocks) {
                irBasicBlock.calConflictGraph();
            }
        } while (ACTIVE_VARIABLE_FLAG == 1);
        /*
            2. 构建冲突图
                指令Value并不包含该函数所有Value，还存在参数的Value，前四个参数是固定存放在A0~A3中，其他的参数也纳入冲突图中
                即所有VirtualReg构建冲突图
         */
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            for (int i = 0; i < irBasicBlock.getInstructions().size(); i++) {
                IrInstruction irInstruction = irBasicBlock.getInstructions().get(i);
                if (irInstruction instanceof IrMove irMove) {
                    if (irMove.source.isVirtualReg() && irMove.target.isVirtualReg()) {
                        System.out.println(irMove.source.value + " !!!! " + irMove.target.value);
                        sameValuesInMove.add(new MoveConnect(irMove.source, irMove.target));
                    }
                } else if (irInstruction instanceof IrZext irZext) {
                    // 有待商榷
                    if (irZext.usedValues.get(0).isVirtualReg()) {
                        System.out.println(irZext.value + " !!!! " + irZext.usedValues.get(0));
                        sameValuesInMove.add(new MoveConnect(irZext, irZext.usedValues.get(0)));
                    }
                }
                if (i == 0) {
                    // 只有第一条指令的in起作用, 其余皆用out
                    Set<Value> in = irInstruction.in;
                    for (Value value : in) {
                        valuesInFunction.add(value);
                        for (Value adjValue : in) {
                            if (!adjValue.equal(value)) {
                                value.adjValues.add(adjValue);
                            }
                        }
                    }
                }
                Set<Value> out = irInstruction.out;
                for (Value value : out) {
                    valuesInFunction.add(value);
                    for (Value adjValue : out) {
                        if (!adjValue.equal(value)) {
                            value.adjValues.add(adjValue);
                        }
                    }
                }
            }
        }

        printConflictGraph();
    }

    public void printConflictGraph() {
        System.out.println("\n函数为" + value + " 其内部冲突图如下: ");
        for (Value value : valuesInFunction) {
            System.out.println(value.value);
            StringBuilder sb = new StringBuilder();
            sb.append("其邻接结点有：");
            for (Value adj : value.adjValues) {
                sb.append(adj.value).append(" ");
            }
            System.out.println(sb);
        }
    }

    public void clearAllValues() {
        for (Value value : valuesInFunction) {
            value.reg = -1;
            value.isInReg = false;
            value.isInStack = false;
            value.combineValue = null;
        }
    }

    /**
     * 分配实际寄存器 开始图着色算法
     * 按照SSA，若存在 %2 = move %1 指令，则%1
     * 将图一点一点入栈
     */
    public void distributeRealReg() {
        clearAllValues();
        // 为形参分配寄存器
        Stack<Value> valueStack = new Stack<>();
        Set<Value> valuesCopyGraph = new HashSet<>(valuesInFunction);
        while (!valuesCopyGraph.isEmpty()) {
            // 合并，和常量传播有冲突，注释掉合并这一步。一种可能的解决方案是将常量传播的move和mem2reg的move区分开 12.25记录
//            for (MoveConnect moveConnect : sameValuesInMove) {
//                Value combineValue = moveConnect.value1;
//                Value combinedValue = moveConnect.value2;
//                // 自己和自己不允許合并
//                if (!combineValue.isInStack && !combinedValue.isInStack && !combinedValue.equal(combineValue) && canCombine(combineValue, combinedValue)) {
//                    combinedValue.combineValue = combineValue;
//                    System.out.println(combineValue.value + " " + combinedValue.value + "可以合并");
//                    // 抹除combined的所有痕迹
//                    sameValuesInMove.remove(moveConnect);
//                    clearCombinedValue(combineValue, combinedValue);
//                    // 将被合并结点删除，将对应move记录删除
//                    valuesCopyGraph.remove(combinedValue);
//                    break;
//                }
//            }

            // 简化
            for (Value value : valuesCopyGraph) {
                if (value.getAdjDegree() < regNum) {
                    System.out.println(value.value + "入栈，一定能分配到寄存器");
                    valueStack.push(value);
                    value.isInStack = true;
                    value.isInReg = true;
                    valuesCopyGraph.remove(value);
                    break;
                }
            }

            // 溢出
            for (Value value : valuesCopyGraph) {
                if (value.getAdjDegree() >= regNum) {
                    valueStack.push(value);
                    value.isInStack = true;
                    // 暂时是false
                    value.isInReg = false;
                    valuesCopyGraph.remove(value);
                    break;
                }
            }
        }

        // 选择
        Set<Value> remapValue = new HashSet<>();
        Set<Value> notInRegValues = new HashSet<>();
        while (!valueStack.empty()) {
            Value value = valueStack.pop();
            if (value.isInReg) {
                // 一定能分配到寄存器

                // 形参A0~A3情况
//                if (value.reg != -1) {
//                    System.out.println("参数，不参与寄存器分配！！！");
//                    continue;
//                }
                Set<Integer> adjRegs = new HashSet<>();
                System.out.println("此时value为：" + value.value);
                for (Value adjValue : value.adjValues) {
                    if (remapValue.contains(adjValue)) {
                        System.out.println("他的邻接点有：" + adjValue.value + ", 邻接点分配到的寄存器为: " + MipsRegister.getRegister(adjValue.reg));
                        adjRegs.add(adjValue.reg);
                    }
                }
                value.reg = getRealReg(adjRegs);
                System.out.println("最终该value分配到了：" + MipsRegister.getRegister(value.reg));
                remapValue.add(value);
            } else {
                notInRegValues.add(value);
            }
        }

        // 对未能分配到寄存器的变量 仍存在一缕希望
        Iterator<Value> iterator = notInRegValues.iterator();
        while (iterator.hasNext()) {
            Value value = iterator.next();
            Set<Integer> adjRegs = new HashSet<>();
            System.out.println("此时在待无法分配寄存器的队列中的 value为：" + value.value);
            for (Value adjValue : value.adjValues) {
                if (remapValue.contains(adjValue)) {
                    System.out.println("他的邻接点有：" + adjValue.value + ", 邻接点分配到的寄存器为: " + MipsRegister.getRegister(adjValue.reg));
                    adjRegs.add(adjValue.reg);
                }
            }
            value.reg = getRealReg(adjRegs);
            if (value.reg != -1) {
                System.out.println("最终该value分配到了：" + MipsRegister.getRegister(value.reg));
                remapValue.add(value);
                iterator.remove();
            } else {
                System.out.println("无法分配寄存器");
            }
        }

//        if (RESTART_LOOP == 2) {
//            return;
//        }

        /*
         * 当存在未分配寄存器的变量时，重新开始，这意味着需要添加指令，并重新分配寄存器
         * 此乃最为关键的一步
         */
        for (Value value : notInRegValues) {
            RESTART_FLAG = 1;
            value.offset = irFpOffset;
            // TODO: no1 好像没错
            System.out.println("value: " + value.value + ",offset: " + value.offset);
            value.isInMem = true;
            // 扫描全部指令 若涉及到对其定义，则使用store存入目标内存；若涉及到对其使用，则将其从目标内存中load取出
            // 全扫复杂度可能较高，maybe可简化;; 传参时可能出现问题，若所传参数被设定为存入内存的值，则搜索不到定义这一步，此时需要在扫描一遍参数表！！
            // 对于溢出的结点，默认用V1存入内存中，不用再占用其他寄存器，
            for (IrBasicBlock irBasicBlock : irBasicBlocks) {
                ListIterator<IrInstruction> irInstructionListIterator = irBasicBlock.getInstructions().listIterator();
                while (irInstructionListIterator.hasNext()) {
                    IrInstruction irInstruction = irInstructionListIterator.next();
                    // 还需要考虑到Move指令
                    if (irInstruction.equal(value)) {
                        System.out.println("找到定义处value: " + value.value + ", offset: " + value.offset);
                        // 定义 addrValue仅记录下偏移即可, store在这里加了，实际也可以不加，重点在将value的type改为10
                        Value addrValue = new Value("!fake", 9);
                        irInstructionListIterator.add(new IrStore(irInstruction, addrValue, 11));
                    } else if (irInstruction instanceof IrMove irMove && irMove.target.equal(value)) {
                        // phi指令被设置在内存中
                        Value addrValue = new Value("!fake", 9);
                        irInstructionListIterator.add(new IrStore(irMove.target, addrValue, 11));
                    }
//                    else if (irInstruction.usedValues.contains(value)) {
//                        // 使用 旨在消除相应的活跃变量向上传播
//                        Value addrValue = new Value("!" + value.value, value.valueType, value.offset);
//                        addrValue.isInMem = true;
//                        for (int i = 0; i < irInstruction.usedValues.size(); i++) {
//                            if (irInstruction.usedValues.get(i).equal(value)) {
//                                irInstruction.usedValues.set(i, addrValue);
//                            }
//                        }
//                    } else if (irInstruction instanceof IrMove irMove && irMove.source.equal(value)) {
//                        irMove.source = new Value("!" + value.value, irMove.valueType, value.offset);
//                        irMove.source.isInMem = true;
//                    }
                }
            }
            irFpOffset += 4;
        }

        // 若存在无法分配的 返回
        if (RESTART_FLAG == 1) {
            return;
        }
        // 将合并的结点的寄存器找到
        for (Value value : valuesInFunction) {
            if (value.reg == -1) {
                value.reg = getCombineReg(value);
            }
        }

        // 输出寄存器保存结果
        System.out.println("函数名为:" + this.value);
        for (Value value : valuesInFunction) {
            if (notInRegValues.contains(value)) {
                System.out.println(value.value + "无法分配寄存器");
            } else {
                System.out.println("value名为：" + value.value + " 被保存在" + MipsRegister.getRegister(value.reg) + "号寄存器中");
            }
        }
    }

    private int getCombineReg(Value value) {
        if (value.combineValue != null) {
            value.reg = getCombineReg(value.combineValue);
            value.combineValue = null;
        }
        return value.reg;
    }

    private void clearCombinedValue(Value combineValue, Value combinedValue) {
        // 直接在原Value上改即可
        System.out.println("combine: " + combineValue.value + ", combined: " + combinedValue.value);
        for (Value value : valuesInFunction) {
            if (value.adjValues.contains(combinedValue)) {
                // 该结点的邻接结点删除被合并的结点
                value.adjValues.remove(combinedValue);
                // 该结点的邻接结点增加合并的结点
                value.adjValues.add(combineValue);
                // 合并的结点增加该结点
                combineValue.adjValues.add(value);
            }
        }

        for (MoveConnect moveConnect : sameValuesInMove) {
            moveConnect.changeValueConnect(combineValue, combinedValue);
        }
    }

    private boolean canCombine(Value combineValue, Value combinedValue) {
        // 合并后度数小于K即可合并
        Set<Value> values = new HashSet<>();
        for (Value adj : combineValue.adjValues) {
            if (!adj.isInStack) {
                values.add(adj);
            }
        }
        for (Value adj : combinedValue.adjValues) {
            if (!adj.isInStack) {
                values.add(adj);
            }
        }
        return values.size() < regNum;
    }

    public void commonSubexpressionDeletion() {
        // 构建全局的表
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            irBasicBlock.commonSubexpressionDeletion();
        }
    }

    public void unusedLoadAndStoreDelete() {
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            irBasicBlock.unusedStoreDelete();
        }
    }

    private void extractCycle(Set<IrBasicBlock> cycleBasicBlocks, Stack<IrBasicBlock> recStack, IrBasicBlock startNode) {
        // 找到环路包含的节点
        for (int i = recStack.size() - 1; i > 0; i--) {
            cycleBasicBlocks.add(recStack.get(i));
            if (recStack.get(i).equals(startNode)) {
                break;
            }
        }
    }

    private void cycleDFS(IrBasicBlock curBasicBlock, HashMap<IrBasicBlock, Boolean> visited
            , Stack<IrBasicBlock> recStack, Set<IrBasicBlock> cycleBasicBlocks) {
        visited.put(curBasicBlock, true);
        recStack.add(curBasicBlock);
        for (IrBasicBlock backNode : curBasicBlock.getBackNodes()) {
            if (recStack.contains(backNode)) {
                // 找到回路，记录回路的基本块
                extractCycle(cycleBasicBlocks, recStack, backNode);
            } else if (!visited.get(backNode)) {
                // 继续
                cycleDFS(backNode, visited, recStack, cycleBasicBlocks);
            }
        }
        recStack.pop(); // 当前节点遍历完成，推出
    }

    public void ptrCommandForward() {
        // 计算处于循环中的基本块
        HashMap<IrBasicBlock, Boolean> visited = new HashMap<>();
        Stack<IrBasicBlock> recStack = new Stack<>();
        Set<IrBasicBlock> cycleBasicBlocks = new HashSet<>();
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            visited.put(irBasicBlock, false);
        }
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            if (!visited.get(irBasicBlock)) {
                cycleDFS(irBasicBlock, visited, recStack, cycleBasicBlocks);
            }
        }

        // 寻找能够前提的指令
        HashMap<Value, ArrayList<IrInstruction>> movePtrHashMap = new HashMap<>();
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            Iterator<IrInstruction> irInstructionIterator = irBasicBlock.getInstructions().iterator();
            while (irInstructionIterator.hasNext()) {
                IrInstruction irInstruction = irInstructionIterator.next();
                if (irInstruction instanceof IrGetElementPtr irPtr && canPtrMove(irPtr) && cycleBasicBlocks.contains(irBasicBlock)) {
                    // 此时该指令可以前移至基地址定义后
                    Value baseAddr = irPtr.usedValues.get(0); // 找到该Value定义处
                    if (movePtrHashMap.containsKey(baseAddr)) {
                        movePtrHashMap.get(baseAddr).add(irInstruction);
                    } else {
                        movePtrHashMap.put(baseAddr, new ArrayList<>());
                        movePtrHashMap.get(baseAddr).add(irInstruction);
                    }
                    irInstructionIterator.remove();
                }
            }
        }

        // 移动全局变量到第一个基本块最前
        Iterator<Map.Entry<Value, ArrayList<IrInstruction>>> hashIterator = movePtrHashMap.entrySet().iterator();
        while (hashIterator.hasNext()) {
            Map.Entry<Value, ArrayList<IrInstruction>> entry = hashIterator.next();
            if (entry.getKey().isGlobal() || entry.getKey() instanceof IrArgument) {
                for (IrInstruction irInstruction : entry.getValue()) {
                    irBasicBlocks.get(0).getInstructions().add(0, irInstruction);
                }
                hashIterator.remove();
            }
        }

        // 移动其他基本块到指定位置
        for (IrBasicBlock irBasicBlock : irBasicBlocks) {
            ListIterator<IrInstruction> listIterator = irBasicBlock.getInstructions().listIterator();
            while (listIterator.hasNext()) {
                IrInstruction irInstruction = listIterator.next();
                if (movePtrHashMap.containsKey(irInstruction)) {
                    // 找到了定义位置
                    ArrayList<IrInstruction> insertInstructions = movePtrHashMap.get(irInstruction);
                    for (IrInstruction insertInstruction : insertInstructions) {
                        listIterator.add(insertInstruction);
                    }
                    movePtrHashMap.remove(irInstruction);
                }
            }
        }
    }

    private boolean canPtrMove(IrGetElementPtr irPtr) {
        int flag = 0;
        for (int i = 1; i < irPtr.usedValues.size(); i++) {
            if (!irPtr.usedValues.get(i).isNum()) {
                flag = 1;
                break;
            }
        }
        return flag == 0;
    }

    public void basicBlockCombine2() {
        Iterator<IrBasicBlock> iterator = irBasicBlocks.iterator();
        while (iterator.hasNext()) {
            IrBasicBlock irBasicBlock = iterator.next();
            if (irBasicBlock.moveFlag) {
                // 找到move指令
                int flag = 0;
                for (int i = 0; i < irBasicBlock.getInstructions().size() - 1; i++) {
                    if (irBasicBlock.getInstructions().get(i) instanceof IrMove irMove
                            && irMove.target.reg == irMove.source.reg && !irMove.target.isInMem() && !irMove.source.isInMem()) {
                        continue;
                    } else {
                        flag = 1;
                        break;
                    }
                }
                if (flag == 0) {
                    // 当前基本块可以被合并, 最后一步了，不需要再维护前驱后继节点了
                    IrBasicBlock frontBasicBlock = irBasicBlock.getFrontNodes().get(0);
                    IrBasicBlock backBasicBlock = irBasicBlock.getBackNodes().get(0);
                    frontBasicBlock.replaceBackNode(backBasicBlock, irBasicBlock);
                    backBasicBlock.replaceFrontNode(frontBasicBlock, irBasicBlock);
                    System.out.println("move块被移走！！！！！！！！！！！！！！");
                    iterator.remove();
                }
            }
        }
    }
}
