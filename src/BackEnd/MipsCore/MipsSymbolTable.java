package BackEnd.MipsCore;

import MidEnd.IrCore.Value;

import java.util.ArrayList;
import java.util.HashMap;

public class MipsSymbolTable {
    // Integer为funcId, 后面为函数符号表
    static public HashMap<Integer, ArrayList<Value>> mipsSymbolTableHashMap = new HashMap<>();
    static public ArrayList<Value> curMipsSymbolTable;

    static public void getCurMipsSymbolTable(int funcId) {
        curMipsSymbolTable = mipsSymbolTableHashMap.get(funcId);
    }

}
