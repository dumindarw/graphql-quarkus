package org.drw.model;

import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Group {

    public String id;
    public String name;
    public GeoJSON location;
    public Boolean blacklisted;
    public String createdBy;
    public LocalDate createdDate;


}
