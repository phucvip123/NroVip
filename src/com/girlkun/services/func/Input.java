package com.girlkun.services.func;

import com.arriety.MaQuaTang.MaQuaTangManager;
import com.girlkun.database.GirlkunDB;
import com.girlkun.consts.ConstNpc;
import com.girlkun.jdbc.daos.PlayerDAO;
import com.girlkun.models.item.Item;
import com.girlkun.models.map.Zone;
import com.girlkun.models.npc.Npc;
import com.girlkun.models.npc.NpcManager;
import com.girlkun.models.player.Player;
import com.girlkun.network.io.Message;
import com.girlkun.network.session.ISession;
import com.girlkun.server.Client;
import com.girlkun.services.Service;
import com.girlkun.services.GiftService;
import com.girlkun.services.InventoryServiceNew;
import com.girlkun.services.ItemService;
import com.girlkun.services.NapThe;
//import com.girlkun.services.NapThe;
import com.girlkun.services.NpcService;
import com.girlkun.utils.Logger;
import com.girlkun.utils.Util;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Map;


public class Input {
    
    public static  String LOAI_THE;
     public static  String MENH_GIA;
    private static final Map<Integer, Object> PLAYER_ID_OBJECT = new HashMap<Integer, Object>();

    public static final int CHANGE_PASSWORD = 500;
    public static final int GIFT_CODE = 501;
    public static final int FIND_PLAYER = 502;
    public static final int CHANGE_NAME = 503;
    public static final int CHOOSE_LEVEL_BDKB = 504;
    public static final int NAP_THE = 505;
    public static final int CHANGE_NAME_BY_ITEM = 506;
    public static final int GIVE_IT = 507;

    public static final int QUY_DOI_COIN = 508;
    public static final int QUY_DOI_HONG_NGOC = 509;
    
    public static final int TAI = 510;
    public static final int XIU = 511;
    

    
    public static final int DOI_RUONG_DONG_VANG = 515;
    public static final int DOI_RUONG_DONG_VANG2 = 516;  
    public static final int QUY_MH= 517;
    public static final int QUY_DXL= 518;
    public static final int QUY_DXV= 519;
    
    public static final int NAP_COIN = 520;
    public static final int NAP_COIN_KEY = 521;
       
    
    public static final byte NUMERIC = 0;
    public static final byte ANY = 1;
    public static final byte PASSWORD = 2;

    private static Input intance;

    private Input() {

    }

    public static Input gI() {
        if (intance == null) {
            intance = new Input();
        }
        return intance;
    }

    public void doInput(Player player, Message msg) {
        try {
            String[] text = new String[msg.reader().readByte()];
            for (int i = 0; i < text.length; i++) {
                text[i] = msg.reader().readUTF();
            }
            switch (player.iDMark.getTypeInput()) {
                case NAP_COIN: {
                    String name = text[0];
                    int coin = Integer.valueOf(text[1]);
                    Player pl = Client.gI().getPlayer(name);
                    if (pl != null) {
                        pl.getSession().coin += coin;
                        PreparedStatement ps = null;
                        try (Connection con = GirlkunDB.getConnection();) {
                            ps = con.prepareStatement("update account set coin = (coin + ?) ,tongnap = (tongnap + ?) where id = ?");
                            ps.setInt(1, coin);
                            ps.setInt(2, coin);
                            ps.setInt(3, pl.getSession().userId);
                            ps.executeUpdate();
                        } catch (Exception e) {
                            Logger.logException(PlayerDAO.class, e, "Lỗi update coin " + pl.name);
                        } finally {
                            try {
                                ps.close();
                            } catch (SQLException ex) {
                                System.out.println("Lỗi khi update tongnap");
                            }
                        }
                        Service.getInstance().sendThongBao(player, "Đã nạp " + coin + " coin cho " + pl.name);
                    } else {
                        Service.getInstance().sendThongBao(player, "Người chơi không online");
                    }
                    break;
                }
                case NAP_COIN_KEY:
                    String Name =  text[0];
                    int coin = Integer.parseInt(text[1]);
                    if(Client.gI().getPlayer(Name) != null){
                        PlayerDAO.addvnd(Client.gI().getPlayer(Name), coin);
                        Service.getInstance().sendThongBao(player, "Đã nạp " + coin + " coin cho " + player.name);
                    }else{
                        Service.gI().sendThongBao(player, "Người chơi không tồn tại hoặc đang offline");
                    }
                    break;
                    
                case GIVE_IT:
                   String name = text[0];
                   int id = Integer.valueOf(text[1]);
                   int q = Integer.valueOf(text[2]);
                   if(Client.gI().getPlayer(name) != null){
                    Item item = ItemService.gI().createNewItem(((short)id));
                    item.quantity = q;
                    InventoryServiceNew.gI().addItemBag(Client.gI().getPlayer(name), item);
                    InventoryServiceNew.gI().sendItemBags(Client.gI().getPlayer(name));
                    Service.gI().sendThongBao(Client.gI().getPlayer(name), "Nhận " + item.template.name +" từ " + player.name);
                
                   }else{
                       Service.gI().sendThongBao(player, "Không online");
                   }
                    break;

                case CHANGE_PASSWORD:
                    Service.gI().changePassword(player, text[0], text[1], text[2]);
                    break;
                case GIFT_CODE:
                    MaQuaTangManager.gI().giftCode(player, text[0]);
                    break;
                case FIND_PLAYER:
                    Player pl = Client.gI().getPlayer(text[0]);
                    if (pl != null) {
                        NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_FIND_PLAYER, -1, "Ngài muốn..?",
                                new String[]{"Đi tới\n" + pl.name, "Gọi " + pl.name + "\ntới đây", "Đổi tên", "Ban", "Kick"},
                                pl);
                    } else {
                        Service.gI().sendThongBao(player, "Người chơi không tồn tại hoặc đang offline");
                    }
                    break;
                case CHANGE_NAME: {
                    Player plChanged = (Player) PLAYER_ID_OBJECT.get((int) player.id);
                    if (plChanged != null) {
                        if (GirlkunDB.executeQuery("select * from player where name = ?", text[0]).next()) {
                            Service.gI().sendThongBao(player, "Tên nhân vật đã tồn tại");
                        } else {
                            plChanged.name = text[0];
                            GirlkunDB.executeUpdate("update player set name = ? where id = ?", plChanged.name, plChanged.id);
                            Service.gI().player(plChanged);
                            Service.gI().Send_Caitrang(plChanged);
                            Service.gI().sendFlagBag(plChanged);
                            Zone zone = plChanged.zone;
                            ChangeMapService.gI().changeMap(plChanged, zone, plChanged.location.x, plChanged.location.y);
                            Service.gI().sendThongBao(plChanged, "Chúc mừng bạn đã có cái tên mới đẹp đẽ hơn tên ban đầu");
                            Service.gI().sendThongBao(player, "Đổi tên người chơi thành công");
                        }
                    }
                }
                break;
                
                case CHANGE_NAME_BY_ITEM: {
                    if (player != null) {
                        if (GirlkunDB.executeQuery("select * from player where name = ?", text[0]).next()) {
                            Service.gI().sendThongBao(player, "Tên nhân vật đã tồn tại");
                            createFormChangeNameByItem(player);
                        } else {
                            Item theDoiTen = InventoryServiceNew.gI().findItem(player.inventory.itemsBag, 2006);
                            if (theDoiTen == null) {
                                Service.gI().sendThongBao(player, "Không tìm thấy thẻ đổi tên");
                            }
                            else {
                                InventoryServiceNew.gI().subQuantityItemsBag(player,theDoiTen,1);
                                player.name = text[0];
                                GirlkunDB.executeUpdate("update player set name = ? where id = ?", player.name, player.id);
                                Service.gI().player(player);
                                Service.gI().Send_Caitrang(player);
                                Service.gI().sendFlagBag(player);
                                Zone zone = player.zone;
                                ChangeMapService.gI().changeMap(player, zone, player.location.x, player.location.y);
                                Service.gI().sendThongBao(player, "Chúc mừng bạn đã có cái tên mới đẹp đẽ hơn tên ban đầu");
                            }
                        }
                    }
                }
                break;
                
                
                
                case TAI:
                    if(player != null){
                    int sohntai = Integer.valueOf(text[0]);
                    if (sohntai > 50000){
                        Service.getInstance().sendThongBao(player, "Tối đa 50000 Hồng Ngọc!!");
                        return;
                    }
                    if (sohntai <= 0){
                        Service.getInstance().sendThongBao(player, "Nu Nu ai cho mày bug!!");
                        return;
                    }
                    if (InventoryServiceNew.gI().getCountEmptyBag(player) <= 1){
                        Service.getInstance().sendThongBao(player, "Ít nhất 2 ô trống trong hành trang!!");
                        return;
                    }
//                    Item tv1 = null;
//                    for (Item item : player.inventory.itemsBag) {
//                        if (item.isNotNullItem() && item.template.id == 457) {
//                            tv1 = item;
//                            break;
//                        }
//                    }
                    try {
                        if (player.inventory.ruby >= sohntai) {
//                            InventoryServiceNew.gI().subQuantityItemsBag(player, tv1, sotvtai);
                            player.inventory.ruby -= sohntai;
                            Service.gI().sendMoney(player);
                            int TimeSeconds = 10;
                            Service.getInstance().sendThongBao(player, "Chờ 10 giây để biết kết quả.");
                            while (TimeSeconds > 0) {
                                TimeSeconds--;
                                Thread.sleep(1000);
                            }
                            int x = Util.nextInt(1, 6);
                            int y = Util.nextInt(1, 6);
                            int z = Util.nextInt(1, 6);
                            int tong = (x+y+z);
                            if (4 <= (x + y + z) && (x + y + z) <= 10) {
                                if (player != null) {
                                    Service.getInstance().sendThongBaoOK(player, "Kết quả"+ "\nSố hệ thống quay ra là :" +
                                    " " + x + " " + y + " " + z + "\nTổng là : " +tong+ "\nBạn đã cược : "
                                    + sohntai + " Hồng Ngọc vào Tài"  + "\nKết quả : Xỉu" + "\nCòn cái nịt.");
                                    return;
                                }
                             } else if (x == y && x == z) {
                                if (player != null) {
                                    Service.getInstance().sendThongBaoOK(player, "Kết quả" + "Số hệ thống quay ra : " + x + " " + y + " " + z + "\nTổng là : " + tong + "\nBạn đã cược : " + sohntai + " Hồng Ngọc vào Xỉu" + "\nKết quả : Tam hoa" + "\nCòn cái nịt.");
                                    return;
                                }
                            } else if ((x + y + z) > 10) {
                                
                                if (player != null) {
//                                    Item tvthang = ItemService.gI().createNewItem((short) 457);
//                                    tvthang.quantity = (int) Math.round(sotvtai * 1.8);
//                                    InventoryServiceNew.gI().addItemBag(player, tvthang);
                                    player.inventory.ruby += sohntai*1.8;
                                    Service.gI().sendMoney(player);
                                    InventoryServiceNew.gI().sendItemBags(player);
                                    Service.getInstance().sendThongBaoOK(player, "Kết quả"+ "\nSố hệ thống quay ra : " + x + " " +
                                    y + " " + z+ "\nTổng là : " + tong + "\nBạn đã cược : " + sohntai +
                                    " Hồng Ngọc vào Tài"+ "\nKết quả : Tài"+ "\n\nVề bờ");
                                    return;
                                }
                            }
                        } else {
                            Service.getInstance().sendThongBao(player, "Bạn không đủ Hồng Ngọc để chơi.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Service.getInstance().sendThongBao(player, "Lỗi.");
                    }
                    }
                case XIU:
                    if (player != null){
                    int sohnxiu = Integer.valueOf(text[0]);
                    if (sohnxiu > 50000){
                        Service.getInstance().sendThongBao(player, "Tối đa 50000 Hồng Ngọc!!");
                        return;
                    }
                    if (sohnxiu <= 0){
                        Service.getInstance().sendThongBao(player, "Nu Nu ai cho mày bug!!");
                        return;
                    }
                    if (InventoryServiceNew.gI().getCountEmptyBag(player) <= 1){
                        Service.getInstance().sendThongBao(player, "Ít nhất 2 ô trống trong hành trang!!");
                        return;
                    }
//                    Item tv2 = null;
//                    for (Item item : player.inventory.itemsBag) {
//                        if (item.isNotNullItem() && item.template.id == 457) {
//                            tv2 = item;
//                            break;
//                        }
//                    }
                    try {
                        if (player.inventory.ruby >= sohnxiu) {
//                            InventoryServiceNew.gI().subQuantityItemsBag(player, tv2, sotvxiu);
                            player.inventory.ruby -= sohnxiu;
                            Service.gI().sendMoney(player);
                            int TimeSeconds = 10;
                            Service.getInstance().sendThongBao(player, "Chờ 10 giây để biết kết quả.");
                            while (TimeSeconds > 0) {
                                TimeSeconds--;
                                Thread.sleep(1000);
                            }
                            int x = Util.nextInt(1, 6);
                            int y = Util.nextInt(1, 6);
                            int z = Util.nextInt(1, 6);
                            int tong = (x+y+z);
                            if (4 <= (x + y + z) && (x + y + z) <= 10) {
                                if (player != null) {
//                                    Item tvthang = ItemService.gI().createNewItem((short) 457);
//                                    tvthang.quantity = (int) Math.round(sotvxiu * 1.8);
//                                    InventoryServiceNew.gI().addItemBag(player, tvthang);
                                    player.inventory.ruby += sohnxiu*1.8;
                                    Service.gI().sendMoney(player);
                                    InventoryServiceNew.gI().sendItemBags(player);
                                    Service.getInstance().sendThongBaoOK(player, "Kết quả"+ "\nSố hệ thống quay ra : " + x + " " +
                                    y + " " + z+ "\nTổng là : " + tong + "\nBạn đã cược : " + sohnxiu +
                                    " Hồng Ngọc vào Xỉu"+ "\nKết quả : Xỉu"+ "\n\nVề bờ");
                                    return;
                                }
                             } else if (x == y && x == z) {
                                if (player != null) {
                                    Service.getInstance().sendThongBaoOK(player, "Kết quả" + "Số hệ thống quay ra : " + x + " " + y + " " + z + "\nTổng là : " + tong + "\nBạn đã cược : " + sohnxiu + " Hồng Ngọc vào Xỉu" + "\nKết quả : Tam hoa" + "\nCòn cái nịt.");
                                    return;
                                }
                            } else if ((x + y + z) > 10) {
                                if (player != null) {
                                    Service.getInstance().sendThongBaoOK(player, "Kết quả"+ "\nSố hệ thống quay ra là :" +
                                    " " + x + " " + y + " " + z + "\nTổng là : " +tong+ "\nBạn đã cược : "
                                    + sohnxiu + " Hồng Ngọc vào Xỉu"  + "\nKết quả : Tài" + "\nCòn cái nịt.");
                                    return;
                                }
                            }
                        } else {
                            Service.getInstance().sendThongBao(player, "Bạn không đủ Hồng Ngọc để chơi.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Service.getInstance().sendThongBao(player, "Lỗi.");
                    }}
                    
                case CHOOSE_LEVEL_BDKB:
                    int level = Integer.parseInt(text[0]);
                    if (level >= 1 && level <= 110) {
                        Npc npc = NpcManager.getByIdAndMap(ConstNpc.QUY_LAO_KAME, player.zone.map.mapId);
                        if (npc != null) {
                            npc.createOtherMenu(player, ConstNpc.MENU_ACCEPT_GO_TO_BDKB,
                                    "Con có chắc chắn muốn tới bản đồ kho báu cấp độ " + level + "?",
                                    new String[]{"Đồng ý", "Từ chối"}, level);
                        }
                    } else {
                        Service.gI().sendThongBao(player, "Không thể thực hiện");
                    }

//                    BanDoKhoBauService.gI().openBanDoKhoBau(player, (byte) );
                    break;
                case NAP_THE:
                   
                   NapThe.SendCard(player, LOAI_THE, MENH_GIA, text[0], text[1]);
                    break;
                case DOI_RUONG_DONG_VANG:
                    int slruongcandoi = Integer.parseInt(text[0]);
                    int sldongxuvangbitru = slruongcandoi*99;
                    if (slruongcandoi > 100){
                        Service.getInstance().sendThongBao(player, "Tối đa 100 rương 1 lần!!");
                        return;
                    }
                    if (slruongcandoi <= 0){
                        Service.getInstance().sendThongBao(player, "Số Lượng không hợp lệ!!");
                        return;
                    }
                    Item dongxuvang = null;
                    for (Item item : player.inventory.itemsBag) {
                        if (item.isNotNullItem() && item.template.id == 1229) {
                            dongxuvang = item;
                            break;
                        }
                    }
                    if (dongxuvang != null && dongxuvang.quantity >= sldongxuvangbitru) {
                        InventoryServiceNew.gI().subQuantityItemsBag(player, dongxuvang, sldongxuvangbitru);    
                        Item ruongdongvang = ItemService.gI().createNewItem((short) 1230);
                        ruongdongvang.quantity = slruongcandoi;
                        InventoryServiceNew.gI().addItemBag(player, ruongdongvang);
                        InventoryServiceNew.gI().sendItemBags(player);
                        Service.getInstance().sendThongBao(player, "Chúc Mừng Bạn Đổi x" + slruongcandoi + " " + ruongdongvang.template.name + " Thành Công !");
                    } else {
                        Service.getInstance().sendThongBao(player, "Không đủ Đồng XU bạn còn thiếu " + (sldongxuvangbitru - dongxuvang.quantity) + " Đồng Xu Vàng nữa!");
                    }
                    break;
                case DOI_RUONG_DONG_VANG2:
                    int slruongcandoi2 = Integer.parseInt(text[0]);
                    int sldongxuvangbitru2 = slruongcandoi2*5000;
                    if (slruongcandoi2 > 100){
                        Service.getInstance().sendThongBao(player, "Tối đa 100 rương 1 lần!!");
                        return;
                    }
                    if (slruongcandoi2 <= 0){
                        Service.getInstance().sendThongBao(player, "Số Lượng không hợp lệ!!");
                        return;
                    }
                    Item dongxuvang2 = null;
                    for (Item item : player.inventory.itemsBag) {
                        if (item.isNotNullItem() && item.template.id == 1229) {
                            dongxuvang2 = item;
                            break;
                        }
                    }
                    if (dongxuvang2 != null && dongxuvang2.quantity >= sldongxuvangbitru2) {
                        InventoryServiceNew.gI().subQuantityItemsBag(player, dongxuvang2, sldongxuvangbitru2);    
                        Item ruongdongvang2 = ItemService.gI().createNewItem((short) 1284);
                        ruongdongvang2.quantity = slruongcandoi2;
                        InventoryServiceNew.gI().addItemBag(player, ruongdongvang2);
                        InventoryServiceNew.gI().sendItemBags(player);
                        Service.getInstance().sendThongBao(player, "Chúc Mừng Bạn Đổi x" + slruongcandoi2 + " " + ruongdongvang2.template.name + " Thành Công !");
                    } else {
                        Service.getInstance().sendThongBao(player, "Không đủ Đồng Xu bạn còn thiếu " + (sldongxuvangbitru2 - dongxuvang2.quantity) + " Đồng Xu Vàng nữa!");
                    }
                    break;
                case QUY_DOI_COIN:
                    int ratioGold = 1; // tỉ lệ đổi tv
                    int coinGold = 1; // là cái loz
                    int goldTrade = Integer.parseInt(text[0]);
                    if(goldTrade<=0 || goldTrade>= 50000000)
                    {
                       Service.gI().sendThongBao(player, "giới hạn");
                    }
                    else if(player.getSession().coin >= goldTrade*coinGold){
                        PlayerDAO.subvnd(player, goldTrade*coinGold);
                        Item thoiVang =ItemService.gI().createNewItem((short)861,goldTrade*1);// x3
                        InventoryServiceNew.gI().addItemBag(player,thoiVang);
                       InventoryServiceNew.gI().sendItemBags(player);
                        Service.gI().sendThongBao(player, "bạn nhận được " +goldTrade*ratioGold
                         +" " + thoiVang.template.name);
                    }else{
                        Service.gI().sendThongBao(player, "Số tiền của bạn là " +player.getSession().coin+ " không đủ để quy "
                                + " đổi " + goldTrade + " Hồng Ngọc " + " " + "bạn cần thêm" +(player.getSession().coin-goldTrade));
                    }
                    break;
                    case QUY_DOI_HONG_NGOC:
                    int ratioGem = 3; // tỉ lệ đổi tv
                    int coinGem = 1000; // là cái loz
                    int gemTrade = Integer.parseInt(text[0]);
                    if(gemTrade<=0 || gemTrade>= 50000000)
                    {
                       Service.gI().sendThongBao(player, "giới hạn");
                    }
                    else if(player.getSession().coin >= gemTrade*coinGem){
                        PlayerDAO.subvnd(player, gemTrade*coinGem);
                        Item thoiVang =ItemService.gI().createNewItem((short)457,gemTrade*3);// x4
                        InventoryServiceNew.gI().addItemBag(player,thoiVang);
                       InventoryServiceNew.gI().sendItemBags(player);
                        Service.gI().sendThongBao(player, "bạn nhận được " +gemTrade*ratioGem
                         +" " + thoiVang.template.name);
                    }else{
                        Service.gI().sendThongBao(player, "Số tiền của bạn là " +player.getSession().coin+ " không đủ để quy "
                                + " đổi " + gemTrade + " Thỏi Vàng" + " " + "bạn cần thêm" +(player.getSession().coin-gemTrade));
                    }
                    break;
                    case QUY_MH:
                    int ratioMH = 50; // tỉ lệ đổi tv
                    int coinMH = 1000; // là cái loz
                    int MHTrade = Integer.parseInt(text[0]);
                    if(MHTrade<=0 || MHTrade>= 50000000)
                    {
                       Service.gI().sendThongBao(player, "giới hạn");
                    }
                    else if(player.getSession().coin >= MHTrade*coinMH){
                        PlayerDAO.subvnd(player, MHTrade*coinMH);
                        Item thoiVang =ItemService.gI().createNewItem((short)935,MHTrade*200);// x4
                        InventoryServiceNew.gI().addItemBag(player,thoiVang);
                       InventoryServiceNew.gI().sendItemBags(player);
                        Service.gI().sendThongBao(player, "bạn nhận được " +MHTrade*ratioMH
                         +" " + thoiVang.template.name);
                    }else{
                        Service.gI().sendThongBao(player, "Số tiền của bạn là " +player.getSession().coin+ " không đủ để quy "
                                + " đổi " + MHTrade + " MẢNH HỒN" + " " + "bạn cần thêm" +(player.getSession().coin-MHTrade));
                    }
                     case QUY_DXV:
                    int ratioDXV = 1; // tỉ lệ đổi tv
                    int coinDXV = 1; // là cái loz
                    int DXVTrade = Integer.parseInt(text[0]);
                    if(DXVTrade<=0 || DXVTrade>= 50000000)
                    {
                       Service.gI().sendThongBao(player, "giới hạn");
                    }
                    else if(player.getSession().coin >= DXVTrade*coinDXV){
                        PlayerDAO.subvnd(player, DXVTrade*coinDXV);
                        Item thoiVang =ItemService.gI().createNewItem((short)1229,DXVTrade*1);// x4
                        InventoryServiceNew.gI().addItemBag(player,thoiVang);
                       InventoryServiceNew.gI().sendItemBags(player);
                        Service.gI().sendThongBao(player, "bạn nhận được " +DXVTrade*ratioDXV
                         +" " + thoiVang.template.name);
                    }else{
                        Service.gI().sendThongBao(player, "Số tiền của bạn là " +player.getSession().coin+ " không đủ để quy "
                                + " đổi " + DXVTrade + " MẢNH HỒN" + " " + "bạn cần thêm" +(player.getSession().coin-DXVTrade));
                    }
                    break;
                    case QUY_DXL:
                    int ratioDXL = 50; // tỉ lệ đổi tv
                    int coinDXL = 1000; // là cái loz
                    int DXLTrade = Integer.parseInt(text[0]);
                    if(DXLTrade<=0 || DXLTrade>= 50000000)
                    {
                       Service.gI().sendThongBao(player, "giới hạn");
                    }
                    else if(player.getSession().coin >= DXLTrade*coinDXL){
                        PlayerDAO.subvnd(player, DXLTrade*coinDXL);
                        Item thoiVang =ItemService.gI().createNewItem((short)934,DXLTrade*200);// x4
                        InventoryServiceNew.gI().addItemBag(player,thoiVang);
                       InventoryServiceNew.gI().sendItemBags(player);
                        Service.gI().sendThongBao(player, "bạn nhận được " +DXLTrade*ratioDXL
                         +" " + thoiVang.template.name);
                    }else{
                        Service.gI().sendThongBao(player, "Số tiền của bạn là " +player.getSession().coin+ " không đủ để quy "
                                + " đổi " + DXLTrade + " ĐÁ XANH LAM" + " " + "bạn cần thêm" +(player.getSession().coin-DXLTrade));
                    }
                    break;
                   
            
            }
                } catch (Exception e) {
        }
    }

    public void createForm(Player pl, int typeInput, String title, SubInput... subInputs) {
        pl.iDMark.setTypeInput(typeInput);
        Message msg;
        try {
            msg = new Message(-125);
            msg.writer().writeUTF(title);
            msg.writer().writeByte(subInputs.length);
            for (SubInput si : subInputs) {
                msg.writer().writeUTF(si.name);
                msg.writer().writeByte(si.typeInput);
            }
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void createForm(ISession session, int typeInput, String title, SubInput... subInputs) {
        Message msg;
        try {
            msg = new Message(-125);
            msg.writer().writeUTF(title);
            msg.writer().writeByte(subInputs.length);
            for (SubInput si : subInputs) {
                msg.writer().writeUTF(si.name);
                msg.writer().writeByte(si.typeInput);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

   public void createFormChangePassword(Player pl) {
        createForm(pl, CHANGE_PASSWORD, "Quên Mật Khẩu", new SubInput("Nhập mật khẩu đã quên", PASSWORD),
                new SubInput("Mật khẩu mới", PASSWORD),
                new SubInput("Nhập lại mật khẩu mới", PASSWORD));
    }
    
    public void createFormGiveItem(Player pl) {
        createForm(pl, GIVE_IT, "Tặng vật phẩm", new SubInput("Tên", ANY),new SubInput("Id Item", ANY),new SubInput("Số lượng", ANY));
    }

    public void createFormGiftCode(Player pl) {
        createForm(pl, GIFT_CODE, "Gift code Vô Cực", new SubInput("Gift-code", ANY));
    }

    public void createFormFindPlayer(Player pl) {
        createForm(pl, FIND_PLAYER, "Tìm kiếm người chơi", new SubInput("Tên người chơi", ANY));
    }
    public void TAI(Player pl) {
        createForm(pl, TAI, "Chọn số thỏi vàng đặt tài", new SubInput("Số thỏi vàng", ANY));
    }
    public void XIU(Player pl) {
        createForm(pl, XIU, "Chọn số thỏi vàng đặt xỉu", new SubInput("Số thỏi vàng", ANY));
    }

    public void createFormNapThe(Player pl, String loaiThe ,String menhGia) {
       LOAI_THE = loaiThe;
       MENH_GIA = menhGia;
        createForm(pl, NAP_THE, "Nạp thẻ", new SubInput("Số Seri", ANY), new SubInput("Mã thẻ", ANY));
    }
    
    public void createFormQDTV(Player pl) {
      
        createForm(pl, QUY_DOI_COIN, "Quy đổi Hồng Ngọc tỉ lệ 1-1"
                + " Chỉ x2 khi lễ, tết "
                + "\n50.000 coin = 50.000 Hồng ngọc "
                + "\nNạp tiền Tại: nrovocuc.com "
                + "\nĐăng Nhập và Chọn Nạp Coin "
                + "\nLưu Ý : Chọn Đúng Mệnh Giá nhé ! Chọn Sai là Mất ^•^ ", new SubInput("Nhập số lượng muốn đổi", NUMERIC));
    }
    
    public void createFormQDHN(Player pl) {
     
        createForm(pl, QUY_DOI_HONG_NGOC, "Quy đổi Thỏi Vàng"
                + "\nNhập 10 Có nghĩa là  10.000đ"
                + "\nTỉ Lệ Quy Đổi 10.000đ = 30 Thỏi Vàng"
                + "\nNạp tiền Tại: nrovocuc.com "
                + "\nĐăng Nhập và Chọn Nạp Coin "
                + "\nLưu Ý : Chọn Đúng Mệnh Giá nhé ! Chọn Sai là Mất ^•^ ", new SubInput("Nhập số lượng muốn đổi", NUMERIC));
    }
    public void createFormChangeName(Player pl, Player plChanged) {
        PLAYER_ID_OBJECT.put((int) pl.id, plChanged);
        createForm(pl, CHANGE_NAME, "Đổi tên " + plChanged.name, new SubInput("Tên mới", ANY));
    }
    
    public void createFormNapCoin(Player pl) {
        createForm(pl, NAP_COIN, "Nạp coin", new SubInput("Tên nhân vật", ANY), new SubInput("Số lượng", ANY));
    }
    
    public void createFormNapCoinKey(Player pl) {
        createForm(pl, NAP_COIN_KEY, "Nạp coin", new SubInput("Tên nhân vật", ANY), new SubInput("Số lượng", ANY));
    }
    
    public void createFormNapTienAdmin(Player pl, Player plChanged) {
        PLAYER_ID_OBJECT.put((int) pl.id, plChanged);
        createForm(pl, CHANGE_NAME, "Đổi tên " + plChanged.name, new SubInput("Tên mới", ANY));
    }
    
    public void createFormChangeNameByItem(Player pl) {
        createForm(pl, CHANGE_NAME_BY_ITEM, "Đổi tên " + pl.name, new SubInput("Tên mới", ANY));
    }

    public void createFormChooseLevelBDKB(Player pl) {
        createForm(pl, CHOOSE_LEVEL_BDKB, "Chọn cấp độ", new SubInput("Cấp độ (1-110)", NUMERIC));
    }
    
    public void createFormTradeRuongDongVang(Player pl) {
        createForm(pl, DOI_RUONG_DONG_VANG, "Nhập Số Lượng Muốn Đổi", new SubInput("Số Lượng", NUMERIC));
    }
      public void createFormTradeRuongDongVang2(Player pl) {
        createForm(pl, DOI_RUONG_DONG_VANG2, "Nhập Số Lượng Muốn Đổi", new SubInput("Số Lượng", NUMERIC));
    }
       public void createFormQDMH(Player pl) {
     
        createForm(pl, QUY_MH, "Quy đổi Mảnh Hồn"
                + "\nNhập 10 Có nghĩa là  10.000đ"
                + "\nTỉ Lệ Quy Đổi 10.000đ = 2000 Mảnh Hồn"
                + "\nNạp tiền Tại: nrovocuc.com "
                + "\nĐăng Nhập và Chọn Nạp Coin "
                + "\nLưu Ý : Chọn Đúng Mệnh Giá nhé ! Chọn Sai là Mất ^•^ ", new SubInput("Nhập số lượng muốn đổi", NUMERIC));
    }
       public void createFormQDDXL(Player pl) {
     
        createForm(pl, QUY_DXL, "Quy đổi Đá xanh lam"
                + "\nNhập 10 Có nghĩa là  10.000đ"
                + "\nTỉ Lệ Quy Đổi 10.000đ = 2000 Đá Xanh Lam"
                + "\nNạp tiền Tại: nrovocuc.com "
                + "\nĐăng Nhập và Chọn Nạp Coin "
                + "\nLưu Ý : Chọn Đúng Mệnh Giá nhé ! Chọn Sai là Mất ^•^ ", new SubInput("Nhập số lượng muốn đổi", NUMERIC));
    }
       public void createFormQDDXV(Player pl) {
     
        createForm(pl, QUY_DXV, "Quy đổi Đồng XU Vàng"               
                + "\nTỉ Lệ Quy Đổi 10.000đ = 10000 Đồng Xu Vàng"
                + "\nNạp tiền Tại: nrovocuc.com "
                + "\nĐăng Nhập và Chọn Nạp Coin "
                + "\nLưu Ý : Chọn Đúng Mệnh Giá nhé ! Chọn Sai là Mất ^•^ ", new SubInput("Nhập số lượng muốn đổi", NUMERIC));
    }
    public static class SubInput {

        private String name;
        private byte typeInput;

        public SubInput(String name, byte typeInput) {
            this.name = name;
            this.typeInput = typeInput;
        }
    }

}
