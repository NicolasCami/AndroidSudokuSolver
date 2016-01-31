package fr.nicolascami.util;

public class Grid {

	int table[][];
	int size;
	
	public Grid() {
		this.size = 9;
		this.table = new int[size][size];
		reset();
	}
	
	public Grid(int size) {
		this.size = size;
		this.table = new int[size][size];
		reset();
	}
	
	public void reset() {
        for(int i=0; i<size; i++) {
            for(int j=0; j<size; j++) {
            	table[i][j] = 0;
            }
        }
	}
	
	public void set(int i, int j, int value) {
		table[i][j] = value;
	}
	
	public int get(int i, int j) {
		return table[i][j];
	}
}
