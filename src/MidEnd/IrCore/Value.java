package MidEnd.IrCore;

import java.util.HashSet;
import java.util.Set;

public class Value {
    // 记录返回值 store无返回值
    public String value;
    /*
        记录返回值的类型 规定如下
       -1 ---> 正常store, move
        0 ---> i32
        1 ---> i32*
        2 ---> [n x i32]*  一维数组的首地址 或 二维指针 n为lineSize!!!
        3 ---> [m x [n x i32]]*  二维数组的首地址
        4 ---> i1
        5 ---> func
        6 ---> basicblock
        7 ---> i32 **
        8 ---> [n x i32]** n为lineSize!!!
        9 ---> fakevalue 之后会被重命名替代
        11 ---> 图着色时新增的将溢出的变量store入内存所用的Value 仅作为标识
     */
    public int valueType;
    // 使用该Value的User
    public Set<User> users;
    public int firstSize;
    public int secondSize;
    public int offset; // 对于基地址，是基于Fp或Gp的偏移
    public Boolean isGp; // 标记全局Alloc的属性
    public Boolean isFp; // 标记Alloc的属性
    public Boolean isInReg; // 标记是否在寄存器中
    public Boolean isInStack;
    public Boolean isInTempStack;
    public int reg;
    public Value combineValue; // 若该Value被合并，则记录它被合并到了哪个Value
    public Boolean isInMem;
    /**
     * 看作是一个冲突图内的节点，需要记录临界矩阵
     * @param value
     * @param valueType
     */
    public Set<Value> adjValues;
    public Value(String value, int valueType) {
        this.value = value;
        this.valueType = valueType;
        this.firstSize = 0;
        this.secondSize = 0;
        this.users = new HashSet<>();
        this.offset = -1;
        this.isGp = false;
        this.isFp = false;
        this.isInReg = false;
        this.reg = -1;
        this.adjValues = new HashSet<>();
        this.isInStack = false;
        this.combineValue = null;
        this.isInTempStack = false;
        this.isInMem = false;
    }

    // 10
    public Value(String value, int valueType, int fpOffset) {
        this.value = value;
        this.valueType = valueType;
        this.firstSize = 0;
        this.secondSize = 0;
        this.users = new HashSet<>();
        this.offset = fpOffset;
        this.isGp = false;
        this.isFp = false;
        this.isInReg = false;
        this.reg = -1;
        this.adjValues = new HashSet<>();
        this.isInStack = false;
        this.combineValue = null;
        this.isInTempStack = false;
        this.isInMem = false;
    }

    // type = 2, 8, 9
    public Value(String value, int valueType, int firstSize, int secondSize) {
        this.value = value;
        this.valueType = valueType;
        this.users = new HashSet<>();
        this.firstSize = firstSize;
        this.secondSize = secondSize;
        this.offset = -1;
        this.isGp = false;
        this.isFp = false;
        this.isInReg = false;
        this.reg = -1;
        this.adjValues = new HashSet<>();
        this.isInStack = false;
        this.combineValue = null;
        this.isInTempStack = false;
        this.isInMem = false;
    }
    // 为该条指令的Value增加User, 代表着被哪些Value（User）使用
    public void addUser(User user) {
        users.add(user);
    }
    public Boolean isNum() {
        return value != null && value.charAt(0) != '@' && value.charAt(0) != '%' && value.charAt(0) != '!' && valueType == 0 && !isInMem;
//        return value.matches("[-+]?\\d*");
    }

    public Boolean isVirtualReg() {
        return value.charAt(0) == '%' && valueType != 6 && !isInMem;
    }

    public Boolean isGlobal() {
        return value.charAt(0) == '@';
    }
    public Boolean isFpOffset() {
        return valueType == 11;
    }

    public Boolean isInMem() {
        return isInMem;
    }
    public Boolean equal(Value value) {
        return value.value.equals(this.value);
    }

    public void setReg(int reg) {
        this.reg = reg;
        this.isInReg = true;
    }

    public int getAdjDegree() {
        int cnt = 0;
        for (Value adj : adjValues) {
            if (!adj.isInStack) {
                cnt++;
            }
        }
        return cnt;
    }
}
