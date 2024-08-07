package FrontEnd.NonTerminal.AllStmt;

import FrontEnd.NonTerminal.Exp;
import FrontEnd.NonTerminal.Stmt;
import MidEnd.IrCore.IrInstruction;
import MidEnd.IrInstuctions.IrCall;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

import static MidEnd.IrCore.IrModule.curIrBasicBlock;
import static MidEnd.IrCore.IrModule.irModule;

public class OutputStmt implements Stmt {
    private String string;
    private ArrayList<Exp> exps;

    public OutputStmt() {
        exps = new ArrayList<>();
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public void addExp(Exp exp) {
        exps.add(exp);
    }

    public int getExpNum() {
        return exps.size();
    }

    @Override
    public void genLLVM() {
        String outputStr = string.substring(1, string.length() - 1);
        ArrayList<Value> outputValues = new ArrayList<>();
        for (Exp exp : exps) {
            outputValues.add(exp.genLLVM());
        }
        int index = 0;
        IrInstruction irInstruction;
        for (int i = 0; i < outputStr.length(); i++) {
            ArrayList<Value> paramsValue = new ArrayList<>();
            if (outputStr.charAt(i) == '%' && outputStr.charAt(i+1) == 'd') {
                i++;
                paramsValue.add(outputValues.get(index));
                index++;
                irInstruction = new IrCall(irModule.getPutIntValue(), paramsValue);
                curIrBasicBlock.addInstruction(irInstruction);
            } else if (outputStr.charAt(i) == '\\' && outputStr.charAt(i+1) == 'n') {
                i++;
                paramsValue.add(new Value("10", 0));
                irInstruction = new IrCall(irModule.getPutChValue(), paramsValue);
                curIrBasicBlock.addInstruction(irInstruction);
            } else {
                paramsValue.add(new Value(String.valueOf(Integer.valueOf(outputStr.charAt(i))), 0));
                irInstruction = new IrCall(irModule.getPutChValue(), paramsValue);
                curIrBasicBlock.addInstruction(irInstruction);
            }
        }
    }
}
