import tester.*;
import java.util.Random;
import javalib.worldimages.*;
import javalib.funworld.*;
import java.awt.Color;

class FishWorld extends World {
    PlayerFish eric;
    ILoBackgroundFish otherFishies;

    FishWorld(PlayerFish eric, ILoBackgroundFish otherFishies) {
        this.eric = eric;
        this.otherFishies = otherFishies;
    }

    public WorldScene makeScene() {
        // renders current fish
        // renders background
        // renders a score / display
        //System.out.println("makeScene: " + this.eric.position.x + ", " + this.eric.position.y);
        return this.draw(new WorldScene(1500, 750));
    }

    public FishWorld onTick() {
        //System.out.println("Tick: " + this.eric.position.x + ", " + this.eric.position.y);
        return this.canEat(this.returnCollidedFish());
    }

    // move the fish around the scene if an arrow key is pressed
    public FishWorld onKeyEvent(String key) {
        PlayerFish movedEric = this.eric.moveFish(key);
        //System.out.println("Key Event: " + key + " -> " + newEric.position.x + ", " + newEric.position.y);
        return new FishWorld(movedEric, this.otherFishies);
    }

    public WorldScene draw(WorldScene acc) {
        //(DEBUGGER) System.out.println("Drawing at: " + this.eric.position.x + ", " + this.eric.position.y);
        // Creates the world with the BGfish first then passes
        // it as an accumulator into draw player fish method
        WorldScene bgFishScene = this.otherFishies.drawBGfish(acc);
        return this.eric.drawFish(bgFishScene);
    }

//    public WorldEnd worldEnds() {
//        // placeholders for now
//        if (this.eric.outOfLives()) {
//            return new WorldEnd(true, new WorldScene(500, 500));
//        }
//        else {
//            return new WorldEnd(false, new WorldScene(500, 500));
//        }
//    }

    // Generate fish
    FishWorld generateFish(String name, int id) {

    }

    // takes in a list of bg fish that collided with the player fish
    // and determines if they can be eaten by the player or if a live needs to be taken
    FishWorld canEat(ILoBackgroundFish collidedFish) {
        return collidedFish.collisionUpdate(this);
    }

    // checks if the player has collided with any background fish and returns a list of
    // collided
    ILoBackgroundFish returnCollidedFish() {
        return this.eric.checkCollision(this.otherFishies);
    }

    FishWorld canEatHelper(BackgroundFish otherFish) {
        if (otherFish.compareFishes(this.eric)) { // will return true when this fish CAN EAT
            return new FishWorld(this.eric.ateFishUpdate(otherFish), otherFishies.eat(otherFish));
        }
        else {
            // If the BG fish is bigger, it'll remove a life from
            // the world state and determine if the game is over
            return new FishWorld(this.eric.updateLives(-1), otherFishies);
        }
    }
}

class PlayerFish {
    CartPt position;
    int size;
    int score;
    int livesLeft;

    WorldImage fishModel = new OverlayOffsetImage(
            new EllipseImage(75, 50, OutlineMode.SOLID, Color.ORANGE), 30, 0,
            (new RotateImage(new EquilateralTriangleImage(50, OutlineMode.SOLID, Color.ORANGE), 270)));

    PlayerFish(CartPt position, int size, int score, int livesLeft) {
        this.position = position;
        this.size = size;
        this.score = score;
        this.livesLeft = livesLeft;
    }

    // renders eric
    WorldScene drawFish(WorldScene acc) {
        return acc
                .placeImageXY(this.fishModel, this.position.x, this.position.y);
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
            Y -= 10; //* speedMod;
        }
        else if (key.equals("down")) {
            Y += 10;
        }
        else if (key.equals("left")) {
            X -= 10;
        }
        else if (key.equals("right")) {
            X += 10;
        }
        // need to make these adjustable so if you change the
        // scene size it'll adjust this too
        if (X < 0) X = 1500;
        if (X > 1500) X = 0;
        if (Y < 0) Y = 750;
        if (Y > 750) Y = 0;

        return new PlayerFish(new CartPt(X, Y), this.size, this.score, this.livesLeft);
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
                otherFish.calculateScoreGained(this.score), this.livesLeft);
    }

    // checks if the size of this PlayerFish is greater than a BackgroundFish
    Boolean compareFishesHelp(double otherFishSize) {
        return this.size >= otherFishSize;
    }

    PlayerFish updateLives(int updateAmt) {
        return new PlayerFish(this.position, this.size, this.score, updateAmt + this.livesLeft);
    }

    boolean outOfLives() {
        return this.livesLeft <= 0;
    }

    ILoBackgroundFish checkCollision(ILoBackgroundFish bgFishList) {
        return bgFishList.checkCollision(this.position, this.size);
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
    CartPt position;
    int size;
    int speed;
    int points;

    BackgroundFish(String name, CartPt position, int size, int speed, int points) {
        this.name = name;
        this.position = position;
        this.size = size;
        this.speed = speed;
        this.points = points;
    }

    // PLACEHOLDER BGFISH MODEL
    WorldImage bgFishModel = new OverlayOffsetImage(
            new EllipseImage(50, 25, OutlineMode.SOLID, Color.GREEN), 30, 0,
            (new RotateImage(new EquilateralTriangleImage(25, OutlineMode.SOLID, Color.GREEN), 270)));

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
        return otherFish.fishMatchHelper(this.name);
    }

    // determines if the name provided matches this bg fish's name
    boolean fishMatchHelper(String otherName) {
        return this.name.equals(otherName);
    }

    WorldScene drawBGfish(WorldScene acc) {
        return acc.placeImageXY(bgFishModel, this.position.x, this.position.y);
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

        return bgX2 < pfX1 &&
                bgX1 > pfX2 &&
                bgY2 < pfY1 &&
                bgY1 > pfY2;
    }
}

interface ILoBackgroundFish {
    // searches a list for that fish and returns a
    // a list of bg fish with that one removed
    ILoBackgroundFish eat(BackgroundFish otherFish);
    WorldScene drawBGfish(WorldScene acc);
    ILoBackgroundFish checkCollision(CartPt pfPosition, double pfSize);
    FishWorld collisionUpdate(FishWorld fishWorld);

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
    // goes through a list of bg fish and returns any that are colliding with
    // the hitbox (calculated through pf position and pf size) of the playerfish
    public ILoBackgroundFish checkCollision(CartPt pfPosition, double pfSize) {
        if (this.first.checkCollisionHelper(pfPosition, pfSize)) {
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

interface Snack {

}

class SpeedSnacks implements Snack {

}

class BadSnack implements Snack {
    // decrease size
}

class ScoreSnacks implements Snack {

}

interface ILoSnacks extends Snack {

}

class MtLoSnacks implements ILoSnacks {

}

class ConsLoSnacks implements ILoSnacks {
    Snack first;
    ILoSnacks rest;

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
    PlayerFish deadFish = new PlayerFish(new CartPt(300, 300), 50, 0, 3);
    PlayerFish pFish1 = new PlayerFish(new CartPt(300, 300), 50, 0, 3);

    BackgroundFish bgFish1 = new BackgroundFish("fish 1",
            new CartPt(250, 250), 20, 2, 20);
    BackgroundFish bgFish2 = new BackgroundFish("fish 2",
            new CartPt(700, 700), 10, 2, 10);


    // pileBgFish1 is directly on top of pFish1 and is smaller than PF
    BackgroundFish pileBgFish1 = new BackgroundFish("pile fish 1",
            new CartPt(300, 300), 30, 1, 20);
    // pileBgFish1 is directly on top of pFish1 and is bigger than PF
    BackgroundFish pileBgFish2 = new BackgroundFish("pile fish 2",
            new CartPt(300, 300), 100, 1, 20);

    // pileBgFish lists
    ILoBackgroundFish tempList =
            new ConsLoBackgroundFish(bgFish2, new MtLoBackgroundFish());
    ILoBackgroundFish loBgFish1 =
            new ConsLoBackgroundFish(bgFish1, new MtLoBackgroundFish());
    ILoBackgroundFish loBgFish2 =
            new ConsLoBackgroundFish(bgFish2, loBgFish1);


    // used to test when a fish is directly over top PF
    ILoBackgroundFish pileLoFish1 =
            new ConsLoBackgroundFish(pileBgFish1, new MtLoBackgroundFish());
    ILoBackgroundFish pileLoFish2 =
            new ConsLoBackgroundFish(pileBgFish2, new MtLoBackgroundFish());

    // used to test gameover
    FishWorld deadFishWorld = new FishWorld (deadFish, loBgFish1);

    // used to test when a bgfish is directly on top of pf
    FishWorld pileWorld1 = new FishWorld (pFish1, pileLoFish1);
    FishWorld pileWorld2 = new FishWorld (pFish1, pileLoFish2);

    // normal game state
    FishWorld FishWorld1 = new FishWorld(pFish1, loBgFish2);

    boolean testBigBang(Tester t) {
        FishWorld fw = FishWorld1;
        int worldWidth = 1500;
        int worldHeight = 750;
        double tickRate = 1;
        return fw.bigBang(worldWidth, worldHeight, tickRate);
    }
    boolean testCollision(Tester t) {
        return t.checkExpect(this.FishWorld1.returnCollidedFish(), new MtLoBackgroundFish())
                && t.checkExpect(this.pileWorld1.returnCollidedFish(), pileLoFish1)
                && t.checkExpect(this.pileWorld2.returnCollidedFish(), pileLoFish2);
    }
}


