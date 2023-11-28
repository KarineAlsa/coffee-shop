package com.example.cafeteria.utilities;

import java.util.List;

public class ListUtil {
    public static <T> void upsert(List<T> list, T element) {
        if (list.contains(element)) {
            list.remove(element);
            list.add(element);
        } else {
            list.add(element);
        }
    }
}
