/*
TODO
current:
	SAY WHEN ITS CHECKMATE. IT TAKES THE FUN OUT OF IT OTHERWISE. UNFINISHED.
	Bugs:
	DONE King can move into pawn check!?
	DONE can castle out of check atm
	DONE castling can be done with knight there! queenside
	en passant not working?! 

	highlighting past move in cyan still highlights when pinned, or when otherwise in check
	- consider a more pretty highlighting move, a small cyan cicle for example, will allow for showing possible moves...
	consider remove piece function - would allow handling of removed pieces!

	Post movement checks:
	checkmate... (king cant move and *nothing block*)
	stalemate

	write the letters and numbers on the board
	Draws!?
	three move repition
	offer draw?! lol
	not enough material for m8

	Potential features
	undo most recent move
	print move with correct notation - keep match resume?
	highlight possible moves?
	highlight last move?
	track captured pieces?
	save game and load?
	flip board visually
	debug flag

	Difficult rules:
	DONE castling
	DONE pawn promotion
	DONE en passant
		

*/
import java.awt.Point;
import java.io.Console;
public class chess {

	static chess game;
	static chessGUI gui;
	Console c;
	int[][] board = new int[8][8];
	String[] files = {"a","b","c","d","e","f","g","h"};
	int turn; //1 white, -1 black

	Point[] lastMovePos = {new Point(-1,-1) , new Point(-1,-1) } ;
	int lastMovePiece = 0 ;
	//track kings for checking checks
	Point whiteKingPos = new Point(4,7);
	Point blackKingPos = new Point(4,0);

	//flags to check if King/Queenside castling possible
	boolean whiteKSideCastlePoss = true;
	boolean whiteQSideCastlePoss = true;
	boolean blackKSideCastlePoss = true;
	boolean blackQSideCastlePoss = true;
	
	public static void main(String args[]){
		System.out.println("Welcome to Louis' Chess. Please do check it out, mate!");
		game = new chess();
		gui = new chessGUI(game);
	}

	public chess(){
		setupBoard();
		startGame();

		c = System.console();
		if (c == null) {
            System.err.println("No console.");
            System.exit(1);
        }
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
		println("Trying move:" +game.files[fromX] + (8-fromY) + " to " + game.files[destX] + (8-destY));

		//preMove
		//is the square occupied by own piece
		if ( (piece < 0 && destSq < 0) || (piece > 0 && destSq > 0) ) return 0;

		//check that the piece could move there
		if( pieceMovementValidation(fromX,fromY,destX,destY) == 0) return 0;

		//nothing blocking its way if not knight
		if(Math.abs(piece) != 4) {
			if ( checkBlock(fromX, fromY, destX, destY) == 0 ) return 0;
		}

		//check castling
		if(pieceMovementValidation(fromX,fromY,destX,destY) == -1){
			println("attempting to castle...");
			if( validateCastling(destX,destY) == 0) {
				print("Can't Castle!");
				return 0;
			}
		}

		//en passant?
		if(pieceMovementValidation(fromX,fromY,destX,destY) == -2){
			println("en passant?!");
			if( validateEnPassant(fromX,fromY,destX,destY) == 0){
				return 0;
			}
		}

		//after piece moves
		movePiece(fromX,fromY,destX,destY);

		//own team in check? if yes, undo move and break out of function
		if(teamInCheck(turn) == 1) {
			movePiece(destX,destY,fromX,fromY);
			//if capture, replace piece
			if(destSq != 0) board[destX][destY] = destSq;
			return 0;
		}

		flipGlobalTurn();
		betweenMoveChecks();
		println("move successful!");
		//printBoard();
		return 1;
	}
	//returns 1 if en passant, 0 if not;
	int validateEnPassant(int fromX, int fromY, int destX, int destY){
		//last move piece must be pawn double move,
		//ps we already know we're currently moving a pawn
		if(Math.abs(lastMovePos[0].y - lastMovePos[1].y) == 2 &&  destX == lastMovePos[0].x){
			if(Math.abs(lastMovePiece) == 6){
				println("en passant last moved piece was a pawn!");
				board[lastMovePos[1].x][lastMovePos[1].y] = 0;
				return 1;
			}
		}
		else return 0;
		//got to be attacking correct column
		return 1;
	}

	//returns 0 if not poss, 1 if poss
	//technically moves rook before king, means king gets set as last move
	int validateCastling(int destX, int destY){
		//boolean flags will have caught already if:
		//king has moved
		//or rook has moved or captured
		
		//cant castle if in check
		if(teamInCheck(turn) == 1) return 0;

		//if can then check if any enemy piece can touch journey square
		int temp = 0; //allows to return king position before acting on result

		//white Kingside castle
		if(destX == 6 && destY == 7) {
			if(whiteKSideCastlePoss) {
				//fake king position and check if team in check, then move him back
				whiteKingPos = new Point(6,7);
				temp += teamInCheck(1);
				whiteKingPos = new Point(5,7);
				temp += teamInCheck(1);
				whiteKingPos = new Point(4,7);
				if(temp > 0) return 0;
				else {
					movePiece(7,7, 5,7);
					print("white King side castle");
				}
			}
			else return 0;
		}

		//white Queenside castle
		if(destX == 2 && destY == 7){
			if(whiteQSideCastlePoss) {
				//fake king position and check if team in check, then move him back
				whiteKingPos = new Point(2,7);
				temp += teamInCheck(1);
				whiteKingPos = new Point(3,7);
				temp += teamInCheck(1);
				whiteKingPos = new Point(4,7);
				if(temp > 0) return 0;
				//rook cant be blocked either
				if(board[1][7] != 0) return 0;
				else {
					movePiece(0,7, 3,7);
					println("white Queen side castle");
				}
			}
			else return 0;
		}

		//black Kingside castle
		if(destX == 6 && destY == 0){
			if(blackKSideCastlePoss) {
				//fake king position and check if team in check, then move him back
				blackKingPos = new Point(6,0);
				temp += teamInCheck(-1);
				blackKingPos = new Point(5,0);
				temp += teamInCheck(-1);
				blackKingPos = new Point(4,0);
				if (temp > 0) return 0;
				else {
					movePiece(7,0, 5,0);
					println("black King side castle");
				}
			}
			else return 0;
		}

		//black Queenside castle
		if(destX == 2 && destY == 0){
			if(blackQSideCastlePoss) {
				//fake king position and check if team in check, then move him back
				blackKingPos = new Point(2,0);
				temp += teamInCheck(-1);
				blackKingPos = new Point(3,0);
				temp += teamInCheck(-1);
				blackKingPos = new Point(4,0);
				if (temp > 0) return 0;
				//rook cant be blocked either
				if (board[1][0] != 0) return 0;
				else {
					movePiece(0,0, 3,0);
					println("black Queen side castle");
				}
			}
			else return 0;
		}
		println(" success!");
		return 1;
	}

	//returns 0 if piece can't move there, 1 if can, 
	// -1 if castling attempt, -2 if potential en passant
	public int pieceMovementValidation (int fromX, int fromY, int destX, int destY){
		int piece = board[fromX][fromY];
		int destSq = board[destX][destY];


		//pre calculating abs(fromX-destX) and Y - only pawns care about direction
		int magDifX = Math.abs(fromX - destX);
		int magDifY = Math.abs(fromY - destY);

		switch(Math.abs(piece)){
			case 1: //King
				if(magDifX == 2 && magDifY == 0) {
					if(fromX == 4){
						if(fromY == 0 || fromY == 7){
							return -1;
						}
					} //can only castle from home square
					else return 0;
				}
				else if(magDifX > 1 || magDifY > 1) return 0;
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
				if ( (piece > 0 && fromY-destY > 0) || (piece < 0 && fromY-destY < 0) ) ;
				else return 0;

				//certainly cant be going sideways >1 or forward >2 or forward by 0
				if(magDifX > 1 || magDifY > 2 || magDifY < 1) return 0;

				//check initial double move
				if(magDifY == 2) {
					if(turn > 0 && fromY != 6) return 0;
					if(turn < 0 && fromY != 1) return 0;
					if(destSq != 0) return 0;
				}

				//capture
				if(magDifX == 1){
					//cant capture own piece
					//if(turn > 0 && destSq > 0) return 0; 
					//if(turn < 0 && destSq < 0) return 0;
					if(piece * destSq > 0) return 0;

					//will be en passant later!
					//en passant!!? shitttt gonna need to know previous move
					if(destSq == 0) {
						if(destY == 2 && turn > 0){
							//white enpassant attempt
							return -2;
						}
						else if(destY == 5 && turn < 0){
							//black en passant attempt
							return -2;
						}
						else return 0;
					}
				}
				//must capture diagonally by 1
				if(destSq != 0 && magDifX != 1) return 0;
				
			break;
		}
		return 1;
	}

	//returns 0 if there is a piece blocking, 1 if not
	public int checkBlock(int x1, int y1, int x2, int y2)
	{
		//technically NESW precision doesnt matter but cba to fiddle with y1y2x1x2
		//println("checking block move [x1,y1] -> [x2,y2]: ["+x1+","+y1+"]" + "["+x2+","+y2+"]");
		//vertical movement
		if(x2-x1 == 0){
			//northward movement
			if(y2<y1){
				for(int j=y1-1; j>y2; j--){
					if(board[x1][j] != 0) return 0;
				}
				
			}
			//southward movement
			else {
				for(int j=y1+1; j<y2; j++){
					if(board[x1][j] != 0) return 0;
				}
			}
		}

		//horizontal movement
		if(y2-y1 == 0){
			//eastward movement
			if(x2>x1){
				for(int i=x1+1; i<x2; i++){
					if(board[i][y1] != 0) return 0;
				}
			}
			//westward movement
			else {
				for(int i=x1-1; i>x2; i--){
					if(board[i][y1] != 0) return 0;
				}
			}
		}

		//diagonal movement
		//uses counter instead of nested for loops to check a line as opposed to square
		if (Math.abs(x2-x1) == Math.abs(y2-y1)){
			//NE eg: 0,5 -> 3,2 should check 1,4 and 2,3
			if(y2<y1 && x2>x1){
				int yCounter = y1-1;
				for(int i = x1+1; i<x2; i++){
					if(board[i][yCounter] != 0) return 0;
					yCounter--;
				}
			}
			//SE
			if(y2>y1 && x2>x1){
				int yCounter = y1+1;
				for(int i = x1+1; i<x2; i++){
					if(board[i][yCounter] != 0) return 0;
					yCounter++;
				}
			}
			//SW
			if(y2>y1 && x2<x1){
				int yCounter = y1+1;
				for(int i = x1-1; i>x2; i--){
					if(board[i][yCounter] != 0) return 0;
					yCounter++;
				}
			}
			//NW
			if(y2<y1 && x2<x1){
				int yCounter = y1-1;
				for(int i = x1-1; i>x2; i--){
					if(board[i][yCounter] != 0) return 0;
					yCounter--;
				}
			}
		}
		//else print sth
		return 1;
	}

	//returns 1 if moving team in check after move, 0 if not
	public int teamInCheck(int turn){
		Point kingPos = null;

		//decide which king we're looking at		
		if(turn > 0) kingPos = whiteKingPos;
		if(turn < 0) kingPos = blackKingPos;
		if(kingPos == null) println("King position not found wtf");

		boolean inCheck = false;
		//check every square for a enemy piece
		for(int i=0;i<8;i++){
			for(int j=0;j<8;j++){
				if(board[i][j] * turn < 0) {
					//1*1 = 1, 0*1 = 0, 0*0 = 0, but -1*1 < 0, so only checks enemy pieces
					println("teamInCheck:");
					int result = pieceMovementValidation(i,j,kingPos.x,kingPos.y) * checkBlock(i,j,kingPos.x,kingPos.y);
					println("piece mov valid = " + pieceMovementValidation(i,j,kingPos.x,kingPos.y) );
					if(pieceMovementValidation(i,j,kingPos.x,kingPos.y) == 1){
						print("piece = " + board[i][j]);
					}
					println("check block = " + checkBlock(i,j,kingPos.x,kingPos.y) );
					println("result = " + result);
					if (result == 1) inCheck = true;
				}
			}
		}
		if(inCheck)	return 1;
		else return 0;
	}
	//returns 1 if play continues, 0 otherwise (stale or checkmate)
	//note that turn is flipped by this point
	public int betweenMoveChecks(){
		//promotion?
		pawnPromotionCheck();
		//check?
		if(teamInCheck(turn) == 1){
			println("Check!");
		}
		//checkmate?
		//stalemate?
		return 1;
	}

	void pawnPromotionCheck(){
		for(int i=0;i<8;i++){
			if(board[i][0] == 6){
				//white pawn promotion!
				board[i][0] = promotePawn();
			}
			if(board[i][7] == -6){
				//black pawn promotion!
				board[i][7] = -1 * promotePawn();
			}
		}
	}
	//returns value (not colour!) of which piece chosen to replace with
	int promotePawn(){
		//1 Queen 2 Bishop 3 Knight 4 Rook
		//ask player what they want in form of number
		println("Pawn promotion! What would you like?");
		boolean pieceChosen = false;
		int newPiece = 0;
		do{
			String answer = c.readLine("Enter 1 for a Queen, 2 for a Bishop, 3 for a Knight or 4 for a Rook.");
			if(answer.equals("1") || answer.equals("2") || answer.equals("3") || answer.equals("4")){
				newPiece = 1+ Integer.parseInt(answer);
				pieceChosen = true;
			} 
			else{
				println("PSYCH! That's the wrong numba!");
			}
		} while(!pieceChosen);

		if(newPiece != 0) return newPiece;
		else {
			println("Pawn promotion error system meltdown!!!");
			return -1;
		}
	}

	public void movePiece(int x1, int y1, int x2, int y2){
		//update board
		int piece = board[x1][y1];
		lastMovePiece = piece;

		//if piece is king update global point
		//once king has moved (even if to castle) castling no longer possible
		//if castling then move rook also!
		if(piece ==  1) {
			whiteKingPos = new Point (x2,y2);
			whiteKSideCastlePoss = false;
			whiteQSideCastlePoss = false;
		}
		if(piece == -1) {
			blackKingPos = new Point (x2,y2);
			blackKSideCastlePoss = false;
			blackQSideCastlePoss = false;
		}

		//if any corner square implicated in moving, prevents castling that side
		if( (x1 == 0 && y1 == 0) || (x2 == 0 && y2 == 0) ) blackQSideCastlePoss = false;
		if( (x1 == 7 && y1 == 0) || (x2 == 7 && y2 == 0) ) blackKSideCastlePoss = false;
		if( (x1 == 0 && y1 == 7) || (x2 == 0 && y2 == 7) ) whiteQSideCastlePoss = false;
		if( (x1 == 7 && y1 == 7) || (x2 == 7 && y2 == 7) ) whiteKSideCastlePoss = false;


		//put it where it's going
		board[x2][y2] = piece;
		//remove piece from where it was
		board[x1][y1] = 0;
		Point from = new Point(x1,y1);
		Point to = new Point(x2,y2);

		lastMovePos[0] = from;
		lastMovePos[1] = to  ;
	}

	void flipGlobalTurn(){

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












