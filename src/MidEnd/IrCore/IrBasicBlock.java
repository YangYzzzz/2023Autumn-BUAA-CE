package MidEnd.IrCore;

import FrontEnd.SymbolTable.Symbol;
import MidEnd.IrInstuctions.FakeValue;
import MidEnd.IrInstuctions.IrAdd;
import MidEnd.IrInstuctions.IrBr;
import MidEnd.IrInstuctions.IrCmp;
import MidEnd.IrInstuctions.IrGetElementPtr;
import MidEnd.IrInstuctions.IrLoad;
import MidEnd.IrInstuctions.IrMove;
import MidEnd.IrInstuctions.IrMul;
import MidEnd.IrInstuctions.IrPhi;
import MidEnd.IrInstuctions.IrRet;
import MidEnd.IrInstuctions.IrSdiv;
import MidEnd.IrInstuctions.IrStore;
import MidEnd.IrInstuctions.IrSub;
import MidEnd.IrInstuctions.IrZext;
import MidEnd.SymbolTable.IrSymbol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import static MidEnd.IrCore.IrModule.COMMON_SUBEXPRESSION_AND_UNUSED_CODE_DELETE_FLAG;
import static MidEnd.IrCore.IrModule.curIrFunction;
import static MidEnd.SymbolTable.IrSymbolTable.irSymbolTable;

// 目前将基本块和指令皆看作虚拟指令，实则基本块应包含内部的指令，或者说基本块作为User他的Value是什么？
public class IrBasicBlock extends User {
    private ArrayList<IrInstruction> irInstructions;

    public String getLabel() {
        return value;
    }

    private ArrayList<IrBasicBlock> backNodes;
    private ArrayList<IrBasicBlock> frontNodes;
    private HashMap<IrBasicBlock, Boolean> domedNodes; // 记录该节点被支配的节点,被支配为True，不被支配为False
    private IrBasicBlock idom; // 记录该节点的直接支配者
    private Set<IrBasicBlock> domFrontier; // 支配边界集合
    private ArrayList<IrBasicBlock> domTreeLeaves; // 支配树的叶子节点
    public Boolean moveFlag;
    public IrBasicBlock(String label) {
        super(label, 6);
        irInstructions = new ArrayList<>();
        backNodes = new ArrayList<>();
        frontNodes = new ArrayList<>();
        domedNodes = new HashMap<>();
        domFrontier = new HashSet<>();
        domTreeLeaves = new ArrayList<>();
        moveFlag = false;
    }

    public Set<IrBasicBlock> getDomFrontier() {
        return domFrontier;
    }

    public ArrayList<IrBasicBlock> getDomTreeLeaves() {
        return domTreeLeaves;
    }

    public void setDomTreeLeaves(ArrayList<IrBasicBlock> domTreeLeaves) {
        this.domTreeLeaves = domTreeLeaves;
    }

    public void setDomFrontier(Set<IrBasicBlock> domFrontier) {
        this.domFrontier = domFrontier;
    }

    public void addDomTreeLeaf(IrBasicBlock irBasicBlock) {
        domTreeLeaves.add(irBasicBlock);
    }

    public void printDF() {
        System.out.println("基本块名为: " + this.value + " 其支配边界为如下");
        for (IrBasicBlock dfBlock : domFrontier) {
            System.out.println(dfBlock.value);
        }
    }

    public void addDomFrontier(IrBasicBlock irBasicBlock) {
        domFrontier.add(irBasicBlock);
    }

    public IrBasicBlock getIdom() {
        return idom;
    }

    public void setIdom(IrBasicBlock idom) {
        this.idom = idom;
    }

    public HashMap<IrBasicBlock, Boolean> getDomedNodes() {
        return domedNodes;
    }

    public void printDomedTree() {
        // System.out.println("该节点为" + this.value + " 其被以下结点支配");
        for (Map.Entry<IrBasicBlock, Boolean> entry : domedNodes.entrySet()) {
            if (entry.getValue()) {
                // System.out.println(entry.getKey().value);
            }
        }
    }

    public void setDomedNodes(HashMap<IrBasicBlock, Boolean> domedNodes) {
        this.domedNodes = domedNodes;
    }

    public void addInstruction(IrInstruction irInstruction) {
//        if (irInstruction instanceof IrRet) {
//            irInstructions.add(irInstruction);
//            return;
//        }
        if (!irInstructions.isEmpty() && (irInstructions.get(irInstructions.size() - 1) instanceof IrBr || irInstructions.get(irInstructions.size() - 1) instanceof IrRet)) {
            return;
        }
        irInstructions.add(irInstruction);
    }

    public void addInstruction(IrMove irMove) {
        if (!irInstructions.isEmpty()) {
            irInstructions.add(irInstructions.size() - 1, irMove);
        } else {
            irInstructions.add(irMove);
        }
    }

    public void addPhiInstruction(IrPhi irPhi) {
        irInstructions.add(0, irPhi);
    }

    public Boolean isEmpty() {
        return irInstructions.isEmpty();
    }

    public ArrayList<IrInstruction> getInstructions() {
        return irInstructions;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!value.equals("delete")) {
            sb.append(value.substring(1)).append(":\n");
        }
        for (IrInstruction irInstruction : irInstructions) {
//            sb.append(irInstruction.printActiveVariable());
            sb.append("    ").append(irInstruction);
        }
        return sb.toString();
    }

    public ArrayList<Value> genMipsSymbolTable() {
        ArrayList<Value> mipsSymbolTable = new ArrayList<>();
        for (IrInstruction irInstruction : irInstructions) {
            if (irInstruction.value != null && irInstruction.value.charAt(0) == '%') {
                mipsSymbolTable.add(irInstruction);
            }
        }
        return mipsSymbolTable;
    }

    public ArrayList<IrBasicBlock> getBackNodes() {
        return backNodes;
    }

    public ArrayList<IrBasicBlock> getFrontNodes() {
        return frontNodes;
    }

    public void addFrontNode(IrBasicBlock irBasicBlock) {
        frontNodes.add(irBasicBlock);
    }

    public void addBackNode(IrBasicBlock irBasicBlock) {
        backNodes.add(irBasicBlock);
    }

    public boolean strictDomedByX(IrBasicBlock x) {
        return domedNodes.get(x) && !x.equals(this);
    }

    public Boolean equals(IrBasicBlock x) {
        return this.value.equals(x.value);
    }


    public void renameIns() {
        // 要被替换的假标签应当在usedValue内，不会出现在value处
        // System.out.println("当前重命名基本块为: " + this.value);
        HashMap<Symbol, IrPhi> irPhiHashMap = new HashMap<>();
        for (IrInstruction irInstruction : irInstructions) {
            if (irInstruction instanceof IrPhi irPhi) {
                irPhiHashMap.put(irPhi.symbol, irPhi);
                addIdomStream(irPhi);
                continue;
            }
            ListIterator<Value> iterator = irInstruction.usedValues.listIterator();
            while (iterator.hasNext()) {
                Value value = iterator.next();
                if (value.valueType == 9) {
                    // 不能瞎转换
                    iterator.remove();
                    if (irPhiHashMap.containsKey(((FakeValue) value).symbol)) {
                        iterator.add(irPhiHashMap.get(((FakeValue) value).symbol));
                    } else {
                        // 找到距离其最近的定义
                        iterator.add(getRencentValue(((FakeValue) value).symbol));
                    }
                }
            }
        }
    }

    public boolean hasDef(Symbol symbol) {
        HashMap<IrBasicBlock, Value> block2LastValues = irSymbolTable.getFunctionIrSymbolTable(curIrFunction).get(symbol).getBlockDefs();
        IrBasicBlock idom = this;
        while (idom != null) {
            if (block2LastValues.containsKey(idom)) {
                return true;
            } else {
                idom = idom.idom;
            }
        }
        return false;
    }

    public void addIdomStream(IrPhi irPhi) {
        // System.out.println(irPhi.symbol.getName());
        HashMap<IrBasicBlock, Value> block2LastValues = irSymbolTable.getFunctionIrSymbolTable(curIrFunction).get(irPhi.symbol).getBlockDefs();
        for (IrBasicBlock frontBasicBlock : frontNodes) {
            // 遍历前驱基本块, 若沿支配树能找到最近定义，则加入基本块流之中
            IrBasicBlock frontTemp = frontBasicBlock;
            while (frontTemp != null) {
                if (block2LastValues.containsKey(frontTemp)) {
                    irPhi.addPhiStream(frontBasicBlock, block2LastValues.get(frontTemp));
                    break;
                }
                frontTemp = frontTemp.idom;
            }
        }
    }

    // 沿着直接支配者向上遍历，找到最近的定义Value
    public Value getRencentValue(Symbol symbol) {
        // System.out.println("基本块为：" + value + " 追踪最近的Value 符号为" + symbol.getName() + " 类型为" + type);
        HashMap<IrBasicBlock, Value> block2LastValues = irSymbolTable.getFunctionIrSymbolTable(curIrFunction).get(symbol).getBlockDefs();
        IrBasicBlock idom = this.idom;
        while (idom != null) {
            // System.out.println("追踪当前基本块为: " + idom.value);
            if (block2LastValues.containsKey(idom)) {
                // System.out.println("追踪最近Value为: " + block2LastValues.get(idom).value);
                return block2LastValues.get(idom);
            } else {
                idom = idom.idom;
            }
        }
        return null;
    }

    public void deleteFrontNode(IrBasicBlock irBasicBlock) {
        for (int i = 0; i < frontNodes.size(); i++) {
            if (frontNodes.get(i).equals(irBasicBlock)) {
                frontNodes.remove(i);
                break;
            }
        }
    }

    public void blockCombine(IrBasicBlock curBasicBlock) {
        HashMap<Symbol, Value> symbolValueHashMap = new HashMap<>();
        HashMap<Symbol, IrSymbol> irSymbol = irSymbolTable.getFunctionIrSymbolTable(curIrFunction);
        for (Map.Entry<Symbol, IrSymbol> entry : irSymbol.entrySet()) {
            if (entry.getValue().getBlockDefs().containsKey(this)) {
                symbolValueHashMap.put(entry.getKey(), entry.getValue().getBlockDefs().get(this));
            }
        }
        // 最后一条一定是br
        irInstructions.remove(irInstructions.size() - 1);
        for (IrInstruction irInstruction : curBasicBlock.irInstructions) {
            // 遍历后继s
            ListIterator<Value> iterator = irInstruction.usedValues.listIterator();
            while (iterator.hasNext()) {
                Value value = iterator.next();
                if (value.valueType == 9 && symbolValueHashMap.containsKey(((FakeValue) value).symbol)) {
                    // 不能瞎转换
                    iterator.remove();
                    iterator.add(symbolValueHashMap.get(((FakeValue) value).symbol));
                }
            }
        }
        irInstructions.addAll(curBasicBlock.irInstructions);
        // 修改后继为当前块的后继
        backNodes.clear();
        backNodes.addAll(curBasicBlock.backNodes);
    }

    // 修改前驱后继基本块
    public void replaceFrontNode(IrBasicBlock curBasicBlock, IrBasicBlock frontBasicBlock) {
        for (int i = 0; i < frontNodes.size(); i++) {
            if (frontNodes.get(i).equals(frontBasicBlock)) {
                System.out.println("本来的前驱结点为：" + frontBasicBlock + ", 被替换的前驱结点为:" + curBasicBlock);
                frontNodes.set(i, curBasicBlock);
                break;
            }
        }
    }

    public void replaceBackNode(IrBasicBlock curBasicBlock, IrBasicBlock backBasicBlock) {
        for (int i = 0; i < backNodes.size(); i++) {
            if (backNodes.get(i).equals(backBasicBlock)) {
                System.out.println("本来的后继结点为：" + backBasicBlock + ", 被替换的后继结点为:" + curBasicBlock);
                backNodes.set(i, curBasicBlock);
                break;
            }
        }
        IrInstruction brIns = irInstructions.get(irInstructions.size() - 1);

        // 同时需要修改br指令的跳转位置
        if (brIns instanceof IrBr irBr) {
            if (irBr.usedValues.get(1).equal(backBasicBlock)) {
                irBr.usedValues.set(1, curBasicBlock);
            } else if (irBr.usedValues.get(2).equal(backBasicBlock)) {
                irBr.usedValues.set(2, curBasicBlock);
            }
        } else {
            throw new RuntimeException();
        }
    }

    public Set<Value> getFirstIn() {
        return irInstructions.get(0).in;
    }

    public void calConflictGraph() {
        ArrayList<IrInstruction> backInstructions = new ArrayList<>();
//        Set<Value> lastOut = irInstructions.get(irInstructions.size() - 1).out;
//        for (IrBasicBlock irBasicBlock : backNodes) {
//            Set<Value> backNodeFirstIn = irBasicBlock.getFirstIn();
//            for (Value in : backNodeFirstIn) {
//                if (!lastOut.contains(in)) {
//                    ACTIVE_VARIABLE_FLAG = 1;
//                    lastOut.add(in);
//                }
//            }
//        }

        for (IrBasicBlock irBasicBlock : backNodes) {
            if (!irBasicBlock.irInstructions.isEmpty()) {
                backInstructions.add(irBasicBlock.irInstructions.get(0));
            }
        }
        for (int i = irInstructions.size() - 1; i >= 0; i--) {
            irInstructions.get(i).genActiveVariable(backInstructions);
            backInstructions.clear();
            backInstructions.add(irInstructions.get(i));
        }
    }

    public void delConflictGraph() {
        for (IrInstruction irInstruction : irInstructions) {
            irInstruction.in.clear();
            irInstruction.out.clear();
            irInstruction.adjValues.clear();
        }
    }

    public void commonSubexpressionDeletion() {
        HashMap<String, Value> commonSubexpression = new HashMap<>(); // 记录哈希值与对应的Value
        for (int i = 0; i < irInstructions.size(); i++) {
            IrInstruction irInstruction = irInstructions.get(i);
            int flag = 0;
            ArrayList<String> hashCodes = irInstruction.toHashCode();
            Value replaceValue = null;
            for (String string : hashCodes) {
                if (commonSubexpression.containsKey(string)) {
                    // 可被公共子表达式删除
                    replaceValue = commonSubexpression.get(string);
                    flag = 1;
                    break;
                }
            }

            if (flag == 0 && (irInstruction instanceof IrAdd || irInstruction instanceof IrSub ||
                    irInstruction instanceof IrMul || irInstruction instanceof IrSdiv ||
                    irInstruction instanceof IrGetElementPtr || irInstruction instanceof IrZext || irInstruction instanceof IrCmp)) {
                for (String string : hashCodes) {
                    commonSubexpression.put(string, irInstruction);
                }
            } else if (flag == 1) {
                for (int j = i + 1; j < irInstructions.size(); j++) {
                    IrInstruction curInstruction = irInstructions.get(j);
                    for (int k = 0; k < curInstruction.usedValues.size(); k++) {
                        Value usedValue = curInstruction.usedValues.get(k);
                        if (usedValue.equal(irInstruction)) {
                            COMMON_SUBEXPRESSION_AND_UNUSED_CODE_DELETE_FLAG = 1;
                            curInstruction.usedValues.set(k, replaceValue);
                        }
                    }
                }
            }
        }
    }

    public void unusedStoreDelete() {
        Set<Integer> index = new HashSet<>();
        for (int i = 0; i < irInstructions.size(); i++) {
            if (irInstructions.get(i) instanceof IrLoad) {
                Value addr = irInstructions.get(i).usedValues.get(0);
                int cnt = 0;
                for (int j = i - 1; j >= 0; j--) {
                    if (irInstructions.get(j) instanceof IrStore irStore && irStore.usedValues.get(1).equal(addr)) {
                        cnt++;
                        if (cnt > 1) {
                            // 非第一次出现
                            System.out.println("多余store！！！！！");
                            index.add(j);
                        }
                    } else if (irInstructions.get(j) instanceof IrLoad) {
                        break;
                    }
                }
            }
        }

        ArrayList<IrInstruction> newIns = new ArrayList<>();
        for (int i = 0; i < irInstructions.size(); i++) {
            if (!index.contains(i)) {
                newIns.add(irInstructions.get(i));
            }
        }

        irInstructions = newIns;
    }

    public void unusedLoadDelete() {
        HashMap<String, Integer> commonSubexpression = new HashMap<>(); // 记录哈希值与对应的Value
        for (int i = 0; i < irInstructions.size(); i++) {
            IrInstruction curInstruction = irInstructions.get(i);
            int flag = 0;
            ArrayList<String> hashCodes = curInstruction.toHashCode();
            if (curInstruction instanceof IrLoad && commonSubexpression.containsKey(hashCodes.get(0))) {
                // 是load且哈希表索引到
                int startIndex = commonSubexpression.get(hashCodes.get(0));
                for (int j = startIndex + 1; j < i; j++) {
                    if (irInstructions.get(j) instanceof IrStore) {
                        flag = 1; // 中间存在store导致两次load之间进行store 说明我们不能消除这条load指令
                        break;
                    }
                }

                if (flag == 0) {
                    for (int j = i + 1; j < irInstructions.size(); j++) {
                        IrInstruction instruction = irInstructions.get(j);
                        for (int k = 0; k < instruction.usedValues.size(); k++) {
                            Value usedValue = instruction.usedValues.get(k);
                            if (usedValue.equal(curInstruction)) {
                                curInstruction.usedValues.set(k, irInstructions.get(startIndex));
                            }
                        }
                    }
                }
            } else if (curInstruction instanceof IrLoad && !commonSubexpression.containsKey(hashCodes.get(0))) {
                commonSubexpression.put(hashCodes.get(0), i);
            }
        }
    }
}
