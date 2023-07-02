package com.girlkun.services.func;

import com.arriety.card.Card;
import com.arriety.card.RadarCard;
import com.arriety.card.RadarService;
import com.girlkun.consts.ConstMap;
import com.girlkun.models.item.Item;
import com.girlkun.consts.ConstNpc;
import com.girlkun.consts.ConstPlayer;
import com.girlkun.models.item.Item.ItemOption;
import com.girlkun.models.map.Zone;
import com.girlkun.models.player.Inventory;
import com.girlkun.services.NpcService;
import com.girlkun.models.player.Player;
import com.girlkun.models.skill.Skill;
import com.girlkun.network.io.Message;
import com.girlkun.utils.SkillUtil;
import com.girlkun.services.Service;
import com.girlkun.utils.Util;
import com.girlkun.server.io.MySession;
import com.girlkun.services.ItemService;
import com.girlkun.services.ItemTimeService;
import com.girlkun.services.PetService;
import com.girlkun.services.PlayerService;
import com.girlkun.services.TaskService;
import com.girlkun.services.InventoryServiceNew;
import com.girlkun.services.MapService;
import com.girlkun.services.NgocRongNamecService;
import com.girlkun.utils.Logger;

public class UseItem {

    private static final int ITEM_BOX_TO_BODY_OR_BAG = 0;
    private static final int ITEM_BAG_TO_BOX = 1;
    private static final int ITEM_BODY_TO_BOX = 3;
    private static final int ITEM_BAG_TO_BODY = 4;
    private static final int ITEM_BODY_TO_BAG = 5;
    private static final int ITEM_BAG_TO_PET_BODY = 6;
    private static final int ITEM_BODY_PET_TO_BAG = 7;
    private static int HP_BUFF = 10000;
    private static int MP_BUFF = 10000;
    private static int SD_BUFF = 2000;

    private static final byte DO_USE_ITEM = 0;
    private static final byte DO_THROW_ITEM = 1;
    private static final byte ACCEPT_THROW_ITEM = 2;
    private static final byte ACCEPT_USE_ITEM = 3;

    private static UseItem instance;

    private UseItem() {

    }

    public static UseItem gI() {
        if (instance == null) {
            instance = new UseItem();
        }
        return instance;
    }

    public void getItem(MySession session, Message msg) {
        Player player = session.player;

        TransactionService.gI().cancelTrade(player);
        try {
            int type = msg.reader().readByte();
            int index = msg.reader().readByte();
            if (index == -1) {
                return;
            }
            switch (type) {
              case ITEM_BOX_TO_BODY_OR_BAG:
                    InventoryServiceNew.gI().itemBoxToBodyOrBag(player, index);
                    TaskService.gI().checkDoneTaskGetItemBox(player);
                    break;
                case ITEM_BAG_TO_BOX:
                    InventoryServiceNew.gI().itemBagToBox(player, index);
                    break;
                case ITEM_BODY_TO_BOX:
                    InventoryServiceNew.gI().itemBodyToBox(player, index);
                    break;
                case ITEM_BAG_TO_BODY:
                    InventoryServiceNew.gI().itemBagToBody(player, index);
                    break;
                case ITEM_BODY_TO_BAG:
                    InventoryServiceNew.gI().itemBodyToBag(player, index);
                    break;
                case ITEM_BAG_TO_PET_BODY:
                    InventoryServiceNew.gI().itemBagToPetBody(player, index);
                    break;
                case ITEM_BODY_PET_TO_BAG:
                    InventoryServiceNew.gI().itemPetBodyToBag(player, index);
                    break;
            }
            player.setClothes.setup();
            if (player.pet != null) {
                player.pet.setClothes.setup();
            }
            player.setClanMember();
            Service.gI().point(player);
        } catch (Exception e) {
            Logger.logException(UseItem.class, e);

        }
    }

    public void testItem(Player player, Message _msg) {
        TransactionService.gI().cancelTrade(player);
        Message msg;
        try {
            byte type = _msg.reader().readByte();
            int where = _msg.reader().readByte();
            int index = _msg.reader().readByte();
            System.out.println("type: " + type);
            System.out.println("where: " + where);
            System.out.println("index: " + index);
        } catch (Exception e) {
            Logger.logException(UseItem.class, e);
        }
    }

    public void doItem(Player player, Message _msg) {
        TransactionService.gI().cancelTrade(player);
        Message msg;
        byte type;
        try {
            type = _msg.reader().readByte();
            int where = _msg.reader().readByte();
            int index = _msg.reader().readByte();
//            System.out.println(type + " " + where + " " + index);
            switch (type) {
                case DO_USE_ITEM:
                    if (player != null && player.inventory != null) {
                        if (index != -1) {
                            Item item = player.inventory.itemsBag.get(index);
                            if (item.isNotNullItem()) {
                                if (item.template.type == 7) {
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc chắn học " + player.inventory.itemsBag.get(index).template.name + "?");
                                    player.sendMessage(msg);
                                } else {
                                    UseItem.gI().useItem(player, item, index);
                                }
                            }
                        } else {
                            this.eatPea(player);
                        }
                    }
                    break;
                case DO_THROW_ITEM:
                    if (!(player.zone.map.mapId == 21 || player.zone.map.mapId == 22 || player.zone.map.mapId == 23)) {
                        Item item = null;
                        if (where == 0) {
                            item = player.inventory.itemsBody.get(index);
                        } else {
                            item = player.inventory.itemsBag.get(index);
                        }
                        msg = new Message(-43);
                        msg.writer().writeByte(type);
                        msg.writer().writeByte(where);
                        msg.writer().writeByte(index);
                        msg.writer().writeUTF("Bạn chắc chắn muốn vứt " + item.template.name + "?");
                        player.sendMessage(msg);
                    } else {
                        Service.gI().sendThongBao(player, "Không thể thực hiện");
                    }
                    break;
                case ACCEPT_THROW_ITEM:
                    InventoryServiceNew.gI().throwItem(player, where, index);
                    Service.gI().point(player);
                    InventoryServiceNew.gI().sendItemBags(player);
                    break;
                case ACCEPT_USE_ITEM:
                    UseItem.gI().useItem(player, player.inventory.itemsBag.get(index), index);
                    break;
            }
        } catch (Exception e) {
//            Logger.logException(UseItem.class, e);
        }
    }

    private void useItem(Player pl, Item item, int indexBag) {
        if (item.template.strRequire <= pl.nPoint.power) {
            switch (item.template.type) {
                case 21:
                    if (pl.newpet != null) {
                        ChangeMapService.gI().exitMap(pl.newpet);
                        pl.newpet.dispose();
                        pl.newpet = null;
                    }
                    InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                    PetService.Pet2(pl,item.template.head, item.template.body, item.template.leg);
                    Service.getInstance().point(pl);
                    break;
                case 7: //sách học, nâng skill
                    learnSkill(pl, item);
                    break;
                case 33:
                    UseCard(pl,item);
                    break;
                case 6: //đậu thần
                    this.eatPea(pl);
                    break;
                case 12: //ngọc rồng các loại
                    controllerCallRongThan(pl, item);
                    break;
                case 23: //thú cưỡi mới
                case 24: //thú cưỡi cũ
                    InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                    break;
                case 11: //item bag
                    InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                    Service.gI().sendFlagBag(pl);
                    break;
                case 72: {
                    InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                    Service.gI().sendPetFollow(pl, (short) (item.template.iconID - 1));
                    break;
                }
                default:
                    switch (item.template.id) {
                        case 992:
                            pl.type = 1;
                            pl.maxTime = 5;
                            Service.gI().Transport(pl);
                            break;
                        case 361:
                            if (pl.idNRNM != -1) {
                                Service.gI().sendThongBao(pl, "Không thể thực hiện");
                                return;
                            }
                            pl.idGo = (short) Util.nextInt(0, 6);
                            NpcService.gI().createMenuConMeo(pl, ConstNpc.CONFIRM_TELE_NAMEC, -1, "1 Sao (" + NgocRongNamecService.gI().getDis(pl, 0, (short) 353) + " m)\n2 Sao (" + NgocRongNamecService.gI().getDis(pl, 1, (short) 354) + " m)\n3 Sao (" + NgocRongNamecService.gI().getDis(pl, 2, (short) 355) + " m)\n4 Sao (" + NgocRongNamecService.gI().getDis(pl, 3, (short) 356) + " m)\n5 Sao (" + NgocRongNamecService.gI().getDis(pl, 4, (short) 357) + " m)\n6 Sao (" + NgocRongNamecService.gI().getDis(pl, 5, (short) 358) + " m)\n7 Sao (" + NgocRongNamecService.gI().getDis(pl, 6, (short) 359) + " m)", "Đến ngay\nViên " + (pl.idGo + 1) + " Sao\n50 ngọc", "Kết thức");
                            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                            InventoryServiceNew.gI().sendItemBags(pl);
                            break;

                        case 211: //nho tím
                        case 212: //nho xanh
                            eatGrapes(pl, item);
                            break;
                        case 1105://hop qua skh, item 2002 xd
                            UseItem.gI().Hopts(pl, item);
                            break;
                        case 342:
                        case 343:
                        case 344:
                        case 345:
                            if (pl.zone.items.stream().filter(it -> it != null && it.itemTemplate.type == 22).count() < 5) {
                                Service.gI().DropVeTinh(pl, item, pl.zone, pl.location.x, pl.location.y);
                                InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                            } else {
                                Service.gI().sendThongBao(pl, "Đặt ít thôi con");
                            }
                            break;
                        case 380: //cskb
                            openCSKB(pl, item);
                            break;
                        case 381: //cuồng nộ
                        case 382: //bổ huyết
                        case 383: //bổ khí
                        case 384: //giáp xên
                        case 385: //ẩn danh
                        case 379: //máy dò capsule
                        case 2037: //máy dò cosmos
                        case 1099:
                        case 1100:
                        case 1101:
                        case 1102:
                        case 1103:
                            useItemTime(pl, item);
                            break;
                        case 521: //tdlt
                            useTDLT(pl, item);
                            break;
                        case 454: //bông tai
                            UseItem.gI().usePorata(pl);
                            break;
                        case 1153:
                            UseItem.gI().usehpbuff(pl);
                            break;
                        case 1152:
                            UseItem.gI().usesdbuff(pl);
                            break;
                        case 1154:
                            UseItem.gI().usekibuff(pl);
                            break;
                        case 193: //gói 10 viên capsule
                            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                        case 194: //capsule đặc biệt
                            openCapsuleUI(pl);
                            break;
                        case 401: //đổi đệ tử
                            changePet(pl, item);
                            break;
                        case 1108: //đổi đệ tử
                            changePetBerus(pl, item);
                            break;
                        case 1230:
                            UseItem.gI().useruondongvang(pl);
                            break;
                             case 1259:
                            UseItem.gI().hopquat1nap(pl);
                            break;
                            case 1264:
                            UseItem.gI().hopquat2nap(pl);
                            break;
                            case 1265:
                            UseItem.gI().hopquat3nap(pl);
                            break;
                             case 1266:
                            UseItem.gI().hopquat1SM(pl);
                            break;
                            case 1267:
                            UseItem.gI().hopquat2SM(pl);
                            break;
                            case 1268:
                            UseItem.gI().hopquat3SM(pl);
                            break;
                            case 1284:
                            UseItem.gI().usehopquaruong(pl);
                            break;
                        case 722: //đổi đệ tử
                            changePetPic(pl, item);
                            break;  
                        case 402: //sách nâng chiêu 1 đệ tử
                        case 403: //sách nâng chiêu 2 đệ tử
                        case 404: //sách nâng chiêu 3 đệ tử
                        case 759: //sách nâng chiêu 4 đệ tử
                            upSkillPet(pl, item);
                            break;
                        case 921: //bông tai c2
                            UseItem.gI().usePorata2(pl);     
                            break;
                        case 1155: //bông tai c3
                            UseItem.gI().usePorata3(pl);
                            break;
                        case 1156: //bông tai c4
                            UseItem.gI().usePorata4(pl);
                            break;
                        case 2000://hop qua skh, item 2000 td
                        case 2001://hop qua skh, item 2001 nm
                        case 2002://hop qua skh, item 2002 xd
                        
                            UseItem.gI().ItemSKH(pl, item);
                            break;

                        case 2003://hop qua skh, item 2003 td
                        case 2004://hop qua skh, item 2004 nm
                        case 2005://hop qua skh, item 2005 xd
                            UseItem.gI().ItemDHD(pl, item);
                            break;
                        case 736:
                            ItemService.gI().OpenItem736(pl, item);
                            break;
                        case 457:
                            UseItem.gI().usethoivang(pl);
                            break;
                        case 987:
                            Service.gI().sendThongBao(pl, "Bảo vệ trang bị không bị rớt cấp"); //đá bảo vệ
                            break;
                        case 1120:
                            useItemHopQuaTanThu(pl, item);
                            break;
                             
                        case 2006:
                            Input.gI().createFormChangeNameByItem(pl);
                            break;
                        case 1131:
                            if (pl.pet == null) {
                                Service.gI().sendThongBao(pl, "Ngươi làm gì có đệ tử?");
                                break;
                            }

                            if(pl.pet.playerSkill.skills.get(1).skillId != -1 && pl.pet.playerSkill.skills.get(2).skillId != -1) {
                                pl.pet.openSkill2();
                                pl.pet.openSkill3();
                                InventoryServiceNew.gI().subQuantityItem(pl.inventory.itemsBag, item, 1);
                                InventoryServiceNew.gI().sendItemBags(pl);
                                Service.gI().sendThongBao(pl, "Đã đổi thành công chiêu 2 3 đệ tử");
                            } else {
                                Service.gI().sendThongBao(pl, "Ít nhất đệ tử ngươi phải có chiêu 2 chứ!");
                            }
                            break;    
                        case 2048:
                            if (pl.pet == null) {
                                Service.gI().sendThongBao(pl, "Ngươi làm gì có đệ tử?");
                                break;
                            }

                            if(pl.pet.playerSkill.skills.get(1).skillId != -1 && pl.pet.playerSkill.skills.get(2).skillId != -1 && pl.pet.playerSkill.skills.get(3).skillId != -1) {
                                pl.pet.openSkill3();
                                pl.pet.openSkill4();
                                InventoryServiceNew.gI().subQuantityItem(pl.inventory.itemsBag, item, 1);
                                InventoryServiceNew.gI().sendItemBags(pl);
                                Service.gI().sendThongBao(pl, "Đã đổi thành công chiêu 3 4 đệ tử");
                            } else {
                                Service.gI().sendThongBao(pl, "Ít nhất đệ tử ngươi phải có chiêu 3 chứ!");
                            }
                            break; 
                        case 2049:
                            if (pl.pet == null) {
                                Service.gI().sendThongBao(pl, "Ngươi làm gì có đệ tử?");
                                break;
                            }

                            if(pl.pet.playerSkill.skills.get(1).skillId != -1 && pl.pet.playerSkill.skills.get(2).skillId != -1 && pl.pet.playerSkill.skills.get(3).skillId != -1 && pl.pet.playerSkill.skills.get(4).skillId != -1) {
                                pl.pet.openSkill4();
                                pl.pet.openSkill5();
                                InventoryServiceNew.gI().subQuantityItem(pl.inventory.itemsBag, item, 1);
                                InventoryServiceNew.gI().sendItemBags(pl);
                                Service.gI().sendThongBao(pl, "Đã đổi thành công chiêu 4 5 đệ tử");
                            } else {
                                Service.gI().sendThongBao(pl, "Ít nhất đệ tử ngươi phải có chiêu 4 chứ!");
                            }
                            break;
                        case 1285: {
                            if (InventoryServiceNew.gI().getCountEmptyBag(pl) <= 0) {
                                Service.gI().sendThongBao(pl, "Hành trang không đủ chỗ trống");
                            } else {
                                InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);                                
                                pl.inventory.ruby += 1000;
                                Service.gI().sendMoney(pl);
                                InventoryServiceNew.gI().sendItemBags(pl);
                                Service.gI().sendThongBao(pl, "Chúc mừng bạn nhận được 1000 hồng ngọc! " );
                            }
                            break;
                           
                        }
                        case 1286: {
                            if (InventoryServiceNew.gI().getCountEmptyBag(pl) <= 0) {
                                Service.gI().sendThongBao(pl, "Hành trang không đủ chỗ trống");
                            } else {
                                InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);                                
                                pl.inventory.gem += 1000;
                                Service.gI().sendMoney(pl);
                                InventoryServiceNew.gI().sendItemBags(pl);
                                Service.gI().sendThongBao(pl, "Chúc mừng bạn nhận được 1000 ngọc! " );
                            }
                            break;
                           
                        }
                        case 2050:
                            if (pl.pet == null) {
                                Service.gI().sendThongBao(pl, "Ngươi làm gì có đệ tử?");
                                break;
                            }

                            if(pl.pet.playerSkill.skills.get(1).skillId != -1) {
                                pl.pet.openSkill2();
                                pl.pet.openSkill3();
                                pl.pet.openSkill4();
                                pl.pet.openSkill5();
                                InventoryServiceNew.gI().subQuantityItem(pl.inventory.itemsBag, item, 1);
                                InventoryServiceNew.gI().sendItemBags(pl);
                                Service.gI().sendThongBao(pl, "Đã đổi thành công chiêu 2 3 4 5 đệ tử");
                            } else {
                                Service.gI().sendThongBao(pl, "Ít nhất đệ tử ngươi phải có chiêu 1 chứ!");
                            }
                            break;  
                        case 2027:
                        case 2028: {
                            if (InventoryServiceNew.gI().getCountEmptyBag(pl) == 0) {
                                Service.gI().sendThongBao(pl, "Hành trang không đủ chỗ trống");
                            } else {
                                InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                                Item linhThu = ItemService.gI().createNewItem((short) Util.nextInt(2019, 2026));
                                linhThu.itemOptions.add(new Item.ItemOption(50, 10));
                                linhThu.itemOptions.add(new Item.ItemOption(77, 7));
                                linhThu.itemOptions.add(new Item.ItemOption(103, 7));
                                linhThu.itemOptions.add(new Item.ItemOption(95, 7));
                                linhThu.itemOptions.add(new Item.ItemOption(96, 7));
                                InventoryServiceNew.gI().addItemBag(pl, linhThu);
                                InventoryServiceNew.gI().sendItemBags(pl);
                                Service.gI().sendThongBao(pl, "Chúc mừng bạn nhận được Linh thú " + linhThu.template.name);
                            }
                            break;
                           
                        }
                    }
                    break;
            }
            InventoryServiceNew.gI().sendItemBags(pl);
        } else {
            Service.gI().sendThongBaoOK(pl, "Sức mạnh không đủ yêu cầu");
        }
    }
    
    public void UseCard(Player pl, Item item){
        RadarCard radarTemplate = RadarService.gI().RADAR_TEMPLATE.stream().filter(c -> c.Id == item.template.id).findFirst().orElse(null);
        if(radarTemplate == null) return;
        if (radarTemplate.Require != -1){
            RadarCard radarRequireTemplate = RadarService.gI().RADAR_TEMPLATE.stream().filter(r -> r.Id == radarTemplate.Require).findFirst().orElse(null); 
            if(radarRequireTemplate == null) return;
            Card cardRequire = pl.Cards.stream().filter(r -> r.Id == radarRequireTemplate.Id).findFirst().orElse(null);  
            if (cardRequire == null || cardRequire.Level < radarTemplate.RequireLevel){
                Service.gI().sendThongBao(pl,"Bạn cần sưu tầm "+radarRequireTemplate.Name+" ở cấp độ "+radarTemplate.RequireLevel+" mới có thể sử dụng thẻ này");
                return;
            }
        }
        Card card = pl.Cards.stream().filter(r->r.Id == item.template.id).findFirst().orElse(null);
        if (card == null){
            Card newCard = new Card(item.template.id,(byte)1,radarTemplate.Max,(byte)-1,radarTemplate.Options);
            if (pl.Cards.add(newCard)){
                RadarService.gI().RadarSetAmount(pl,newCard.Id, newCard.Amount, newCard.MaxAmount);
                RadarService.gI().RadarSetLevel(pl,newCard.Id, newCard.Level);
                InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                InventoryServiceNew.gI().sendItemBags(pl);
            }
        }else{
            if (card.Level >= 2){
                Service.gI().sendThongBao(pl,"Thẻ này đã đạt cấp tối đa");
                return;
            }
            card.Amount++;
            if (card.Amount >= card.MaxAmount){
                card.Amount = 0;
                if (card.Level == -1){
                    card.Level = 1;
                }else {
                    card.Level++;
                }
                Service.gI().point(pl);
            }
            RadarService.gI().RadarSetAmount(pl,card.Id, card.Amount, card.MaxAmount);
            RadarService.gI().RadarSetLevel(pl,card.Id, card.Level);
            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
            InventoryServiceNew.gI().sendItemBags(pl);
        }
    }

    private void useItemChangeFlagBag(Player player, Item item) {
        switch (item.template.id) {
            case 994: //vỏ ốc
                break;
            case 995: //cây kem
                break;
            case 996: //cá heo
                break;
            case 997: //con diều
                break;
            case 998: //diều rồng
                break;
            case 999: //mèo mun
                if (!player.effectFlagBag.useMeoMun) {
                    player.effectFlagBag.reset();
                    player.effectFlagBag.useMeoMun = !player.effectFlagBag.useMeoMun;
                } else {
                    player.effectFlagBag.reset();
                }
                break;
            case 1000: //xiên cá
                if (!player.effectFlagBag.useXienCa) {
                    player.effectFlagBag.reset();
                    player.effectFlagBag.useXienCa = !player.effectFlagBag.useXienCa;
                } else {
                    player.effectFlagBag.reset();
                }
                break;
            case 1001: //phóng heo
                if (!player.effectFlagBag.usePhongHeo) {
                    player.effectFlagBag.reset();
                    player.effectFlagBag.usePhongHeo = !player.effectFlagBag.usePhongHeo;
                } else {
                    player.effectFlagBag.reset();
                }
                break;
        }
        Service.gI().point(player);
        Service.gI().sendFlagBag(player);
    }

    private void changePet(Player player, Item item) {
        if (player.pet != null) {
            int gender = player.pet.gender + 1;
            if (gender > 2) {
                gender = 0;
            }
            PetService.gI().changeNormalPet(player, gender);
            InventoryServiceNew.gI().subQuantityItemsBag(player, item, 1);
        } else {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
        }
    }

    private void changePetBerus(Player player, Item item) {
        if (player.pet != null) {
            int gender = player.pet.gender;
            PetService.gI().changeBerusPet(player, gender);
            InventoryServiceNew.gI().subQuantityItemsBag(player, item, 1);
        } else {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
        }
    }
 private void changePetPic(Player player, Item item) {
        if (player.pet != null) {
            int gender = player.pet.gender;
            PetService.gI().changePicPet(player, gender);
            InventoryServiceNew.gI().subQuantityItemsBag(player, item, 1);
        } else {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
        }
    }
    private void openPhieuCaiTrangHaiTac(Player pl, Item item) {
        if (InventoryServiceNew.gI().getCountEmptyBag(pl) > 0) {
            Item ct = ItemService.gI().createNewItem((short) Util.nextInt(618, 626));
            ct.itemOptions.add(new ItemOption(147, 3));
            ct.itemOptions.add(new ItemOption(77, 3));
            ct.itemOptions.add(new ItemOption(103, 3));
            ct.itemOptions.add(new ItemOption(149, 0));
            if (item.template.id == 2006) {
                ct.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
            } else if (item.template.id == 2007) {
                ct.itemOptions.add(new ItemOption(93, Util.nextInt(7, 30)));
            }
            InventoryServiceNew.gI().addItemBag(pl, ct);
            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
            InventoryServiceNew.gI().sendItemBags(pl);
            CombineServiceNew.gI().sendEffectOpenItem(pl, item.template.iconID, ct.template.iconID);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void eatGrapes(Player pl, Item item) {
        int percentCurrentStatima = pl.nPoint.stamina * 100 / pl.nPoint.maxStamina;
        if (percentCurrentStatima > 50) {
            Service.gI().sendThongBao(pl, "Thể lực vẫn còn trên 50%");
            return;
        } else if (item.template.id == 211) {
            pl.nPoint.stamina = pl.nPoint.maxStamina;
            Service.gI().sendThongBao(pl, "Thể lực của bạn đã được hồi phục 100%");
        } else if (item.template.id == 212) {
            pl.nPoint.stamina += (pl.nPoint.maxStamina * 20 / 100);
            Service.gI().sendThongBao(pl, "Thể lực của bạn đã được hồi phục 20%");
        }
        InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
        InventoryServiceNew.gI().sendItemBags(pl);
        PlayerService.gI().sendCurrentStamina(pl);
    }

    private void openCSKB(Player pl, Item item) {
        if (InventoryServiceNew.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = {381, 190, 382, 383, 384, 385};
            int[][] gold = {{1000000, 2000000}};
            byte index = (byte) Util.nextInt(0, temp.length);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            if (index >= temp.length) {
                pl.inventory.gold += Util.nextInt(gold[0][0], gold[0][1]);
                if (pl.inventory.gold > Inventory.LIMIT_GOLD) {
                    pl.inventory.gold = Inventory.LIMIT_GOLD;
                }
                PlayerService.gI().sendInfoHpMpMoney(pl);
                icon[1] = 930;
            } else {
                Item it = ItemService.gI().createNewItem(temp[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryServiceNew.gI().addItemBag(pl, it);
                icon[1] = it.template.iconID;
            }
            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
            InventoryServiceNew.gI().sendItemBags(pl);

            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }
     private void useItemHopQuaTanThu(Player pl, Item item) {
        if (InventoryServiceNew.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = { 14, 16, 17, 18, 19, 20, 21, 22};
            int[][] gold = {{100000000, 200000000}};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            if (index <= 3) {
                pl.inventory.gold += Util.nextInt(gold[0][0], gold[0][1]);
                if (pl.inventory.gold > Inventory.LIMIT_GOLD) {
                    pl.inventory.gold = Inventory.LIMIT_GOLD;
                }
                PlayerService.gI().sendInfoHpMpMoney(pl);
                icon[1] = 930;
            } else {
                Item it = ItemService.gI().createNewItem(temp[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryServiceNew.gI().addItemBag(pl, it);
                icon[1] = it.template.iconID;
            }
            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
            InventoryServiceNew.gI().sendItemBags(pl);

            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void useItemTime(Player pl, Item item) {
        switch (item.template.id) {
            case 382: //bổ huyết
                pl.itemTime.lastTimeBoHuyet = System.currentTimeMillis();
                pl.itemTime.isUseBoHuyet = true;
                break;
            case 383: //bổ khí
                pl.itemTime.lastTimeBoKhi = System.currentTimeMillis();
                pl.itemTime.isUseBoKhi = true;
                break;
            case 384: //giáp xên
                pl.itemTime.lastTimeGiapXen = System.currentTimeMillis();
                pl.itemTime.isUseGiapXen = true;
                break;
            case 381: //cuồng nộ
                pl.itemTime.lastTimeCuongNo = System.currentTimeMillis();
                pl.itemTime.isUseCuongNo = true;
                Service.gI().point(pl);
                break;
            case 385: //ẩn danh
                pl.itemTime.lastTimeAnDanh = System.currentTimeMillis();
                pl.itemTime.isUseAnDanh = true;
                break;
            case 379: //máy dò capsule
                pl.itemTime.lastTimeUseMayDo = System.currentTimeMillis();
                pl.itemTime.isUseMayDo = true;
                break;
            case 1099:// cn
                pl.itemTime.lastTimeCuongNo2 = System.currentTimeMillis();
                pl.itemTime.isUseCuongNo2 = true;
                Service.gI().point(pl);

                break;
            case 1100:// bo huyet
                pl.itemTime.lastTimeBoHuyet2 = System.currentTimeMillis();
                pl.itemTime.isUseBoHuyet2 = true;
                break;
            case 1102://bo khi
                pl.itemTime.lastTimeBoKhi2 = System.currentTimeMillis();
                pl.itemTime.isUseBoKhi2 = true;
                break;
            case 1101://xbh
                pl.itemTime.lastTimeGiapXen2 = System.currentTimeMillis();
                pl.itemTime.isUseGiapXen2 = true;
                break;
            case 1103://an danh
                pl.itemTime.lastTimeAnDanh2 = System.currentTimeMillis();
                pl.itemTime.isUseAnDanh2 = true;
                break;
            case 663: //bánh pudding
            case 664: //xúc xíc
            case 665: //kem dâu
            case 666: //mì ly
            case 667: //sushi
                pl.itemTime.lastTimeEatMeal = System.currentTimeMillis();
                pl.itemTime.isEatMeal = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTime.iconMeal);
                pl.itemTime.iconMeal = item.template.iconID;
                break;
            case 2037: //máy dò đồ
                pl.itemTime.lastTimeUseMayDo2 = System.currentTimeMillis();
                pl.itemTime.isUseMayDo2 = true;
                break;
        }
        Service.gI().point(pl);
        ItemTimeService.gI().sendAllItemTime(pl);
        InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
        InventoryServiceNew.gI().sendItemBags(pl);
    }

    private void controllerCallRongThan(Player pl, Item item) {
        int tempId = item.template.id;
        if (tempId >= SummonDragon.NGOC_RONG_1_SAO && tempId <= SummonDragon.NGOC_RONG_7_SAO) {
            switch (tempId) {
                case SummonDragon.NGOC_RONG_1_SAO:
                case SummonDragon.NGOC_RONG_2_SAO:
                case SummonDragon.NGOC_RONG_3_SAO:
                    SummonDragon.gI().openMenuSummonShenron(pl, (byte) (tempId - 13));
                    break;
                default:
                    NpcService.gI().createMenuConMeo(pl, ConstNpc.TUTORIAL_SUMMON_DRAGON,
                            -1, "Bạn chỉ có thể gọi rồng từ ngọc 3 sao, 2 sao, 1 sao", "Hướng\ndẫn thêm\n(mới)", "OK");
                    break;
            }
        }
    }

    private void learnSkill(Player pl, Item item) {
        Message msg;
        try {
            if (item.template.gender == pl.gender || item.template.gender == 3) {
                String[] subName = item.template.name.split("");
                byte level = Byte.parseByte(subName[subName.length - 1]);
                Skill curSkill = SkillUtil.getSkillByItemID(pl, item.template.id);
                if (curSkill.point == 7) {
                    Service.gI().sendThongBao(pl, "Kỹ năng đã đạt tối đa!");
                } else {
                    if (curSkill.point == 0) {
                        if (level == 1) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.gI().messageSubCommand((byte) 23);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                        } else {
                            Skill skillNeed = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            Service.gI().sendThongBao(pl, "Vui lòng học " + skillNeed.template.name + " cấp " + skillNeed.point + " trước!");
                        }
                    } else {
                        if (curSkill.point + 1 == level) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            //System.out.println(curSkill.template.name + " - " + curSkill.point);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.gI().messageSubCommand((byte) 62);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                        } else {
                            Service.gI().sendThongBao(pl, "Vui lòng học " + curSkill.template.name + " cấp " + (curSkill.point + 1) + " trước!");
                        }
                    }
                    InventoryServiceNew.gI().sendItemBags(pl);
                }
            } else {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
            }
        } catch (Exception e) {
            Logger.logException(UseItem.class, e);
        }
    }

    private void useTDLT(Player pl, Item item) {
        if (pl.itemTime.isUseTDLT) {
            ItemTimeService.gI().turnOffTDLT(pl, item);
        } else {
            ItemTimeService.gI().turnOnTDLT(pl, item);
        }
    }

    private void usePorata(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4 || pl.fusion.typeFusion == 8 || pl.fusion.typeFusion == 10  || pl.fusion.typeFusion == 12) {
            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }
    private void usePorata2(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4 || pl.fusion.typeFusion == 6 || pl.fusion.typeFusion == 10 || pl.fusion.typeFusion == 12) {
            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion2(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }
    private void usePorata3(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4 || pl.fusion.typeFusion == 6 || pl.fusion.typeFusion == 8  || pl.fusion.typeFusion == 12) {
            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion3(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }
    private void usePorata4(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4 || pl.fusion.typeFusion == 6 || pl.fusion.typeFusion == 8  || pl.fusion.typeFusion == 10) {
            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion4(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }
    private void usehpbuff(Player pl){
        Item hpbuff = null;
        for (Item item : pl.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 1153) {
                hpbuff = item;
                break;
            }
        }
        if (hpbuff != null) {
            pl.nPoint.hpg += HP_BUFF;
            InventoryServiceNew.gI().subQuantityItemsBag(pl, hpbuff, 1);
            InventoryServiceNew.gI().sendItemBags(pl);
            Service.getInstance().sendThongBaoOK(pl, "HP Gốc của bạn đã tăng  " + HP_BUFF);
            Service.getInstance().point(pl);
        }
    }
    private void usesdbuff(Player pl){
        Item sdbuff = null;
        for (Item item : pl.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 1152) {
                sdbuff = item;
                break;
            }
        }
        if (sdbuff != null) {
            pl.nPoint.dameg += SD_BUFF;
            InventoryServiceNew.gI().subQuantityItemsBag(pl, sdbuff, 1);
            InventoryServiceNew.gI().sendItemBags(pl);
            Service.getInstance().sendThongBaoOK(pl, "SD Gốc của bạn đã tăng  " + SD_BUFF);
            Service.getInstance().point(pl);
        }
    }
    private void usekibuff(Player pl){
        Item kibuff = null;
        for (Item item : pl.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 1154) {
                kibuff = item;
                break;
            }
        }
        if (kibuff != null) {
            pl.nPoint.mpg += MP_BUFF;
            InventoryServiceNew.gI().subQuantityItemsBag(pl, kibuff, 1);
            InventoryServiceNew.gI().sendItemBags(pl);
            Service.getInstance().sendThongBaoOK(pl, "KI Gốc của bạn đã tăng  " + MP_BUFF);
            Service.getInstance().point(pl);
        }
    }
    public void usethoivang(Player player) {
        Item tv = null;
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 457) {
                tv = item;
                break;
            }
        }
        if (tv != null) {
            if(player.inventory.gold <= (Inventory.LIMIT_GOLD - 500000000)){
                InventoryServiceNew.gI().subQuantityItemsBag(player, tv, 1);
                player.inventory.gold += 500000000;
                PlayerService.gI().sendInfoHpMpMoney(player);
                InventoryServiceNew.gI().sendItemBags(player);
            } else {
                Service.getInstance().sendThongBao(player, "không được vượt quá " + Inventory.LIMIT_GOLD);
            }     
        }
    }
    public void useruondongvang(Player player) {
        try {
            if (InventoryServiceNew.gI().getCountEmptyBag(player) <= 1) {
                Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 2 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            Item ruongdongvang = null;
            for (Item item : player.inventory.itemsBag) {
                if (item.isNotNullItem() && item.template.id == 1230) {
                    ruongdongvang = item;
                    break;
                }
            }
            if (ruongdongvang != null){
            int rd = Util.nextInt(0, 100);
            int rac = 90;
            int ruby = 0;
            int tv = 5;
            int ct = 0;
            Item item = randomRac();
            if (rd <= rac){
                item = randomRac();
            } else if (rd <= rac + ruby){
                item = hongngocrdv();
            } else if (rd <= rac + ruby + tv) {
                item = thoivangrdv();
            } else if (rd <= rac + ruby + tv + ct) {
                item = caitrangrdv(true);
            }
            icon[0] = ruongdongvang.template.iconID;
            icon[1] = item.template.iconID;
            InventoryServiceNew.gI().subQuantityItemsBag(player, ruongdongvang, 1);
            InventoryServiceNew.gI().addItemBag(player, item);
            InventoryServiceNew.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineServiceNew.gI().sendEffectOpenItem(player, icon[0], icon[1]); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Item randomRac() {
        short[] racs = {20, 19, 18, 17, 16};
        Item item = ItemService.gI().createNewItem(racs[Util.nextInt(racs.length - 1)], 1);
        return item;
    }
    public Item caitrangrdv(boolean rating) {
        Item item = ItemService.gI().createNewItem((short)2043);
        item.itemOptions.add(new Item.ItemOption(76, 1));//VIP
        item.itemOptions.add(new Item.ItemOption(77, Util.nextInt(10,15)));//hp 50%
        item.itemOptions.add(new Item.ItemOption(103, Util.nextInt(10,15)));//ki 50%
        item.itemOptions.add(new Item.ItemOption(147, Util.nextInt(10,15)));//sd 50%
        item.itemOptions.add(new Item.ItemOption(101, Util.nextInt(5,10)));//smtn + 500%
        item.itemOptions.add(new Item.ItemOption(106, 0)); //k ảnh hưởng bới cái lạnh
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, Util.nextInt(9) + 1));//hsd
        }
        return item;
    }
    public Item hongngocrdv() {
        Item item = ItemService.gI().createNewItem((short)861);
        item.quantity = Util.nextInt(1,2);
        return item;
    }
    public Item thoivangrdv() {
        Item item = ItemService.gI().createNewItem((short)457);
        item.quantity = Util.nextInt(1,2);
        return item;
    }
    
    private void openCapsuleUI(Player pl) {
        pl.iDMark.setTypeChangeMap(ConstMap.CHANGE_CAPSULE);
        ChangeMapService.gI().openChangeMapTab(pl);
    }

    public void choseMapCapsule(Player pl, int index) {
        int zoneId = -1;
        Zone zoneChose = pl.mapCapsule.get(index);
        //Kiểm tra số lượng người trong khu

        if (zoneChose.getNumOfPlayers() > 25
                || MapService.gI().isMapDoanhTrai(zoneChose.map.mapId)
                || MapService.gI().isMapMaBu(zoneChose.map.mapId)
                || MapService.gI().isMapHuyDiet(zoneChose.map.mapId)
                || MapService.gI().isMapBanDoKhoBau(zoneChose.map.mapId)
                || MapService.gI().isnguhs(zoneChose.map.mapId)) {
            Service.gI().sendThongBao(pl, "Hiện tại không thể vào được khu!");
            return;
        }
        if (index != 0 || zoneChose.map.mapId == 21
                || zoneChose.map.mapId == 22
                || zoneChose.map.mapId == 23) {
            pl.mapBeforeCapsule = pl.zone;
        } else {
            zoneId = pl.mapBeforeCapsule != null ? pl.mapBeforeCapsule.zoneId : -1;
            pl.mapBeforeCapsule = null;
        }
        ChangeMapService.gI().changeMapBySpaceShip(pl, pl.mapCapsule.get(index).map.mapId, zoneId, -1);
    }

    public void eatPea(Player player) {
        Item pea = null;
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.type == 6) {
                pea = item;
                break;
            }
        }
        if (pea != null) {
            int hpKiHoiPhuc = 0;
            int lvPea = Integer.parseInt(pea.template.name.substring(13));
            for (Item.ItemOption io : pea.itemOptions) {
                if (io.optionTemplate.id == 2) {
                    hpKiHoiPhuc = io.param * 1000;
                    break;
                }
                if (io.optionTemplate.id == 48) {
                    hpKiHoiPhuc = io.param;
                    break;
                }
            }
            player.nPoint.setHp(player.nPoint.hp + hpKiHoiPhuc);
            player.nPoint.setMp(player.nPoint.mp + hpKiHoiPhuc);
            PlayerService.gI().sendInfoHpMp(player);
            Service.gI().sendInfoPlayerEatPea(player);
            if (player.pet != null && player.zone.equals(player.pet.zone) && !player.pet.isDie()) {
                int statima = 100 * lvPea;
                player.pet.nPoint.stamina += statima;
                if (player.pet.nPoint.stamina > player.pet.nPoint.maxStamina) {
                    player.pet.nPoint.stamina = player.pet.nPoint.maxStamina;
                }
                player.pet.nPoint.setHp(player.pet.nPoint.hp + hpKiHoiPhuc);
                player.pet.nPoint.setMp(player.pet.nPoint.mp + hpKiHoiPhuc);
                Service.gI().sendInfoPlayerEatPea(player.pet);
                Service.gI().chatJustForMe(player, player.pet, "Cảm ơn sư phụ đã cho con đậu thần");
            }

            InventoryServiceNew.gI().subQuantityItemsBag(player, pea, 1);
            InventoryServiceNew.gI().sendItemBags(player);
        }
    }
private void hopquat1nap (Player pl) {
        try {
            if (InventoryServiceNew.gI().getCountEmptyBag(pl) <= 4) {
                Service.getInstance().sendThongBao(pl, "Bạn phải có ít nhất 5 ô trống hành trang");
                return;
            }
            Item hopquat1nap = null;
            for (Item item : pl.inventory.itemsBag) {
                if (item.isNotNullItem() && item.template.id == 1259) {
                    hopquat1nap = item;
                    break;
                }
            }
            if (hopquat1nap != null){
            Item gang = ItemService.gI().createNewItem((short)1269);
            Item tv = ItemService.gI().createNewItem((short)457);
            Item dh = ItemService.gI().createNewItem((short)1255);
            tv.quantity = 5 ;
            dh.itemOptions.add(new ItemOption(147, Util.nextInt(35,35)));
            dh.itemOptions.add(new ItemOption(77, Util.nextInt(35,35)));
            dh.itemOptions.add(new ItemOption(103, Util.nextInt(30,30)));
            dh.itemOptions.add(new ItemOption(14,Util.nextInt(15,15)));
            dh.itemOptions.add(new ItemOption(94,Util.nextInt(15,15)));
            dh.itemOptions.add(new ItemOption(95,Util.nextInt(10,10)));
            dh.itemOptions.add(new ItemOption(96,Util.nextInt(10,10)));
            dh.itemOptions.add(new ItemOption(159,Util.nextInt(3,3)));
            dh.itemOptions.add(new ItemOption(211,0));
            dh.itemOptions.add(new ItemOption(30,0));
            gang.itemOptions.add(new ItemOption(147, Util.nextInt(40,40)));
            gang.itemOptions.add(new Item.ItemOption(77, Util.nextInt(50,50)));
            gang.itemOptions.add(new Item.ItemOption(103, Util.nextInt(50,50)));
            gang.itemOptions.add(new Item.ItemOption(101, Util.nextInt(15,15)));
            gang.itemOptions.add(new Item.ItemOption(211,0));
            gang.itemOptions.add(new Item.ItemOption(30,0));
            tv.itemOptions.add(new Item.ItemOption(211,0));
            tv.itemOptions.add(new Item.ItemOption(30,0));
            InventoryServiceNew.gI().subQuantityItemsBag(pl, hopquat1nap, 1);
            InventoryServiceNew.gI().addItemBag(pl, gang);
            InventoryServiceNew.gI().addItemBag(pl, dh);
            InventoryServiceNew.gI().addItemBag(pl, tv);
            InventoryServiceNew.gI().sendItemBags(pl);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + gang.template.name);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + tv.template.name);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + dh.template.name);
            }          
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
private void hopquat2nap (Player pl) {
        try {
            if (InventoryServiceNew.gI().getCountEmptyBag(pl) <= 4) {
                Service.getInstance().sendThongBao(pl, "Bạn phải có ít nhất 5 ô trống hành trang");
                return;
            }
            Item hopquat2nap = null;
            for (Item item : pl.inventory.itemsBag) {
                if (item.isNotNullItem() && item.template.id == 1264) {
                    hopquat2nap = item;
                    break;
                }
            }
            if (hopquat2nap != null){
            Item gang = ItemService.gI().createNewItem((short)1251);
            Item tv = ItemService.gI().createNewItem((short)457);
            tv.quantity = 5 ;
            gang.itemOptions.add(new ItemOption(147, Util.nextInt(45,45)));
            gang.itemOptions.add(new Item.ItemOption(77, Util.nextInt(45,45)));
            gang.itemOptions.add(new Item.ItemOption(103, Util.nextInt(45,45)));
            gang.itemOptions.add(new Item.ItemOption(101, Util.nextInt(12,12)));
            gang.itemOptions.add(new Item.ItemOption(211,0));
            gang.itemOptions.add(new Item.ItemOption(30,0));
            tv.itemOptions.add(new Item.ItemOption(211,0));
            tv.itemOptions.add(new Item.ItemOption(30,0));
            InventoryServiceNew.gI().subQuantityItemsBag(pl, hopquat2nap, 1);
            InventoryServiceNew.gI().addItemBag(pl, gang);
            InventoryServiceNew.gI().addItemBag(pl, tv);
            InventoryServiceNew.gI().sendItemBags(pl);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + gang.template.name);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + tv.template.name);
            }          
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
private void hopquat3nap (Player pl) {
        try {
            if (InventoryServiceNew.gI().getCountEmptyBag(pl) <= 4) {
                Service.getInstance().sendThongBao(pl, "Bạn phải có ít nhất 5 ô trống hành trang");
                return;
            }
            Item hopquat3nap = null;
            for (Item item : pl.inventory.itemsBag) {
                if (item.isNotNullItem() && item.template.id == 1265) {
                    hopquat3nap = item;
                    break;
                }
            }
            if (hopquat3nap != null){
            Item gang = ItemService.gI().createNewItem((short)1251);
            Item tv = ItemService.gI().createNewItem((short)457);
            tv.quantity = 5 ;
            gang.itemOptions.add(new ItemOption(147, Util.nextInt(40,40)));
            gang.itemOptions.add(new Item.ItemOption(77, Util.nextInt(40,40)));
            gang.itemOptions.add(new Item.ItemOption(103, Util.nextInt(40,40)));
            gang.itemOptions.add(new Item.ItemOption(101, Util.nextInt(10,10)));
            gang.itemOptions.add(new Item.ItemOption(211,0));
            gang.itemOptions.add(new Item.ItemOption(30,0));
            tv.itemOptions.add(new Item.ItemOption(211,0));
            tv.itemOptions.add(new Item.ItemOption(30,0));
            InventoryServiceNew.gI().subQuantityItemsBag(pl, hopquat3nap, 1);
            InventoryServiceNew.gI().addItemBag(pl, gang);
            InventoryServiceNew.gI().addItemBag(pl, tv);
            InventoryServiceNew.gI().sendItemBags(pl);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + gang.template.name);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + tv.template.name);
            }          
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
private void hopquat1SM (Player pl) {
        try {
            if (InventoryServiceNew.gI().getCountEmptyBag(pl) <= 4) {
                Service.getInstance().sendThongBao(pl, "Bạn phải có ít nhất 5 ô trống hành trang");
                return;
            }
            Item hopquat1SM = null;
            for (Item item : pl.inventory.itemsBag) {
                if (item.isNotNullItem() && item.template.id == 1266) {
                    hopquat1SM = item;
                    break;
                }
            }
            if (hopquat1SM != null){
            Item gang = ItemService.gI().createNewItem((short)1210);
            Item DL = ItemService.gI().createNewItem((short)1249);
            Item tv = ItemService.gI().createNewItem((short)457); 
            Item hn = ItemService.gI().createNewItem((short)861);
            Item LT = ItemService.gI().createNewItem((short)1246);
            
            tv.quantity = 150 ;
            hn.quantity = 20000 ; 
            gang.itemOptions.add(new ItemOption(190,0));
            gang.itemOptions.add(new ItemOption(21,15));
            gang.itemOptions.add(new ItemOption(147, Util.nextInt(55,15)));
            gang.itemOptions.add(new Item.ItemOption(77, Util.nextInt(70,15)));
            gang.itemOptions.add(new Item.ItemOption(103, Util.nextInt(70,15)));
            gang.itemOptions.add(new Item.ItemOption(101, Util.nextInt(80,15)));
            gang.itemOptions.add(new Item.ItemOption(211,0));
            gang.itemOptions.add(new Item.ItemOption(30,0));
            DL.itemOptions.add(new ItemOption(190,0));
            DL.itemOptions.add(new ItemOption(21,15));
            DL.itemOptions.add(new ItemOption(147, Util.nextInt(55,15)));
            DL.itemOptions.add(new Item.ItemOption(77, Util.nextInt(70,15)));
            DL.itemOptions.add(new Item.ItemOption(103, Util.nextInt(70,15)));
            DL.itemOptions.add(new Item.ItemOption(101, Util.nextInt(80,15)));
            DL.itemOptions.add(new Item.ItemOption(211,0));
            DL.itemOptions.add(new Item.ItemOption(30,0));
            LT.itemOptions.add(new ItemOption(190,0));
            LT.itemOptions.add(new ItemOption(21,15));
            LT.itemOptions.add(new ItemOption(147, Util.nextInt(55,15)));
            LT.itemOptions.add(new Item.ItemOption(77, Util.nextInt(70,15)));
            LT.itemOptions.add(new Item.ItemOption(103, Util.nextInt(70,15)));
            LT.itemOptions.add(new Item.ItemOption(101, Util.nextInt(80,15)));
            LT.itemOptions.add(new Item.ItemOption(211,0));
            LT.itemOptions.add(new Item.ItemOption(30,0));
            InventoryServiceNew.gI().subQuantityItemsBag(pl, hopquat1SM, 1);
            InventoryServiceNew.gI().addItemBag(pl, gang);
            InventoryServiceNew.gI().addItemBag(pl, tv);
            InventoryServiceNew.gI().addItemBag(pl, hn);
            InventoryServiceNew.gI().addItemBag(pl, DL);
            InventoryServiceNew.gI().addItemBag(pl, LT);
            InventoryServiceNew.gI().sendItemBags(pl);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + gang.template.name);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + tv.template.name);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + hn.template.name);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + DL.template.name);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + LT.template.name);
            }          
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
private void hopquat2SM (Player pl) {
        try {
            if (InventoryServiceNew.gI().getCountEmptyBag(pl) <= 4) {
                Service.getInstance().sendThongBao(pl, "Bạn phải có ít nhất 5 ô trống hành trang");
                return;
            }
            Item hopquat2SM = null;
            for (Item item : pl.inventory.itemsBag) {
                if (item.isNotNullItem() && item.template.id == 1267) {
                    hopquat2SM = item;
                    break;
                }
            }
            if (hopquat2SM != null){
            Item gang = ItemService.gI().createNewItem((short)1209);
            Item DL = ItemService.gI().createNewItem((short)1245);
            Item tv = ItemService.gI().createNewItem((short)457); 
            Item hn = ItemService.gI().createNewItem((short)861);
            Item LT = ItemService.gI().createNewItem((short)1250);
            
            tv.quantity = 100 ;
            hn.quantity = 10000 ; 
            gang.itemOptions.add(new ItemOption(190,0));
            gang.itemOptions.add(new ItemOption(21,15));
            gang.itemOptions.add(new ItemOption(147, Util.nextInt(40,15)));
            gang.itemOptions.add(new Item.ItemOption(77, Util.nextInt(50,15)));
            gang.itemOptions.add(new Item.ItemOption(103, Util.nextInt(65,15)));
            gang.itemOptions.add(new Item.ItemOption(101, Util.nextInt(80,15)));
            gang.itemOptions.add(new Item.ItemOption(211,0));
            gang.itemOptions.add(new Item.ItemOption(30,0));
            DL.itemOptions.add(new ItemOption(190,0));
            DL.itemOptions.add(new ItemOption(21,15));
            DL.itemOptions.add(new ItemOption(147, Util.nextInt(40,15)));
            DL.itemOptions.add(new Item.ItemOption(77, Util.nextInt(60,15)));
            DL.itemOptions.add(new Item.ItemOption(103, Util.nextInt(65,15)));
            DL.itemOptions.add(new Item.ItemOption(101, Util.nextInt(80,15)));
            DL.itemOptions.add(new Item.ItemOption(211,0));
            DL.itemOptions.add(new Item.ItemOption(30,0));
            LT.itemOptions.add(new ItemOption(190,0));
            LT.itemOptions.add(new ItemOption(21,15));
            LT.itemOptions.add(new ItemOption(147, Util.nextInt(40,15)));
            LT.itemOptions.add(new Item.ItemOption(77, Util.nextInt(60,15)));
            LT.itemOptions.add(new Item.ItemOption(103, Util.nextInt(65,15)));
            LT.itemOptions.add(new Item.ItemOption(101, Util.nextInt(80,15)));
            LT.itemOptions.add(new Item.ItemOption(211,0));
            LT.itemOptions.add(new Item.ItemOption(30,0));
            InventoryServiceNew.gI().subQuantityItemsBag(pl, hopquat2SM, 1);
            InventoryServiceNew.gI().addItemBag(pl, gang);
            InventoryServiceNew.gI().addItemBag(pl, tv);
            InventoryServiceNew.gI().addItemBag(pl, hn);
            InventoryServiceNew.gI().addItemBag(pl, DL);
            InventoryServiceNew.gI().addItemBag(pl, LT);
            InventoryServiceNew.gI().sendItemBags(pl);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + gang.template.name);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + tv.template.name);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + hn.template.name);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + DL.template.name);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + LT.template.name);
            }          
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
private void hopquat3SM (Player pl) {
        try {
            if (InventoryServiceNew.gI().getCountEmptyBag(pl) <= 4) {
                Service.getInstance().sendThongBao(pl, "Bạn phải có ít nhất 5 ô trống hành trang");
                return;
            }
            Item hopquat3SM = null;
            for (Item item : pl.inventory.itemsBag) {
                if (item.isNotNullItem() && item.template.id == 1268) {
                    hopquat3SM = item;
                    break;
                }
            }
            if (hopquat3SM != null){
            Item gang = ItemService.gI().createNewItem((short)1208);
            Item DL = ItemService.gI().createNewItem((short)1244);
            Item tv = ItemService.gI().createNewItem((short)457); 
            Item hn = ItemService.gI().createNewItem((short)861);
            Item LT = ItemService.gI().createNewItem((short)1251);
            
            tv.quantity = 70 ;
            hn.quantity = 5000 ; 
            gang.itemOptions.add(new ItemOption(190,0));
            gang.itemOptions.add(new ItemOption(21,15));
            gang.itemOptions.add(new ItemOption(147, Util.nextInt(30,15)));
            gang.itemOptions.add(new Item.ItemOption(77, Util.nextInt(50,15)));
            gang.itemOptions.add(new Item.ItemOption(103, Util.nextInt(50,15)));
            gang.itemOptions.add(new Item.ItemOption(101, Util.nextInt(80,15)));
            gang.itemOptions.add(new Item.ItemOption(211,0));
            gang.itemOptions.add(new Item.ItemOption(30,0));
            DL.itemOptions.add(new ItemOption(190,0));
            DL.itemOptions.add(new ItemOption(21,15));
            DL.itemOptions.add(new ItemOption(147, Util.nextInt(30,15)));
            DL.itemOptions.add(new Item.ItemOption(77, Util.nextInt(50,15)));
            DL.itemOptions.add(new Item.ItemOption(103, Util.nextInt(50,15)));
            DL.itemOptions.add(new Item.ItemOption(101, Util.nextInt(80,15)));
            DL.itemOptions.add(new Item.ItemOption(211,0));
            DL.itemOptions.add(new Item.ItemOption(30,0));
            LT.itemOptions.add(new ItemOption(190,0));
            LT.itemOptions.add(new ItemOption(21,15));
            LT.itemOptions.add(new ItemOption(147, Util.nextInt(30,15)));
            LT.itemOptions.add(new Item.ItemOption(77, Util.nextInt(50,15)));
            LT.itemOptions.add(new Item.ItemOption(103, Util.nextInt(50,15)));
            LT.itemOptions.add(new Item.ItemOption(101, Util.nextInt(80,15)));
            LT.itemOptions.add(new Item.ItemOption(211,0));
            LT.itemOptions.add(new Item.ItemOption(30,0));
            InventoryServiceNew.gI().subQuantityItemsBag(pl, hopquat3SM, 1);
            InventoryServiceNew.gI().addItemBag(pl, gang);
            InventoryServiceNew.gI().addItemBag(pl, tv);
            InventoryServiceNew.gI().addItemBag(pl, hn);
            InventoryServiceNew.gI().addItemBag(pl, DL);
            InventoryServiceNew.gI().addItemBag(pl, LT);
            InventoryServiceNew.gI().sendItemBags(pl);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + gang.template.name);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + tv.template.name);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + hn.template.name);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + DL.template.name);
            Service.getInstance().sendThongBao(pl, "Bạn đã nhận được " + LT.template.name);
            }          
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
 public void usehopquaruong(Player player) {
        try {
            if (InventoryServiceNew.gI().getCountEmptyBag(player) <= 2) {
                Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 3 ô trống hành trang");
                return;
            }
            short[] icon = new short[2];
            Item hopquaruong = null;
            for (Item item : player.inventory.itemsBag) {
                if (item.isNotNullItem() && item.template.id == 1284) {
                    hopquaruong = item;
                    break;
                }
            }
            if (hopquaruong != null){
            int rd1 = Util.nextInt(0, 100);
            int rac1 = 60;
            int ruby1 = 0;
            int tv1 = 20;
            int ct1 = 0;
            Item item = randomRac1(true);
            if (rd1 <= rac1){
                item = randomRac1(true);
            } else if (rd1 <= rac1 + ruby1){
                item = hongngocrdv();
            } else if (rd1 <= rac1 + ruby1 + tv1) {
                item = thoivangrdv();
            } else if (rd1 <= rac1 + ruby1 + tv1 + ct1) {
                item = caitrangrdv1(true);
            }
            icon[0] = hopquaruong.template.iconID;
            icon[1] = item.template.iconID;
            InventoryServiceNew.gI().subQuantityItemsBag(player, hopquaruong, 1);
            InventoryServiceNew.gI().addItemBag(player, item);
            InventoryServiceNew.gI().sendItemBags(player);
            Service.getInstance().sendThongBao(player, "Bạn đã nhận được " + item.template.name);
            CombineServiceNew.gI().sendEffectOpenItem(player, icon[0], icon[1]); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Item randomRac1(boolean rating) {
        short[] rac1 = {2043, 1283, 1237, 1238,1233,1234,1235,1239,1240,1241,1242,1243,1244,1245,1246,1247,1248,1249,1250,1251,1252,1253,1254,1260,1262,1263,1270,1271,1272,1273,1274,1275,1276,1277,1280,1281,1282, 1269,1167,1168,1169,1149,1150,1151};
        Item item = ItemService.gI().createNewItem(rac1[Util.nextInt(rac1.length - 1)], 1);
        item.itemOptions.add(new Item.ItemOption(76, 1));//VIP
        item.itemOptions.add(new Item.ItemOption(77, Util.nextInt(10,15)));//hp 50%
        item.itemOptions.add(new Item.ItemOption(103, Util.nextInt(10,15)));//ki 50%
        item.itemOptions.add(new Item.ItemOption(147, Util.nextInt(5,15)));//sd 50%
        item.itemOptions.add(new Item.ItemOption(101, Util.nextInt(1,15)));//smtn + 500%
        item.itemOptions.add(new Item.ItemOption(106, 0)); //k ảnh hưởng bới cái lạnh
        if (Util.isTrue(995, 1000) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, Util.nextInt(20) + 1));//hsd
       }
        return item;
    }
    public Item caitrangrdv1(boolean rating) {
        Item item = ItemService.gI().createNewItem((short)1269);
        item.itemOptions.add(new Item.ItemOption(76, 1));//VIP
        item.itemOptions.add(new Item.ItemOption(77, Util.nextInt(10,15)));//hp 50%
        item.itemOptions.add(new Item.ItemOption(103, Util.nextInt(10,15)));//ki 50%
        item.itemOptions.add(new Item.ItemOption(147, Util.nextInt(1,15)));//sd 50%
        item.itemOptions.add(new Item.ItemOption(101, Util.nextInt(10,15)));//smtn + 500%
        item.itemOptions.add(new Item.ItemOption(106, 0)); //k ảnh hưởng bới cái lạnh
        if (Util.isTrue(0, 0) && rating) {// tỉ lệ ra hsd
            item.itemOptions.add(new Item.ItemOption(93, Util.nextInt(50) + 1));//hsd
        }
        return item;
    }
    public Item hongngocrdv1() {
        Item item = ItemService.gI().createNewItem((short)861);
        item.quantity = Util.nextInt(2,3);
        return item;
    }
    public Item thoivangrdv1(boolean rating) {
         short[] thoivangrdv = {16,17,18,19,20};
        Item item = ItemService.gI().createNewItem(thoivangrdv[Util.nextInt(thoivangrdv.length - 1)],30 );
       return item;
    }
    private void upSkillPet(Player pl, Item item) {
        if (pl.pet == null) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            return;
        }
        try {
            switch (item.template.id) {
                case 402: //skill 1
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 0)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cảm ơn sư phụ");
                        InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 403: //skill 2
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 1)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cảm ơn sư phụ");
                        InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 404: //skill 3
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 2)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cảm ơn sư phụ");
                        InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;
                case 759: //skill 4
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 3)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cảm ơn sư phụ");
                        InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                    break;

            }

        } catch (Exception e) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        }
    }

    private void ItemSKH(Player pl, Item item) {//hop qua skh
        NpcService.gI().createMenuConMeo(pl, item.template.id, -1, "Hãy chọn một món quà", "Áo", "Quần", "Găng", "Giày", "Rada", "Từ Chối");
    }

    private void ItemDHD(Player pl, Item item) {//hop qua do huy diet
        NpcService.gI().createMenuConMeo(pl, item.template.id, -1, "Hãy chọn một món quà", "Áo", "Quần", "Găng", "Giày", "Rada", "Từ Chối");
    }

    private void Hopts(Player pl, Item item) {//hop qua do huy diet
        NpcService.gI().createMenuConMeo(pl, item.template.id, -1, "Chọn hành tinh của mày đi", "Set trái đất", "Set namec", "Set xayda", "Từ chổi");
    }

}
