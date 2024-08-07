package FrontEnd.SymbolTable;

import MidEnd.IrCore.Value;

import java.util.ArrayList;
import java.util.HashMap;

public class StaticSymbolTable {
    static public HashMap<Integer, SymbolTable> symbolTableHashMap = new HashMap<>();
    static public SymbolTable curSymbolTable;
    static public int curSymbolId = 100000; // 在语法树建立和错误处理部分不用考虑该情况，在中间代码生成部分需要符号表的顺序逻辑
    static public ArrayList<SymbolTable> mipsSymbolTables;
    // 非定义时查询符号表 需要考虑符号顺序
    static public Symbol getSymbol(String name) {
        SymbolTable symbolTable = curSymbolTable;
        while (symbolTable.getFatherId() != -1) {
            if (symbolTable.searchSymbolTable(name) != null) {
                return symbolTable.searchSymbolTable(name);
            } else {
                symbolTable = symbolTableHashMap.get(symbolTable.getFatherId());
            }
        }
        // 最后遍历最外层符号表
        return symbolTable.searchSymbolTable(name);
    }

    // 定义时查询符号表 无需考虑顺序
    static public Symbol getSymbol(String name, int ignoreSeq) {
        SymbolTable symbolTable = curSymbolTable;
        while (symbolTable.getFatherId() != -1) {
            if (symbolTable.searchSymbolTable(name, ignoreSeq) != null) {
                return symbolTable.searchSymbolTable(name, ignoreSeq);
            } else {
                symbolTable = symbolTableHashMap.get(symbolTable.getFatherId());
            }
        }
        // 最后遍历最外层符号表
        return symbolTable.searchSymbolTable(name, ignoreSeq);
    }

    // mips生成时使用
    static public Symbol getSymbol(Value identifier) {
        for (SymbolTable symbolTable : mipsSymbolTables) {
            if (symbolTable.searchSymbolTable(identifier) != null) {
                return symbolTable.searchSymbolTable(identifier);
            }
        }
        // 最后遍历最外层符号表
        // System.out.println("ERROR! 一定会找到对应的符号");
        return null;
    }

    static public Boolean isRepeatDecl(String string) {
        // 当前符号表中不允许出现重复声明的情况！
        return curSymbolTable.searchSymbolTable(string) != null;
    }

    static public SymbolTable getCurSymbolTable(int id) {
        return symbolTableHashMap.get(id);
    }

    // 判断是否使用未声明的符号
    static public Boolean hasSameNameSymbol(String string) {
        // 需要一层层找到最上级
        SymbolTable symbolTable = curSymbolTable;
        while (symbolTable.getFatherId() != -1) {
            if (symbolTable.searchSymbolTable(string) != null) {
                return true;
            } else {
                symbolTable = symbolTableHashMap.get(symbolTable.getFatherId());
            }
        }
        // 最后遍历最外层符号表
        return symbolTable.searchSymbolTable(string) != null;
    }
}
