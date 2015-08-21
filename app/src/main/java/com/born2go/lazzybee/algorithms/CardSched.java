package com.born2go.lazzybee.algorithms;

import com.born2go.lazzybee.db.Card;

/**
 * Created by Nobody on 7/14/2015.
 */
public class CardSched {

    final static int REVLOG_LRN = 0;
    final static int REVLOG_REV = 1;
    final static int REVLOG_RELRN = 2;
    final static int REVLOG_CRAM = 3;
    /**
     * Return the next interval for CARD as a string.
     */
    public final static int EASE_AGAIN = 0;
    public final static int EASE_HARD = 1;
    public final static int EASE_GOOD = 2;
    public final static int EASE_EASY = 3;

    protected static final int SECONDS_PERDAY = 86400;

    private static final int[] FACTOR_ADDITION_VALUES = { -300, -150, 0, 150 };
    private static final double BONUS_EASY = 1.4;
    private static final int MIN_FACTOR = 1300;

    public CardSched(){
    }

    /*
    Return string of next time to review corresponded to ease level
     */
    public String[] nextIvlStrLst(Card card) {
        String ret[] = new String[4];
        for (int i = 0; i < 4; i++){
            ret[i] = _nextIvlStr(card, i);
        }
        return ret;
    }
    /*
    Return string of next time to review corresponded to ease level
     */
    public String _nextIvlStr(Card card, int ease) {
        String str;
        int ivl = nextIvlBySeconds(card, ease);

        if (ivl < SECONDS_PERDAY)
            str =  "< 10min";
        else {
            int day = ivl / SECONDS_PERDAY;
            if (day <= 30)
                str = Math.round(day) + " day";
            else {
                double month = day / 30;
                str = month + " month";
                if (month > 12) {
                    double year = day / 365;
                    str = year + " year";
                }
            }
        }
        return str;
    }


    /**
     * Return the next interval for CARD, in seconds.
     */
    public int nextIvlBySeconds(Card card, int ease) {
        if (ease == EASE_AGAIN){
            return 600; /*10 minute*/
        }
        else {
            return _nextIntervalByDays(card, ease) * SECONDS_PERDAY;
        }
    }

    /**
     * Ideal next interval by days for CARD, given EASE > 0
     */
    public int _nextIntervalByDays(Card card, int ease) {
        assert (ease > EASE_AGAIN && ease <= EASE_EASY);

        long delay = _daysLate(card);
        int interval = 0;

        double fct = card.getFactor() / 1000.0;

        int ivl_hard = _constrainedIvl((int)((card.getLast_ivl() + delay/4) * 1.2), card.getLast_ivl());
        int ivl_good = _constrainedIvl((int)((card.getLast_ivl() + delay/2) * fct), ivl_hard);
        int ivl_easy = _constrainedIvl((int) ((card.getLast_ivl() + delay) * fct * BONUS_EASY), ivl_good);
        if (ease == EASE_HARD) {
            interval = ivl_hard;
        } else if (ease == EASE_GOOD) {
            interval = ivl_good;
        } else if (ease == EASE_EASY) {
            interval = ivl_easy;
        }
        // Should we maximize the interval?
        return interval;
    }

    /**
     * Number of days later than scheduled.
     * only for reviewing, not just learnt few minute ago
     */
    protected long _daysLate(Card card) {
        if(card.getQueue() != Card.QUEUE_REV2)
            return 0;

        long due = card.getDue();;
        long now = Utils.intNow();

        long diff_day = (now - due)/SECONDS_PERDAY;
        return Math.max(0, diff_day );
    }

    /** Integer interval after interval factor and prev+1 constraints applied */
    private int _constrainedIvl(int ivl, double prev) {
/*
        double newIvl = ivl;
        newIvl = ivl * conf.optDouble("ivlFct",1.0);
*/
        return (int) Math.max(ivl, prev + 1);
    }

    /**
    * Whenever a Card is answered, call this function on Card.
    * Scheduler will update the following parameters into Card's instance:
    * <ul>
    * <li>due
    * <li>last_ivl
    * <li>queue
    * <li>e_factor
    * <li>rev_count
    * </ul>
    * After 'answerCard', the caller will check Card's data for further decisions
     * (update database or/and put it back to app's queue)
    */
    public void answerCard(Card card, int ease){
        int nextIvl = nextIvlBySeconds(card, ease);
        card.increaseRevCount();

        long current = Utils.intNow();

        if (nextIvl < SECONDS_PERDAY) {
            /*User forget card or just learnt
            * We don't re-count 'due', because app will put it back to learnt queue
            * */
            card.setQueue(Card.QUEUE_LNR1);
            //Reset last-interval to reduce next review
            card.setLast_ivl(0);
        }
        else {
            card.setQueue(Card.QUEUE_REV2);
            card.setDue(current + nextIvl);
            card.setLast_ivl(_nextIntervalByDays(card, ease));
        }
        card.setFactor(Math.max(MIN_FACTOR, card.getFactor() + FACTOR_ADDITION_VALUES[ease]));
    }
}
