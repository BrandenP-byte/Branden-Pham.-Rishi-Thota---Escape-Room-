import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Image;
import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.JFrame;

import java.io.File;
import javax.imageio.ImageIO;

import java.util.Random;

/**
 * A Game board on which to place and move players.
 * 
 * @author PLTW
 * @version 1.0
 */
public class GameGUI extends JComponent
{
  static final long serialVersionUID = 141L;

  private static final int WIDTH = 510;
  private static final int HEIGHT = 360;
  private static final int SPACE_SIZE = 60;
  private static final int GRID_W = 8;
  private static final int GRID_H = 5;
  private static final int START_LOC_X = 15;
  private static final int START_LOC_Y = 15;
  
  //score stuff
  private int score = 0;
  private boolean canMove = true;
  private Image trapImage;
  private Rectangle[] coins;
  private int totalCoins;
  private Rectangle trap;

  // initial placement of player
  int x = START_LOC_X; 
  int y = START_LOC_Y;

  // grid image to show in background
  private Image bgImage;

  // player image and info
  private Image player;
  private Point playerLoc;
  private int playerSteps;

  // walls
  private int totalWalls;
  private Rectangle[] walls; 
  private Image prizeImage;
  private int totalTraps;
  private Rectangle[] traps;
  private boolean isImmune;

  // scores, sometimes awarded as (negative) penalties
  private int prizeVal = 10;
  private int trapVal = 5;
  private int endVal = 10;
  private int offGridVal = 5; // penalty only
  private int hitWallVal = 5;  // penalty only

  // game frame
  private JFrame frame;

  /**
   * Constructor for the GameGUI class.
   * Creates a frame with a background image and a player that will move around the board.
   */
  public GameGUI()
  {
    // load images, student can customize these images by changing files on disk
    try {
      trapImage = ImageIO.read(new File("barrier.png"));
    } catch (Exception e) {
      System.err.println("Could not open file barrier.png");
    }
    try {
      bgImage = ImageIO.read(new File("grid.png"));      
    } catch (Exception e) {
      System.err.println("Could not open file grid.png");
    }      
    try {
      prizeImage = ImageIO.read(new File("coin.png"));      
    } catch (Exception e) {
      System.err.println("Could not open file coin.png");
    }
    try {
      player = ImageIO.read(new File("player.png"));      
    } catch (Exception e) {
     System.err.println("Could not open file player.png");
    }
    // save player location
    playerLoc = new Point(x,y);

    // create the game frame
    frame = new JFrame();
    frame.setTitle("EscapeRoom");
    frame.setSize(WIDTH, HEIGHT);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(this);
    frame.setVisible(true);
    frame.setResizable(false); 

    // set default config
    totalWalls = 20;
    totalTraps = 5;
    
    frame.addKeyListener(new java.awt.event.KeyAdapter() {
      public void keyPressed(java.awt.event.KeyEvent e) {
        if (!canMove) return;
        int key = e.getKeyCode();
        if (key == java.awt.event.KeyEvent.VK_LEFT) {
          movePlayer(-SPACE_SIZE, 0);
        } else if (key == java.awt.event.KeyEvent.VK_RIGHT) {
          movePlayer(SPACE_SIZE, 0);
        } else if (key == java.awt.event.KeyEvent.VK_UP) {
          movePlayer(0, -SPACE_SIZE);
        } else if (key == java.awt.event.KeyEvent.VK_DOWN) {
          movePlayer(0, SPACE_SIZE);
        }
        else if (key == java.awt.event.KeyEvent.VK_SPACE) {
          System.out.println("Stepped on trap safely! You are immune for 2 seconds.");
          isImmune = true;
          if (trap.contains(x + 20, y + 20)) {
            // Allow stepping on trap without penalty or being trapped for 2 seconds
            canMove = true;
            new javax.swing.Timer(2000, evt -> {
              canMove = true;
              System.out.println("Immunity ended. You can move again!");
              ((javax.swing.Timer)evt.getSource()).stop();
            }).start();
          }
          }
      }
    });
  }

  /**
   * After a GameGUI object is created, this method adds the walls, prizes, and traps to the gameboard.
   * Note that traps and prizes may occupy the same location.
   */
  public void createBoard()
  {
    Random rand = new Random();
    int s = SPACE_SIZE;
    totalCoins = rand.nextInt(2) + 2; // 2 or 3 coins
    coins = new Rectangle[totalCoins];
    for (int i = 0; i < totalCoins; i++) {
      coins[i] = new Rectangle(rand.nextInt(GRID_W)*s + 15, rand.nextInt(GRID_H)*s + 15, 15, 15);
    }
    trap = new Rectangle(rand.nextInt(GRID_W)*s + 15, rand.nextInt(GRID_H)*s + 15, 40, 40);
    walls = new Rectangle[totalWalls];
    createWalls();
  }

  /**
   * Increment/decrement the player location by the amount designated.
   * This method checks for bumping into walls and going off the grid,
   * both of which result in a penalty.
   */
  public int movePlayer(int incrx, int incry)
  {
    if (!canMove) return 0;
    int newX = x + incrx;
    int newY = y + incry;
    
    playerSteps++;

    // check if off grid horizontally and vertically
    if ( (newX < 0 || newX > WIDTH-SPACE_SIZE) || (newY < 0 || newY > HEIGHT-SPACE_SIZE) )
    {
      System.out.println ("OFF THE GRID!");
      return -offGridVal;
    }

    // wall collision
    for (Rectangle r: walls)
    {
      int startX =  (int)r.getX();
      int endX  =  (int)r.getX() + (int)r.getWidth();
      int startY =  (int)r.getY();
      int endY = (int) r.getY() + (int)r.getHeight();

      if ((incrx > 0) && (x <= startX) && (startX <= newX) && (y >= startY) && (y <= endY))
      {
        System.out.println("A WALL IS IN THE WAY");
        return -hitWallVal;
      }
      else if ((incrx < 0) && (x >= startX) && (startX >= newX) && (y >= startY) && (y <= endY))
      {
        System.out.println("A WALL IS IN THE WAY");
        return -hitWallVal;
      }
      else if ((incry > 0) && (y <= startY && startY <= newY && x >= startX && x <= endX))
      {
        System.out.println("A WALL IS IN THE WAY");
        return -hitWallVal;
      }
      else if ((incry < 0) && (y >= startY) && (startY >= newY) && (x >= startX) && (x <= endX))
      {
        System.out.println("A WALL IS IN THE WAY");
        return -hitWallVal;
      }     
    }

    // all is well, move player
    x += incrx;
    y += incry;
    playerLoc.setLocation(x, y);

    // Check coin collision for all coins
    for (int i = 0; i < totalCoins; i++) {
      if (coins[i].contains(x + 10, y + 10)) { // center of player
      // Every 5th coin is worth 2 points
      if ((score + 1) % 5 == 0) { 
        score += 2;
        System.out.println("Bonus! 5th coin worth 2 points.");
      } else {
        score++;
      }
        System.out.println("Score: " + score);
        // Move coin to new random location
        Random rand = new Random();
        coins[i].setLocation(rand.nextInt(GRID_W)*SPACE_SIZE + 15, rand.nextInt(GRID_H)*SPACE_SIZE + 15);
      }
    }

    // Check trap collision
    if (trap.contains(x + 20, y + 20)) {
     if (!isImmune){
      score = Math.max(0, score - 1);
      System.out.println("Hit trap! Score: " + score);
      canMove = false;
     }

      // Move trap to new random location
      Random rand = new Random();
      trap.setLocation(rand.nextInt(GRID_W)*SPACE_SIZE + 15, rand.nextInt(GRID_H)*SPACE_SIZE + 15);
      // Timer to re-enable movement after 3 seconds
      new javax.swing.Timer(3000, evt -> {
        canMove = true;
        System.out.println("You can move again!");
        ((javax.swing.Timer)evt.getSource()).stop();
      }).start();
    }

    repaint();   
    return 0;   
  }

  public int getSteps()
  {
    return playerSteps;
  }
  
  public void setCoins(int p) 
  {
    totalCoins = p;
  }
  
  public void setTraps(int t) 
  {
    totalTraps = t;
  }
  
  public void setWalls(int w) 
  {
    totalWalls = w;
  }

  public int replay()
  {
    int win = playerAtEnd();
    for (Rectangle p: coins)
      p.setSize(SPACE_SIZE/3, SPACE_SIZE/3);
    // move player to start of board
    x = START_LOC_X;
    y = START_LOC_Y;
    playerSteps = 0;
    repaint();
    return win;
  }

  public int endGame() 
  {
    int win = playerAtEnd();
    setVisible(false);
    frame.dispose();
    return win;
  }

  /** 
   * For internal use and should not be called directly: Users graphics buffer to paint board elements.
   */
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D)g;

    // draw grid
    g.drawImage(bgImage, 0, 0, null);

    // draw all coins
    for (int i = 0; i < totalCoins; i++) {
      int coinX = (int)coins[i].getX();
      int coinY = (int)coins[i].getY();
      g.drawImage(prizeImage, coinX, coinY, 15, 15, null);
    }

    // draw trap
    int trapX = (int)trap.getX();
    int trapY = (int)trap.getY();
    g.drawImage(trapImage, trapX, trapY, 40, 40, null);

    // add walls
    for (Rectangle r : walls) 
    {
      g2.setPaint(Color.BLACK);
      g2.fill(r);
    }
   
    // draw player, saving its location
    g.drawImage(player, x, y, 40,40, null);
    playerLoc.setLocation(x,y);
  }

  private void createCoins()
  {
    int s = SPACE_SIZE; 
    Random rand = new Random();
    for (int numCoins = 0; numCoins < totalCoins; numCoins++)
    {
      int h = rand.nextInt(GRID_H);
      int w = rand.nextInt(GRID_W);
      Rectangle r = new Rectangle((w*s + 15),(h*s + 15), 15, 15);
      coins[numCoins] = r;
    }
  }

  private void createWalls()
  {
    int s = SPACE_SIZE; 
    Random rand = new Random();
    for (int numWalls = 0; numWalls < totalWalls; numWalls++)
    {
      int h = rand.nextInt(GRID_H);
      int w = rand.nextInt(GRID_W);
      Rectangle r;
      if (rand.nextInt(2) == 0) 
      {
        // vertical wall
        r = new Rectangle((w*s + s - 5),h*s, 8,s);
      }
      else
      {
        // horizontal
        r = new Rectangle(w*s,(h*s + s - 5), s, 8);
      }
      walls[numWalls] = r;
    }
  }

  private int playerAtEnd() 
  {
    int score;
    double px = playerLoc.getX();
    if (px > (WIDTH - 2*SPACE_SIZE))
    {
      System.out.println("YOU MADE IT!");
      score = endVal;
    }
    else
    {
      System.out.println("OOPS, YOU QUIT TOO SOON!");
      score = -endVal;
    }
    return score;
  }
}
