package FrontEnd.NonTerminal;

import MidEnd.IrCore.Value;

import java.util.ArrayList;

public class InitVal {
    private Exp exp;
    private ArrayList<InitVal> initVals;

    public InitVal() {
        initVals = new ArrayList<>();
    }

    public Exp getExp() {
        return exp;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }

    public void addInitVal(InitVal initVal) {
        initVals.add(initVal);
    }

    public int calInitVal() {
        return exp.calExp();
    }

    public ArrayList<InitVal> getInitVals() {
        return initVals;
    }

    public void setInitVals(ArrayList<InitVal> initVals) {
        this.initVals = initVals;
    }

    public ArrayList<Integer> calArr1InitVal() {
        ArrayList<Integer> initials = new ArrayList<>();
        for (InitVal initVal : initVals) {
            initials.add(initVal.calInitVal());
        }
        return initials;
    }

    public ArrayList<Integer> calArr2InitVal() {
        ArrayList<Integer> initials = new ArrayList<>();
        for (InitVal initVal : initVals) {
            for (InitVal initVal1 : initVal.initVals) {
                initials.add(initVal1.calInitVal());
            }
        }
        return initials;
    }

    public Value genLLVM() {
        return exp.genLLVM();
    }
}
