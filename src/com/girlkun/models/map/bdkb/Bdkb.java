
package com.girlkun.models.map.bdkb;
import com.girlkun.models.clan.Clan;
import com.girlkun.models.map.Zone;
import com.girlkun.models.mob.Mob;
import com.girlkun.models.player.Player;
import com.girlkun.services.MapService;
import com.girlkun.services.Service;
import com.girlkun.services.func.ChangeMapService;
import com.girlkun.utils.TimeUtil;
import com.girlkun.utils.Util;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

public class Bdkb {
    public static final byte HOUR_OPEN_MAP_BDKB = 0;
    public static final byte MIN_OPEN_MAP_BDKB = 0;
    public static final byte SECOND_OPEN_MAP_BDKB = 0;


    public static final byte HOUR_CLOSE_MAP_BDKB = 24;
    public static final byte MIN_CLOSE_MAP_BDKB = 35;
    public static final byte SECOND_CLOSE_MAP_BDKB = 0;

    public static final int AVAILABLE = 7;

    private static Bdkb i;

    public static long TIME_OPEN_BDKB;
    public static long TIME_CLOSE_BDKB;

    private int day = -1;

    public static Bdkb gI() {
        if (i == null) {
            i = new Bdkb();
        }
        i.setTimeJoinBdkb();
        return i;
    }
    
    public void setTimeJoinBdkb() {
        if (i.day == -1 || i.day != TimeUtil.getCurrDay()) {
            i.day = TimeUtil.getCurrDay();
            try {
                TIME_OPEN_BDKB = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + HOUR_OPEN_MAP_BDKB + ":" + MIN_OPEN_MAP_BDKB + ":" + SECOND_OPEN_MAP_BDKB, "dd/MM/yyyy HH:mm:ss");
                TIME_CLOSE_BDKB = TimeUtil.getTime(TimeUtil.getTimeNow("dd/MM/yyyy") + " " + HOUR_CLOSE_MAP_BDKB + ":" + MIN_CLOSE_MAP_BDKB + ":" + SECOND_CLOSE_MAP_BDKB, "dd/MM/yyyy HH:mm:ss");
            } catch (Exception ignored) {
            }
        }
    }


    private void kickOutOfBdkb(Player player) {
        if (MapService.gI().isMapBanDoKhoBau(player.zone.map.mapId)) {
            Service.getInstance().sendThongBao(player, "Hết thời gian rồi, tàu vận chuyển sẽ đưa bạn về nhà");
            ChangeMapService.gI().changeMapBySpaceShip(player, player.gender + 21, -1, 250);
        }
    }

    private void ketthucbdkb(Player player) {
        player.zone.finishBdkb = true;
        List<Player> playersMap = player.zone.getPlayers();
        for (int i = playersMap.size() - 1; i >= 0; i--) {
            Player pl = playersMap.get(i);
            kickOutOfBdkb(pl);
        }
    }

    public void joinBdkb(Player player) {
        boolean changed = false;
        if (player.clan != null) {
            List<Player> players = player.zone.getPlayers();
            for (Player pl : players) {
                if (pl.clan != null && !player.equals(pl) && player.clan.equals(pl.clan) && !player.isBoss) {
                    Service.getInstance().changeFlag(player, 8);
                    changed = true;
                    break;
                }
            }
        }
        if (!changed && !player.isBoss) {
            Service.getInstance().changeFlag(player, 8);
        }
    }
    public void update(Player player) {
        try {
            long now = System.currentTimeMillis();
            if (!(now > TIME_OPEN_BDKB && now < TIME_CLOSE_BDKB) && MapService.gI().isMapBanDoKhoBau(player.zone.map.mapId) ) {
                
                    ketthucbdkb(player);
                
            }
        } catch (Exception ex) {
        }
    }

    
}
