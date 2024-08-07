package FrontEnd.SymbolTable;

import MidEnd.IrCore.Value;

import java.util.HashMap;
import java.util.Map;

import static FrontEnd.SymbolTable.StaticSymbolTable.curSymbolId;

public class SymbolTable {
    static int ID = 0;
    private int id;
    private int fatherId;
    private HashMap<String, Symbol> symbolHashMap;
    private int funcId; // 函数内所有block的SymbolTable都指向最外层的id，最外层的block指向0，即全局变量表

    public SymbolTable(int fatherId) {
        this.id = ID;
        this.fatherId = fatherId;
        this.funcId = 0;
        ID++;
        this.symbolHashMap = new HashMap<>();
    }

    public int getfuncId() {
        return funcId;
    }

    public void setfuncId(int funcId) {
        this.funcId = funcId;
    }

    public int getId() {
        return id;
    }

    public int getFatherId() {
        return fatherId;
    }

    public Symbol searchSymbolTable(String str, int ignoreSeq) {
        if (symbolHashMap.containsKey(str)) {
            // type等于0时正常检索 适用于判断是否重复定义 type等于1时判断函数是否无定义 type等于2时判断常变量是否无定义
            // 在最上层变量比函数先定义 重名函数一定不会被加入符号表中 当检查变量是否未定义时 一定不会产生将变量认成函数的情况
            return symbolHashMap.get(str);
        }
        return null;
    }
    public Symbol searchSymbolTable(String str) {
        if (symbolHashMap.containsKey(str) && symbolHashMap.get(str).getId() <= curSymbolId) {
            // type等于0时正常检索 适用于判断是否重复定义 type等于1时判断函数是否无定义 type等于2时判断常变量是否无定义
            // 在最上层变量比函数先定义 重名函数一定不会被加入符号表中 当检查变量是否未定义时 一定不会产生将变量认成函数的情况
            return symbolHashMap.get(str);
        }
        return null;
    }

    public Symbol searchSymbolTable(Value identifier) {
        for (Map.Entry<String, Symbol> entry : symbolHashMap.entrySet())  {
            if (entry.getValue().getIdentifier().equal(identifier)) {
                return entry.getValue();
            }
        }
        return null;
    }
    public void addSymbol(String string, Symbol symbol) {
        // 先把symbol内容填好，然后再装进对应的符号表中
        symbolHashMap.put(string, symbol);
    }

    public HashMap<String, Symbol> getSymbolHashMap() {
        return symbolHashMap;
    }
}
