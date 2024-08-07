package FrontEnd.SymbolTable;

import java.util.ArrayList;

public class FuncSymbol extends Symbol {
    private int retype; // 0 -> int, 1 -> void
    private int paramNum; // 参数数量
    private ArrayList<Integer> paramTypeList; // 存储参数类型 为 0 1 2

    public FuncSymbol(String name, int type, int retype, ArrayList<Integer> paramTypeList) {
        super(name, type);
        this.retype = retype;
        this.paramTypeList = paramTypeList;
    }

    public void setParamNum(int paramNum) {
        this.paramNum = paramNum;
    }

    public int getRetype() {
        return retype;
    }

    public int getParamNum() {
        return paramNum;
    }

    public ArrayList<Integer> getParamTypeList() {
        return paramTypeList;
    }

    public String getTypeList() {
        StringBuilder sb = new StringBuilder();
        for (Integer i : paramTypeList) {
            sb.append(i).append(" ");
        }
        return !paramTypeList.isEmpty() ? sb.toString() : "无";
    }
}
