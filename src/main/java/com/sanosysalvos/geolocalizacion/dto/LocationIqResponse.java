package com.sanosysalvos.geolocalizacion.dto;

import lombok.Data;

@Data
public class LocationIqResponse {
    private String lat;
    private String lon;
    private String display_name;
}