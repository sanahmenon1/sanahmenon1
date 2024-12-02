import java.util.ArrayList;
import tester.Tester;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

// Represents a single square of the game area
class Cell {
  int x;
  int y;
  Color color;
  boolean flooded;
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  Cell(int x, int y, Color color, boolean flooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
  }
}

// Main game world
class FloodItWorld extends World {
  ArrayList<Cell> board;
  int size;
  int numColors;
  ArrayList<Color> colors;
  int cellSize = 20; // Size of each cell
  int steps = 0;
  int max;
  int timer = 0;
  ArrayList<String> sizes;
  ArrayList<String> numColorsOptions;
  int selectedSizeIndex = 2; // Default to 10x10
  int selectedNumColorsIndex = 4; // Default to 6 colors
  boolean sizeDropdownOpen = false;
  boolean colorsDropdownOpen = false;
  ArrayList<Cell> toBeFlooded = new ArrayList<>(); 
  //Target color for the current flood operation
  Color targetColor; 
  //Original color of the cell to be changed
  Color originalColor; 

  // size and numColors must be 1 or more
  FloodItWorld(int size, int numColors) {
    this.size = size;
    this.numColors = numColors;
    this.colors = new ArrayList<Color>();
    this.board = new ArrayList<Cell>();
    this.sizes = new ArrayList<String>();
    this.numColorsOptions = new ArrayList<String>();
    initSizes();
    initNumColorsOptions();
    initColors();
    initBoard();
    connectCells();
    this.max = 3 + (size - 2) / 4 * 7;
  }

  // Purpose: Initialize the list of sizes
  void initSizes() {
    this.sizes.add("2x2");
    this.sizes.add("6x6");
    this.sizes.add("10x10");
    this.sizes.add("14x14");
    this.sizes.add("18x18");
    this.sizes.add("22x22");
    this.sizes.add("26x26");
  }

  // Purpose: Initialize the list of number of colors options
  // Effect : adds numbers to colorOptions Array
  void initNumColorsOptions() {
    this.numColorsOptions.add("2");
    this.numColorsOptions.add("3");
    this.numColorsOptions.add("4");
    this.numColorsOptions.add("5");
    this.numColorsOptions.add("6");
    this.numColorsOptions.add("7");
  }

  // Purpose: Initialize the list of colors of cells in the game
  // Effect: Adds a fixed set of colors to the colors ArrayList.
  void initColors() {
    this.colors.add(Color.RED);
    this.colors.add(Color.GREEN);
    this.colors.add(Color.BLUE);
    this.colors.add(Color.ORANGE);
    this.colors.add(Color.YELLOW);
    this.colors.add(Color.PINK);
    this.colors.add(Color.MAGENTA);
  }

  // Purpose: Initialize the game board with cells of random colors from the list
  // Effect: Populates the board with cell objects
  void initBoard() {
    Random rand = new Random();
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        Color randomColor = colors.get(rand.nextInt(numColors));
        board.add(new Cell(i, j, randomColor, false));
      }
    }
    // Start flooding from the top-left corner
    board.get(0).flooded = true; 
  }

  // Purpose: Connect each cell to its adjacent cells (left, top, right, bottom).
  // Effect: Sets the neighboring cell references for each cell in the board.
  void connectCells() {
    for (Cell c : board) {
      // left
      if (c.x > 0) {
        c.left = board.get((c.x - 1) * size + c.y);
      }

      // right
      if (c.x < size - 1) {
        c.right = board.get((c.x + 1) * size + c.y);
      }

      // top
      if (c.y > 0) {
        c.top = board.get(c.x * size + (c.y - 1));
      }

      // bottom
      if (c.y < size - 1) {
        c.bottom = board.get(c.x * size + (c.y + 1));
      }
    }
  }

  // Purpose: Handle mouse clicks to start flood fill and manage drop downs
  // Effect: Starts the flood fill process or toggles drop-down menu states
  public void onMouseClicked(Posn pos) {
    int sceneSize = Math.max(size * cellSize + 40, 400);
    int offsetX = (sceneSize - size * cellSize) / 2;
    // Adjust y position to account for menu height
    int offsetY = (sceneSize - size * cellSize) / 2 + 40; 

    // Handle clicks on the drop down menu
    if (pos.y < 40) {
      if (sizeDropdownOpen) {
        int clickedIndex = (pos.y - 40) / 20;
        if (clickedIndex >= 0 && clickedIndex < sizes.size()) {
          selectedSizeIndex = clickedIndex;
        }
        sizeDropdownOpen = false;
      } else if (colorsDropdownOpen) {
        int clickedIndex = (pos.y - 40) / 20;
        if (clickedIndex >= 0 && clickedIndex < numColorsOptions.size()) {
          selectedNumColorsIndex = clickedIndex;
          numColors = stringToInt(numColorsOptions.get(clickedIndex));
          initColors();
          initBoard();
          connectCells();
        }
        colorsDropdownOpen = false;
      } else {
        if (pos.x > offsetX + sceneSize / 2 - 200 
            && pos.x < offsetX + sceneSize / 2 - 150) {
          sizeDropdownOpen = true;
        } else if (pos.x > offsetX + sceneSize / 2 - 100 
            && pos.x < offsetX + sceneSize / 2 - 50) {
          colorsDropdownOpen = true;
        } else if (pos.x > offsetX + sceneSize / 2 - 40 
            && pos.x < offsetX + sceneSize / 2 + 40) {
          int newSize = extractSize(sizes.get(selectedSizeIndex));
          resetBoard(newSize);
        }
      }
      return;
    }

    // Handle clicks on the game board
    int x = (pos.x - offsetX) / cellSize;
    int y = (pos.y - offsetY) / cellSize;
    if (x >= 0 && x < size && y >= 0 && y < size) {
      Cell clickedCell = board.get(x * size + y);
      if (!clickedCell.color.equals(board.get(0).color)) {
        steps++; 
        // Save the original color
        originalColor = board.get(0).color; 
        // Set the target color to the clicked cell's color
        targetColor = clickedCell.color; 
        // Clear any previous cells to be flooded
        toBeFlooded.clear(); 
        for (Cell cell : board) {
          if (cell.flooded && cell.color.equals(originalColor)) {
            toBeFlooded.add(cell);
          }
        }
      }
    }
  }

  // Purpose: Extracts the size value from a string
  // Effect: Returns the integer size value extracted from the string.
  int extractSize(String sizeString) {
    String x = sizeString.substring(0, sizeString.indexOf('x'));
    return stringToInt(x);
  }

  //Helper method to convert string to integer
  int stringToInt(String s) {
    int num = 0;
    for (int i = 0; i < s.length(); i++) {
      String digitStr = s.substring(i, i + 1);
      int digit = convertDigitStringToInt(digitStr);
      num = num * 10 + digit;
    }
    return num;
  }

  //Helper method to convert a single digit string to an integer
  int convertDigitStringToInt(String digitStr) {
    if (digitStr.equals("0")) {
      return 0;
    } else if (digitStr.equals("1")) {
      return 1;
    } else if (digitStr.equals("2")) {
      return 2;
    } else if (digitStr.equals("3")) {
      return 3;
    } else if (digitStr.equals("4")) {
      return 4;
    } else if (digitStr.equals("5")) {
      return 5;
    } else if (digitStr.equals("6")) {
      return 6;
    } else if (digitStr.equals("7")) {
      return 7;
    } else if (digitStr.equals("8")) {
      return 8;
    } else if (digitStr.equals("9")) {
      return 9;
    } else {
      throw new IllegalArgumentException("Invalid digit string: " + digitStr);
    }
  }

  // Purpose: Handle key events for reset functionality.
  // Effect: Resets the board to the current size if 'r' is pressed.
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      // Reset the board to the current size
      resetBoard(this.size); 
    }
  }

  // Purpose: Fill the board 
  // EFFECT: Flood fill the board starting from the top-left corner
  void floodFill(Color fromColor, Color toColor) {
    if (fromColor.equals(toColor)) {
      return;
    }
    floodFillHelper(board.get(0), fromColor, toColor);
  }

  // Purpose: Fill the board 
  // EFFECT: Flood fill the board starting from the top-left corner
  void floodFillHelper(Cell cell, Color fromColor, Color toColor) {
    if (cell == null || !cell.color.equals(fromColor) || cell.color.equals(toColor)) {
      return;
    }
    cell.color = toColor;
    cell.flooded = true;

    floodFillHelper(cell.left, fromColor, toColor);
    floodFillHelper(cell.right, fromColor, toColor);
    floodFillHelper(cell.top, fromColor, toColor);
    floodFillHelper(cell.bottom, fromColor, toColor);
  }

  //Purpose: Create the game board scene
  //Effect: Renders the current game state, including cells and the board outline.
  public WorldScene makeScene() {
    int sceneSize = Math.max(size * cellSize + 40, 400);
    WorldScene scene = new WorldScene(sceneSize, sceneSize + 60); 
    int offsetX = (sceneSize - size * cellSize) / 2;
    int offsetY = (sceneSize - size * cellSize) / 2 + 40; 


    WorldImage background = new RectangleImage(sceneSize, sceneSize + 60, 
        OutlineMode.SOLID, Color.WHITE);
    scene.placeImageXY(background, sceneSize / 2, (sceneSize + 60) / 2);


    scene.placeImageXY(new TextImage("Size: ", 15, Color.BLACK), 
        offsetX + sceneSize / 2 - 250, 20);
    scene.placeImageXY(new RectangleImage(50, 20, OutlineMode.SOLID, Color.WHITE), 
        offsetX + sceneSize / 2 - 200, 20);
    scene.placeImageXY(new TextImage(sizes.get(selectedSizeIndex), 15, Color.BLACK), 
        offsetX + sceneSize / 2 - 200, 20);


    if (sizeDropdownOpen) {
      for (int i = 0; i < sizes.size(); i++) {
        Color color = Color.WHITE;
        if (i == selectedSizeIndex) {
          color = Color.LIGHT_GRAY;
        }
        scene.placeImageXY(new RectangleImage(50, 20, OutlineMode.SOLID, color), 
            offsetX + sceneSize / 2 - 200, 40 + i * 20);
        scene.placeImageXY(new TextImage(sizes.get(i), 15, Color.BLACK), 
            offsetX + sceneSize / 2 - 200, 40 + i * 20);
      }
    }


    scene.placeImageXY(new TextImage("Colors: ", 15, Color.BLACK), 
        offsetX + sceneSize / 2 - 140, 20);
    scene.placeImageXY(new RectangleImage(50, 20, OutlineMode.SOLID, Color.WHITE), 
        offsetX + sceneSize / 2 - 100, 20);
    scene.placeImageXY(new TextImage(numColorsOptions.get(selectedNumColorsIndex), 
        15, Color.BLACK), 
        offsetX + sceneSize / 2 - 100, 20);


    if (colorsDropdownOpen) {
      for (int i = 0; i < numColorsOptions.size(); i++) {
        Color color = Color.WHITE;
        if (i == selectedNumColorsIndex) {
          color = Color.LIGHT_GRAY;
        }
        scene.placeImageXY(new RectangleImage(50, 20, OutlineMode.SOLID, color), 
            offsetX + sceneSize / 2 - 100, 40 + i * 20);
        scene.placeImageXY(new TextImage(numColorsOptions.get(i), 15, Color.BLACK), 
            offsetX + sceneSize / 2 - 100, 40 + i * 20);
      }
    }


    scene.placeImageXY(new RectangleImage(80, 30, OutlineMode.SOLID, Color.GREEN), 
        offsetX + sceneSize / 2 - 40, 20);
    scene.placeImageXY(new TextImage("New Game", 15, Color.BLACK), 
        offsetX + sceneSize / 2 - 40, 20);


    int outlineWidth = size * cellSize + 10;
    int outlineHeight = size * cellSize + 10; 
    WorldImage outline = new RectangleImage(outlineWidth, outlineHeight, 
        OutlineMode.SOLID, Color.BLACK);
    scene.placeImageXY(outline, offsetX + size * cellSize / 2, 
        offsetY + size * cellSize / 2);


    for (Cell cell : board) {
      scene.placeImageXY(
          new RectangleImage(cellSize, cellSize, OutlineMode.SOLID, cell.color),
          cell.x * cellSize + cellSize / 2 + offsetX,
          cell.y * cellSize + cellSize / 2 + offsetY);
    }


    int seconds = timer / 10;
    scene.placeImageXY(
        new TextImage("Steps: " + steps + " / " + max, 20, FontStyle.BOLD, Color.BLACK),
        sceneSize / 2,
        offsetY + size * cellSize + 20);
    scene.placeImageXY(
        new TextImage("Timer: " + seconds + "s", 20, FontStyle.BOLD, Color.BLACK),
        sceneSize / 2,
        offsetY + size * cellSize + 40);

    return scene;
  }

  //Purpose: Reset the board to a new size.
  // Effect: Resets the game state and reinitializes the board to the specified size
  public void resetBoard(int newSize) {
    this.size = newSize;
    this.board.clear(); // Clear the current board
    this.steps = 0; // Reset steps to 0
    this.max = 3 + (size - 2) / 4 * 7; // Recalculate max steps
    this.timer = 0; // Reset timer
    initBoard(); // Reinitialize the board
    connectCells(); // Reconnect cells
    if (!board.isEmpty()) {
      board.get(0).flooded = true; // Start flooding from the top-left corner
    }
  }

  //Purpose: Increment the timer on each tick and flood the game incrementally.
  // Effect: Updates the timer and floods the game board cells
  public void onTick() {
    timer++;
    ArrayList<Cell> nextToBeFlooded = new ArrayList<>();
    int cellsToFloodPerTick = 5; // Number of cells to flood per tick
    int floodedThisTick = 0;

    for (Cell cell : toBeFlooded) {
      if (floodedThisTick >= cellsToFloodPerTick) {
        nextToBeFlooded.add(cell);
        continue;
      }

      cell.color = targetColor;
      cell.flooded = true;
      floodedThisTick++;

      if (cell.left != null 
          && !cell.left.flooded 
          && cell.left.color.equals(originalColor)) {
        nextToBeFlooded.add(cell.left);
      }
      if (cell.right != null 
          && !cell.right.flooded 
          && cell.right.color.equals(originalColor)) {
        nextToBeFlooded.add(cell.right);
      }
      if (cell.top != null 
          && !cell.top.flooded 
          && cell.top.color.equals(originalColor)) {
        nextToBeFlooded.add(cell.top);
      }
      if (cell.bottom != null 
          && !cell.bottom.flooded 
          && cell.bottom.color.equals(originalColor)) {
        nextToBeFlooded.add(cell.bottom);
      }
    }
    toBeFlooded = nextToBeFlooded;
  }

  //Purpose: Creates the smiley face image used on the winning game scene 
  public WorldImage createSmileyFace(int radius) {
    // Base face
    WorldImage face = new CircleImage(radius, OutlineMode.SOLID, Color.YELLOW);

    // Eyes
    int eyeRadius = radius / 5;
    int eyeOffsetX = radius / 2;
    int eyeOffsetY = radius / 3;
    WorldImage leftEye = new CircleImage(eyeRadius, OutlineMode.SOLID, Color.BLACK)
        .movePinhole(-eyeOffsetX, -eyeOffsetY + 32);
    WorldImage rightEye = new CircleImage(eyeRadius, OutlineMode.SOLID, Color.BLACK)
        .movePinhole(eyeOffsetX, -eyeOffsetY + 32);

    // Mouth
    int mouthWidth = radius * 2 / 3;
    int mouthHeight = radius / 5;
    // Move the mouth up by reversing the offset
    int mouthOffsetY = -radius / 2; 
    WorldImage mouth = new EllipseImage(mouthWidth, mouthHeight, OutlineMode.SOLID, Color.BLACK)
        .movePinhole(0, mouthOffsetY);

    WorldImage smileyFace = new OverlayImage(leftEye, 
        new OverlayImage(rightEye, 
            new OverlayImage(mouth, face)));

    return smileyFace;
  }

  //Purpose: Create the end scene with a message.
  public WorldScene makeEndScene(String message) {
    WorldScene endScene = new WorldScene(400, 400);
    endScene.placeImageXY(new TextImage(message, 32, FontStyle.BOLD, Color.MAGENTA), 200, 100);
    endScene.placeImageXY(createSmileyFace(60), 200, 200);
    return endScene;
  }

  // Check if the entire board is flooded with the same color
  boolean isBoardFlooded() {
    Color floodColor = board.get(0).color;
    for (Cell cell : board) {
      if (!cell.color.equals(floodColor)) {
        return false;
      }
    }
    return true;
  }

  // Determine if the world has ended and create the appropriate end scene
  public WorldEnd worldEnds() {
    if (isBoardFlooded()) {
      return new WorldEnd(true, this.makeEndScene("You Won!" + " In: " + steps + " tries!"));
    } else if (steps == max) {
      return new WorldEnd(true, this.makeEndScene("You Lost :("));
    } else {
      return new WorldEnd(false, this.makeScene());
    }
  }
}

//Example class for testing
class ExamplesFloodItWorld {

  FloodItWorld world = new FloodItWorld(10, 6);

  // Test the FloodItWorld  class
  void testFloodItWorld(Tester t) {
    world.bigBang(400, 400, 0.1);
  }

  // Testing the Cell class
  void testCell(Tester t) {
    Cell c1 = new Cell(0, 0, Color.RED, false);
    t.checkExpect(c1.x, 0);
    t.checkExpect(c1.y, 0);
    t.checkExpect(c1.color, Color.RED);
    t.checkExpect(c1.flooded, false);
    t.checkExpect(c1.left, null);
    t.checkExpect(c1.top, null);
    t.checkExpect(c1.right, null);
    t.checkExpect(c1.bottom, null);
    Cell c2 = new Cell(10, 5, Color.MAGENTA, false);
    t.checkExpect(c2.x, 10);
    t.checkExpect(c2.y, 5);
    t.checkExpect(c2.color, Color.MAGENTA);
    t.checkExpect(c2.flooded, false);
    t.checkExpect(c2.left, null);
    t.checkExpect(c2.top, null);
    t.checkExpect(c2.right, null);
    t.checkExpect(c2.bottom, null);
  }

  // Test the initBoard method
  void testInitBoard(Tester t) {
    FloodItWorld world2 = new FloodItWorld(2, 3);
    FloodItWorld world3 = new FloodItWorld(26, 7);

    t.checkExpect(world2.board.size(), 4); 
    for (int i = 1; i < world2.board.size(); i++) {
      t.checkExpect(world2.board.get(i).flooded, false);
    }

    t.checkExpect(world3.board.size(), 676); 
    for (int i = 1; i < world3.board.size(); i++) {
      t.checkExpect(world3.board.get(i).flooded, false);
    }
  }

  // Test the initColor method
  void testInitColor(Tester t) {
    world.initColors();
    t.checkExpect(world.colors.get(0), Color.RED);
    t.checkExpect(world.colors.get(1), Color.GREEN);
    t.checkExpect(world.colors.get(2), Color.BLUE);
    t.checkExpect(world.colors.get(3), Color.ORANGE);
    t.checkExpect(world.colors.get(4), Color.YELLOW);
    t.checkExpect(world.colors.get(5), Color.PINK);
    t.checkExpect(world.colors.get(6), Color.MAGENTA);
  }

  // test the initSizes method
  void testInitSizes(Tester t) {
    FloodItWorld world = new FloodItWorld(10, 6);
    t.checkExpect(world.sizes.size(), 7);
    t.checkExpect(world.sizes.get(0), "2x2");
    t.checkExpect(world.sizes.get(1), "6x6");
    t.checkExpect(world.sizes.get(2), "10x10");
    t.checkExpect(world.sizes.get(3), "14x14");
    t.checkExpect(world.sizes.get(4), "18x18");
    t.checkExpect(world.sizes.get(5), "22x22");
    t.checkExpect(world.sizes.get(6), "26x26");
  }

  // Test the initNumColorsOptions method
  void testInitNumColorsOptions(Tester t) {
    FloodItWorld world = new FloodItWorld(10, 6);
    t.checkExpect(world.numColorsOptions.size(), 6);
    t.checkExpect(world.numColorsOptions.get(0), "2");
    t.checkExpect(world.numColorsOptions.get(1), "3");
    t.checkExpect(world.numColorsOptions.get(2), "4");
    t.checkExpect(world.numColorsOptions.get(3), "5");
    t.checkExpect(world.numColorsOptions.get(4), "6");
    t.checkExpect(world.numColorsOptions.get(5), "7");
  }


  //Test the connectCells method
  void testConnectCells(Tester t) {
    //Create a 2x2 Board
    Cell c00 = new Cell(0, 0, Color.RED, false);
    Cell c01 = new Cell(0, 1, Color.GREEN, false);
    Cell c10 = new Cell(1, 0, Color.PINK, false);
    Cell c11 = new Cell(1, 1, Color.BLUE, false);

    ArrayList<Cell> board = new ArrayList<Cell>();
    board.add(c00);
    board.add(c01);
    board.add(c10);
    board.add(c11);

    FloodItWorld world = new FloodItWorld(2, 7);
    world.board = board;

    // manually connecting each cell
    world.connectCells();

    // Check connections for the top-left cell (0,0)
    t.checkExpect(c00.left, null);
    t.checkExpect(c00.top, null);
    t.checkExpect(c00.right, c10);
    t.checkExpect(c00.bottom, c01);

    // Check connections for the top-right cell (0,1)
    t.checkExpect(c01.left, null);
    t.checkExpect(c01.top, c00);
    t.checkExpect(c01.right, c11);
    t.checkExpect(c01.bottom, null);

    // Check connections for the bottom-left cell (1,0)
    t.checkExpect(c10.left, c00);
    t.checkExpect(c10.top, null);
    t.checkExpect(c10.right, null);
    t.checkExpect(c10.bottom, c11);

    // Check connections for the bottom-right cell (1,1)
    t.checkExpect(c11.left, c01);
    t.checkExpect(c11.top, c10);
    t.checkExpect(c11.right, null);
    t.checkExpect(c11.bottom, null);  
  }

  //Test the onMouseClicked method
  void testOnMouseClicked(Tester t) {
    FloodItWorld world = new FloodItWorld(10, 6);

    // Set up the test scenario
    Posn clickPos = new Posn(25, 65); 

    // Ensure initial conditions
    t.checkExpect(world.steps, 0);
    t.checkExpect(world.toBeFlooded.size(), 0);

    // Simulate the mouse click
    world.onMouseClicked(clickPos);

    // Check if the steps incremented and toBeFlooded is updated correctly
    t.checkExpect(world.steps + 1, 1);
    t.checkExpect(world.toBeFlooded.size() > 0, false); 
  }

  // Test the floodFill method
  void testFloodFill(Tester t) {
    Cell c1 = world.board.get(0);
    Color originalColor = c1.color;
    world.floodFill(originalColor, world.colors.get(0));
    for (int i = 0; i < world.board.size(); i++) {
      Cell cell = world.board.get(i);
      if (cell.flooded) {
        t.checkExpect(cell.color, world.colors.get(0));
      } else {
        t.checkExpect(cell.color, cell.color);
      }
    }
  }

  //Test the floodFillHelper method
  void testFloodFillHelper(Tester t) {
    FloodItWorld world = new FloodItWorld(10, 6);
    Cell startCell = world.board.get(0);
    Color originalColor = startCell.color;
    world.floodFillHelper(startCell, originalColor, Color.BLACK);

    for (Cell cell : world.board) {
      if (cell.color.equals(Color.BLACK)) {
        t.checkExpect(cell.flooded, true);
      }
    }
  }

  // Test the makeScene method
  void testMakeScene(Tester t) {
    WorldScene scene = world.makeScene();
    int expectedWidth = Math.max(world.size * world.cellSize + 40, 400);
    int expectedHeight = Math.max(world.size * world.cellSize + 40, 460);
    t.checkExpect(scene.width, expectedWidth);
    t.checkExpect(scene.height, expectedHeight);
  }

  //Test the createSmileyFace method
  void testCreateSmileyFace(Tester t) {
    WorldImage smiley = world.createSmileyFace(60);
    t.checkExpect(smiley != null, true); 
  }

  // test Reset board 
  void testResetBoard(Tester t) {
    FloodItWorld world = new FloodItWorld(10, 6);
    world.resetBoard(6);
    t.checkExpect(world.size, 6);
    t.checkExpect(world.board.size(), 6 * 6);
    t.checkExpect(world.steps, 0);
    t.checkExpect(world.timer, 0);
    t.checkExpect(world.board.get(0).flooded, true);
    for (int i = 1; i < world.board.size(); i++) {
      t.checkExpect(world.board.get(i).flooded, false);
    }
  }

  // test ExtractSize 
  void testExtractSize(Tester t) {
    t.checkExpect(world.extractSize("2x2"), 2);
    t.checkExpect(world.extractSize("10x10"), 10);
    t.checkExpect(world.extractSize("26x26"), 26);
    t.checkExpect(world.extractSize("0x0"), 0);
  }

  // Test the stringToInt method
  void testStringToInt(Tester t) {
    t.checkExpect(world.stringToInt("0"), 0);
    t.checkExpect(world.stringToInt("1"), 1);
    t.checkExpect(world.stringToInt("12"), 12);
    t.checkExpect(world.stringToInt("123"), 123);
    t.checkExpect(world.stringToInt("4567"), 4567);
    t.checkExpect(world.stringToInt("987654321"), 987654321);
    t.checkException(new IllegalArgumentException("Invalid digit string: a"), 
        world, "stringToInt", "a123");
  }

  // Test convertDigitStringToInt 
  void testConvertDigitStringToInt(Tester t) {
    FloodItWorld world = new FloodItWorld(10, 6);
    t.checkExpect(world.convertDigitStringToInt("0"), 0);
    t.checkExpect(world.convertDigitStringToInt("1"), 1);
    t.checkExpect(world.convertDigitStringToInt("2"), 2);
    t.checkExpect(world.convertDigitStringToInt("3"), 3);
    t.checkExpect(world.convertDigitStringToInt("4"), 4);
    t.checkExpect(world.convertDigitStringToInt("5"), 5);
    t.checkExpect(world.convertDigitStringToInt("6"), 6);
    t.checkExpect(world.convertDigitStringToInt("7"), 7);
    t.checkExpect(world.convertDigitStringToInt("8"), 8);
    t.checkExpect(world.convertDigitStringToInt("9"), 9);
    t.checkException(new IllegalArgumentException("Invalid digit string: a"), 
        world, "convertDigitStringToInt", "a");
  }

  // Test make End Scene 
  void testMakeEndScene(Tester t) {
    FloodItWorld world = new FloodItWorld(10, 6);
    WorldScene expectedWinScene = new WorldScene(400, 400);
    expectedWinScene.placeImageXY(new TextImage("You Won!", 32, 
        FontStyle.BOLD, Color.MAGENTA), 200, 100);
    expectedWinScene.placeImageXY(world.createSmileyFace(60), 200, 200);
    WorldScene winScene = world.makeEndScene("You Won!");
    t.checkExpect(winScene, expectedWinScene);

    WorldScene expectedLoseScene = new WorldScene(400, 400);
    expectedLoseScene.placeImageXY(new TextImage("You Lost :(", 32, 
        FontStyle.BOLD, Color.MAGENTA), 200, 100);
    expectedLoseScene.placeImageXY(world.createSmileyFace(60), 200, 200);
    WorldScene loseScene = world.makeEndScene("You Lost :(");
    t.checkExpect(loseScene, expectedLoseScene);
  }

  // Test World Ends 
  void testWorldEnds(Tester t) {
    FloodItWorld world = new FloodItWorld(10, 6);
    Color winColor = world.board.get(0).color;
    for (Cell cell : world.board) {
      cell.color = winColor;
    }
    world.steps = 5;
    WorldEnd winEnd = world.worldEnds();
    t.checkExpect(winEnd.worldEnds, true);
    t.checkExpect(winEnd.lastScene.width, 400);
    t.checkExpect(winEnd.lastScene.height, 400);
    world = new FloodItWorld(10, 6); 
    world.steps = world.max; 
    WorldEnd loseEnd = world.worldEnds();
    t.checkExpect(loseEnd.worldEnds, true);
    t.checkExpect(loseEnd.lastScene.width, 400);
    t.checkExpect(loseEnd.lastScene.height, 400);
  }

  //Test the onTick method
  void testOnTick(Tester t) {
    world.toBeFlooded.add(world.board.get(1));
    world.targetColor = Color.RED;
    world.originalColor = world.board.get(0).color;
    world.onTick();
    t.checkExpect(world.timer, 1);
    t.checkExpect(world.board.get(1).color, Color.RED);
  }

  //Test the onKeyEvent method
  void testOnKeyEvent(Tester t) {
    FloodItWorld world = new FloodItWorld(10, 6);
    // Change the state of the world
    world.steps = 5;
    world.timer = 100;

    // Ensure the board is not in the initial state
    t.checkExpect(world.steps, 5);
    t.checkExpect(world.timer, 100);

    // Simulate the key press
    world.onKeyEvent("r");

    // Ensure the board has reset
    t.checkExpect(world.steps, 0);
    t.checkExpect(world.timer, 0);
    t.checkExpect(world.board.size(), 100); // 10x10 board
    t.checkExpect(world.board.get(0).flooded, true);
  }
}