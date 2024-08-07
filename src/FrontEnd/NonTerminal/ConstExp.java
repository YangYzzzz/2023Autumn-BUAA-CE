package FrontEnd.NonTerminal;
public class ConstExp {
    // 区分于Exp 因为这里的标识符都必须要是常量
    private AddExp addExp;

    public ConstExp() {
    }

    public AddExp getAddExp() {
        return addExp;
    }

    public void setAddExp(AddExp addExp) {
        this.addExp = addExp;
    }

    public int calConstExp() {
        return addExp.calAddExp();
    }
}
