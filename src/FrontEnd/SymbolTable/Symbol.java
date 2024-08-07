package FrontEnd.SymbolTable;

import FrontEnd.NonTerminal.Ident;
import MidEnd.IrCore.Value;

public class Symbol {
    // 构建符号表
    private static int ID = 0;
    private int id; // 自增唯一标识
    private String name;
    private int type; // 规定 0 -> a; 1 -> a[]; 2 -> a[][]; 3 -> func; 4 -> 一维指针; 5 -> 二维指针
    private int con; // 规定 1为常量 0为变量
    private int global; // 1 为全局 0 为非全局
    private int line;
    private int initVal; // 初值，为全局常量准备
    private Value identifier; // 符号，%1 @a 诸如此类

    public Symbol(Ident ident, int type, int con) {
        // 适用于变量 常量的构造
        this.id = ID;
        ID++;
        this.name = ident.getName();
        this.line = ident.getLine();
        this.type = type;
        this.con = con;
        this.global = 0;
    }

    public int getGlobal() {
        return global;
    }

    public void setGlobal(int global) {
        this.global = global;
    }

    public Symbol(String name, int type) {
        // 适用于函数符号的构造
        this.id = ID;
        ID++;
        this.name = name;
        this.type = type;
        this.con = 0;
        this.global = 0;
    }
    public int getInitVal() {
        return initVal;
    }

    public void setInitVal(int initVal) {
        this.initVal = initVal;
    }

    // 存的是地址
    public Value getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Value identifier) {
        this.identifier = identifier;
    }

    public int getLine() {
        return line;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public int getCon() {
        return con;
    }
}
