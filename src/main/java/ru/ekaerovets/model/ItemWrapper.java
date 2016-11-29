package ru.ekaerovets.model;

/**
 * User: dmitry.karyakin
 * Date: 29.11.2016
 */
public class ItemWrapper {

    private Item item;
    private char type;

    public ItemWrapper() {

    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }
}
