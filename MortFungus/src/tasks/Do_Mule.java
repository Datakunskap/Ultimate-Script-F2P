import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.api.component.WorldHopper;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Predicate;


public class Do_Mule extends Task {

    public static Integer MuleCount;
    public static Integer World;
    public Boolean Sold = false;
    public static Predicate<Item> Loot =  Loot -> Loot.getName().contains("Blood rune") || Loot.getName().contains("Death rune") || Loot.getName().contains("boots") || Loot.getName().contains("wand") || Loot.getName().contains("Coins") || Loot.getName().contains("Mage's book") || Loot.getName().contains("bottom") || Loot.getName().contains("top");
    public static Predicate<Item> LootNoteddeposit =Loot -> (Loot.getName().contains("Blood rune") || Loot.getName().contains("Death rune")) || (Loot.isNoted() && (Loot.getName().contains("boots") || Loot.getName().contains("wand")  || Loot.getName().contains("bottom") || Loot.getName().contains("top"))) || Loot.getName().contains("Coins");
    private String ip = "5.68.255.129";
    private Area MageArea =  Area.rectangular(3354, 3323, 3371, 3299);

    @Override
    public int execute() {
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\laure\\Desktop\\IP.txt"))) {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                ip = sCurrentLine;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(MTAShop.isOpen()){
            Movement.setWalkFlag(Players.getLocal().getPosition().translate(Random.nextInt(-1, 1), Random.nextInt(-1, 1)));
        }

        if(Worlds.getCurrent() != 525){
            if(Bank.isOpen()){
                Movement.setWalkFlag(Players.getLocal().getPosition().translate(Random.nextInt(-1, 1), Random.nextInt(-1, 1)));
            }
            WorldHopper.hopTo(525);
        }
        if(Worlds.getCurrent() == 525) {

            if (SceneObjects.getNearest(10776) != null) {
                SceneObjects.getNearest(10776).click();
                Time.sleep(2000);
            }

            if (SceneObjects.getNearest(23677) != null) {
                SceneObjects.getNearest(23677).click();
                Time.sleep(1500);
            }
            if (Players.getLocal().getFloorLevel() == 1) {
                if (SceneObjects.getNearest(16672) != null) {
                    SceneObjects.getNearest(16672).interact("Climb-down");
                }
            }
            ArrayList<String> arr = new ArrayList<String>();
            try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\laure\\Desktop\\Mules.txt"))) {
                String sCurrentLine;
                while ((sCurrentLine = br.readLine()) != null) {
                    arr.add(sCurrentLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Main.muleName = arr.get(0);

            if (GrandExchange.isOpen()) {
                Main.daxWalker.walkTo(Players.getLocal().getPosition().translate(1, 1));
            }
//        if(Main.TimeHours == 7) {

            if (BankLocation.DUEL_ARENA.getPosition().distance() > 3) {
                if(MageArea.contains(Players.getLocal())){
                    if(SceneObjects.getNearest(10721) != null){
                        SceneObjects.getNearest(10721).click();
                        Time.sleep(5000);
                    }
                }
                if (!MageArea.contains(Players.getLocal())) {
                    Main.daxWalker.walkTo(BankLocation.DUEL_ARENA.getPosition());
                    Time.sleepUntil(() -> !Players.getLocal().isMoving(), Random.mid(1300, 3850));
                }
            }
            if (BankLocation.DUEL_ARENA.getPosition().distance() <= 3) {
                if (Main.muleName != null) {
                    Log.info(Main.muleName);
                }

//                    send("Trade:" + Players.getLocal().getName() + ":" + Worlds.getCurrent());
                if (BankLocation.DUEL_ARENA.getPosition().distance() <= 3 && !Bank.isOpen() && !GrandExchange.isOpen() && !Sold && !Trade.isOpen()) {
                    SceneObject sceneObjectBank = SceneObjects.newQuery().nameContains("Bank", "Chest", "chest").actions("Use", "Bank").within(5).reachable().results().limit(1).nearest();
                    if (sceneObjectBank != null) {
                        sceneObjectBank.interact(s -> s.equals("Bank") || s.equals("Use"));
                    }
                }
                if (BankLocation.getNearest() == BankLocation.DUEL_ARENA && !GrandExchange.isOpen() && !Sold && !Trade.isOpen()) {
                    if (BankLocation.getNearest().getPosition().distance() <= 3 && !Bank.isOpen()) {
                        Npc NpcsBank = Npcs.getNearest(Npc -> (Npc.getName().equals("Banker") && Npc.containsAction("Bank")));
                        if (NpcsBank != null) {
                            NpcsBank.interact(s -> s.equals("Bank") || s.equals("Use"));
                            Time.sleepUntil(() -> Bank.isOpen(), Random.mid(8500, 16850)); // randomise movements to bank
                        }
                    }
                }

                if (Bank.isOpen() && !GrandExchange.isOpen()) {
                    send("Trade:" + Players.getLocal().getName() + ":" + Worlds.getCurrent() + ":" + Main.Tradeno);
                    Time.sleep(649, 1240);
                    Bank.depositAllExcept(LootNoteddeposit);
                    Time.sleep(649, 1240);

                    if (Bank.getWithdrawMode() != Bank.WithdrawMode.NOTE) {
                        Bank.setWithdrawMode(Bank.WithdrawMode.NOTE);
                        Time.sleep(500);
                    }
                    if (Bank.getWithdrawMode() == Bank.WithdrawMode.NOTE) {
                        if (Bank.contains(Loot)) {
                            Item Lootinbank[] = Bank.getItems(Loot);
                            for (int i = 0; i < Lootinbank.length; i++) {
                                Bank.withdrawAll(Lootinbank[i].getName());
                                Time.sleep(300);
                                if (!Bank.contains(Loot)) {
                                    break;
                                }
                            }
                        }
                    }
                    if (Inventory.contains(LootNoteddeposit) && !Bank.contains(Loot)) {
                        if (Players.getNearest(Main.muleName) != null && !Trade.isOpen()) {
                            Players.getNearest(Main.muleName).interact("Trade with");
                            Time.sleep(1000);
                            Time.sleepUntil(() -> Trade.isOpen(), Random.mid(4000, 9000)); // randomise movements to bank
                        }
                    }
                }
                if (Trade.isOpen(false)) {
                    send("Trade:" + Players.getLocal().getName() + ":" + Worlds.getCurrent() + ":" + Main.Tradeno);
                    if (!Trade.contains(true, Loot)) {
                        Item Lootininv[] = Inventory.getItems(Loot);
                        for (int i = 0; i < Lootininv.length; i++) {
                            Trade.offerAll(Lootininv[i].getName());
                            Time.sleep(500);
                        }
                        Trade.accept();
                        Time.sleep(700);
                    }
                    if (Trade.hasOtherAccepted()) {
                        Trade.accept();
                        Time.sleep(500);
                    }
                }
                if (Trade.isOpen(true)) {
                    send("Trade:" + Players.getLocal().getName() + ":" + Worlds.getCurrent() + ":" + Main.Tradeno);
                    Trade.accept();
                    Time.sleepUntil(() -> !Trade.isOpen(), Random.mid(4000, 9000));
                    if (!Trade.isOpen()) {
                        Log.info("Gold exchanged!");
                        send("Done:" + Players.getLocal().getName());
                        Main.Mule = false;
                        Main.Tradeno = Main.Tradeno + 1;
                    }
                }
            }
        }
        return 800;
    }

    /**
     * Send method
     *
     * @param message - TRADE = Activate login , DONE - Turn off login
     */
    void send(String message) {
        try {
            this.sendTradeRequest(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends message to server from client (Slave)
     *
     * @param message - TRADE = Activate login , DONE - Turn off login
     * @throws IOException
     * @throws InterruptedException
     * @throws ClassNotFoundException
     */
    public void sendTradeRequest(String message) throws IOException, InterruptedException, ClassNotFoundException {
        //get the localhost IP address, if server is running on some other IP, you need to use that
        InetAddress host = InetAddress.getLocalHost();
        Socket socket = null;
        ObjectOutputStream oos = null;
        //establish socket connection to server
        socket = new Socket(host, 9876);
        //write to socket using ObjectOutputStream
        oos = new ObjectOutputStream(socket.getOutputStream());
        Log.fine("Sending request to Socket Server");
        oos.writeObject(message);
        //read the server response message
        //close resources
        oos.close();
        Thread.sleep(500);
    }

    public String Getmessage() throws IOException, InterruptedException, ClassNotFoundException {
        //create the socket server object
        ServerSocket server = new ServerSocket(9876);
        String Message;
        //keep listens indefinitely until receives 'exit' call or program terminates
        while(true){
            Log.fine("Waiting for the client request");
            //creating socket and waiting for client connection
            Socket socket = server.accept();
            //read from socket to ObjectInputStream object
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            //convert ObjectInputStream object to String
            String message = (String) ois.readObject();
            Message = message;
            Log.fine("Message Received: " + message);
            //close resources
            ois.close();
            socket.close();
            //terminate the server if client sends exit request
            if(message.equalsIgnoreCase("exit")) break;
        }
        Log.fine("Shutting down Socket server!!");
        //close the ServerSocket object
        server.close();
        Thread.sleep(500);
        return Message;
    }


    public boolean validate() {
        return (Main.Mule);
    }

}