package com.yalin.googleio2016.explore.data;

import java.util.ArrayList;

/**
 * YaLin
 * 2016/11/30.
 * <p>
 * Data describing an Explore Event Card.
 */
public class EventData {
    private ArrayList<EventCard> mCards = new ArrayList<>();
    private String mTitle;

    public EventData() {
    }

    public void addEventCard(EventCard card) {
        mCards.add(card);
    }

    public ArrayList<EventCard> getCards() {
        return mCards;
    }

    public String getTitle() {
        return mTitle;
    }
}
