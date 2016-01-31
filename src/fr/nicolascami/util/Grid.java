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
	
	@Override
	public boolean equals(Object obj) {
	    if (obj == null) {
	        return false;
	    }
	    if (getClass() != obj.getClass()) {
	        return false;
	    }
	    final Grid other = (Grid) obj;
	    if (this.size != other.size) {
	        return false;
	    }
        for(int i=0; i<size; i++) {
            for(int j=0; j<size; j++) {
            	if(table[i][j] != other.table[i][j]) {
            		return false;
            	}
            }
        }
	    return true;
	}

	@Override
	public int hashCode() {
	    int hash = 3;
	    hash = 42 * hash + (this.table != null ? this.table.hashCode() : 0);
	    hash = 42 * hash + this.size;
	    return hash;
	}
}
