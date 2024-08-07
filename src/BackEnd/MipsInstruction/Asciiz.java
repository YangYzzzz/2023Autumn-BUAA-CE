package BackEnd.MipsInstruction;

/*
    用于记录字符串常量
 */
public class Asciiz implements MipsInstruction {
    static public int STRCNT = 0;
    private String name; // 给分配的
    private String string;

    public Asciiz(String string) {
        this.name = "str_" + STRCNT;
        this.string = string;
        STRCNT++;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        // 字符串在LLVM中将\n转化成10，在mips中要再转化成\n两个字符
        string = string.replaceAll("\n", "\\\\n");
        sb.append(name).append(": .asciiz \"").append(string).append("\"\n");
        return sb.toString();
    }

}
