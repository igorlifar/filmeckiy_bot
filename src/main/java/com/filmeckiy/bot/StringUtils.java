package com.filmeckiy.bot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author egor
 */
public class StringUtils {
    private static final Logger logger = LogManager.getLogger(StringUtils.class);


    public static boolean good_Char(char c) {
        boolean b = false;
        if ((c >= 'A' && c <= 'Z') ||
                (c >= 'А' && c <= 'Я') ||
                (c >= 'a' && c <= 'z') ||
                (c >= 'а' && c <= 'я') ||
                (c >= '0' && c <= '9'))
        {
            b = true;
        }
        return b;
    }


    public static List<String> main(String s) {
        List<String> ans = new ArrayList<>();
        String s1 = "";
        for (int i = 0; i < s.length(); i++) {
            if (good_Char(s.charAt(i))) {
                s1 += s.charAt(i);
            }
            if ((i != 0 && !good_Char(s.charAt(i)) && good_Char(s.charAt(i - 1))) || i == s.length() - 1) {
                if (s1.length() != 0) {
                    ans.add(s1.toLowerCase());
                }
                s1 = "";
            }
        }
        return ans;
    }

    public static int levenshtein(String a, String b) {
        int[][] m = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                m[i][j] = 1000000;
            }
        }
        m[0][0] = 0;
        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (a.length() == i && b.length() == j) {
                    continue;
                }
                if (a.length() == i) {
                    m[i][j + 1] = Math.min(m[i][j + 1], m[i][j] + 1);
                    continue;
                }
                if (b.length() == j) {
                    m[i + 1][j] = Math.min(m[i + 1][j], m[i][j] + 1);
                    continue;
                }
                m[i + 1][j + 1] = Math.min(m[i + 1][j + 1], m[i][j] + 1);
                if (a.charAt(i) == b.charAt(j)) {
                    m[i + 1][j + 1] = Math.min(m[i + 1][j + 1], m[i][j]);
                }
                m[i + 1][j] = Math.min(m[i + 1][j], m[i][j] + 1);
                m[i][j + 1] = Math.min(m[i][j + 1], m[i][j] + 1);
            }
        }
        return m[a.length()][b.length()];
    }

    public static int nop(String a, String b, int maxk) {
        int[][][] m = new int[a.length() + 1][b.length() + 1][maxk + 1];
        int ans = 0;
        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                for (int k = 0; k <= maxk; k++) {
                    if (i == 0 || j == 0) {
                        continue;
                    }
                    if (a.charAt(i - 1) != b.charAt(j - 1)) {
                        if (k >= 1) {
                            m[i][j][k] = m[i - 1][j - 1][k - 1] + 1;
                        }
                    } else {
                        m[i][j][k] = m[i - 1][j - 1][k] + 1;
                    }
                    if (k >= 1) {
                        m[i][j][k] = Math.max(m[i][j][k], m[i][j - 1][k - 1]);
                        m[i][j][k] = Math.max(m[i][j][k], m[i - 1][j][k - 1]);
                    }
                    ans = Math.max(ans, m[i][j][k]);
                }
            }
        }
        return ans;
    }
}
