package com.neo.nbdapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailGroupConFigDTO implements Serializable {
    private String id;
    private String code;
    private String name;
    private String description;
    private String status;
    private List<Integer> userInSites;
    private List<Integer> warningConfig;

}
