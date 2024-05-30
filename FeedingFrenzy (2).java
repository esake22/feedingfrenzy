import org.w3c.dom.Text;
import tester.*;
import javalib.worldimages.*;
import javalib.funworld.*;

import java.awt.*;
import java.util.Random;

class FishWorld extends World {
  PlayerFish eric;
  ILoBackgroundFish otherFishies;
  int id;
  int clockTick; // added clock tick for more control over timing of generation and life loss
  ILoSnacks snacks;
  // should these be instantiated here or in the constructor? for abstraction
  // here should be fine? - eric
  int windowW;
  int windowH;

  FishWorld(PlayerFish eric, ILoBackgroundFish otherFishies, int id, int clockTick, ILoSnacks snacks) {
    this.eric = eric;
    this.otherFishies = otherFishies;
    this.id = id;
    this.clockTick = clockTick;
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
   * this.moveFishiesFW() ... FishWorld
   * this.generateFish() ... FishWorld
   * this.caneat(ILoBackgroundFish) ... FishWorld
   * this.returnCollidedFish() ... ILoBackgroundFish
   * this.canEatHelper(BackgroundFish) ... FishWorld
   * this.updateClock ... FishWorld
   * this.generateFreebie ... FishWorld
   * -- SNACK METHODS ---
   *
   *
   * Methods on Fields:
   * this.otherFishies.eat(BackgroundFish) ... ILoBackgroundFish
   * this.otherFishies.drawBGfish ... WorldScene
   * this.otherFishies.moveFishies ... ILoBackgroundFish
   * this.otherFishies.addToLoBG(int, PlayerFish) ... ILoBackgroundFish
   * this.otherFishies.maxSpawn ... boolean
   * this.otherFishies.countGameFish ... int
   * this.otherFishies.addFreebie ... ILoBackgroundFish
   * this.otherFishies.isBossDead ... boolean
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
    else if (this.otherFishies.isBossDead()) {
      return this.lastScene("Game Won");
    }
    else {
      return this.draw(new WorldScene(windowW, windowH)
              .placeImageXY
                      (new RectangleImage
                              (windowW, windowH, OutlineMode.SOLID, Color.CYAN),
                              windowW / 2, windowH / 2))
              .placeImageXY
                      (new TextImage(String.valueOf(this.clockTick), 50, Color.BLACK), 1500,30);
    }
  }

  // method called every 'tick' and handles
  // fish collision (canEat)
  // snack collision (handleSnackCollision)
  // generation of bg fish (generateFish, generateFreebie)
  // ticks elapsed in program (updateClock)
  public FishWorld onTick() {
    if (this.clockTick % 50 == 0 && !this.otherFishies.maxSpawn()) { // generates new fish every 100 ticks
      return this.generateFish().moveFishiesFW().canEat(this.returnCollidedFish())
          .handleSnackCollision(this.returnCollidedSnacks()).updateClock();
    }
    else if (this.clockTick % 40 == 0) {
      return this.moveFishiesFW().canEat(this.returnCollidedFish())
              .handleSnackCollision(this.returnCollidedSnacks()).generateFreebie().updateClock();
    }
    else {
      return this.moveFishiesFW().canEat(this.returnCollidedFish())
              .handleSnackCollision(this.returnCollidedSnacks()).updateClock();
    }
  }

  // move the fish around the scene if an arrow key is pressed
  public FishWorld onKeyEvent(String key) {
    PlayerFish movedEric = this.eric.moveFish(key);
    return new FishWorld(movedEric, this.otherFishies, this.id, this.clockTick, this.snacks);
  }

  // renders the background fish and snacks using an accumulator.
  public WorldScene draw(WorldScene acc) {
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

  // moves the entire list of background fish
  public FishWorld moveFishiesFW() {
    return new FishWorld(this.eric, this.otherFishies.moveFishiesList(), this.id, this.clockTick, this.snacks);
  }

  // spawns in additional background fish
  FishWorld generateFish() {
    Random rand = new Random();
    return new FishWorld(this.eric, otherFishies.addToLoBG(this.id + 1, this.eric), this.id + 1, this.clockTick + 1,
        this.snacks);
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

  // COME BACK TO THIS
  FishWorld handleSnackCollision(ILoSnacks collidedSnacks) {
    return collidedSnacks.snackUpdate(this);
  }

  // COME BACK TO THIS
  ILoSnacks returnCollidedSnacks() {
    return this.snacks.checkSnackCollision(this.eric);
  }

  // updates clock, keeps track of how many ticks program has elapsed
  FishWorld updateClock() {
    return new FishWorld(this.eric, this.otherFishies, this.id, this.clockTick + 1, this.snacks);
  }

  // generates a bg fish that is always size 5, aka always edible
  FishWorld generateFreebie() {
    return new FishWorld(this.eric, this.otherFishies.addFreebie(this.id + 1), this.id + 1, this.clockTick + 1, this.snacks);
  }

  // updates the fishworld based on if the given background fish (which the
  // playerfish has collided with) will be eaten or not
  FishWorld canEatHelper(BackgroundFish otherFish) {
    if (otherFish.compareFishes(this.eric)) { // will return true when this fish CAN EAT
      return new FishWorld(this.eric.ateFishUpdate(otherFish), otherFishies.eat(otherFish), this.id, this.clockTick,
              this.snacks);
    }
    else {
      // If the BG fish is bigger, it'll remove a life from
      // the world state and determine if the game is over
      return new FishWorld(this.eric.updateLives(-1), otherFishies, this.id, this.clockTick, this.snacks);
    }
  }

  // updates the fishworld based on the eaten snack
  FishWorld snackUpdateHelper(ASnack snack) {
    return new FishWorld(snack.applyEffect(this.eric), this.otherFishies, this.id, this.clockTick, this.snacks.eatSnack(snack));
  }
}

class PlayerFish {
  CartPt position; // position of the fish on the world scene
  int size; // size of the fish, used for edible calculations and to determine size of world image model
  int score; // total points earned by the player
  int speed; // how quickly the player moves across the world scene
  int livesLeft; // how many times the player can touch a fish bigger than it // eat bad snack
  int direction; // which way (on x axis) fish is moving
  WorldImage skin; // world image depiction of fish

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
              this.size * 1.5, 0,
              (new RotateImage(
                  new EquilateralTriangleImage(this.size * 2, OutlineMode.SOLID, Color.ORANGE),
                  270))));
    }
    else {
      this.skin = new OverlayImage(
          new RectangleImage(this.size * 4, this.size * 2, OutlineMode.OUTLINE, Color.RED),
          new OverlayOffsetImage(
              new EllipseImage(this.size * 4, this.size * 2, OutlineMode.SOLID, Color.ORANGE),
              (this.size * -1.5), 0,
              (new RotateImage(
                  new EquilateralTriangleImage(this.size * 2, OutlineMode.SOLID, Color.ORANGE),
                  -270))));
    }
  }
  /* Template
   * Fields:
   * this.position ... CartPt
   * this.size ... int
   * this.score ... int
   * this.speed ... int
   * this.livesLeft ... int
   * this.direction ... int
   * this.skin ... WorldImage
   *
   * Methods:
   *
   * this.drawfish ... WorldScene
   * this.moveFish ... PlayerFish
   * this.ateFishUpdate ... PlayerFish
   * this.compareFishesHelp(int) ... boolean
   * this.updateLives(int) ... PlayerFish
   * this.outOfLives() ... boolean
   * this.checkCollision(ILoBackgroundFish) ... ILoBackgroundFish
   * this.getStats ... String
   * this.eatSnack ... PlayerFish
   * this.checkSnackCollision ... boolean
   * this.applyEffect ... PlayerFish
   * Methods on Fields:
   * 
   */

  // draws the playerfish on top of a given worldscene
  WorldScene drawFish(WorldScene acc) {
    return acc
            .placeImageXY(this.skin, this.position.x, this.position.y)
            .placeImageXY(new TextImage("Score: " + String.valueOf(this.score), 20, Color.BLACK), 1300, 15)
            .placeImageXY(new TextImage("Lives: " + String.valueOf(this.livesLeft), 20, Color.BLACK), 1400, 15);
  }

  // handles keys pressed and moves the playerfish accordingly
  PlayerFish moveFish(String key) {
    int X = this.position.x;
    int Y = this.position.y;
    int dir = this.direction;
    if (key.equals("up")) {
      Y = Y - this.speed;
    }
    else if (key.equals("down")) {
      Y = Y + this.speed;
    }
    else if (key.equals("left")) {
      X = X - this.speed;
      dir = 0;
    }
    else if (key.equals("right")) {
      X = X + this.speed;
      dir = 1;
    }

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
  boolean compareFishesHelp(int otherFishSize) {
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
  // returns multiple fish in case collision with multiple fish happens on same tick
  ILoBackgroundFish checkCollision(ILoBackgroundFish bgFishList) {
    return bgFishList.checkCollision(this.position, this.size);
  }

  // Displays the score and lives a player has on the world scene
  String getStats() {
    return "Score: " + this.score + " Lives Left: " + this.livesLeft;
  }

//  // COME BACK TO THIS
//  PlayerFish eatSnack(ASnack snack) {
//    return snack.applyEffect(this);
//  }

  // Method to check collision with a snack
  boolean checkSnackCollision(ASnack snack) {
    return snack.checkSnackCollision(this.position, this.size);

  }
}

// class to represent background fish
class BackgroundFish {
  int id; // unique integer assigned to every bgfish, used to track where it is in a list
  CartPt position; // determines where bg fish is on a scene
  int size; // how big the fish is, used to determine collision and visual on scene
  int speed; // how fast the fish moves
  int points; // how many points the player receives for eating said fish
  int direction; // which way (on x axis) fish is swimming
  WorldImage fishModel; // image of the bg fish to be displayed on world scene

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
   * this.id ... int
   * this.position ... CartPt
   * this.size ... int
   * this.speed ... int
   * this.points ... int
   * this.direction ... int
   * this.fishModel ... WorldImage
   *
   * Methods:
   *
   * this.calculateSizeGained(int) ... int
   * this.calculateScoreGained(int) ... int
   * this.compareFishes(PlayerFish) ... boolean
   * this.fishMatch(BackgroundFish) ... boolean
   * this.fishMatchHelper(int) ... boolean
   * this.drawBGfish(WorldScene) ... WorldScene
   * this.moveBGFish() ... BackgroundFish
   * this.checkCollisionHelper(CartPT, double) ... boolean
   * this.isFreebie ... boolean
   * this.isFishboss ... boolean
   * 
   * Methods on Fields:
   *
   * none
   */

  BackgroundFish(int id, CartPt position) {
    this.id = id;
    // random position of where bg fish is placed is determined in addToLoBG in FishWorld
    this.position = position;
    Random rand = new Random();
    // rolls a number between 0 - 99 inclusive to determine which category of
    // size the bg fish will fall into.
    int sizer = rand.nextInt(100);
    Color colorName;
    // Huge 5% chance of generation
    if (sizer > 94) {
      this.size = 70;
      this.speed = 10;
      colorName = Color.RED;
    }

    // Large 20% chance of generation
    else if (sizer > 74) {
      this.size = 40;
      this.speed = 12;
      colorName = Color.MAGENTA;
    }

    // Medium 45% chance of generation
    else if (sizer > 29) {
      this.size = 20;
      this.speed = 14;
      colorName = Color.BLUE;
    }

    // Small 20% chance of generation
    else if (sizer > 9) {
      this.size = 15;
      this.speed = 17;
      colorName = Color.GREEN;
    }

    // Tiny 10% chance of generation
    else {
      this.size = 10;
      this.speed = 20;
      colorName = Color.PINK;
    }

    // the score that players receive for eating this bg fish is just its size
    this.points = this.size;

    // randomly determines which direction the fish will swim
    // 0 is right, 1 is left
    this.direction = rand.nextInt(2);
    // determines which model will be used to represent the BG fish
    if (this.direction == 0) {
      this.fishModel =
              new OverlayImage(
                      new RectangleImage(this.size * 4, this.size * 2, OutlineMode.OUTLINE, Color.RED),
                      new OverlayOffsetImage(
                              new EllipseImage(this.size * 4, this.size * 2, OutlineMode.SOLID, colorName), this.size * 1.5, 0,
                              (new RotateImage(new EquilateralTriangleImage(this.size * 2, OutlineMode.SOLID, colorName),
                                      270))));
    }
    else {
      this.fishModel =
              new OverlayImage(
                      new RectangleImage(this.size * 4, this.size * 2, OutlineMode.OUTLINE, Color.RED),
                      new OverlayOffsetImage(
                              new EllipseImage(this.size * 4, this.size * 2, OutlineMode.SOLID, colorName), this.size * -1.5, 0,
                              (new RotateImage(new EquilateralTriangleImage(this.size * 2, OutlineMode.SOLID, colorName),
                                      -270))));
    }
  }

  // calculates how much size is gained from eating this bgfish
  // and adds it to the player fish's size
  int calculateSizeGained(int playerFishSize) {
    if (this.size == 70) {
      return playerFishSize + 6;
    }
    else if (this.size == 40) {
      return playerFishSize + 5;
    }

    else if (this.size == 20) {
      return playerFishSize + 4;
    }

    else if (this.size == 15) {
      return playerFishSize + 3;
    }

    else if (this.size == 10) {
      return playerFishSize + 2;
    }
    else {
      return playerFishSize + 2;
    }
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

  // determines if this bg fish and an other bg fish share the same id
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

  boolean isFreebie() {
    return this.size == 5;
  }

  // determines if this fish is the boss
  boolean isFishBoss() {
    return this.id == 999999999;
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
  ILoBackgroundFish addToLoBG(int id, PlayerFish pf);

  // moves all of the fish in the list
  ILoBackgroundFish moveFishiesList();

  // checks if the list contains the max amount of BG fish
  boolean maxSpawn();

  // counts the number of elements in a bg fish list
  int countGameFish();

  // generates a tiny fish that can be eaten by the player no matter size
  ILoBackgroundFish addFreebie(int id);
  //
  boolean isBossDead();
}

// a class to represent the empty list of background fish
class MtLoBackgroundFish implements ILoBackgroundFish {
  MtLoBackgroundFish() {
  }
  /*
   * Template
   * Fields:
   * Methods:
   * this.eat ... ILoBackground
   * this.drawBGfish ... WorldScene
   * this.checkCollision ... ILoBackgroundFish
   * this.collisionUpdate ... FishWorld
   * this.moveFishies ... ILoBackgroundFish
   * this.addToLoBG(int, PlayerFish) ... ILoBackgroundFish
   * this.maxSpawn ... boolean
   * this.countGameFish ... int
   * this.addFreebie ... ILoBackgroundFish
   * this.isBossDead ... boolean
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
  public ILoBackgroundFish moveFishiesList() {
    return this;
  }

  // 'generates' a new bgfish randomly
  public ILoBackgroundFish addToLoBG(int id, PlayerFish pf) {
    // can be abstracted
    Random randY = new Random();
    Random randX = new Random();
    int X;
    int Y;
    // ensures that a bg fish won't spawn directly ontop of the Playerfish
    if (randX.nextBoolean()) {
      X = (randX.nextInt(pf.position.x) - (pf.size * 4 + 50)) % 1600;
    }
    else {
      X = (randX.nextInt(1600) + (pf.size * 4 + 50) + pf.position.x) % 1600;
    }
    if (randY.nextBoolean())
      Y = (randY.nextInt(pf.position.y) - (pf.size * 2 + 25)) % 950;
    else {
      Y = (randY.nextInt(950) + (pf.size * 2 + 25) + pf.position.y) % 950;
    }

    return new ConsLoBackgroundFish(
            new BackgroundFish(id, new CartPt(X, Y)), this);
  }

  // checks to see if max number of fish are generated in the world
  public boolean maxSpawn() {
    return false;
  }

  // counts the number of elements in a list of bg fish, except for freebies
  public int countGameFish() {
    return 0;
  }

  // generates a fish that can be eaten at any playerfish size
  public ILoBackgroundFish addFreebie(int id) {
      Random randX = new Random();
      Random randY = new Random();
      Random randDir = new Random();
      return new ConsLoBackgroundFish(new BackgroundFish (id, new CartPt(randX.nextInt(1600), randY.nextInt(950)), 5, 20, 1, randDir.nextInt(1),
              new OverlayOffsetImage(
                      new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE),
                      7.5, 0,
                      (new RotateImage(
                              new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE),
                              270)))), this);
  }
  public boolean isBossDead() {
    return true;
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

  /* Template
  * Fields
  * this.first ... BackgroundFish
  * this.rest ... ILoBackgroundFish
  *
  * Methods
  * this.eat(BackgroundFish) ... ILoBackgroundFish
  * this.drawBGfish ... WorldScene
  * this.moveFishiesList ... ILoBackgroundFish
  * this.addToLoBG(int, PlayerFish) ... ILoBackgroundFish
  * this.maxSpawn ... boolean
  * this.countGameFish ... int
  * this.addFreebie ... ILoBackgroundFish
  * this.isBossDead ... boolean
  *
  * Fields of Methods
  * this.first.calculateSizeGained(int) ... int
  * this.first.calculateScoreGained(int) ... int
  * this.first.compareFishes(PlayerFish) ... boolean
  * this.first.fishMatch(BackgroundFish) ... boolean
  * this.first.fishMatchHelper(int) ... boolean
  * this.first.drawBGfish(WorldScene) ... WorldScene
  * this.first.moveBGFish() ... BackgroundFish
  * this.first.checkCollisionHelper(CartPT, double) ... boolean
  * this.first.isFreebie ... boolean
  * this.first.isFishboss ... boolean
  */

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
  public ILoBackgroundFish moveFishiesList() {
    return new ConsLoBackgroundFish(this.first.moveBGFish(), this.rest.moveFishiesList());
  }

  // 'generates' a new bgfish randomly and adds it to the current list of bg fish
  public ILoBackgroundFish addToLoBG(int id, PlayerFish pf) {
    Random randY = new Random();
    Random randX = new Random();
    int X;
    int Y;
    // ensures that a bg fish won't spawn directly ontop of the Playerfish
    if (randX.nextBoolean()) {
      X = (randX.nextInt(pf.position.x) - (pf.size * 4 + 50)) % 1600;
    }
    else {
      X = (randX.nextInt(1600) + (pf.size * 4 + 50) + pf.position.x) % 1600;
    }
    if (randY.nextBoolean())
      Y = (randY.nextInt(pf.position.y) - (pf.size * 2 + 25)) % 950;
    else {
      Y = (randY.nextInt(950) + (pf.size * 2 + 25) + pf.position.y) % 950;
    }

      return new ConsLoBackgroundFish(
              new BackgroundFish(id, new CartPt(X, Y)), this);
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

  // counts amount of elements in a list of bg fish EXCEPT for freebie fish
  public int countGameFish() {
    if (this.first.isFreebie()) {
      return this.rest.countGameFish();
    }
    else {
      return 1 + this.rest.countGameFish();
    }
  }

  // checks to see if the maximum amount of bg fish are generated in the world
  public boolean maxSpawn() {
    return this.countGameFish() == 20;
  }

  public ILoBackgroundFish addFreebie(int id) {
      Random randX = new Random();
      Random randY = new Random();
      Random randDir = new Random();
      int dir = randDir.nextInt(2);
      if (dir == 0) {
        return new ConsLoBackgroundFish(new BackgroundFish(id, new CartPt(randX.nextInt(1600), randY.nextInt(950)), 5, 20, 1, dir,
                new OverlayOffsetImage(
                        new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE),
                        7.5, 0,
                        (new RotateImage(
                                new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE),
                                270)))), this);
      }

      else {
        return new ConsLoBackgroundFish(new BackgroundFish(id, new CartPt(randX.nextInt(1600), randY.nextInt(950)), 5, 20, 1, dir,
                new OverlayOffsetImage(
                        new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE),
                        -7.5, 0,
                        (new RotateImage(
                                new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE),
                                -270)))), this);
      }
  }

  // determines if the list contains the boss, if it doesn't then the boss is dead
  public boolean isBossDead() {
    if (this.first.isFishBoss()) {
      return false;
    } else {
      return this.rest.isBossDead();
    }
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

  // applies the effect of a snack eaten by the playerFish
  public PlayerFish applyEffect (PlayerFish pf) {
    return new PlayerFish(this.position, pf.size, pf.score,
            pf.speed + this.speedBoost, pf.livesLeft, pf.direction);
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

  // decreases the size of playerfish when it eats this snack
  public PlayerFish applyEffect(PlayerFish pf) {
    return new PlayerFish(this.position, pf.size + this.sizeDecrease, pf.score,
            pf.speed, pf.livesLeft, pf.direction);
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

  // decreases the score of playerfish when it eats this snack
  public PlayerFish applyEffect(PlayerFish pf) {
    return new PlayerFish(this.position, pf.size, pf.score + this.scoreIncrease,
            pf.speed, pf.livesLeft, pf.direction);
  }
}

// an interface to represent a list of Snacks
interface ILoSnacks {
  // 'eats' a snack off the snack list
  ILoSnacks eatSnack(ASnack snack);

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
  public ILoSnacks eatSnack(ASnack snack) {
    return this;
  }

  // generates snacks randomly and adds them to this list of snacks
  public ILoSnacks generateSnack(int rand, int id) {
    Random rand2 = new Random();
    if (rand <= 20) { // change the posn
      return new ConsLoSnacks(new SpeedSnacks(id, new CartPt(rand2.nextInt(), rand2.nextInt())),
          this);
    }
    else if (rand <= 50) {
      return new ConsLoSnacks(new BadSnacks(id, new CartPt(rand2.nextInt(), rand2.nextInt())),
          this);
    }
    else if (rand <= 70) { // change the posn
      return new ConsLoSnacks(new ScoreSnacks(id, new CartPt(rand2.nextInt(), rand2.nextInt())),
          this);
    }
    else {
      return this;
    }
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
  public ILoSnacks eatSnack(ASnack snack) {
    if (this.first.snackMatch(snack)) {
      return this.rest;
    }
    else {
      return new ConsLoSnacks(this.first, this.rest.eatSnack(snack));
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
    return this.rest.snackUpdate(world.snackUpdateHelper(this.first));

//    if (world.eric.checkSnackCollision(this.first)) {
//      PlayerFish newEric = world.eric.eatSnack(this.first);
//      return new FishWorld(newEric, world.otherFishies, world.id, world.clockTick, this.rest.eat(this.first));
//    }
//    else {
//      FishWorld updatedWorld = this.rest.snackUpdate(world);
//      return new FishWorld(updatedWorld.eric, updatedWorld.otherFishies, updatedWorld.id, updatedWorld.clockTick,
//          new ConsLoSnacks(this.first, updatedWorld.snacks));
//    }
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
  PlayerFish pilePFish = new PlayerFish(new CartPt(300, 600), 5, 0, 10, 3, 1);

  PlayerFish pFish1 = new PlayerFish(new CartPt(300, 20), 5, 0, 10, 3, 1);

  // BG Fish Boss <- edit this to change win cond, don't change id
  BackgroundFish fishBoss = new BackgroundFish(999999999, new CartPt(1600, 830), 100, 2, 1000, 1,
          new OverlayOffsetImage(
                  new EllipseImage(400, 200, OutlineMode.SOLID, Color.BLACK), -150, 0,
                  (new RotateImage(new EquilateralTriangleImage(200, OutlineMode.SOLID, Color.BLACK),
                          -270))));

  // BG Freebie
  BackgroundFish freebie = new BackgroundFish(100, new CartPt (0, 0), 5, 20, 1, 1,
          new OverlayOffsetImage(
                  new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE),
                  7.5, 0,
                  (new RotateImage(
                          new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE),
                          270))));

  // Huge BG fish
  BackgroundFish hugeFishR = new BackgroundFish(1, new CartPt(0, 0), 70, 10, 70, 1,
          new OverlayOffsetImage(
                  new EllipseImage(280, 140, OutlineMode.SOLID, Color.RED), -140, 0,
                  (new RotateImage(new EquilateralTriangleImage(140, OutlineMode.SOLID, Color.RED),
                          -270))));

  BackgroundFish hugeFishL = new BackgroundFish(2, new CartPt(0, 0), 70, 10, 70, 0,
          new OverlayOffsetImage(
                  new EllipseImage(280, 140, OutlineMode.SOLID, Color.RED), 140, 0,
                  (new RotateImage(new EquilateralTriangleImage(140, OutlineMode.SOLID, Color.RED),
                          270))));

  // Large BG Fish
  BackgroundFish largeFishR = new BackgroundFish(3, new CartPt(0, 0), 40, 12, 40, 1,
          new OverlayOffsetImage(
                  new EllipseImage(160, 80, OutlineMode.SOLID, Color.MAGENTA), -80, 0,
                  (new RotateImage(new EquilateralTriangleImage(80, OutlineMode.SOLID, Color.MAGENTA),
                          -270))));

  BackgroundFish largeFishL = new BackgroundFish(4, new CartPt(0, 0), 40, 12, 40, 0,
          new OverlayOffsetImage(
                  new EllipseImage(160, 80, OutlineMode.SOLID, Color.MAGENTA), 80, 0,
                  (new RotateImage(new EquilateralTriangleImage(80, OutlineMode.SOLID, Color.MAGENTA),
                          270))));

  // Medium BG Fish
  BackgroundFish mediumFishR = new BackgroundFish(5, new CartPt(0, 0), 20, 14, 20, 1,
          new OverlayOffsetImage(
                  new EllipseImage(80, 40, OutlineMode.SOLID, Color.BLUE), -40, 0,
                  (new RotateImage(new EquilateralTriangleImage(80, OutlineMode.SOLID, Color.BLUE),
                          -270))));

  BackgroundFish mediumFishL = new BackgroundFish(6, new CartPt(0, 0), 20, 14, 20, 0,
          new OverlayOffsetImage(
                  new EllipseImage(80, 40, OutlineMode.SOLID, Color.BLUE), 40, 0,
                  (new RotateImage(new EquilateralTriangleImage(80, OutlineMode.SOLID, Color.BLUE),
                          270))));

  // Small BG Fish
  BackgroundFish smallFishR = new BackgroundFish(7, new CartPt(0, 0), 15, 17, 15, 1,
          new OverlayOffsetImage(
                  new EllipseImage(60, 30, OutlineMode.SOLID, Color.GREEN), -30, 0,
                  (new RotateImage(new EquilateralTriangleImage(30, OutlineMode.SOLID, Color.GREEN),
                          -270))));

  BackgroundFish smallFishL = new BackgroundFish(8, new CartPt(0, 0), 15, 17, 15, 0,
          new OverlayOffsetImage(
                  new EllipseImage(60, 30, OutlineMode.SOLID, Color.GREEN), 30, 0,
                  (new RotateImage(new EquilateralTriangleImage(30, OutlineMode.SOLID, Color.GREEN),
                          270))));

  // Tiny BG Fish
  BackgroundFish tinyFishR = new BackgroundFish(9, new CartPt(0, 0), 10, 20, 10, 1,
          new OverlayOffsetImage(
                  new EllipseImage(40, 20, OutlineMode.SOLID, Color.PINK), -20, 0,
                  (new RotateImage(new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.PINK),
                          -270))));

  BackgroundFish tinyFishL = new BackgroundFish(10, new CartPt(0, 0), 10, 20, 10, 0,
          new OverlayOffsetImage(
                  new EllipseImage(40, 20, OutlineMode.SOLID, Color.PINK), 20, 0,
                  (new RotateImage(new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.PINK),
                          270))));

  // List of BG fish using preset sizes
  ILoBackgroundFish mt = new MtLoBackgroundFish();
  ILoBackgroundFish loBGfish0 = new ConsLoBackgroundFish(fishBoss, mt);
  ILoBackgroundFish loBGfish1 = new ConsLoBackgroundFish(tinyFishL, loBGfish0);
  ILoBackgroundFish loBGfish2 = new ConsLoBackgroundFish(smallFishR, loBGfish1);
  ILoBackgroundFish loBGfish3 = new ConsLoBackgroundFish(mediumFishL, loBGfish2);
  ILoBackgroundFish loBGfish4 = new ConsLoBackgroundFish(largeFishR, loBGfish3);
  ILoBackgroundFish loBGfish5 = new ConsLoBackgroundFish(hugeFishR, loBGfish4);
  ILoBackgroundFish loBGfish6 = new ConsLoBackgroundFish(freebie, loBGfish5);
  // All going left (dir0)
  ILoBackgroundFish loBGfish1L = new ConsLoBackgroundFish(tinyFishL, mt);
  ILoBackgroundFish loBGfish2L = new ConsLoBackgroundFish(smallFishL, loBGfish1L);
  ILoBackgroundFish loBGfish3L = new ConsLoBackgroundFish(mediumFishL, loBGfish2L);
  ILoBackgroundFish loBGfish4L = new ConsLoBackgroundFish(largeFishL, loBGfish3L);
  ILoBackgroundFish loBGfish5L = new ConsLoBackgroundFish(hugeFishL, loBGfish4L);
  // All going Right (dir 1)
  ILoBackgroundFish loBGfish1R = new ConsLoBackgroundFish(tinyFishR, mt);
  ILoBackgroundFish loBGfish2R = new ConsLoBackgroundFish(smallFishR, loBGfish1R);
  ILoBackgroundFish loBGfish3R = new ConsLoBackgroundFish(mediumFishR, loBGfish2R);
  ILoBackgroundFish loBGfish4R = new ConsLoBackgroundFish(largeFishR, loBGfish3R);
  ILoBackgroundFish loBGfish5R = new ConsLoBackgroundFish(hugeFishR, loBGfish4R);
  // Freebie lists
  ILoBackgroundFish loFreebie1 = new ConsLoBackgroundFish(freebie, mt);
  ILoBackgroundFish loFreebie2 = new ConsLoBackgroundFish(freebie, loFreebie1);
  // list of freebie fish with bg fish that's not freebie
  ILoBackgroundFish loFreebie3 = new ConsLoBackgroundFish(fishBoss, loFreebie2);

  BackgroundFish bgFish1 = new BackgroundFish(1, new CartPt(1600, 250), 20, 20, 1, 1,
          new OverlayOffsetImage(
                  new EllipseImage(80, 40, OutlineMode.SOLID, Color.BLUE), -40, 0,
                  (new RotateImage(new EquilateralTriangleImage(40, OutlineMode.SOLID, Color.BLUE),
                          -270))));
  BackgroundFish bgFish2 = new BackgroundFish(2, new CartPt(1600, 400), 40, 10, 2, 0,
          new OverlayOffsetImage(
                  new EllipseImage(160, 80, OutlineMode.SOLID, Color.YELLOW), 80, 0,
                  (new RotateImage(new EquilateralTriangleImage(80, OutlineMode.SOLID, Color.YELLOW),
                          270))));
  BackgroundFish bgFish3 = new BackgroundFish(5, new CartPt(1600, 700), 50, 16, 3, 1,
          new OverlayOffsetImage(
                  new EllipseImage(200, 100, OutlineMode.SOLID, Color.GREEN), -100, 0,
                  (new RotateImage(new EquilateralTriangleImage(100, OutlineMode.SOLID, Color.GREEN),
                          -270))));


  // pileBgFish1 is directly on top of pFish1 and is smaller than PF
  BackgroundFish pileBgFish1 = new BackgroundFish(3, new CartPt(300, 600));
  // pileBgFish1 is directly on top of pFish1 and is bigger than PF
  BackgroundFish pileBgFish2 = new BackgroundFish(4, new CartPt(300, 600));

  // Background lists
  ILoBackgroundFish mtList = new MtLoBackgroundFish();
  ILoBackgroundFish tempList = new ConsLoBackgroundFish(bgFish2, new MtLoBackgroundFish());
  ILoBackgroundFish loBgFish1 = new ConsLoBackgroundFish(bgFish1, new MtLoBackgroundFish());
  ILoBackgroundFish loBgFish2 = new ConsLoBackgroundFish(bgFish2, loBgFish1);
  ILoBackgroundFish loBgFish3 = new ConsLoBackgroundFish(bgFish3, loBgFish2);
  ILoBackgroundFish loBgFish4 = new ConsLoBackgroundFish(fishBoss, loBgFish3);

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
  FishWorld deadFishWorld = new FishWorld(deadFish, loBgFish1, 1, 0, this.snacks1);

  // used to test when a bgfish is directly on top of pf
  FishWorld pileWorld1 = new FishWorld(pilePFish, pileLoFish1, 2, 0, this.snacks1);
  FishWorld pileWorld2 = new FishWorld(pilePFish, pileLoFish2, 3, 0, this.snacks1);

  // normal game state
  FishWorld FishWorld1 = new FishWorld(pFish1, loBgFish4, 3, 0, this.snacks1);
  FishWorld fw0 = new FishWorld(pFish1, mt, 20, 0, this.snacks1);
  FishWorld fw1 = new FishWorld(pFish1, loBGfish0, 20, 0, this.snacks1);
  FishWorld fw2 = new FishWorld(pFish1, loBGfish1, 20, 0, this.snacks1);
  FishWorld fwFreebie = new FishWorld(pFish1, loFreebie1, 20, 0, this.snacks1);


  boolean testBigBang(Tester t) {
    FishWorld fw = FishWorld1;
    int worldWidth = 1600;
    int worldHeight = 950;
    double tickRate = 0.1;
    return fw.bigBang(worldWidth, worldHeight, tickRate);
  }

  // FishWorld Tests

  boolean testMoveFishiesFW(Tester t) {
    return t.checkExpect(this.fw0.moveFishiesFW(), fw0)
            && t.checkExpect(this.fw1.moveFishiesFW(),
            new FishWorld(pFish1, new ConsLoBackgroundFish(
                    new BackgroundFish(999999999, new CartPt(0, 830), 100, 2, 1000, 1,
                            new OverlayOffsetImage(
                                    new EllipseImage(400, 200, OutlineMode.SOLID, Color.BLACK), -150, 0,
                                    (new RotateImage(new EquilateralTriangleImage(200, OutlineMode.SOLID, Color.BLACK),
                                            -270)))), mt), 20, 0, this.snacks1))
            && t.checkExpect(this.fw2.moveFishiesFW(),
            new FishWorld(pFish1, new ConsLoBackgroundFish(
                    new BackgroundFish(10, new CartPt(1600, 0), 10, 20, 10, 0,
                            new OverlayOffsetImage(
                                    new EllipseImage(40, 20, OutlineMode.SOLID, Color.PINK), 20, 0,
                                    (new RotateImage(new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.PINK),
                                            270)))),
                    new ConsLoBackgroundFish(
                            new BackgroundFish(999999999, new CartPt(0, 830), 100, 2, 1000, 1,
                                    new OverlayOffsetImage(
                                            new EllipseImage(400, 200, OutlineMode.SOLID, Color.BLACK), -150, 0,
                                            (new RotateImage(new EquilateralTriangleImage(200, OutlineMode.SOLID, Color.BLACK),
                                                    -270))))
                            , mt)), 20, 0, this.snacks1));
  }

  boolean testCollision(Tester t) {
    return t.checkExpect(this.FishWorld1.returnCollidedFish(), new MtLoBackgroundFish())
        && t.checkExpect(this.pileWorld1.returnCollidedFish(), pileLoFish1)
        && t.checkExpect(this.pileWorld2.returnCollidedFish(), pileLoFish2);
  }

  boolean testCountGameFish(Tester t) {
    return t.checkExpect(this.loBgFish1.countGameFish(), 1)
            && t.checkExpect(this.loBgFish2.countGameFish(), 2)
            && t.checkExpect(mtList.countGameFish(), 0)
            && t.checkExpect(loFreebie1.countGameFish(), 0)
            && t.checkExpect(loFreebie2.countGameFish(), 0)
            && t.checkExpect(loFreebie3.countGameFish(), 1);
  }

  boolean testUpdateClock(Tester t) {
    return t.checkExpect(this.FishWorld1.updateClock(),
            new FishWorld(this.pFish1, loBgFish4, 3, 1, this.snacks1))
            && t.checkExpect(new FishWorld(this.pFish1, loBgFish4, 3, 1, this.snacks1).updateClock(),
            new FishWorld(this.pFish1, loBgFish4, 3, 2, this.snacks1));
  }

  boolean testIsBossDead(Tester t) {
    return t.checkExpect(this.loBgFish4.isBossDead(), false)
            && t.checkExpect(this.loBgFish2.isBossDead(), true)
            && t.checkExpect(this.mtList.isBossDead(), true);
  }
  boolean testIsBoss(Tester t) {
    return t.checkExpect(this.fishBoss.isFishBoss(), true)
            && t.checkExpect(this.bgFish1.isFishBoss(), false);
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
