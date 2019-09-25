package script.fighter.nodes.progressive;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.ui.Log;
import script.Beggar;
import script.fighter.Fighter;
import script.fighter.config.ProgressiveSet;
import script.fighter.framework.BackgroundTaskExecutor;
import script.fighter.framework.Node;
import script.fighter.models.Progressive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgressionChecker extends Node {

    private Progressive progressive;

    private Fighter main;

    public ProgressionChecker(Fighter main){
        this.main = main;
        BackgroundTaskExecutor.submit(() -> ProgressiveSet.setCurrent(ProgressiveSet.getBest()), 1000);
    }

    public ProgressionChecker() {
        BackgroundTaskExecutor.submit(() -> ProgressiveSet.setCurrent(ProgressiveSet.getBest()), 1000);
    }

    private List<Item> toSwitch;

    private List<Item> checkEquipmentToSwitch() {
        //Log.info("Equipping items");
        List<Item> indexes = new ArrayList<>();
        HashMap<EquipmentSlot, String> map = progressive.getEquipmentMap();

        for (Map.Entry<EquipmentSlot, String> entry : map.entrySet()) {
            String equipment = entry.getValue();
            if (equipment == null) {
                continue;
            }
            String name = entry.getKey().getItemName();

            if (equipment.toLowerCase().contains("bow") && Equipment.contains(equipment) &&
                    !Equipment.isOccupied(EquipmentSlot.QUIVER) && !Inventory.contains(x -> x.getName().toLowerCase().contains("arrow"))) {
                noAmmoSwitch();
            }
            if (name == null || !name.equals(equipment)) {
                Item inv = Inventory.getFirst(equipment);
                if (inv == null) {
                    continue;
                }
                indexes.add(inv);
            }
        }

        return indexes;
    }


    @Override
    public boolean validate() {
        progressive = ProgressiveSet.getCurrent();
        if (progressive == null) {
            return false;
        }
        Combat.AttackStyle style = Combat.getAttackStyle();
        if (style != null && !progressive.getStyle().equals(style)) {
            return true;
        }
        toSwitch = checkEquipmentToSwitch();
        return toSwitch.size() > 0;
    }

    @Override
    public int execute() {
        Combat.AttackStyle style = Combat.getAttackStyle();
        if (!progressive.getStyle().equals(style)) {
            Combat.WeaponType type = Combat.getWeaponType();
            Combat.AttackStyle[] possibleStyles = type.getAttackStyles();
            for (int i = 0; i < type.getAttackStyles().length; i++) {
                if (possibleStyles[i] == null) {
                    Log.severe("STYLE: " + Combat.getAttackStyle().getName() + "  |  TYPE"  + Combat.getWeaponType().name());
                    ProgressiveSet.getCurrent().setStyle(Combat.AttackStyle.ACCURATE);
                }
                if(possibleStyles[i].equals(progressive.getStyle())) {
                    Combat.select(i);
                    break;
                }
            }
        }
        if (toSwitch == null) {
            return Fighter.getLoopReturn();
        }
        for (Item item : toSwitch) {
            System.out.println("Equipping: " + item.getName());
            item.click();
            Time.sleep(100, 350);
        }
        return Fighter.getLoopReturn();
    }

    @Override
    public String status() {
        return "Equipping items.";
    }

    @Override
    public void onScriptStop() {
        super.onScriptStop();
    }

    private void noAmmoSwitch() {
        HashMap<EquipmentSlot, String> map = new HashMap<>();

        switch (Beggar.randInt(0, 1)) {
            case 0:
                map.put(EquipmentSlot.MAINHAND, "Bronze sword");
                map.put(EquipmentSlot.OFFHAND, "Wooden shield");
                break;
            case 1:
                map.put(EquipmentSlot.MAINHAND, "Bronze dagger");
                map.put(EquipmentSlot.OFFHAND, "Wooden shield");
                break;
        }
        map.put(EquipmentSlot.LEGS, "Iron platelegs");
        map.put(EquipmentSlot.CHEST, "Iron platebody");
        map.put(EquipmentSlot.HEAD, "Iron full helm");
        map.put(EquipmentSlot.NECK, "Amulet of strength");
        map.put(EquipmentSlot.CAPE, "Team-17 cape");

        Progressive p = ProgressiveSet.getCurrent();
        p.setStyle(Combat.AttackStyle.ACCURATE);
        p.setSkill(Skill.ATTACK);
        p.setEquipmentMap(map);
        Equipment.unequip("Shortbow");
    }
}
