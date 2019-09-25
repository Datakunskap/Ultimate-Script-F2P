package script.tutorial_island;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.InterfaceOptions;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Tab;
import org.rspeer.runetek.api.component.tab.Tabs;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.input.menu.ActionOpcodes;
import org.rspeer.runetek.api.scene.SceneObjects;
import script.Beggar;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

public final class RuneScapeGuideSection extends TutorialSection{


    private InterfaceComponent nameAcceptedWidget;//.getFirst()w -> w.getMessage().contains("Great!"));
    private InterfaceComponent nameLookupWidget;
    private InterfaceComponent nameInputWidget;//(w -> w.getMessage().contains("unique"));
    private InterfaceComponent nameSetWidget;
    private InterfaceComponent nameScreenDetectionWidget; //("Choose display name");
    private InterfaceComponent creationScreenWidget = Interfaces.getComponent(269, 120);
    private boolean isAudioDisabled;
    private final char[] vowels = "aeiouAEIOU".toCharArray();
    private final char[] nonVowels = "bcdfghjklmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ".toCharArray();

    public RuneScapeGuideSection() {
        super("Gielinor Guide");
    }

    @Override
    public boolean validate() {
        return getTutorialSection() >= 0 && getTutorialSection() <= 1;
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
    public int execute () {

        getComponents();

        if (pendingContinue()) {
            selectContinue();
            return TutorialIsland.getRandSleep();
        }

        switch (getProgress()) {
            case 0:
            case 1:
            case 2:
                if (nameScreenDetectionWidget != null && nameScreenDetectionWidget.isVisible()) {
                    //Log.info("Setting IGN");
                    setDisplayName();
                } else if (isCreationScreenVisible()) {
                    try {
                        createRandomCharacter();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (Dialog.isViewingChatOptions()) {//experienceWidget.get(getWidgets()).isPresent()) {
                    if (Dialog.process(Beggar.randInt(1, 3))) {
                        Time.sleepUntil(() -> !Dialog.isProcessing() && !Dialog.isViewingChatOptions(), 2000, 2000);
                    }
                } else {
                    talkToInstructor();
                }
                break;
            case 3:
                Tabs.open(Tab.OPTIONS);
                break;
            case 10:
                if (!InterfaceOptions.getViewMode().equals(InterfaceOptions.ViewMode.FIXED_MODE)) {
                    InterfaceComponent fixed = Interfaces.getComponent(261, 33);
                    if (fixed != null && fixed.isVisible()) {
                        fixed.interact(ActionOpcodes.INTERFACE_ACTION);
                    }
                } else if (false/*!isAudioDisabled*/) {
                    isAudioDisabled = disableAudio();
                } else if (!InterfaceOptions.Display.isRoofsHidden()) {
                    InterfaceComponent adv = Interfaces.getComponent(261, 35);
                    if (adv != null && adv.isVisible() && adv.interact(ActionOpcodes.INTERFACE_ACTION)) {
                        toggleRoofsHidden();
                    }
                } /*else if (InterfaceOptions.!getSettings().isShiftDropActive()) {
                    toggleShiftDrop();
                }*/ else if (SceneObjects.getNearest("Door").interact("Open")) {
                    randWalker(SceneObjects.getNearest("Door").getPosition());
                    Time.sleepUntil(() -> getProgress() != 10, 2000, 5000);
                }
                break;
            default:
                talkToInstructor();
                break;
        }
        return TutorialIsland.getRandSleep();
    }

    private void setDisplayName() {
        if (nameAcceptedWidget != null && nameAcceptedWidget.isVisible() && nameAcceptedWidget.getText().contains("Great!")) {
            if (nameSetWidget != null && nameSetWidget.isVisible() && nameSetWidget.interact(ActionOpcodes.INTERFACE_ACTION)) {
                Time.sleepUntil(() -> !nameScreenDetectionWidget.isVisible(), 2000, 8000);
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
        } else if (nameLookupWidget != null && nameLookupWidget.isVisible()
                && nameLookupWidget.interact(ActionOpcodes.INTERFACE_ACTION)) {
            Time.sleepUntil(() -> nameInputWidget != null && nameInputWidget.isVisible() && !nameInputWidget.isExplicitlyHidden(), 2000,8000);
        }
    }

    private String getRandString(int minLength, int maxLength) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ abcdefghijklmnopqrstuvwxyz";
        StringBuilder salt = new StringBuilder();
        salt.append(randomPrefix());
        java.util.Random rnd = new java.util.Random();
        int strLen = Beggar.randInt(minLength, maxLength);

        while (salt.length() < strLen) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }

        return salt.toString();
    }

    private String randomPrefix() {
        StringBuilder salt = new StringBuilder();

        switch (Beggar.randInt(0, 1)) {
            case 0:
                salt.append(vowels[Beggar.randInt(0, (vowels.length - 1))]);
                salt.append(nonVowels[Beggar.randInt(0, (nonVowels.length - 1))]);
                salt.append(vowels[Beggar.randInt(0, (vowels.length - 1))]);
            case 1:
                salt.append(nonVowels[Beggar.randInt(0, (nonVowels.length - 1))]);
                salt.append(vowels[Beggar.randInt(0, (vowels.length - 1))]);
                salt.append(nonVowels[Beggar.randInt(0, (nonVowels.length - 1))]);
        }
        return salt.toString();
    }

    private String generateRandomString(int maxLength) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvwxyz";
        return new Random().ints(new Random().nextInt(maxLength) + 1, 0, chars.length())
                .mapToObj(i -> "" + chars.charAt(i))
                .collect(Collectors.joining());
    }

    private boolean isCreationScreenVisible() {
        return creationScreenWidget != null && creationScreenWidget.isVisible();//.get(getWidgets()).filter(RS2Widget::isVisible).isPresent();
    }

    private void createRandomCharacter() throws InterruptedException {
        if (new Random().nextInt(2) == 1) {
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
        }
    }

    private void clickRandomTimes(final InterfaceComponent widget) throws InterruptedException {
        int clickCount = new Random().nextInt(8);

        for (int i = 0; i < clickCount; i++) {
            if (widget.interact(ActionOpcodes.INTERFACE_ACTION)) {
                Time.sleep(300,  1000);
            }
        }
    }

    private boolean disableAudio() {
        /*Event disableAudioEvent = new DisableAudioEvent();
        execute(disableAudioEvent);
        return disableAudioEvent.hasFinished();*/
        isAudioDisabled = true;
        return true;
    }

    private boolean toggleRoofsHidden() {
        InterfaceComponent hide = Interfaces.getComponent(60, 14);
        if (hide != null && hide.isVisible()) {
            hide.interact(ActionOpcodes.INTERFACE_ACTION);
        }
        return true;
    }

    private boolean toggleShiftDrop() {
        /*Event toggleShiftDrop = new ToggleShiftDropEvent();
        execute(toggleShiftDrop);
        return toggleShiftDrop.hasFinished();*/
        return true;
    }
}