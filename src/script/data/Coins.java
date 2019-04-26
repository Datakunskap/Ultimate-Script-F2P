package script.data;

public enum Coins {
    GP_25(25),
    GP_1000(1000),
    GP_10000(10000);

    private int gp;
    private String Sgp;

    Coins (int gp){
        this.gp = gp;
        Sgp = Integer.toString(gp);
    }

    public int getGp() {
        return gp;
    }

    public String getSgp() {
        return Sgp;
    }
}
