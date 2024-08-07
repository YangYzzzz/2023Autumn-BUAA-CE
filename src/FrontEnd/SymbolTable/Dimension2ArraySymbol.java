package FrontEnd.SymbolTable;

import FrontEnd.NonTerminal.Ident;

import java.util.ArrayList;

public class Dimension2ArraySymbol extends Symbol {
    private int dim1Size;
    private int dim2Size;
    private ArrayList<Integer> initials;
    public Dimension2ArraySymbol(Ident ident, int type, int con) {
        super(ident, type, con);
        initials = new ArrayList<>();
    }

    public int getDim1Size() {
        return dim1Size;
    }

    public void setDim1Size(int dim1Size) {
        this.dim1Size = dim1Size;
    }

    public int getDim2Size() {
        return dim2Size;
    }

    public void setDim2Size(int dim2Size) {
        this.dim2Size = dim2Size;
    }

    public ArrayList<Integer> getInitials() {
        return initials;
    }

    public void setInitials(ArrayList<Integer> initials) {
        this.initials = initials;
    }
}
