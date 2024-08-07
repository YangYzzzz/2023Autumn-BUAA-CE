package FrontEnd.NonTerminal;

import java.util.ArrayList;

import static FrontEnd.SymbolTable.StaticSymbolTable.curSymbolTable;
import static FrontEnd.SymbolTable.StaticSymbolTable.symbolTableHashMap;
public class CompUnit {
    private ArrayList<Decl> globalDecls;
    private ArrayList<FuncDef> funcDefs;
    private MainFuncDef mainFuncDef;

    public CompUnit() {
        globalDecls = new ArrayList<>();
        funcDefs = new ArrayList<>();
    }

    public void setMainFuncDef(MainFuncDef mainFuncDef) {
        this.mainFuncDef = mainFuncDef;
    }

    public void addDecl(Decl decl) {
        globalDecls.add(decl);
    }

    public void addFuncDef(FuncDef funcDef) {
        funcDefs.add(funcDef);
    }

    // 生成LLVM的module结构
    public void genLLVM() {
        // 全局符号表
        curSymbolTable = symbolTableHashMap.get(0);
        for (Decl decl : globalDecls) {
            // @a = dso_local global [10 x i32] [i32 1, i32 2, i32 3, i32 0, i32 0, i32 0, i32 0, i32 0, i32 0, i32 0]
            //@b = dso_local global [10 x [20 x i32]] zeroinitializer
            //@c = dso_local global [5 x [5 x i32]] [[5 x i32] [i32 1, i32 2, i32 3, i32 0, i32 0], [5 x i32] [i32 1, i32 2, i32 3, i32 4, i32 5], [5 x i32] zeroinitializer, [5 x i32] zeroinitializer, [5 x i32] zeroinitializer]
            //@a = dso_local global i32 1, align 4
            decl.genGlobalLLVM();
        }
        for (FuncDef funcDef : funcDefs) {
            funcDef.genLLVM();
        }
        mainFuncDef.genLLVM();
    }
}
