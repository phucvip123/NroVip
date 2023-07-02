package com.girlkun.services;
import com.girlkun.database.GirlkunDB;
import com.girlkun.jdbc.daos.PlayerDAO;
import com.girlkun.models.item.Item;

import com.girlkun.models.player.NPoint;
import com.girlkun.models.player.Pet;
import com.girlkun.models.player.Player;
import com.girlkun.server.Client;
import com.girlkun.utils.Util;
import java.awt.Point;


public class OpenPowerService {

    public static final int COST_SPEED_OPEN_LIMIT_POWER = 500000000;

    private static OpenPowerService i;

    private OpenPowerService() {

    }

    public static OpenPowerService gI() {
        if (i == null) {
            i = new OpenPowerService();
        }
        return i;
    }

    public boolean openPowerBasic(Player player) {
        byte curLimit = player.nPoint.limitPower;
        if (curLimit < NPoint.MAX_LIMIT) {
            if (!player.itemTime.isOpenPower && player.nPoint.canOpenPower()) {
                player.itemTime.isOpenPower = true;
                player.itemTime.lastTimeOpenPower = System.currentTimeMillis();
                ItemTimeService.gI().sendAllItemTime(player);
                return true;
            } else {
                Service.gI().sendThongBao(player, "Sức mạnh của bạn không đủ để thực hiện");
                return false;
            }
        } else {
            Service.gI().sendThongBao(player, "Sức mạnh của bạn đã đạt tới mức tối đa");
            return false;
        }
    }
    
    public boolean chuyenSinh(Player player) {
        if (InventoryServiceNew.gI().getCountEmptyBag(player) <= 0)  {
            Service.gI().sendThongBao(player, "Hành trang không đủ chỗ trống");              
        
            } else {
        if (player.nPoint.power >= 1999999999999L) {
            player.nPoint.power -= (player.nPoint.power - 2000);
            player.chuyenSinh++;
            player.nPoint.hpg += 10000;
            player.nPoint.dameg += 500;
            player.nPoint.mpg += 10000;
            Service.getInstance().point(player);
            Client.gI().kickSession(player.getSession());
            
        if (player.nPoint.power < 1999999999999L) {
            }          
        if (!player.isPet) {
            Service.gI().sendThongBao(player, "Giới hạn sức mạnh của bạn đã được reset");
        } else {
            Service.gI().sendThongBao(((Pet) player).master, "Giới hạn sức mạnh của đệ tử đã được reset");
        }
            return true; 
        } else {
            if (!player.isPet) {
                Service.gI().sendThongBao(player, "Bạn phải đạt 2000tỷ sức mạnh mới có thể thực hiện!");
            } else {
                Service.gI().sendThongBao(((Pet) player).master, "Sức mạnh của đệ tử chưa đạt đủ điều kiện");
            }
            return false;
        }
        
        }
        return true;
    }
    
    public boolean openPowerSpeed(Player player) {
        if (player.nPoint.limitPower < NPoint.MAX_LIMIT) {
//            if (player.nPoint.power >= 17900000000L) {
            player.nPoint.limitPower++;
            if (player.nPoint.limitPower > NPoint.MAX_LIMIT) {
                player.nPoint.limitPower = NPoint.MAX_LIMIT;
            }
            if (!player.isPet) {
                Service.gI().sendThongBao(player, "Giới hạn sức mạnh của bạn đã được tăng lên 1 bậc");
            } else {
                Service.gI().sendThongBao(((Pet) player).master, "Giới hạn sức mạnh của đệ tử đã được tăng lên 1 bậc");
            }
            return true;
//            } else {
//                if (!player.isPet) {
//                    Service.gI().sendThongBao(player, "Sức mạnh của bạn không đủ để thực hiện");
//                } else {
//                    Service.gI().sendThongBao(((Pet) player).master, "Sức mạnh của đệ tử không đủ để thực hiện");
//                }
//                return false;
//            }
        } else {
            if (!player.isPet) {
                Service.gI().sendThongBao(player, "Sức mạnh của bạn đã đạt tới mức tối đa");
            } else {
                Service.gI().sendThongBao(((Pet) player).master, "Sức mạnh của đệ tử đã đạt tới mức tối đa");
            }
            return false;
        }
    }

}
