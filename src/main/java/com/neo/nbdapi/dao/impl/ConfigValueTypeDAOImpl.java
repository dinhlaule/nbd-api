package com.neo.nbdapi.dao.impl;

import com.neo.nbdapi.dao.ConfigValueTypeDAO;
import com.neo.nbdapi.dto.ConfigStationsCommrelateDTO;
import com.neo.nbdapi.dto.ConfigValueTypeDTO;
import com.neo.nbdapi.dto.DefaultResponseDTO;
import com.neo.nbdapi.dto.StationValueTypeSpatialDTO;
import com.neo.nbdapi.entity.ComboBox;
import com.neo.nbdapi.entity.ComboBoxStr;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ConfigValueTypeDAOImpl implements ConfigValueTypeDAO {

    private Logger logger = LogManager.getLogger(ConfigValueTypeDAOImpl.class);

    @Autowired
    private HikariDataSource ds;

    @Override
    public List<ComboBox> getValueType(String stationId, Long valueTypeId) throws SQLException {
        List<ComboBox> comboBoxes = new ArrayList<>();
        String sql = "select c.PARAMETER_TYPE_ID, v.parameter_type_code , v.parameter_type_name, c.code from config_value_types c inner join parameter_type v on c.PARAMETER_TYPE_ID = v.parameter_type_id where c.station_id = ? and c.PARAMETER_TYPE_ID = ?";
        try (Connection connection = ds.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
            statement.setString(1, stationId);
            statement.setLong(2, valueTypeId);
            ResultSet resultSet = statement.executeQuery();
            ComboBox comboBox = null;
            while (resultSet.next()) {
                comboBox = ComboBox.builder().id(resultSet.getLong("parameter_type_id"))
                        .text(resultSet.getString("parameter_type_code") + "-" + resultSet.getString("parameter_type_name") + "-" + resultSet.getString("code")).build();
                comboBoxes.add(comboBox);
            }
            return comboBoxes;
        }
    }

    @Override
    public List<ComboBox> getStationComboBox(String query) throws SQLException {
        String sql = "select distinct c.station_id as id, s.station_code as code, s.station_name as name from stations s inner join config_value_types c on s.station_id = c.station_id where 1 = 1";
        if (query != null && !query.equals("")) {
            sql = sql + " and station_name like ? and ISDEL = 0 and STATUS = 1";
        }
        sql = sql + " and rownum < 100";
        try (Connection connection = ds.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
            if (query != null && !query.equals("")) {
                statement.setString(1, "%" + query + "%");
            }
            ResultSet resultSet = statement.executeQuery();
            List<ComboBox> comboBoxes = new ArrayList<>();
            while (resultSet.next()) {
                ComboBox comboBox = ComboBox.builder().id(resultSet.getLong(1)).text(resultSet.getString(2) + "-" + resultSet.getString(3)).build();
                comboBoxes.add(comboBox);
            }
            statement.close();
            return comboBoxes;
        }
    }

    @Override
    public List<ComboBoxStr> getStationComboBox(String query, String idStation) throws SQLException {
        String sql = "select distinct c.station_id as id, s.station_code as code, s.station_name as name from stations s  inner join config_value_types c on s.station_id = c.station_id " +
                "inner join stations_object_type t on t.station_id = s.station_id where t.object_type_id in (select st.object_type_id from stations_object_type st where st.station_id = ?) " +
                "and ISDEL = 0 and IS_ACTIVE = 1 and c.station_id != ? ";

        if (query != null && !query.equals("")) {
            sql = sql + " and UPPER(station_name) like ?";
        }
        sql = sql + " and rownum < 100";
        try (Connection connection = ds.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
            if (query != null && !query.equals("")) {
                statement.setString(1, idStation);
                statement.setString(2, idStation);
                statement.setString(3, "%" + query.toUpperCase() + "%");
            } else {
                statement.setString(1, idStation);
                statement.setString(2, idStation);
            }
            ResultSet resultSet = statement.executeQuery();
            List<ComboBoxStr> comboBoxes = new ArrayList<>();
            while (resultSet.next()) {
                ComboBoxStr comboBox = ComboBoxStr.builder().id(resultSet.getString(1)).text(resultSet.getString(2) + "-" + resultSet.getString(3)).build();
                comboBoxes.add(comboBox);
            }
            statement.close();
            return comboBoxes;
        }
    }

    @Override
    public StationValueTypeSpatialDTO getStationValueTypeSpatial(String idStation, Long idValueType, String code) throws SQLException {
        StationValueTypeSpatialDTO stationValueTypeSpatialDTO = new StationValueTypeSpatialDTO();
        String sql = "select c.id, c.station_id,s.station_code, s.station_name , c.parameter_type_id, v.parameter_type_code, v.parameter_type_name , c.variable_spatial, c.code from config_value_types c inner join stations s on s.station_id = c.station_id inner join parameter_type v on v.parameter_type_id = c.parameter_type_id where c.station_id = ? and c.parameter_type_id = ? and c.code = ?";
        try (Connection connection = ds.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
            statement.setString(1, idStation);
            statement.setLong(2, idValueType);
            statement.setString(3, code);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                stationValueTypeSpatialDTO = StationValueTypeSpatialDTO.
                        builder().id(resultSet.getLong("id"))
                        .stationId(resultSet.getString("station_id"))
                        .stationCode(resultSet.getString("station_code"))
                        .stationName(resultSet.getString("station_name"))
                        .valueTypeId(resultSet.getLong("parameter_type_id"))
                        .valueTypeCode(resultSet.getString("parameter_type_code"))
                        .valueTypeName(resultSet.getString("parameter_type_name"))
                        .variableSpatial(resultSet.getInt("variable_spatial"))
                        .code(resultSet.getString("code"))
                        .build();
            }
            return stationValueTypeSpatialDTO;
        }
    }

    @Override
    public DefaultResponseDTO createConfigValuetype(ConfigValueTypeDTO configValueTypeDTO) throws Exception {
        String sqlInsertConfig = "insert into config_value_types (ID,STATION_ID,PARAMETER_TYPE_ID,MIN,MAX,VARIABLE_TIME,VARIABLE_SPATIAL,START_APPLY_DATE,END_APPLY_DATE,CODE) values(config_value_types_seq.nextval,?,?,?,?,?,?,?,?,?)";
        String sqlInsertSpatial = "insert into config_stations_commrelate (ID, CONFIG_VALUE_TYPES_ID,CONFIG_VALUE_TYPES_PARENT) values (config_stations_commrelate_seq.nextval, ?,?)";
        String sqlGetCurrentId = "SELECT  config_value_types_seq.CURRVAL FROM dual";
        try (Connection connection = ds.getConnection();
             PreparedStatement stmInsertConfig = connection.prepareStatement(sqlInsertConfig);
             PreparedStatement stmGetCurrentId = connection.prepareStatement(sqlGetCurrentId);
             PreparedStatement stmInsertSpatial = connection.prepareStatement(sqlInsertSpatial);
             ) {
            // th???c hi???n insert
            connection.setAutoCommit(false);
            //check tr??ng kho???ng th???i gian
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            // th??m c???u h??nh tr???m m???i
            stmInsertConfig.setString(1, configValueTypeDTO.getStationId());
            stmInsertConfig.setLong(2, configValueTypeDTO.getValueTypeId());
            stmInsertConfig.setFloat(3, configValueTypeDTO.getMin());
            stmInsertConfig.setFloat(4, configValueTypeDTO.getMax());
            if (configValueTypeDTO.getVariableTime() == null) {
                stmInsertConfig.setNull(5, Types.FLOAT);
            } else {
                stmInsertConfig.setFloat(5, configValueTypeDTO.getVariableTime());
            }
            if (configValueTypeDTO.getVariableSpatial() == null) {
                stmInsertConfig.setNull(6, Types.FLOAT);
            } else {
                stmInsertConfig.setFloat(6, configValueTypeDTO.getVariableSpatial());
            }
            if (configValueTypeDTO.getStartDateApply() != null) {
                stmInsertConfig.setDate(7, new Date(df.parse(configValueTypeDTO.getStartDateApply()).getTime()));
            } else {
                stmInsertConfig.setDate(7, null);
            }
            if (configValueTypeDTO.getEndDateApply() != null)
                stmInsertConfig.setDate(8, new Date(df.parse(configValueTypeDTO.getEndDateApply()).getTime()));
            else
                stmInsertConfig.setDate(8, null);
            stmInsertConfig.setString(9, configValueTypeDTO.getCode());
            stmInsertConfig.executeUpdate();
            Long idStationParent = null;
            ResultSet resultSet = stmGetCurrentId.executeQuery();
            if (resultSet.next()) {
                idStationParent = resultSet.getLong(1);
            } else {
                throw new Exception();
            }

            Long[] configSpatial = configValueTypeDTO.getStationSpatial();
            if (configSpatial.length > 0) {
                for (int i = 0; i < configSpatial.length; i++) {
                    stmInsertSpatial.setLong(1, configSpatial[i]);
                    stmInsertSpatial.setLong(2, idStationParent);
                    stmInsertSpatial.addBatch();
                }
                stmInsertSpatial.executeBatch();

            }
            stmInsertSpatial.close();
            stmInsertConfig.close();
            stmGetCurrentId.close();
            connection.commit();
        } catch (SQLIntegrityConstraintViolationException e) {
            return DefaultResponseDTO.builder().status(0).message("M?? qu???n l?? chu???n h??a s??? li???u ???? t???n t???i").build();
        }
        return DefaultResponseDTO.builder().status(1).message("Th??nh c??ng").build();
    }

    @Override
    public List<StationValueTypeSpatialDTO> getStationValueTypeSpatials(Long idConfigValueTypeParent) throws SQLException {
        List<StationValueTypeSpatialDTO> stationValueTypeSpatialDTOs = new ArrayList<>();
        String sql = "select ct.id, s.station_id, s.station_code, s.station_name, v.parameter_type_id, v.parameter_type_code, v.parameter_type_name, ct.variable_spatial, ct.code  from config_stations_commrelate c inner join config_value_types ct on ct.id = c.config_value_types_id inner join stations s on ct.station_id = s.station_id inner join parameter_type v on v.parameter_type_id = ct.parameter_type_id where c.config_value_types_parent  = ?";
        try (Connection connection = ds.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
            statement.setLong(1, idConfigValueTypeParent);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                StationValueTypeSpatialDTO stationValueTypeSpatialDTO = StationValueTypeSpatialDTO.
                        builder().id(resultSet.getLong("id"))
                        .stationId(resultSet.getString("station_id"))
                        .stationCode(resultSet.getString("station_code"))
                        .stationName(resultSet.getString("station_name"))
                        .valueTypeId(resultSet.getLong("parameter_type_id"))
                        .valueTypeCode(resultSet.getString("parameter_type_code"))
                        .valueTypeName(resultSet.getString("parameter_type_name"))
                        .variableSpatial(resultSet.getInt("variable_spatial"))
                        .code(resultSet.getString("code"))
                        .build();
                stationValueTypeSpatialDTOs.add(stationValueTypeSpatialDTO);
            }
            return stationValueTypeSpatialDTOs;
        }
    }


    @Override
    public DefaultResponseDTO editConfigValuetype(ConfigValueTypeDTO configValueTypeDTO, List<ConfigStationsCommrelateDTO> deletesSpatials, List<ConfigStationsCommrelateDTO> createSpatials) throws Exception {
        Connection connection = ds.getConnection();
        String sqlUpdateConfig = "update config_value_types set MIN = ? , MAX = ? , VARIABLE_TIME = ? , VARIABLE_SPATIAL = ?, START_APPLY_DATE = TO_DATE(?, 'dd/mm/yyy'), END_APPLY_DATE = TO_DATE(?, 'dd/mm/yyy'), CODE = ? where ID = ?";
        String sqlDeleteSpatial = "delete from config_stations_commrelate where id  = ?";
        String sqlCreateSpatial = "insert into config_stations_commrelate (ID, CONFIG_VALUE_TYPES_ID,CONFIG_VALUE_TYPES_PARENT) values (config_stations_commrelate_seq.nextval, ?,?)";
        PreparedStatement stmUpdateConfig = null;
        PreparedStatement stmDeleteSpatial = null;
        PreparedStatement stmCreateSpatial = null;
        try {
            connection.setAutoCommit(false);
            stmUpdateConfig = connection.prepareStatement(sqlUpdateConfig);
            stmDeleteSpatial = connection.prepareStatement(sqlDeleteSpatial);
            stmCreateSpatial = connection.prepareStatement(sqlCreateSpatial);
            // s???a c??c th??ng tin c???a c???u h??nh
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            stmUpdateConfig.setFloat(1, configValueTypeDTO.getMin());
            stmUpdateConfig.setFloat(2, configValueTypeDTO.getMax());
            if (configValueTypeDTO.getVariableTime() == null) {
                stmUpdateConfig.setNull(3, Types.FLOAT);
            } else {
                stmUpdateConfig.setFloat(3, configValueTypeDTO.getVariableTime());
            }
            if (configValueTypeDTO.getVariableSpatial() == null) {
                stmUpdateConfig.setNull(4, Types.FLOAT);
            } else {
                stmUpdateConfig.setFloat(4, configValueTypeDTO.getVariableSpatial());
            }

            if (configValueTypeDTO.getStartDateApply() != null) {
                stmUpdateConfig.setDate(5, new Date(df.parse(configValueTypeDTO.getStartDateApply()).getTime()));
            } else {
                stmUpdateConfig.setDate(5, null);
            }
            if (configValueTypeDTO.getEndDateApply() != null)
                stmUpdateConfig.setDate(6, new Date(df.parse(configValueTypeDTO.getEndDateApply()).getTime()));
            else
                stmUpdateConfig.setDate(6, null);

            stmUpdateConfig.setString(7, configValueTypeDTO.getCode());
            stmUpdateConfig.setLong(8, configValueTypeDTO.getId());
            stmUpdateConfig.executeUpdate();

            // th???c hi???n xo?? nh???ng b???n ghi ???? b??? x??a ??? table

            for (ConfigStationsCommrelateDTO tmp : deletesSpatials) {
                stmDeleteSpatial.setLong(1, tmp.getId());
                stmDeleteSpatial.addBatch();
            }
            if (deletesSpatials.size() > 0) {
                stmDeleteSpatial.executeBatch();
            }

            for (ConfigStationsCommrelateDTO tmp : createSpatials) {
                stmCreateSpatial.setLong(1, tmp.getConfigValueTypeId());
                stmCreateSpatial.setLong(2, tmp.getConfigValueTypeParent());
                stmCreateSpatial.addBatch();
            }
            if (createSpatials.size() > 0) {
                stmCreateSpatial.executeBatch();
            }
            stmCreateSpatial.close();
            stmDeleteSpatial.close();
            stmUpdateConfig.close();
            connection.commit();
        } catch (SQLIntegrityConstraintViolationException e) {
            return DefaultResponseDTO.builder().status(0).message("M?? qu???n l?? chu???n h??a s??? li???u ???? t???n t???i").build();
        } finally {
            if (stmCreateSpatial != null) {
                stmCreateSpatial.close();
            }
            if (stmDeleteSpatial != null) {
                stmDeleteSpatial.close();
            }
            if (stmUpdateConfig != null) {
                stmUpdateConfig.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
        return DefaultResponseDTO.builder().status(1).message("Th??nh c??ng").build();
    }

    @Override
    public List<ConfigStationsCommrelateDTO> getListConfigStationsCommrelateDTO(Long parentId) throws Exception {
        List<ConfigStationsCommrelateDTO> configStationsCommrelateDTOs = new ArrayList<>();
        String sql = "select id, CONFIG_VALUE_TYPES_ID, config_value_types_parent from config_stations_commrelate where config_value_types_parent = ?";
        try (Connection connection = ds.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
            statement.setLong(1, parentId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                ConfigStationsCommrelateDTO configStationsCommrelateDTO = ConfigStationsCommrelateDTO.
                        builder().id(resultSet.getLong("id"))
                        .configValueTypeId(resultSet.getLong("CONFIG_VALUE_TYPES_ID"))
                        .configValueTypeParent(resultSet.getLong("config_value_types_parent"))
                        .build();
                configStationsCommrelateDTOs.add(configStationsCommrelateDTO);
            }
            return configStationsCommrelateDTOs;
        }
    }

    @Override
    public DefaultResponseDTO deleteConfigValuetype(Long id) throws Exception {
        String sqlDeleteConfig = "delete from config_stations_commrelate where config_value_types_parent = ?";
        String sqlDeteSpatial = "delete from config_value_types where id = ?";
        try (Connection connection = ds.getConnection(); PreparedStatement stmDeleteConfig = connection.prepareStatement(sqlDeleteConfig);
             PreparedStatement stmDeteSpatial = connection.prepareStatement(sqlDeteSpatial);) {
            connection.setAutoCommit(false);
            stmDeteSpatial.setLong(1, id);
            stmDeteSpatial.executeUpdate();

            stmDeleteConfig.setLong(1, id);
            stmDeleteConfig.executeUpdate();

            stmDeleteConfig.close();
            stmDeteSpatial.close();
            connection.commit();
        }
        return DefaultResponseDTO.builder().status(1).message("Th??nh c??ng").build();
    }

    @Override
    public Boolean isInsert(ConfigValueTypeDTO configValueTypeDTO) throws Exception {
        String sqlCheck = "";
        if (configValueTypeDTO.getEndDateApply() == null && configValueTypeDTO.getStartDateApply() == null) {
            sqlCheck = "select id from config_value_types where  station_id = ? and parameter_type_id = ?";
        } else if (configValueTypeDTO.getStartDateApply() == null && configValueTypeDTO.getEndDateApply() != null) {
            sqlCheck = "select id from config_value_types where (trunc(start_apply_date) <= trunc(?) or (start_apply_date is null and end_apply_date is null)) and station_id = ? and parameter_type_id = ?";
        } else if (configValueTypeDTO.getEndDateApply() == null && configValueTypeDTO.getStartDateApply() != null) {
            sqlCheck = "select id from config_value_types where (trunc(end_apply_date) >= trunc(?) or (start_apply_date is null and end_apply_date is null)) and station_id = ? and parameter_type_id = ?";
        } else {
            sqlCheck = "select id from config_value_types where ((trunc(start_apply_date) <= trunc(?) and trunc(end_apply_date)>= trunc(?)) or (trunc(start_apply_date) <= trunc(?) and trunc(end_apply_date)>= trunc(?)) or (start_apply_date is null and end_apply_date is null)) and station_id = ? and parameter_type_id = ?";
        }
        if (configValueTypeDTO.getId() != null) {
            sqlCheck = sqlCheck += " and id != ?";
        }
        logger.info("sql : {}", sqlCheck);

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        try (
                Connection connection = ds.getConnection();
                PreparedStatement statement = connection.prepareStatement(sqlCheck);
        ) {

            if (configValueTypeDTO.getEndDateApply() == null && configValueTypeDTO.getStartDateApply() == null) {
                statement.setString(1, configValueTypeDTO.getStationId());
                statement.setLong(2, configValueTypeDTO.getValueTypeId());
                if (configValueTypeDTO.getId() != null) {
                    statement.setLong(3, configValueTypeDTO.getId());
                }

            } else if (configValueTypeDTO.getStartDateApply() == null && configValueTypeDTO.getEndDateApply() != null) {
                statement.setDate(1, new Date(df.parse(configValueTypeDTO.getEndDateApply()).getTime()));
                statement.setString(2, configValueTypeDTO.getStationId());
                statement.setLong(3, configValueTypeDTO.getValueTypeId());
                if (configValueTypeDTO.getId() != null) {
                    statement.setLong(4, configValueTypeDTO.getId());
                }

            } else if (configValueTypeDTO.getEndDateApply() == null && configValueTypeDTO.getStartDateApply() != null) {
                statement.setDate(1, new Date(df.parse(configValueTypeDTO.getStartDateApply()).getTime()));
                statement.setString(2, configValueTypeDTO.getStationId());
                statement.setLong(3, configValueTypeDTO.getValueTypeId());
                if (configValueTypeDTO.getId() != null) {
                    statement.setLong(4, configValueTypeDTO.getId());
                }
            } else {
                statement.setDate(1, new Date(df.parse(configValueTypeDTO.getEndDateApply()).getTime()));
                statement.setDate(2, new Date(df.parse(configValueTypeDTO.getEndDateApply()).getTime()));
                statement.setDate(3, new Date(df.parse(configValueTypeDTO.getStartDateApply()).getTime()));
                statement.setDate(4, new Date(df.parse(configValueTypeDTO.getStartDateApply()).getTime()));
                statement.setString(5, configValueTypeDTO.getStationId());
                statement.setLong(6, configValueTypeDTO.getValueTypeId());
                if (configValueTypeDTO.getId() != null) {
                    statement.setLong(7, configValueTypeDTO.getId());
                }
            }
            ResultSet resultSet = statement.executeQuery();
            boolean isCheck = true;
            while (resultSet.next()) {
                isCheck = false;
                break;
            }
            return isCheck;
        }
    }

}
