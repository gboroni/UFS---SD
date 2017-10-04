package com.chatt.ufs.model;

import java.util.ArrayList;

/**
 * Created by guilhermeboroni on 03/10/2017.
 */

public class ChatConversation {

    private ArrayList<Conversation> convList;

    private String user;

    public ChatConversation(ArrayList<Conversation> convList, String user) {
        this.convList = convList;
        this.user = user;
    }

    public ArrayList<Conversation> getConvList() {
        if (convList == null)
            convList = new ArrayList<Conversation>();
        return convList;
    }

    public void setConvList(ArrayList<Conversation> convList) {
        this.convList = convList;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
