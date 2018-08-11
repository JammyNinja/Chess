import javax.swing.*; //JPanel/containers/scrollpane
import java.awt.*; //Dimension/colour/graphics
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
public class chessGUI extends JPanel
					implements KeyListener, MouseListener
{
	//colours
	Color lightSqColour = Color.WHITE;
	Color darkSqColour = Color.BLACK;
	Color whitePieceColour = Color.RED;
	Color blackPieceColour = Color.BLUE;

	chess game;
	int boardWidth, boardHeight; 
	int sqSize, pieceSize, pieceGap;

	//move making variables
	int currentPiece;
	int moveFromX, moveFromY;

	public chessGUI (chess game){
		this.game = game;

		Frame f = new Frame(); //class at bottom of file

		initialiseGUI(f);
	}

	public void initialiseGUI(Frame f){
		setPreferredSize(new Dimension(f.frameWidth, f.frameHeight) );
		boardWidth = f.frameWidth;
		boardHeight = f.frameHeight;
		sqSize = (boardWidth + boardHeight) / 2 / 8; //average /8 
		pieceGap = sqSize / 10;
		pieceSize = sqSize - 2 * pieceGap; //oof hardcode

		//get wet for input
		addKeyListener(this);
		setFocusable(true);
		addMouseListener(this);


		f.initialise(this);
	}

	public void paint(Graphics g){
		//g.setColor(darkSqColour);
		g.setFont(new Font("Monospaced", Font.PLAIN, sqSize/2) ); //for king hell
		paintBoard(g);
		paintPieces(g);
	}

	public void paintBoard(Graphics g){
		g.setColor(lightSqColour);
		for(int i = 0; i<=8; i++){
			for(int j=0; j<=8; j++){
				if(currentPiece != 0 && moveFromX == i && moveFromY == j){
					g.setColor(Color.CYAN);
				}
				else if((i+j) %2 ==1) g.setColor(darkSqColour);
				else g.setColor(lightSqColour);
				
				g.fillRect(i*sqSize,j*sqSize, sqSize,sqSize);
			}
		}
	}

	public void paintPieces(Graphics g){
		g.setFont(new Font("Monospaced", Font.PLAIN, sqSize/2) );
		//go through board and paint appropriate piece
		for(int i = 0; i<8; i++){
			for(int j=0; j<8; j++){
				int piece = game.board[i][j];
				//until source images
				if(piece > 0) g.setColor(whitePieceColour);
				if(piece < 0) g.setColor(blackPieceColour);
				if(piece != 0) {
					g.fillOval(i*sqSize + pieceGap, j*sqSize + pieceGap, pieceSize, pieceSize);
					g.setColor(Color.YELLOW);
					switch(Math.abs(piece)){
						case 1: //King K
							g.drawString("K", i*sqSize + sqSize/3, j*sqSize +  2 * sqSize /3 );
						break;
						case 2: //Queen Q
							g.drawString("Q", i*sqSize + sqSize/3, j*sqSize +  2 * sqSize /3 );
						break;
						case 3: //Bishop B
							g.drawString("B", i*sqSize + sqSize/3, j*sqSize +  2 * sqSize /3 );
						break;
						case 4: //Knight N
							g.drawString("N", i*sqSize + sqSize/3, j*sqSize +  2 * sqSize /3 );
						break;
						case 5: //Rook R
							g.drawString("R", i*sqSize + sqSize/3, j*sqSize +  2 * sqSize /3 );
						break;
						case 6: //Pawn p?
							g.drawString("p", i*sqSize + sqSize/3, j*sqSize +  2 * sqSize /3 );
						break;
					}
				}
			}
		}
	}

	public void mouseClicked(MouseEvent m){
		int x = m.getX() / sqSize;
		int y = m.getY() / sqSize;
		game.print("Clicked square: ["+ x +"]["+ y +"].");
		game.println(" aka " + game.files[x] + (8-y) );
		int piece = game.board[x][y];
		//game.print("piece = " + piece);
		//if already selected a piece
		if(currentPiece != 0) { //about to select destination
			if (x == moveFromX && y == moveFromY){ //double clicked piece
				currentPiece = 0; //unhighlight piece
			}
			else { //trying a move
				int success = game.tryMove(moveFromX, moveFromY, x, y);
				if (success == 1) currentPiece = 0;
			}
		}
		//if selecting piece to move
		else {
			//cant start with an empty square
			if(piece != 0) {
				if ( (piece > 0 && game.turn > 0) || (piece < 0 && game.turn < 0) ){
					moveFromX = x;
					moveFromY = y;
					currentPiece = piece;
				}
			}
		}

		/* printing pieces
		if (piece > 0) System.out.print("White ");
		else if (piece < 0) System.out.print("Black ");

		switch(Math.abs(piece)){
		case 1: //King K
			game.print("King");
		break;
		case 2: //Queen Q
			game.print("Queen");
		break;
		case 3: //Bishop B
			game.print("Bishop");
		break;
		case 4: //Knight N
			game.print("Knight");
		break;
		case 5: //Rook R
			game.print("Rook");
		break;
		case 6: //Pawn p?
			game.print("Pawn");
		break;
		}
		*/
		//game. println("Current piece: " + currentPiece );
		repaint();
	}

	public void keyPressed(KeyEvent e){
		switch(e.getKeyCode()){
			case KeyEvent.VK_ESCAPE:
				System.exit(0);
			break;
		}
	}


	//UNUSED listener functions
	public void keyReleased(KeyEvent e){}
	public void keyTyped(KeyEvent e){}
	public void mouseExited(MouseEvent m){}
	public void mouseEntered(MouseEvent m){}
	public void mouseReleased(MouseEvent m){}
	public void mousePressed(MouseEvent m){}
}



class Frame extends JFrame {
	static int frameWidth = 760; //windowWidth = 1000;
	static int frameHeight = 760; //windowHeight = 600;  

	public Frame(){
		setTitle("Chess");
		setSize(frameWidth,frameHeight);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void initialise(chessGUI guiPanel)
	{
		setLayout(new GridLayout(1,1));
		add(guiPanel);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
}