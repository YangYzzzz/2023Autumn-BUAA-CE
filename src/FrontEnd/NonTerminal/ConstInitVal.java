package FrontEnd.NonTerminal;

import java.util.ArrayList;

public class ConstInitVal {
    private ConstExp constExp; // 0维
    private ArrayList<ConstInitVal> constInitVals; // 1维 2维

    public ConstInitVal() {
        constInitVals = new ArrayList<>();
    }

    public void addConstInitVal(ConstInitVal constInitVal) {
        constInitVals.add(constInitVal);
    }

    public ConstExp getConstExp() {
        return constExp;
    }

    public ArrayList<ConstInitVal> getConstInitVals() {
        return constInitVals;
    }

    public void setConstInitVals(ArrayList<ConstInitVal> constInitVals) {
        this.constInitVals = constInitVals;
    }

    public void setConstExp(ConstExp constExp) {
        this.constExp = constExp;
    }

    public int calConstInitVal() {
        return constExp.calConstExp();
    }

    public ArrayList<Integer> calArr1ConstInitVal() {
        ArrayList<Integer> initials = new ArrayList<>();
        for (ConstInitVal constInitVal : constInitVals) {
            initials.add(constInitVal.calConstInitVal());
        }
        return initials;
    }

    public ArrayList<Integer> calArr2ConstInitVal() {
        ArrayList<Integer> initials = new ArrayList<>();
        for (ConstInitVal constInitVal : constInitVals) {
            for (ConstInitVal constInitVal1 : constInitVal.constInitVals) {
                initials.add(constInitVal1.calConstInitVal());
            }
        }
        return initials;
    }

}
