package FrontEnd.NonTerminal.AllStmt;

import FrontEnd.NonTerminal.Block;
import FrontEnd.NonTerminal.Stmt;

public class BlockStmt implements Stmt {
    private Block block;

    public BlockStmt() {
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    @Override
    public void genLLVM() {
        block.genLLVM();
    }
}
