package fr.nicolascami.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class ComparateurList implements Comparator<List<Integer>> {
	public int compare(List<Integer> s1, List<Integer> s2){
        //tri desc
		if (s1.get(2) < s2.get(2)) {
			return -1;
		} else if (s1.get(2) > s2.get(2)) {
			return 1;        	
		} else {
			return 0;
		}
	}      
}

public class Solver {
	
	// grid array buffer
	Grid buffer[];
	
	// grid occurence in the buffer
	int bufferOccur[];
	
	// current index 
	int bufferIndex;
	int bufferIndexMax;
	int bufferSize = 8;
	 
	// Sudoku size K=square size, N=grid size (N=K*K)
	static private int K, N;
	 
	// the grid to solve 
	static private int[][] solution;
	 
	// marker for the initial (fixed) values
	static private boolean[][] fixed;
	 
	// locks on the rows, columns and squares
	static private boolean[][] lockrow, lockcol, locksqr;
	
	boolean backtrack;
	int position;

	public Solver(int k) {
		K = k;
		N = k*k;
		
        buffer = new Grid[bufferSize];
        bufferOccur = new int[bufferSize];
        bufferIndex = 0;
        bufferIndexMax = 0;
        for(int i=0; i<bufferSize; i++) {
        	buffer[i] = new Grid(k);
        	bufferOccur[i] = 0;
        }
	}
	
	public int[][] getSolution() {
		return solution;
	}
	 
	// initialize/reset structures
	static private void initialize() {
		solution = new int[N][N];
		fixed    = new boolean[N][N];
		lockrow  = new boolean[N][N];
		lockcol  = new boolean[N][N];
		locksqr  = new boolean[N][N];
	}
	 
	// load initial grid
	static private void load(int[][] grid) {
		for(int i=0; i<9; i++) {
			for(int j=0; j<9; j++) {
				int row = i, col = j, val = grid[i][j];
				if (val==0) continue;
				solution[row][col]=val;
				fixed[row][col]=true;
				setlock(row,col,val, true);
			}
		}	
	}
	 
	// set/unset locks for the tuple (row,col,value)
	static private void setlock(int row, int col, int val, boolean state) {
		int sqr=(col/K)+K*(row/K);
		lockrow[row][val-1]=state;
		lockcol[col][val-1]=state;
		locksqr[sqr][val-1]=state;	
	}
	 
	// check if a tuple (row,col,value) is locked
	static private boolean islocked(int row, int col, int val) {
		if (lockrow[row][val-1]) return true;
		if (lockcol[col][val-1]) return true;
		int sqr=(col/K)+K*(row/K);
		if (locksqr[sqr][val-1]) return true;
		return false;
	}
	 
	// print grid
	static private void print() {
		StringBuffer sb = new StringBuffer();
		for(int r=0;r<N;r++) {
		for(int c=0;c<N;c++)
		sb.append( (solution[r][c]==0)?".":solution[r][c] ).append(" ");
		sb.append("\n");
		}
		System.out.println(sb.toString());
	}
	 
	// solver
	public boolean solve(int[][] grid, boolean nouveau) {
		if(nouveau) {
			initialize();
			load(grid);
			print();
			backtrack=false;
			position=0;
		}
	 
		int cpt=0;
		while(position>=0 && position<N*N) {
			if(cpt++ > 2000) {
				grid = solution;
				return false;
			}
			int row = position/N;
			int col = position%N;
	
			if (fixed[row][col]) {
				if (backtrack) position--;
				else position++;
				continue;
			}
	
			if (solution[row][col]>0) setlock(row,col,solution[row][col], false);
	
			int val = solution[row][col]+1;
			while(val<=N && islocked(row,col,val)) val++;
			 
			if (val<=N) {
				solution[row][col]=val;
				setlock(row,col,val, true); 
				backtrack=false;
				position++;
			} else {
				solution[row][col]=0;
				backtrack=true;
				position--;
			}
		}
		return true;
	}
}