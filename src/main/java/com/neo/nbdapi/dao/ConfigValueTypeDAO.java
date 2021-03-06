package com.neo.nbdapi.dao;

import com.neo.nbdapi.dto.ConfigStationsCommrelateDTO;
import com.neo.nbdapi.dto.ConfigValueTypeDTO;
import com.neo.nbdapi.dto.DefaultResponseDTO;
import com.neo.nbdapi.dto.StationValueTypeSpatialDTO;
import com.neo.nbdapi.entity.ComboBox;
import com.neo.nbdapi.entity.ComboBoxStr;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.SQLException;
import java.util.List;

public interface ConfigValueTypeDAO {
    List<ComboBox> getValueType(String stationId, Long valueTypeId) throws SQLException;
    List<ComboBox> getStationComboBox(String query) throws SQLException;
    List<ComboBoxStr> getStationComboBox(String query, String idStation) throws SQLException;
    StationValueTypeSpatialDTO getStationValueTypeSpatial( String idStation,  Long idValueType,String code) throws  SQLException;
    DefaultResponseDTO createConfigValuetype( ConfigValueTypeDTO configValueTypeDTO) throws Exception;
    List<StationValueTypeSpatialDTO> getStationValueTypeSpatials( Long idConfigValueTypeParent) throws  SQLException;
    DefaultResponseDTO editConfigValuetype(ConfigValueTypeDTO configValueTypeDTO, List<ConfigStationsCommrelateDTO> deletesSpatial, List<ConfigStationsCommrelateDTO> createSpatial) throws Exception;
    List<ConfigStationsCommrelateDTO> getListConfigStationsCommrelateDTO(Long parentId) throws Exception;
    DefaultResponseDTO deleteConfigValuetype(Long id) throws Exception;
    Boolean isInsert(ConfigValueTypeDTO configValueTypeDTO) throws Exception;

}
