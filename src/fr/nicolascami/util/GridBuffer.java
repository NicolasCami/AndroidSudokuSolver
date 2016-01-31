package fr.nicolascami.util;

public class GridBuffer {

	private Grid[] 			_tampon;
	private int[] 			_tamponOccur;
	private int 			_itampon;
	private int 			_itamponMax;
	private Grid 			_grille;
	private int				_bufferSize;
	
	public GridBuffer(int size) {
		_bufferSize = size;
		_tampon = new Grid[_bufferSize];
		_tamponOccur = new int[_bufferSize];
		for(int i=0; i<size; i++) {
			_tampon[i] = new Grid(9);
			_tamponOccur[i] = 0;
		}
		_itampon = 0;
		_itamponMax = 0;
		_grille = new Grid(9);
	}
	
    public boolean changeGrid() {
    	boolean change = false;
    	for(int i=0; i<9; i++) {
    		for(int j=0; j<9; j++) {
    			if(_grille.get(j, i) != _tampon[_itamponMax].get(j, i)) {
    				_grille.set(j, i, _tampon[_itamponMax].get(j, i));
    				change = true;
    			}
    		}
    	}
    	return change;
    }
    
    public void fillBuffer(int[][] s) {
    	for(int i=0; i<9; i++) {
    		for(int j=0; j<9; j++) {
    			_tampon[_itampon].set(j, i, s[j][i]);
    			_tamponOccur[_itampon] = 0;
    			for(int k=0; k<8; k++) {
    				if(s[j][i] == _tampon[k].get(j, i)) _tamponOccur[_itampon]++;
    			}
    		}
    	}
    	_itamponMax = 0;
		for(int k=1; k<8; k++) {
			if(_tamponOccur[k] > _tamponOccur[_itamponMax]) _itamponMax = k;
		}
    	_itampon = (_itampon+1)%8;
    	
    	changeGrid();
    }
    
    public Grid getReliableGrid() {
    	return _grille;
    }
}
