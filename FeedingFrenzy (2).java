import org.w3c.dom.Text;
import tester.*;
import javalib.worldimages.*;
import javalib.funworld.*;

import java.awt.*;
import java.util.Random;

// class to represent worldstate
class FishWorld extends World {
  PlayerFish eric; // the player fish
  ILoBackgroundFish otherFishies; // the background fish
  int id; // the current id to make new snacks and bg fish
  int clockTick; // added clock tick for more control over timing of generation and life loss
  ILoSnacks snacks; // the snacks
  int windowW; // the width of the window
  int windowH; // the height of the window

  FishWorld(PlayerFish eric, ILoBackgroundFish otherFishies, int id, int clockTick,
      ILoSnacks snacks) {
    this.eric = eric;
    this.otherFishies = otherFishies;
    this.id = id;
    this.clockTick = clockTick;
    this.snacks = snacks;
    this.windowW = 1600;
    this.windowH = 950;
  }

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
      return this.draw(new WorldScene(windowW, windowH).placeImageXY(
          new RectangleImage(windowW, windowH, OutlineMode.SOLID, Color.CYAN), windowW / 2,
          windowH / 2));
    }
  }

  // method called every 'tick' and handles
  // fish collision (canEat)
  // snack collision (handleSnackCollision)
  // generation of bg fish (generateFish, generateFreebie)
  // ticks elapsed in program (updateClock)
  public FishWorld onTick() {
    if (this.eric.outOfLives()) {
      return this;
    }
    else if (this.clockTick % 50 == 0 && !this.otherFishies.maxSpawn()) { // generates new fish
                                                                          // every 100 ticks
      return this.updateTicksWorld().generateFish().moveFishiesFW()
          .canEat(this.returnCollidedFish()).generateSnacks()
          .handleSnackCollision(this.returnCollidedSnacks()).updateClock();
    }
    else if (this.clockTick % 40 == 0) {
      return this.updateTicksWorld().moveFishiesFW().canEat(this.returnCollidedFish())
          .handleSnackCollision(this.returnCollidedSnacks()).generateFreebie().updateClock();
    }
    else {
      return this.updateTicksWorld().moveFishiesFW().canEat(this.returnCollidedFish())
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
    return new FishWorld(this.eric, this.otherFishies.moveFishiesList(), this.id, this.clockTick,
        this.snacks);
  }

  // spawns in additional background fish
  FishWorld generateFish() {
    return new FishWorld(this.eric, otherFishies.addToLoBG(this.id + 1, this.eric), this.id + 1,
        this.clockTick + 1, this.snacks);
  }

  // used to test random
  FishWorld generateFishTest(Random seed) {
    return new FishWorld(this.eric, otherFishies.addToLoBGTest(this.id + 1, this.eric, seed),
        this.id + 1, this.clockTick + 1, this.snacks);
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

  // handles snack collision and returns updated fishworld
  FishWorld handleSnackCollision(ILoSnacks collidedSnacks) {
    return collidedSnacks.snackUpdate(this);
  }

  // returns a list of collided snacks
  ILoSnacks returnCollidedSnacks() {
    return this.snacks.checkSnackCollision(this.eric);
  }

  // updates clock, keeps track of how many ticks program has elapsed
  FishWorld updateClock() {
    return new FishWorld(this.eric, this.otherFishies, this.id, this.clockTick + 1, this.snacks);
  }

  // updates the fishworld for the 'tick' grace period
  FishWorld updateTicksWorld() {
    return new FishWorld(this.eric.updateTicks(), this.otherFishies, this.id, this.clockTick,
        this.snacks);
  }

  // generates a bg fish that is always size 5, aka always edible
  FishWorld generateFreebie() {
    return new FishWorld(this.eric, this.otherFishies.addFreebie(this.id + 1), this.id + 1,
        this.clockTick + 1, this.snacks);
  }

  // used to test generatefreebie
  FishWorld generateFreebieTest(Random seed) {
    return new FishWorld(this.eric, this.otherFishies.addFreebieTest(this.id + 1, seed),
        this.id + 1, this.clockTick + 1, this.snacks);
  }

  // updates the fishworld based on if the given background fish (which the
  // playerfish has collided with) will be eaten or not
  FishWorld canEatHelper(BackgroundFish otherFish) {
    if (otherFish.compareFishes(this.eric)) { // will return true when this fish CAN EAT
      return new FishWorld(this.eric.ateFishUpdate(otherFish), otherFishies.eat(otherFish), this.id,
          this.clockTick, this.snacks);
    }
    else {
      // If the BG fish is bigger, it'll remove a life from
      // the world state and determine if the game is over
      return new FishWorld(this.eric.updateLives(-1), otherFishies, this.id, this.clockTick,
          this.snacks);
    }
  }

  // generates snacks
  public FishWorld generateSnacks() {
    Random rand = new Random();
    return new FishWorld(this.eric, this.otherFishies, this.id++, this.clockTick,
        this.snacks.generateSnack(rand.nextInt(100), this.id));
  }

  // used to test random
  public FishWorld generateSnacksTest(Random seed) {
    return new FishWorld(this.eric, this.otherFishies, this.id++, this.clockTick,
        this.snacks.generateSnackTest(seed, this.id));
  }

  // updates the fishworld based on the eaten snack
  FishWorld snackUpdateHelper(ASnack snack) {
    return new FishWorld(snack.applyEffect(this.eric), this.otherFishies, this.id, this.clockTick,
        this.snacks.eatSnack(snack));
  }
}

// class to represent the playerfish
class PlayerFish {
  CartPt position; // position of the fish on the world scene
  int size; // size of the fish, used for edible calculations and to determine size of world
            // image model
  int score; // total points earned by the player
  int speed; // how quickly the player moves across the world scene
  int livesLeft; // how many times the player can touch a fish bigger than it // eat bad snack
  int direction; // which way (on x axis) fish is moving
  int immunityTicks; // grace period between losing lives
  WorldImage skin; // world image depiction of fish

  PlayerFish(CartPt position, int size, int score, int speed, int livesLeft, int direction,
      int immunityTicks) {
    this.position = position;
    this.size = size;
    this.score = score;
    this.speed = speed;
    this.livesLeft = livesLeft;
    this.immunityTicks = immunityTicks;
    if (direction == 0) {
      this.skin = new OverlayOffsetImage(
          new EllipseImage(this.size * 4, this.size * 2, OutlineMode.SOLID, Color.ORANGE),
          this.size * 1.5, 0, (new RotateImage(
              new EquilateralTriangleImage(this.size * 2, OutlineMode.SOLID, Color.ORANGE), 270)));
    }
    else {
      this.skin = new OverlayOffsetImage(
          new EllipseImage(this.size * 4, this.size * 2, OutlineMode.SOLID, Color.ORANGE),
          (this.size * -1.5), 0, (new RotateImage(
              new EquilateralTriangleImage(this.size * 2, OutlineMode.SOLID, Color.ORANGE), -270)));
    }
  }

  PlayerFish(CartPt position, int size, int score, int speed, int livesLeft, int direction,
      int immunityTicks, WorldImage skin) {
    this.position = position;
    this.size = size;
    this.score = score;
    this.speed = speed;
    this.livesLeft = livesLeft;
    this.direction = direction;
    this.immunityTicks = immunityTicks;
    this.skin = skin;
  }

  // draws the playerfish on top of a given worldscene
  WorldScene drawFish(WorldScene acc) {
    return acc.placeImageXY(this.skin, this.position.x, this.position.y)
        .placeImageXY(new TextImage("Score: " + String.valueOf(this.score), 20, Color.BLACK), 1300,
            15)
        .placeImageXY(new TextImage("Lives: " + String.valueOf(this.livesLeft), 20, Color.BLACK),
            1400, 15);
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

    return new PlayerFish(new CartPt(X, Y), this.size, this.score, this.speed, this.livesLeft, dir,
        this.immunityTicks);
  }

  // updates the playerfish when it properly eats another fish (size and score
  // update)
  PlayerFish ateFishUpdate(BackgroundFish otherFish) {
    return new PlayerFish(this.position, otherFish.calculateSizeGained(this.size),
        otherFish.calculateScoreGained(this.score), this.speed, this.livesLeft, this.direction,
        this.immunityTicks);
  }

  // checks if the size of this PlayerFish is greater than a BackgroundFish
  boolean compareFishesHelp(int otherFishSize) {
    return this.size >= otherFishSize;
  }

  // updates the lives of the playerfish based on a given change
  PlayerFish updateLives(int updateAmt) {
    if (this.immunityTicks > 0) {
      return this;
    }
    else {
      return new PlayerFish(this.position, this.size, this.score, this.speed,
          updateAmt + this.livesLeft, this.direction, 10); // change ticks later
    }
  }

  // updates the ticks the pf has for grace period between losing lives
  PlayerFish updateTicks() {
    if (this.immunityTicks > 0) {
      return new PlayerFish(this.position, this.size, this.score, this.speed, this.livesLeft,
          this.direction, this.immunityTicks - 1);
    }
    else {
      return this;
    }
  }

  // checks if the playerfish is out of lives
  boolean outOfLives() {
    return this.livesLeft <= 0;
  }

  // returns a list of background fish that are colliding with the playerfish
  // returns multiple fish in case collision with multiple fish happens on same
  // tick
  ILoBackgroundFish checkCollision(ILoBackgroundFish bgFishList) {
    return bgFishList.checkCollision(this.position, this.size);
  }

  // Displays the score and lives a player has on the world scene
  String getStats() {
    return "Score: " + this.score + " Lives Left: " + this.livesLeft;
  }

  // returns the playerfish with the updated snack effect
  PlayerFish eatSnack(ASnack snack) {
    return snack.applyEffect(this);
  }

  PlayerFish applySnackEffect(int sizeB, int scoreB, int speedB) {
    return new PlayerFish(this.position, this.size + sizeB, this.score + scoreB,
        this.speed + speedB, this.livesLeft, this.direction, this.immunityTicks);
  }

  // Method to check collision with a snack
  boolean checkSnackCollision(ASnack snack) {
    return snack.checkSnackCollision(this.position, this.size);

  }

  // checks if the pf is too small
  boolean isTooSmall() {
    return this.size <= 5;
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

  BackgroundFish(int id, CartPt position, Random seed) {
    this.id = id;
    // random position of where bg fish is placed is determined in addToLoBG in
    // FishWorld
    this.position = position;
    // rolls a number between 0 - 99 inclusive to determine which category of
    // size the bg fish will fall into.
    int sizer = seed.nextInt(100);
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
    this.direction = seed.nextInt(2);
    // determines which model will be used to represent the BG fish
    if (this.direction == 0) {
      this.fishModel = new OverlayOffsetImage(
          new EllipseImage(this.size * 4, this.size * 2, OutlineMode.SOLID, colorName),
          this.size * 1.5, 0, (new RotateImage(
              new EquilateralTriangleImage(this.size * 2, OutlineMode.SOLID, colorName), 270)));
    }
    else {
      this.fishModel = new OverlayOffsetImage(
          new EllipseImage(this.size * 4, this.size * 2, OutlineMode.SOLID, colorName),
          this.size * -1.5, 0, (new RotateImage(
              new EquilateralTriangleImage(this.size * 2, OutlineMode.SOLID, colorName), -270)));
    }
  }

  BackgroundFish(int id, CartPt position) {
    this.id = id;
    // random position of where bg fish is placed is determined in addToLoBG in
    // FishWorld
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
      this.fishModel = new OverlayOffsetImage(
          new EllipseImage(this.size * 4, this.size * 2, OutlineMode.SOLID, colorName),
          this.size * 1.5, 0, (new RotateImage(
              new EquilateralTriangleImage(this.size * 2, OutlineMode.SOLID, colorName), 270)));
    }
    else {
      this.fishModel = new OverlayOffsetImage(
          new EllipseImage(this.size * 4, this.size * 2, OutlineMode.SOLID, colorName),
          this.size * -1.5, 0, (new RotateImage(
              new EquilateralTriangleImage(this.size * 2, OutlineMode.SOLID, colorName), -270)));
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

  // helper for the 'freebie' first fish
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

  ILoBackgroundFish addToLoBGTest(int id, PlayerFish pf, Random seed);

  // moves all of the fish in the list
  ILoBackgroundFish moveFishiesList();

  // checks if the list contains the max amount of BG fish
  boolean maxSpawn();

  // counts the number of elements in a bg fish list
  int countGameFish();

  // generates a tiny fish that can be eaten by the player no matter size
  ILoBackgroundFish addFreebie(int id);

  // used to test addFreebie
  ILoBackgroundFish addFreebieTest(int id, Random seed);

  // checks if the boss is alive by seeing if it's in a list of bg fish
  boolean isBossDead();
}

// a class to represent the empty list of background fish
class MtLoBackgroundFish implements ILoBackgroundFish {
  MtLoBackgroundFish() {
  }
  /*
   * Template Fields: Methods: this.eat ... ILoBackground this.drawBGfish ...
   * WorldScene this.checkCollision ... ILoBackgroundFish this.collisionUpdate ...
   * FishWorld this.moveFishies ... ILoBackgroundFish this.addToLoBG(int,
   * PlayerFish) ... ILoBackgroundFish this.maxSpawn ... boolean
   * this.countGameFish ... int this.addFreebie ... ILoBackgroundFish
   * this.isBossDead ... boolean Fields of Methods:
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
    Random rand = new Random();
    int X;
    int Y;
    // ensures that a bg fish won't spawn directly ontop of the Playerfish
    if (rand.nextBoolean()) {
      X = (rand.nextInt(pf.position.x) - (pf.size * 4 + 50)) % 1600;
    }
    else {
      X = (rand.nextInt(1600) + (pf.size * 4 + 50) + pf.position.x) % 1600;
    }
    if (rand.nextBoolean())
      Y = (rand.nextInt(pf.position.y) - (pf.size * 2 + 25)) % 950;
    else {
      Y = (rand.nextInt(950) + (pf.size * 2 + 25) + pf.position.y) % 950;
    }

    return new ConsLoBackgroundFish(new BackgroundFish(id, new CartPt(X, Y)), this);
  }

  public ILoBackgroundFish addToLoBGTest(int id, PlayerFish pf, Random seed) {
    // can be abstracted
    int X;
    int Y;
    // ensures that a bg fish won't spawn directly ontop of the Playerfish
    if (seed.nextBoolean()) {
      X = (seed.nextInt(pf.position.x) - (pf.size * 4 + 50)) % 1600;
    }
    else {
      X = (seed.nextInt(1600) + (pf.size * 4 + 50) + pf.position.x) % 1600;
    }
    if (seed.nextBoolean())
      Y = (seed.nextInt(pf.position.y) - (pf.size * 2 + 25)) % 950;
    else {
      Y = (seed.nextInt(950) + (pf.size * 2 + 25) + pf.position.y) % 950;
    }

    return new ConsLoBackgroundFish(new BackgroundFish(id, new CartPt(X, Y), seed), this);
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
    Random rand = new Random();
    return new ConsLoBackgroundFish(new BackgroundFish(id,
        new CartPt(rand.nextInt(1600), rand.nextInt(950)), 5, 20, 1, rand.nextInt(1),
        new OverlayOffsetImage(new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE), 7.5, 0,
            (new RotateImage(new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE),
                270)))),
        this);
  }

  // used to test addFreebie
  public ILoBackgroundFish addFreebieTest(int id, Random seed) {
    return new ConsLoBackgroundFish(new BackgroundFish(id,
        new CartPt(seed.nextInt(1600), seed.nextInt(950)), 5, 20, 1, seed.nextInt(1),
        new OverlayOffsetImage(new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE), 7.5, 0,
            (new RotateImage(new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE),
                270)))),
        this);
  }

  // returns true because if there are no fish left, the boss must be dead!
  public boolean isBossDead() {
    return true;
  }
}

// class for non empty list of fish
class ConsLoBackgroundFish implements ILoBackgroundFish {
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

    return new ConsLoBackgroundFish(new BackgroundFish(id, new CartPt(X, Y)), this);
  }

  // used to test addtolobg
  public ILoBackgroundFish addToLoBGTest(int id, PlayerFish pf, Random seed) {
    // can be abstracted
    int X;
    int Y;
    // ensures that a bg fish won't spawn directly ontop of the Playerfish
    if (seed.nextBoolean()) {
      X = (seed.nextInt(pf.position.x) - (pf.size * 4 + 50)) % 1600;
    }
    else {
      X = (seed.nextInt(1600) + (pf.size * 4 + 50) + pf.position.x) % 1600;
    }
    if (seed.nextBoolean())
      Y = (seed.nextInt(pf.position.y) - (pf.size * 2 + 25)) % 950;
    else {
      Y = (seed.nextInt(950) + (pf.size * 2 + 25) + pf.position.y) % 950;
    }

    return new ConsLoBackgroundFish(new BackgroundFish(id, new CartPt(X, Y), seed), this);
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

  // adds a free starting fish so that the game is always playable
  public ILoBackgroundFish addFreebie(int id) {
    Random rand = new Random();
    int dir = rand.nextInt(2);
    if (dir == 0) {
      return new ConsLoBackgroundFish(
          new BackgroundFish(id, new CartPt(rand.nextInt(1600), rand.nextInt(950)), 5, 20, 1, dir,
              new OverlayOffsetImage(new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE), 7.5,
                  0,
                  (new RotateImage(
                      new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE), 270)))),
          this);
    }

    else {
      return new ConsLoBackgroundFish(
          new BackgroundFish(id, new CartPt(rand.nextInt(1600), rand.nextInt(950)), 5, 20, 1, dir,
              new OverlayOffsetImage(new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE),
                  -7.5, 0,
                  (new RotateImage(
                      new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE), -270)))),
          this);
    }
  }

  // tests addFreebie
  public ILoBackgroundFish addFreebieTest(int id, Random seed) {
    int dir = seed.nextInt(2);
    if (dir == 0) {
      return new ConsLoBackgroundFish(
          new BackgroundFish(id, new CartPt(seed.nextInt(1600), seed.nextInt(950)), 5, 20, 1, dir,
              new OverlayOffsetImage(new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE), 7.5,
                  0,
                  (new RotateImage(
                      new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE), 270)))),
          this);
    }

    else {
      return new ConsLoBackgroundFish(
          new BackgroundFish(id, new CartPt(seed.nextInt(1600), seed.nextInt(950)), 5, 20, 1, dir,
              new OverlayOffsetImage(new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE),
                  -7.5, 0,
                  (new RotateImage(
                      new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE), -270)))),
          this);
    }
  }

  // determines if the list contains the boss, if it doesn't then the boss is dead
  public boolean isBossDead() {
    if (this.first.isFishBoss()) {
      return false;
    }
    else {
      return this.rest.isBossDead();
    }
  }
}

// an abstract class to represent snacks (extra stuff) 
abstract class ASnack {
  int id;
  CartPt position;
  Random rand;
  WorldImage model;

  ASnack(int id, CartPt position) {
    this.id = id;
    this.position = position;
  }

  ASnack(int id, CartPt position, Random rand) {
    this.id = id;
    this.position = position;
    this.rand = rand;
  }

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

  // draws this snack on a given worldscene
  public WorldScene drawSnacks(WorldScene acc) {
    return acc.placeImageXY(this.model, this.position.x, this.position.y);
  }

  // applies the effect of a snack eaten by the playerFish
  public PlayerFish applyEffect(PlayerFish pf) {
    return pf.applySnackEffect(0, 0, this.speedBoost);
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

  // draws this snack on a given worldscene
  public WorldScene drawSnacks(WorldScene acc) {
    return acc.placeImageXY(this.model, this.position.x, this.position.y);
  }

  // decreases the size of playerfish when it eats this snack
  public PlayerFish applyEffect(PlayerFish pf) {
    if (!pf.isTooSmall()) {
      return pf.applySnackEffect(this.sizeDecrease, 0, 0);
    }
    else {
      return pf;
    }
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

  // draws this snack on a given worldscene
  public WorldScene drawSnacks(WorldScene acc) {
    return acc.placeImageXY(this.model, this.position.x, this.position.y);
  }

  // decreases the score of playerfish when it eats this snack
  public PlayerFish applyEffect(PlayerFish pf) {
    return pf.applySnackEffect(0, this.scoreIncrease, 0);
  }
}

// an interface to represent a list of Snacks
interface ILoSnacks {
  // 'eats' a snack off the snack list
  ILoSnacks eatSnack(ASnack snack);

  // generates snacks and adds them to this list
  ILoSnacks generateSnack(int rand, int id);

  // for random testing
  ILoSnacks generateSnackTest(Random seed, int id);

  // draws this list of snacks on a worldscene
  WorldScene drawSnacks(WorldScene acc);

  // checks if the snacks are colliding
  ILoSnacks checkSnackCollision(PlayerFish pf);

  // updates the fishworld with the correct list of snacks
  FishWorld snackUpdate(FishWorld world);
}

// a class to represent the empty list
class MtLoSnacks implements ILoSnacks {

  // cannot eat no snacks!
  public ILoSnacks eatSnack(ASnack snack) {
    return this;
  }

  // generates snacks randomly and adds them to this list of snacks
  public ILoSnacks generateSnack(int rand, int id) {
    Random rand2 = new Random();
    if (rand <= 20) { // change the posn
      return new ConsLoSnacks(
          new SpeedSnacks(id, new CartPt(rand2.nextInt(1600), rand2.nextInt(900))), this);
    }
    else if (20 > rand && rand <= 50) {
      return new ConsLoSnacks(
          new BadSnacks(id, new CartPt(rand2.nextInt(1600), rand2.nextInt(900))), this);
    }
    else if (50 > rand && rand <= 70) { // change the posn
      return new ConsLoSnacks(
          new ScoreSnacks(id, new CartPt(rand2.nextInt(1600), rand2.nextInt(900))), this);
    }
    else {
      return this;
    }
  }

  // for random testing
  public ILoSnacks generateSnackTest(Random seed, int id) {
    if (seed.nextInt() <= 20) { // change the posn
      return new ConsLoSnacks(
          new SpeedSnacks(id, new CartPt(seed.nextInt(1600), seed.nextInt(900))), this);
    }
    else if (20 > seed.nextInt() && seed.nextInt() <= 50) {
      return new ConsLoSnacks(new BadSnacks(id, new CartPt(seed.nextInt(1600), seed.nextInt(900))),
          this);
    }
    else if (50 > seed.nextInt() && seed.nextInt() <= 70) { // change the posn
      return new ConsLoSnacks(
          new ScoreSnacks(id, new CartPt(seed.nextInt(1600), seed.nextInt(900))), this);
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
      return new ConsLoSnacks(
          new SpeedSnacks(id, new CartPt(rand2.nextInt(1600), rand2.nextInt(900))), this);
    }
    else if (20 > rand && rand <= 50) {
      return new ConsLoSnacks(
          new BadSnacks(id, new CartPt(rand2.nextInt(1600), rand2.nextInt(900))), this);
    }
    else if (50 > rand && rand <= 70) { // change the posn
      return new ConsLoSnacks(
          new ScoreSnacks(id, new CartPt(rand2.nextInt(1600), rand2.nextInt(900))), this);
    }
    else {
      return this;
    }
  }

  // for random testing
  public ILoSnacks generateSnackTest(Random seed, int id) {
    if (seed.nextInt() <= 20) { // change the posn
      return new ConsLoSnacks(
          new SpeedSnacks(id, new CartPt(seed.nextInt(1600), seed.nextInt(900))), this);
    }
    else if (20 > seed.nextInt() && seed.nextInt() <= 50) {
      return new ConsLoSnacks(new BadSnacks(id, new CartPt(seed.nextInt(1600), seed.nextInt(900))),
          this);
    }
    else if (50 > seed.nextInt() && seed.nextInt() <= 70) { // change the posn
      return new ConsLoSnacks(
          new ScoreSnacks(id, new CartPt(seed.nextInt(1600), seed.nextInt(900))), this);
    }
    else {
      return this;
    }
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

  // updates the given fishworld with the snack effects
  public FishWorld snackUpdate(FishWorld world) {
    return this.rest.snackUpdate(world.snackUpdateHelper(this.first));
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
  PlayerFish deadFish = new PlayerFish(new CartPt(300, 300), 50, 0, 10, 0, 0, 0);
  PlayerFish pilePFish = new PlayerFish(new CartPt(300, 600), 5, 0, 10, 3, 1, 0);

  PlayerFish pFishGame = new PlayerFish(new CartPt(800, 475), 5, 0, 10, 3, 1, 0);
  PlayerFish pFish1 = new PlayerFish(new CartPt(300, 20), 5, 0, 10, 3, 1, 0);
  PlayerFish pFishITick = new PlayerFish(new CartPt(300, 20), 5, 0, 10, 3, 1, 5);

  // BG Fish Boss
  BackgroundFish fishBoss = new BackgroundFish(999999999, new CartPt(1600, 830), 100, 2, 1000, 1,
      new OverlayOffsetImage(new EllipseImage(400, 200, OutlineMode.SOLID, Color.BLACK), -150, 0,
          (new RotateImage(new EquilateralTriangleImage(200, OutlineMode.SOLID, Color.BLACK),
              -270))));

  // BG Freebie
  BackgroundFish freebie = new BackgroundFish(100, new CartPt(0, 0), 5, 20, 1, 1,
      new OverlayOffsetImage(new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE), 7.5, 0,
          (new RotateImage(new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE),
              270))));

  // Huge BG fish
  BackgroundFish hugeFishR = new BackgroundFish(1, new CartPt(0, 0), 70, 10, 70, 1,
      new OverlayOffsetImage(new EllipseImage(280, 140, OutlineMode.SOLID, Color.RED), -140, 0,
          (new RotateImage(new EquilateralTriangleImage(140, OutlineMode.SOLID, Color.RED),
              -270))));

  BackgroundFish hugeFishL = new BackgroundFish(2, new CartPt(0, 0), 70, 10, 70, 0,
      new OverlayOffsetImage(new EllipseImage(280, 140, OutlineMode.SOLID, Color.RED), 140, 0,
          (new RotateImage(new EquilateralTriangleImage(140, OutlineMode.SOLID, Color.RED), 270))));

  // Large BG Fish
  BackgroundFish largeFishR = new BackgroundFish(3, new CartPt(0, 0), 40, 12, 40, 1,
      new OverlayOffsetImage(new EllipseImage(160, 80, OutlineMode.SOLID, Color.MAGENTA), -80, 0,
          (new RotateImage(new EquilateralTriangleImage(80, OutlineMode.SOLID, Color.MAGENTA),
              -270))));

  BackgroundFish largeFishL = new BackgroundFish(4, new CartPt(0, 0), 40, 12, 40, 0,
      new OverlayOffsetImage(new EllipseImage(160, 80, OutlineMode.SOLID, Color.MAGENTA), 80, 0,
          (new RotateImage(new EquilateralTriangleImage(80, OutlineMode.SOLID, Color.MAGENTA),
              270))));

  // Medium BG Fish
  BackgroundFish mediumFishR = new BackgroundFish(5, new CartPt(0, 0), 20, 14, 20, 1,
      new OverlayOffsetImage(new EllipseImage(80, 40, OutlineMode.SOLID, Color.BLUE), -40, 0,
          (new RotateImage(new EquilateralTriangleImage(40, OutlineMode.SOLID, Color.BLUE),
              -270))));

  BackgroundFish mediumFishL = new BackgroundFish(6, new CartPt(0, 0), 20, 14, 20, 0,
      new OverlayOffsetImage(new EllipseImage(80, 40, OutlineMode.SOLID, Color.BLUE), 40, 0,
          (new RotateImage(new EquilateralTriangleImage(40, OutlineMode.SOLID, Color.BLUE), 270))));

  // Small BG Fish
  BackgroundFish smallFishR = new BackgroundFish(7, new CartPt(0, 0), 15, 17, 15, 1,
      new OverlayOffsetImage(new EllipseImage(60, 30, OutlineMode.SOLID, Color.GREEN), -30, 0,
          (new RotateImage(new EquilateralTriangleImage(30, OutlineMode.SOLID, Color.GREEN),
              -270))));

  BackgroundFish smallFishL = new BackgroundFish(8, new CartPt(0, 0), 15, 17, 15, 0,
      new OverlayOffsetImage(new EllipseImage(60, 30, OutlineMode.SOLID, Color.GREEN), 30, 0,
          (new RotateImage(new EquilateralTriangleImage(30, OutlineMode.SOLID, Color.GREEN),
              270))));

  // Tiny BG Fish
  BackgroundFish tinyFishR = new BackgroundFish(9, new CartPt(0, 0), 10, 20, 10, 1,
      new OverlayOffsetImage(new EllipseImage(40, 20, OutlineMode.SOLID, Color.PINK), -20, 0,
          (new RotateImage(new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.PINK),
              -270))));

  BackgroundFish tinyFishL = new BackgroundFish(10, new CartPt(0, 0), 10, 20, 10, 0,
      new OverlayOffsetImage(new EllipseImage(40, 20, OutlineMode.SOLID, Color.PINK), 20, 0,
          (new RotateImage(new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.PINK), 270))));

  // BG fish for game
  BackgroundFish tinyFishGame = new BackgroundFish(1, new CartPt(1500, 200), 10, 20, 10, 0,
      new OverlayOffsetImage(new EllipseImage(40, 20, OutlineMode.SOLID, Color.PINK), 20, 0,
          (new RotateImage(new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.PINK), 270))));

  BackgroundFish smallFishGame = new BackgroundFish(2, new CartPt(100, 350), 15, 17, 15, 1,
      new OverlayOffsetImage(new EllipseImage(60, 30, OutlineMode.SOLID, Color.GREEN), -30, 0,
          (new RotateImage(new EquilateralTriangleImage(30, OutlineMode.SOLID, Color.GREEN),
              -270))));

  BackgroundFish mediumFishGame = new BackgroundFish(3, new CartPt(1500, 500), 20, 14, 20, 0,
      new OverlayOffsetImage(new EllipseImage(80, 40, OutlineMode.SOLID, Color.BLUE), 40, 0,
          (new RotateImage(new EquilateralTriangleImage(40, OutlineMode.SOLID, Color.BLUE), 270))));

  BackgroundFish largeFishGame = new BackgroundFish(4, new CartPt(100, 650), 40, 12, 40, 1,
      new OverlayOffsetImage(new EllipseImage(160, 80, OutlineMode.SOLID, Color.MAGENTA), -80, 0,
          (new RotateImage(new EquilateralTriangleImage(80, OutlineMode.SOLID, Color.MAGENTA),
              -270))));

  BackgroundFish hugeFishGame = new BackgroundFish(5, new CartPt(1500, 800), 70, 10, 70, 0,
      new OverlayOffsetImage(new EllipseImage(280, 140, OutlineMode.SOLID, Color.RED), 140, 0,
          (new RotateImage(new EquilateralTriangleImage(140, OutlineMode.SOLID, Color.RED), 270))));

  ILoBackgroundFish loFishGame = new ConsLoBackgroundFish(tinyFishGame,
      new ConsLoBackgroundFish(smallFishGame,
          new ConsLoBackgroundFish(mediumFishGame,
              new ConsLoBackgroundFish(largeFishGame, new ConsLoBackgroundFish(hugeFishGame,
                  new ConsLoBackgroundFish(fishBoss, new MtLoBackgroundFish()))))));

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
      new OverlayOffsetImage(new EllipseImage(80, 40, OutlineMode.SOLID, Color.BLUE), -40, 0,
          (new RotateImage(new EquilateralTriangleImage(40, OutlineMode.SOLID, Color.BLUE),
              -270))));
  BackgroundFish bgFish2 = new BackgroundFish(2, new CartPt(1600, 400), 40, 10, 2, 0,
      new OverlayOffsetImage(new EllipseImage(160, 80, OutlineMode.SOLID, Color.YELLOW), 80, 0,
          (new RotateImage(new EquilateralTriangleImage(80, OutlineMode.SOLID, Color.YELLOW),
              270))));
  BackgroundFish bgFish3 = new BackgroundFish(5, new CartPt(1600, 700), 50, 16, 3, 1,
      new OverlayOffsetImage(new EllipseImage(200, 100, OutlineMode.SOLID, Color.GREEN), -100, 0,
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

  // list containing fish boss
  ILoBackgroundFish loFishBoss = new ConsLoBackgroundFish(fishBoss, new MtLoBackgroundFish());
  // list containing two fish bigger than starting player fish (pfFish1)
  ILoBackgroundFish loBiggerFish = new ConsLoBackgroundFish(hugeFishR, loFishBoss);

  // used to test when a fish is directly over top PF
  ILoBackgroundFish pileLoFish1 = new ConsLoBackgroundFish(pileBgFish1, new MtLoBackgroundFish());
  ILoBackgroundFish pileLoFish2 = new ConsLoBackgroundFish(pileBgFish2, new MtLoBackgroundFish());

  // snacks
  ASnack badGame = new BadSnacks(90, new CartPt(250, 250));
  ASnack speedGame = new SpeedSnacks(23, new CartPt(1200, 900));
  ASnack scoreGame = new ScoreSnacks(19, new CartPt(1400, 200));

  ASnack speed1 = new SpeedSnacks(23, new CartPt(90, 90));
  ASnack speed2 = new SpeedSnacks(24, new CartPt(120, 120));
  ASnack bad1 = new BadSnacks(90, new CartPt(100, 100));
  ASnack bad2 = new BadSnacks(91, new CartPt(111, 111));
  ASnack score1 = new ScoreSnacks(19, new CartPt(200, 200));
  ASnack score2 = new ScoreSnacks(20, new CartPt(210, 210));

  // lo snacks
  ILoSnacks loSnackGame = new ConsLoSnacks(badGame,
      new ConsLoSnacks(speedGame, new ConsLoSnacks(scoreGame, new MtLoSnacks())));

  ILoSnacks snacks1 = new ConsLoSnacks(this.speed1,
      new ConsLoSnacks(this.bad1, new ConsLoSnacks(this.score1, new MtLoSnacks())));
  ILoSnacks snacks2 = new ConsLoSnacks(this.speed2,
      new ConsLoSnacks(this.bad2, new ConsLoSnacks(this.score2, new MtLoSnacks())));
  // used to test gameover
  ILoSnacks snacks3 = new ConsLoSnacks(this.speed1,
      new ConsLoSnacks(this.score1, new MtLoSnacks()));
  ILoSnacks snacks4 = new ConsLoSnacks(this.speed1, new MtLoSnacks());
  ILoSnacks mtsnack = new MtLoSnacks();

  FishWorld deadFishWorld = new FishWorld(deadFish, loBgFish1, 1, 0, this.snacks1);

  // used to test when a bgfish is directly on top of pf
  FishWorld pileWorld1 = new FishWorld(pilePFish, pileLoFish1, 2, 0, this.snacks1);
  FishWorld pileWorld2 = new FishWorld(pilePFish, pileLoFish2, 3, 0, this.snacks1);

  // normal game state
  FishWorld worldFishGame = new FishWorld(pFish1, loFishGame, 6, 0, this.loSnackGame);
  FishWorld FishWorld1 = new FishWorld(pFish1, loBgFish4, 3, 0, this.snacks1);
  FishWorld fw0 = new FishWorld(pFish1, mt, 20, 0, this.snacks1);
  FishWorld fw1 = new FishWorld(pFish1, loBGfish0, 20, 0, this.snacks1);
  FishWorld fw2 = new FishWorld(pFish1, loBGfish1, 20, 0, this.snacks1);
  FishWorld fwFreebie = new FishWorld(pFish1, loFreebie1, 20, 0, this.snacks1);
  FishWorld fwITick = new FishWorld(pFishITick, mt, 20, 0, this.snacks1);

  // testing worldScene
  WorldScene ws = new WorldScene(800, 800);

//  boolean testBigBang(Tester t) {
//    FishWorld fw = worldFishGame;
//    int worldWidth = 1600;
//    int worldHeight = 950;
//    double tickRate = 0.1;
//    return fw.bigBang(worldWidth, worldHeight, tickRate);
//  }

  // FishWorld Tests
  boolean testOnKeyEvent(Tester t) {
    return t.checkExpect(this.fw0.onKeyEvent("up"),
        new FishWorld(new PlayerFish(new CartPt(300, 10), 5, 0, 10, 3, 0, 0), mt, 20, 0,
            fw0.snacks))
        && t.checkExpect(this.fw0.onKeyEvent("down"),
            new FishWorld(new PlayerFish(new CartPt(300, 30), 5, 0, 10, 3, 0, 0), mt, 20, 0,
                fw0.snacks))
        && t.checkExpect(this.fw0.onKeyEvent("left"),
            new FishWorld(new PlayerFish(new CartPt(290, 20), 5, 0, 10, 3, 0, 0), mt, 20, 0,
                fw0.snacks))
        && t.checkExpect(this.fw0.onKeyEvent("right"), new FishWorld(
            new PlayerFish(new CartPt(310, 20), 5, 0, 10, 3, 1, 0), mt, 20, 0, fw0.snacks));
  }

  boolean testMoveFishiesFW(Tester t) {
    return t.checkExpect(this.fw0.moveFishiesFW(), fw0)
        && t.checkExpect(this.fw1.moveFishiesFW(), new FishWorld(pFish1, new ConsLoBackgroundFish(
            new BackgroundFish(999999999, new CartPt(0, 830), 100, 2, 1000, 1,
                new OverlayOffsetImage(new EllipseImage(400, 200, OutlineMode.SOLID, Color.BLACK),
                    -150, 0,
                    (new RotateImage(
                        new EquilateralTriangleImage(200, OutlineMode.SOLID, Color.BLACK), -270)))),
            mt), 20, 0, this.snacks1))
        && t.checkExpect(this.fw2.moveFishiesFW(), new FishWorld(pFish1, new ConsLoBackgroundFish(
            new BackgroundFish(10, new CartPt(1600, 0), 10, 20, 10, 0,
                new OverlayOffsetImage(new EllipseImage(40, 20, OutlineMode.SOLID, Color.PINK), 20,
                    0,
                    (new RotateImage(
                        new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.PINK), 270)))),
            new ConsLoBackgroundFish(new BackgroundFish(999999999, new CartPt(0, 830), 100, 2, 1000,
                1,
                new OverlayOffsetImage(new EllipseImage(400, 200, OutlineMode.SOLID, Color.BLACK),
                    -150, 0,
                    (new RotateImage(
                        new EquilateralTriangleImage(200, OutlineMode.SOLID, Color.BLACK), -270)))),
                mt)),
            20, 0, this.snacks1));
  }

  boolean testGenerateFish(Tester t) {
    return t.checkExpect(this.fw0.generateFishTest(new Random(20)),
        new FishWorld(fw0.eric, new ConsLoBackgroundFish(
            new BackgroundFish(21, new CartPt(66, 816), 10, 20, 10, 1,
                new OverlayOffsetImage(new EllipseImage(40, 20, OutlineMode.SOLID, Color.PINK), -15,
                    0,
                    (new RotateImage(
                        new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.PINK), -270)))),
            mt), 21, 1, this.snacks1))
        && t.checkExpect(this.fw1.generateFishTest(new Random(20)),
            new FishWorld(fw1.eric, new ConsLoBackgroundFish(
                new BackgroundFish(21, new CartPt(66, 816), 10, 20, 10, 1, new OverlayOffsetImage(
                    new EllipseImage(40, 20, OutlineMode.SOLID, Color.PINK), -15, 0,
                    (new RotateImage(
                        new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.PINK), -270)))),
                new ConsLoBackgroundFish(fishBoss, mt)), 21, 1, this.snacks1));
  }

  boolean testCanEat(Tester t) {
    return t.checkExpect(this.fw0.canEat(mt), fw0)
        && t.checkExpect(this.fw0.canEat(loFreebie1),
            new FishWorld(new PlayerFish(new CartPt(300, 20), 7, 1, 10, 3, 0, 0), mt, 20, 0,
                this.snacks1))
        && t.checkExpect(this.fw0.canEat(loFishBoss),
            new FishWorld(new PlayerFish(new CartPt(300, 20), 5, 0, 10, 2, 0, 10), mt, 20, 0,
                this.snacks1))
        && t.checkExpect(this.fw0.canEat(loBiggerFish),
            new FishWorld(new PlayerFish(new CartPt(300, 20), 5, 0, 10, 2, 0, 10), mt, 20, 0,
                this.snacks1))
        && t.checkExpect(
            this.fw0.canEat(new ConsLoBackgroundFish(fishBoss,
                new ConsLoBackgroundFish(freebie, new MtLoBackgroundFish()))),
            new FishWorld(new PlayerFish(new CartPt(300, 20), 7, 1, 10, 2, 0, 10), mt, 20, 0,
                this.snacks1));
  }

  boolean testReturnCollidedFish(Tester t) {
    return t.checkExpect(this.FishWorld1.returnCollidedFish(), new MtLoBackgroundFish())
        && t.checkExpect(this.pileWorld1.returnCollidedFish(), pileLoFish1)
        && t.checkExpect(this.pileWorld2.returnCollidedFish(), pileLoFish2);
  }

  boolean testUpdateClock(Tester t) {
    return t.checkExpect(this.FishWorld1.updateClock(),
        new FishWorld(this.pFish1, loBgFish4, 3, 1, this.snacks1))
        && t.checkExpect(new FishWorld(this.pFish1, loBgFish4, 3, 1, this.snacks1).updateClock(),
            new FishWorld(this.pFish1, loBgFish4, 3, 2, this.snacks1));
  }

  boolean testUpdateTicksWorld(Tester t) {
    return t.checkExpect(this.fw0.updateTicksWorld(), fw0)
        && t.checkExpect(this.fwITick.updateTicksWorld(), new FishWorld(
            new PlayerFish(new CartPt(300, 20), 5, 0, 10, 3, 0, 4), mt, 20, 0, this.snacks1));
  }

  boolean testGenerateFreebie(Tester t) {
    return t.checkExpect(this.fw0.generateFreebieTest(new Random(20)),
        new FishWorld(this.pFish1, new ConsLoBackgroundFish(
            new BackgroundFish(21, new CartPt(253, 886), 5, 20, 1, 0,
                new OverlayOffsetImage(new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE),
                    7.5, 0,
                    (new RotateImage(
                        new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE), 270)))),
            mt), 21, 1, this.snacks1))
        && t.checkExpect(
            this.fw1.generateFreebieTest(new Random(
                20)),
            new FishWorld(this.pFish1, new ConsLoBackgroundFish(
                new BackgroundFish(21, new CartPt(1436, 151), 5, 20, 1, 1, new OverlayOffsetImage(
                    new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE), -7.5, 0,
                    (new RotateImage(
                        new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE), -270)))),
                new ConsLoBackgroundFish(fishBoss, mt)), 21, 1, this.snacks1));
  }

  boolean testCanEatHelper(Tester t) {
    return t.checkExpect(this.fw0.canEatHelper(this.freebie),
        new FishWorld(new PlayerFish(new CartPt(300, 20), 7, 1, 10, 3, 0, 0), mt, 20, 0,
            this.snacks1))
        && t.checkExpect(this.fw0.canEatHelper(this.fishBoss), new FishWorld(
            new PlayerFish(new CartPt(300, 20), 5, 0, 10, 2, 0, 10), mt, 20, 0, this.snacks1));
  }

  // Background Fish tests
  boolean testCalculateSizeGained(Tester t) {
    return t.checkExpect(this.smallFishL.calculateSizeGained(10), 13)
        && t.checkExpect(this.mediumFishL.calculateSizeGained(10), 14)
        && t.checkExpect(this.largeFishL.calculateSizeGained(10), 15)
        && t.checkExpect(this.hugeFishL.calculateSizeGained(10), 16)
        // bossfish size gain doesn't really matter since the game ends right after,
        // this stat isn't reported like score so no need to make case for it
        && t.checkExpect(this.fishBoss.calculateSizeGained(10), 12);
  }

  boolean testCalculateScoreGained(Tester t) {
    return t.checkExpect(this.smallFishL.calculateScoreGained(10), 25)
        && t.checkExpect(this.mediumFishL.calculateScoreGained(10), 30)
        && t.checkExpect(this.largeFishL.calculateScoreGained(10), 50)
        && t.checkExpect(this.hugeFishL.calculateScoreGained(10), 80)
        && t.checkExpect(this.fishBoss.calculateScoreGained(10), 1010);
  }

  boolean testCompareFishes(Tester t) {
    return t.checkExpect(this.freebie.compareFishes(this.pFish1), true)
        && t.checkExpect(this.bgFish2.compareFishes(this.pFish1), false);
  }

  boolean testFishMatch(Tester t) {
    return t.checkExpect(this.bgFish1.fishMatch(bgFish1), true)
        && t.checkExpect(this.bgFish2.fishMatch(bgFish1), false);
  }

  boolean testFishMatchHelper(Tester t) {
    return t.checkExpect(this.bgFish1.fishMatchHelper(1), true)
        && t.checkExpect(this.bgFish1.fishMatchHelper(10), false);
  }

  boolean testLastScene(Tester t) {
    return t.checkExpect(this.fw0.lastScene("Game Won"),
        new WorldScene(1600, 950).placeImageXY(new TextImage("Game Won", 50, Color.BLACK), 800, 475)
            .placeImageXY(new TextImage(fw0.eric.getStats(), 20, Color.BLACK), 800, 700))
        && t.checkExpect(this.fw0.lastScene("Game Lost"),
            new WorldScene(1600, 950)
                .placeImageXY(new TextImage("Game Lost", 50, Color.BLACK), 800, 475)
                .placeImageXY(new TextImage(fw0.eric.getStats(), 20, Color.BLACK), 800, 700))
        && t.checkExpect(this.fw0.lastScene(""), new WorldScene(500, 500));
  }

  boolean testMoveBGFish(Tester t) {
    return t
        .checkExpect(this.bgFish1.moveBGFish(), new BackgroundFish(
            1, new CartPt(0, 250), 20, 20, 1, 1,
            new OverlayOffsetImage(new EllipseImage(80, 40, OutlineMode.SOLID, Color.BLUE), -40, 0,
                (new RotateImage(new EquilateralTriangleImage(40, OutlineMode.SOLID, Color.BLUE),
                    -270)))))
        && t.checkExpect(this.bgFish2.moveBGFish(),
            new BackgroundFish(2, new CartPt(1590, 400), 40, 10, 2, 0,
                new OverlayOffsetImage(new EllipseImage(160, 80, OutlineMode.SOLID, Color.YELLOW),
                    80, 0, (new RotateImage(
                        new EquilateralTriangleImage(80, OutlineMode.SOLID, Color.YELLOW), 270)))));
  }

  boolean testCheckCollisionHelper(Tester t) {
    return t.checkExpect(this.smallFishL.checkCollisionHelper(new CartPt(0, 0), 10), true)
        && t.checkExpect(this.fishBoss.checkCollisionHelper(new CartPt(0, 0), 20), false);
  }

  boolean testIsFreebie(Tester t) {
    return t.checkExpect(this.bgFish1.isFreebie(), false)
        && t.checkExpect(this.freebie.isFreebie(), true);
  }

  boolean testIsFishBoss(Tester t) {
    return t.checkExpect(this.bgFish1.isFishBoss(), false)
        && t.checkExpect(this.fishBoss.isFishBoss(), true);
  }

  // LOBG FISH
  boolean testEat(Tester t) {
    return t.checkExpect(this.loBgFish1.eat(this.bgFish1), this.mtList)
        && t.checkExpect(this.loBgFish2.eat(this.bgFish1),
            new ConsLoBackgroundFish(this.bgFish2, new MtLoBackgroundFish()))
        && t.checkExpect(this.mtList.eat(this.bgFish1), this.mtList);
  }

//  boolean testLoDrawBGFish(Tester t) {
//    return t.checkExpect(this.loBGfish1.drawBGfish(this.ws), this.ws.placeImageXY(new OverlayOffsetImage(new EllipseImage(40, 20, OutlineMode.SOLID, Color.BLUE), -40, 0,
//        (new RotateImage(new EquilateralTriangleImage(20, OutlineMode.SOLID, Color.BLUE),
//            270))), 1600, 250))
//        && t.checkExpect(this.mtList.drawBGfish(this.ws), this.ws);
//  }

  boolean testLoCheckCollision(Tester t) {
    return t.checkExpect(this.loBGfish1.checkCollision(new CartPt(900, 900), 10), this.mtList)
        && t.checkExpect(this.loBGfish0.checkCollision(new CartPt(1600, 830), 10), this.loBGfish0);
  }

//  boolean testCollisionUpdate(Tester t) {
//    return t.checkExpect(this.mtList.collisionUpdate(this.FishWorld1), this.FishWorld1)
//        && t.checkExpect(this.loBgFish1.collisionUpdate(this.fw1), new FishWorld(
//            new PlayerFish(new CartPt(300, 20), 5, 0, 10, 2, 1, 10), mt, 20, 0, this.snacks1));
//  }

//  boolean testAddToLoBG(Tester t) {
//    return t.checkExpect(this.mtList.addToLoBGTest(9, this.pFish1, new Random(20)),
//        new ConsLoBackgroundFish(new BackgroundFish(9, new CartPt(66, 816), new Random(20)),
//            this.mtList));
//  }

  boolean testMoveFishiesList(Tester t) {
    return t.checkExpect(this.mtList.moveFishiesList(), this.mtList) && t.checkExpect(
        this.loBgFish1.moveFishiesList(),
        new ConsLoBackgroundFish(
            new BackgroundFish(1, new CartPt(0, 250), 20, 20, 1, 1,
                new OverlayOffsetImage(new EllipseImage(80, 40, OutlineMode.SOLID, Color.BLUE), -40,
                    0,
                    (new RotateImage(
                        new EquilateralTriangleImage(40, OutlineMode.SOLID, Color.BLUE), -270)))),
            this.mtList));
  }

  boolean testMaxSpawn(Tester t) {
    return t.checkExpect(this.loBGfish0.maxSpawn(), false)
        && t.checkExpect(this.mtList.maxSpawn(), false);
  }

  boolean testCountGameFish(Tester t) {
    return t.checkExpect(this.loBgFish1.countGameFish(), 1)
        && t.checkExpect(this.loBgFish2.countGameFish(), 2)
        && t.checkExpect(mtList.countGameFish(), 0) && t.checkExpect(loFreebie1.countGameFish(), 0)
        && t.checkExpect(loFreebie2.countGameFish(), 0)
        && t.checkExpect(loFreebie3.countGameFish(), 1);
  }

  boolean testIsBossDead(Tester t) {
    return t.checkExpect(this.loBgFish4.isBossDead(), false)
        && t.checkExpect(this.loBgFish2.isBossDead(), true)
        && t.checkExpect(this.mtList.isBossDead(), true);
  }

  boolean testAddFreebie(Tester t) { // NEEDS RANDOM
    return t
        .checkExpect(this.loBgFish1.addFreebieTest(10, new Random(20)), new ConsLoBackgroundFish(
            new BackgroundFish(10, new CartPt(1436, 151), 5, 20, 1, 1,
                new OverlayOffsetImage(new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE),
                    -7.5, 0,
                    (new RotateImage(
                        new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE), -270)))),
            this.loBgFish1))
        && t.checkExpect(this.mt.addFreebieTest(10, new Random(20)), new ConsLoBackgroundFish(
            new BackgroundFish(10, new CartPt(253, 886), 5, 20, 1, 0,
                new OverlayOffsetImage(new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE),
                    7.5, 0,
                    (new RotateImage(
                        new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE), 270)))),
            mt));
  }

  // Playerfish tests
  boolean testDrawFish(Tester t) {
    return t.checkExpect(pFish1.drawFish(new WorldScene(1600, 900)),
        new WorldScene(1600, 900)
            .placeImageXY(
                new OverlayOffsetImage(new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE),
                    -7.5, 0,
                    (new RotateImage(
                        new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE), -270))),
                pFish1.position.x, pFish1.position.y)
            .placeImageXY(new TextImage("Score: " + String.valueOf(pFish1.score), 20, Color.BLACK),
                1300, 15)
            .placeImageXY(
                new TextImage("Lives: " + String.valueOf(pFish1.livesLeft), 20, Color.BLACK), 1400,
                15));
  }

  boolean testMoveFish(Tester t) {
    return t.checkExpect(pFish1.moveFish("up"), new PlayerFish(new CartPt(300, 10), 5, 0, 10, 3, 0,
        0,
        new OverlayOffsetImage(new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE), 7.5, 0,
            (new RotateImage(new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE),
                270)))))
        && t.checkExpect(pFish1.moveFish("down"),
            new PlayerFish(new CartPt(300, 30), 5, 0, 10, 3, 0, 0,
                new OverlayOffsetImage(new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE),
                    7.5, 0,
                    (new RotateImage(
                        new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE), 270)))))
        && t.checkExpect(pFish1.moveFish("left"),
            new PlayerFish(new CartPt(290, 20), 5, 0, 10, 3, 0, 0,
                new OverlayOffsetImage(new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE),
                    7.5, 0,
                    (new RotateImage(
                        new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE), 270)))))
        && t.checkExpect(pFish1.moveFish("right"),
            new PlayerFish(new CartPt(310, 20), 5, 0, 10, 3, 0, 0,
                new OverlayOffsetImage(new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE),
                    -7.5, 0,
                    (new RotateImage(
                        new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE),
                        -270)))));
  }

  boolean testAteFishUpdate(Tester t) {
    return t.checkExpect(pFish1.ateFishUpdate(freebie),
        new PlayerFish(new CartPt(300, 20), 7, 1, 10, 3, 0, 0))
        && t.checkExpect(pFish1.ateFishUpdate(smallFishL),
            new PlayerFish(new CartPt(300, 20), 8, 15, 10, 3, 0, 0))
        && t.checkExpect(pFish1.ateFishUpdate(mediumFishL),
            new PlayerFish(new CartPt(300, 20), 9, 20, 10, 3, 0, 0))
        && t.checkExpect(pFish1.ateFishUpdate(largeFishL),
            new PlayerFish(new CartPt(300, 20), 10, 40, 10, 3, 0, 0))
        && t.checkExpect(pFish1.ateFishUpdate(hugeFishL),
            new PlayerFish(new CartPt(300, 20), 11, 70, 10, 3, 0, 0))
        && t.checkExpect(pFish1.ateFishUpdate(fishBoss),
            new PlayerFish(new CartPt(300, 20), 7, 1000, 10, 3, 0, 0));
  }

  boolean testCompareFishesHelp(Tester t) {
    return t.checkExpect(pFish1.compareFishesHelp(5), true)
        && t.checkExpect(pFish1.compareFishesHelp(6), false);
  }

  boolean testUpdateLives(Tester t) {
    return t.checkExpect(pFishITick.updateLives(-1), pFishITick)
        && t.checkExpect(pFish1.updateLives(-1),
            new PlayerFish(new CartPt(300, 20), 5, 0, 10, 2, 0, 10))
        && t.checkExpect(pFish1.updateLives(-2),
            new PlayerFish(new CartPt(300, 20), 5, 0, 10, 1, 0, 10));
  }

  boolean testOutOfLives(Tester t) {
    return t.checkExpect(pFish1.outOfLives(), false) && t.checkExpect(deadFish.outOfLives(), true);
  }

  boolean testCheckCollision(Tester t) {
    return t.checkExpect(pilePFish.checkCollision(pileLoFish1), pileLoFish1)
        && t.checkExpect(pilePFish.checkCollision(new MtLoBackgroundFish()),
            new MtLoBackgroundFish())
        && t.checkExpect(pilePFish.checkCollision(new ConsLoBackgroundFish(fishBoss, pileLoFish1)),
            new ConsLoBackgroundFish(pileBgFish1, new MtLoBackgroundFish()));
  }

  boolean testGetStats(Tester t) {
    return t.checkExpect(pFish1.getStats(), "Score: 0 Lives Left: 3");
  }

//  boolean testApplySnackEffect(Tester t) {
//    
//  }

//  // ASnack Tests
//
//  // tests .snackMatch(ASnack)
  boolean testSnackMatch(Tester t) {
    return t.checkExpect(this.bad1.snackMatch(this.bad1), true)
        && t.checkExpect(this.bad1.snackMatch(this.speed1), false);
  }

//  // tests .checkSnackCollision(CartPt, pfSize)
  boolean testCheckSnackCollision(Tester t) {
    return t.checkExpect(this.bad2.checkSnackCollision(new CartPt(200, 200), 8), false)
        && t.checkExpect(this.bad1.checkSnackCollision(new CartPt(100, 100), 9), true);
  }

//
//  // tests drawSnacks
  boolean testDrawSnacks(Tester t) {
    return t.checkExpect(this.speed1.drawSnacks(this.ws),
        ws.placeImageXY(new OverlayImage(new TextImage("O", Color.BLACK),
            new CircleImage(30, OutlineMode.SOLID, Color.BLUE)), 90, 90))
        && t.checkExpect(null, null);
  }

//
//  // tests applyEffect
  boolean testApplyEffect(Tester t) {
    return t.checkExpect(this.speed1.applyEffect(this.pFish1),
        new PlayerFish(new CartPt(300, 20), 5, 0, 15, 3, 0, 0,
            new OverlayOffsetImage(new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE),
                5 * 1.5, 0,
                (new RotateImage(new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE),
                    270)))))
        && t.checkExpect(this.bad1.applyEffect(this.pFish1),
            new PlayerFish(new CartPt(300, 20), 5, 0, 10, 3, 0, 0,
                new OverlayOffsetImage(new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE),
                    -5 * 1.5, 0,
                    (new RotateImage(
                        new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE), -270)))))
        && t.checkExpect(this.score1.applyEffect(this.pFish1),
            new PlayerFish(new CartPt(300, 20), 5, 2, 10, 3, 0, 0,
                new OverlayOffsetImage(new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE),
                    5 * 1.5, 0, (new RotateImage(
                        new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE), 270)))));
  }

//
//  // LOSNACK Tests
  boolean testGenerateSnack(Tester t) {
    return t.checkExpect(this.snacks1.generateSnackTest(new Random(20), 7),
        new ConsLoSnacks(new SpeedSnacks(7, new CartPt(1436, 801)), this.snacks1))
        && t.checkExpect(this.snacks2.generateSnackTest(new Random(20), 8),
            new ConsLoSnacks(new SpeedSnacks(8, new CartPt(1436, 801)), this.snacks2));
  }

  boolean testEatSnack(Tester t) {
    return t.checkExpect(this.mtsnack.eatSnack(this.bad1), this.mtsnack)
        && t.checkExpect(this.snacks1.eatSnack(this.speed1),
            new ConsLoSnacks(this.bad1, new ConsLoSnacks(this.score1, new MtLoSnacks())));
  }

  boolean testLoDrawSnacks(Tester t) {
    return t.checkExpect(this.mtsnack.drawSnacks(this.ws), this.ws)
        && t.checkExpect(this.snacks1.drawSnacks(this.ws),
            this.ws
                .placeImageXY(new OverlayImage(new TextImage("O", Color.BLACK),
                    new CircleImage(30, OutlineMode.SOLID, Color.BLUE)), 90, 90)
                .placeImageXY(new OverlayImage(new TextImage("X", Color.BLACK),
                    new CircleImage(30, OutlineMode.SOLID, Color.RED)), 100, 100)
                .placeImageXY(new OverlayImage(new TextImage("!!", Color.BLACK),
                    new CircleImage(30, OutlineMode.SOLID, Color.GREEN)), 200, 200));
  }

  boolean testLoCheckSnackCollision(Tester t) {
    return t.checkExpect(this.mtsnack.checkSnackCollision(this.pFish1), this.mtsnack)
        && t.checkExpect(this.snacks1.checkSnackCollision(this.pFish1), this.mtsnack);
  }

  boolean testSnackUpdate(Tester t) {
    return t.checkExpect(this.mtsnack.snackUpdate(this.FishWorld1), this.FishWorld1)
        && t.checkExpect(this.snacks1.snackUpdate(this.fw0), new FishWorld(
            new PlayerFish(new CartPt(300, 20), 5, 2, 15, 3, 0, 0,
                new OverlayOffsetImage(new EllipseImage(20, 10, OutlineMode.SOLID, Color.ORANGE),
                    (5 * 1.5), 0,
                    (new RotateImage(
                        new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.ORANGE), 270)))),
            mt, 20, 0, this.mtsnack));
  }

}

/*
 * info for README
 * 
 * 
 * Feeding Frenzy: Eric Kitagawa & Emma Shum Fundamentals of Computer Science II
 * - Prof Razzaq
 * 
 * How to play: - Use arrow keys to move the 'player fish' (orange fish) - 'eat'
 * other fish by running into them. - you can only eat fish that are smaller
 * than you - you have 3 lives - if you run into a fish that is bigger than you,
 * a life will be removed - once you are out of lives, the game is over - you
 * can win by becoming the biggest fish! - there will always be a 'freebie' fish
 * that is smaller than you - eating snacks will give you boosts, but watch out
 * for bad snacks! those will decrease your size. - you are given a grace period
 * (10 ticks) after colliding with a fish bigger than you. - run the big bang
 * with worldFishGame please! - also note that the window size is not scalable
 * :(
 * 
 * 
 * 
 */
