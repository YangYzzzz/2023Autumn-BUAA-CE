package FrontEnd.NonTerminal;

import FrontEnd.SymbolTable.Dimension1ArraySymbol;
import FrontEnd.SymbolTable.Dimension2ArraySymbol;
import FrontEnd.SymbolTable.Dimension2PtrSymbol;
import FrontEnd.SymbolTable.Symbol;
import MidEnd.IrInstuctions.FakeValue;
import MidEnd.IrCore.IrInstruction;
import MidEnd.IrInstuctions.IrGetElementPtr;
import MidEnd.IrInstuctions.IrLoad;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

import static FrontEnd.SymbolTable.StaticSymbolTable.getSymbol;
import static MidEnd.IrCore.IrModule.MODE;
import static MidEnd.IrCore.IrModule.curIrBasicBlock;
import static MidEnd.IrCore.IrModule.curIrFunction;
import static MidEnd.SymbolTable.IrSymbolTable.irSymbolTable;

public class LVal {
    private Ident ident;
    private int line;
    private ArrayList<Exp> exps;

    public LVal() {
        exps = new ArrayList<>();
    }

    public Ident getIdent() {
        return ident;
    }

    public void setIdent(Ident ident) {
        this.ident = ident;
    }

    public void addExp(Exp exp) {
        exps.add(exp);
    }

    public int getLValType() {
        return exps.size();
    }

    public ArrayList<Integer> calLValExps() {
        ArrayList<Integer> constValues = new ArrayList<>();
        for (Exp exp : exps) {
            constValues.add(exp.calExp());
        }
        return constValues;
    }

    public Value genGetAddrOnlyLLVM() {
        Symbol symbol = getSymbol(ident.getName());
        Value baseAddr = symbol.getIdentifier();
        switch (symbol.getType()) {
            case 0: {
                // 直接取符号表中保存的地址即可
                return baseAddr;
            }
            case 1: {
                // 一维数组
                Value pos = exps.get(0).genLLVM();
                return getTarAddr((Dimension1ArraySymbol) symbol, pos);
            }
            case 2: {
                // 二维数组
                Value pos1 = exps.get(0).genLLVM();
                Value pos2 = exps.get(1).genLLVM();
                return getTarAddr((Dimension2ArraySymbol) symbol, pos1, pos2);
            }
            case 4: {
                // 一维数组指针
                Value pos = exps.get(0).genLLVM();
                return getTarAddr(baseAddr, pos);
            }
            case 5: {
                // 二维数组指针
                Value pos1 = exps.get(0).genLLVM();
                Value pos2 = exps.get(1).genLLVM();
                return getTarAddr((Dimension2PtrSymbol) symbol, pos1, pos2);
            }
        }
        return null;
    }

    // 二维数组，求一维地址
    private Value getTarAddr(Dimension2ArraySymbol symbol, Value pos1, Value pos2) {
        String tarAddr = curIrFunction.allocVirtualReg();
        IrInstruction irInstruction = new IrGetElementPtr(tarAddr, symbol.getIdentifier(), new Value("0", 0), pos1, pos2);
        curIrBasicBlock.addInstruction(irInstruction);
        return irInstruction;
    }

    // 一维数组，求一维地址
    private Value getTarAddr(Dimension1ArraySymbol symbol, Value pos) {
        String tarAddr = curIrFunction.allocVirtualReg();
        IrInstruction irInstruction = new IrGetElementPtr(tarAddr, symbol.getIdentifier(), new Value("0", 0), pos);
        curIrBasicBlock.addInstruction(irInstruction);
        return irInstruction;
    }

    // 一维指针，求pos处一维地址(指向i32地址)
    private Value getTarAddr(Value baseAddr, Value pos) {
        if (MODE != 1) {
            String trueBaseAddr = curIrFunction.allocVirtualReg();
            IrInstruction irInstruction = new IrLoad(trueBaseAddr, 1, baseAddr);
            curIrBasicBlock.addInstruction(irInstruction);
            baseAddr = irInstruction;
        }
        String tarAddr = curIrFunction.allocVirtualReg();
        IrInstruction irInstruction = new IrGetElementPtr(tarAddr, baseAddr, pos);
        curIrBasicBlock.addInstruction(irInstruction);
        return irInstruction;
    }

    /*
        需要先把真实地址从真实地址的地址中取出，再做求位置操作
        二维指针，求一维地址(指向i32)
     */
    private Value getTarAddr(Dimension2PtrSymbol symbol, Value pos1, Value pos2) {
        Value baseAddr;
        if (MODE == 1) {
            baseAddr = symbol.getIdentifier();
        } else {
            String trueBaseAddr = curIrFunction.allocVirtualReg();
            IrInstruction irInstruction = new IrLoad(trueBaseAddr, 2, symbol.getIdentifier());
            curIrBasicBlock.addInstruction(irInstruction);
            baseAddr = irInstruction;
        }
        String tarAddr = curIrFunction.allocVirtualReg();
        IrInstruction irInstruction = new IrGetElementPtr(tarAddr, baseAddr, pos1, pos2);
        curIrBasicBlock.addInstruction(irInstruction);
        return irInstruction;
    }

    /*
        二维指针，传递自己
     */
    private Value getTarAddr(Dimension2PtrSymbol symbol) {
        if (MODE == 1) {
            return symbol.getIdentifier();
        } else {
            String tarAddr = curIrFunction.allocVirtualReg();
            IrInstruction irInstruction = new IrLoad(tarAddr, 2, symbol.getIdentifier());
            curIrBasicBlock.addInstruction(irInstruction);
            return irInstruction;
        }
    }

    public Value genLLVM() {
        Symbol symbol = getSymbol(ident.getName());
        Value baseAddr = symbol.getIdentifier();
        int reType;
        switch (symbol.getType()) {
            case 4: {
                reType = 1 - exps.size();
                break;
            }
            case 5: {
                reType = 2 - exps.size();
                break;
            }
            default: {
                reType = symbol.getType() - exps.size();
                break;
            }
        }
        // 返回的类型 若为0 代表返回的是值 若为1 返回的是1维指针 2返回的是2维指针
        IrInstruction irInstruction;
        if (reType == 0) {
            switch (symbol.getType()) {
                case 0: {
                    // 返回值
                    if (symbol.getCon() == 1) {
                        return new Value(String.valueOf(symbol.getInitVal()), 0);
                    } else {
                        if (MODE == 1 && symbol.getGlobal() == 0) {
                            Value value = irSymbolTable.getValue(symbol);
                            if (value != null) {
                                return value;
                            } else {
                                // 产生假value，该假value记录着该符号Symbol
                                return new FakeValue(symbol);
                            }
                        } else {
                            String value = curIrFunction.allocVirtualReg();
                            irInstruction = new IrLoad(value, 0, baseAddr);
                            curIrBasicBlock.addInstruction(irInstruction); // 值 和 地址
                            return irInstruction;
                        }
                    }
                }
                case 1: {
                    // 一维数组 需要好多指令 int a[1] 传 a[1]
                    Value pos = exps.get(0).genLLVM();
                    if (pos.isNum() && symbol.getCon() == 1) {
                        return new Value(String.valueOf(((Dimension1ArraySymbol) symbol).getInitials().get(Integer.parseInt(pos.value))), 0);
                    } else {
                        Value tarAddr = getTarAddr((Dimension1ArraySymbol) symbol, pos);
                        String value = curIrFunction.allocVirtualReg();
                        irInstruction = new IrLoad(value, 0, tarAddr);
                        curIrBasicBlock.addInstruction(irInstruction);
                        return irInstruction;
                    }
                }
                case 2: {
                    // 二维数组 需要好多指令
                    Value pos1 = exps.get(0).genLLVM();
                    Value pos2 = exps.get(1).genLLVM();
                    if (pos1.isNum() && pos2.isNum() && symbol.getCon() == 1) {
                        int pos = symbol.getIdentifier().secondSize * Integer.parseInt(pos1.value) + Integer.parseInt(pos2.value);
                        return new Value(String.valueOf(((Dimension2ArraySymbol) symbol).getInitials().get(pos)), 0);
                    } else {
                        Value tarAddr = getTarAddr((Dimension2ArraySymbol) symbol, pos1, pos2);
                        String value = curIrFunction.allocVirtualReg();
                        irInstruction = new IrLoad(value, 0, tarAddr);
                        curIrBasicBlock.addInstruction(irInstruction);
                        return irInstruction;
                    }
                }
                case 4: {
                    Value pos = exps.get(0).genLLVM();
                    // 当前是地址的地址，需要从当前地址中取出真实地址
                    // 原指针 a[]，求值 a[1]
                    Value tarAddr = getTarAddr(baseAddr, pos);
                    String value = curIrFunction.allocVirtualReg();
                    irInstruction = new IrLoad(value, 0, tarAddr);
                    curIrBasicBlock.addInstruction(irInstruction);
                    return irInstruction;
                }
                case 5: {
                    // 原指针a[][2]，求值a[1][2]
                    Value pos1 = exps.get(0).genLLVM();
                    Value pos2 = exps.get(1).genLLVM();
                    Value tarAddr = getTarAddr((Dimension2PtrSymbol) symbol, pos1, pos2);
                    String value = curIrFunction.allocVirtualReg();
                    irInstruction = new IrLoad(value, 0, tarAddr);
                    curIrBasicBlock.addInstruction(irInstruction);
                    return irInstruction;
                }
            }
        } else if (reType == 1) {
            switch (symbol.getType()) {
                case 1: {
                    // 原一维数组a[1] 传a 即自己
                    return getTarAddr((Dimension1ArraySymbol) symbol, new Value("0", 0));
                }
                case 2: {
                    // 原2维a[1][1] 传a[0] 本质上传的还是零维数组指针
                    Value shiftingOnLine = exps.get(0).genLLVM();
                    return getTarAddr((Dimension2ArraySymbol) symbol, shiftingOnLine, new Value("0", 0));
                }
                case 4: {
                    // 原1维指针a[] i32**，
                    return getTarAddr(baseAddr, new Value("0", 0));
                }
                case 5: {
                    // 原二维指针a[1][1] 传a[1]
                    Value shiftingOnLine = exps.get(0).genLLVM();
                    return getTarAddr((Dimension2PtrSymbol) symbol, shiftingOnLine, new Value("0", 0));
                }
            }
        } else {
            switch (symbol.getType()) {
                case 2: {
                    // 二维数组a[1][1]，传二维指针a，转化为一维数组指针传递
                    String tarAddr = curIrFunction.allocVirtualReg();
                    irInstruction = new IrGetElementPtr(tarAddr, baseAddr, new Value("0", 0), new Value("0", 0), true);
                    curIrBasicBlock.addInstruction(irInstruction);
                    return irInstruction;
                }
                case 5: {
                    // 二维指针a[][1]，传二维a 其实相当于不变 原先是[3 x i32]* 传的还是 [3 x i32]* 需要将地址从基地址中取出来
                    // 二维指针，求二维地址(指向[n x i32])
                    return getTarAddr((Dimension2PtrSymbol) symbol);
                }
            }
        }
        return null;
    }
}
