package com.neo.nbdapi.services;

import com.neo.nbdapi.dto.DefaultPaginationDTO;
import com.neo.nbdapi.dto.DefaultResponseDTO;
import com.neo.nbdapi.entity.ComboBox;
import com.neo.nbdapi.entity.WarningThreshold;
import com.neo.nbdapi.exception.BusinessException;
import com.neo.nbdapi.rest.vm.DefaultRequestPagingVM;
import com.neo.nbdapi.rest.vm.WarningThresholdValueVM;

import java.sql.SQLException;


public interface WarningThresholdStationService {
    DefaultPaginationDTO getListWarningThresholdStation(DefaultRequestPagingVM defaultRequestPagingVM) throws SQLException, BusinessException;
    public DefaultResponseDTO createWarningThreshold(WarningThresholdValueVM warningThresholdValueVM) throws SQLException;
    DefaultResponseDTO editWarningThreshold(WarningThresholdValueVM warningThresholdValueVM) throws SQLException;
    ComboBox getValueType(Long id) throws SQLException;
    public DefaultResponseDTO deleteWarningThresholdValue(Long id) throws SQLException;
}
