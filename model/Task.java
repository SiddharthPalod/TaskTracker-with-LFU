package tasktracker.model;
import java.util.Date;
import java.util.Map;

import tasktracker.utils.JsonUtil;

public class Task {
    public int id;
    public String description;
    public Status status;
    public Date createdAt;
    public Date updatedAt;

    public String toJson() {
        return "{"
                + "\"id\":" + id + ","
                + "\"description\":\"" + JsonUtil.escape(description) + "\","
                + "\"status\":\"" + status + "\","
                + "\"createdAt\":" + createdAt.getTime() + ","
                + "\"updatedAt\":" + updatedAt.getTime()
                + "}";
    }

    public static Task fromJson(String json){
        Task t = new Task();
        Map<String, String> obj = JsonUtil.parseObject(json);

        t.id = Integer.parseInt(required(obj, "id"));
        t.description = required(obj, "description");
        t.status = Status.valueOf(required(obj, "status"));
        t.createdAt = new Date(Long.parseLong(required(obj, "createdAt")));
        t.updatedAt = new Date(Long.parseLong(required(obj, "updatedAt")));

        return t;
    }

    private static String required(Map<String, String> obj, String key) {
        String v = obj.get(key);
        if (v == null) throw new IllegalArgumentException("Missing field: " + key);
        return v;
    }
}