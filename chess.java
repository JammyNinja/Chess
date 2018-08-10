/*
TODO
Board setup:
	8x8 Grid
	enumerate piece types (remember promotion so no limit, perhaps 12 to include both colours) King, Queen, Bishop, Knight, Rook, Pawn
	Pieces Placement
	Piece movement validation:
		can the piece land on that square based on how it moves?
		nothing blocking its way?
		is the square occupied? - by own piece or enemy king
		is own team now not in check? #pin #checkDefence

		pawn en passant
	Post movement checks:
		Pawn promotion?
		other team in check now? (use piece occupy king on next move using validation above?)

*/
public class chess {

	static chess game;
	static chessGUI gui;
	int[][] board = new int[8][8];
	String[] files = {"a","b","c","d","e","f","g","h"};
	int turn; //1 white, -1 black
	
	public static void main(String args[]){
		System.out.println("Welcome to Louis' Chess. Please do *check* it out!");
		game = new chess();
		gui = new chessGUI(game);

	}

	public chess(){
		setupBoard();
		startGame();
	}

	//Positive numbers are white, negative are black, board 0,0 is top left
	//King = 1, Queen = 2, Bishop = 3, Knight = 4, Rook = 5, Pawn = 6
	private void setupBoard(){
		
		//Kings and Queens #Royalty
		board[4][7] =  1; //White King
		board[4][0] = -1; //Black King
		board[3][7] =  2; //White Queen
		board[3][0] = -2; //Black Queen

		//Bishops
		board[2][7] =  3; //White dark Bishop
		board[5][7] =  3; //White light Bishop
		board[2][0] = -3; //Black light Bishop
		board[5][0] = -3; //Black dark Bishop

		//Knights
		board[1][7] =  4; //White Queenside Knight
		board[6][7] =  4; //White Kingside Knight
		board[1][0] = -4; //Black Queenside Knight
		board[6][0] = -4; //Black Kingside Knight

		//Rooks
		board[0][7] =  5; //White Queenside Rook
		board[7][7] =  5; //White Kingside Rook
		board[0][0] = -5; //Black Queenside Rook
		board[7][0] = -5; //Black Kingside Rook

		//Pawns
		for(int col = 0; col<8; col++){
			board[col][6] =  6;	//White
			board[col][1] = -6; //Black
		}

		printBoard();
	}

	public void startGame(){
		turn = 1;
	}
	//return 1 if able to make the move
	public int tryMove(int fromX, int fromY, int destX, int destY){
		int piece = board[fromX][fromY];
		int destSq = board[destX][destY];
		print("Trying move:" +game.files[fromX] + (8-fromY) + " to " + game.files[destX] + (8-destY));

		//preMove
		//is the square occupied by own piece or enemy king
		if ( (piece < 0 && destSq < 0) || (piece > 0 && destSq > 0) ) return 0;
		else if (Math.abs(destSq) == 1) return 0; //?!?! Peculiar, never in position to take their king if not check already = mate

		//consider pre calculating abs(fromX-destX) and Y - only pawns care about direction
		int magDifX = Math.abs(fromX - destX);
		int magDifY = Math.abs(fromY - destY);

		//can the piece land on that square based on how it moves?
		switch(Math.abs(piece)){
			case 1: //King
				if(magDifX > 1 || magDifY > 1) return 0;
			break;

			case 2: //Queen
				if((magDifX == magDifY) || //diagonal allowed
						(magDifX == 0 && magDifY !=0) || //vertical movement allowed
							(magDifY == 0 && magDifX != 0) ) ; // horizontal movement allowed
				else return 0; //everything else is not allowed
			break;

			case 3: //Bishop
				if(magDifX != magDifY) return 0;
			break;

			case 4: //Knight
				//combination of 2 and 1
				//dont go running too far
				if(magDifX > 2 || magDifY > 2) return 0;
				//dont go running too close
				if(magDifX < 2 && magDifY < 2) return 0;
				//therefore total ==3 means combo of 2 and 1
				if(magDifX + magDifY != 3) return 0;
			break;

			case 5: //Rook
				if(magDifX != 0 && magDifY != 0) return 0; 
			break;

			case 6: //Pawn
				//movement always forwards
				if ( (turn > 0 && fromY-destY > 0) || (turn < 0 && fromY-destY < 0) ) ;
				else return 0;

				//certainly cant be going sideways >1 or forward >2
				if(magDifX > 1 || magDifY > 2) return 0;

				//check initial double move
				if(magDifY == 2) {
					if(turn > 0 && fromY != 6) return 0;
					if(turn < 0 && fromY != 1) return 0;
					if(destSq != 0) return 0;
				}

				//capture
				if(magDifX == 1){
					if(turn > 0 && destSq > 0) return 0; //cant capture own piece
					if(turn < 0 && destSq < 0) return 0;

					//will be en passant later!
					//en passant!!? shitttt gonna need to know previous move
					if(destSq == 0) return 0;
				}
				//must capture diagonally by 1
				if(destSq != 0 && magDifX != 1) return 0;
				
			break;
		}

		
		//select absoulte apart from pawn, note that black is negative which is neg rows
		//nothing blocking its way?

		//after piece moves
		//own team in check?
		makeMove(fromX,fromY,destX,destY);
		return 1;
	}	

	public void betweenMoveChecks(){
		//check?
		//checkmate?
		//stalemate?
		//promotion?
	}

	public void makeMove(int x1, int y1, int x2, int y2){
		//update board
		int piece = board[x1][y1];
		//put it where it's going
		board[x2][y2] = piece;
		//remove piece from where it was
		board[x1][y1] = 0;
		//flip turn
		turn = -turn;
	}

	//perhaps pass it a board in particular?
	void printBoard(){
		System.out.println("|------------------------------|");

		for(int i = 0; i<8; i++){
			System.out.print("|");
			for(int j=0; j<8; j++){
				if(board[j][i] < 0) System.out.print(board[j][i] + "| ");
				else System.out.print(" " + board[j][i] + "| ");
			}
			//System.out.println("\n--- --  --  --  --  --  --  --  ");

			System.out.println("\n|------------------------------|");
		}
		//System.out.println("-------------------------------");

	}

	void print(String text){
		System.out.print(text);
	}
	void println(String text){
		System.out.println(text);
	}
}












