package fr.nicolascami.util;

public class Grid {

	private int _table[][];
	private int _size;
	
	public Grid() {
		_size = 9;
		_table = new int[_size][_size];
		reset();
	}
	
	public Grid(int size) {
		_size = size;
		_table = new int[size][size];
		reset();
	}
	
	public Grid(int[][] gridArray) {
		_size = gridArray.length;
		_table = new int[_size][_size];
        for(int i=0; i<_size; i++) {
            for(int j=0; j<_size; j++) {
            	_table[i][j] = gridArray[i][j];
            }
        }
	}
	
	public void reset() {
        for(int i=0; i<_size; i++) {
            for(int j=0; j<_size; j++) {
            	_table[i][j] = 0;
            }
        }
	}
	
	public void set(int i, int j, int value) {
		_table[i][j] = value;
	}
	
	public int get(int i, int j) {
		return _table[i][j];
	}
	
	public int[][] toArray() {
		return _table;
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
	    if (this._size != other._size) {
	        return false;
	    }
        for(int i=0; i<_size; i++) {
            for(int j=0; j<_size; j++) {
            	if(_table[i][j] != other._table[i][j]) {
            		return false;
            	}
            }
        }
	    return true;
	}

	@Override
	public int hashCode() {
	    int hash = 3;
	    hash = 42 * hash + (this._table != null ? this._table.hashCode() : 0);
	    hash = 42 * hash + this._size;
	    return hash;
	}
}
