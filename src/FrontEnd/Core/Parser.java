package FrontEnd.Core;

import FrontEnd.Info.WordInfo;
import FrontEnd.Info.WrongInfo;
import FrontEnd.NonTerminal.AddExp;
import FrontEnd.NonTerminal.AllStmt.AssignStmt;
import FrontEnd.NonTerminal.AllStmt.BlockStmt;
import FrontEnd.NonTerminal.AllStmt.BreakOrContinueStmt;
import FrontEnd.NonTerminal.AllStmt.ExpStmt;
import FrontEnd.NonTerminal.AllStmt.InputStmt;
import FrontEnd.NonTerminal.AllStmt.JudgeStmt;
import FrontEnd.NonTerminal.AllStmt.LoopStmt;
import FrontEnd.NonTerminal.AllStmt.OutputStmt;
import FrontEnd.NonTerminal.AllStmt.ReturnStmt;
import FrontEnd.NonTerminal.BType;
import FrontEnd.NonTerminal.Block;
import FrontEnd.NonTerminal.BlockItem;
import FrontEnd.NonTerminal.CompUnit;
import FrontEnd.NonTerminal.Cond;
import FrontEnd.NonTerminal.ConstDecl;
import FrontEnd.NonTerminal.ConstDef;
import FrontEnd.NonTerminal.ConstExp;
import FrontEnd.NonTerminal.ConstInitVal;
import FrontEnd.NonTerminal.Decl;
import FrontEnd.NonTerminal.EqExp;
import FrontEnd.NonTerminal.Exp;
import FrontEnd.NonTerminal.ForStmt;
import FrontEnd.NonTerminal.FuncDef;
import FrontEnd.NonTerminal.FuncFParam;
import FrontEnd.NonTerminal.FuncFParams;
import FrontEnd.NonTerminal.FuncRParams;
import FrontEnd.NonTerminal.FuncType;
import FrontEnd.NonTerminal.Ident;
import FrontEnd.NonTerminal.InitVal;
import FrontEnd.NonTerminal.LAndExp;
import FrontEnd.NonTerminal.LOrExp;
import FrontEnd.NonTerminal.LVal;
import FrontEnd.NonTerminal.MainFuncDef;
import FrontEnd.NonTerminal.MulExp;
import FrontEnd.NonTerminal.Number;
import FrontEnd.NonTerminal.PrimaryExp;
import FrontEnd.NonTerminal.RelExp;
import FrontEnd.NonTerminal.Sign;
import FrontEnd.NonTerminal.Stmt;
import FrontEnd.NonTerminal.UnaryExp;
import FrontEnd.NonTerminal.UnaryOp;
import FrontEnd.NonTerminal.VarDecl;
import FrontEnd.NonTerminal.VarDef;
import FrontEnd.SymbolTable.Dimension1ArraySymbol;
import FrontEnd.SymbolTable.Dimension1PtrSymbol;
import FrontEnd.SymbolTable.Dimension2ArraySymbol;
import FrontEnd.SymbolTable.Dimension2PtrSymbol;
import FrontEnd.SymbolTable.FuncSymbol;
import FrontEnd.SymbolTable.Symbol;
import FrontEnd.SymbolTable.SymbolTable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Stack;

import static FrontEnd.Core.Lexer.errorFilePath;
import static FrontEnd.SymbolTable.StaticSymbolTable.curSymbolTable;
import static FrontEnd.SymbolTable.StaticSymbolTable.getSymbol;
import static FrontEnd.SymbolTable.StaticSymbolTable.hasSameNameSymbol;
import static FrontEnd.SymbolTable.StaticSymbolTable.isRepeatDecl;
import static FrontEnd.SymbolTable.StaticSymbolTable.symbolTableHashMap;

public class Parser {
    private final Lexer lexer;
    private final ArrayList<WrongInfo> wrongInfos;
    // 将Parser和符号表去耦合
    private Stack<LoopStmt> loopStmts; // 当前的循环嵌套层数
    private Sign curFuncType; // 当前解析中的函数的类型 0 -> int; 1 -> void;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.wrongInfos = new ArrayList<>();
        this.loopStmts = new Stack<>();
        // 预读一个单词
        lexer.next();
    }

    public ArrayList<WrongInfo> getWrongInfos() {
        return wrongInfos;
    }

    public void writeOutput(String s) {
        if (lexer.getFlag() == 0) {
            lexer.addOutput("<" + s + ">" + "\n");
        }
    }

    // 开始分析CompUnit
    public CompUnit parseCompUnit() {
        CompUnit compUnit = new CompUnit();
        WordInfo wordInfo = lexer.peek();
        // 创建全局 即第0层的symbolTable
        curSymbolTable = new SymbolTable(-1);
        symbolTableHashMap.put(curSymbolTable.getId(), curSymbolTable);
        while (wordInfo.getCategory() == Sign.INTTK || wordInfo.getCategory() == Sign.CONSTTK) {
            if (wordInfo.getCategory() == Sign.CONSTTK) {
                compUnit.addDecl(parseConstDecl());
                wordInfo = lexer.peek();
            } else {
                // 咋瓦鲁多
                lexer.stop();
                lexer.next();
                wordInfo = lexer.peek();
                if (wordInfo.getCategory() == Sign.MAINTK) {
                    lexer.reflow();
                    wordInfo = lexer.peek();
                    break;
                } else {
                    lexer.next();
                    wordInfo = lexer.peek();
                    if (wordInfo.getCategory() == Sign.LPARENT) {
                        lexer.reflow();
                        wordInfo = lexer.peek();
                        break;
                    } else if (wordInfo.getCategory() == Sign.ASSIGN || wordInfo.getCategory() == Sign.LBRACK
                            || wordInfo.getCategory() == Sign.SEMICN || wordInfo.getCategory() == Sign.COMMA) {
                        lexer.reflow();
                        compUnit.addDecl(parseVarDecl());
                        wordInfo = lexer.peek();
                    }
                }
            }
        }
        while (wordInfo.getCategory() == Sign.VOIDTK || wordInfo.getCategory() == Sign.INTTK) {
            if (wordInfo.getCategory() == Sign.VOIDTK) {
                compUnit.addFuncDef(parseFuncDef());
                wordInfo = lexer.peek();
            } else {
                lexer.stop();
                lexer.next();
                wordInfo = lexer.peek();
                if (wordInfo.getCategory() == Sign.MAINTK) {
                    lexer.reflow();
                    wordInfo = lexer.peek();
                    // 想要跳出该循环
                    break;
                } else if (wordInfo.getCategory() == Sign.IDENFR) {
                    lexer.reflow();
                    compUnit.addFuncDef(parseFuncDef());
                    wordInfo = lexer.peek();
                }
            }
        }
        if (wordInfo.getCategory() == Sign.INTTK) {
            compUnit.setMainFuncDef(parseMainFuncDef());
        }
        writeOutput("CompUnit");
        return compUnit;
    }

    private MainFuncDef parseMainFuncDef() {
        MainFuncDef mainFuncDef = new MainFuncDef();
        curSymbolTable.addSymbol("main", new FuncSymbol("main", 3, 0, new ArrayList<>()));
        curFuncType = Sign.INTTK;
        lexer.next();
        lexer.next();
        lexer.next();
        executeWrongJ();
        mainFuncDef.setBlock(parseBlock(null));
        ArrayList<BlockItem> blockItems = mainFuncDef.getBlock().getBlockItems();
        if (blockItems.isEmpty() || !(blockItems.get(blockItems.size() - 1) instanceof ReturnStmt)) {
            wrongInfos.add(new WrongInfo("g", lexer.getLastLine()));
        }
        writeOutput("MainFuncDef");
        return mainFuncDef;
    }


    private FuncDef parseFuncDef() {
        FuncDef funcDef = new FuncDef();
        funcDef.setFuncType(parseFuncType());
        curFuncType = funcDef.getFuncType().getSign();
        funcDef.setIdent(parseIdent());
        String name = funcDef.getIdent().getName();
        ArrayList<Integer> paramTypeList = new ArrayList<>();
        ArrayList<Symbol> paramSymbol = new ArrayList<>();
        lexer.next();
        WordInfo wordInfo = lexer.peek();
        if (wordInfo.getCategory() != Sign.RPARENT && wordInfo.getCategory() != Sign.LBRACE) {
            funcDef.setFuncFParams(parseFuncFParams());
        }
        FuncFParams funcFParams = funcDef.getFuncFParams();
        FuncSymbol funcSymbol = null;
        if (!isRepeatDecl(name)) {
            funcSymbol = new FuncSymbol(name, 3,
                    funcDef.getFuncType().getSign() == Sign.VOIDTK ? 1 : 0, paramTypeList);
            curSymbolTable.addSymbol(name, funcSymbol);
        } else {
            wrongInfos.add(new WrongInfo("b", funcDef.getIdent().getLine()));
        }
        // 函数有形参
        if (funcFParams != null) {
            ArrayList<FuncFParam> fParams = funcFParams.getFuncFParams();
            for (FuncFParam funcFParam : fParams) {
                if (funcFParam.getDim() == 0) {
                    // 传值
                    paramSymbol.add(new Symbol(funcFParam.getIdent(), 0, 0));
                    paramTypeList.add(0);
                } else if (funcFParam.getDim() == 1) {
                    // System.out.println("函数名: " + name + " 一维指针参数为：" + funcFParam.getIdent().getName());
                    // int a[], int a[][1] 传地址，作为指针类型
                    paramSymbol.add(new Dimension1PtrSymbol(funcFParam.getIdent(), 4, 0));
                    paramTypeList.add(4);
                } else {
                    // System.out.println("函数名: " + name + " 二维指针参数为：" + funcFParam.getIdent().getName());
                    paramSymbol.add(new Dimension2PtrSymbol(funcFParam.getIdent(), 5, 0));
                    paramTypeList.add(5);
                }
            }
        }
        if (funcSymbol != null) {
            funcSymbol.setParamNum(paramTypeList.size());
        }
        executeWrongJ();
        // 把函数的参数算到下一级的符号表中去
        funcDef.setBlock(parseBlock(paramSymbol));

        writeOutput("FuncDef");
        Sign funcType = funcDef.getFuncType().getSign();
        ArrayList<BlockItem> blockItems = funcDef.getBlock().getBlockItems();
        if (funcType == Sign.INTTK && (blockItems.isEmpty() || !(blockItems.get(blockItems.size() - 1) instanceof ReturnStmt))) {
            wrongInfos.add(new WrongInfo("g", lexer.getLastLine()));
        }
        return funcDef;
    }

    private FuncFParams parseFuncFParams() {
        FuncFParams funcFParams = new FuncFParams();
        funcFParams.addFuncFParam(parseFuncFParam());
        WordInfo wordInfo = lexer.peek();
        while (wordInfo.getCategory() == Sign.COMMA) {
            lexer.next();
            funcFParams.addFuncFParam(parseFuncFParam());
            wordInfo = lexer.peek();
        }
        writeOutput("FuncFParams");
        return funcFParams;
    }

    private FuncFParam parseFuncFParam() {
        FuncFParam funcFParam = new FuncFParam();
        funcFParam.setbType(parseBType());
        funcFParam.setIdent(parseIdent());
        WordInfo wordInfo = lexer.peek();
        if (wordInfo.getCategory() == Sign.LBRACK) {
            lexer.next();
            executeWrongK();
            funcFParam.addFirstDim();
            wordInfo = lexer.peek();
            while (wordInfo.getCategory() == Sign.LBRACK) {
                lexer.next();
                funcFParam.addConstExp(parseConstExp());
                executeWrongK();
                wordInfo = lexer.peek();
            }
        }
        writeOutput("FuncFParam");
        return funcFParam;
    }

    private Block parseBlock(ArrayList<Symbol> symbols) {
        // 开始一个作用域，即开始一张新的符号表！先把函数定义涉及的参数加入到新的符号表中
        curSymbolTable = new SymbolTable(curSymbolTable.getId());
        symbolTableHashMap.put(curSymbolTable.getId(), curSymbolTable);
        if (symbols != null) {
            for (Symbol symbol : symbols) {
                // 在不重复的定义的前提下加入
                if (!isRepeatDecl(symbol.getName())) {
                    curSymbolTable.addSymbol(symbol.getName(), symbol);
                } else {
                    wrongInfos.add(new WrongInfo("b", symbol.getLine()));
                }
            }
        }
        // 块中记录符号表Id
        Block block = new Block(curSymbolTable.getId());
        lexer.next();
        WordInfo wordInfo;
        for (wordInfo = lexer.peek(); wordInfo.getCategory() != Sign.RBRACE; wordInfo = lexer.peek()) {
            if (wordInfo.getCategory() == Sign.INTTK) {
                block.addBlockItems(parseVarDecl());
            } else if (wordInfo.getCategory() == Sign.CONSTTK) {
                block.addBlockItems(parseConstDecl());
            } else {
                //wordInfo.getCategory() == Sign.IFTK || wordInfo.getCategory() == Sign.FORTK
                //                    || wordInfo.getCategory() == Sign.BREAKTK || wordInfo.getCategory() == Sign.CONTINUETK
                //                    || wordInfo.getCategory() == Sign.RETURNTK || wordInfo.getCategory() == Sign.IDENFR
                //                    || wordInfo.getCategory() == Sign.PRINTFTK || wordInfo.getCategory() == Sign.LBRACE
                //                    || wordInfo.getCategory() == Sign.SEMICN || wordInfo.getCategory() == Sign.LPARENT
                //                    || wordInfo.getCategory() == Sign.INTCON || wordInfo.getCategory() == Sign.PLUS
                //                    || wordInfo.getCategory() == Sign.MINU || wordInfo.getCategory() == Sign.NOT
                block.addBlockItems(parseStmt());
            }
        }
        lexer.next();
        writeOutput("Block");
        // 将符号表回退至上一层级
        curSymbolTable = symbolTableHashMap.get(curSymbolTable.getFatherId());
        return block;
    }

    private Stmt parseStmt() {
        // <Exp>: {'(', Number, Ident, '+', '-', '!'}   <LVal>: {Ident}
        WordInfo wordInfo = lexer.peek();
        Stmt stmt;
        if (wordInfo.getCategory() == Sign.IFTK) {
            stmt = parseJudgeStmt();
        } else if (wordInfo.getCategory() == Sign.FORTK) {
            stmt = parseLoopStmt();
        } else if (wordInfo.getCategory() == Sign.BREAKTK || wordInfo.getCategory() == Sign.CONTINUETK) {
            stmt = parseBreakOrContinueStmt();
        } else if (wordInfo.getCategory() == Sign.PRINTFTK) {
            stmt = parseOutputStmt();
        } else if (wordInfo.getCategory() == Sign.RETURNTK) {
            stmt = parseReturnStmt();
        } else if (wordInfo.getCategory() == Sign.LBRACE) {
            stmt = parseBlockStmt();
        } else if (wordInfo.getCategory() == Sign.SEMICN) {
            stmt = parseExpStmt();
        } else {
            lexer.stop();
            // 试探解析
            parseExp();
            wordInfo = lexer.peek();
            // 错误处理时存在问题 下一个字符可能不是';'
            if (wordInfo.getCategory() != Sign.ASSIGN) {
                lexer.reflow();
                stmt = parseExpStmt();
            } else {
                lexer.next();
                wordInfo = lexer.peek();
                lexer.reflow();
                if (wordInfo.getCategory() == Sign.GETINTTK) {
                    stmt = parseInputStmt();
                } else {
                    stmt = parseAssignStmt();
                }
            }
        }
        writeOutput("Stmt");
        return stmt;
    }

    private Stmt parseExpStmt() {
        ExpStmt expStmt = new ExpStmt();
        if (lexer.peek().getCategory() != Sign.SEMICN) {
            expStmt.setExp(parseExp());
        }
        executeWrongI();
        return expStmt;
    }

    private Stmt parseInputStmt() {
        InputStmt inputStmt = new InputStmt();
        inputStmt.setlVal(parseLVal());
        // 先确保可以在符号表中找到该符号
        if (hasSameNameSymbol(inputStmt.getlVal().getIdent().getName()) &&
                getSymbol(inputStmt.getlVal().getIdent().getName()).getCon() == 1) {
            wrongInfos.add(new WrongInfo("h", inputStmt.getlVal().getIdent().getLine()));
        }
        lexer.next();
        lexer.next();
        lexer.next();
        executeWrongJ();
        executeWrongI();
        return inputStmt;
    }

    private Stmt parseAssignStmt() {
        AssignStmt assignStmt = new AssignStmt();
        assignStmt.setlVal(parseLVal());
        if (hasSameNameSymbol(assignStmt.getlVal().getIdent().getName()) &&
                getSymbol(assignStmt.getlVal().getIdent().getName()).getCon() == 1) {
            wrongInfos.add(new WrongInfo("h", assignStmt.getlVal().getIdent().getLine()));
        }
        lexer.next();
        assignStmt.setExp(parseExp());
        executeWrongI();
        return assignStmt;
    }

    private void executeWrongI() {
        if (lexer.peek().getCategory() == Sign.SEMICN) {
            lexer.next();
        } else {
            wrongInfos.add(new WrongInfo("i", lexer.getLastLine()));
        }
    }

    private void executeWrongJ() {
        if (lexer.peek().getCategory() == Sign.RPARENT) {
            lexer.next();
        } else {
            wrongInfos.add(new WrongInfo("j", lexer.getLastLine()));
        }
    }

    private void executeWrongK() {
        if (lexer.peek().getCategory() == Sign.RBRACK) {
            lexer.next();
        } else {
            wrongInfos.add(new WrongInfo("k", lexer.getLastLine()));
        }
    }

    private Stmt parseBlockStmt() {
        BlockStmt blockStmt = new BlockStmt();
        blockStmt.setBlock(parseBlock(null));
        return blockStmt;
    }

    private Stmt parseReturnStmt() {
        ReturnStmt returnStmt = new ReturnStmt();
        returnStmt.setLine(lexer.peek().getLine());
        lexer.next();
        WordInfo wordInfo = lexer.peek();
        if (wordInfo.getCategory() != Sign.SEMICN) {
            returnStmt.setExp(parseExp());
            if (curFuncType == Sign.VOIDTK) {
                wrongInfos.add(new WrongInfo("f", returnStmt.getLine()));
            }
        }
        executeWrongI();
        return returnStmt;
    }

    private Stmt parseOutputStmt() {
        OutputStmt outputStmt = new OutputStmt();
        int printfLine = lexer.peek().getLine();
        lexer.next();
        lexer.next();
        outputStmt.setString(lexer.peek().getWord());
        int illegal = lexer.peek().getValue() < 0 ? 1 : 0;
        int placeHolderNum = lexer.peek().getValue() < 0 ? -(lexer.peek().getValue() + 1) : lexer.peek().getValue();
        // 两个错误都要判断
        if (illegal == 1) {
            wrongInfos.add(new WrongInfo("a", lexer.peek().getLine()));
        }
        lexer.next();
        WordInfo wordInfo = lexer.peek();
        while (wordInfo.getCategory() == Sign.COMMA) {
            lexer.next();
            outputStmt.addExp(parseExp());
            wordInfo = lexer.peek();
        }
        if (outputStmt.getExpNum() != placeHolderNum) {
            wrongInfos.add(new WrongInfo("l", printfLine));
        }
        executeWrongJ();
        executeWrongI();
        return outputStmt;
    }

    private Stmt parseBreakOrContinueStmt() {
        if (loopStmts.empty()) {
            wrongInfos.add(new WrongInfo("m", lexer.peek().getLine()));
        }
        BreakOrContinueStmt breakOrContinueStmt = new BreakOrContinueStmt();
        breakOrContinueStmt.setLoopStmt(loopStmts.peek());
        breakOrContinueStmt.setSign(lexer.peek().getCategory());
        lexer.next();
        executeWrongI();
        return breakOrContinueStmt;
    }

    private Stmt parseLoopStmt() {
        LoopStmt loopStmt = new LoopStmt();
        loopStmts.add(loopStmt);
        lexer.next();
        lexer.next();
        WordInfo wordInfo = lexer.peek();
        if (wordInfo.getCategory() == Sign.SEMICN) {
            lexer.next();
        } else {
            loopStmt.setInitForStmt(parseForStmt());
            lexer.next();
        }
        wordInfo = lexer.peek();
        if (wordInfo.getCategory() == Sign.SEMICN) {
            lexer.next();
        } else {
            loopStmt.setCond(parseCond());
            lexer.next();
        }
        wordInfo = lexer.peek();
        if (wordInfo.getCategory() == Sign.RPARENT) {
            lexer.next();
        } else {
            loopStmt.setForStmt(parseForStmt());
            lexer.next();
        }
        loopStmt.setStmt(parseStmt());
        loopStmts.pop();
        return loopStmt;
    }

    private ForStmt parseForStmt() {
        ForStmt forStmt = new ForStmt();
        forStmt.setlVal(parseLVal());
        if (hasSameNameSymbol(forStmt.getlVal().getIdent().getName()) &&
                getSymbol(forStmt.getlVal().getIdent().getName()).getCon() == 1) {
            wrongInfos.add(new WrongInfo("h", forStmt.getlVal().getIdent().getLine()));
        }
        lexer.next();
        forStmt.setExp(parseExp());
        writeOutput("ForStmt");
        return forStmt;
    }

    private Stmt parseJudgeStmt() {
        JudgeStmt judgeStmt = new JudgeStmt();
        lexer.next();
        lexer.next();
        judgeStmt.setCond(parseCond());
        executeWrongJ();
        judgeStmt.setStmt(parseStmt());
        if (lexer.peek().getCategory() == Sign.ELSETK) {
            lexer.next();
            judgeStmt.setElseStmt(parseStmt());
        }
        return judgeStmt;
    }

    private Cond parseCond() {
        Cond cond = new Cond();
        cond.setlOrExp(parseLOrExp());
        writeOutput("Cond");
        return cond;
    }

    private LOrExp parseLOrExp() {
        LOrExp lOrExp = new LOrExp();
        lOrExp.addLAndExp(parseLAndExp());
        writeOutput("LOrExp");
        WordInfo wordInfo = lexer.peek();
        while (wordInfo.getCategory() == Sign.OR) {
            lexer.next();
            lOrExp.addLAndExp(parseLAndExp());
            writeOutput("LOrExp");
            wordInfo = lexer.peek();
        }
        return lOrExp;
    }

    private LAndExp parseLAndExp() {
        LAndExp lAndExp = new LAndExp();
        lAndExp.addEqExp(parseEqExp());
        writeOutput("LAndExp");
        WordInfo wordInfo = lexer.peek();
        while (wordInfo.getCategory() == Sign.AND) {
            lexer.next();
            lAndExp.addEqExp(parseEqExp());
            writeOutput("LAndExp");
            wordInfo = lexer.peek();
        }
        return lAndExp;
    }

    private EqExp parseEqExp() {
        EqExp eqExp = new EqExp();
        eqExp.addRelExp(parseRelExp());
        writeOutput("EqExp");
        WordInfo wordInfo = lexer.peek();
        while (wordInfo.getCategory() == Sign.EQL || wordInfo.getCategory() == Sign.NEQ) {
            lexer.next();
            eqExp.addRelAndSign(parseRelExp(), wordInfo.getCategory());
            writeOutput("EqExp");
            wordInfo = lexer.peek();
        }
        return eqExp;
    }

    private RelExp parseRelExp() {
        RelExp relExp = new RelExp();
        relExp.addAddExp(parseAddExp());
        writeOutput("RelExp");
        WordInfo wordInfo = lexer.peek();
        while (wordInfo.getCategory() == Sign.GRE || wordInfo.getCategory() == Sign.GEQ || wordInfo.getCategory() == Sign.LEQ || wordInfo.getCategory() == Sign.LSS) {
            lexer.next();
            relExp.addAddExpAndSign(parseAddExp(), wordInfo.getCategory());
            writeOutput("RelExp");
            wordInfo = lexer.peek();
        }
        return relExp;
    }

    private FuncType parseFuncType() {
        FuncType funcType = new FuncType();
        funcType.setSign(lexer.peek().getCategory());
        lexer.next();
        writeOutput("FuncType");
        return funcType;
    }

    private Decl parseVarDecl() {
        // Flag=1代表全局
        VarDecl varDecl = new VarDecl();
        varDecl.setbType(parseBType());
        varDecl.addVarDef(parseVarDef());
        WordInfo wordInfo = lexer.peek();
        while (wordInfo.getCategory() == Sign.COMMA) {
            lexer.next();
            varDecl.addVarDef(parseVarDef());
            wordInfo = lexer.peek();
        }
        executeWrongI();
        writeOutput("VarDecl");
        return varDecl;
    }

    private VarDef parseVarDef() {
        VarDef varDef = new VarDef();
        varDef.setIdent(parseIdent());
        WordInfo wordInfo = lexer.peek();
        while (wordInfo.getCategory() == Sign.LBRACK) {
            lexer.next();
            varDef.addConstExp(parseConstExp());
            executeWrongK();
            wordInfo = lexer.peek();
        }
        if (wordInfo.getCategory() == Sign.ASSIGN) {
            lexer.next();
            varDef.setInitVal(parseInitVal());
        }
        writeOutput("VarDef");
        if (!isRepeatDecl(varDef.getIdent().getName())) {
            if (varDef.getArrDim() == 0) {
                curSymbolTable.addSymbol(varDef.getIdent().getName(),
                        new Symbol(varDef.getIdent(), varDef.getArrDim(), 0));
            } else if (varDef.getArrDim() == 1) {
                curSymbolTable.addSymbol(varDef.getIdent().getName(),
                        new Dimension1ArraySymbol(varDef.getIdent(), varDef.getArrDim(), 0));
            } else {
                curSymbolTable.addSymbol(varDef.getIdent().getName(),
                        new Dimension2ArraySymbol(varDef.getIdent(), varDef.getArrDim(), 0));
            }
        } else {
            wrongInfos.add(new WrongInfo("b", varDef.getIdent().getLine()));
        }
        return varDef;
    }

    private InitVal parseInitVal() {
        InitVal initVal = new InitVal();
        WordInfo wordInfo = lexer.peek();
        if (wordInfo.getCategory() == Sign.LBRACE) {
            lexer.next();
            wordInfo = lexer.peek();
            if (wordInfo.getCategory() == Sign.RBRACE) {
                lexer.next();
            } else {
                initVal.addInitVal(parseInitVal());
                wordInfo = lexer.peek();
                while (wordInfo.getCategory() == Sign.COMMA) {
                    lexer.next();
                    initVal.addInitVal(parseInitVal());
                    wordInfo = lexer.peek();
                }
                // 读出最后的'}'
                lexer.next();
            }
        } else {
            initVal.setExp(parseExp());
        }
        writeOutput("InitVal");
        return initVal;
    }

    private Decl parseConstDecl() {
        ConstDecl constDecl = new ConstDecl();
        // 读走const
        lexer.next();
        constDecl.setBType(parseBType());
        constDecl.addConstDefs(parseConstDef());
        WordInfo wordInfo = lexer.peek();

        while (wordInfo.getCategory() == Sign.COMMA) {
            lexer.next();
            constDecl.addConstDefs(parseConstDef());
            wordInfo = lexer.peek();
        }
        executeWrongI();
        writeOutput("ConstDecl");
        return constDecl;
    }

    private ConstDef parseConstDef() {
        ConstDef constDef = new ConstDef();
        constDef.setIdent(parseIdent());
        WordInfo wordInfo = lexer.peek();
        while (wordInfo.getCategory() == Sign.LBRACK) {
            lexer.next();
            constDef.addConstExp(parseConstExp());
            executeWrongK();
            wordInfo = lexer.peek();
        }
        // 读走‘=’
        lexer.next();
        constDef.setConstInitVal(parseConstInitVal());
        writeOutput("ConstDef");
        if (!isRepeatDecl(constDef.getIdent().getName())) {
            if (constDef.getArrDim() == 0) {
                curSymbolTable.addSymbol(constDef.getIdent().getName(), new Symbol(constDef.getIdent(), constDef.getArrDim(), 1));
            } else if (constDef.getArrDim() == 1) {
                curSymbolTable.addSymbol(constDef.getIdent().getName(), new Dimension1ArraySymbol(constDef.getIdent(), constDef.getArrDim(), 1));
            } else {
                curSymbolTable.addSymbol(constDef.getIdent().getName(), new Dimension2ArraySymbol(constDef.getIdent(), constDef.getArrDim(), 1));
            }
        } else {
            wrongInfos.add(new WrongInfo("b", constDef.getIdent().getLine()));
        }
        return constDef;
    }

    private ConstInitVal parseConstInitVal() {
        ConstInitVal constInitVal = new ConstInitVal();
        WordInfo wordInfo = lexer.peek();
        if (wordInfo.getCategory() == Sign.LBRACE) {
            lexer.next();
            wordInfo = lexer.peek();
            if (wordInfo.getCategory() == Sign.RBRACE) {
                lexer.next();
            } else {
                constInitVal.addConstInitVal(parseConstInitVal());
                wordInfo = lexer.peek();
                while (wordInfo.getCategory() == Sign.COMMA) {
                    lexer.next();
                    constInitVal.addConstInitVal(parseConstInitVal());
                    wordInfo = lexer.peek();
                }
                // 读出最后的'}'
                lexer.next();
            }
        } else {
            constInitVal.setConstExp(parseConstExp());
        }
        writeOutput("ConstInitVal");
        return constInitVal;
    }

    private ConstExp parseConstExp() {
        ConstExp constExp = new ConstExp();
        constExp.setAddExp(parseAddExp());
        writeOutput("ConstExp");
        return constExp;
    }

    private AddExp parseAddExp() {
        AddExp addExp = new AddExp();
        addExp.addMulExp(parseMulExp());
        writeOutput("AddExp");
        WordInfo wordInfo = lexer.peek();
        while (wordInfo.getCategory() == Sign.PLUS || wordInfo.getCategory() == Sign.MINU) {
            lexer.next();
            addExp.addMulAndSign(parseMulExp(), wordInfo.getCategory());
            writeOutput("AddExp");
            wordInfo = lexer.peek();
        }
        return addExp;
    }

    private MulExp parseMulExp() {
        MulExp mulExp = new MulExp();
        mulExp.addUnaryExp(parseUnaryExp());
        writeOutput("MulExp");
        WordInfo wordInfo = lexer.peek();
        while (wordInfo.getCategory() == Sign.MULT || wordInfo.getCategory() == Sign.DIV || wordInfo.getCategory() == Sign.MOD) {
            lexer.next();
            mulExp.addUnaryAndSign(parseUnaryExp(), wordInfo.getCategory());
            writeOutput("MulExp");
            wordInfo = lexer.peek();
        }
        return mulExp;
    }

    private int getExpType(Exp exp) {
        /*
            1.
            前提：传高维数组不涉及基本运算
            递归解析 一旦出现运算符号 {+， -， *， /， %} 一定是0维
            只有一种情况可能是1维或者2维
            Exp -> AddExp -> MulExp -> UnaryExp -> PrimaryExp -> LVal a or a[] or a[][]
                                                              -> (Exp) -> ... ...
            归根到底 有唯一左值时才会存在高维可能

            2.
            判断函数返回值是否为空值(void)
            递归遍历Exp树，将所有函数名取出，查符号表一一比较，若至少存在一个void，则该参数类型不匹配，返回-1
         */
        if (exp.hasSingleLVal()) {
            LVal lVal = exp.getSingleLVal();
            if (hasSameNameSymbol(lVal.getIdent().getName())) {
                // 可以保证变量值存在
                Symbol symbol = getSymbol(lVal.getIdent().getName());
                // 符号表中维度与左值维度相减，即得到最终传参维度
                return symbol.getType() - lVal.getLValType();
            }
        } else {
            // 前提保证函数名存在
            ArrayList<Ident> funcIdents = exp.getAllFuncIdent();
            for (Ident ident : funcIdents) {
                if (hasSameNameSymbol(ident.getName()) &&
                        getSymbol(ident.getName()) instanceof FuncSymbol && ((FuncSymbol) getSymbol(ident.getName())).getRetype() == 1) {
                    return -1;
                } else if (!hasSameNameSymbol(ident.getName())) {
                    return -2;
                }
            }
            return 0;
        }
        return -2;
    }

    private UnaryExp parseUnaryExp() {
        UnaryExp unaryExp = new UnaryExp();
        WordInfo wordInfo = lexer.peek();
        if (wordInfo.getCategory() == Sign.IDENFR) {
            lexer.stop();
            lexer.next();
            wordInfo = lexer.peek();
            if (wordInfo.getCategory() == Sign.LPARENT) {
                /* 预读下一单词是 ) 还是 其他
                    考虑到错误处理 还存在第三种情况 不存在) 需要判断下一个是否是FuncRParams 存在歧义
                 */
                lexer.next();
                wordInfo = lexer.peek();
                lexer.reflow();
                unaryExp.setFlag(1);
                unaryExp.setIdent_1(parseIdent());
                // 若右小括号缺失 则出现多种情况
                if (wordInfo.getCategory() == Sign.LPARENT || wordInfo.getCategory() == Sign.INTCON ||
                        wordInfo.getCategory() == Sign.IDENFR || wordInfo.getCategory() == Sign.PLUS ||
                        wordInfo.getCategory() == Sign.MINU) {
                    lexer.next();
                    unaryExp.setFuncFParams_1(parseFuncRParams());
                } else {
                    lexer.next();
                }
                executeWrongJ();
                FuncRParams funcRParams = unaryExp.getFuncFParams_1();
                if (!hasSameNameSymbol(unaryExp.getIdent_1().getName()) || getSymbol(unaryExp.getIdent_1().getName()).getType() != 3) {
                    wrongInfos.add(new WrongInfo("c", unaryExp.getIdent_1().getLine()));
                } else {
                    Symbol symbol = getSymbol(unaryExp.getIdent_1().getName());
                    // 为函数定义才合理 检查参数数量是否相同
                    if ((funcRParams == null && ((FuncSymbol) symbol).getParamNum() != 0) ||
                            (funcRParams != null && ((FuncSymbol) symbol).getParamNum() != funcRParams.getParamsNum())) {
                        wrongInfos.add(new WrongInfo("d", unaryExp.getIdent_1().getLine()));
                    } else {
                        // 检查实参类型是否相同
                        ArrayList<Integer> fTypeList = ((FuncSymbol) symbol).getParamTypeList();
                        ArrayList<Integer> rTypeList = new ArrayList<>();
                        if (funcRParams != null) {
                            for (Exp exp : funcRParams.getExps()) {
                                rTypeList.add(getExpType(exp));
                            }
                        }
                        for (int i = 0; i < fTypeList.size(); i++) {
                            // 判断每个实参的类型 不判断未定义的实参
                            if (rTypeList.get(i) != -2 && ((rTypeList.get(i) == 0 && fTypeList.get(i) !=0)
                                    || ((rTypeList.get(i) == 1 || rTypeList.get(i) == 4) && fTypeList.get(i) != 4)
                                    || ((rTypeList.get(i) == 2 || rTypeList.get(i) == 5) && fTypeList.get(i) != 5))) {
                                // System.out.println("形参类型: " + fTypeList.get(i) + ", 实参类型: " + rTypeList.get(i));
                                wrongInfos.add(new WrongInfo("e", unaryExp.getIdent_1().getLine()));
                            }
                        }
                    }
                }
            } else {
                lexer.reflow();
                unaryExp.setFlag(0);
                unaryExp.setPrimaryExp_0(parsePrimaryExp());
            }
        } else if (wordInfo.getCategory() == Sign.LPARENT || wordInfo.getCategory() == Sign.INTCON) {
            unaryExp.setFlag(0);
            unaryExp.setPrimaryExp_0(parsePrimaryExp());
        } else if (wordInfo.getCategory() == Sign.PLUS || wordInfo.getCategory() == Sign.MINU || wordInfo.getCategory() == Sign.NOT) {
            unaryExp.setFlag(2);
            unaryExp.setUnaryOp_2(parseUnaryOp());
            unaryExp.setUnaryExp_2(parseUnaryExp());
        }
        writeOutput("UnaryExp");
        return unaryExp;
    }

    private UnaryOp parseUnaryOp() {
        UnaryOp unaryOp = new UnaryOp();
        unaryOp.setSign(lexer.peek().getCategory());
        lexer.next();
        writeOutput("UnaryOp");
        return unaryOp;
    }

    private PrimaryExp parsePrimaryExp() {
        PrimaryExp primaryExp = new PrimaryExp();
        WordInfo wordInfo = lexer.peek();
        if (wordInfo.getCategory() == Sign.LPARENT) {
            lexer.next();
            primaryExp.setFlag(0);
            primaryExp.setExp_0(parseExp());
            lexer.next();
        } else if (wordInfo.getCategory() == Sign.INTCON) {
            primaryExp.setFlag(2);
            primaryExp.setNumber_2(parseNumber());
        } else if (wordInfo.getCategory() == Sign.IDENFR) {
            primaryExp.setFlag(1);
            primaryExp.setlVal_1(parseLVal());
        }
        writeOutput("PrimaryExp");
        return primaryExp;
    }

    private Number parseNumber() {
        Number number = new Number();
        number.setNum(lexer.peek().getValue());
        lexer.next();
        writeOutput("Number");
        return number;
    }

    private LVal parseLVal() {
        LVal lVal = new LVal();
        lVal.setIdent(parseIdent());
        // 符号表中找不到符号 或 符号表找到的符号与该符号类型不匹配 即看作名字未定义
        if (!hasSameNameSymbol(lVal.getIdent().getName()) || getSymbol(lVal.getIdent().getName()).getType() == 3) {
            wrongInfos.add(new WrongInfo("c", lVal.getIdent().getLine()));
        }
        WordInfo wordInfo = lexer.peek();
        while (wordInfo.getCategory() == Sign.LBRACK) {
            lexer.next();
            lVal.addExp(parseExp());
            executeWrongK();
            wordInfo = lexer.peek();
        }
        writeOutput("LVal");
        return lVal;
    }

    private Exp parseExp() {
        Exp exp = new Exp();
        exp.setAddExp(parseAddExp());
        writeOutput("Exp");
        return exp;
    }

    private FuncRParams parseFuncRParams() {
        FuncRParams funcRParams = new FuncRParams();
        funcRParams.addExps(parseExp());
        WordInfo wordInfo = lexer.peek();
        while (wordInfo.getCategory() == Sign.COMMA) {
            lexer.next();
            funcRParams.addExps(parseExp());
            wordInfo = lexer.peek();
        }
        writeOutput("FuncRParams");
        return funcRParams;
    }

    private Ident parseIdent() {
        Ident ident = new Ident();
        ident.setName(lexer.peek().getWord());
        ident.setLine(lexer.peek().getLine());
        lexer.next();
        return ident;
    }

    private BType parseBType() {
        BType bType = new BType();
        bType.setSign(lexer.peek().getCategory());
        lexer.next();
        return bType;
    }

    public void printWrongInfo() throws IOException {
        // 行数小到大排序
        wrongInfos.sort(Comparator.comparingInt(WrongInfo::getWrongLine));
        for (WrongInfo wrongInfo : wrongInfos) {
            if (wrongInfo.getFlag() == 0) {
                Files.writeString(errorFilePath, wrongInfo.getWrongLine() + " " + wrongInfo.getWrongCode() + "\n", StandardOpenOption.APPEND);
            }
        }
    }
}