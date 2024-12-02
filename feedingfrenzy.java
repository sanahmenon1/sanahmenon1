import tester.*;
import java.util.Random;
import javalib.worldimages.*;
import javalib.funworld.*;
import java.awt.Color;

// PlayerFish class
class PlayerFish {
  int x;
  int y;
  int radius;
  Color color;
  boolean facingRight;
  int velX;
  int velY;

  PlayerFish(int x, int y, int radius, Color color, 
      boolean facingRight, int velX, int velY) {
    this.x = x;
    this.y = y;
    this.radius = radius;
    this.color = color;
    this.facingRight = facingRight; 
    this.velX = velX;
    this.velY = velY;
  }

  // Draw the player fish
  WorldScene draw(WorldScene scene) {
    int outlineThickness = 1; // Adjust thickness as needed
    Color outlineColor = Color.WHITE; // Outline color

    // Body with outline
    WorldImage body = new CircleImage(this.radius, 
        OutlineMode.SOLID, this.color);
    WorldImage bodyOutline = new CircleImage(this.radius + outlineThickness, 
        OutlineMode.OUTLINE, outlineColor);

    // Tail with outline
    WorldImage tailBase = new EquilateralTriangleImage(this.radius * 1.5, 
        OutlineMode.SOLID, this.color);
    WorldImage tailOutline = new EquilateralTriangleImage(this.radius * 1.5 + outlineThickness, 
        OutlineMode.OUTLINE, outlineColor);
    WorldImage tail = new RotateImage(tailBase, 90);
    WorldImage tailOutlined = new OverlayOffsetImage(new RotateImage(tailOutline, 90), 0, 0, tail);

    // Combine outlined parts
    WorldImage fishBody = new OverlayOffsetImage(bodyOutline, 0, 0, body);
    WorldImage fish = new OverlayOffsetImage(fishBody, -this.radius, 0, tailOutlined);

    if (!this.facingRight) {
      fish = new RotateImage(fish, 180);
    }

    return scene.placeImageXY(fish, this.x, this.y);
  }

  // Move the player fish based on key input
  PlayerFish move(String key) {
    int acceleration = 5;
    if (key.equals("left")) {
      velX -= acceleration;
      facingRight = false;
    } else if (key.equals("right")) {
      velX += acceleration;
      facingRight = true;
    } else if (key.equals("up")) {
      velY -= acceleration;
    } else if (key.equals("down")) {
      velY += acceleration;
    }
    return new PlayerFish(x + velX, y + velY, radius, color, facingRight, 
        velX, velY).applyFriction().wrapAround(800, 600);
  } 

  PlayerFish applyFriction() {
    int friction = 1; // Adjust the friction level
    velX = reduceVelocity(velX, friction);
    velY = reduceVelocity(velY, friction);
    return new PlayerFish(x + velX, y + velY, radius, color, 
        facingRight, velX, velY).wrapAround(800, 600);
  }

  // Helper method to reduce velocity
  int reduceVelocity(int velocity, int friction) {
    if (velocity > 0) {
      return Math.max(0, velocity - friction);
    }
    else if (velocity < 0) {
      return Math.min(0, velocity + friction);
    } else { 
      return 0;
    }
  }

  // Wrap the coordinates around if they go out of bounds
  PlayerFish wrapAround(int width, int height) {
    int newX = this.x;
    int newY = this.y;

    if (this.x > width) {
      newX = 0;  // Wrap from right to left
    } else if (this.x < 0) {
      newX = width;  // Wrap from left to right
    }

    if (this.y > height) {
      newY = 0;  // Wrap from bottom to top
    } else if (this.y < 0) {
      newY = height;  // Wrap from top to bottom
    }

    // Return new PlayerFish instance with the updated position and the same velocities
    return new PlayerFish(newX, newY, this.radius, this.color, 
        this.facingRight, this.velX, this.velY);
  }


  // Grow the player fish
  PlayerFish grow(int increment) {
    return new PlayerFish(this.x, this.y, this.radius + increment, 
        this.color, this.facingRight, velX, velY);
  }
}

// BackgroundFish class
class BackgroundFish {
  int x;
  int y;
  int radius;
  int speed;
  boolean direction; // true for right, false for left
  Color color;

  BackgroundFish(int x, int y, int radius, int speed, 
      boolean direction, Color color) {
    this.x = x;
    this.y = y;
    this.radius = radius;
    this.speed = speed;
    this.direction = direction;
    this.color = color;
  }

  // Draw the background fish
  WorldScene draw(WorldScene scene) {
    WorldImage body = new CircleImage(this.radius, OutlineMode.SOLID, 
        this.color);
    WorldImage tailBase = new EquilateralTriangleImage(this.radius * 1.5, 
        OutlineMode.SOLID, this.color);
    WorldImage tail = new RotateImage(tailBase, 90);
    WorldImage fish = new OverlayOffsetImage(body, -this.radius, 0, tail);

    if (!this.direction) {
      return scene.placeImageXY(new RotateImage(fish, 180), this.x, this.y);
    } else {
      return scene.placeImageXY(fish, this.x, this.y);
    }
  }

  // Move the background fish
  BackgroundFish move() {
    if (this.direction) {
      return new BackgroundFish(this.x + this.speed, this.y, this.radius, 
          this.speed, this.direction, this.color).wrapAround(800, 600);
    } else {
      return new BackgroundFish(this.x - this.speed, this.y, this.radius, 
          this.speed, this.direction, this.color).wrapAround(800, 600);
    }
  }

  // Wrap the coordinates around if they go out of bounds
  BackgroundFish wrapAround(int width, int height) {
    if (this.x > width) {
      return new BackgroundFish(0, this.y, this.radius, this.speed, 
          this.direction, this.color);
    } else if (this.x < 0) {
      return new BackgroundFish(width, this.y, this.radius, this.speed, 
          this.direction, this.color);
    } else if (this.y > height) {
      return new BackgroundFish(this.x, 0, this.radius, this.speed, 
          this.direction, this.color);
    } else if (this.y < 0) {
      return new BackgroundFish(this.x, height, this.radius, this.speed, 
          this.direction, this.color);
    } else {
      return this;
    }
  }

}

// ILoFish interface
interface ILoFish {
  // Draws fish from this list onto the given scene
  WorldScene draw(WorldScene acc);

  // Moves this list of fish
  ILoFish move();

  // Checks for collisions with the player fish
  CollisionResult checkCollisions(PlayerFish player);

  // Find largest fish radius in list
  int largestFish();

  // Add random fish to list
  ILoFish addRandomFish(Random rand, int count);
}

// Result of a collision check
class CollisionResult {
  ILoFish fishList;
  PlayerFish player;
  boolean gameLost;

  CollisionResult(ILoFish fishList, PlayerFish player, boolean gameLost) {
    this.fishList = fishList;
    this.player = player;
    this.gameLost = gameLost;
  }
}

// MtLoFish class
class MtLoFish implements ILoFish {
  public WorldScene draw(WorldScene acc) {
    return acc;
  }

  public ILoFish move() {
    return this;
  }

  public CollisionResult checkCollisions(PlayerFish player) {
    return new CollisionResult(this, player, false);
  }

  public int largestFish() {
    return 0;
  }

  public ILoFish addRandomFish(Random rand, int count) {
    if (count > 0) {
      int randomSize = rand.nextInt(40) + 5; // Random size between 5 and 45
      boolean direction = rand.nextBoolean(); // true for moving to the right, false for left
      int initialX;
      if (direction) {
        initialX = -randomSize; // Position left of the screen for fish moving to the right
      } else {
        initialX = 800 + randomSize; // Position right of the screen for fish moving to the left
      }
      int randomY = rand.nextInt(600); // Random y position within the screen height
      int speed = rand.nextInt(3) + 1; // Random speed between 1 and 3
      Color randomColor = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));

      BackgroundFish newFish = new BackgroundFish(initialX, randomY, 
          randomSize, speed, direction, randomColor);
      return new ConsLoFish(newFish, this.addRandomFish(rand, count - 1));
    } else {
      return this;
    }
  }
}

// ConsLoFish class
class ConsLoFish implements ILoFish {
  BackgroundFish first;
  ILoFish rest;

  ConsLoFish(BackgroundFish first, ILoFish rest) {
    this.first = first;
    this.rest = rest;
  }

  public WorldScene draw(WorldScene acc) {
    return this.rest.draw(this.first.draw(acc));
  }

  public ILoFish move() {
    return new ConsLoFish(this.first.move(), this.rest.move());
  }

  public CollisionResult checkCollisions(PlayerFish player) {
    if (Math.sqrt(Math.pow(this.first.x - player.x, 2) 
        + Math.pow(this.first.y - player.y, 2)) < (this.first.radius + player.radius)) {
      if (player.radius >= this.first.radius) {
        return new CollisionResult(this.rest, player.grow(this.first.radius), false);
      } else {
        return new CollisionResult(this.rest, player, true);
      }
    } else {
      CollisionResult result = this.rest.checkCollisions(player);
      return new CollisionResult(new ConsLoFish(this.first, 
          result.fishList), result.player, result.gameLost);
    }
  }

  public int largestFish() {
    return Math.max(this.first.radius, this.rest.largestFish());
  }

  public ILoFish addRandomFish(Random rand, int count) {
    if (count > 0) {
      int randomSize = rand.nextInt(50) + 10; // Random size between 10 and 60
      return new ConsLoFish(new BackgroundFish(rand.nextInt(800), 
          rand.nextInt(600), randomSize, rand.nextInt(3) + 1, rand.nextBoolean(), 
          new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256))), 
          this.rest.addRandomFish(rand, count - 1));
    } else {
      return this;
    }
  }
}

// GameWorld class
class GameWorld extends World {
  PlayerFish player;
  ILoFish backgroundFish;
  int time;
  boolean playerLost;
  Random rand;

  GameWorld(PlayerFish player, ILoFish backgroundFish, int time, boolean playerLost) {
    this.player = player;
    this.backgroundFish = backgroundFish;
    this.time = time;
    this.playerLost = playerLost;
    this.rand = new Random();
  }

  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(800, 600);
    scene = scene.placeImageXY(new RectangleImage(800, 600, 
        OutlineMode.SOLID, Color.CYAN), 400, 300); // Cyan background
    if (!playerLost && this.time > 0) {
      scene = this.backgroundFish.draw(this.player.draw(scene));
    }
    return scene;
  }

  public World onKeyEvent(String key) {
    PlayerFish movedPlayer = this.player.move(key);
    return new GameWorld(movedPlayer, this.backgroundFish, this.time, this.playerLost);
  }

  public World onTick() {
    if (!playerLost) {
      // Update the player's position based on its current velocity
      this.player = this.player.applyFriction(); 
      this.backgroundFish = this.backgroundFish.move();
      CollisionResult collisionResult = this.backgroundFish.checkCollisions(this.player);
      this.player = collisionResult.player;
      this.backgroundFish = collisionResult.fishList;
      this.playerLost = collisionResult.gameLost;
      this.time--;

      if (this.playerLost) {
        return new GameWorld(this.player, this.backgroundFish, 
            this.time, true).endOfWorld("Game Over: You were eaten!");
      }

      if (this.backgroundFish.largestFish() < this.player.radius) {
        return new GameWorld(this.player, this.backgroundFish, 
            this.time, false).endOfWorld("YOU WIN!");
      }

      if (this.time % 20 == 0) {
        this.backgroundFish = this.backgroundFish.addRandomFish(rand, 1);
      }
    }
    return new GameWorld(this.player, this.backgroundFish, 
        this.time, this.playerLost);
  }

  public WorldEnd worldEnds() {
    if (this.playerLost) {
      return new WorldEnd(true, this.makeEndScene("Game Over: You were eaten!"));
    } else if (this.backgroundFish.largestFish() < this.player.radius) {
      return new WorldEnd(true, this.makeEndScene("YOU WIN!"));
    }
    return new WorldEnd(false, this.makeScene());
  }

  public WorldScene makeEndScene(String message) {
    WorldScene scene = new WorldScene(800, 600);
    scene = scene.placeImageXY(new RectangleImage(800, 600, 
        OutlineMode.SOLID, Color.CYAN), 400, 300); // Cyan background
    scene = scene.placeImageXY(new TextImage(message, 32, 
        FontStyle.BOLD, Color.RED), 400, 300);
    return scene;
  }
}

// Examples class to start the game
class ExamplesMyWorldProgram {

  PlayerFish player = new PlayerFish(250, 200, 15, Color.black, true, 0, 0);
  ILoFish backgroundFish = new MtLoFish();
  GameWorld game = new GameWorld(player, 
      backgroundFish.addRandomFish(new Random(), 30), 1000, false);
  ILoFish consLoFish = new ConsLoFish(new BackgroundFish(250, 200, 10, 2, true, Color.blue), 
      new MtLoFish());

  boolean testBigBang(Tester t) {
    PlayerFish player = new PlayerFish(250, 200, 15, Color.black, true, 0, 0);
    ILoFish backgroundFish = new MtLoFish();
    backgroundFish = backgroundFish.addRandomFish(new Random(), 30);
    GameWorld game = new GameWorld(player, backgroundFish, 1000, false);
    return game.bigBang(800, 600, 0.1);
  }

  boolean testPlayerFishMove(Tester t) {
    PlayerFish player = new PlayerFish(250, 200, 15, Color.black, true, 0, 0);
    PlayerFish movedLeft = player.move("left");
    PlayerFish movedRight = player.move("right");
    PlayerFish movedUp = player.move("up");
    PlayerFish movedDown = player.move("down");
    return t.checkExpect(movedLeft, new PlayerFish(245, 200, 15, Color.black, false, -5, 0))
        && t.checkExpect(movedRight, new PlayerFish(255, 200, 15, Color.black, true, 5, 0))
        && t.checkExpect(movedUp, new PlayerFish(250, 195, 15, Color.black, true, 0, -5))
        && t.checkExpect(movedDown, new PlayerFish(250, 205, 15, Color.black, true, 0, 5));
  }

  boolean testPlayerFishWrapAround(Tester t) {
    PlayerFish player = new PlayerFish(810, 200, 15, Color.black, true, 0, 0);
    PlayerFish wrapped = player.wrapAround(800, 600);
    return t.checkExpect(wrapped, new PlayerFish(0, 200, 15, Color.black, true, 0, 0))
        && t.checkExpect(new PlayerFish(-10, 200, 15, Color.black, true, 0, 0).wrapAround(800, 600),
            new PlayerFish(800, 200, 15, Color.black, true, 0, 0))
        && t.checkExpect(new PlayerFish(250, 610, 15, Color.black, true, 0, 0).wrapAround(800, 600),
            new PlayerFish(250, 0, 15, Color.black, true, 0, 0))
        && t.checkExpect(new PlayerFish(250, -10, 15, Color.black, true, 0, 0).wrapAround(800, 600),
            new PlayerFish(250, 600, 15, Color.black, true, 0, 0));
  }

  boolean testPlayerFishGrow(Tester t) {
    PlayerFish player = new PlayerFish(250, 200, 15, Color.black, true, 0, 0);
    PlayerFish grown = player.grow(5);
    return t.checkExpect(grown, new PlayerFish(250, 200, 20, Color.black, true, 0, 0));
  }

  boolean testBackgroundFishMove(Tester t) {
    BackgroundFish fish = new BackgroundFish(250, 200, 15, 5, true, Color.blue);
    BackgroundFish movedRight = fish.move();
    BackgroundFish movedLeft = new BackgroundFish(250, 200, 15, 5, false, Color.blue).move();
    return t.checkExpect(movedRight, new BackgroundFish(255, 200, 15, 5, true, Color.blue))
        && t.checkExpect(movedLeft, new BackgroundFish(245, 200, 15, 5, false, Color.blue));
  }

  boolean testBackgroundFishWrapAround(Tester t) {
    BackgroundFish fish = new BackgroundFish(810, 200, 15, 5, true, Color.blue);
    BackgroundFish wrapped = fish.wrapAround(800, 600);
    return t.checkExpect(wrapped, new BackgroundFish(0, 200, 15, 5, true, Color.blue))
        && t.checkExpect(new BackgroundFish(-10, 200, 15, 5, false, Color.blue)
            .wrapAround(800, 600),
            new BackgroundFish(800, 200, 15, 5, false, Color.blue))
        && t.checkExpect(new BackgroundFish(250, 610, 15, 5, true, Color.blue)
            .wrapAround(800, 600),
            new BackgroundFish(250, 0, 15, 5, true, Color.blue))
        && t.checkExpect(new BackgroundFish(250, -10, 15, 5, true, Color.blue)
            .wrapAround(800, 600),
            new BackgroundFish(250, 600, 15, 5, true, Color.blue));
  }

  boolean testILoFishAddRandomFish(Tester t) {
    ILoFish emptyList = new MtLoFish();
    ILoFish newList = emptyList.addRandomFish(new Random(), 1);
    return t.checkExpect(newList instanceof ConsLoFish, true);
  }

  boolean testILoFishMove(Tester t) {
    ILoFish fishList = new ConsLoFish(
        new BackgroundFish(250, 200, 15, 5, true, Color.blue), 
        new MtLoFish());
    ILoFish movedList = fishList.move();
    return t.checkExpect(movedList, new ConsLoFish(
        new BackgroundFish(255, 200, 15, 5, true, Color.blue), 
        new MtLoFish()));
  }

  boolean testILoFishCheckCollisions(Tester t) {
    ILoFish fishList = new ConsLoFish(new BackgroundFish(250, 200, 15, 5, true, Color.blue), 
        new MtLoFish());
    PlayerFish player = new PlayerFish(250, 200, 15, Color.black, true, 0, 0);
    CollisionResult result = fishList.checkCollisions(player);
    return t.checkExpect(result.gameLost, false)
        && t.checkExpect(result.player.radius, 30);
  }

  boolean testILoFishLargestFish(Tester t) {
    ILoFish fishList = new ConsLoFish(new BackgroundFish(250, 200, 10, 5, true, Color.blue), 
        new MtLoFish());
    return t.checkExpect(fishList.largestFish(), 10);
  }

  boolean testGameWorldMakeScene(Tester t) {
    PlayerFish player = new PlayerFish(250, 200, 15, Color.black, true, 0, 0);
    ILoFish consLoFish = new ConsLoFish(new BackgroundFish(250, 200, 10, 2, true, Color.blue), 
        new MtLoFish());
    GameWorld game = new GameWorld(player, consLoFish, 1000, false);

    WorldScene scene = new WorldScene(800, 600);
    WorldScene expectedScene = scene.placeImageXY(new RectangleImage(800, 600, OutlineMode.SOLID, 
        Color.CYAN), 
        400, 300)
        .placeImageXY(
            new OverlayOffsetImage(
                new CircleImage(16, OutlineMode.OUTLINE, Color.WHITE),
                0, 0,
                new OverlayOffsetImage(
                    new RotateImage(
                        new EquilateralTriangleImage(22.5, OutlineMode.OUTLINE, Color.WHITE), 90),
                    0, 0,
                    new OverlayOffsetImage(
                        new CircleImage(15, OutlineMode.SOLID, Color.black),
                        0, 0,
                        new RotateImage(
                            new EquilateralTriangleImage(22.5, OutlineMode.SOLID, Color.black), 90)
                        )
                    )
                ),
            250, 200);
    return t.checkExpect(game.makeScene(), expectedScene);
  }

  boolean testGameWorldOnKeyEvent(Tester t) {
    PlayerFish player = new PlayerFish(250, 200, 15, Color.black, true, 0, 0);
    ILoFish consLoFish = new ConsLoFish(new BackgroundFish(250, 200, 10, 2, true, Color.blue), 
        new MtLoFish());
    GameWorld game = new GameWorld(player, consLoFish, 1000, false);

    return t.checkExpect(game.onKeyEvent("left"), 
        new GameWorld(new PlayerFish(245, 200, 15, Color.black, false, -5, 0), 
            consLoFish, 1000, false))
        && t.checkExpect(game.onKeyEvent("right"), 
            new GameWorld(new PlayerFish(255, 200, 15, Color.black, true, 5, 0), 
                consLoFish, 1000, false))
        && t.checkExpect(game.onKeyEvent("up"), 
            new GameWorld(new PlayerFish(250, 195, 15, Color.black, true, 0, -5), 
                consLoFish, 1000, false))
        && t.checkExpect(game.onKeyEvent("down"), 
            new GameWorld(new PlayerFish(250, 205, 15, Color.black, true, 0, 5), 
                consLoFish, 1000, false));
  }

  boolean testGameWorldOnTick(Tester t) {
    PlayerFish player = new PlayerFish(250, 200, 15, Color.black, true, 5, 5);
    ILoFish consLoFish = new ConsLoFish(new BackgroundFish(250, 200, 10, 2, true, Color.blue), 
        new MtLoFish());
    GameWorld game = new GameWorld(player, consLoFish, 1000, false);

    return t.checkExpect(game.onTick(), 
        new GameWorld(new PlayerFish(254, 204, 15, Color.black, true, 4, 4), 
            consLoFish.move(), 999, false));
  }

  boolean testGameWorldWorldEnds(Tester t) {
    PlayerFish player = new PlayerFish(250, 200, 15, Color.black, true, 0, 0);
    ILoFish consLoFish = new ConsLoFish(new BackgroundFish(250, 200, 10, 2, true, Color.blue), 
        new MtLoFish());
    GameWorld game = new GameWorld(player, consLoFish, 1000, true);

    boolean testGameLost = t.checkExpect(game.worldEnds(), 
        new WorldEnd(true, game.makeEndScene("Game Over: You were eaten!")));

    PlayerFish largePlayer = new PlayerFish(250, 200, 20, Color.black, true, 0, 0);
    game = new GameWorld(largePlayer, consLoFish, 1000, false);

    boolean testGameWon = t.checkExpect(game.worldEnds(), 
        new WorldEnd(true, game.makeEndScene("YOU WIN!")));

    return testGameLost && testGameWon;
  }
}
