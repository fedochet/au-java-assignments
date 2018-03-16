package ru.spbau.task2;

import javax.annotation.Nullable;

public interface Dictionary {

    // хеш-таблица, использующая список
    // ключами и значениями выступают строки
    // стандартный способ получить хеш объекта -- вызвать у него метод hashCode()

    // кол-во ключей в таблице
    int size();

    // true, если такой ключ содержится в таблице
    boolean contains(@Nullable String key);

    // возвращает значение, хранимое по ключу key
    // если такого нет, возвращает null
    @Nullable
    String get(@Nullable String key);

    // положить по ключу key значение value
    // и вернуть ранее хранимое, либо null;
    // провести рехеширование по необходимости
    @Nullable
    String put(@Nullable String key, @Nullable String value);

    // забыть про пару key-value для переданного key
    // и вернуть забытое value, либо null, если такой пары не было;
    // провести рехеширование по необходимости
    @Nullable
    String remove(@Nullable String key);

    // забыть про все пары key-value
    void clear();
}