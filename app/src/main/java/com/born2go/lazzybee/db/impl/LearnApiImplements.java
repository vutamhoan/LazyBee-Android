package com.born2go.lazzybee.db.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.born2go.lazzybee.algorithms.WordEstimate;
import com.born2go.lazzybee.db.Card;
import com.born2go.lazzybee.db.DataBaseHelper;
import com.born2go.lazzybee.db.api.LearnApi;
import com.born2go.lazzybee.shared.LazzyBeeShare;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Hue on 7/8/2015.
 */
public class LearnApiImplements implements LearnApi {
    //Column name in database
    private static final String KEY_ID = "id";
    private static final String KEY_QUESTION = "question";
    private static final String KEY_ANSWERS = "answers";
    private static final String KEY_CATEGORIES = "categories";
    private static final String KEY_SUBCAT = "subcat";
    private static final String KEY_STATUS = "status";
    private static final String KEY_G_ID = "gid";
    private static final String KEY_RELATED = "related";
    private static final String KEY_TAGS = "tags";
    //Table name
    private static final String TABLE_VOCABULARY = "vocabulary";
    private static final String TABLE_SYSTEM = "system";
    private static final String TAG = "LearnApiImplements";
    private static final int STATUS_CARD_LEARN_TODAY = 1;
    private static final String QUEUE_LIST = "queue_List";
    private static final String KEY_SYSTEM = "key";
    private static final String KEY_SYSTEM_VALUE = "value";
    private static final int STATUS_NO_LEARN = -1;
    private static final String KEY_QUEUE = "queue";
    private static final String KEY_DUE = "due";
    private static final String KEY_REV_COUNT = "rev_count";
    private static final String KEY_LAT_IVL = "last_ivl";
    private static final String KEY_FACTOR = "e_factor";


    String inputPattern = "EEE MMM d HH:mm:ss zzz yyyy";

    String outputPattern = "dd-MM-yyyy";

    SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
    SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

    Context context;
    DataBaseHelper dataBaseHelper;

    public LearnApiImplements(Context context) {
        this.context = context;
        //init dataBaseHelper
        dataBaseHelper = new DataBaseHelper(context);
    }

    static int CARD_INDEX_ID = 0;
    static int CARD_INDEX_QUESTION = 1;
    static int CARD_INDEX_ANSWER = 2;
    static int CARD_INDEX_CATRGORIES = 3;
    static int CARD_INDEX_SUBCAT = 4;
    static int CARD_INDEX_TAGS = 5;
    static int CARD_INDEX_RELATED = 6;
    static int CARD_INDEX_GID = 7;
    static int CARD_INDEX_STATUS = 8;
    static int CARD_INDEX_QUEUE = 9;
    static int CARD_INDEX_PACKAGE = 10;
    static int CARD_INDEX_LEVEL = 11;
    static int CARD_INDEX_DUE = 12;
    static int CARD_INDEX_REV_COUNT = 13;
    static int CARD_INDEX_USER_NOTE = 14;
    static int CARD_INDEX_LAST_IVL = 15;
    static int CARD_INDEX_E_FACTOR = 16;


    /**
     * Get card by ID form sqlite
     *
     * @param cardId
     */
    @Override
    public Card _getCardByID(String cardId) {
        Card card = null;

        String selectbyIDQuery = "SELECT  * FROM " + TABLE_VOCABULARY + " WHERE " + KEY_ID + " = " + cardId;
        SQLiteDatabase db = this.dataBaseHelper.getReadableDatabase();

        //query for cursor
        Cursor cursor = db.rawQuery(selectbyIDQuery, null);
        if (cursor.moveToFirst()) {
            if (cursor.getCount() > 0)
                do {
                    card = new Card();
                    //get data from sqlite
                    card.setId(cursor.getInt(CARD_INDEX_ID));

                    card.setQuestion(cursor.getString(CARD_INDEX_QUESTION));
                    card.setAnswers(cursor.getString(CARD_INDEX_ANSWER));
                    card.setCategories(cursor.getString(CARD_INDEX_CATRGORIES));
                    card.setSubcat(cursor.getString(CARD_INDEX_SUBCAT));

                    card.setStatus(0);
                    card.setQueue(cursor.getInt(CARD_INDEX_QUEUE));
                    card.setPackage(cursor.getString(CARD_INDEX_PACKAGE));
                    card.setLevel(cursor.getInt(CARD_INDEX_LEVEL));
                    card.setDue(cursor.getLong(CARD_INDEX_DUE));

                    card.setRev_count(cursor.getInt(CARD_INDEX_REV_COUNT));
                    card.setUser_note(cursor.getString(CARD_INDEX_USER_NOTE));
                    card.setLast_ivl(cursor.getInt(CARD_INDEX_LAST_IVL));
                    card.setFactor(cursor.getInt(CARD_INDEX_E_FACTOR));

                    Log.i(TAG, card.toString());
                    System.out.print(card.toString());

                } while (cursor.moveToNext());
        }
        return card;
    }

    /**
     * Get list card from today
     */
    @Override
    public List<Card> _getListCardForToday() {
        return _getListCard();
    }

    /**
     * Get Review List Today
     * <p>List vocabulary complete in today</p>
     */
    @Override
    public List<Card> _getReviewListCard() {
        return _getListCard();
    }

    /**
     * Seach vocabulary
     *
     * @param query
     */
    @Override
    public List<Card> _searchCard(String query) {


        //select like query
        String likeQuery = "SELECT  * FROM " + TABLE_VOCABULARY + " WHERE " + KEY_QUESTION + " like '%" + query + "%'";

        //Todo:Seach card
        List<Card> datas = _getListCardQueryString(likeQuery);

        return datas;
    }

    /**
     * Get Random list card from today
     * Check List Card
     * Get random card->update List card and update statu card
     *
     * @param number
     */
    @Override
    public List<Card> _getRandomCard(int number, boolean learnmore) {
        SQLiteDatabase db = this.dataBaseHelper.getReadableDatabase();
        List<Card> datas = new ArrayList<Card>();

        //Check today list card
        int checkListToday = _checkListTodayExit();
        if (checkListToday > -1 && !learnmore) {

            //TODO: get data from sqlite
            String value = _getValueFromSystemByKey(QUEUE_LIST);
            datas = _getListCardFromStringArray(value);

        } else {

            //limit learn more =5 row
            if (learnmore == true)
                number = 5;

            //select random limit number row
            String selectRandomAndLimitQuery = "SELECT  * FROM " + TABLE_VOCABULARY + " where queue = 0 ORDER BY RANDOM() LIMIT " + number;

//            SQLiteDatabase db = this.dataBaseHelper.getReadableDatabase();
            //query for cursor
            Cursor cursor = db.rawQuery(selectRandomAndLimitQuery, null);
            if (cursor.moveToFirst()) {
                if (cursor.getCount() > 0)
                    do {
                        //get data from sqlite
                        Card card = new Card();
                        card.setId(cursor.getInt(CARD_INDEX_ID));

                        card.setQuestion(cursor.getString(CARD_INDEX_QUESTION));
                        card.setAnswers(cursor.getString(CARD_INDEX_ANSWER));
                        card.setCategories(cursor.getString(CARD_INDEX_CATRGORIES));
                        card.setSubcat(cursor.getString(CARD_INDEX_SUBCAT));

                        card.setQueue(cursor.getInt(CARD_INDEX_QUEUE));

                        card.setPackage(cursor.getString(CARD_INDEX_PACKAGE));
                        card.setLevel(cursor.getInt(CARD_INDEX_LEVEL));
                        card.setDue(cursor.getLong(CARD_INDEX_DUE));

                        card.setRev_count(cursor.getInt(CARD_INDEX_REV_COUNT));
                        card.setUser_note(cursor.getString(CARD_INDEX_USER_NOTE));
                        card.setLast_ivl(cursor.getInt(CARD_INDEX_LAST_IVL));
                        card.setFactor(cursor.getInt(CARD_INDEX_E_FACTOR));

                        Log.i(TAG, card.toString());
                        System.out.print(card.toString());

                        datas.add(card);
                        _updateStatusCard("" + card.getId(), STATUS_CARD_LEARN_TODAY);

                    } while (cursor.moveToNext());
            }
            //TODO: ADD QUEUE LIST TO SYSTEM
            _insertOrUpdateToSystemTable(QUEUE_LIST, _listCardTodayToArrayListCardId(datas, null));
        }


        return datas;
    }

    private String _listCardTodayToArrayListCardId(List<Card> datas, List<String> _listCardId) {
        String jsonValuestr = "";//Todo: init string json value
        JSONObject valueJoson = new JSONObject();

        Date nowdate = new Date();
        long now_date_long = nowdate.getTime();//get Time by @param nowdate

        //TODO: init ListCardID
        try {
            List<String> listCardId = new ArrayList<String>();
            if (_listCardId != null && !_listCardId.isEmpty()) {
                listCardId = _listCardId;
            } else {
                if (datas != null) {
                    for (Card card : datas) {
                        listCardId.add("" + card.getId());
                    }
                }
            }

            JSONArray cardIDArray = new JSONArray(listCardId);//TODO:init cardID Array

            //todo: put properties of @param valueJoson
            valueJoson.put("date", now_date_long);
            valueJoson.put("card", cardIDArray);

            jsonValuestr = valueJoson.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "jsonValuestr:" + jsonValuestr);
        return jsonValuestr;
    }

    public int _checkListTodayExit() {
        //TODO:get value queue List
        String value = _getValueFromSystemByKey(QUEUE_LIST);
        if (value == null) {
            //TODO: NO List Queue
            Log.i(TAG, "_checkListTodayExit:First Initial:Return=-2");
            return -2;
        } else {
            //TODO Yes,Compareto Date
            try {
                //TODO:Pass string value to object
                JSONObject valueObj = new JSONObject(value);

                //TODO:get date create list today
                long _longQueueDate = (valueObj.getLong("date"));//get Long date
                JSONArray listIdArray = valueObj.getJSONArray("card");//get List card ID
                Log.i(TAG, "-Long date:" + _longQueueDate);

                Date _date = new Date(_longQueueDate);
                //new date
                Date _nowDate = new Date();

                int countListId = listIdArray.length();

                //TODO: format date to string
                String today_parse = outputFormat.format(_date);
                String str_date_now = outputFormat.format(_nowDate);

                //TODO: compareTo date learn vs now date
                if (today_parse.compareTo(str_date_now) == 0) {

                    Log.i(TAG, "_checkListTodayExit:today_parse is equal to date_now");

                    return countListId;
                } else {

                    Log.i(TAG, "_checkListTodayExit:today_parse is not equal to date_now");

                    return -1;
                }
            } catch (JSONException e) {
                Log.i(TAG, "_checkListTodayExit:Error Return=" + -1);
                e.printStackTrace();
                return -1;
            }
        }


    }

    /**
     * _get List Card By List CardId JsonArray
     *
     * @param cardListIDArray JsonArray String
     * @return list card
     */
    private List<Card> _getListCardByListCardIdJsonArray(JSONArray cardListIDArray) {
        //TODO:


        List<Card> cardList = new ArrayList<Card>();//init Card List

        int lengh = cardListIDArray.length();//get lenght

        for (int i = 0; i < lengh; i++) {
            try {

                String cardId = cardListIDArray.getString(i);//get CardId by index

                Card card = _getCardByID(cardId);//get card by @param cardId

                cardList.add(card);//Add card to list

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return cardList;
    }

    /**
     * _export to SqlIte form ListCard
     *
     * @param cardList
     * @return 1 if _export complete else 2 to false
     */
    @Override
    public int _export(List<Card> cardList) {
        return 0;
    }

    /**
     * _updateListCardByStatus to SqlIte form ListCard
     *
     * @param cardList
     * @param status
     * @return 1 if update complete else -1 false
     */
    @Override
    public int _updateListCardByStatus(List<Card> cardList, int status) {
        return 0;
    }

    /**
     * _updateCompleteCard to SqlIte form System Table
     *
     * @param cardId
     * @return 1 if update complete else -1 false
     */
    @Override
    public int _updateCompleteCard(String cardId) {
        return 0;
    }

    /**
     * _updateQueueCard to SqlIte form System Table
     *
     * @param cardId
     * @param queue
     * @return 1 if update complete else -1 false
     */
    @Override
    public int _updateQueueCard(String cardId, long queue) {

        //TODO: Update staus card by id
        SQLiteDatabase db = this.dataBaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();


        values.put(KEY_QUEUE, queue);//put Status

        //
        int update_result = db.update(TABLE_VOCABULARY, values, KEY_ID + " = ?",
                new String[]{String.valueOf(cardId)});
        Log.i(TAG, "Update Queue Card Complete: Update Result Code:" + update_result);
        return update_result;
    }

    /**
     * _insertListTodayCard to SqlIte form System Table
     *
     * @param cardList
     * @return 1 if update complete else -1 false
     */
    @Override
    public int _insertListTodayCard(List<Card> cardList) {
        List<String> listCardId = new ArrayList<String>();
        for (Card card : cardList) {
            listCardId.add("" + card.getId());
        }
        Date date = new Date();
        JSONObject valueObj = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray(listCardId);
            valueObj.put("date", date.getTime());
            valueObj.put("card", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Quere Obj:" + valueObj.toString());
        return 0;
    }


    /**
     *
     * */
    private List<Card> _getListCard() {
        //select query
        String selectQuery = "SELECT  * FROM " + TABLE_VOCABULARY;
        //select limit 5 row
        String selectLimitQuery = "SELECT  * FROM " + TABLE_VOCABULARY + " LIMIT 5 ";

        //TODO query select List Card by status=learned
        String selectListCardByStatus = "SELECT  * FROM " + TABLE_VOCABULARY + " where status = 1 ";
        List<Card> datas = _getListCardQueryString(selectQuery);
        return datas;
    }

    /**
     * Update Status Card and Time Again Lean
     */
    public int updateStatusAndTimeAgainVocabulary(Card card) {
        SQLiteDatabase db = this.dataBaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_STATUS, card.getStatus());
        return db.update(TABLE_VOCABULARY, values, KEY_ID + " = ?",
                new String[]{String.valueOf(card.getId())});

    }


    @Override
    public int _updateStatusCard(String cardId, int status) {
        //TODO: Update staus card by id
        SQLiteDatabase db = this.dataBaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_STATUS, status);//put Status

        //
        int update_result = db.update(TABLE_VOCABULARY, values, KEY_ID + " = ?",
                new String[]{String.valueOf(cardId)});
        Log.i(TAG, "Update Status Card Complete: Update Result Code:" + update_result);
        return update_result;

    }

    /**
     * add to system config
     * Key and value JSON
     *
     * @param key   key of system
     * @param value format json string
     * @return 1 if update complete else -1 false
     */
    @Override
    public int _insertOrUpdateToSystemTable(String key, String value) {
        Log.i(TAG, "value:" + value);


        //TODO check value by key
        String valuebyKey = _getValueFromSystemByKey(key);
        if (_getValueFromSystemByKey(key) == null) {
            Log.i(TAG, "Insert list card");
            //Todo: No,Then insert
            ContentValues values = new ContentValues();

            //TODO put value
            values.put(KEY_SYSTEM, key);
            values.put(KEY_SYSTEM_VALUE, value);

            //TODO insert to system table
            SQLiteDatabase db_insert = this.dataBaseHelper.getWritableDatabase();
            long long_insert_results = db_insert.insert(TABLE_SYSTEM, null, values);
            Log.i(TAG, "Insert Results:" + long_insert_results);
            db_insert.close();
            return (int) long_insert_results;
        } else {
            Log.i(TAG, "Update list card today:" + valuebyKey);
            //Todo: Yes,update for key
            ContentValues values = new ContentValues();
            //TODO put value
            values.put(KEY_SYSTEM_VALUE, value);
            //TODO update to system table
            SQLiteDatabase db_update = this.dataBaseHelper.getWritableDatabase();
            try {
                int update_results = db_update.update(TABLE_SYSTEM, values, KEY_SYSTEM + " = ?",
                        new String[]{String.valueOf(key)});
                Log.i(TAG, "update_results:" + update_results);
                String valuebyKeyafterUpdate = _getValueFromSystemByKey(key);
                Log.i(TAG, "valuebyKeyafterUpdate:" + valuebyKeyafterUpdate);
                return update_results;
            } catch (Exception e) {
                e.printStackTrace();
                return -1;
            }

        }

    }

    @Override
    public String _getValueFromSystemByKey(String key) {
        String queue_List_value = null;

        String selectValueByKey = "SELECT value FROM " + TABLE_SYSTEM + " where key = '" + key + "'";

        SQLiteDatabase db = this.dataBaseHelper.getReadableDatabase();
        try {
            //Todo query for cursor
            Cursor cursor = db.rawQuery(selectValueByKey, null);
            if (cursor.moveToFirst()) {
                if (cursor.getCount() > 0)
                    do {
                        //TODO:get data from sqlite
                        String value = cursor.getString(0);
                        queue_List_value = value;
                    } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return queue_List_value;
    }


    List<Card> _getListCardFromStringArray(String value) {
        List<Card> cardList = new ArrayList<Card>();
        try {
            //Pass string value to object
            JSONObject valueObj = new JSONObject(value);
            JSONArray listIdArray = valueObj.getJSONArray("card");//get List card ID

            for (int i = 0; i < listIdArray.length(); i++) {
                String cardId = listIdArray.getString(i);

                //TODO:get Card by id
                Card card = _getCardByID(cardId);

                cardList.add(card);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cardList;
    }

    List<String> _getListCardIdFromStringArray(String value) {
        List<String> cardListId = new ArrayList<String>();
        try {
            //Pass string value to object
            JSONObject queueObj = new JSONObject(value);
            JSONArray listIdArray = queueObj.getJSONArray("card");//get List card ID

            for (int i = 0; i < listIdArray.length(); i++) {

                String cardId = listIdArray.getString(i);
                cardListId.add(cardId);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cardListId;
    }

    @Override
    public List<Card> _getListCardByStatus(int status) {

        //Query select_list_card_by_status
        String select_list_card_by_status = "SELECT  * FROM " + TABLE_VOCABULARY + " where status = " + status;

        //Get card list by status
        List cardListByStatus = _getListCardQueryString(select_list_card_by_status);
        return cardListByStatus;
    }

    /**
     * Get List Card by queue
     *
     * @param queue
     */
    @Override
    public List<Card> _getListCardByQueue(int queue) {
//
//        if (queue <= 600l) {

        //get current time
        long long_curent_time = new Date().getTime();

        int curent_time = (int) (long_curent_time / 1000);
        Log.i(TAG, "Current Time:" + curent_time + ":" + new Date().getTime());

        String select_list_card_by_queue = "";
        int limit = 20;
        if (queue == Card.QUEUE_LNR1)
            //Query select_list_card_by_queue
            select_list_card_by_queue = "SELECT  * FROM " + TABLE_VOCABULARY + " where queue = " + queue;
        else if (queue == Card.QUEUE_REV2) {

            int countToday = _checkListTodayExit();

            if (countToday == -1)
                limit = LazzyBeeShare.TOTTAL_LEAN_PER_DAY;
            else if (countToday > -1)
                limit = LazzyBeeShare.TOTTAL_LEAN_PER_DAY - countToday;

            select_list_card_by_queue = "SELECT  * FROM " + TABLE_VOCABULARY + " where queue = " + queue + " AND due < " + curent_time + " LIMIT " + limit;
        } else {
            select_list_card_by_queue = "SELECT  * FROM " + TABLE_VOCABULARY + " where queue = " + queue + " LIMIT " + limit;
        }


        //Get card list by status
        List<Card> cardListByQueue = _getListCardQueryString(select_list_card_by_queue);
        return cardListByQueue;
//        } else {
//            //select query
//            String selectQuery = "SELECT  * FROM " + TABLE_VOCABULARY + " where queue > 600";
//
//            //
//            Date now_date = new Date(queue);
//
//            //Get card list by status
//            List<Card> cardListAll = _getListCardQueryString(selectQuery);
//
//
//            List<Card> cardListDueToday = new ArrayList<Card>();
//            //TODO: Check queue date
//            for (Card card : cardListAll) {
//                //TODO:CompateTo date_due vs now date
//                Date card_due_date = new Date(card.getQueue());
//                if (_compreaToDate(card_due_date, now_date)) {
//                    cardListDueToday.add(card);
//                }
//
//            }
//            return cardListDueToday;
//        }


    }


    /**
     * Update queue and due card
     *
     * @param cardId
     * @param queue  queue
     * @param due    due time review card
     */
    @Override
    public int _updateCardQueueAndCardDue(String cardId, int queue, int due) {
        //TODO: Update staus card by id
        SQLiteDatabase db = this.dataBaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();


        values.put(KEY_QUEUE, queue);//put Status
        values.put(KEY_DUE, due);
        //
        int update_result = db.update(TABLE_VOCABULARY, values, KEY_ID + " = ?",
                new String[]{String.valueOf(cardId)});
        Log.i(TAG, "Update Queue Card Complete: Update Result Code:" + update_result);
        return update_result;
    }

    private boolean _compreaToDate(Date card_due_date, Date now_date) {
        try {
            Date date_compateTo = inputFormat.parse(card_due_date.toString());
            Date date_now = inputFormat.parse(now_date.toString());

            //TODO: format date to string
            String str_date_create_list_card_today_parse = outputFormat.format(date_compateTo);
            String str_date_now = outputFormat.format(date_now);

            //TODO: compareTo date learn vs now date
            if (str_date_create_list_card_today_parse.compareTo(str_date_now) == 0) {
                //TODO: Equal then return true
                Log.i(TAG, "date_compateTo is equal to date_now");

                return true;
            } else {
                //TODO: Not equal then return false
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
     *
     * */
    private List<Card> _getListCardQueryString(String query) {
        List<Card> datas = new ArrayList<Card>();
        SQLiteDatabase db = this.dataBaseHelper.getReadableDatabase();
        //query for cursor
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            if (cursor.getCount() > 0)
                do {
                    Card card = new Card();
                    card.setId(cursor.getInt(CARD_INDEX_ID));

                    card.setQuestion(cursor.getString(CARD_INDEX_QUESTION));
                    card.setAnswers(cursor.getString(CARD_INDEX_ANSWER));
                    card.setCategories(cursor.getString(CARD_INDEX_CATRGORIES));
                    card.setSubcat(cursor.getString(CARD_INDEX_SUBCAT));

                    card.setStatus(0);
                    card.setQueue(cursor.getInt(CARD_INDEX_QUEUE));
                    card.setPackage(cursor.getString(CARD_INDEX_PACKAGE));
                    card.setLevel(cursor.getInt(CARD_INDEX_LEVEL));
                    card.setDue(cursor.getLong(CARD_INDEX_DUE));

                    card.setRev_count(cursor.getInt(CARD_INDEX_REV_COUNT));
                    card.setUser_note(cursor.getString(CARD_INDEX_USER_NOTE));
                    card.setLast_ivl(cursor.getInt(CARD_INDEX_LAST_IVL));
                    card.setFactor(cursor.getInt(CARD_INDEX_E_FACTOR));


                    datas.add(card);

                } while (cursor.moveToNext());
        }
        Log.i(TAG, "Query String: " + query + " --Result card count:" + datas.size());
        return datas;
    }

    /**
     * Update card
     *
     * @param card
     */
    @Override
    public int _updateCard(Card card) {
        String cardId = String.valueOf(card.getId());

        //TODO: Update staus card by id
        SQLiteDatabase db = this.dataBaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();


        values.put(KEY_QUEUE, card.getQueue());

        if (card.getDue() != 0)
            values.put(KEY_DUE, card.getDue());
        if (card.getLast_ivl() != 0)
            values.put(KEY_LAT_IVL, card.getLast_ivl());
        if (card.getRev_count() != 0)
            values.put(KEY_REV_COUNT, card.getRev_count());
        if (card.getFactor() != 0)
            values.put(KEY_FACTOR, card.getFactor());


        //
        int update_result = db.update(TABLE_VOCABULARY, values, KEY_ID + " = ?",
                new String[]{cardId});
        Log.i(TAG, "Update Queue Card Complete: Update Result Code:" + update_result);

        //TODO:Update queue_list system table
        String queue_list = _getValueFromSystemByKey(QUEUE_LIST);

        //Get Card list id form system tabele
        List<String> cardListId = _getListCardIdFromStringArray(queue_list);

        //Check cardListId.contains(cardId)==true remeve carId
        if (cardListId.contains(cardId)) {
            cardListId.remove(cardId);

            //update queue list
            _insertOrUpdateToSystemTable(QUEUE_LIST, _listCardTodayToArrayListCardId(null, cardListId));
        }


        return update_result;
    }


    public String _getStringDueToday() {
        String duetoday = LazzyBeeShare.EMPTY;
        int todayCount = _checkListTodayExit();
        int againCount = _getListCardByQueue(Card.QUEUE_LNR1).size();
        int dueCount = _getListCardByQueue(Card.QUEUE_REV2).size();
        if (todayCount == -2) {
            dueCount = 0;
            againCount = 0;
            todayCount = LazzyBeeShare.MAX_NEW_LEARN_PER_DAY;
        } else {
            if (todayCount == 0) {
                //Complete leanrn today
                if (dueCount > LazzyBeeShare.TOTTAL_LEAN_PER_DAY)
                    dueCount = LazzyBeeShare.TOTTAL_LEAN_PER_DAY;
                todayCount = 0;
            } else if (todayCount == -1) {
                todayCount = LazzyBeeShare.MAX_NEW_LEARN_PER_DAY;
                if (dueCount > LazzyBeeShare.TOTTAL_LEAN_PER_DAY - todayCount)
                    dueCount = LazzyBeeShare.TOTTAL_LEAN_PER_DAY - todayCount;
            } else {
                Log.i(TAG, "Today:" + todayCount);
            }
        }

        duetoday = todayCount + " " + againCount + " " + dueCount;
        return duetoday;
    }

    public List<Card> _getAllListCard() {
        String query = "SELECT  * FROM " + TABLE_VOCABULARY;
        List<Card> cardList = _getListCardQueryString(query);
        return cardList;
    }

    public List<Card> _getListCardLearned() {
        String query = "SELECT  * FROM " + TABLE_VOCABULARY + " where queue >= 1";
        List<Card> cardList = _getListCardQueryString(query);
        return cardList;
    }

    public int _checkCompleteLearned() {
        int again = _getListCardByQueue(Card.QUEUE_LNR1).size();
        int due = _getListCardByQueue(Card.QUEUE_REV2).size();
        int today = _checkListTodayExit();

        Log.i(TAG, today + ":" + again + ":" + due);

        if (today > 0 || again > 0 || due > 0 || today == -1 || today == -2) {
            return 1;
        } else {
            return 0;
        }


    }

    @Override
    public List<Card> _get100Card() {
        List<Card> cards = new ArrayList<Card>();
        WordEstimate wordEstimate = new WordEstimate();
        int number[] = wordEstimate.getNumberWordEachLevel(0d);

        for (int i = 1; i < number.length; i++) {
            int limit = number[i];
            //SELECT  * FROM vovabulary where queue = 0 AND level = " + i + " LIMIT " + number[i]
            String select_list_card_by_queue = "SELECT  * FROM " + TABLE_VOCABULARY + " where queue = " + Card.QUEUE_NEW_CRAM0 + " AND level = " + i + " LIMIT " + limit;

            List<Card> cardListbylevel = _getListCardQueryString(select_list_card_by_queue);

            int count = cardListbylevel.size();
            // if count < number[i] else limit=(number[i]-count)+number[i+1]
            if (count < number[i]) {

                limit = (number[i] - count) + number[i + 1];
            }
            Log.i(TAG, "_get100Card: LIMIT=" + limit);


            cards.addAll(cardListbylevel);
        }

        Log.i(TAG, "_get100Card: Card size=" + cards.size());
        return cards;
    }
}
