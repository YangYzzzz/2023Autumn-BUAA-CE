import FrontEnd.Core.Lexer;
import FrontEnd.Core.Parser;
import FrontEnd.Info.WrongInfo;
import FrontEnd.NonTerminal.CompUnit;
import FrontEnd.SymbolTable.FuncSymbol;
import FrontEnd.SymbolTable.Symbol;
import FrontEnd.SymbolTable.SymbolTable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

import static BackEnd.MipsCore.MipsModule.mipsModule;
import static FrontEnd.SymbolTable.StaticSymbolTable.symbolTableHashMap;
import static MidEnd.IrCore.IrModule.MODE;
import static MidEnd.IrCore.IrModule.irModule;

public class Compiler {
    public static void main(String[] args) throws IOException {
        // 刷新文件
        Lexer lexer = new Lexer();
        lexer.clearAnnotation();
//        lexer.exePlusAndSub();
        Parser parser = new Parser(lexer);
        // 进行词法分析
        // 语法分析
        CompUnit compUnit = parser.parseCompUnit();
        lexer.printOutput();
        for (Map.Entry<Integer, SymbolTable> entry : symbolTableHashMap.entrySet()) {
            // System.out.println("符号表的id为： " + entry.getKey() + "; 父表id为：" + entry.getValue().getFatherId());
            SymbolTable symbolTable = entry.getValue();
            for (Map.Entry<String, Symbol> entry1 : symbolTable.getSymbolHashMap().entrySet()) {
                // System.out.println("符号名称为："+entry1.getKey() + "; 符号类型为：" + entry1.getValue().getType() + "; 是否常数：" +
//                        entry1.getValue().getCon());
                if (entry1.getValue().getType() == 3) {
                    FuncSymbol funcSymbol = (FuncSymbol) entry1.getValue();
                    // System.out.println("返回类型：" + funcSymbol.getRetype() + "; 参数数目：" + funcSymbol.getParamNum() + "; 参数类型：" + funcSymbol.getTypeList());
                }
            }
        }
        parser.printWrongInfo();
        ArrayList<WrongInfo> wrongInfos = parser.getWrongInfos();
        if (!wrongInfos.isEmpty()) {
            // System.out.println("编译报错，请先解决问题");
            for (WrongInfo wrongInfo : wrongInfos) {
                if (wrongInfo.getFlag() == 0) {
                    // System.out.println(wrongInfo.getWrongCode() + " " + wrongInfo.getWrongMes() + " " + wrongInfo.getWrongLine());
                }
            }
        }

        compUnit.genLLVM();
        Path path = Path.of("llvm_ir.txt");
        if (MODE == 1) {
            irModule.optimizeLLVM();
        }
        Files.writeString(path, irModule.toString());
        System.out.println(irModule);

        // 生成Mips代码
        mipsModule.setMipsModule(irModule);
        mipsModule.genMips();
        Path mipsPath = Path.of("mips.txt");
        Files.writeString(mipsPath, mipsModule.toString());
        System.out.println(mipsModule);
//        throw new RuntimeException();
    }
}
