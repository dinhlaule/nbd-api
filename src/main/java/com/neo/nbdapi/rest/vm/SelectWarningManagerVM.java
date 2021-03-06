package com.neo.nbdapi.rest.vm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectWarningManagerVM implements Serializable {
    private String term;
    private String _type;
    @NotNull(message = "id không được để trống")
    private Long id;
    private String idStation;
}
