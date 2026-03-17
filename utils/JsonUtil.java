package tasktracker.utils;

import java.util.HashMap;
import java.util.Map;

public class JsonUtil {
    public static String escape(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': out.append("\\\\"); break;
                case '"': out.append("\\\""); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default: out.append(c);
            }
        }
        return out.toString();
    }

    public static String unescape(String s) {
        if (s == null) return null;
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != '\\' || i + 1 >= s.length()) {
                out.append(c);
                continue;
            }
            char n = s.charAt(++i);
            switch (n) {
                case '\\': out.append('\\'); break;
                case '"': out.append('"'); break;
                case 'n': out.append('\n'); break;
                case 'r': out.append('\r'); break;
                case 't': out.append('\t'); break;
                default: out.append(n);
            }
        }
        return out.toString();
    }

    public static Map<String, String> parseObject(String json) {
        if (json == null) throw new IllegalArgumentException("json is null");
        int i = 0;
        json = json.trim();
        if (json.isEmpty() || json.charAt(0) != '{') throw new IllegalArgumentException("Invalid JSON object");

        Map<String, String> out = new HashMap<>();
        i++;
        while (i < json.length()) {
            i = skipWs(json, i);
            if (i >= json.length()) break;
            char c = json.charAt(i);
            if (c == '}') return out;
            if (c != '"') throw new IllegalArgumentException("Expected string key");

            ParsedString key = readString(json, i);
            i = skipWs(json, key.next);
            if (i >= json.length() || json.charAt(i) != ':') throw new IllegalArgumentException("Expected ':'");
            i++;

            i = skipWs(json, i);
            if (i >= json.length()) throw new IllegalArgumentException("Expected value");

            char vc = json.charAt(i);
            if (vc == '"') {
                ParsedString val = readString(json, i);
                out.put(key.value, val.value);
                i = val.next;
            } else {
                int start = i;
                while (i < json.length()) {
                    char ch = json.charAt(i);
                    if (ch == ',' || ch == '}' || Character.isWhitespace(ch)) break;
                    i++;
                }
                String raw = json.substring(start, i);
                out.put(key.value, raw);
            }

            i = skipWs(json, i);
            if (i < json.length() && json.charAt(i) == ',') {
                i++;
                continue;
            }
            if (i < json.length() && json.charAt(i) == '}') return out;
        }

        throw new IllegalArgumentException("Unterminated JSON object");
    }

    private static int skipWs(String s, int i) {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        return i;
    }

    private static ParsedString readString(String s, int i) {
        if (s.charAt(i) != '"') throw new IllegalArgumentException("Expected '\"'");
        i++; 
        StringBuilder out = new StringBuilder();
        while (i < s.length()) {
            char c = s.charAt(i++);
            if (c == '"') {
                return new ParsedString(out.toString(), i);
            }
            if (c == '\\') {
                if (i >= s.length()) throw new IllegalArgumentException("Bad escape");
                char n = s.charAt(i++);
                switch (n) {
                    case '\\': out.append('\\'); break;
                    case '"': out.append('"'); break;
                    case 'n': out.append('\n'); break;
                    case 'r': out.append('\r'); break;
                    case 't': out.append('\t'); break;
                    default: out.append(n);
                }
            } else {
                out.append(c);
            }
        }
        throw new IllegalArgumentException("Unterminated string");
    }

    private static class ParsedString {
        final String value;
        final int next;
        ParsedString(String value, int next) {
            this.value = value;
            this.next = next;
        }
    }
}
