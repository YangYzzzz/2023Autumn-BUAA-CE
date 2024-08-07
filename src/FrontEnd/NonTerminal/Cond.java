package FrontEnd.NonTerminal;

import MidEnd.IrInstuctions.IrBr;

import java.util.ArrayList;

public class Cond {
    private LOrExp lOrExp;

    public Cond() {
    }

    public void setlOrExp(LOrExp lOrExp) {
        this.lOrExp = lOrExp;
    }

    public void genLLVM(ArrayList<IrBr> irBrLLVMCode, String fakeLabel1, String fakeLabel2) {
        lOrExp.genLLVM(irBrLLVMCode, fakeLabel1, fakeLabel2);
    }

}
