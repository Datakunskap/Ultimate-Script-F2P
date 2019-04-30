package script.data;

import java.util.ArrayList;
import java.util.List;

public enum Coins {
    GP_25(25),
    GP_50(50),
    GP_100(100),
    GP_500(500),
    GP_1000(1000),
    GP_2500(2500),
    GP_5000(5000),
    GP_10000(10000);

    private int gp;
    private String Sgpk;
    private String Sgp;

//    private static final ArrayList GP_ARR;
//
//    static {
//        GP_ARR = new ArrayList();
//        for (Coins gps : Coins.values()){
//            GP_ARR.add(gps);
//        }
//    }

    Coins (int gp){
        this.gp = gp;

        if(gp % 1000 == 0) {
            Sgp = (gp / 1000) + "k";
        } else {
            Sgp = Integer.toString(gp);
        }
    }

    public int getGp() {
        return gp;
    }

    public String getSgp(){
        return Sgp;
    }
}
