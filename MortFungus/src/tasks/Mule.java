import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Login;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.*;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.script.Script;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;

@ScriptMeta(developer = "Dan600", desc = "mule", name = "mule")


public class Mule extends Script implements ChatMessageListener{


    public static StopWatch stop_watch;
    private boolean trade = false;
    public static String muleName = "Null";
    public static String userTrading;
    private String user = "laurens.mergaerts@yahoo.com";
            ;
    private String pass = "WNXcrk5612";
    private int world = 0;
    private int Tradeno = 1;


    @Override
    public void onStart()
    {
        if (stop_watch == null) {
            stop_watch = StopWatch.start();
        }
    }
    @Override
    public int loop() {
        if(Trade.isOpen()){
            trade = true;
        }
        Log.info(trade);
            try {
                Getmessage();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (Dialog.isOpen()) {
                Dialog.processContinue();
            }
        if(!Game.isLoggedIn() && trade) {
            if (!Game.getClient().isLoginWorldSelectorOpen() && Game.getClient().getCurrentWorld() != world) {
                Game.getClient().loadWorlds();
                Time.sleepUntil(() -> Worlds.getLoaded().length > 0,  (1000)); // randomise movements to bank
            }
            if (Game.getClient().isLoginWorldSelectorOpen() && Game.getClient().getCurrentWorld() != world) {
                Game.getClient().setWorld(Worlds.get(world));
            }
            if (Game.getClient().getCurrentWorld() == world && Game.getClient().isLoginWorldSelectorOpen()) {
                Game.getClient().setLoginWorldSelectorOpen(false);
                Time.sleepUntil(() -> !Game.getClient().isLoginWorldSelectorOpen(),  (1000)); // randomise movements to bank
            }
            if (Game.getClient().getCurrentWorld() == world && !Game.getClient().isLoginWorldSelectorOpen()) {
                if(user != null && pass != null) {
                    Login.enterCredentials(user, pass);
                    Keyboard.pressEnter();
                    Keyboard.pressEnter();
                }
            }

        }
        if(Game.isLoggedIn() && !trade) {
            if(stop_watch.exceeds(Duration.ofSeconds(20))){
                Game.logout();
            }
        }
        if(stop_watch.exceeds(Duration.ofSeconds(120))){
            trade = false;
        }

        if(Game.isLoggedIn() && trade) {
            if (Worlds.getCurrent() != world) {
                WorldHopper.hopTo(world);
            }
            if (Inventory.getFreeSlots() < 5) {
                DoBanking();
                if (Bank.isOpen()
                ) {

                    Time.sleep(500);
                    Bank.depositAllExcept("Nature rune", "Cosmic rune", "Law rune");
                    Time.sleep(500);
                }
            }
            if (Inventory.getFreeSlots() > 4) {
                if (muleName != null) {
                    Log.info(muleName);
                }
                if (Players.getNearest(muleName) != null && !Trade.isOpen()) {
                    Players.getNearest(muleName).interact("Trade with");
                    Time.sleep(1000);
                    Time.sleepUntil(() -> Trade.isOpen(), (4000)); // randomise movements to bank
                }
                if (Tradeno <2 ) {
                    if (Trade.isOpen(false)){
                        Time.sleep(2000);
//                            if (!Trade.contains(true, "Nature rune")) {
//                                if (Inventory.contains("Nature rune")) {
//                                    Trade.offer("Nature rune", x -> x.contains("Offer-X"));
//                                    Time.sleep(500);
//                                    if (EnterInput.isOpen()) {
//                                        EnterInput.initiate(210);
//                                        Time.sleep(500);
//                                        Time.sleepUntil(() -> Trade.contains(true, "Nature rune"), Random.mid(1000, 3000)); // randomise movements to bank
//                                    }
//                                }
//                            }
//
//                            if (!Inventory.contains("Nature rune")) {
//                                Trade.accept();
//                            }
//                            if (Inventory.containsAll("Nature rune")) {
//                                if (Trade.contains(true, "Nature rune")) {
//                                    Trade.accept();
//                                }
//                            }
                        if (Trade.hasOtherAccepted()) {
                            Trade.accept();
                        }
                    }
                    if (Trade.isOpen(true)) {
                        if (Trade.hasOtherAccepted()) {
                            Trade.accept();
                            Log.info("Trade accepted");
                            muleName = null;
                            Time.sleep(1000);
//                            Game.logout();
                            Time.sleep(2000);
                        }
                    }
                }
                if (Tradeno >= 2 && Tradeno < 2) {
                    if (!Trade.contains(true, "Nature rune")) {
                        if (Inventory.contains("Nature rune")) {
                            Trade.offer("Nature rune", x -> x.contains("Offer-X"));
                            Time.sleep(500);
                            if (EnterInput.isOpen()) {
                                EnterInput.initiate(510);
                                Time.sleep(500);
                                Time.sleepUntil(() -> Trade.contains(true, "Nature rune"), Random.mid(1000, 3000)); // randomise movements to bank
                            }
                        }
                    }

                    if (!Trade.contains(true, "Cosmic rune")) {
                        if (Inventory.contains("Cosmic rune")) {
                            Trade.offer("Cosmic rune", x -> x.contains("Offer-X"));
                            Time.sleep(500);
                            if (EnterInput.isOpen()) {
                                EnterInput.initiate(280);
                                Time.sleep(500);
                                Time.sleepUntil(() -> Trade.contains(true, "Cosmic rune"), Random.mid(1000, 3000)); // randomise movements to bank
                            }
                        }
                    }
                    if (!Trade.contains(true, "law rune")) {
                        if (Inventory.contains("law rune")) {
                            Trade.offer("law rune", x -> x.contains("Offer-X"));
                            Time.sleep(500);
                            if (EnterInput.isOpen()) {
                                EnterInput.initiate(240);
                                Time.sleep(500);
                                Time.sleepUntil(() -> Trade.contains(true, "law rune"), Random.mid(1000, 3000)); // randomise movements to bank
                            }
                        }
                    }
                    if (!Inventory.contains("Nature rune") || !Inventory.contains("Law rune") || !Inventory.contains("Cosmic rune")) {
                        Trade.accept();
                    }
                    if (Inventory.containsAll("Nature rune", "Law rune", "Cosmic rune")) {
                        if (Trade.contains(true, "Nature rune") && Trade.contains(true, "Law rune") && Trade.contains(true, "Cosmic rune")) {
                            Trade.accept();
                        }
                    }
                }
                        if (Tradeno >= 2) {
                            if (!Trade.contains(true, "Nature rune")) {
                                if (Inventory.contains("Nature rune")) {
                                    Trade.offer("Nature rune", x -> x.contains("Offer-X"));
                                    Time.sleep(500);
                                    if (EnterInput.isOpen()) {
                                        EnterInput.initiate(560);
                                        Time.sleep(500);
                                        Time.sleepUntil(() -> Trade.contains(true, "Nature rune"), Random.mid(1000, 3000)); // randomise movements to bank
                                    }
                                }
                            }

                            if (!Trade.contains(true, "Cosmic rune")) {
                                if (Inventory.contains("Cosmic rune")) {
                                    Trade.offer("Cosmic rune", x -> x.contains("Offer-X"));
                                    Time.sleep(500);
                                    if (EnterInput.isOpen()) {
                                        EnterInput.initiate(260);
                                        Time.sleep(500);
                                        Time.sleepUntil(() -> Trade.contains(true, "Cosmic rune"), Random.mid(1000, 3000)); // randomise movements to bank
                                    }
                                }
                            }
                            if (!Trade.contains(true, "law rune")) {
                                if (Inventory.contains("law rune")) {
                                    Trade.offer("law rune", x -> x.contains("Offer-X"));
                                    Time.sleep(500);
                                    if (EnterInput.isOpen()) {
                                        EnterInput.initiate(250);
                                        Time.sleep(500);
                                        Time.sleepUntil(() -> Trade.contains(true, "law rune"), Random.mid(1000, 3000)); // randomise movements to bank
                                    }
                                }
                            }
                            if (!Inventory.contains("Nature rune") || !Inventory.contains("Law rune") || !Inventory.contains("Cosmic rune")) {
                                Trade.accept();
                            }
                            if (Inventory.containsAll("Nature rune", "Law rune", "Cosmic rune")) {
                                if (Trade.contains(true, "Nature rune") && Trade.contains(true, "Law rune") && Trade.contains(true, "Cosmic rune")) {
                                    Trade.accept();
                                }
                            }
                        }
                    if (Trade.isOpen(true)) {
                        if (Trade.hasOtherAccepted()) {
                            Trade.accept();
                            Log.info("Trade accepted");
                            muleName = null;
                            Time.sleep(1000);
//                            Game.logout();
                            Time.sleep(2000);
                        }
                    }
                }
            }
        return 600;
    }


    public void DoBanking(){
        if (BankLocation.getNearest().getPosition().distance() > 2) {
            Movement.walkTo(BankLocation.getNearest().getPosition());
        }
        if (BankLocation.getNearest().getPosition().distance() <= 2 && !Bank.isOpen()) {
            Npc NpcsBank = Npcs.getNearest(Npc -> (Npc.getName().equals("Banker") && Npc.containsAction("Bank")));
            if (NpcsBank != null) {
                NpcsBank.interact(s -> s.equals("Bank") || s.equals("Use"));
                Time.sleepUntil(() -> Bank.isOpen(), Random.mid(8500, 16850)); // randomise movements to bank
            }
        }
        if (BankLocation.getNearest().getPosition().distance() <= 2 && !Bank.isOpen()) {
            Log.info("Using Booth");
            SceneObject sceneObjectBank = SceneObjects.newQuery().nameContains("Bank", "Chest", "chest").actions("Use", "Bank").within(5).reachable().results().limit(1).nearest();
            if (sceneObjectBank != null) {
                sceneObjectBank.click();
                Time.sleepUntil(() -> Bank.isOpen(), Random.mid(5500, 8850));
            }
        }
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
//        InetAddress host = InetAddress.getLocalHost();
//        Socket socket = null;
//        //establish socket connection to server
//        socket = new Socket(host.getHostName(), 9876);
        ObjectOutputStream oos = null;
        Socket socket = new Socket("5.66.48.253", 9876);
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
        Log.fine("Waiting for the client request");
        //creating socket and waiting for client connection
        Socket socket = server.accept();
        //read from socket to ObjectInputStream object
        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        //convert ObjectInputStream object to String
        String message = (String) ois.readObject();
        Message = message;
        if(message.contains("Trade")) {
            stop_watch.reset();
            trade = true;
            String parts[] = message.split(":");
            world = Integer.valueOf(parts[2]);
            Tradeno = Integer.valueOf(parts[3]);
            muleName = parts[1];
            Log.fine("Message Received: " + message);
            //close resources
            ois.close();
            socket.close();
            //terminate the server if client sends exit request
            if (message.equalsIgnoreCase("exit")) {
                trade = false;
                muleName = null;
                Log.fine("Shutting down Socket server!!");
                //close the ServerSocket object
                server.close();
//                Game.logout();
            }
        }
        if (Trade.isOpen(true)) {
            if (!Trade.isOpen()) {
                trade = false;
                muleName = null;
                Log.fine("Shutting down Socket server!!");
                //close the ServerSocket object
                server.close();
                Game.logout();
            }
        }

        Thread.sleep(500);
        return Message;
    }

    public void notify(ChatMessageEvent Chatevent) {

        ChatMessageType type = Chatevent.getType();
        if (type.equals(ChatMessageType.TRADE)) {
            userTrading = Chatevent.getSource();
            // Do stuff
            Log.info(userTrading + " is Trading");

        }

    }

}

