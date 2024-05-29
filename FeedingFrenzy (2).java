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

  FishWorld(PlayerFish eric, ILoBackgroundFish otherFishies, int id, ILoSnacks snacks) {
    this.eric = eric;
    this.otherFishies = otherFishies;
    this.id = id;
    this.snacks = snacks;
  }

  public WorldScene makeScene() {
    // renders current fish
    // renders background
    // renders a score / display
    // System.out.println("makeScene: " + this.eric.position.x + ", " +
    // this.eric.position.y);
    if (this.eric.outOfLives()) {
      return this.lastScene("Game Lost");
    }
    else if (this.isBiggestFish()) {
      return this.lastScene("Game Won");
    }
    else {
      return this.draw(new WorldScene(1600, 950));
    }
  }

  public FishWorld onTick() {
    // System.out.println("Tick: " + this.eric.position.x + ", " +
    // this.eric.position.y);
    // slow tick condition --> moveFishies & generateFish
    Random rand = new Random();
    if (rand.nextInt(100) > 80) {
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

  public WorldScene draw(WorldScene acc) {
    // (DEBUGGER) System.out.println("Drawing at: " + this.eric.position.x + ", " +
    // this.eric.position.y);
    // Creates the world with the BGfish first then passes
    // it as an accumulator into draw player fish method
    WorldScene bgFishScene = this.otherFishies.drawBGfish(acc);
    WorldScene withSnacksScene = this.snacks.drawSnacks(bgFishScene);
    return this.eric.drawFish(withSnacksScene);
  }

  public FishWorld moveFishies() {
    return new FishWorld(this.eric, this.otherFishies.moveFishies(), this.id, this.snacks);
  }

  @Override
  public WorldScene lastScene(String msg) {
    if (msg.equals("Game Won")) {
      return new WorldScene(500, 500).placeImageXY(new TextImage("Game Won", 50, Color.BLACK), 250,
          250); // with game WOn display and stats
    }
    if (msg.equals("Game Lost")) {
      return new WorldScene(500, 500).placeImageXY(new TextImage("Game Lost", 50, Color.BLACK), 250,
          250); // with game LOST display and stats
    }
    else {
      return new WorldScene(500, 500);
    }
  }

  public boolean isBiggestFish() {
    return this.eric.biggestFish();
  }

  // Generate fish
  FishWorld generateFish() {
    // some way to generate a random number
    // use that number to generate a preset fish
    Random rand = new Random();
    return new FishWorld(this.eric, otherFishies.addToLoBG(this.id++, rand.nextInt(100)), this.id++,
        this.snacks);
  }

  // takes in a list of bg fish that collided with the player fish
  // and determines if they can be eaten by the player or if a live needs to be
  // taken
  FishWorld canEat(ILoBackgroundFish collidedFish) {
    return collidedFish.collisionUpdate(this);
  }

  // checks if the player has collided with any background fish and returns a list
  // of
  // collided
  ILoBackgroundFish returnCollidedFish() {
    return this.eric.checkCollision(this.otherFishies);
  }

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

  FishWorld handleSnackCollision(ILoSnacks collidedSnacks) {
    return collidedSnacks.snackUpdate(this);
  }

  ILoSnacks returnCollidedSnacks() {
    return this.snacks.checkSnackCollision(this.eric);
  }
}

class PlayerFish {
  CartPt position;
  int size;
  int score;
  int livesLeft;
  int speed;
  WorldImage skin;

  PlayerFish(CartPt position, int size, int score, int speed, int livesLeft) {
    this.position = position;
    this.size = size;
    this.score = score;
    this.speed = speed;
    this.livesLeft = livesLeft;
    this.skin = new OverlayImage(
        new RectangleImage(this.size * 4, this.size * 2, OutlineMode.OUTLINE, Color.RED),
        new OverlayOffsetImage(
            new EllipseImage(this.size * 4, this.size * 2, OutlineMode.SOLID, Color.ORANGE),
            this.size * 2, 0,
            (new RotateImage(
                new EquilateralTriangleImage(this.size * 2, OutlineMode.SOLID, Color.ORANGE),
                270))));
  }

  // renders eric
  WorldScene drawFish(WorldScene acc) {
    return acc
        .placeImageXY(new TextImage("Score: " + String.valueOf(this.score), 20, Color.BLACK), 1490,
            15)
        .placeImageXY(new CircleImage(10, OutlineMode.SOLID, Color.BLACK), 800, 80)
        .placeImageXY(new CircleImage(10, OutlineMode.SOLID, Color.BLACK), 800, 260)
        .placeImageXY(new CircleImage(10, OutlineMode.SOLID, Color.BLACK), 800, 440)
        .placeImageXY(new CircleImage(10, OutlineMode.SOLID, Color.BLACK), 800, 620)
        .placeImageXY(new CircleImage(10, OutlineMode.SOLID, Color.BLACK), 800, 870)
        .placeImageXY(this.skin, this.position.x, this.position.y);
  }

  // methods
  // update position
  // --> needs to check whether we've bumped into a fish, call canEat?
  // needs to be able to loop through screen
  PlayerFish moveFish(String key) {
    int X = this.position.x;
    int Y = this.position.y;
    if (key.equals("up")) {
      // make movement adjustable for the speedmod
      // other movement TBD
      Y -= this.speed; // * speedMod;
    }
    else if (key.equals("down")) {
      Y += this.speed;
    }
    else if (key.equals("left")) {
      X -= this.speed;
    }
    else if (key.equals("right")) {
      X += this.speed;
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

    return new PlayerFish(new CartPt(X, Y), this.size, this.score, this.speed, this.livesLeft);
  }

  // --> if no, gameOver (lost)
  // --> if yes, call eat (removes the eaten fish from the ILoBackgroundFish

  /*
   * gameOver? --> returns Fish/FishWOrld --> check # lives if won -->
   * this.endOfWorld("A message") if lost --> this.endOfWorld("A message") if
   * nothing --> this
   *
   */

  PlayerFish ateFishUpdate(BackgroundFish otherFish) {
    return new PlayerFish(this.position, otherFish.calculateSizeGained(this.size),
        otherFish.calculateScoreGained(this.score), this.speed, this.livesLeft);
  }

  // checks if the size of this PlayerFish is greater than a BackgroundFish
  Boolean compareFishesHelp(double otherFishSize) {
    return this.size >= otherFishSize;
  }

  PlayerFish updateLives(int updateAmt) {
    return new PlayerFish(this.position, this.size, this.score, this.speed,
        updateAmt + this.livesLeft);
  }

  boolean outOfLives() {
    return this.livesLeft <= 0;
  }

  ILoBackgroundFish checkCollision(ILoBackgroundFish bgFishList) {
    return bgFishList.checkCollision(this.position, this.size);
  }

  boolean biggestFish() {
    return this.size > 80;
  }

  PlayerFish eatSnack(ASnack snack) {
    return snack.applyEffect(this);
  }

  // Method to check collision with a snack
  boolean checkSnackCollision(ASnack snack) {
    return snack.checkSnackCollision(this.position, this.size);
 
  }

}

class BackgroundFish {
  /*
   * fields: position (y position) --> random size --> random (with limit) speed
   * --> random score --> based on size
   *
   * fish constructor should be random
   */
  String name;
  int id;
  CartPt position;
  int size;
  int speed;
  int points;
  WorldImage fishModel;

  // PLACEHOLDER BGFISH MODEL
  WorldImage tinyModel = new OverlayOffsetImage(
      new EllipseImage(20, 10, OutlineMode.SOLID, Color.GREEN), 10, 0,
      (new RotateImage(new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.GREEN), 270)));
  WorldImage smallModel = new OverlayOffsetImage(
      new EllipseImage(80, 40, OutlineMode.SOLID, Color.RED), 40, 0,
      (new RotateImage(new EquilateralTriangleImage(40, OutlineMode.SOLID, Color.RED), 270)));
  WorldImage mediumModel = new OverlayOffsetImage(
      new EllipseImage(160, 80, OutlineMode.SOLID, Color.YELLOW), 80, 0,
      (new RotateImage(new EquilateralTriangleImage(80, OutlineMode.SOLID, Color.YELLOW), 270)));
  WorldImage largeModel = new OverlayOffsetImage(
      new EllipseImage(240, 120, OutlineMode.SOLID, Color.BLUE), 120, 0,
      (new RotateImage(new EquilateralTriangleImage(120, OutlineMode.SOLID, Color.BLUE), 270)));
  WorldImage hugeModel = new OverlayOffsetImage(
      new EllipseImage(320, 160, OutlineMode.SOLID, Color.BLACK), 160, 0,
      (new RotateImage(new EquilateralTriangleImage(160, OutlineMode.SOLID, Color.BLACK), 270)));

  BackgroundFish(String name, int id, CartPt position, int size, int speed, int points,
      WorldImage fishModel) {
    this.name = name;
    this.id = id;
    this.position = position;
    this.size = size;
    this.speed = speed;
    this.points = points;
    this.fishModel = fishModel;
  }

  BackgroundFish(String name, int id, CartPt position) {
    this.name = name;
    this.id = id;
    this.position = position;
    if (name.equals("Tiny")) {
      this.size = 5;
      this.speed = 20;
      this.points = 1;
      ;
      this.fishModel = this.tinyModel;

    }
    else if (name.equals("Small")) {
      this.size = 20;
      this.speed = 20;
      this.points = 2;
      this.fishModel = this.smallModel;
    }
    else if (name.equals("Medium")) {
      this.size = 40;
      this.speed = 20;
      this.points = 3;
      this.fishModel = this.mediumModel;
    }
    else if (name.equals("Large")) {
      this.size = 60;
      this.speed = 20;
      this.points = 4;
      this.fishModel = this.largeModel;
    }
    else if (name.equals("Huge")) {
      this.size = 80;
      this.speed = 20;
      this.points = 5;
      this.fishModel = this.hugeModel;
    }
  }

  // calculates how much size is gained from eating this fish
  // and adds it to the player fish's size
  int calculateSizeGained(int playerFishSize) {
    return this.size + playerFishSize;
  }

  // calculates how many points are gained from eating this fish
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

  WorldScene drawBGfish(WorldScene acc) {
    return acc.placeImageXY(this.fishModel, this.position.x, this.position.y);
  }

  BackgroundFish moveBGFish() {
    int X = this.position.x - this.speed;

    if (X < 0)
      X = 1600;
    if (X > 1600)
      X = 0;

    return new BackgroundFish(this.name, this.id, new CartPt(X, this.position.y));
  }

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

interface ILoBackgroundFish {
  // searches a list for that fish and returns a
  // a list of bg fish with that one removed
  ILoBackgroundFish eat(BackgroundFish otherFish);

  WorldScene drawBGfish(WorldScene acc);

  ILoBackgroundFish checkCollision(CartPt pfPosition, double pfSize);

  FishWorld collisionUpdate(FishWorld fishWorld);

  ILoBackgroundFish addToLoBG(int id, int randomNum);

  ILoBackgroundFish moveFishies();

}

class MtLoBackgroundFish implements ILoBackgroundFish {
  MtLoBackgroundFish() {
  }

  public ILoBackgroundFish eat(BackgroundFish otherFish) {
    return this;
  }

  public WorldScene drawBGfish(WorldScene acc) {
    return acc;
  }

  public ILoBackgroundFish checkCollision(CartPt pfPosition, double pfSize) {
    return this;
  }

  public FishWorld collisionUpdate(FishWorld fishWorld) {
    return fishWorld;
  }

  public ILoBackgroundFish moveFishies() {
    return this;
  }
  // Adds a preset fish that is determined from a random value to the list of
  // bgFish in
  // the current world state
  // potentially make it so only fish that are smaller than the current fish spawn
  // **** this function is exactly the same for both mt and cons and can be
  // abstracted

  public ILoBackgroundFish addToLoBG(int id, int randomNum) {
    Random rand = new Random();

    if (randomNum <= 20) {
      // Preset bgFish 1/5: tiny
      return new ConsLoBackgroundFish(new BackgroundFish("Tiny", id, // the y below is a placeholder
                                                                     // for all
          new CartPt(rand.nextInt(1600), 80)), this);
    }
    if (20 < randomNum && randomNum <= 40) {
      // Preset bgFish 2/5: small
      return new ConsLoBackgroundFish(
          new BackgroundFish("Small", id, new CartPt(rand.nextInt(1600), 260)), this);
    }
    if (40 < randomNum && randomNum <= 60) {
      // Preset bgFish 3/5: medium
      return new ConsLoBackgroundFish(
          new BackgroundFish("Medium", id, new CartPt(rand.nextInt(1600), 440)), this);
    }
    if (60 < randomNum && randomNum <= 80) {
      // Preset bgFish 4/5: large
      return new ConsLoBackgroundFish(
          new BackgroundFish("Large", id, new CartPt(rand.nextInt(1600), 620)), this);
    }
    if (80 < randomNum && randomNum <= 100) {
      // Preset bgFish 5/5: huge
      return new ConsLoBackgroundFish(
          new BackgroundFish("Huge", id, new CartPt(rand.nextInt(1600), 870)), this);
    }
    else {
      return this;
    }
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
  // methods
  // adds to list at random times

  // eat fn
  // --> remove given fish from list
  // -->
  // return the same list with the given fishie removed
  public ILoBackgroundFish eat(BackgroundFish otherFish) {
    if (this.first.fishMatch(otherFish)) {
      return this.rest;
    }
    else {
      return new ConsLoBackgroundFish(this.first, this.rest.eat(otherFish));
    }
  }

  public WorldScene drawBGfish(WorldScene acc) {
    return this.rest.drawBGfish(this.first.drawBGfish(acc));
  }

  public ILoBackgroundFish moveFishies() {
    return new ConsLoBackgroundFish(this.first.moveBGFish(), this.rest.moveFishies());
  }

  // Adds a preset fish that is determined from a random value to the list of
  // bgFish in
  // the current world state
  // potentially make it so only fish that are smaller than the current fish spawn
  public ILoBackgroundFish addToLoBG(int id, int randomNum) {
    Random rand = new Random();

    if (randomNum <= 20) {
      // Preset bgFish 1/5: tiny
      return new ConsLoBackgroundFish(new BackgroundFish("Tiny", id, // the y below is a placeholder
                                                                     // for all
          new CartPt(rand.nextInt(1600), 80)), this);
    }
    if (20 < randomNum && randomNum <= 40) {
      // Preset bgFish 2/5: small
      return new ConsLoBackgroundFish(
          new BackgroundFish("Small", id, new CartPt(rand.nextInt(1600), 260)), this);
    }
    if (40 < randomNum && randomNum <= 60) {
      // Preset bgFish 3/5: medium
      return new ConsLoBackgroundFish(
          new BackgroundFish("Medium", id, new CartPt(rand.nextInt(1600), 440)), this);
    }
    if (60 < randomNum && randomNum <= 80) {
      // Preset bgFish 4/5: large
      return new ConsLoBackgroundFish(
          new BackgroundFish("Large", id, new CartPt(rand.nextInt(1600), 620)), this);
    }
    if (80 < randomNum && randomNum <= 100) {
      // Preset bgFish 5/5: huge
      return new ConsLoBackgroundFish(
          new BackgroundFish("Huge", id, new CartPt(rand.nextInt(1600), 870)), this);
    }
    else {
      return this;
    }
  }

  // goes through a list of bg fish and returns any that are colliding with
  // the hitbox (calculated through pf position and pf size) of the playerfish
  public ILoBackgroundFish checkCollision(CartPt pfPosition, double pfSize) {
    if (this.first.checkCollisionHelper(pfPosition, pfSize)) {
      System.out.println(this.first.id);
      return new ConsLoBackgroundFish(this.first, this.rest.checkCollision(pfPosition, pfSize));
    }
    else {
      return this.rest.checkCollision(pfPosition, pfSize);
    }
  }

  public FishWorld collisionUpdate(FishWorld fishWorld) {
    return this.rest.collisionUpdate(fishWorld.canEatHelper(this.first));
  }

}

abstract class ASnack {
  int id;
  CartPt position;
  WorldImage model;

  ASnack(int id, CartPt position) {
    this.id = id;
    this.position = position;
  }

  public boolean snackMatch(ASnack other) {
    return this.id == other.id;
  }

  public abstract WorldScene drawSnacks(WorldScene acc);

  public abstract PlayerFish applyEffect(PlayerFish pf);

  boolean checkSnackCollision(CartPt pfPosition, double pfSize) {
    // there is definitely a way to simplify this LOL
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
  // add visual representation
  WorldImage model = new OverlayImage(new TextImage("O", Color.BLACK),
      new CircleImage(30, OutlineMode.SOLID, Color.BLUE));

  SpeedSnacks(int id, CartPt position) {
    super(id, position);
  }
  /*
   * template
   */

  // checks whether a given snack is the same type
  public WorldScene drawSnacks(WorldScene acc) {
    return acc.placeImageXY(this.model, this.position.x, this.position.y);
  }

  // CHANGE TO NOT HAVE GETTERS
  public PlayerFish applyEffect(PlayerFish pf) {
    return new PlayerFish(pf.position, pf.size, pf.score, pf.speed + this.speedBoost, pf.livesLeft);
  }
}

// a class to represent snacks that decrease the size of the PlayerFish when eaten
class BadSnacks extends ASnack {
  // decrease size
  int sizeDecrease = 1;
  WorldImage model = new OverlayImage(new TextImage("X", Color.BLACK),
      new CircleImage(30, OutlineMode.SOLID, Color.RED));

  BadSnacks(int id, CartPt position) {
    super(id, position);
  }

  public WorldScene drawSnacks(WorldScene acc) {
    return acc.placeImageXY(this.model, this.position.x, this.position.y);
  }

  // CHANGE TO NOT HAVE GETTERS
  public PlayerFish applyEffect(PlayerFish pf) {
    return new PlayerFish(pf.position, pf.size - this.sizeDecrease, pf.score, pf.speed,
        pf.livesLeft);
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

  public WorldScene drawSnacks(WorldScene acc) {
    return acc.placeImageXY(this.model, this.position.x, this.position.y);
  }

  public PlayerFish applyEffect(PlayerFish pf) {
    return new PlayerFish(pf.position, pf.size, pf.score + this.scoreIncrease, pf.speed,
        pf.livesLeft);
  }
}

// an interface to represent a list of Snacks
interface ILoSnacks {
  // 'eats' a snack off the snack list
  ILoSnacks eat(ASnack snack);

  ILoSnacks generateSnack(int rand, int id);

  WorldScene drawSnacks(WorldScene acc);

  ILoSnacks checkSnackCollision(PlayerFish pf);

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

  public WorldScene drawSnacks(WorldScene acc) {
    return acc;
  }

  public ILoSnacks checkSnackCollision(PlayerFish pf) {
    return this;
  }

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

  public ILoSnacks eat(ASnack snack) {
    if (!this.first.snackMatch(snack)) {
      return new ConsLoSnacks(this.first, this.rest.eat(snack));
    }
    else {
      return this.rest;
    }
  }

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

  public WorldScene drawSnacks(WorldScene acc) {
    return this.rest.drawSnacks(this.first.drawSnacks(acc));
  }

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

  // TEMPLATE FIELDS ... this.x ... -- int ... this.y ... -- int
}

class ExamplesFishWorldProgram {
  PlayerFish deadFish = new PlayerFish(new CartPt(300, 300), 50, 0, 10, 3);
  PlayerFish pFish1 = new PlayerFish(new CartPt(300, 600), 10, 0, 10, 3);

  BackgroundFish bgFish1 = new BackgroundFish("Tiny", 1, new CartPt(250, 250));
  BackgroundFish bgFish2 = new BackgroundFish("Large", 2, new CartPt(1600, 1600));

  // pileBgFish1 is directly on top of pFish1 and is smaller than PF
  BackgroundFish pileBgFish1 = new BackgroundFish("Tiny", 3, new CartPt(300, 300));
  // pileBgFish1 is directly on top of pFish1 and is bigger than PF
  BackgroundFish pileBgFish2 = new BackgroundFish("Huge", 4, new CartPt(300, 300));

  // pileBgFish lists
  ILoBackgroundFish tempList = new ConsLoBackgroundFish(bgFish2, new MtLoBackgroundFish());
  ILoBackgroundFish loBgFish1 = new ConsLoBackgroundFish(bgFish1, new MtLoBackgroundFish());
  ILoBackgroundFish loBgFish2 = new ConsLoBackgroundFish(bgFish2, loBgFish1);

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
  FishWorld FishWorld1 = new FishWorld(pFish1, loBgFish2, 3, this.snacks1);

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
