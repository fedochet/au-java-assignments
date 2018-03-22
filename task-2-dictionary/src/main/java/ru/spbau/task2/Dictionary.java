package ru.spbau.task2;

import org.jetbrains.annotations.Nullable;

public interface Dictionary<K, V> {

    // хеш-таблица, использующая список
    // ключами и значениями выступают строки
    // стандартный способ получить хеш объекта -- вызвать у него метод hashCode()

    // кол-во ключей в таблице
    int size();

    // true, если такой ключ содержится в таблице
    boolean contains(@Nullable K key);

    // возвращает значение, хранимое по ключу key
    // если такого нет, возвращает null
    @Nullable
    V get(@Nullable K key);

    // положить по ключу key значение value
    // и вернуть ранее хранимое, либо null;
    // провести рехеширование по необходимости
    @Nullable
    V put(@Nullable K key, @Nullable V value);

    // забыть про пару key-value для переданного key
    // и вернуть забытое value, либо null, если такой пары не было;
    // провести рехеширование по необходимости
    @Nullable
    V remove(@Nullable K key);

    // забыть про все пары key-value
    void clear();
}