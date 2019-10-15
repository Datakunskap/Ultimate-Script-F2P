package script.tutorial_island;

import org.rspeer.runetek.adapter.Interactable;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.input.menu.ActionOpcodes;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.runetek.providers.RSTileDecor;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

import java.util.*;
import java.util.function.Predicate;

public class dqw4w9wgxcq extends Task {
    public static final int VARP = 281;
    private static final Position STRIKE_POS = new Position(3139, 3091);
    private InterfaceComponent nameAcceptedWidget;//.getFirst()w -> w.getMessage().contains("Great!"));
    private InterfaceComponent nameLookupWidget;
    private InterfaceComponent nameInputWidget;//(w -> w.getMessage().contains("unique"));
    private InterfaceComponent nameSetWidget;
    private InterfaceComponent nameScreenDetectionWidget; //("Choose display name");
    private InterfaceComponent creationScreenWidget = Interfaces.getComponent(269, 120);
    private boolean isAudioDisabled;
    private final char[] vowels = "aeiouAEIOU".toCharArray();
    private final char[] nonVowels = "bcdfghjklmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ".toCharArray();

    private int config;
    private TutorialIsland main;

    public dqw4w9wgxcq(TutorialIsland main) {
        this.main = main;
    }

    private void getComponents() {
        nameAcceptedWidget = Interfaces.getComponent(558, 12);
        nameLookupWidget = Interfaces.getComponent(558, 17);
        nameInputWidget = Interfaces.getComponent(162, 44);
        nameSetWidget = Interfaces.getComponent(558, 18);
        nameScreenDetectionWidget = Interfaces.getComponent(558, 3, 1);
        creationScreenWidget = Interfaces.getComponent(269, 120);
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public int execute() {
        config = Varps.get(VARP);
        boolean doDefault = false;
        Predicate<String> defaultAction = a -> true;

        Log.info("" + config);

        if (Players.getLocal().getAnimation() != -1 || Players.getLocal().isMoving()) {
            return 1000;
        }

        switch (config) {
            case 0:
            case 1:
            case 2:
                getComponents();
                if (nameScreenDetectionWidget != null && nameScreenDetectionWidget.isVisible()) {
                    setDisplayName();
                } else if (isCreationScreenVisible()) {
                    try {
                        createRandomCharacter();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (Dialog.isViewingChatOptions()) {//experienceWidget.get(getWidgets()).isPresent()) {
                    if (Dialog.process(randInt(1, 3))) {
                        Time.sleep(400, 800);
                    }
                } else {
                    doDefault = true;
                }
                break;
            case 3:
                if (Tabs.getOpen() == Tab.OPTIONS) {
                    doDefault = true;
                } else {
                    Tabs.open(Tab.OPTIONS);
                    doDefault = true;
                }
                break;
            case 7: case 10:
                int componentIndex = -1;
                if (Varps.get(168) != 4) {
                    componentIndex = 24;
                } else if (Varps.get(169) != 4) {
                    componentIndex = 30;
                } else if (Varps.get(872) != 4) {
                    componentIndex = 36;
                }

                if (componentIndex != -1) {
                    doDefault = true;
                    break;
                }

                InterfaceComponent mute = Interfaces.getComponent(261, componentIndex);
                if (mute == null) {
                    Interfaces.getComponent(261, 1, 2).interact(a -> true);
                } else {
                    mute.interact(a -> true);
                }
                break;
            case 20:
                if (!Movement.isRunEnabled()) {
                    Movement.toggleRun(true);
                    Time.sleep(400, 800);
                }

                doDefault = true;
                break;
            case 30:
                if (Dialog.canContinue()) {
                    Dialog.processContinue();
                } else {
                    Interfaces.getComponent(164, 53).interact(a->true);
                }
                break;
            case 50:
                Tabs.open(Tab.SKILLS);
                break;
            case 70:
                SceneObject tree = SceneObjects.getNearest("Tree");
                if (tree != null && tree.interact("Chop down")) {
                    Time.sleep(3000);
                }
                break;
            case 80:
            case 90:
            case 100:
            case 110:
                if (!Tabs.isOpen(Tab.INVENTORY)) {
                    Tabs.open(Tab.INVENTORY);
                } else if (!Inventory.contains("Raw shrimps")) {
                    Npcs.getNearest("Fishing spot").interact("Net");
                } else if (SceneObjects.getNearest("Fire") == null || (Interfaces.getComponent(263, 1, 0) != null &&
                        Interfaces.getComponent(263, 1, 0).getText().contains("time to light a fire"))) {
                    if (!Inventory.contains("Logs")) {
                        SceneObjects.getNearest("Tree").interact("Chop down");
                        Time.sleep(3000);
                    } else {
                        SceneObject fire = SceneObjects.getFirstAt(Players.getLocal().getPosition());
                        if (fire == null || !fire.getName().equals("Fire")) {
                            useItemOn("Logs", Inventory.getFirst("Tinderbox"));
                        } else {
                            Movement.walkTo(Players.getLocal().getPosition().translate(Random.nextInt(-3, 3), Random.nextInt(-3, 3)));
                            Time.sleep(3000);
                        }
                    }
                } else {
                    if (Inventory.contains("Raw shrimps")) {
                        useItemOn("Raw shrimps", SceneObjects.getNearest("Fire"));
                    } else {
                        Npcs.getNearest("Fishing spot").interact("Net");
                    }
                }
                break;
            case 150:
                useItemOn("Pot of flour", Inventory.getFirst("Bucket of water"));
                break;
            case 160:
                if (Inventory.isItemSelected()) {
                    doDefault = true;
                } else {
                    Inventory.getFirst("Bread dough").interact("Use");
                }
                break;
            case 170:
                Tabs.open(Tab.INVENTORY);
                if (!Movement.isRunEnabled()) {
                    Movement.toggleRun(true);
                    Time.sleep(400, 800);
                }else if(new Position(3085, 3127).distance() > 20){
                    randWalker(new Position(3085, 3127), p -> p.distance() > 20);
                    //Movement.walkTo(new Position(3085, 3127));
                }else {
                    doDefault = true;
                }
                break;
            case 183:
                Tabs.open(Tab.EMOTES);
                break;
            case 187:
                Interfaces.getComponent(216, 1, 0).interact("Yes");
                break;
            case 190:
                Tabs.open(Tab.OPTIONS);
                break;
            case 200:
                Interfaces.getComponent(160, 22).interact("Toggle Run");
                break;
            case 210:
                if (!Movement.isRunEnabled()) {
                    Movement.toggleRun(true);
                    Time.sleep(400, 800);
                }else if(new Position(3085, 3127).distance() > 20){
                    Movement.walkTo(new Position(3085, 3127));
                }else {
                    doDefault = true;
                }
                break;
            case 230:
                Tabs.open(Tab.QUEST_LIST);
                doDefault = true;
                break;
            case 260:
                if (Players.getLocal().getY() > 9517) {
                    Movement.walkTo(new Position(3081, 9509));
                } else {
                    doDefault = true;
                }
                break;
            case 270: case 280:
                defaultAction = a -> a.equals("Prospect");
                doDefault = true;
                break;
            case 320:
                if (Inventory.isItemSelected()) {
                    doDefault = true;
                } else {
                    Inventory.getFirst("Tin ore").interact("Use");
                }
                break;
            case 350:
                Interfaces.getComponent(312, 9).interact("Smith");
                break;
            case 390:
                Tabs.open(Tab.EQUIPMENT);
                doDefault = true;
                break;
            case 400:
                Interfaces.getComponent(387, 17).interact("View equipment stats");
                break;
            case 405:
                wieldItem("Bronze dagger");
                break;
            case 420:
                if (!Equipment.contains("Bronze sword")) {
                    wieldItem("Bronze sword");
                } else if (!Equipment.contains("Wooden shield")) {
                    wieldItem("Wooden shield");
                }
                break;
            case 430:
                Tabs.open(Tab.COMBAT);
                break;
            case 440:
                SceneObjects.getNearest(so -> so.getName().equalsIgnoreCase("Gate") && so.getY() > 9515).interact("Open");
                break;
            case 450: case 460:
                if (Players.getLocal().getTargetIndex() == -1) {
                    Npcs.getNearest(n -> n.getName().equals("Giant rat") && n.getTargetIndex() == -1).interact(a -> true);
                }
                break;
            case 470:
                if (Movement.isInteractable(Npcs.getNearest(3307), false)) {
                    doDefault = true;
                } else {
                    SceneObjects.getNearest(so -> so.getName().equalsIgnoreCase("Gate") && so.getY() > 9515).interact("Open");
                }
                break;
            case 490:
                if (!Equipment.contains("Shortbow")) {
                    wieldItem("Shortbow");
                } else if (!Equipment.contains("Bronze arrow")) {
                    wieldItem("Bronze arrow");
                } else if (Players.getLocal().getTargetIndex() == -1) {
                    Npcs.getNearest(n -> n.getName().equals("Giant rat") && n.getTargetIndex() == -1).interact(a -> true);
                }
                break;
            case 510:
                if (Dialog.isViewingChatOptions()) {
                    Dialog.process(0);
                } else {
                    doDefault = true;
                }
                break;
            case 531:
                Tabs.open(Tab.ACCOUNT_MANAGEMENT);
                doDefault = true;
                break;
            case 550:
                if (Players.getLocal().getY() > 3116) {
                    Movement.walkTo(new Position(3134, 3116));

                } else if(Movement.isInteractable(new Position(3127, 3106), false)){
                    doDefault = true;
                }else {
                    SceneObjects.getNearest(so -> so.distance(new Position(3127, 3106)) < 5 && so.containsAction("Open")).interact("Open");
                }
                break;
            case 560:
                Tabs.open(Tab.PRAYER);
                doDefault = true;
                break;
            case 580:
                Tabs.open(Tab.FRIENDS_LIST);
                doDefault = true;
                break;
            case 590:
                Tabs.open(Tab.ACCOUNT_MANAGEMENT);
                break;
            case 620:
                if (Players.getLocal().getY() > 3100) {
                    Movement.walkTo(new Position(3131, 3088));
                } else {
                    doDefault = true;
                }
                break;
            case 630:
                Tabs.open(Tab.MAGIC);
                break;
            case 650:
                if (STRIKE_POS.equals(Players.getLocal().getPosition())) {
                    if (Magic.isSpellSelected()) {
                        Npcs.getNearest("Chicken").interact(8);
                    } else {
                        Magic.cast(Spell.Modern.WIND_STRIKE);
                    }
                } else {
                    Movement.walkTo(STRIKE_POS);
                }
                break;
            case 670:
                if (Dialog.isViewingChatOptions()) {
                    if (Interfaces.getComponent(219, 1, 0).getText().contains("o you want to go to")) {
                        Dialog.process(0);
                    } else {
                        Dialog.process(2);
                    }
                } else {
                    doDefault = true;
                }
                break;
            case 1000:
                if (!main.onTutorialIsland()) {
                    switch (Beggar.randInt(0, 1)) {
                        case 0:
                            getEmptyPosition(false, Beggar.TUTORIAL_COMPLETED_WALK_DIST, false).ifPresent(this::randWalker);
                            break;
                        case 1:
                            randWalker(BankLocation.LUMBRIDGE_CASTLE.getPosition());
                            break;
                    }
                    main.beggar.startFighter(true);
                }
                break;
            default:
                doDefault = true;
                break;
        }

        if (doDefault) {
            InterfaceComponent wierdContinue = Interfaces.getComponent(162, 44);
            if (wierdContinue != null && wierdContinue.isVisible() && !wierdContinue.isExplicitlyHidden()) {
                Game.getClient().fireScriptEvent(299, 1, 1);
            } else if (Dialog.canContinue()) {
                Dialog.processContinue();
            } else if (!Dialog.isProcessing()) {
                switch (Game.getClient().getHintArrowType()) {
                    case 0:
                        Log.info("no hint arrow");
                        break;
                    case 1:
                        Npcs.getAt(Game.getClient().getHintArrowNpcIndex()).interact(defaultAction);
                        break;
                    case 2:
                        Position hintPos = new Position(Game.getClient().getHintArrowX(), Game.getClient().getHintArrowY(), Players.getLocal().getFloorLevel());
                        Log.info(hintPos.toString());
                        for (SceneObject so : SceneObjects.getAt(hintPos)) {
                            if (so.containsAction(defaultAction)) {
                                so.interact(defaultAction);
                                break;
                            }
                        }
                        break;
                }
            }
        }

        return Random.high(800, 2500);
    }

    private void useItemOn(String itemName, Interactable target) {
        if (Inventory.isItemSelected()) {
            if (target.interact("Use")) {
                Time.sleepUntil(() -> Varps.get(VARP) != config, 2000, 30 * 1000);
            }
        } else {
            Inventory.getFirst(itemName).interact("Use");
        }
    }

    private void setDisplayName() {
        if (nameAcceptedWidget != null && nameAcceptedWidget.isVisible() && nameAcceptedWidget.getText().contains("Great!")) {
            if (nameSetWidget != null && nameSetWidget.isVisible() && nameSetWidget.interact(ActionOpcodes.INTERFACE_ACTION)) {
                Time.sleepUntil(() -> !nameScreenDetectionWidget.isVisible(), 2000, 8000);
                Time.sleep(400, 800);
            }
        } else if (nameInputWidget != null && nameInputWidget.isVisible()
                && !nameInputWidget.isExplicitlyHidden()
                && nameInputWidget.getText().contains("unique")) {

            Keyboard.sendText(getRandString(3, 7));
            //Keyboard.sendText(generateRandomString(7));
            Keyboard.pressEnter();
            final int configValue = Varps.get(1042);

            Time.sleepUntil(() -> Varps.get(1042) != configValue, 2000, 8000);
            Time.sleepUntil(() -> Varps.get(1042) == configValue || (nameAcceptedWidget != null && nameAcceptedWidget.isVisible()), 2000, 8000);
            Time.sleep(400, 800);
        } else if (nameLookupWidget != null && nameLookupWidget.isVisible()
                && nameLookupWidget.interact(ActionOpcodes.INTERFACE_ACTION)) {
            Time.sleepUntil(() -> nameInputWidget != null && nameInputWidget.isVisible() && !nameInputWidget.isExplicitlyHidden(), 2000,8000);
            Time.sleep(400, 800);
        }
    }

    private boolean isCreationScreenVisible() {
        return creationScreenWidget != null && creationScreenWidget.isVisible();//.get(getWidgets()).filter(RS2Widget::isVisible).isPresent();
    }

    private void createRandomCharacter() throws InterruptedException {
        if (new java.util.Random().nextInt(2) == 1) {
            Interfaces.getComponent(269, 137).interact(ActionOpcodes.INTERFACE_ACTION);//.getWidgetContainingText("Female").interact();
        }

        final InterfaceComponent[] childWidgets = Interfaces.get(creationScreenWidget.getRootIndex());//.getWidgets(creationScreenWidget.get(getWidgets()).get().getRootId());
        Collections.shuffle(Arrays.asList(childWidgets));

        for (final InterfaceComponent childWidget : childWidgets) {
            //Log.info(childWidget.getToolTip());
            if (childWidget.getToolTip() == null) {
                continue;
            }
            if (childWidget.getToolTip().contains("Change") || childWidget.getToolTip().contains("Recolour")) {
                clickRandomTimes(childWidget);
            }
        }

        if (Interfaces.getComponent(269, 99).interact(ActionOpcodes.INTERFACE_ACTION)) {
            Time.sleepUntil(() -> !isCreationScreenVisible(), 2000, 3000);
            Time.sleep(400, 800);
        }
    }

    private void clickRandomTimes(final InterfaceComponent widget) {
        int clickCount = new java.util.Random().nextInt(8);

        for (int i = 0; i < clickCount; i++) {
            if (widget.interact(ActionOpcodes.INTERFACE_ACTION)) {
                Time.sleep(300,  1000);
            }
        }
    }

    private int randInt(int min, int max) {
        java.util.Random rand = new java.util.Random();
        return rand.nextInt(max - min + 1) + min;
    }

    private String getRandString(int minLength, int maxLength) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghijklmnopqrstuvwxyz";
        StringBuilder salt = new StringBuilder();
        salt.append(randomPrefix());
        java.util.Random rnd = new java.util.Random();
        int strLen = randInt(minLength, maxLength);

        while (salt.length() < strLen) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }

        return salt.toString();
    }

    private String randomPrefix() {
        StringBuilder salt = new StringBuilder();

        switch (randInt(0, 1)) {
            case 0:
                salt.append(vowels[randInt(0, (vowels.length - 1))]);
                salt.append(nonVowels[randInt(0, (nonVowels.length - 1))]);
                salt.append(vowels[randInt(0, (vowels.length - 1))]);
            case 1:
                salt.append(nonVowels[randInt(0, (nonVowels.length - 1))]);
                salt.append(vowels[randInt(0, (vowels.length - 1))]);
                salt.append(nonVowels[randInt(0, (nonVowels.length - 1))]);
        }
        return salt.toString();
    }

    private void wieldItem(String name) {
        if (Inventory.getFirst(name).interact("Wield") || Inventory.getFirst(name).interact("Equip")) {
            Time.sleepUntil(() -> Equipment.contains(name), 2000, 1500);
            Time.sleep(400, 800);
        }
    }

    private void randWalker(Position posRequired, Predicate<Position> predicate) {
        Log.info("Walking to position");
        while (predicate.test(posRequired) && Game.isLoggedIn()) {
            Time.sleep(800, 1800);
            Movement.walkToRandomized(posRequired);
        }
            int times = 1;//Beggar.randInt(1, 2);
            Log.info("Random walking " + times + " time(s)");
            for (int i = 0; i < times; i++) {
                Movement.walkToRandomized(Players.getLocal().getPosition().randomize(8));
                //getEmptyPosition(false, Beggar.randInt(1, 9), false).ifPresent(Movement::walkTo);
                Time.sleepUntil(() -> Players.getLocal().isMoving(), Beggar.randInt(800, 1500));
                Time.sleepUntil(() -> !Players.getLocal().isMoving(), 1000, Beggar.randInt(2000, 5000));
            }
    }

    private void randWalker(Position posRequired) {
        Log.info("Walking to position");
        while (!Players.getLocal().getPosition().equals(posRequired) && Game.isLoggedIn()) {
            if (!Time.sleepUntil(() -> Players.getLocal().getPosition().equals(posRequired), Random.low(600, 1800))) {
                Movement.walkTo(posRequired);
            }
        }
        if (posRequired.distance(Players.getLocal()) <= 3) {
            int times = Beggar.randInt(1, 2);
            Log.info("Random walking " + times + " time(s)");
            for (int i = 0; i < times; i++) {
                //Movement.walkToRandomized(Players.getLocal().getPosition().randomize(8));
                getEmptyPosition(false, Beggar.randInt(1, 9), false).ifPresent(Movement::walkTo);
                Time.sleepUntil(() -> Players.getLocal().isMoving(), Beggar.randInt(800, 1500));
                Time.sleepUntil(() -> !Players.getLocal().isMoving(), 600, Beggar.randInt(2000, 4000));
            }
        }
    }

    private Optional<Position> getEmptyPosition(boolean min, int distance, boolean removeNonInteractable) {
        List<Position> allPositions = Area.surrounding(Players.getLocal().getPosition(), distance).getTiles();

        // Remove any position with an object (except ground decorations, as they can be walked on)
        for (SceneObject object : SceneObjects.getLoaded()) {
            if (object.getProvider() instanceof RSTileDecor) {
                continue;
            }
            allPositions.removeIf(position -> object.getPosition().equals(position));
        }

        allPositions.removeIf(position -> !position.isPositionWalkable() || !Movement.isWalkable(position, false));
        allPositions.removeIf(position -> position.distance(Players.getLocal()) <= 0);
        if (removeNonInteractable) {
            allPositions.removeIf(position -> Movement.isInteractable(position, false));
        }

        if (min) {
            return allPositions.stream().min(Comparator.comparingInt(p -> (int) Players.getLocal().getPosition().distance(p)));
        } else {
            return allPositions.stream().max(Comparator.comparingInt(p -> (int) Players.getLocal().getPosition().distance(p)));
        }
    }
}

