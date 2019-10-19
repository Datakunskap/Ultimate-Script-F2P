package script.fighter;

import script.fighter.framework.Node;
import script.fighter.nodes.combat.BackToFightZone;
import script.fighter.nodes.combat.FightNode;
import script.fighter.nodes.combat.Splash;
import script.fighter.nodes.food.EatNode;
import script.fighter.nodes.food.GetFoodNode;
import script.fighter.nodes.idle.IdleNode;
import script.fighter.nodes.loot.BuryBones;
import script.fighter.nodes.loot.DepositLootNode;
import script.fighter.nodes.loot.LootNode;
import script.fighter.nodes.progressive.ProgressionChecker;
import script.fighter.nodes.restock.BuyGE;
import script.fighter.nodes.restock.SellGE;
import script.fighter.nodes.ogress.GoToCove;
import script.fighter.nodes.ogress.Ogress;

public class NodeSupplier {

    public NodeSupplier(Fighter main) {
        EAT = new EatNode(main);
        GET_FOOD = new GetFoodNode(main);
        IDLE = new IdleNode(main);
        DEPOSIT_LOOT = new DepositLootNode(main);
        LOOT = new LootNode(main);
        PROGRESSION_CHECKER = new ProgressionChecker(main);
        BURY_BONES = new BuryBones(main);
        BACK_TO_FIGHT = new BackToFightZone(main);
        FIGHT = new FightNode(main);
        BUY_GE = new BuyGE(main);
        SELL_GE = new SellGE(main);
        SPLASH = new Splash(main);
        OGRESS = new Ogress(main);
        GO_TO_COVE = new GoToCove(main);
    }

    public final Node EAT;
    public final Node GET_FOOD;
    public final Node IDLE;
    public final Node DEPOSIT_LOOT;
    public final Node LOOT;
    public final Node PROGRESSION_CHECKER;
    public final Node BURY_BONES;
    public final Node BACK_TO_FIGHT;
    public final Node FIGHT;
    public final Node BUY_GE;
    public final Node SELL_GE;
    public final Node SPLASH;
    public final Node OGRESS;
    public final Node GO_TO_COVE;

    public Node[] getTasks() {
        Node[] tasks = new Node[]{
                EAT,
                GET_FOOD,
                DEPOSIT_LOOT,
                LOOT,
                PROGRESSION_CHECKER,
                BURY_BONES,
                IDLE,
                FIGHT,
                BACK_TO_FIGHT,
                BUY_GE,
                SELL_GE,
                SPLASH,
                OGRESS,
                GO_TO_COVE
        };

        return tasks;
    }
}
