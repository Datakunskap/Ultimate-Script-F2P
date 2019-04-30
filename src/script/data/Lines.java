package script.data;

import script.Beggar;

public class Lines {
//    L1("Can someone pls double my " + Beggar.gp.getSgp() + " gold?",
//            "I only have " + Beggar.gp.getSgp() + " can anyone double it pls?",
//            "I have " + Beggar.gp.getSgp() + " Can someone double it so I buy pick",
//            "Can Any1 Double My " + Beggar.gp.getSgp() + " gold pls??")
//    ;

    private String[] arr;

    public Lines (String... lines){
        arr = lines;
    }

    public String[] getArr() {
        return arr;
    }

    public String getRandLine(){
        return getArr()[Beggar.randInt(0, getArr().length-1)];
    }
}
