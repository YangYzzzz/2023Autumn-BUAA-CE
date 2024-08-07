package MidEnd.IrInstuctions;

import FrontEnd.SymbolTable.Symbol;
import MidEnd.IrCore.Value;

public class FakeValue extends Value {
    public Symbol symbol;
    public FakeValue(Symbol symbol) {
        super("%这是一个假标签", 9);
        this.symbol = symbol;
    }
}
