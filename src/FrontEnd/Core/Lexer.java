package FrontEnd.Core;

import FrontEnd.Info.WordInfo;
import FrontEnd.NonTerminal.Sign;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Lexer {
    public static Path readFilePath = Path.of("testfile.txt");
    public static Path writeFilePath = Path.of("output.txt");
    public static Path errorFilePath = Path.of("error.txt");
    private final HashMap<String, Sign> CATEGORY_MAP = new HashMap<>();
    private final String[] KEY = {"main", "const", "int", "break", "continue", "if", "else", "for", "getint", "printf", "return", "void"};
    private final String[] SYM = {"!", "&&", "||", "+", "-", "*", "/", "%", "<", "<=", ">", ">=",
            "==", "!=", "=", ";", ",", "(", ")", "[", "]", "{", "}"};
    private final ArrayList<String> KEYWORDS = new ArrayList<>(Arrays.asList(KEY));
    private final ArrayList<String> SYMBOLS = new ArrayList<>(Arrays.asList(SYM));
    private final ArrayList<String> output = new ArrayList<>();

    public void genCategoryCode() {
        /*
        根据单词编码与类别码的关系建立哈希表用于转换
        前三个不是某个固定单词 是常变量
        后面是关键字 or 字符
         */
        CATEGORY_MAP.put("FrontEnd.NonTerminal.Ident", Sign.IDENFR);
        CATEGORY_MAP.put("IntConst", Sign.INTCON);
        CATEGORY_MAP.put("FormatString", Sign.STRCON);
        CATEGORY_MAP.put("main", Sign.MAINTK);
        CATEGORY_MAP.put("const", Sign.CONSTTK);
        CATEGORY_MAP.put("int", Sign.INTTK);
        CATEGORY_MAP.put("break", Sign.BREAKTK);
        CATEGORY_MAP.put("continue", Sign.CONTINUETK);
        CATEGORY_MAP.put("if", Sign.IFTK);
        CATEGORY_MAP.put("else", Sign.ELSETK);
        CATEGORY_MAP.put("!", Sign.NOT);
        CATEGORY_MAP.put("&&", Sign.AND);
        CATEGORY_MAP.put("||", Sign.OR);
        CATEGORY_MAP.put("for", Sign.FORTK);
        CATEGORY_MAP.put("getint", Sign.GETINTTK);
        CATEGORY_MAP.put("printf", Sign.PRINTFTK);
        CATEGORY_MAP.put("return", Sign.RETURNTK);
        CATEGORY_MAP.put("+", Sign.PLUS);
        CATEGORY_MAP.put("-", Sign.MINU);
        CATEGORY_MAP.put("void", Sign.VOIDTK);
        CATEGORY_MAP.put("*", Sign.MULT);
        CATEGORY_MAP.put("/", Sign.DIV);
        CATEGORY_MAP.put("%", Sign.MOD);
        CATEGORY_MAP.put("<", Sign.LSS);
        CATEGORY_MAP.put("<=", Sign.LEQ);
        CATEGORY_MAP.put(">", Sign.GRE);
        CATEGORY_MAP.put(">=", Sign.GEQ);
        CATEGORY_MAP.put("==", Sign.EQL);
        CATEGORY_MAP.put("!=", Sign.NEQ);
        CATEGORY_MAP.put("=", Sign.ASSIGN);
        CATEGORY_MAP.put(";", Sign.SEMICN);
        CATEGORY_MAP.put(",", Sign.COMMA);
        CATEGORY_MAP.put("(", Sign.LPARENT);
        CATEGORY_MAP.put(")", Sign.RPARENT);
        CATEGORY_MAP.put("[", Sign.LBRACK);
        CATEGORY_MAP.put("]", Sign.RBRACK);
        CATEGORY_MAP.put("{", Sign.LBRACE);
        CATEGORY_MAP.put("}", Sign.RBRACE);
    }

    private String input;
    private int pos = 0;
    private int line = 1;
    private int lastLine = 0;
    private int tmpPos = 0;
    private int tmpLine = 1;
    private int tmpTmpPos = 0;
    private int tmpTmpLine = 1;
    private static int flag;
    private WordInfo curWordInfo;
    private WordInfo tmpWordInfo;
    private WordInfo tmpTmpWordInfo;

    public Lexer() throws IOException {
        flag = 0;
        genCategoryCode();
        input = Files.readString(readFilePath);
        Files.writeString(writeFilePath, "");
        Files.writeString(errorFilePath, "");
    }

    public void stop() {
        if (flag == 0) {
            tmpLine = line;
            tmpPos = pos;
            tmpWordInfo = curWordInfo;
            flag++;
        } else if (flag == 1) {
            tmpTmpPos = pos;
            tmpTmpLine = line;
            tmpTmpWordInfo = curWordInfo;
            flag++;
        } else {
            // System.out.println("出现问题");
        }
//        // System.out.println("stop flag: " + flag);
    }

    public void addOutput(String s) {
        if (s.charAt(0) == '<') {
            String string = output.get(output.size() - 1);
            if (string.charAt(0) == '<') {
                output.add(s);
            } else if (pos == input.length() && (s.contains("Block") || s.contains("MainFuncDef") || s.contains("CompUnit"))) {
                output.add(s);
            } else {
                output.add(output.size() - 1, s);
            }
        } else {
            output.add(s);
        }
    }

    public void reflow() {
        if (flag == 1) {
            pos = tmpPos;
            line = tmpLine;
            curWordInfo = tmpWordInfo;
            flag--;
        } else if (flag == 2) {
            pos = tmpTmpPos;
            line = tmpTmpLine;
            curWordInfo = tmpTmpWordInfo;
            flag--;
        } else {
            // System.out.println("出现问题");
        }
//        // System.out.println("reflow flag： " + flag);
    }

    private String getNumber() { //消去前导0
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            pos++;
        }
        return sb.toString();
    }

    public int getLastLine() {
        return lastLine;
    }

    private String getIdentOrKeyword() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && isIdentifier(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            pos++;
        }
        return sb.toString();
    }

    // 输出字符串的组成部分包括 前后由”包裹 中间只出现 空格 ! 40-126 不会出现引号 转义字符只有\n
    private void getFormatString() {
        StringBuilder sb = new StringBuilder();
        int value = 0;
        int flag = 0;
        sb.append('"');
        pos++;
        while (pos < input.length() && input.charAt(pos) != '"') {
            if ((input.charAt(pos) == '%' && input.charAt(pos + 1) == 'd')) {
                value++;
            } else if (input.charAt(pos) == '\\' && input.charAt(pos + 1) == 'n') {
                // System.out.println("将\\n情况提前判断出来,接下来进行\\单独出现情况的特殊处理");
            } else if (input.charAt(pos) != 32 && input.charAt(pos) != 33 &&
                    (input.charAt(pos) < 40 || input.charAt(pos) > 126 || input.charAt(pos) == 92)) {
                /* 即只允许出现' ', '!', 40到126之间的字符
                    特殊情况：'\' ascii = 92，只允许和n搭配出现
                 */
                flag = 1;
            }
            sb.append(input.charAt(pos));
            pos++;
        }
        // System.out.println("lexer: " + value);
        sb.append('"');
        pos++;
        if (flag == 1) {
            value = -value - 1;
        }
        this.curWordInfo = new WordInfo(sb.toString(), Sign.STRCON, value, line);
    }

    // identifier-nondigit 包括大小写字母和下划线 ; identifier 开头必须是 nondigit 后续可跟数字
    private boolean isIdentifierNondigit(char s) {
        return (s >= 65 && s <= 90) || (s >= 97 && s <= 122) || s == '_';
    }

    private boolean isIdentifier(char s) {
        return (s >= 65 && s <= 90) || (s >= 97 && s <= 122) || s == '_' || (s >= '0' && s <= '9');
    }

    // 是否清空
    public void next() {
        // 记录前两个词的位置
        /*
         认为在一个token之内不存在空格or换行
         出现\n就line++ ; 将 空格 和 可能出现的(???) \t 和 \r 全部读走
        */
        lastLine = line;
        while (pos < input.length() && (input.charAt(pos) == ' ' || input.charAt(pos) == '\n' || input.charAt(pos) == '\r' ||
                input.charAt(pos) == '\t')) {
            if (input.charAt(pos) == '\n') {
                line++;
            }
            pos++;
        }
        if (pos == input.length()) {
            return;
        }
        // 正式开始解析
        char curChar = input.charAt(pos);
        // // System.out.println((int)curChar);
        if (isIdentifierNondigit(curChar)) {
            // 是一个标识符或者关键字 值等于0
            String word = getIdentOrKeyword();
            if (KEYWORDS.contains(word)) {
                this.curWordInfo = new WordInfo(word, CATEGORY_MAP.get(word), 0, line);
            } else {
                this.curWordInfo = new WordInfo(word, Sign.IDENFR, 0, line);
            }
        } else if (Character.isDigit(curChar)) {
            // 是一个数字
            String num = getNumber();
            this.curWordInfo = new WordInfo(num, Sign.INTCON, Integer.parseInt(num), line);
        } else if (curChar == '"') {
            // 是一个字符串
            getFormatString();
        } else {
            // 剩下的是各种字符
            /* "!", "&&", "||", "+", "-", "*", "/", "%", "<", "<=", ">", ">=",
                    "==", "!=", "=", ";", ",", "(", ")", "[", "]", "{", "}" */
            // 其中 + - * / ( ) [ ] { } % ; , 是无歧义且单字符的
            String symbol;
            if (curChar == '+' || curChar == '-' || curChar == '*' || curChar == '/' || curChar == '%' || curChar == ';'
                    || curChar == ',' || curChar == '(' || curChar == ')' || curChar == '[' || curChar == ']' || curChar == '{'
                    || curChar == '}') {
                pos++;
                symbol = String.valueOf(curChar);
                this.curWordInfo = new WordInfo(symbol, CATEGORY_MAP.get(symbol), 0, line);
            } else if (curChar == '&' || curChar == '|') {
                pos++;
                if (pos < input.length() && curChar == input.charAt(pos)) {
                    symbol = curChar + String.valueOf(curChar);
                    pos++;
                    this.curWordInfo = new WordInfo(symbol, CATEGORY_MAP.get(symbol), 0, line);
                } else {
                    // System.out.println("错误接口");
                }
            } else if (curChar == '!' || curChar == '>' || curChar == '<' || curChar == '=') {
                // 有可能需要错误处理
                if (pos < input.length() - 1 && input.charAt(pos + 1) == '=') {
                    pos += 2;
                    symbol = curChar + "=";
                    this.curWordInfo = new WordInfo(symbol, CATEGORY_MAP.get(symbol), 0, line);
                } else {
                    pos++;
                    symbol = String.valueOf(curChar);
                    this.curWordInfo = new WordInfo(symbol, CATEGORY_MAP.get(symbol), 0, line);
                }
            } else {
                pos++;
                // System.out.println("出现无法识别的字符，错误处理");
            }
        }
        if (flag == 0) {
            // 说明没有试探预读
            String output1 = curWordInfo.getCategory() + " " + curWordInfo.getWord() + "\n";
            addOutput(output1);
        }
    }

    public WordInfo peek() {
        return this.curWordInfo;
    }

    public void clearAnnotation() {
        StringBuilder stringBuilder = new StringBuilder();
        int len = this.input.length();
        char curChar, nextChar;
        int i;
        for (i = 0; i < len - 1; i++) {
            curChar = this.input.charAt(i);
            nextChar = this.input.charAt(i + 1);
            if (curChar == '"') {
                stringBuilder.append(curChar);
                i++;
                curChar = this.input.charAt(i);
                while (curChar != '"') {
                    stringBuilder.append(curChar);
                    i++;
                    curChar = this.input.charAt(i);
                }
                stringBuilder.append(curChar);
            } else if (curChar != '/') {
                stringBuilder.append(curChar);
            } else if (nextChar == '*') {
                i = i + 2;
                while (i < len - 1) {
                    if (this.input.charAt(i) == '*' && this.input.charAt(i + 1) == '/') {
                        i++;
                        break;
                    } else if (this.input.charAt(i) == '\n') {
                        i++;
                        stringBuilder.append('\n');
                    } else {
                        i++;
                    }
                }
            } else if (nextChar == '/') {
                i = i + 2;
                while (i < len) {
                    // 当读到 \n 或 文件结尾时停止
                    if (this.input.charAt(i) == '\n') {
                        stringBuilder.append('\n');
                        break;
                    } else if (i == len - 1) {
                        break;
                    } else {
                        i++;
                    }
                }
            } else {
                // 此时 / 是本意 不是注释
                stringBuilder.append(curChar);
            }
        }
        if (i == len - 1) {
            stringBuilder.append(this.input.charAt(len - 1));
        }
        this.input = stringBuilder.toString();
    }

    public static int getFlag() {
        return flag;
    }

    public void printOutput() throws IOException {
        for (String s : output) {
            // 语法词法输出流开启or关闭
            Files.writeString(writeFilePath, s, StandardOpenOption.APPEND);
        }
    }

    public void exePlusAndSub() {
        StringBuilder sb = new StringBuilder();
        int len = input.length();
        int flag = 0;
        for (int i = 0; i < len; i++) {
            if (input.charAt(i) == '+' || input.charAt(i) == '-') {
                while (input.charAt(i) == '+' || input.charAt(i) == '-') {
                    if (input.charAt(i) == '-') {
                        flag++;
                    }
                    i++;
                }
                i--;
                if (flag % 2 == 1) {
                    sb.append("-");
                } else {
                    sb.append("+");
                }
                flag = 0;
            } else {
                sb.append(input.charAt(i));
            }
        }
        input = sb.toString();
    }
}