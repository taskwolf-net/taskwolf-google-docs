package com.dulno.google.docs.structure;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class GoogleDocument {
  public static GoogleDocument of(String content) {
    var json = new JSONObject(content);
    return create(json.getString("id"), json.getString("name"));
  }

  private final String id;
  private final String name;

  public String toJson() {
    var information = Maps.newHashMap();
    information.put("id", id);
    information.put("name", name);
    return new JSONObject(information).toString();
  }
}
