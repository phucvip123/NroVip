/*
 * Beo Sờ tu đi ô
*/
package com.arriety.MaQuaTang;

import com.girlkun.database.GirlkunDB;
import com.girlkun.models.item.Item.ItemOption;
import com.girlkun.models.player.Player;
import com.girlkun.services.InventoryServiceNew;
import com.girlkun.services.NpcService;
import com.girlkun.services.Service;
import com.girlkun.utils.Logger;
import com.girlkun.utils.Util;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author Administrator
 */
public class MaQuaTangManager {
     public String name;
    public final ArrayList<MaQuaTang> listGiftCode = new ArrayList<>();
    private static MaQuaTangManager instance;

    public static MaQuaTangManager gI() {
        if (instance == null) {
            instance = new MaQuaTangManager();
        }
        return instance;
    }

    public void init() {
        try (Connection con = GirlkunDB.getConnection();) {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM giftcode WHERE status = 1");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                MaQuaTang giftcode = new MaQuaTang();
                giftcode.id = rs.getInt("id");
                giftcode.code = rs.getString("code");
                giftcode.countLeft = rs.getInt("count_left");
                giftcode.dateCreate = rs.getTimestamp("datecreate");
                giftcode.dateExpired = rs.getTimestamp("expired");
                giftcode.listIdPlayer = (List<Long>) JSONValue.parse(rs.getString("user_id_used"));
                giftcode.bagCount = rs.getInt("bag_count");
                giftcode.detail = rs.getString("detail");
                listGiftCode.add(giftcode);
            }
        } catch (Exception erorlog) {
            erorlog.printStackTrace();
        }
    }

    public void giftCode(Player player, String code) {
        MaQuaTang giftCode = MaQuaTangManager.gI().checkUseGiftCode((int) player.id, code);
        if (giftCode == null) {
            Service.getInstance().sendThongBao(player, "Code đã được sử dụng, hoặc không tồn tại!");
        } else if (giftCode.timeCode()) {
            Service.getInstance().sendThongBao(player, "Code đã hết hạn");
        } else if (InventoryServiceNew.gI().getCountEmptyBag(player) < giftCode.bagCount) {
            Service.getInstance().sendThongBao(player, "Hành trang không đủ chỗ trống");
        } else {
            giftCode.countLeft -= 1;
            giftCode.addPlayerUsed((int) player.id);
            InventoryServiceNew.gI().addItemGiftCodeToPlayer(player, giftCode);
        }
    }



    public MaQuaTang checkUseGiftCode(int idPlayer, String code) {
        for (MaQuaTang giftCode : listGiftCode) {
            if (giftCode.code.equals(code) && giftCode.countLeft > 0 && !giftCode.isUsedGiftCode(idPlayer)) {
                return giftCode;
            }
        }
        return null;
    }

    public void checkInfomationGiftCode(Player p) {
        StringBuilder sb = new StringBuilder();
        for (MaQuaTang giftCode : listGiftCode) {
            sb.append("Code: ").append(giftCode.code).append(", Số lượng: ").append(giftCode.countLeft).append("\b").append(", Ngày tạo: ")
                    .append(giftCode.dateCreate).append("Ngày hết hạn").append(giftCode.dateExpired);
        }

        NpcService.gI().createTutorial(p, 5073, sb.toString());
    }

    public void close() {
        try (Connection con = GirlkunDB.getConnection()) {
            Statement s = con.createStatement();
            for (MaQuaTang maQuaTang : MaQuaTangManager.gI().listGiftCode) {
                if (maQuaTang != null) {
                    String query = "UPDATE giftcode set count_left = " + maQuaTang.countLeft + ", user_id_used = \"" + maQuaTang.listIdPlayer.toString() + "\" where id = " + maQuaTang.id;
                    s.executeUpdate(query);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}