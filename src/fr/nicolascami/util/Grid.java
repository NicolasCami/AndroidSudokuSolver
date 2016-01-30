package fr.nicolascami.util;

public class Grid {

	int table[][];
	int size;
	
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
}
