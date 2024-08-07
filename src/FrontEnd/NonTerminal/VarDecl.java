package FrontEnd.NonTerminal;

import java.util.ArrayList;

public class VarDecl implements Decl {
    private BType bType;
    private ArrayList<VarDef> varDefs;

    public VarDecl() {
        varDefs = new ArrayList<>();
    }

    public BType getbType() {
        return bType;
    }

    public void setbType(BType bType) {
        this.bType = bType;
    }

    public void addVarDef(VarDef varDef) {
        varDefs.add(varDef);
    }

    @Override
    public void genGlobalLLVM() {
        for (VarDef varDef : varDefs) {
            varDef.genGlobalLLVM();
        }
    }

    @Override
    public void genLLVM() {
        for (VarDef varDef : varDefs) {
            varDef.genLLVM();
        }
    }
}
