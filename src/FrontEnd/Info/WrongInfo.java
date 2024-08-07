package FrontEnd.Info;

import FrontEnd.Core.Lexer;

import java.util.HashMap;

public class WrongInfo {
    private String wrongCode;
    private String wrongMes;
    private int wrongLine;
    private int flag; // 记录该条错误信息是预读时产生的虚拟错误信息还是真实错误信息
    static HashMap<String, String> wrongHash = new HashMap<>() {
        {
            put("a", "非法符号");
            put("b", "名字重定义");
            put("c", "名字未定义");
            put("d", "函数参数个数不匹配");
            put("e", "函数参数类型不匹配");
            put("f", "无返回值函数返回数值");
            put("g", "有返回值函数不返回");
            put("h", "常量值改变");
            put("i", "缺少分号");
            put("j", "缺少右小括号");
            put("k", "缺少右中括号");
            put("l", "print格式字符个数不匹配");
            put("m", "非循环体使用continue/break");
        }
    };

    public WrongInfo(String wrongCode, int wrongLine) {
        this.wrongCode = wrongCode;
        this.wrongLine = wrongLine;
        this.wrongMes = wrongHash.get(wrongCode);
        this.flag = Lexer.getFlag();
    }

    public int getFlag() {
        return flag;
    }

    public String getWrongCode() {
        return wrongCode;
    }

    public String getWrongMes() {
        return wrongMes;
    }

    public int getWrongLine() {
        return wrongLine;
    }
}
