package FrontEnd.NonTerminal;

import MidEnd.IrCore.Value;

import java.util.ArrayList;

public class Exp {
    private AddExp addExp;

    public Exp() {
    }

    public AddExp getAddExp() {
        return addExp;
    }

    public void setAddExp(AddExp addExp) {
        this.addExp = addExp;
    }

    public Integer getType() {
        // 0 0维； 1 1维； 2 2维
        return 0;
    }

    public boolean hasSingleLVal() {
        return addExp.hasSingleLVal();
    }

    public LVal getSingleLVal() {
        return addExp.getSingleLVal();
    }

    public ArrayList<Ident> getAllFuncIdent() {
        return new ArrayList<>(addExp.getAllFuncIdent());
    }

    public int calExp() {
        return addExp.calAddExp();
    }

    public Value genLLVM() {
        return addExp.genLLVM();
    }
}
