package org.drw.model;

import lombok.Data;

@Data
public class GeoJSON {
    String type;
    Geometry geometry;
    GeoJSONProperties properties;
}
