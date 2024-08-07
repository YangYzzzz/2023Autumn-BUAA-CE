package MidEnd.IrInstuctions;

import MidEnd.IrCore.IrInstruction;

public class IrAlloc extends IrInstruction {

    // i32*
    public IrAlloc(String value) {
        super(value, 1);
    }

    // [n x i32]**
    public IrAlloc(String value, int firstSize, Boolean ptrFlag) {
        super(value, 8, firstSize, 0);
    }

    // i32**
    public IrAlloc(String value, Boolean ptrFlag) {
        super(value, 7);
    }

    // [n x i32]*
    public IrAlloc(String value, int firstSize) {
        super(value, 2, firstSize, 0);
    }

    // [m x [n x i32]]*
    public IrAlloc(String value, int firstSize, int secondSize) {
        super(value, 3, firstSize, secondSize);
    }
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(value).append(" = alloca ");
        switch (valueType) {
            case 1: {
                sb.append("i32");
                break;
            }
            case 7: {
                sb.append("i32*");
                break;
            }
            case 8: {
                sb.append("[").append(firstSize).append(" x i32]*");
                break;
            }
            case 2: {
                sb.append("[").append(firstSize).append(" x i32]");
                break;
            }
            case 3: {
                sb.append("[").append(firstSize).append(" x [").append(secondSize).append(" x i32]]");
            }
        }
        return sb + "\n";
    }
}
