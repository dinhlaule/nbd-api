package com.neo.nbdapi.rest.vm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectTypeVM {
    private String term;
    @JsonProperty("_type")
    private String type;
    private String typeId;
}
