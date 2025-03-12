package org.kdepo.solutions.mealplanner.tools;

import java.util.List;

public class DbUtils {

    public static String toArray(List<Integer> items) {
        StringBuilder sb = new StringBuilder();
        for (Integer i : items) {
            sb.append(i);
            sb.append(",");
        }
        String tmp = sb.toString();
        int lastIndex = tmp.lastIndexOf(',');
        if (tmp.length() - 1 == lastIndex) {
            tmp = tmp.substring(0, lastIndex);
        }
        return tmp;
    }

}
