package fr.nicolascami.util;

public class GridBuffer {

	private Grid[] 			_buffer;
	private int[] 			_gridFrenquecy; // frequency of a grid pattern in the buffer
	private int 			_currentGridIndex;
	private int 			_reliableGridIndex;
	private Grid 			_reliableGrid;
	private int				_bufferSize;
	
	public GridBuffer(int size) {
		_bufferSize = size;
		_buffer = new Grid[_bufferSize];
		_gridFrenquecy = new int[_bufferSize];
		for(int i=0; i<size; i++) {
			_buffer[i] = new Grid(9);
			_gridFrenquecy[i] = 0;
		}
		_currentGridIndex = 0;
		_reliableGridIndex = 0;
		_reliableGrid = new Grid(9);
	}
	
    public boolean changeGrid() {
    	boolean change = false;
    	for(int i=0; i<9; i++) {
    		for(int j=0; j<9; j++) {
    			if(_reliableGrid.get(j, i) != _buffer[_reliableGridIndex].get(j, i)) {
    				_reliableGrid.set(j, i, _buffer[_reliableGridIndex].get(j, i));
    				change = true;
    			}
    		}
    	}
    	return change;
    }
    
    public void fillBuffer(int[][] s) {
    	// fill the buffer with the new grid
    	for(int i=0; i<9; i++) {
    		for(int j=0; j<9; j++) {
    			_buffer[_currentGridIndex].set(j, i, s[j][i]);
    		}
    	}
    	
    	// TODO : improve speed, by decrease by 1 all grid equals to the older grid, and by compute only the new grid frequency (x8 faster)
    	// compute each grids frequency
		for(int k=0; k<_bufferSize; k++) {
			_gridFrenquecy[k] = 0;
			for(int l=0; l<_bufferSize; l++) {
				if(_buffer[k].equals(_buffer[l])) {
					_gridFrenquecy[k]++;
				}
			}
		}
    	
    	// choose the most reliable grid
    	_reliableGridIndex = 0;
		for(int k=0; k<_bufferSize; k++) {
			if(_gridFrenquecy[k] > _gridFrenquecy[_reliableGridIndex]) _reliableGridIndex = k;
		}
    	_currentGridIndex = (_currentGridIndex+1)%8;
    	
    	changeGrid();
    }
    
    public Grid getReliableGrid() {
    	return _reliableGrid;
    }
}
