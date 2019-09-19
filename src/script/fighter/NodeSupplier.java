package script.fighter;

import org.rspeer.script.task.Task;
import script.fighter.nodes.combat.BackToFightZone;
import script.fighter.nodes.combat.FightNode;
import script.fighter.nodes.food.EatNode;
import script.fighter.nodes.food.GetFoodNode;
import script.fighter.nodes.idle.IdleNode;
import script.fighter.nodes.loot.BuryBones;
import script.fighter.nodes.loot.DepositLootNode;
import script.fighter.nodes.loot.LootNode;
import script.fighter.nodes.progressive.ProgressionChecker;

public class NodeSupplier {

    private Fighter main;

    public NodeSupplier(Fighter main) {
        this.main = main;

        EAT = new EatNode(main);
        GET_FOOD = new GetFoodNode(main);
        IDLE = new IdleNode(main);
        DEPOSIT_LOOT = new DepositLootNode(main);
        LOOT = new LootNode(main);
        PROGRESSION_CHECKER = new ProgressionChecker();
        BURY_BONES = new BuryBones(main);
        BACK_TO_FIGHT = new BackToFightZone(main);
        FIGHT = new FightNode(main);
    }

    public final Task EAT;
    public final Task GET_FOOD;
    public final Task IDLE;
    public final Task DEPOSIT_LOOT;
    public final Task LOOT;
    public final Task PROGRESSION_CHECKER;
    public final Task BURY_BONES;
    public final Task BACK_TO_FIGHT;
    public final Task FIGHT;

    public Task[] getTasks() {
        Task[] tasks = new Task[]{
                EAT,
                GET_FOOD,
                DEPOSIT_LOOT,
                LOOT,
                PROGRESSION_CHECKER,
                BURY_BONES,
                IDLE,
                FIGHT,
                BACK_TO_FIGHT
        };

        return tasks;
    }
}
