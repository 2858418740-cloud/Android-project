package com.example.projectw;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库帮助类 — 统一管理 SQLite 的建表、预置数据、CRUD 操作
 * 数据库名：nailong.db，版本号：4
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "nailong.db";
    private static final int DB_VERSION = 6;

    // ==================== 表名 ====================
    private static final String TABLE_USER = "user";
    private static final String TABLE_SHOP = "shop";
    private static final String TABLE_FOOD = "food";
    private static final String TABLE_CART = "cart";
    private static final String TABLE_ORDERS = "orders";
    private static final String TABLE_ADDRESS = "address";

    // ==================== 构造 ====================
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // ==================== 建表 ====================
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 用户表
        db.execSQL("CREATE TABLE " + TABLE_USER + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL UNIQUE, "
                + "password TEXT NOT NULL, "
                + "is_remember INTEGER DEFAULT 0)");

        // 店铺表（含新增：距离、排名、堂食外卖、评分、优惠、大图）
        db.execSQL("CREATE TABLE " + TABLE_SHOP + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "shop_name TEXT NOT NULL, "
                + "shop_logo TEXT, "
                + "sale_count INTEGER DEFAULT 0, "
                + "delivery_price REAL DEFAULT 0.0, "
                + "delivery_time TEXT, "
                + "shop_desc TEXT, "
                + "distance TEXT, "
                + "local_rank INTEGER DEFAULT 0, "
                + "dine_type TEXT, "
                + "rating REAL DEFAULT 0.0, "
                + "promotion TEXT, "
                + "shop_image TEXT)");

        // 菜品表
        db.execSQL("CREATE TABLE " + TABLE_FOOD + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "shop_id INTEGER NOT NULL, "
                + "food_name TEXT NOT NULL, "
                + "price REAL NOT NULL, "
                + "food_desc TEXT, "
                + "image_res TEXT)");

        // 购物车表
        db.execSQL("CREATE TABLE " + TABLE_CART + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL, "
                + "food_id INTEGER NOT NULL, "
                + "food_name TEXT NOT NULL, "
                + "price REAL NOT NULL, "
                + "count INTEGER DEFAULT 1)");

        // 订单表
        db.execSQL("CREATE TABLE " + TABLE_ORDERS + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL, "
                + "total_price REAL NOT NULL, "
                + "order_time TEXT NOT NULL, "
                + "food_info TEXT NOT NULL)");

        // 收货地址表
        db.execSQL("CREATE TABLE " + TABLE_ADDRESS + " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL, "
                + "detail TEXT NOT NULL, "
                + "is_default INTEGER DEFAULT 0)");

        // 插入预置测试数据
        insertTestData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADDRESS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    // ==================== 预置测试数据（15 家店铺 + 70+ 道菜品） ====================
    private void insertTestData(SQLiteDatabase db) {
        // ==================== 插入 15 家店铺（含距离/排名/堂食外卖/评分/优惠/大图） ====================
        //                          店名        Logo                  月销   配送费  时长       简介                         距离    排名  堂食外卖      评分  优惠         大图
        insertShop(db, "老王面馆",   "shop_logo_noodle",       3856, 2.0, "25-35分钟", "三十年老店，手工拉面，汤浓面劲道",      "1.2km",  3, "堂食",       4.7, "",           "shop_banner_noodle");
        insertShop(db, "麦香汉堡",   "shop_logo_burger",       5621, 3.0, "20-30分钟", "现点现做，牛肉饼现煎，新鲜蔬菜搭配",    "0.8km",  1, "堂食&外卖",  4.9, "满30减5",     "shop_banner_burger");
        insertShop(db, "蜀味川菜",   "shop_logo_sichuan",      4203, 1.5, "30-40分钟", "正宗四川麻辣，食材新鲜，现炒出锅",      "2.1km",  5, "堂食",       4.6, "满50减10",    "shop_banner_sichuan");
        insertShop(db, "幸福茶餐厅", "shop_logo_tea",          3198, 2.5, "25-35分钟", "地道港味，丝袜奶茶，碟头饭，菠萝油",    "1.5km",  7, "堂食&外卖",  4.5, "",           "shop_banner_tea");
        insertShop(db, "樱花日料",   "shop_logo_japanese",     2745, 4.0, "30-45分钟", "新鲜刺身，手握寿司，精致便当",          "3.0km",  9, "堂食",       4.8, "满80减15",    "shop_banner_japanese");
        insertShop(db, "必胜披萨",   "shop_logo_pizza",        4102, 5.0, "25-40分钟", "手工现做披萨，芝士拉丝，料足味美",      "1.8km",  4, "堂食&外卖",  4.7, "满60减8",     "shop_banner_pizza");
        insertShop(db, "韩式炸鸡屋", "shop_logo_korean_ck",    3890, 3.5, "25-35分钟", "酥脆炸鸡，秘制酱料，韩剧同款",          "1.0km",  2, "外卖",       4.9, "满40减6",     "shop_banner_korean_ck");
        insertShop(db, "湘味小厨",   "shop_logo_hunan",        3512, 2.0, "30-40分钟", "地道湘菜，香辣下饭，米饭杀手",          "2.5km",  6, "堂食",       4.5, "",           "shop_banner_hunan");
        insertShop(db, "粤式烧腊",   "shop_logo_cantonese",    4721, 2.5, "20-30分钟", "明炉烧腊，皮脆肉嫩，传承三代人",        "0.6km",  8, "堂食&外卖",  4.6, "满35减5",     "shop_banner_cantonese");
        insertShop(db, "兰州拉面馆", "shop_logo_lanzhou",      2987, 1.5, "20-30分钟", "一清二白三红四绿五黄，正宗兰州味",      "1.3km", 10, "堂食",       4.4, "",           "shop_banner_lanzhou");
        insertShop(db, "沙县小吃",   "shop_logo_shaxian",      5314, 1.0, "15-25分钟", "国民小吃，经济实惠，品种丰富",          "0.5km", 11, "堂食&外卖",  4.2, "",           "shop_banner_shaxian");
        insertShop(db, "黄焖鸡米饭", "shop_logo_braised_ck",   3678, 2.0, "20-30分钟", "一只鸡的传说，浓汁拌饭，回味无穷",      "0.9km", 12, "外卖",       4.3, "满20减3",     "shop_banner_braised_ck");
        insertShop(db, "麻辣烫工坊", "shop_logo_malatang",     2945, 2.0, "20-30分钟", "自选食材，骨汤熬制，麻辣鲜香",          "1.6km", 13, "堂食",       4.3, "满25减4",     "shop_banner_malatang");
        insertShop(db, "甜蜜时光甜品","shop_logo_dessert",     2230, 3.0, "20-35分钟", "港式甜品，新鲜水果，甜蜜每一天",        "2.2km", 14, "堂食&外卖",  4.6, "满30减5",     "shop_banner_dessert");
        insertShop(db, "大嘴烧烤",   "shop_logo_bbq",          5032, 2.5, "35-50分钟", "炭火现烤，秘制撒料，深夜必点",          "1.1km", 15, "堂食",       4.4, "",           "shop_banner_bbq");

        // ==================== 插入菜品 ====================
        // 店铺1：老王面馆 (shop_id=1) —— 4道
        insertFood(db, 1, "牛肉拉面", 18.0, "秘制红烧牛肉，手工拉面，配香菜葱花", "food_noodle_beef");
        insertFood(db, 1, "炸酱面", 15.0, "老北京炸酱，黄瓜丝豆芽，酱香浓郁", "food_noodle_zha_jiang");
        insertFood(db, 1, "酸辣粉", 12.0, "红薯粉条，花生碎，酸辣开胃", "food_noodle_sour_spicy");
        insertFood(db, 1, "煎饺（8只）", 16.0, "猪肉白菜馅，底部金黄酥脆", "food_noodle_dumpling");
        insertFood(db, 1, "红烧牛肉面", 20.0, "大块牛腩炖得酥烂，红汤浓郁，面条劲道", "food_noodle_braised_beef");
        insertFood(db, 1, "担担面", 14.0, "肉末芽菜花生碎，红油麻酱，咸鲜微辣", "food_noodle_dandan");
        insertFood(db, 1, "油泼面", 13.0, "裤带面，蒜末辣椒面热油一泼，香气四溢", "food_noodle_youpo");
        insertFood(db, 1, "葱油拌面", 10.0, "小葱慢炸出香，酱油调味，简单却好味", "food_noodle_scallion");
        insertFood(db, 1, "肉夹馍", 12.0, "现烤白吉馍夹腊汁肉，肥瘦相间汁水足", "food_noodle_meat_bun");

        // 店铺2：麦香汉堡 (shop_id=2) —— 10道
        insertFood(db, 2, "经典牛肉堡", 22.0, "安格斯牛肉饼，芝士片，生菜番茄", "food_burger_beef");
        insertFood(db, 2, "香辣鸡腿堡", 19.0, "整块鸡腿肉，香辣腌料，外酥里嫩", "food_burger_chicken");
        insertFood(db, 2, "薯条（大份）", 10.0, "粗切土豆，外酥里糯，配番茄酱", "food_burger_fries");
        insertFood(db, 2, "鸡米花", 12.0, "一口一个，金黄酥脆，配甜辣酱", "food_burger_popcorn_chicken");
        insertFood(db, 2, "可乐（中杯）", 7.0, "冰镇可口可乐", "food_burger_cola");
        insertFood(db, 2, "双层芝士堡", 28.0, "双层牛肉饼+双重芝士，肉食者狂欢", "food_burger_double_cheese");
        insertFood(db, 2, "鳕鱼堡", 18.0, "整块鳕鱼排，外酥里嫩，配塔塔酱", "food_burger_fish");
        insertFood(db, 2, "洋葱圈", 8.0, "金黄酥脆洋葱圈，配番茄酱，追剧必备", "food_burger_onion_rings");
        insertFood(db, 2, "圣代冰淇淋", 9.0, "香草/草莓/巧克力三选一，冰凉解腻", "food_burger_sundae");
        insertFood(db, 2, "柠檬茶", 6.0, "柠檬+红茶，酸甜清爽，去油解腻", "food_burger_lemon_tea");

        // 店铺3：蜀味川菜 (shop_id=3) —— 11道
        insertFood(db, 3, "宫保鸡丁", 28.0, "鸡腿肉丁，花生米，干辣椒，荔枝味型", "food_sichuan_gongbao");
        insertFood(db, 3, "麻婆豆腐", 18.0, "嫩豆腐，牛肉末，花椒粉，麻辣鲜香", "food_sichuan_mapo");
        insertFood(db, 3, "回锅肉", 26.0, "五花肉片，蒜苗，豆瓣酱，肥而不腻", "food_sichuan_huiguo");
        insertFood(db, 3, "水煮鱼片", 38.0, "草鱼片，豆芽，麻辣红油，嫩滑入味", "food_sichuan_shuizhu");
        insertFood(db, 3, "酸辣土豆丝", 12.0, "土豆丝，干辣椒，醋溜爽脆", "food_sichuan_potato");
        insertFood(db, 3, "米饭", 3.0, "东北大米，粒粒分明", "food_sichuan_rice");
        insertFood(db, 3, "鱼香肉丝", 22.0, "猪肉丝+木耳+笋丝，泡椒炒制，酸甜微辣", "food_sichuan_yuxiang");
        insertFood(db, 3, "辣子鸡丁", 30.0, "鸡丁干香，辣椒花椒堆里找肉吃，越嚼越香", "food_sichuan_lazi_ck");
        insertFood(db, 3, "水煮牛肉", 42.0, "牛里脊薄片，麻辣红油浇面，嫩滑无比", "food_sichuan_boiled_beef");
        insertFood(db, 3, "干煸四季豆", 15.0, "四季豆煸至表皮起皱，肉末芽菜增香", "food_sichuan_beans");
        insertFood(db, 3, "夫妻肺片", 25.0, "牛腱牛肚卤制切片，红油花椒凉拌，麻辣回甜", "food_sichuan_fuqi");

        // 店铺4：幸福茶餐厅 (shop_id=4) —— 11道
        insertFood(db, 4, "叉烧饭", 24.0, "蜜汁叉烧，时蔬，溏心蛋", "food_tea_chashao");
        insertFood(db, 4, "咖喱牛腩饭", 28.0, "牛腩软烂，马铃薯，咖喱浓郁", "food_tea_curry_beef");
        insertFood(db, 4, "菠萝油", 10.0, "热菠萝包夹冰黄油，外热内冷", "food_tea_boluo_bun");
        insertFood(db, 4, "丝袜奶茶", 13.0, "港式拉茶，茶香奶滑", "food_tea_milk_tea");
        insertFood(db, 4, "西多士", 15.0, "花生酱夹心，蛋液煎制，淋炼奶", "food_tea_french_toast");
        insertFood(db, 4, "干炒牛河", 26.0, "河粉+牛肉+芽菜，猛火快炒，镬气十足", "food_tea_beef_chow_fun");
        insertFood(db, 4, "云吞面", 18.0, "鲜虾云吞+竹升面+大地鱼汤底，港味经典", "food_tea_wonton_noodle");
        insertFood(db, 4, "蛋挞（3个）", 12.0, "酥皮蛋挞，蛋奶馅嫩滑，刚出炉最香", "food_tea_egg_tart");
        insertFood(db, 4, "冻柠茶", 9.0, "柠檬片+红茶，冰凉爽口，解腻神器", "food_tea_lemon_tea");
        insertFood(db, 4, "猪扒包", 20.0, "现煎猪扒+脆皮面包，澳门街头风味", "food_tea_pork_bun");
        insertFood(db, 4, "滑蛋牛肉饭", 25.0, "嫩滑炒蛋+牛肉片，盖在米饭上，家常美味", "food_tea_beef_rice");

        // 店铺5：樱花日料 (shop_id=5) —— 10道
        insertFood(db, 5, "三文鱼刺身", 42.0, "挪威三文鱼，厚切6片，配芥末酱油", "food_jp_salmon");
        insertFood(db, 5, "鳗鱼饭", 38.0, "蒲烧鳗鱼，蛋丝，米饭，酱汁渗透", "food_jp_eel_rice");
        insertFood(db, 5, "猪骨拉面", 26.0, "豚骨浓汤，溏心蛋，叉烧肉片", "food_jp_ramen");
        insertFood(db, 5, "章鱼小丸子", 16.0, "6颗，木鱼花，沙拉酱，照烧汁", "food_jp_takoyaki");
        insertFood(db, 5, "抹茶大福", 12.0, "糯米皮，红豆馅，抹茶粉，软糯Q弹", "food_jp_mochi");
        insertFood(db, 5, "天妇罗拼盘", 32.0, "炸虾+蔬菜天妇罗，外衣酥脆，蘸汁解腻", "food_jp_tempura");
        insertFood(db, 5, "加州卷（8枚）", 28.0, "蟹棒+牛油果+青瓜，反卷寿司，口感丰富", "food_jp_california");
        insertFood(db, 5, "盐烤秋刀鱼", 18.0, "整条秋刀鱼海盐炙烤，挤柠檬汁，原汁原味", "food_jp_saury");
        insertFood(db, 5, "味噌汤", 10.0, "豆腐+海带+葱花，日式味噌煮制，暖心暖胃", "food_jp_miso");
        insertFood(db, 5, "日式煎饺", 16.0, "薄皮猪肉煎饺，底部焦脆，配酱油醋汁", "food_jp_gyoza");

        // 店铺6：必胜披萨 (shop_id=6) —— 10道
        insertFood(db, 6, "超级至尊披萨（9寸）", 58.0, "牛肉粒+意大利肠+青椒+洋葱+芝士", "food_pizza_supreme");
        insertFood(db, 6, "榴莲披萨（9寸）", 48.0, "泰国金枕榴莲果肉，搭配芝士", "food_pizza_durian");
        insertFood(db, 6, "意式肉酱面", 26.0, "番茄肉酱，帕玛森干酪，意面筋道", "food_pizza_pasta");
        insertFood(db, 6, "奶油蘑菇汤", 15.0, "新鲜口蘑，淡奶油，浓香顺滑", "food_pizza_soup");
        insertFood(db, 6, "蒜香面包条", 8.0, "法棍面包，黄油蒜蓉，烤至金黄", "food_pizza_bread");
        insertFood(db, 6, "夏威夷披萨（9寸）", 45.0, "火腿+菠萝+芝士，酸甜可口，小朋友最爱", "food_pizza_hawaii");
        insertFood(db, 6, "黑椒牛柳意面", 30.0, "牛柳+彩椒+黑椒汁，意面裹满酱汁", "food_pizza_beef_pasta");
        insertFood(db, 6, "提拉米苏", 18.0, "马斯卡彭芝士+咖啡手指饼干，层层叠叠", "food_pizza_tiramisu");
        insertFood(db, 6, "凯撒沙拉", 22.0, "罗马生菜+培根碎+帕玛森+凯撒酱", "food_pizza_caesar");
        insertFood(db, 6, "芝士条", 12.0, "马苏里拉芝士条炸至拉丝，配番茄酱", "food_pizza_cheese_sticks");

        // 店铺7：韩式炸鸡屋 (shop_id=7) —— 10道
        insertFood(db, 7, "原味炸鸡（半只）", 32.0, "外酥里嫩，汁水丰盈，配萝卜块", "food_korean_fried_ck");
        insertFood(db, 7, "甜辣酱炸鸡", 35.0, "韩式甜辣酱裹衣，撒芝麻，甜辣过瘾", "food_korean_spicy_ck");
        insertFood(db, 7, "芝士年糕", 18.0, "Q弹年糕+马苏里拉芝士，韩式辣酱炒制", "food_korean_tteok");
        insertFood(db, 7, "泡菜炒饭", 20.0, "韩式泡菜+五花肉丁+溏心煎蛋", "food_korean_kimchi_rice");
        insertFood(db, 7, "韩式炸酱面", 22.0, "春酱炒制，黑豆酱汁，配黄瓜丝", "food_korean_black_noodle");
        insertFood(db, 7, "酱香炸鸡", 33.0, "酱油蒜香腌料，咸甜入味，不辣星人之选", "food_korean_soy_ck");
        insertFood(db, 7, "蜂蜜黄油炸鸡", 36.0, "蜂蜜黄油裹衣，香甜酥脆，一口上瘾", "food_korean_honey_ck");
        insertFood(db, 7, "韩式炒年糕", 15.0, "条状年糕+鱼饼，韩式辣酱煮制，汤汁浓稠", "food_korean_rice_cake");
        insertFood(db, 7, "泡菜豆腐汤", 16.0, "五花肉+泡菜+嫩豆腐，滚烫上桌，酸辣开胃", "food_korean_tofu_soup");
        insertFood(db, 7, "紫菜包饭", 14.0, "米饭+胡萝卜+蛋皮+火腿，芝麻油增香", "food_korean_gimbap");

        // 店铺8：湘味小厨 (shop_id=8) —— 11道
        insertFood(db, 8, "剁椒鱼头", 48.0, "鲜嫩花鲢鱼头，剁椒蒸制，鲜辣入味", "food_hunan_fish_head");
        insertFood(db, 8, "辣椒炒肉", 22.0, "宁乡花猪肉，螺丝椒，豆豉爆炒", "food_hunan_pepper_meat");
        insertFood(db, 8, "酸豆角肉末", 16.0, "自制酸豆角，肉末炒香，下饭神器", "food_hunan_sour_beans");
        insertFood(db, 8, "剁椒蒸排骨", 32.0, "小排剁椒同蒸，骨酥肉烂，香辣扑鼻", "food_hunan_ribs");
        insertFood(db, 8, "米饭", 3.0, "五常大米，粒粒分明", "food_hunan_rice");
        insertFood(db, 8, "小炒黄牛肉", 35.0, "黄牛肉薄片+香菜+小米辣爆炒，嫩滑香辣", "food_hunan_beef");
        insertFood(db, 8, "农家一碗香", 20.0, "五花肉+鸡蛋+青椒同炒，农家下饭菜", "food_hunan_yiwanxiang");
        insertFood(db, 8, "口味虾", 58.0, "小龙虾+紫苏+蒜瓣，香辣入味，夏日必点", "food_hunan_crayfish");
        insertFood(db, 8, "蒜蓉空心菜", 14.0, "新鲜空心菜，蒜蓉爆香，爽脆碧绿", "food_hunan_water_spinach");
        insertFood(db, 8, "擂辣椒皮蛋", 15.0, "烤青椒+皮蛋擂碎拌匀，湘味凉菜灵魂", "food_hunan_preserved_egg");
        insertFood(db, 8, "剁椒芋头", 18.0, "小芋头+剁椒蒸制，软糯入味入口即化", "food_hunan_taro");

        // 店铺9：粤式烧腊 (shop_id=9) —— 10道
        insertFood(db, 9, "蜜汁叉烧", 28.0, "猪梅肉，麦芽糖蜜汁，入口即化", "food_cantonese_chashao");
        insertFood(db, 9, "脆皮烧鸭（半只）", 32.0, "明炉挂烧，皮脆肉嫩，配酸梅酱", "food_cantonese_duck");
        insertFood(db, 9, "白切鸡（半只）", 30.0, "三黄鸡，浸熟后冰镇，皮爽肉滑", "food_cantonese_chicken");
        insertFood(db, 9, "烧肉拼盘", 35.0, "脆皮烧肉+叉烧+烧鸭，三拼满足", "food_cantonese_platter");
        insertFood(db, 9, "烧鹅腿饭", 38.0, "整只烧鹅腿，油亮诱人，配时蔬米饭", "food_cantonese_goose");
        insertFood(db, 9, "蜜汁叉烧包（3个）", 15.0, "面皮松软，叉烧馅甜香，早茶必点", "food_cantonese_bbq_bun");
        insertFood(db, 9, "豉汁蒸排骨", 26.0, "小排+豆豉+蒜蓉蒸制，鲜美嫩滑", "food_cantonese_spare_ribs");
        insertFood(db, 9, "虾饺（4只）", 20.0, "澄面皮透亮，整只鲜虾馅，Q弹爽滑", "food_cantonese_shrimp_dumpling");
        insertFood(db, 9, "干炒牛河", 22.0, "河粉+牛肉+芽菜，猛火快炒，镬气十足", "food_cantonese_beef_chow_fun");
        insertFood(db, 9, "例汤", 8.0, "每日现煲老火汤，当日食材，清甜滋补", "food_cantonese_daily_soup");

        // 店铺10：兰州拉面馆 (shop_id=10) —— 6道
        insertFood(db, 10, "兰州牛肉面", 16.0, "一清二白三红四绿五黄，汤清面筋", "food_lanzhou_beef_noodle");
        insertFood(db, 10, "炒拉条", 18.0, "手工拉条子，番茄洋葱青椒爆炒", "food_lanzhou_fried_noodle");
        insertFood(db, 10, "羊肉泡馍", 28.0, "羊肉烂熟，馍粒吸汤，配糖蒜辣酱", "food_lanzhou_yangrou_paomo");
        insertFood(db, 10, "凉皮", 10.0, "陕西凉皮，黄瓜丝面筋，油泼辣子醋汁", "food_lanzhou_liangpi");
        insertFood(db, 10, "牛肉饼", 8.0, "千层酥皮，牛肉大葱馅，现烙热乎", "food_lanzhou_beef_pie");
        insertFood(db, 10, "自制酸奶", 6.0, "纯牛奶发酵，撒葡萄干花生碎，酸甜解腻", "food_lanzhou_yogurt");

        // 店铺11：沙县小吃 (shop_id=11) —— 6道
        insertFood(db, 11, "蒸饺", 8.0, "猪肉大葱馅，薄皮大馅，配花生酱", "food_shaxian_dumpling");
        insertFood(db, 11, "拌面", 7.0, "碱面拌花生酱，撒葱花，简单好味", "food_shaxian_noodle");
        insertFood(db, 11, "扁肉", 6.0, "薄如蝉翼的燕皮，猪肉馅，清汤煮就", "food_shaxian_wonton");
        insertFood(db, 11, "炖罐", 12.0, "排骨/乌鸡/猪心任选，隔水炖制四小时", "food_shaxian_soup");
        insertFood(db, 11, "炒米粉", 10.0, "兴化米粉，鸡蛋蔬菜同炒，锅气十足", "food_shaxian_rice_noodle");
        insertFood(db, 11, "鸡腿饭", 15.0, "卤鸡腿+卤蛋+青菜+米饭，一份管饱", "food_shaxian_ck_rice");

        // 店铺12：黄焖鸡米饭 (shop_id=12) —— 5道
        insertFood(db, 12, "黄焖鸡（小份）", 18.0, "鸡腿肉焖制，酱汁浓郁，配一碗米饭", "food_braised_ck_small");
        insertFood(db, 12, "黄焖鸡（大份）", 25.0, "双倍鸡腿肉，多加配菜，配一碗米饭", "food_braised_ck_large");
        insertFood(db, 12, "黄焖排骨", 22.0, "猪小排焖制，土豆软糯，酱香入味", "food_braised_ribs");
        insertFood(db, 12, "黄焖牛腩", 28.0, "牛腩+土豆+胡萝卜，文火慢焖，软烂入味", "food_braised_beef");
        insertFood(db, 12, "加份米饭", 2.0, "五常大米，一碗不够再来一碗", "food_braised_rice");

        // 店铺13：麻辣烫工坊 (shop_id=13) —— 6道
        insertFood(db, 13, "麻辣烫（素菜套餐）", 12.0, "生菜+藕片+土豆+海带+豆皮+粉丝", "food_malatang_veggie");
        insertFood(db, 13, "麻辣烫（荤素套餐）", 20.0, "牛肉丸+午餐肉+蟹棒+时蔬+粉丝", "food_malatang_meat");
        insertFood(db, 13, "麻辣拌", 15.0, "煮熟后干拌芝麻酱辣椒油，不加汤", "food_malatang_dry");
        insertFood(db, 13, "骨汤麻辣烫", 18.0, "猪骨奶白浓汤底，不辣星人的选择", "food_malatang_bone");
        insertFood(db, 13, "麻辣香锅（小份）", 32.0, "香辣酱爆炒，虾+午餐肉+藕片+土豆+花菜", "food_malatang_xiangguo");
        insertFood(db, 13, "冰镇酸梅汤", 5.0, "古法熬制酸梅汤，解辣必备", "food_malatang_drink");

        // 店铺14：甜蜜时光甜品 (shop_id=14) —— 6道
        insertFood(db, 14, "芒果班戟", 16.0, "薄皮包裹奶油+新鲜芒果，软糯香甜", "food_dessert_mango_pancake");
        insertFood(db, 14, "杨枝甘露", 18.0, "芒果+西柚+西米+椰浆，酸甜清爽", "food_dessert_sago");
        insertFood(db, 14, "双皮奶", 12.0, "顺德水牛奶，两层奶皮，嫩滑如丝", "food_dessert_double_milk");
        insertFood(db, 14, "椰汁西米露", 10.0, "椰浆+西米+芋头块，香甜解暑", "food_dessert_coconut_sago");
        insertFood(db, 14, "芋圆烧仙草", 14.0, "手工芋圆+仙草冻+红豆+珍珠，料超足", "food_dessert_taro_ball");
        insertFood(db, 14, "榴莲千层", 22.0, "猫山王榴莲果肉+奶油，薄如蝉翼的可丽饼皮", "food_dessert_durian_cake");

        // 店铺15：大嘴烧烤 (shop_id=15) —— 6道
        insertFood(db, 15, "羊肉串（10串）", 30.0, "宁夏滩羊，孜然辣椒，炭火现烤", "food_bbq_lamb");
        insertFood(db, 15, "烤生蚝（6只）", 28.0, "蒜蓉粉丝铺面，炭火烤至冒泡", "food_bbq_oyster");
        insertFood(db, 15, "烤茄子", 12.0, "整只茄子剖开，铺蒜蓉小米辣，软烂入味", "food_bbq_eggplant");
        insertFood(db, 15, "烤鸡翅（5只）", 20.0, "奥尔良腌料，外焦里嫩，一咬脱骨", "food_bbq_wings");
        insertFood(db, 15, "烤五花肉", 18.0, "厚切五花肉，烤得滋滋冒油，蘸干碟", "food_bbq_pork_belly");
        insertFood(db, 15, "烤馒头", 5.0, "奶香馒头，刷蜂蜜黄油，外脆里软", "food_bbq_bun");
    }

    private void insertShop(SQLiteDatabase db, String name, String logo, int sales, double deliveryPrice,
            String deliveryTime, String desc, String distance, int localRank, String dineType,
            double rating, String promotion, String shopImage) {
        ContentValues cv = new ContentValues();
        cv.put("shop_name", name);
        cv.put("shop_logo", logo);
        cv.put("sale_count", sales);
        cv.put("delivery_price", deliveryPrice);
        cv.put("delivery_time", deliveryTime);
        cv.put("shop_desc", desc);
        cv.put("distance", distance);
        cv.put("local_rank", localRank);
        cv.put("dine_type", dineType);
        cv.put("rating", rating);
        cv.put("promotion", promotion);
        cv.put("shop_image", shopImage);
        db.insert(TABLE_SHOP, null, cv);
    }

    private void insertFood(SQLiteDatabase db, int shopId, String name, double price, String desc, String imageRes) {
        ContentValues cv = new ContentValues();
        cv.put("shop_id", shopId);
        cv.put("food_name", name);
        cv.put("price", price);
        cv.put("food_desc", desc);
        cv.put("image_res", imageRes);
        db.insert(TABLE_FOOD, null, cv);
    }

    // ==================== 用户相关操作 ====================

    /** 注册新用户（含重名检查） */
    public boolean registerUser(String username, String password) {
        if (isUserExists(username)) return false;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("password", password);
        cv.put("is_remember", 0);
        long result = db.insert(TABLE_USER, null, cv);
        return result != -1;
    }

    /** 检查用户名是否已存在 */
    public boolean isUserExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USER, null, "username=?", new String[]{username}, null, null, null);
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    /** 登录校验 */
    public boolean loginUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USER, null,
                "username=? AND password=?",
                new String[]{username, password}, null, null, null);
        boolean success = c.getCount() > 0;
        c.close();
        return success;
    }

    /** 更新记住密码状态（先全部清零，再设置当前用户） */
    public void updateRememberStatus(String username, boolean remember) {
        SQLiteDatabase db = getWritableDatabase();
        // 全部清零
        ContentValues clearCv = new ContentValues();
        clearCv.put("is_remember", 0);
        db.update(TABLE_USER, clearCv, null, null);
        // 设置当前用户
        if (remember) {
            ContentValues cv = new ContentValues();
            cv.put("is_remember", 1);
            db.update(TABLE_USER, cv, "username=?", new String[]{username});
        }
    }

    /** 获取记住密码的用户信息，返回 [username, password]，无记录返回 null */
    public String[] getRememberedUser() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_USER, new String[]{"username", "password"},
                "is_remember=?", new String[]{"1"}, null, null, null);
        if (c.moveToFirst()) {
            String[] result = new String[]{c.getString(0), c.getString(1)};
            c.close();
            return result;
        }
        c.close();
        return null;
    }

    // ==================== 店铺相关操作 ====================

    /** 获取所有店铺列表 */
    public List<Shop> getAllShops() {
        List<Shop> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_SHOP, null, null, null, null, null, "local_rank ASC");
        while (c.moveToNext()) {
            list.add(cursorToShop(c));
        }
        c.close();
        return list;
    }

    /** 搜索店铺（按名称模糊匹配） */
    public List<Shop> searchShops(String keyword) {
        List<Shop> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_SHOP, null, "shop_name LIKE ?",
                new String[]{"%" + keyword + "%"}, null, null, "local_rank ASC");
        while (c.moveToNext()) {
            list.add(cursorToShop(c));
        }
        c.close();
        return list;
    }

    /** 根据 ID 获取店铺 */
    public Shop getShopById(int shopId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_SHOP, null, "_id=?", new String[]{String.valueOf(shopId)}, null, null, null);
        if (c.moveToFirst()) {
            Shop shop = cursorToShop(c);
            c.close();
            return shop;
        }
        c.close();
        return null;
    }

    /** Cursor → Shop 对象映射 */
    private Shop cursorToShop(Cursor c) {
        Shop shop = new Shop();
        shop.setId(c.getInt(c.getColumnIndexOrThrow("_id")));
        shop.setShopName(c.getString(c.getColumnIndexOrThrow("shop_name")));
        shop.setShopLogo(c.getString(c.getColumnIndexOrThrow("shop_logo")));
        shop.setSaleCount(c.getInt(c.getColumnIndexOrThrow("sale_count")));
        shop.setDeliveryPrice(c.getDouble(c.getColumnIndexOrThrow("delivery_price")));
        shop.setDeliveryTime(c.getString(c.getColumnIndexOrThrow("delivery_time")));
        shop.setShopDesc(c.getString(c.getColumnIndexOrThrow("shop_desc")));
        shop.setDistance(c.getString(c.getColumnIndexOrThrow("distance")));
        shop.setLocalRank(c.getInt(c.getColumnIndexOrThrow("local_rank")));
        shop.setDineType(c.getString(c.getColumnIndexOrThrow("dine_type")));
        shop.setRating(c.getDouble(c.getColumnIndexOrThrow("rating")));
        shop.setPromotion(c.getString(c.getColumnIndexOrThrow("promotion")));
        shop.setShopImage(c.getString(c.getColumnIndexOrThrow("shop_image")));
        return shop;
    }

    // ==================== 菜品相关操作 ====================

    /** 按店铺 ID 获取菜品列表 */
    public List<Food> getFoodsByShopId(int shopId) {
        List<Food> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_FOOD, null, "shop_id=?", new String[]{String.valueOf(shopId)}, null, null, null);
        while (c.moveToNext()) {
            list.add(cursorToFood(c));
        }
        c.close();
        return list;
    }

    /** 根据菜品 ID 获取菜品详情 */
    public Food getFoodById(int foodId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_FOOD, null, "_id=?", new String[]{String.valueOf(foodId)}, null, null, null);
        if (c.moveToFirst()) {
            Food food = cursorToFood(c);
            c.close();
            return food;
        }
        c.close();
        return null;
    }

    private Food cursorToFood(Cursor c) {
        Food food = new Food();
        food.setId(c.getInt(c.getColumnIndexOrThrow("_id")));
        food.setShopId(c.getInt(c.getColumnIndexOrThrow("shop_id")));
        food.setFoodName(c.getString(c.getColumnIndexOrThrow("food_name")));
        food.setPrice(c.getDouble(c.getColumnIndexOrThrow("price")));
        food.setFoodDesc(c.getString(c.getColumnIndexOrThrow("food_desc")));
        food.setImageRes(c.getString(c.getColumnIndexOrThrow("image_res")));
        return food;
    }

    // ==================== 购物车相关操作 ====================

    /** 加入购物车：已存在则数量+1，否则新增 */
    public void addToCart(String username, int foodId, String foodName, double price) {
        SQLiteDatabase db = getWritableDatabase();
        // 检查是否已存在
        Cursor c = db.query(TABLE_CART, null,
                "username=? AND food_id=?",
                new String[]{username, String.valueOf(foodId)}, null, null, null);
        if (c.moveToFirst()) {
            // 已存在：数量+1
            int id = c.getInt(c.getColumnIndexOrThrow("_id"));
            int newCount = c.getInt(c.getColumnIndexOrThrow("count")) + 1;
            ContentValues cv = new ContentValues();
            cv.put("count", newCount);
            db.update(TABLE_CART, cv, "_id=?", new String[]{String.valueOf(id)});
        } else {
            // 不存在：新增
            ContentValues cv = new ContentValues();
            cv.put("username", username);
            cv.put("food_id", foodId);
            cv.put("food_name", foodName);
            cv.put("price", price);
            cv.put("count", 1);
            db.insert(TABLE_CART, null, cv);
        }
        c.close();
    }

    /** 加入购物车（指定数量） */
    public void addToCartWithCount(String username, int foodId, String foodName, double price, int count) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.query(TABLE_CART, null,
                "username=? AND food_id=?",
                new String[]{username, String.valueOf(foodId)}, null, null, null);
        if (c.moveToFirst()) {
            int id = c.getInt(c.getColumnIndexOrThrow("_id"));
            int newCount = c.getInt(c.getColumnIndexOrThrow("count")) + count;
            ContentValues cv = new ContentValues();
            cv.put("count", newCount);
            db.update(TABLE_CART, cv, "_id=?", new String[]{String.valueOf(id)});
        } else {
            ContentValues cv = new ContentValues();
            cv.put("username", username);
            cv.put("food_id", foodId);
            cv.put("food_name", foodName);
            cv.put("price", price);
            cv.put("count", count);
            db.insert(TABLE_CART, null, cv);
        }
        c.close();
    }

    /** 获取当前用户购物车列表 */
    public List<CartItem> getCartByUser(String username) {
        List<CartItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_CART, null, "username=?", new String[]{username}, null, null, null);
        while (c.moveToNext()) {
            CartItem item = new CartItem();
            item.setId(c.getInt(c.getColumnIndexOrThrow("_id")));
            item.setUsername(c.getString(c.getColumnIndexOrThrow("username")));
            item.setFoodId(c.getInt(c.getColumnIndexOrThrow("food_id")));
            item.setFoodName(c.getString(c.getColumnIndexOrThrow("food_name")));
            item.setPrice(c.getDouble(c.getColumnIndexOrThrow("price")));
            item.setCount(c.getInt(c.getColumnIndexOrThrow("count")));
            list.add(item);
        }
        c.close();
        return list;
    }

    /** 获取购物车总份数 */
    public int getCartTotalCount(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(count) FROM " + TABLE_CART + " WHERE username=?",
                new String[]{username});
        int total = 0;
        if (c.moveToFirst()) total = c.getInt(0);
        c.close();
        return total;
    }

    /** 获取购物车总价格 */
    public double getCartTotalPrice(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(price * count) FROM " + TABLE_CART + " WHERE username=?",
                new String[]{username});
        double total = 0;
        if (c.moveToFirst()) total = c.getDouble(0);
        c.close();
        return total;
    }

    /** 删除购物车中单个菜品 */
    public void removeCartItem(String username, int foodId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CART, "username=? AND food_id=?", new String[]{username, String.valueOf(foodId)});
    }

    /** 清空用户购物车 */
    public void clearCartByUser(String username) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CART, "username=?", new String[]{username});
    }

    // ==================== 订单相关操作 ====================

    /** 提交订单 */
    public void createOrder(String username, double totalPrice, String orderTime, String foodInfo) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("total_price", totalPrice);
        cv.put("order_time", orderTime);
        cv.put("food_info", foodInfo);
        db.insert(TABLE_ORDERS, null, cv);
    }

    /** 获取用户订单列表（按时间倒序） */
    public List<Order> getOrdersByUser(String username) {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_ORDERS, null, "username=?",
                new String[]{username}, null, null, "order_time DESC");
        while (c.moveToNext()) {
            Order order = new Order();
            order.setId(c.getInt(c.getColumnIndexOrThrow("_id")));
            order.setUsername(c.getString(c.getColumnIndexOrThrow("username")));
            order.setTotalPrice(c.getDouble(c.getColumnIndexOrThrow("total_price")));
            order.setOrderTime(c.getString(c.getColumnIndexOrThrow("order_time")));
            order.setFoodInfo(c.getString(c.getColumnIndexOrThrow("food_info")));
            list.add(order);
        }
        c.close();
        return list;
    }

    // ==================== 收货地址操作 ====================

    /** 保存地址 */
    public void saveAddress(String username, String detail) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username);
        cv.put("detail", detail);
        cv.put("is_default", getAddressCount(username) == 0 ? 1 : 0);
        db.insert(TABLE_ADDRESS, null, cv);
    }

    /** 获取用户所有地址 */
    public List<Address> getAddressesByUser(String username) {
        List<Address> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_ADDRESS, null, "username=?",
                new String[]{username}, null, null, "is_default DESC, _id DESC");
        while (c.moveToNext()) {
            Address a = new Address();
            a.setId(c.getInt(c.getColumnIndexOrThrow("_id")));
            a.setUsername(c.getString(c.getColumnIndexOrThrow("username")));
            a.setDetail(c.getString(c.getColumnIndexOrThrow("detail")));
            a.setIsDefault(c.getInt(c.getColumnIndexOrThrow("is_default")));
            list.add(a);
        }
        c.close();
        return list;
    }

    /** 删除地址 */
    public void deleteAddress(int addressId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_ADDRESS, "_id=?", new String[]{String.valueOf(addressId)});
    }

    /** 设置默认地址 */
    public void setDefaultAddress(String username, int addressId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues clear = new ContentValues();
        clear.put("is_default", 0);
        db.update(TABLE_ADDRESS, clear, "username=?", new String[]{username});
        ContentValues set = new ContentValues();
        set.put("is_default", 1);
        db.update(TABLE_ADDRESS, set, "_id=?", new String[]{String.valueOf(addressId)});
    }

    private int getAddressCount(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ADDRESS + " WHERE username=?", new String[]{username});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }
}
