package ru.wars333.client.module;

public enum Category {
    VISUAL("Визуал"),
    PLAYER("Игрок"),
    SYSTEM("Система");

    public final String title;

    Category(String title) {
        this.title = title;
    }
}
