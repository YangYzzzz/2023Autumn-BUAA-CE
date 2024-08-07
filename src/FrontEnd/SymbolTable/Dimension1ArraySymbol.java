package FrontEnd.SymbolTable;

import FrontEnd.NonTerminal.Ident;

import java.util.ArrayList;

public class Dimension1ArraySymbol extends Symbol {
    // 数组标识符的定义 是否可以用于表示指针
    private int dimSize;
    private ArrayList<Integer> initials;
    public Dimension1ArraySymbol(Ident ident, int type, int con) {
        super(ident, type, con);
        initials = new ArrayList<>();
    }

    public int getDimSize() {
        return dimSize;
    }

    public void setDimSize(int dimSize) {
        this.dimSize = dimSize;
    }

    public ArrayList<Integer> getInitials() {
        return initials;
    }

    public void setInitials(ArrayList<Integer> initials) {
        this.initials = initials;
    }
}
