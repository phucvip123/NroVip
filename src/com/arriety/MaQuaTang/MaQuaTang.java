/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.arriety.MaQuaTang;

import com.girlkun.models.item.Item.ItemOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class MaQuaTang {
    public int id;
    String code;
    public int countLeft;
    public String detail;
    public List<Long> listIdPlayer = new ArrayList<>();
    Timestamp dateCreate;
    Timestamp dateExpired;
    public int bagCount;
    public boolean isUsedGiftCode(int idPlayer) {
        for (int i = 0; i < listIdPlayer.size(); i++) {
            if (Integer.parseInt(listIdPlayer.get(i).toString()) == (long) idPlayer) {
                return true;
            }
        }
        return false;
    }
    public void addPlayerUsed(long idPlayer) {
        listIdPlayer.add(idPlayer);
    }
    public boolean timeCode() {
        return this.dateCreate.getTime() > this.dateExpired.getTime();
    }
}