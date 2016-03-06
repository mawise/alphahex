package com.matt_wise.alphahex;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Created by matt on 3/5/16.
 */
public class TestToLibSvm {

    @Test
    public void boardToVectors(){
        String movelist = "A1,B1";

        List<String> vectors = CsvGameToLibSvm.movesFromGame(movelist);

        Assert.assertEquals(vectors.get(0), "0 ");
        Assert.assertEquals(vectors.get(1), "11 0:-1");

    }
}
