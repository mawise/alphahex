package com.matt_wise.alphahex;

import org.apache.spark.mllib.linalg.SparseVector;
import org.apache.spark.mllib.linalg.Vector;

import java.util.*;

/**
 * All games are 11x11
 * Grid coordinates for input moves look like this:
 *  A B C D E F ...
 * 1 o o o o o o
 *  2 o o o o o o
 *   3 o o o o o o
 *    4 o o o o o o
 *     5 o o o o o o
 *      6 o o o o o o
 *        ...         ...
 * 0 is an empty cell
 * 1 is a red move
 * -1 is a blue move
 * Red is North-South
 * Blue is East-West
 * Red plays first
 */
public class HexBoard {

    private static int n = 11;

    //board size
    public static byte RED = 1;
    public static byte BLUE = -1;
    public static byte PLAYER_ONE = RED;
    public static byte PLAYER_TWO = BLUE;
    public static byte otherPlayer(byte thisPlayer){
        if (thisPlayer == RED){
            return BLUE;
        } else {
            return RED;
        }
    }

    public static int SWAP_INDEX = n*n;

    private byte[][] board;
    private boolean isEmpty = true;
    private static Map<String, Integer> coordToIndex;
    private static Map<Integer, String> indexToColumn;
    static{
        indexToColumn = new HashMap<>();
        coordToIndex = new HashMap<>();
        for (int i=0; i<n; i++) {
            coordToIndex.put(Character.toString((char)('A' + i)), i);
            indexToColumn.put(i, Character.toString((char)('A' + i)));
            coordToIndex.put(Integer.toString(i+1), i);
        }
    }

    public HexBoard(){
        board = new byte[n][n];
        for (byte[] i : board){
            for (short j : i){
                j = 0;
            }
        }
    }

    private HexBoard(byte[][] board, boolean isEmpty){
        this.board = board;
        this.isEmpty = isEmpty;
    }

    public boolean isEmpty(){
        return this.isEmpty;
    }

    /**
     * Assumes move is fair, does not check if cell is free does no validation
     * first character is column
     * remainder is numeric row
     *
     * @param move
     * @param player 1 for red, -1 for blue
     */

    public void addMove(String move, byte player) {
        String col = move.substring(0, 1);
        String row = move.substring(1, move.length());
        board[coordToIndex.get(row)][coordToIndex.get(col)] = player;
        isEmpty = false;
    }

    public HexBoard withPlayerOneToMove(byte currentPlayerToMove){
        if (currentPlayerToMove == PLAYER_ONE){
            return this;
        } else {
            byte[][] newBoard = new byte[n][n];
            for (int i=0; i<n; i++){
                for (int j=0; j<n; j++){
                    newBoard[i][j] = (byte) (board[j][i] * -1);
                }
            }
            return new HexBoard(newBoard, isEmpty);
        }
    }

    public HexBoard rotate(){
        byte[][] newBoard = new byte[n][n];
        for (int i=0; i<n; i++){
            newBoard[i] = board[n-(i+1)].clone();
            reverseArray(newBoard[i]);
        }
        return new HexBoard(newBoard, isEmpty);
    }

    public static void reverseArray(byte[] array){
        for(int i = 0; i < array.length / 2; i++)
        {
            byte temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }
    }

    public byte[] serialize(){
        byte[] output = new byte[n*n];
        for (int i=0; i<n; i++){
            System.arraycopy(board[i], 0, output, i*n, n);
        }
        return output;
    }

    public String toSparseLibSvmVector(){
        StringBuilder out = new StringBuilder();
        for (int i=0; i<n; i++){
            byte[] row = board[i];
            for (int j=0; j<n; j++){
                byte player = row[j];
                if (player != 0){
                    String index = Integer.toString(i*n+j);
                    out.append(index + ":" + Integer.toString(player));
                    out.append(" ");
                }
            }
        }
        if (out.length() > 0){
            out.deleteCharAt(out.length()-1);
        }
        return out.toString();
    }

    public Vector toVector(){
        int spot = 0;
        int[] indicies = new int[SWAP_INDEX+1];
        double[] values = new double[SWAP_INDEX+1];
        for (int i=0; i<n; i++){
            byte[] row = board[i];
            for (int j=0; j<n; j++){
                byte player = row[j];
                if (player != 0){
                    indicies[spot] = i*n+j;
                    values[spot] = ((double) player);
                    spot +=1;
                }
            }
        }
        int[] smallIndicies = new int[spot];
        double[] smallValues = new double[spot];
        System.arraycopy(indicies, 0, smallIndicies, 0, spot);
        System.arraycopy(values, 0, smallValues, 0, spot);
        return new SparseVector(SWAP_INDEX+1, smallIndicies, smallValues);
    }

    public static String indexOfMoveForPlayerOne(String move, byte currentPlayer){
        String col = move.substring(0, 1);
        String row = move.substring(1, move.length());
        int colInt;
        int rowInt;
        try {
            colInt = coordToIndex.get(col);
            rowInt = coordToIndex.get(row);
        } catch (NullPointerException npe){
            System.out.println("Move: " + move);
            System.out.println("Parsed Col: " + col);
            System.out.println("Parsed Row: " + row);
            throw npe;
        }
        if (currentPlayer == PLAYER_ONE){
            return Integer.toString(n*rowInt + colInt);
        } else {
            return Integer.toString(n*colInt + rowInt);
        }
    }

    public static String indexOfMove(String move){
        if (move.equals("SWAP")){
            return Integer.toString(SWAP_INDEX);
        }
        String col = move.substring(0, 1);
        String row = move.substring(1, move.length());
        int colInt = coordToIndex.get(col);
        int rowInt = coordToIndex.get(row);
        return Integer.toString(n*rowInt + colInt);
    }

    public static String indexToMove(double index){
        return indexToMove((int) Math.round(index));
    }
    public static String indexToMove(int index){
        if (index == SWAP_INDEX){
            return "SWAP";
        }
        String row = Integer.toString((index / n) + 1);
        String col = indexToColumn.get(index % n);
        return col+row;
    }

    public void printBoard(){
        System.out.print(" ");
        for (int i=0; i<n; i++){
            System.out.print(" " + (char)('A' + i));
        }
        System.out.println("");
        for (int i=0; i<n; i++){
            byte[] row = board[i];

            if (i+1<10){
                System.out.print(" "); //pad single digits
            }
            for (int s=0; s<i; s++){ //row offsets (spaces)
                System.out.print(" ");
            }
            System.out.print(i+1); //row marker

            for (int j=0; j<n; j++){
                System.out.print(" ");
                if (row[j]==RED) {
                    System.out.print("R");
                } else if (row[j]==BLUE){
                    System.out.print("B");
                } else {
                    System.out.print("O");
                }
            }
            System.out.println("");  //newline at end of row
        }
    }

}
