package com.amos.shared;

import java.io.Serializable;

public class SubTouchEvent implements Serializable {


    private float x;
    private float y;
    private int id;
    private int tooltyp;
    private int size;

    private float max = 1;


    SubTouchEvent(int id,float x , float y ,int tooltyp,int size)
    {

        this.id = id;
        this.x = x;
        this.y = y;
        this.tooltyp = tooltyp;

        this.size = size;
    }


    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    public int getTooltyp() {
        return tooltyp;
    }

    public float getMax() {
        return max;
    }
}
