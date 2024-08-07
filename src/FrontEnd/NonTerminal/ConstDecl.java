package FrontEnd.NonTerminal;

import java.util.ArrayList;

public class ConstDecl implements Decl {
    private BType bType;
    ArrayList<ConstDef> constDefs;

    public ConstDecl() {
        this.constDefs = new ArrayList<>();
    }

    public BType getBType() {
        return bType;
    }

    public void setBType(BType bType) {
        this.bType = bType;
    }

    public void addConstDefs(ConstDef constDef) {
        constDefs.add(constDef);
    }

    @Override
    public void genGlobalLLVM() {
        for (ConstDef constDef : constDefs) {
            constDef.genGlobalLLVM();
        }
    }

    @Override
    public void genLLVM() {
        for (ConstDef constDef : constDefs) {
            constDef.genLLVM();
        }
    }
}
