package dev.itsLarss.model;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CardRegistry {

    private static final Map<Integer, Card> CARDS = new HashMap<>();

    static {

        // Charakter | Common, Uncommon, Rare
        registerCard(1, "Yae Miko - YM01", CardRarity.COMMON,
                "YM01", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Yae_Miko/Yae_Miko_YM01.png");
        registerCard(2, "Yae Miko - YM02", CardRarity.UNCOMMON,
                "YM02", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Yae_Miko/Yae_Miko_YM02.png");
        registerCard(3, "Yae Miko - YM03", CardRarity.COMMON,
                "YM03", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Yae_Miko/Yae_Miko_YM03.png");
        registerCard(4, "Yae Miko - YM04", CardRarity.COMMON,
                "YM04", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Yae_Miko/Yae_Miko_YM04.png");
        registerCard(5, "Yae Miko - YM05", CardRarity.COMMON,
                "YM05", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Yae_Miko/Yae_Miko_YM05.png");
        registerCard(6, "Yae Miko - YM06", CardRarity.COMMON,
                "YM06", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Yae_Miko/Yae_Miko_YM06.png");
        registerCard(7, "Yae Miko - YM07", CardRarity.COMMON,
                "YM07", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Yae_Miko/Yae_Miko_YM07.png");
        registerCard(8, "Yae Miko - YM08", CardRarity.RARE,
                "YM08", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Yae_Miko/Yae_Miko_YM08.png");
        registerCard(9, "Yae Miko - YM09", CardRarity.RARE,
                "YM09", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Yae_Miko/Yae_Miko_YM09.png");
        registerCard(10, "Yae Miko - YM10", CardRarity.RARE,
                "YM10", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/NSFW_Charakter/Yae_Miko/Yae_Miko_YM10.png");
        registerCard(11, "Yae Miko - YM11", CardRarity.RARE,
                "YM11", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/NSFW_Charakter/Yae_Miko/Yae_Miko_YM11.png");
        registerCard(12, "Yae Miko - YM12", CardRarity.RARE,
                "YM12", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/NSFW_Charakter/Yae_Miko/Yae_Miko_YM12.png");
        registerCard(13, "Yae Miko - YM13", CardRarity.RARE,
                "YM13", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/NSFW_Charakter/Yae_Miko/Yae_Miko_YM13.png");
        registerCard(14, "Yae Miko - YM14", CardRarity.RARE,
                "YM14", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/NSFW_Charakter/Yae_Miko/Yae_Miko_YM14.png");
        registerCard(15, "Yae Miko - YM15", CardRarity.RARE,
                "YM15", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/NSFW_Charakter/Yae_Miko/Yae_Miko_YM15.png");
        registerCard(16, "Yae Miko - YM16", CardRarity.RARE,
                "YM16", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/NSFW_Charakter/Yae_Miko/Yae_Miko_YM16.png");
        registerCard(17, "Yae Miko - YM17", CardRarity.RARE,
                "YM17", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/NSFW_Charakter/Yae_Miko/Yae_Miko_YM17.png");
        registerCard(18, "Yae Miko - YM18", CardRarity.RARE,
                "YM18", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/NSFW_Charakter/Yae_Miko/Yae_Miko_YM18.png");
        registerCard(19, "Yae Miko - YM19", CardRarity.RARE,
                "YM19", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/NSFW_Charakter/Yae_Miko/Yae_Miko_YM19.png");

        registerCard(101, "Mualani - MU01", CardRarity.COMMON,
                "MU01", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mualani/Mualani_MU01.png");
        registerCard(102, "Mualani - MU02", CardRarity.COMMON,
                "MU02", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mualani/Mualani_MU02.png");
        registerCard(103, "Mualani - MU03", CardRarity.UNCOMMON,
                "MU03", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mualani/Mualani_MU03.png");
        registerCard(104, "Mualani - MU04", CardRarity.UNCOMMON,
                "MU04", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mualani/Mualani_MU04.png");
        registerCard(105, "Mualani - MU05", CardRarity.COMMON,
                "MU05", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mualani/Mualani_MU05.png");
        registerCard(106, "Mualani - MU06", CardRarity.RARE,
                "MU06", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mualani/Mualani_MU06.png");
        registerCard(107, "Mualani - MU07", CardRarity.COMMON,
                "MU07", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mualani/Mualani_MU07.png");
        registerCard(108, "Mualani - MU08", CardRarity.UNCOMMON,
                "MU08", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mualani/Mualani_MU08.png");
        registerCard(109, "Mualani - MU09", CardRarity.UNCOMMON,
                "MU09", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mualani/Mualani_MU09.png");
        registerCard(110, "Mualani - MU10", CardRarity.RARE,
                "MU10", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mualani/Mualani_MU10.png");

        registerCard(201, "Ganyu - GA01", CardRarity.COMMON,
                "GA01", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Ganyu/Ganyu_GA01.png");
        registerCard(202, "Ganyu - GA02", CardRarity.UNCOMMON,
                "GA02", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Ganyu/Ganyu_GA02.png");
        registerCard(203, "Ganyu - GA03", CardRarity.COMMON,
                "GA03", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Ganyu/Ganyu_GA03.png");
        registerCard(204, "Ganyu - GA04", CardRarity.COMMON,
                "GA04", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Ganyu/Ganyu_GA04.png");
        registerCard(205, "Ganyu - GA05", CardRarity.RARE,
                "GA05", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Ganyu/Ganyu_GA05.png");
        registerCard(206, "Ganyu - GA06", CardRarity.UNCOMMON,
                "GA06", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Ganyu/Ganyu_GA06.png");
        registerCard(207, "Ganyu - GA07", CardRarity.COMMON,
                "GA07", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Ganyu/Ganyu_GA07.png");
        registerCard(208, "Ganyu - GA08", CardRarity.UNCOMMON,
                "GA08", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Ganyu/Ganyu_GA08.png");
        registerCard(209, "Ganyu - GA09", CardRarity.COMMON,
                "GA09", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Ganyu/Ganyu_GA09.png");
        registerCard(210, "Ganyu - GA10", CardRarity.RARE,
                "GA10", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Ganyu/Ganyu_GA10.png");

        registerCard(301, "Furina - FU01", CardRarity.COMMON,
                "FU01", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Furina/Furina_FU01.png");
        registerCard(302, "Furina - FU02", CardRarity.COMMON,
                "FU02", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Furina/Furina_FU02.png");
        registerCard(303, "Furina - FU03", CardRarity.UNCOMMON,
                "FU03", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Furina/Furina_FU03.png");
        registerCard(304, "Furina - FU04", CardRarity.COMMON,
                "FU04", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Furina/Furina_FU04.png");
        registerCard(305, "Furina - FU05", CardRarity.UNCOMMON,
                "FU05", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Furina/Furina_FU05.png");
        registerCard(306, "Furina - FU06", CardRarity.COMMON,
                "FU06", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Furina/Furina_FU06.png");
        registerCard(307, "Furina - FU07", CardRarity.UNCOMMON,
                "FU07", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Furina/Furina_FU07.png");
        registerCard(308, "Furina - FU08", CardRarity.RARE,
                "FU08", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Furina/Furina_FU08.png");
        registerCard(309, "Furina - FU09", CardRarity.RARE,
                "FU09", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Furina/Furina_FU09.png");
        registerCard(310, "Furina - FU10", CardRarity.UNCOMMON,
                "FU10", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Furina/Furina_FU10.png");

        registerCard(401, "Keqing - KE01", CardRarity.COMMON,
                "KE01", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Keqing/Keqing_KE01.png");
        registerCard(402, "Keqing - KE02", CardRarity.UNCOMMON,
                "KE02", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Keqing/Keqing_KE02.png");
        registerCard(403, "Keqing - KE03", CardRarity.COMMON,
                "KE03", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Keqing/Keqing_KE03.png");
        registerCard(404, "Keqing - KE04", CardRarity.RARE,
                "KE04", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Keqing/Keqing_KE04.png");
        registerCard(405, "Keqing - KE05", CardRarity.COMMON,
                "KE05", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Keqing/Keqing_KE05.png");
        registerCard(406, "Keqing - KE06", CardRarity.COMMON,
                "KE06", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Keqing/Keqing_KE06.png");
        registerCard(407, "Keqing - KE07", CardRarity.UNCOMMON,
                "KE07", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Keqing/Keqing_KE07.png");
        registerCard(408, "Keqing - KE08", CardRarity.UNCOMMON,
                "KE08", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Keqing/Keqing_KE08.png");
        registerCard(409, "Keqing - KE09", CardRarity.RARE,
                "KE09", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Keqing/Keqing_KE09.png");
        registerCard(410, "Keqing - KE10", CardRarity.UNCOMMON,
                "KE10", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Keqing/Keqing_KE10.png");
        registerCard(411, "Keqing - KE12", CardRarity.RARE,
                "KE11", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Keqing/Keqing_KE11.png");
        registerCard(412, "Keqing - KE12", CardRarity.RARE,
                "KE12", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Keqing/Keqing_KE12.png");
        registerCard(413, "Keqing - KE13", CardRarity.RARE,
                "KE13", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Keqing/Keqing_KE13.png");

        registerCard(501, "Hu Tao - HT01", CardRarity.COMMON,
                "HT01", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Hu_Tao/Hu_Tao_HT01.png");
        registerCard(502, "Hu Tao - HT02", CardRarity.COMMON,
                "HT02", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Hu_Tao/Hu_Tao_HT02.png");
        registerCard(503, "Hu Tao - HT03", CardRarity.COMMON,
                "HT03", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Hu_Tao/Hu_Tao_HT03.png");
        registerCard(504, "Hu Tao - HT04", CardRarity.UNCOMMON,
                "HT04", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Hu_Tao/Hu_Tao_HT04.png");
        registerCard(505, "Hu Tao - HT05", CardRarity.UNCOMMON,
                "HT05", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Hu_Tao/Hu_Tao_HT05.png");
        registerCard(506, "Hu Tao - HT06", CardRarity.RARE,
                "HT06", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Hu_Tao/Hu_Tao_HT06.png");
        registerCard(507, "Hu Tao - HT07", CardRarity.UNCOMMON,
                "HT07", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Hu_Tao/Hu_Tao_HT07.png");
        registerCard(508, "Hu Tao - HT08", CardRarity.UNCOMMON,
                "HT08", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Hu_Tao/Hu_Tao_HT08.png");
        registerCard(509, "Hu Tao - HT09", CardRarity.RARE,
                "HT09", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Hu_Tao/Hu_Tao_HT09.png");
        registerCard(510, "Hu Tao - HT10", CardRarity.RARE,
                "HT10", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Hu_Tao/Hu_Tao_HT10.png");

        registerCard(601, "Raiden Shogun - RS01", CardRarity.COMMON,
                "RS01", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Raiden_Shogun/Raiden_Shogun_RS01.png");
        registerCard(602, "Raiden Shogun - RS02", CardRarity.COMMON,
                "RS02", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Raiden_Shogun/Raiden_Shogun_RS02.png");
        registerCard(603, "Raiden Shogun - RS03", CardRarity.COMMON,
                "RS03", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Raiden_Shogun/Raiden_Shogun_RS03.png");
        registerCard(604, "Raiden Shogun - RS04", CardRarity.UNCOMMON,
                "RS04", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Raiden_Shogun/Raiden_Shogun_RS04.png");
        registerCard(605, "Raiden Shogun - RS05", CardRarity.UNCOMMON,
                "RS05", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Raiden_Shogun/Raiden_Shogun_RS05.png");
        registerCard(606, "Raiden Shogun - RS06", CardRarity.UNCOMMON,
                "RS06", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Raiden_Shogun/Raiden_Shogun_RS06.png");
        registerCard(607, "Raiden Shogun - RS07", CardRarity.UNCOMMON,
                "RS07", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Raiden_Shogun/Raiden_Shogun_RS07.png");
        registerCard(608, "Raiden Shogun - RS08", CardRarity.RARE,
                "RS08", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Raiden_Shogun/Raiden_Shogun_RS08.png");
        registerCard(609, "Raiden Shogun - RS09", CardRarity.RARE,
                "RS09", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Raiden_Shogun/Raiden_Shogun_RS09.png");
        registerCard(610, "Raiden Shogun - RS10", CardRarity.RARE,
                "RS10", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Raiden_Shogun/Raiden_Shogun_RS10.png");
        registerCard(611, "Raiden Shogun - RS11", CardRarity.RARE,
                "RS11", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Raiden_Shogun/Raiden_Shogun_RS11.png");

        registerCard(701, "Mona - MO01", CardRarity.COMMON,
                "MO01", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mona/Mona_MO01.png");
        registerCard(702, "Mona - MO02", CardRarity.COMMON,
                "MO02", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mona/Mona_MO02.png");
        registerCard(703, "Mona - MO03", CardRarity.COMMON,
                "MO03", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mona/Mona_MO03.png");
        registerCard(704, "Mona - MO04", CardRarity.UNCOMMON,
                "MO04", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mona/Mona_MO04.png");
        registerCard(705, "Mona - MO05", CardRarity.UNCOMMON,
                "MO05", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mona/Mona_MO05.png");
        registerCard(706, "Mona - MO06", CardRarity.RARE,
                "MO06", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mona/Mona_MO06.png");
        registerCard(707, "Mona - MO07", CardRarity.UNCOMMON,
                "MO07", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mona/Mona_MO07.png");
        registerCard(708, "Mona - MO08", CardRarity.COMMON,
                "MO08", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mona/Mona_MO08.png");
        registerCard(709, "Mona - MO09", CardRarity.RARE,
                "MO09", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mona/Mona_MO09.png");
        registerCard(710, "Mona - MO10", CardRarity.RARE,
                "MO10", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mona/Mona_MO10.png");

        registerCard(801, "Varesa - VA01", CardRarity.COMMON,
                "VA01", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Varesa/Varesa_VA01.png");
        registerCard(802, "Varesa - VA02", CardRarity.COMMON,
                "VA02", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Varesa/Varesa_VA02.png");
        registerCard(803, "Varesa - VA03", CardRarity.UNCOMMON,
                "VA03", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Varesa/Varesa_VA03.png");
        registerCard(804, "Varesa - VA04", CardRarity.COMMON,
                "VA04", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Varesa/Varesa_VA04.png");
        registerCard(805, "Varesa - VA05", CardRarity.UNCOMMON,
                "VA05", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Varesa/Varesa_VA05.png");
        registerCard(806, "Varesa - VA06", CardRarity.UNCOMMON,
                "VA06", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Varesa/Varesa_VA06.png");
        registerCard(807, "Varesa - VA07", CardRarity.RARE,
                "VA07", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Varesa/Varesa_VA07.png");
        registerCard(808, "Varesa - VA08", CardRarity.UNCOMMON,
                "VA08", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Varesa/Varesa_VA08.png");
        registerCard(809, "Varesa - VA09", CardRarity.RARE,
                "VA09", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Varesa/Varesa_VA09.png");
        registerCard(810, "Varesa - VA10", CardRarity.RARE,
                "VA10", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Varesa/Varesa_VA10.png");

        registerCard(901, "Lauma - LA01", CardRarity.UNCOMMON,
                "LA01", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Lauma/Lauma_LA01.png");
        registerCard(902, "Lauma - LA02", CardRarity.RARE,
                "LA02", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Lauma/Lauma_LA02.png");
        registerCard(903, "Lauma - LA03", CardRarity.RARE,
                "LA03", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Lauma/Lauma_LA03.png");
        registerCard(904, "Lauma - LA04", CardRarity.UNCOMMON,
                "LA04", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Lauma/Lauma_LA04.png");
        registerCard(905, "Lauma - LA05", CardRarity.UNCOMMON,
                "LA05", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Lauma/Lauma_LA05.png");
        registerCard(906, "Lauma - LA06", CardRarity.UNCOMMON,
                "LA06", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Lauma/Lauma_LA06.png");
        registerCard(907, "Lauma - LA07", CardRarity.COMMON,
                "LA07", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Lauma/Lauma_LA07.png");
        registerCard(908, "Lauma - LA08", CardRarity.COMMON,
                "LA08", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Lauma/Lauma_LA08.png");
        registerCard(909, "Lauma - LA09", CardRarity.COMMON,
                "LA09", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Lauma/Lauma_LA09.png");
        registerCard(910, "Lauma - LA10", CardRarity.RARE,
                "LA10", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Lauma/Lauma_LA10.png");


        // Groups und Landscape | Epic
        //nothing there yet

        // Special Cards | Legendary
        registerCard(100001, "Yae Miko - YMS1", CardRarity.LEGENDARY,
                "Special Card 2026 YMS1", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Yae_Miko/Yae_Miko_YMS1.png");

        registerCard(100002, "Furina - FUS1", CardRarity.LEGENDARY,
                "Special Card 2026 FUS1", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Furina/Furina_FUS1.png");

        registerCard(100003, "Mualani - MUS1", CardRarity.LEGENDARY,
                "Special Card 2026 MUS1", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mualani/Mualani_MUS1.png");
        registerCard(100004, "Mualani - MUS2", CardRarity.LEGENDARY,
                "Special Card 2026 MUS2", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mualani/Mualani_MUS2.png");

        registerCard(100005, "Ganyu - GAS1", CardRarity.LEGENDARY,
                "Special Card 2026 GAS1", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Ganyu/Ganyu_GAS1.png");

        registerCard(100006, "Keqing - KES1", CardRarity.LEGENDARY,
                "Special Card 2026 KES1", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Keqing/Keqing_KES1.png");

        registerCard(100007, "Hu Tao - HTS1", CardRarity.LEGENDARY,
                "Special Card 2026 HTS1", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Hu_Tao/Hu_Tao_HTS1.png");

        registerCard(100008, "Raiden Shogun - RSS1", CardRarity.LEGENDARY,
                "Special Card 2026 RSS1", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Raiden_Shogun/Raiden_Shogun_RSS1.png");
        registerCard(100009, "Raiden Shogun - RSS2", CardRarity.LEGENDARY,
                "Special Card 2026 RSS2", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Raiden_Shogun/Raiden_Shogun_RSS2.png");
        registerCard(100010, "Raiden Shogun - RSS3", CardRarity.LEGENDARY,
                "Special Card 2026 RSS3", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Raiden_Shogun/Raiden_Shogun_RSS3.png");

        registerCard(100011, "Mona - MOS1", CardRarity.LEGENDARY,
                "Special Card 2026 MOS1", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mona/Mona_MOS1.png");
        registerCard(100012, "Mona - MOS2", CardRarity.LEGENDARY,
                "Special Card 2026 MOS2", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mona/Mona_MOS2.png");
        registerCard(100013, "Mona - RMOS3", CardRarity.LEGENDARY,
                "Special Card 2026 MOS3", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Mona/Mona_MOS3.png");

        registerCard(100014, "Varesa - VAS1", CardRarity.LEGENDARY,
                "Special Card 2026 VAS1", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Varesa/Varesa_VAS1.png");
        registerCard(100015, "Varesa - VAS2", CardRarity.LEGENDARY,
                "Special Card 2026 VAS2", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Varesa/Varesa_VAS2.png");

        registerCard(100016, "Lauma - LAS1", CardRarity.LEGENDARY,
                "Special Card 2026 LAS1", "Genshin Impact","https://raw.githubusercontent.com/itsLarss/cards_img/main/CYC_Sammelkarten/SFW_Charakter/Lauma/Lauma_LAS1.png");

        // Super Special Cards | Mythic
        //nothing there yet

    }

    private static void registerCard(int id, String name, CardRarity rarity,
                                     String description, String series, String imageUrl) {
        CARDS.put(id, new Card(id, name, rarity, description, series, imageUrl));
    }

    /**
     * Gibt eine Karte anhand ihrer ID zurück
     */
    public static Card getCard(int id) {
        return CARDS.get(id);
    }

    /**
     * Gibt eine Karte anhand ihres Namens zurück
     */
    public static Card getCardByName(String name) {
        for (Card card : CARDS.values()) {
            if (card.getName().equalsIgnoreCase(name)) {
                return card;
            }
        }
        return null;
    }

    /**
     * Gibt alle Karten zurück
     */
    public static Collection<Card> getAllCards() {
        return CARDS.values();
    }

    /**
     * Gibt die Gesamtanzahl der Karten zurück
     */
    public static int getTotalCardCount() {
        return CARDS.size();
    }

    /**
     * Zieht eine zufällige Karte basierend auf Seltenheitswahrscheinlichkeiten
     */
    public static Card drawRandomCard() {
        // Gruppiere Karten nach Seltenheit
        Map<CardRarity, List<Card>> cardsByRarity = new HashMap<>();
        for (CardRarity rarity : CardRarity.values()) {
            cardsByRarity.put(rarity, new ArrayList<>());
        }

        for (Card card : CARDS.values()) {
            cardsByRarity.get(card.getRarity()).add(card);
        }

        // Ziehe zuerst die Seltenheit
        double rand = ThreadLocalRandom.current().nextDouble(100);
        double cumulative = 0;
        CardRarity selectedRarity = CardRarity.COMMON;

        for (CardRarity rarity : CardRarity.values()) {
            cumulative += rarity.getDropChance();
            if (rand <= cumulative) {
                selectedRarity = rarity;
                break;
            }
        }

        // Ziehe eine zufällige Karte der gewählten Seltenheit
        List<Card> availableCards = cardsByRarity.get(selectedRarity);
        if (availableCards.isEmpty()) {
            // Fallback zu Common falls keine Karten verfügbar
            availableCards = cardsByRarity.get(CardRarity.COMMON);
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(availableCards.size());
        return availableCards.get(randomIndex);
    }

    /**
     * Zieht mehrere verschiedene Karten (keine Duplikate)
     */
    public static List<Card> drawMultipleUniqueCards(int count) {
        List<Card> drawn = new ArrayList<>();
        Set<Integer> drawnIds = new HashSet<>();

        int attempts = 0;
        int maxAttempts = count * 10; // Verhindere Endlosschleife

        while (drawn.size() < count && attempts < maxAttempts) {
            Card card = drawRandomCard();
            if (!drawnIds.contains(card.getId())) {
                drawn.add(card);
                drawnIds.add(card.getId());
            }
            attempts++;
        }

        return drawn;
    }

    /**
     * Gibt alle Karten einer bestimmten Seltenheit zurück
     */
    public static List<Card> getCardsByRarity(CardRarity rarity) {
        List<Card> result = new ArrayList<>();
        for (Card card : CARDS.values()) {
            if (card.getRarity() == rarity) {
                result.add(card);
            }
        }
        return result;
    }

    /**
     * Gibt alle Karten einer bestimmten Serie zurück
     */
    public static List<Card> getCardsBySeries(String series) {
        List<Card> result = new ArrayList<>();
        for (Card card : CARDS.values()) {
            if (card.getSeries().equalsIgnoreCase(series)) {
                result.add(card);
            }
        }
        return result;
    }
}