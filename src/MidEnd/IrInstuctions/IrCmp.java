package MidEnd.IrInstuctions;

import FrontEnd.NonTerminal.Sign;
import MidEnd.IrCore.IrInstruction;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

public class IrCmp extends IrInstruction {
    private Sign cmpOp;
    public IrCmp(String value, Value cmp1, Value cmp2, Sign cmpOp) {
        super(value, 4);
        super.addUsedValue(cmp1);
        super.addUsedValue(cmp2);
        this.cmpOp = cmpOp;
    }

    public Sign getCmpOp() {
        return cmpOp;
    }

    public String toString() {
        String cond = null;
        switch (cmpOp) {
            case EQL -> cond = "eq";
            case NEQ -> cond = "ne";
            case GEQ -> cond = "sge";
            case LEQ -> cond = "sle";
            case GRE -> cond = "sgt";
            case LSS -> cond = "slt";
        }
        return value + " = icmp " + cond + " i32 " + usedValues.get(0).value + ", " + usedValues.get(1).value + "\n";
    }

    @Override
    public ArrayList<String> toHashCode() {
        ArrayList<String> hashCode = new ArrayList<>(); // 滿足結合律
        switch (cmpOp) {
            case EQL: {
                hashCode.add("&=&" + usedValues.get(0).value + usedValues.get(1).value);
                hashCode.add("&=&" + usedValues.get(1).value + usedValues.get(0).value);
                break;
            }
            case NEQ: {
                hashCode.add("&!=&" + usedValues.get(0).value + usedValues.get(1).value);
                hashCode.add("&!=&" + usedValues.get(1).value + usedValues.get(0).value);
                break;
            }
            case GEQ: {
                hashCode.add("&>=&" + usedValues.get(0).value + usedValues.get(1).value);
                hashCode.add("&<=&" + usedValues.get(1).value + usedValues.get(0).value);
                break;
            }
            case LEQ: {
                hashCode.add("&<=&" + usedValues.get(0).value + usedValues.get(1).value);
                hashCode.add("&>=&" + usedValues.get(1).value + usedValues.get(0).value);
                break;
            }
            case GRE: {
                hashCode.add("&>&" + usedValues.get(0).value + usedValues.get(1).value);
                hashCode.add("&<&" + usedValues.get(1).value + usedValues.get(0).value);
                break;
            }
            case LSS: {
                hashCode.add("&<&" + usedValues.get(0).value + usedValues.get(1).value);
                hashCode.add("&>&" + usedValues.get(1).value + usedValues.get(0).value);
                break;
            }
        }
        return hashCode;
    }
}
