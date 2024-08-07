package FrontEnd.NonTerminal;

import java.util.ArrayList;

public class FuncFParams {
    private ArrayList<FuncFParam> funcFParams;

    public FuncFParams() {
        funcFParams = new ArrayList<>();
    }

    public ArrayList<FuncFParam> getFuncFParams() {
        return funcFParams;
    }

    public void addFuncFParam(FuncFParam funcFParam) {
        funcFParams.add(funcFParam);
    }
}
