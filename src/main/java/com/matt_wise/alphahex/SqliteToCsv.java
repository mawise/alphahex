package com.matt_wise.alphahex;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.*;

/**
 * Created by matt on 3/5/16.
 */
public class SqliteToCsv {
    public static void main( String args[] )
    {
        Connection c = null;
        Statement stmt;
        String minScore = "1700";
        String outputFile = "/Users/matt/Documents/hex/"+minScore+"moves.csv";
        try {
            Class.forName("org.sqlite.JDBC");
            String dbfile = "jdbc:sqlite:/Users/matt/Documents/hex/hexparse.sqlite";
            c = DriverManager.getConnection(dbfile);
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(1);
        }
        System.out.println("Opened database successfully");

        try {
            stmt = c.createStatement();
            String sql = "SELECT * from game where playerOneScoreOld > "+minScore+" and playerTwoScoreOld > "+minScore + " and boardSize = 11";
            ResultSet rs = stmt.executeQuery(sql);

            boolean count = false;
            if (count){
                while (rs.next()) {
                    System.out.println(rs.getInt(1));
                }
            } else {
                try(PrintWriter writer = new PrintWriter(outputFile, "UTF-8")) {
                    while (rs.next()) {
                        String rawMovelist = rs.getString("movelist");
                        if (rawMovelist.length() > 0) {
                            if (rawMovelist.charAt(0) == '|') {
                                rawMovelist = rawMovelist.substring(1);
                            }
                            rawMovelist = rawMovelist.substring(0, rawMovelist.length() - 1);

                            writer.println(rawMovelist.replace('|', ','));
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }


    }
}
