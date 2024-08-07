package FrontEnd.NonTerminal;

import MidEnd.IrCore.Value;

import java.util.ArrayList;

import static FrontEnd.SymbolTable.StaticSymbolTable.curSymbolTable;
import static FrontEnd.SymbolTable.StaticSymbolTable.getSymbol;
import static FrontEnd.SymbolTable.StaticSymbolTable.symbolTableHashMap;
import static MidEnd.IrCore.IrModule.curIrFunction;

public class Block {
    private ArrayList<BlockItem> blockItems;
    private int symbolTableId;

    public Block(int symbolTableId) {
        blockItems = new ArrayList<>();
        this.symbolTableId = symbolTableId;
    }

    public void addBlockItems(BlockItem blockItem) {
        blockItems.add(blockItem);
    }

    public ArrayList<BlockItem> getBlockItems() {
        return blockItems;
    }
    // 非最外层Block
    public void genLLVM() {
        curSymbolTable = symbolTableHashMap.get(symbolTableId);
        curSymbolTable.setfuncId(curIrFunction.getSymbolTableId()); // 设置为
        for (BlockItem blockItem : blockItems) {
            blockItem.genLLVM();
        }
        curSymbolTable = symbolTableHashMap.get(curSymbolTable.getFatherId());
    }

    // 最外层Block
    public void genParamsLLVM(FuncFParams funcFParams) {
        curSymbolTable = symbolTableHashMap.get(symbolTableId);
        curSymbolTable.setfuncId(curSymbolTable.getId());
        curIrFunction.setSymbolTableId(curSymbolTable.getId()); // 在生成Mips时一个函数共享一张符号表
        if (funcFParams != null) {
            ArrayList<FuncFParam> fParams = funcFParams.getFuncFParams();
            for (FuncFParam fParam : fParams) {
                String value = curIrFunction.allocVirtualReg();
                Value paramValue;
                if (fParam.getDim() == 0) {
                    paramValue = curIrFunction.addParam(value, 0);
                } else if (fParam.getDim() == 1) {
                    paramValue = curIrFunction.addParam(value, 1);
                } else {
                    paramValue =  curIrFunction.addParam(value, 2, fParam.getConstExps().get(0).calConstExp());
                }
                getSymbol(fParam.getIdent().getName(), 0).setIdentifier(paramValue);
            }
            // 为指针或变量重新分配地址，并保存
            for (FuncFParam fParam : fParams) {
                fParam.genLLVM();
            }
        }
        for (BlockItem blockItem : blockItems) {
            blockItem.genLLVM();
        }
        curSymbolTable = symbolTableHashMap.get(curSymbolTable.getFatherId());
    }
}
