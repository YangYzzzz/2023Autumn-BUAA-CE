package FrontEnd.NonTerminal;

import FrontEnd.SymbolTable.Dimension1ArraySymbol;
import FrontEnd.SymbolTable.Dimension2ArraySymbol;
import FrontEnd.SymbolTable.Symbol;
import MidEnd.IrCore.Value;

import java.util.ArrayList;

import static FrontEnd.SymbolTable.StaticSymbolTable.getSymbol;

public class PrimaryExp {
    private int flag;
    private Exp exp_0;
    private LVal lVal_1;
    private Number number_2;

    public PrimaryExp() {
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public Exp getExp_0() {
        return exp_0;
    }

    public void setExp_0(Exp exp_0) {
        this.exp_0 = exp_0;
    }

    public LVal getlVal_1() {
        return lVal_1;
    }

    public void setlVal_1(LVal lVal_1) {
        this.lVal_1 = lVal_1;
    }

    public Number getNumber_2() {
        return number_2;
    }

    public void setNumber_2(Number number_2) {
        this.number_2 = number_2;
    }

    public boolean hasSingleLVal() {
        return switch (flag) {
            case 0 -> exp_0.hasSingleLVal();
            // 存在单独的LVal
            case 1 -> true;
            default -> false;
        };
    }

    public LVal getSingleLVal() {
        return switch (flag) {
            case 0 -> exp_0.getSingleLVal();
            case 1 -> lVal_1;
            default -> null;
        };
    }

    public int calPrimaryExp() {
        int constValue = 0;
        switch (flag) {
            case 0: {
                constValue = exp_0.calExp();
                break;
            }
            case 1: {
                Symbol symbol = getSymbol(lVal_1.getIdent().getName());
                ArrayList<Integer> pos = lVal_1.calLValExps();
                switch (symbol.getType()) {
                    // 0维
                    case 0: {
                        constValue = symbol.getInitVal();
                        break;
                    }
                    // 1维
                    case 1: {
                        ArrayList<Integer> initials = ((Dimension1ArraySymbol) symbol).getInitials();
                        if (pos.get(0) + 1 > initials.size()) {
                            // System.out.println("数组越界");
                        } else {
                            constValue = initials.get(pos.get(0));
                        }
                        break;
                    }
                    // 2维
                    case 2: {
                        ArrayList<Integer> initials = ((Dimension2ArraySymbol) symbol).getInitials();
                        // 二维数组展开成一维数组寻址
                        int dim2 = ((Dimension2ArraySymbol) symbol).getDim2Size();
                        int pos1 = pos.get(0); // 行
                        int pos2 = pos.get(1); // 列
                        constValue = initials.get(pos1 * dim2 + pos2);
                        break;
                    }
                }
                break;
            }
            case 2: {
                constValue = number_2.getNum();
                break;
            }
        }
        return constValue;
    }

    public Value genLLVM() {
        switch (flag) {
            case 0: {
                return exp_0.genLLVM();
            }
            case 1: {
                // 返回的是 值！
                return lVal_1.genLLVM();
            }
            case 2: {
                return new Value(String.valueOf(number_2.getNum()), 0);
            }
            default: {
                return null;
            }
        }
    }
}
