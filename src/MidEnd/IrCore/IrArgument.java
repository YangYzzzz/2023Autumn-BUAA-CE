package MidEnd.IrCore;

public class IrArgument extends User {
    // 函数的形参
    public IrArgument(String value, int type) {
        // 一维数组指针 或 常量 / type 对应 0和1
        super(value, type);
    }

    public IrArgument(String value, int type, int fistSize) {
        // 不使用任何Value, Value即形参的Value %0 诸如此类 / type 对应 2
        super(value, type, fistSize, 0);
    }
}
