package FrontEnd.NonTerminal;

import MidEnd.IrCore.Value;

import java.util.ArrayList;

public class FuncRParams {
    private ArrayList<Exp> exps;

    public FuncRParams() {
        exps = new ArrayList<>();
    }

    public void addExps(Exp exp) {
        exps.add(exp);
    }
    public int getParamsNum() {
        return exps.size();
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }

    public ArrayList<Value> genLLVM() {
        ArrayList<Value> rParams = new ArrayList<>();
        // 两种情况 值或地址 先区分出来
        for (Exp exp : exps) {
            rParams.add(exp.genLLVM());
        }
        return rParams;
    }
}
