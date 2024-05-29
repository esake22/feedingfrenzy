import tester.*;
import javalib.worldimages.*;
import javalib.funworld.*;

import java.awt.*;
import java.util.Random;

// make visual hit box to debug
class FishWorld extends World {
  int id;
  PlayerFish eric;
  ILoBackgroundFish otherFishies;
  ILoSnacks snacks;
  // should these be instantiated here or in the constructor? for abstraction
  int windowW;
  int windowH;

  FishWorld(PlayerFish eric, ILoBackgroundFish otherFishies, int id, ILoSnacks snacks) {
    this.eric = eric;
    this.otherFishies = otherFishies;
    this.id = id;
    this.snacks = snacks;
    this.windowW = 1600;
    this.windowH = 950;
  }
  /*
   * Template: Fields:
   * this.id ... int
   * this.eric ... PlayerFish
   * this.otherFishies ... ILoBackGroundFish
   * this.windowW ... int
   * this.windowH ... int
   * 
   * Methods:
   * this.makeScene() ... WorldScene
   * this.onTick() ... FishWorld
   * this.onKeyEvent(String) ... FishWorld
   * this.draw(WorldScene) ... WorldScene
   * this.lastScene(String) ... WorldScene
   * this.moveFishies() ... FishWorld
   * this.generateFish() ... FishWorld
   * this.isBiggestFish() ... boolean
   * this.caneat(ILoBackgroundFish) ... FishWorld
   * this.returnCollidedFish() ... ILoBackgroundFish
   * this.canEatHelper(BackgroundFish) ... FishWorld
   * -- SNACK METHODS ---
   * 
   * Methods on Fields:
   * 
   * 
   */

  // renders the current scene with the playerfish, background fish, snacks, and
  // score display
  // will also display the end game screen
  public WorldScene makeScene() {
    // System.out.println("makeScene: " + this.eric.position.x + ", " +
    // this.eric.position.y);
    if (this.eric.outOfLives()) {
      return this.lastScene("Game Lost");
    }
    else if (this.isBiggestFish()) {
      return this.lastScene("Game Won");
    }
    else {
      return this.draw(new WorldScene(windowW, windowH).placeImageXY(
          new RectangleImage(windowW, windowH, OutlineMode.SOLID, Color.CYAN), windowW / 2,
          windowH / 2));
    }
  }

  // method called every 'tick' to generate fish and snacks. also handles when the
  // playerfish 'collides' with other objects
  public FishWorld onTick() {
    // System.out.println("Tick: " + this.eric.position.x + ", " +
    // this.eric.position.y);
    Random rand = new Random();
    if (rand.nextInt(100) > 85) {
      return this.generateFish().moveFishies().canEat(this.returnCollidedFish())
          .handleSnackCollision(this.returnCollidedSnacks());
    }
    return this.moveFishies().canEat(this.returnCollidedFish())
        .handleSnackCollision(this.returnCollidedSnacks());
  }

  // move the fish around the scene if an arrow key is pressed
  public FishWorld onKeyEvent(String key) {
    PlayerFish movedEric = this.eric.moveFish(key);
    // System.out.println("Key Event: " + key + " -> " + newEric.position.x + ", " +
    // newEric.position.y);
    return new FishWorld(movedEric, this.otherFishies, this.id, this.snacks);
  }

  // renders the background fish and snacks using an accumulator.
  public WorldScene draw(WorldScene acc) {
    // (DEBUGGER) System.out.println("Drawing at: " + this.eric.position.x + ", " +
    // this.eric.position.y);
    // Creates the world with the BGfish first then passes
    // it as an accumulator into draw player fish method
    WorldScene bgFishScene = this.otherFishies.drawBGfish(acc);
    WorldScene withSnacksScene = this.snacks.drawSnacks(bgFishScene);
    return this.eric.drawFish(withSnacksScene);
  }

  // determines what is displayed with the game is over (won or lost)
  @Override
  public WorldScene lastScene(String msg) {
    if (msg.equals("Game Won")) {
      // add winning stats
      return new WorldScene(this.windowW, this.windowH)
          .placeImageXY(new TextImage("Game Won", 50, Color.BLACK), this.windowW / 2,
              this.windowH / 2)
          .placeImageXY(new TextImage(this.eric.getStats(), 20, Color.BLACK), this.windowW / 2,
              700);
    }
    if (msg.equals("Game Lost")) {
      return new WorldScene(this.windowW, this.windowH)
          .placeImageXY(new TextImage("Game Lost", 50, Color.BLACK), this.windowW / 2,
              this.windowH / 2)
          .placeImageXY(new TextImage(this.eric.getStats(), 20, Color.BLACK), this.windowW / 2,
              700);
    }
    else {
      return new WorldScene(500, 500);
    }
  }

  // moves the background fish
  public FishWorld moveFishies() {
    return new FishWorld(this.eric, this.otherFishies.moveFishies(), this.id, this.snacks);
  }

  // makes background fish and updates the world state (fishworld)
  FishWorld generateFish() {
    Random rand = new Random();
    return new FishWorld(this.eric, otherFishies.addToLoBG(this.id++, rand.nextInt(100)), this.id++,
        this.snacks);
  }

  // determines whether the playerfish is the largest fish
  public boolean isBiggestFish() {
    return this.eric.biggestFish();
  }

  // takes in a list of bg fish that collided with the player fish
  // and determines if they can be eaten by the player or if a life needs to be
  // taken
  FishWorld canEat(ILoBackgroundFish collidedFish) {
    return collidedFish.collisionUpdate(this);
  }

  // checks if the player has collided with any background fish and returns a list
  // of collided fish
  ILoBackgroundFish returnCollidedFish() {
    return this.eric.checkCollision(this.otherFishies);
  }

  // updates the fishworld based on if the given background fish (which the
  // playerfish has collided with) will be eaten or not
  FishWorld canEatHelper(BackgroundFish otherFish) {
    if (otherFish.compareFishes(this.eric)) { // will return true when this fish CAN EAT
      return new FishWorld(this.eric.ateFishUpdate(otherFish), otherFishies.eat(otherFish), this.id,
          this.snacks);
    }
    else {
      // If the BG fish is bigger, it'll remove a life from
      // the world state and determine if the game is over
      return new FishWorld(this.eric.updateLives(-1), otherFishies, this.id, this.snacks);
    }
  }

  // COME BACK TO THIS
  FishWorld handleSnackCollision(ILoSnacks collidedSnacks) {
    return collidedSnacks.snackUpdate(this);
  }

  // COME BACK TO THIS
  ILoSnacks returnCollidedSnacks() {
    return this.snacks.checkSnackCollision(this.eric);
  }
}

class PlayerFish {
  CartPt position;
  int size;
  int score;
  int speed;
  int livesLeft;
  int direction;
  WorldImage skin;

  PlayerFish(CartPt position, int size, int score, int speed, int livesLeft, int direction) {
    this.position = position;
    this.size = size;
    this.score = score;
    this.speed = speed;
    this.livesLeft = livesLeft;
    if (direction == 0) {
      this.skin = new OverlayImage(
          new RectangleImage(this.size * 4, this.size * 2, OutlineMode.OUTLINE, Color.RED),
          new OverlayOffsetImage(
              new EllipseImage(this.size * 4, this.size * 2, OutlineMode.SOLID, Color.ORANGE),
              this.size * 2, 0,
              (new RotateImage(
                  new EquilateralTriangleImage(this.size * 2, OutlineMode.SOLID, Color.ORANGE),
                  270))));
    }
    else {
      this.skin = new OverlayImage(
          new RectangleImage(this.size * 4, this.size * 2, OutlineMode.OUTLINE, Color.RED),
          new OverlayOffsetImage(
              new EllipseImage(this.size * 4, this.size * 2, OutlineMode.SOLID, Color.ORANGE),
              (-1 * this.size * 2), 0,
              (new RotateImage(
                  new EquilateralTriangleImage(this.size * 2, OutlineMode.SOLID, Color.ORANGE),
                  -270))));
    }
  }
  /*
   * Template Fields:
   * 
   * Methods:
   * 
   * Methods on Fields:
   * 
   */

  // draws the playerfish on top of a given worldscene
  WorldScene drawFish(WorldScene acc) {
    return acc.placeImageXY(this.skin, this.position.x, this.position.y).placeImageXY(new TextImage("Score: " + String.valueOf(this.score), 20, Color.BLACK),
        1300, 15).placeImageXY(new TextImage("Lives: " + String.valueOf(this.livesLeft), 20, Color.BLACK), 1400, 15);
  }

  // handles keys pressed and moves the playerfish accordingly !!!
  //
  PlayerFish moveFish(String key) {
    int X = this.position.x;
    int Y = this.position.y;
    int dir = this.direction;
    if (key.equals("up")) {
      Y -= this.speed;
    }
    else if (key.equals("down")) {
      Y += this.speed;
    }
    else if (key.equals("left")) {
      X -= this.speed;
      dir = 0;
    }
    else if (key.equals("right")) {
      X += this.speed;
      dir = 1;
    }
    // need to make these adjustable so if you change the
    // scene size it'll adjust this too
    if (X < 0)
      X = 1600;
    if (X > 1600)
      X = 0;
    if (Y < 0)
      Y = 950;
    if (Y > 950)
      Y = 0;

    return new PlayerFish(new CartPt(X, Y), this.size, this.score, this.speed, this.livesLeft, dir);
  }

  // updates the playerfish when it properly eats another fish (size and score
  // update)
  PlayerFish ateFishUpdate(BackgroundFish otherFish) {
    return new PlayerFish(this.position, otherFish.calculateSizeGained(this.size),
        otherFish.calculateScoreGained(this.score), this.speed, this.livesLeft, this.direction);
  }

  // checks if the size of this PlayerFish is greater than a BackgroundFish
  Boolean compareFishesHelp(double otherFishSize) {
    return this.size >= otherFishSize;
  }

  // updates the lives of the playerfish based on a given change
  PlayerFish updateLives(int updateAmt) {
    return new PlayerFish(this.position, this.size, this.score, this.speed,
        updateAmt + this.livesLeft, this.direction);
  }

  // checks if the playerfish is out of lives
  boolean outOfLives() {
    return this.livesLeft <= 0;
  }

  // returns a list of background fish that are colliding with the playerfish
  // WHY IS IT RETURNING A LIST RATHER THAN AN INDIV BGFISH?
  ILoBackgroundFish checkCollision(ILoBackgroundFish bgFishList) {
    return bgFishList.checkCollision(this.position, this.size);
  }

  // checks if the playerfish is the biggest fish based on a fixed size
  boolean biggestFish() {
    return this.size > 80;
  }

  String getStats() {
    return "Score: " + this.score + " Lives Left: " + this.livesLeft;
  }

  // COME BACK TO THIS
  PlayerFish eatSnack(ASnack snack) {
    return snack.applyEffect(this);
  }

  // Method to check collision with a snack
  boolean checkSnackCollision(ASnack snack) {
    return snack.checkSnackCollision(this.position, this.size);

  }

  PlayerFish applyEffect(int sizeBoost, int scoreBoost, int speedBoost) {
    return new PlayerFish(this.position, this.size + sizeBoost, this.score + scoreBoost,
        this.speed + speedBoost, this.livesLeft, this.direction);
  }

}

// class to represent background fish
class BackgroundFish {
  int id;
  CartPt position;
  int size;
  int speed;
  int points;
  int direction;
  WorldImage fishModel;

  BackgroundFish(int id, CartPt position, int size, int speed, int points, int direction,
      WorldImage fishModel) {
    this.id = id;
    this.position = position;
    this.size = size;
    this.speed = speed;
    this.points = points;
    this.direction = direction;
    this.fishModel = fishModel;
  }
  /*
   * Template
   * 
   * Fields:
   * 
   * Methods:
   * 
   * Methods on Fields:
   */

  BackgroundFish(int id, CartPt position) {
    Random rand = new Random();
    int sizer = rand.nextInt(100) + 10;
    int colorNum = rand.nextInt(10);
    Color colorName;

    this.id = id;
    this.position = position;
    this.size = sizer;

    if (size <= 20) {
      this.points = 1;
      this.speed = 20;
    }
    else if (size > 20 && size <= 40) {
      this.points = 2;
      this.speed = 18;
    }
    else if (size > 40 && size <= 60) {
      this.points = 3;
      this.speed = 16;
    }
    else if (size > 60 && size <= 80) {
      this.points = 4;
      this.speed = 14;
    }
    else if (size > 80) {
      this.points = 5;
      this.speed = 12;
    }

    this.direction = rand.nextInt(2);

    if (colorNum == 1) {
      colorName = Color.PINK;
    }
    else if (colorNum == 2) {
      colorName = Color.CYAN;
    }
    else if (colorNum == 3) {
      colorName = Color.GREEN;
    }
    else if (colorNum == 4) {
      colorName = Color.BLUE;
    }
    else if (colorNum == 5) {
      colorName = Color.BLACK;
    }
    else if (colorNum == 6) {
      colorName = Color.RED;
    }
    else if (colorNum == 7) {
      colorName = Color.YELLOW;
    }
    else {
      colorName = Color.MAGENTA;
    }

    if (this.direction == 0) {
      this.fishModel = new OverlayOffsetImage(
          new EllipseImage(sizer * 2, sizer, OutlineMode.SOLID, colorName), sizer, 0,
          (new RotateImage(new EquilateralTriangleImage(sizer, OutlineMode.SOLID, colorName),
              270)));
    }
    else {
      this.fishModel = new OverlayOffsetImage(
          new EllipseImage(sizer * 2, sizer, OutlineMode.SOLID, colorName), (-1) * sizer, 0,
          (new RotateImage(new EquilateralTriangleImage(sizer, OutlineMode.SOLID, colorName),
              -270)));
    }
  }

  // calculates how much size is gained from eating this bgfish
  // and adds it to the player fish's size
  int calculateSizeGained(int playerFishSize) {
    return this.size + playerFishSize;
  }

  // calculates how many points are gained from eating this bgfish
  // and adds it to the fish world state
  int calculateScoreGained(int fishWorldPoints) {
    return this.points + fishWorldPoints;
  }

  // determines if the PlayerFish is bigger than this BackgroundFish
  boolean compareFishes(PlayerFish playerFish) {
    return playerFish.compareFishesHelp(this.size);
  }

  // determines if this bg fish and an other bg fish share the same name
  boolean fishMatch(BackgroundFish otherFish) {
    return otherFish.fishMatchHelper(this.id);
  }

  // determines if the name provided matches this bg fish's name
  boolean fishMatchHelper(int otherID) {
    return this.id == otherID;
  }

  // draws the bg fish on a given worldscene
  WorldScene drawBGfish(WorldScene acc) {
    return acc.placeImageXY(this.fishModel, this.position.x, this.position.y);
  }

  // moved the bg fish and updates its position
  BackgroundFish moveBGFish() {
    int x = this.position.x;

    if (this.direction == 0) {
      x -= this.speed;
    }
    else {
      x += this.speed;
    }

    if (x < 0)
      x = 1600;
    if (x > 1600)
      x = 0;

    return new BackgroundFish(this.id, new CartPt(x, this.position.y), this.size, this.speed,
        this.points, this.direction, this.fishModel);
  }

  // checks if this bg fish is overlapping with the pf
  boolean checkCollisionHelper(CartPt pfPosition, double pfSize) {
    // there is definitely a way to simplify this LOL
    // but for now this method will do the job
    // X1 is rightmost point
    double bgX1 = this.position.x + this.size * 2;
    // X2 is leftmost point
    double bgX2 = this.position.x - this.size * 2;
    // Y1 is highest point
    double bgY1 = this.position.y + this.size;
    // Y2 is lowest point
    double bgY2 = this.position.y - this.size;
    // X1 is rightmost point
    double pfX1 = pfPosition.x + pfSize * 2;
    // X2 is leftmost point
    double pfX2 = pfPosition.x - pfSize * 2;
    // Y1 is highest point
    double pfY1 = pfPosition.y + pfSize;
    // Y2 is lowest point
    double pfY2 = pfPosition.y - pfSize;

    return bgX2 < pfX1 && bgX1 > pfX2 && bgY2 < pfY1 && bgY1 > pfY2;
  }
}

// an interface to represent a list of background fish
interface ILoBackgroundFish {
  // searches a list for otherFish and returns a
  // a list of bg fish with otherFish removed
  ILoBackgroundFish eat(BackgroundFish otherFish);

  // iterates through the list and draws the bgfish
  WorldScene drawBGfish(WorldScene acc);

  // iterates thorugh the list and checks if the pf is colliding with any items
  ILoBackgroundFish checkCollision(CartPt pfPosition, double pfSize);

  // updates the world state if there is a collision
  FishWorld collisionUpdate(FishWorld fishWorld);

  // adds new bg fish with a unique id to the list
  ILoBackgroundFish addToLoBG(int id, int randomNum);

  // moves all of the fish in the list
  ILoBackgroundFish moveFishies();

}

// a class to represent the empty list of background fish
class MtLoBackgroundFish implements ILoBackgroundFish {
  MtLoBackgroundFish() {
  }
  /*
   * Template Fields:
   * 
   * Methods:
   * 
   * Fields of Methods:
   * 
   * 
   */

  // there are no fish in the empty list, thus 'eat' will return the empty list
  public ILoBackgroundFish eat(BackgroundFish otherFish) {
    return this;
  }

  // returns all of the currently drawn bg fish
  public WorldScene drawBGfish(WorldScene acc) {
    return acc;
  }

  // returns empty list. there are no collisions with no fish!
  public ILoBackgroundFish checkCollision(CartPt pfPosition, double pfSize) {
    return this;
  }

  // returns given worldstate. nothing to update on the empty list
  public FishWorld collisionUpdate(FishWorld fishWorld) {
    return fishWorld;
  }

  // returns empty list. cannot move no fishies!
  public ILoBackgroundFish moveFishies() {
    return this;
  }

  // 'generates' a new bgfish randomly
  public ILoBackgroundFish addToLoBG(int id, int randomNum) {
    Random rand = new Random();
    return new ConsLoBackgroundFish(
        new BackgroundFish(id, new CartPt(rand.nextInt(1600), rand.nextInt(900))), this);
  }
}

class ConsLoBackgroundFish implements ILoBackgroundFish {
  // fields
  //
  BackgroundFish first;
  ILoBackgroundFish rest;

  ConsLoBackgroundFish(BackgroundFish first, ILoBackgroundFish rest) {
    this.first = first;
    this.rest = rest;
  }

  // return the same list with the given fishie removed
  public ILoBackgroundFish eat(BackgroundFish otherFish) {
    if (this.first.fishMatch(otherFish)) {
      return this.rest;
    }
    else {
      return new ConsLoBackgroundFish(this.first, this.rest.eat(otherFish));
    }
  }

  // draws this list of bg fish on a world scene
  public WorldScene drawBGfish(WorldScene acc) {
    return this.rest.drawBGfish(this.first.drawBGfish(acc));
  }

  // moves all the fishies in the list
  public ILoBackgroundFish moveFishies() {
    return new ConsLoBackgroundFish(this.first.moveBGFish(), this.rest.moveFishies());
  }

  // 'generates' a new bgfish randomly and adds it to the current list of bg fish
  public ILoBackgroundFish addToLoBG(int id, int randomNum) {
    Random rand = new Random();
    return new ConsLoBackgroundFish(
        new BackgroundFish(id, new CartPt(rand.nextInt(1600), rand.nextInt(900))), this);
  }

  // goes through a list of bg fish and returns any that are colliding with
  // the hitbox (calculated through pf position and pf size) of the playerfish
  public ILoBackgroundFish checkCollision(CartPt pfPosition, double pfSize) {
    if (this.first.checkCollisionHelper(pfPosition, pfSize)) {
      // System.out.println(this.first.id);
      return new ConsLoBackgroundFish(this.first, this.rest.checkCollision(pfPosition, pfSize));
    }
    else {
      return this.rest.checkCollision(pfPosition, pfSize);
    }
  }

  // updates the fishworld if there has been a collision
  public FishWorld collisionUpdate(FishWorld fishWorld) {
    return this.rest.collisionUpdate(fishWorld.canEatHelper(this.first));
  }

}

// an abstract class to represent snacks (extra stuff) 
abstract class ASnack {
  int id;
  CartPt position;
  WorldImage model;

  ASnack(int id, CartPt position) {
    this.id = id;
    this.position = position;
  }
  /*
   * template Fields:
   * 
   * Methods:
   * 
   * Methods on Fields:
   * 
   */

  // checks if the given snack (other) matches this snack
  public boolean snackMatch(ASnack other) {
    return this.id == other.id;
  }

  // draws a snack on a worldscene
  public abstract WorldScene drawSnacks(WorldScene acc);

  // retruns the pf with updated effects of a snack
  public abstract PlayerFish applyEffect(PlayerFish pf);

  // checks if the snack is colliding with the pf
  boolean checkSnackCollision(CartPt pfPosition, double pfSize) {
    // can we abstract this with the bg fish collision checker
    // but for now this method will do the job
    // X1 is rightmost point
    double sX1 = this.position.x + 15; // 15 is a placeholder for the radius of the snack
    // X2 is leftmost point
    double sX2 = this.position.x - 15;
    // Y1 is highest point
    double sY1 = this.position.y + 15;
    // Y2 is lowest point
    double sY2 = this.position.y - 15;
    // X1 is rightmost point
    double pfX1 = pfPosition.x + pfSize * 2;
    // X2 is leftmost point
    double pfX2 = pfPosition.x - pfSize * 2;
    // Y1 is highest point
    double pfY1 = pfPosition.y + pfSize;
    // Y2 is lowest point
    double pfY2 = pfPosition.y - pfSize;

    return sX2 < pfX1 && sX1 > pfX2 && sY2 < pfY1 && sY1 > pfY2;
  }

}

// a class to represent snacks that speed up the PlayerFish when eaten
class SpeedSnacks extends ASnack {
  int speedBoost = 5;
  WorldImage model = new OverlayImage(new TextImage("O", Color.BLACK),
      new CircleImage(30, OutlineMode.SOLID, Color.BLUE));

  SpeedSnacks(int id, CartPt position) {
    super(id, position);
  }
  /*
   * template
   */

  // draws this snack on a given worldscene
  public WorldScene drawSnacks(WorldScene acc) {
    return acc.placeImageXY(this.model, this.position.x, this.position.y);
  }

  // increases the speed of the pf
  public PlayerFish applyEffect(PlayerFish pf) {
    return pf.applyEffect(0, 0, this.speedBoost);
  }
}

// a class to represent snacks that decrease the size of the PlayerFish when eaten
class BadSnacks extends ASnack {
  // decrease size
  int sizeDecrease = -1;
  WorldImage model = new OverlayImage(new TextImage("X", Color.BLACK),
      new CircleImage(30, OutlineMode.SOLID, Color.RED));

  BadSnacks(int id, CartPt position) {
    super(id, position);
  }
  /*
   * template
   */

  // draws this snack on a given worldscene
  public WorldScene drawSnacks(WorldScene acc) {
    return acc.placeImageXY(this.model, this.position.x, this.position.y);
  }

  // decreases the size of the pf
  public PlayerFish applyEffect(PlayerFish pf) {
    return pf.applyEffect(this.sizeDecrease, 0, 0);
  }

}

// a class to represent snacks that increase the score of the PlayerFish when eaten
class ScoreSnacks extends ASnack {
  int scoreIncrease = 2;
  WorldImage model = new OverlayImage(new TextImage("!!", Color.BLACK),
      new CircleImage(30, OutlineMode.SOLID, Color.GREEN));

  ScoreSnacks(int id, CartPt position) {
    super(id, position);
  }
  /*
   * template
   */

  // draws this snack on a given worldscene
  public WorldScene drawSnacks(WorldScene acc) {
    return acc.placeImageXY(this.model, this.position.x, this.position.y);
  }

  // increases the speed of the pf
  public PlayerFish applyEffect(PlayerFish pf) {
    return pf.applyEffect(0, this.scoreIncrease, 0);
  }
}

// an interface to represent a list of Snacks
interface ILoSnacks {
  // 'eats' a snack off the snack list
  ILoSnacks eat(ASnack snack);

  // generates snacks and adds them to this list
  ILoSnacks generateSnack(int rand, int id);

  // draws this list of snacks on a worldscene
  WorldScene drawSnacks(WorldScene acc);

  // checks if the snacks are colliding
  ILoSnacks checkSnackCollision(PlayerFish pf);

  // updates the fishworld with the correct list of snacks
  FishWorld snackUpdate(FishWorld world);
}

// a class to represent the empty list
class MtLoSnacks implements ILoSnacks {

  /*
   * template
   */

  // cannot eat no snacks!
  public ILoSnacks eat(ASnack snack) {
    return this;
  }

  // generates snacks randomly and adds them to this list of snacks
  public ILoSnacks generateSnack(int rand, int id) {
    Random rand2 = new Random();
    if (rand <= 20) { // change the posn
      return new ConsLoSnacks(new SpeedSnacks(id, new CartPt(rand2.nextInt(), rand2.nextInt())),
          this);
    }
    else if (rand > 20 && rand <= 50) {
      return new ConsLoSnacks(new BadSnacks(id, new CartPt(rand2.nextInt(), rand2.nextInt())),
          this);
    }
    else if (rand > 40 && rand <= 70) { // change the posn
      return new ConsLoSnacks(new ScoreSnacks(id, new CartPt(rand2.nextInt(), rand2.nextInt())),
          this);
    }
    else
      return this;
  }

  // returns the given worldscene since there are no more snacks to draw
  public WorldScene drawSnacks(WorldScene acc) {
    return acc;
  }

  // returns mt list since there are no snacks to be collided with
  public ILoSnacks checkSnackCollision(PlayerFish pf) {
    return this;
  }

  // returns the given world state since there are no snacks to update
  public FishWorld snackUpdate(FishWorld world) {
    return world;
  }
}

// a class to represent the non-empty list
class ConsLoSnacks implements ILoSnacks {
  ASnack first;
  ILoSnacks rest;

  ConsLoSnacks(ASnack first, ILoSnacks rest) {
    this.first = first;
    this.rest = rest;
  }
  /*
   * template
   */

  // removes the given snack off this list
  public ILoSnacks eat(ASnack snack) {
    if (!this.first.snackMatch(snack)) {
      return new ConsLoSnacks(this.first, this.rest.eat(snack));
    }
    else {
      return this.rest;
    }
  }

  // generates a new snack and adds it to the list
  public ILoSnacks generateSnack(int rand, int id) {
    Random rand2 = new Random();
    if (rand <= 20) { // change the posn
      return new ConsLoSnacks(new SpeedSnacks(id, new CartPt(rand2.nextInt(), rand2.nextInt())),
          this);
    }
    else if (rand > 20 && rand <= 50) { // change the posn
      return new ConsLoSnacks(new BadSnacks(id, new CartPt(rand2.nextInt(), rand2.nextInt())),
          this);
    }
    else if (rand > 40 && rand <= 70) { // change the posn
      return new ConsLoSnacks(new ScoreSnacks(id, new CartPt(rand2.nextInt(), rand2.nextInt())),
          this);
    }
    else
      return this;
  }

  // draws this list of snacks on a worldscene
  public WorldScene drawSnacks(WorldScene acc) {
    return this.rest.drawSnacks(this.first.drawSnacks(acc));
  }

  // returns a list of snacks that are colliding with the playerfish
  public ILoSnacks checkSnackCollision(PlayerFish pf) {
    if (pf.checkSnackCollision(this.first)) {
      return new ConsLoSnacks(this.first, this.rest.checkSnackCollision(pf));
    }
    else {
      return this.rest.checkSnackCollision(pf);
    }
  }

  // NEEDS TO BE FIXED
  public FishWorld snackUpdate(FishWorld world) {
    if (world.eric.checkSnackCollision(this.first)) {
      PlayerFish newEric = world.eric.eatSnack(this.first);
      return new FishWorld(newEric, world.otherFishies, world.id, this.rest.eat(this.first));
    }
    else {
      FishWorld updatedWorld = this.rest.snackUpdate(world);
      return new FishWorld(updatedWorld.eric, updatedWorld.otherFishies, updatedWorld.id,
          new ConsLoSnacks(this.first, updatedWorld.snacks));
    }
  }

}

class CartPt {
  int x;
  int y;

  CartPt(int x, int y) {
    this.x = x;
    this.y = y;
  }
}

class ExamplesFishWorldProgram {
  PlayerFish deadFish = new PlayerFish(new CartPt(300, 300), 50, 0, 10, 3, 0);
  PlayerFish pFish1 = new PlayerFish(new CartPt(300, 600), 20, 0, 10, 3, 1);

  BackgroundFish bgFish1 = new BackgroundFish(1, new CartPt(250, 250));
  BackgroundFish bgFish2 = new BackgroundFish(2, new CartPt(1600, 1600));
  BackgroundFish bgFish3 = new BackgroundFish(5, new CartPt(400, 400));
  BackgroundFish bgFish4 = new BackgroundFish(6, new CartPt(500, 500));

  // pileBgFish1 is directly on top of pFish1 and is smaller than PF
  BackgroundFish pileBgFish1 = new BackgroundFish(3, new CartPt(300, 300));
  // pileBgFish1 is directly on top of pFish1 and is bigger than PF
  BackgroundFish pileBgFish2 = new BackgroundFish(4, new CartPt(300, 300));

  // pileBgFish lists
  ILoBackgroundFish tempList = new ConsLoBackgroundFish(bgFish2, new MtLoBackgroundFish());
  ILoBackgroundFish loBgFish1 = new ConsLoBackgroundFish(bgFish1, new MtLoBackgroundFish());
  ILoBackgroundFish loBgFish2 = new ConsLoBackgroundFish(bgFish2, loBgFish1);
  ILoBackgroundFish loBgFish3 = new ConsLoBackgroundFish(bgFish3, loBgFish2);
  ILoBackgroundFish loBgFish4 = new ConsLoBackgroundFish(bgFish4, loBgFish3);

  // used to test when a fish is directly over top PF
  ILoBackgroundFish pileLoFish1 = new ConsLoBackgroundFish(pileBgFish1, new MtLoBackgroundFish());
  ILoBackgroundFish pileLoFish2 = new ConsLoBackgroundFish(pileBgFish2, new MtLoBackgroundFish());

  // snacks
  ASnack speed1 = new SpeedSnacks(23, new CartPt(90, 90));
  ASnack bad1 = new BadSnacks(90, new CartPt(100, 100));
  ASnack score1 = new ScoreSnacks(19, new CartPt(200, 200));

  // lo snacks
  ILoSnacks snacks1 = new ConsLoSnacks(this.speed1,
      new ConsLoSnacks(this.bad1, new ConsLoSnacks(this.score1, new MtLoSnacks())));

  // used to test gameover
  FishWorld deadFishWorld = new FishWorld(deadFish, loBgFish1, 1, this.snacks1);

  // used to test when a bgfish is directly on top of pf
  FishWorld pileWorld1 = new FishWorld(pFish1, pileLoFish1, 2, this.snacks1);
  FishWorld pileWorld2 = new FishWorld(pFish1, pileLoFish2, 3, this.snacks1);

  // normal game state
  FishWorld FishWorld1 = new FishWorld(pFish1, loBgFish4, 3, this.snacks1);

  boolean testBigBang(Tester t) {
    FishWorld fw = FishWorld1;
    int worldWidth = 1600;
    int worldHeight = 950;
    double tickRate = 0.25;
    return fw.bigBang(worldWidth, worldHeight, tickRate);
  }

  boolean testCollision(Tester t) {
    return t.checkExpect(this.FishWorld1.returnCollidedFish(), new MtLoBackgroundFish())
        && t.checkExpect(this.pileWorld1.returnCollidedFish(), pileLoFish1)
        && t.checkExpect(this.pileWorld2.returnCollidedFish(), pileLoFish2);
  }
}

/*
 * info for README
 * 
 * 
 * Feeding Frenzy:
 * Eric Kitagawa & Emma Shum
 * Fundamentals of Computer Science II - Prof Razzaq
 * 
 * How to play:
 * - Use arrow keys to move the 'player fish' (orange fish)
 * - 'eat' other fish by running into them.
 * - you can only eat fish that are smaller than you
 * - you have 3 lives
 * - if you run into a fish that is bigger than you, a life will be removed
 * - once you are out of lives, the game is over
 * - you can win by becoming the biggest fish!
 * 
 * 
 */
