package com.hub.draughts;

public class Draughts {

    public static void init(String variant) {
        Square.init(variant);
        Bit.init();
        Pos.init();
    }
}
