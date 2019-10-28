package script.data;

import script.Script;

public class Lines {
//    L1("Can someone pls double my " + Script.gp.getSgp() + " gold?",
//            "I only have " + Script.gp.getSgp() + " can anyone double it pls?",
//            "I have " + Script.gp.getSgp() + " Can someone double it so I buy pick",
//            "Can Any1 Double My " + Script.gp.getSgp() + " gold pls??")
//    ;

    private String[] arr;

    public Lines (String... lines){
        arr = lines;
    }

    private String[] getArr() {
        return arr;
    }

    public String getRandLine(){
        return getArr()[Script.randInt(0, getArr().length-1)];
    }
}
