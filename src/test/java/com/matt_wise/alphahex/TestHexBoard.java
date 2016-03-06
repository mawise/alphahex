package com.matt_wise.alphahex;


import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by matt on 3/5/16.
 */
public class TestHexBoard {
    @Test
    public void HexBoardDoesThings(){
        HexBoard b = new HexBoard();
        Assert.assertTrue(b.isEmpty());
        String serialized = b.toSparseLibSvmVector();
        Assert.assertEquals(serialized, "");

        b.addMove("A1", HexBoard.RED);
        serialized = b.toSparseLibSvmVector();
        Assert.assertEquals(serialized, "0:1");

        b.addMove("B1", HexBoard.BLUE);
        serialized = b.toSparseLibSvmVector();
        Assert.assertEquals(serialized, "0:1 1:-1");
        Assert.assertFalse(b.isEmpty());

        b.printBoard();
    }

    @Test
    public void indexOfMove(){
        String move = "C1";
        String index = HexBoard.indexOfMove(move);
        Assert.assertEquals(index, "2");

        index = HexBoard.indexOfMoveForPlayerOne(move, HexBoard.PLAYER_ONE);
        Assert.assertEquals(index, "2");

        index = HexBoard.indexOfMoveForPlayerOne(move, HexBoard.PLAYER_TWO);
        Assert.assertEquals(index, "22");
    }

    @Test
    public void switchPlayer(){
        HexBoard b = new HexBoard();
        b.addMove("A3", HexBoard.RED);
        String serialized = b.toSparseLibSvmVector();
        Assert.assertEquals(serialized, "22:1");

        HexBoard b2 = b.withPlayerOneToMove(HexBoard.BLUE);
        serialized = b2.toSparseLibSvmVector();
        Assert.assertEquals(serialized, "2:-1");
        Assert.assertFalse(b2.isEmpty());
    }

    @Test
    public void getMoveFromIndex(){
        String move = "F9";
        String index = HexBoard.indexOfMove(move);
        String moveFromIndex = HexBoard.indexToMove(Integer.parseInt(index));

        Assert.assertEquals(moveFromIndex, move);

        move = "SWAP";
        index = HexBoard.indexOfMove(move);
        moveFromIndex = HexBoard.indexToMove(Integer.parseInt(index));

        Assert.assertEquals(moveFromIndex, move);
    }
}
