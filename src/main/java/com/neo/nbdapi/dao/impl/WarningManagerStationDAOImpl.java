package com.neo.nbdapi.dao.impl;

import com.neo.nbdapi.dao.WarningManagerStationDAO;
import com.neo.nbdapi.dto.*;
import com.neo.nbdapi.entity.ComboBox;
import com.neo.nbdapi.entity.ComboBoxStr;
import com.neo.nbdapi.entity.NotificationHistoryDetail;
import com.neo.nbdapi.entity.WarningThresholdINF;
import com.neo.nbdapi.rest.vm.SelectVM;
import com.neo.nbdapi.rest.vm.SelectWarningManagerStrVM;
import com.neo.nbdapi.rest.vm.SelectWarningManagerVM;
import com.neo.nbdapi.rest.vm.WarningStationHistoryVM;
import com.neo.nbdapi.utils.DateUtils;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class WarningManagerStationDAOImpl implements WarningManagerStationDAO {
    private Logger logger = LogManager.getLogger(WarningManagerStationDAOImpl.class);
    @Autowired
    private HikariDataSource ds;

    @Override
    public List<ComboBox> getListParameterWarningConfig(SelectWarningManagerStrVM selectVM) throws SQLException {
        List<ComboBox> comboBoxes = new ArrayList<>();
        try (Connection connection = ds.getConnection()) {
            String sql = "select DISTINCT pt.parameter_type_id, pt.parameter_type_code, pt.parameter_type_name from warning_threshold_value wv inner join parameter_type pt on pt.parameter_type_id = wv.parameter_type_id where wv.station_id = ?";
            if (selectVM.getTerm() != null) {
                sql += "  and (pt.parameter_type_code like ? or pt.parameter_type_name like ?) ";
            }
            PreparedStatement statement = connection.prepareStatement(sql);
            if (selectVM.getId() == null) {
                return comboBoxes;
            }
            statement.setString(1, selectVM.getId());
            if (selectVM.getTerm() != null) {
                statement.setString(2, "%" + selectVM.getTerm() + "%");
                statement.setString(3, "%" + selectVM.getTerm() + "%");
            }
            ResultSet resultSet = statement.executeQuery();
            ComboBox comboBox = null;
            while (resultSet.next()) {
                comboBox = ComboBox.builder().id(resultSet.getLong("parameter_type_id"))
                        .text(resultSet.getString("parameter_type_code") + "-" + resultSet.getString("parameter_type_name")).build();
                comboBoxes.add(comboBox);
            }
            return comboBoxes;
        }
    }

    @Override
    public List<ComboBox> getListParameterWarningThreshold(SelectWarningManagerVM selectVM) throws SQLException {
        List<ComboBox> comboBoxes = new ArrayList<>();
        try (Connection connection = ds.getConnection()) {
            String sql = "select wt.id, wt.code from warning_threshold wt inner join warning_threshold_value wv on wt.warning_threshold_value_id = wv.id where wv.parameter_type_id = ? and wv.STATION_ID = ?";

            if (selectVM.getTerm() != null) {
                sql += "  and wt.code like ? ";
            }
            PreparedStatement statement = connection.prepareStatement(sql);
            if (selectVM.getId() == null) {
                return comboBoxes;
            }
            statement.setLong(1, selectVM.getId());
            statement.setString(2, selectVM.getIdStation());
            if (selectVM.getTerm() != null) {
                statement.setString(3, "%" + selectVM.getTerm() + "%");
            }
            ResultSet resultSet = statement.executeQuery();
            ComboBox comboBox = null;
            while (resultSet.next()) {
                comboBox = ComboBox.builder().id(resultSet.getLong("id"))
                        .text(resultSet.getString("code")).build();
                comboBoxes.add(comboBox);
            }
            return comboBoxes;
        }
    }

    @Override
    public WarningThresholdINF getInFoWarningThreshold(Long idThreshold) throws SQLException {
        String sql = "select w.level_warning, w.level_clean, wv.value_level1, wv.value_level2, wv.value_level3, wv.value_level4, wv.value_level5 from warning_threshold w inner join warning_threshold_value wv on wv.id = w.warning_threshold_value_id where w.id = ?";
        logger.info("sql get WarningThresholdINF : {}", sql);
        try (Connection connection = ds.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setLong(1, idThreshold);
            ResultSet resultSet = statement.executeQuery();
            WarningThresholdINF warningThresholdINF = null;
            while (resultSet.next()) {
                warningThresholdINF = WarningThresholdINF.builder()
                        .warningThreshold(resultSet.getString("level_warning") != null ? resultSet.getInt("level_warning") : null)
                        .warningThresholdCancel(resultSet.getString("level_clean") != null ? resultSet.getInt("level_clean") : null)
                        .valueLevel1(resultSet.getString("value_level1") != null ? resultSet.getFloat("value_level1") : null )
                        .valueLevel2(resultSet.getString("value_level2") != null ? resultSet.getFloat("value_level2") : null)
                        .valueLevel3(resultSet.getString("value_level3") != null ? resultSet.getFloat("value_level3") : null)
                        .valueLevel4(resultSet.getString("value_level4") != null ? resultSet.getFloat("value_level4") : null)
                        .valueLevel5(resultSet.getString("value_level5") != null ? resultSet.getFloat("value_level5") : null)
                        .build();
            }
            return warningThresholdINF;
        }
    }

    @Override
    public DefaultResponseDTO createWarningManagerStation(WarningManagerStationDTO warningManagerStationDTO) throws SQLException {
        String sqlCreateWarningManageStation = "insert into warning_manage_stations(id, code, name, description, content, color, icon, station_id,SUFFIXES_TABLE, created_by, created_at) values (WARNING_MANAGER_STATION_SEQ.nextval,?,?,?,?,?,?,?,?,?,sysdate)";
        String sqlCreateWarningManagerDetail = "insert into warning_manage_detail(id, warning_manage_station_id, warning_threshold_id, created_by, created_at) values (WARNING_MANAGER_DETAIL_SEQ.nextval, WARNING_MANAGER_STATION_SEQ.CURRVAL,?,?,sysdate)";

        logger.info("WarningManagerStationDAOImpl sql : {}", sqlCreateWarningManageStation);

        logger.info("WarningManagerStationDAOImpl sql : {}", sqlCreateWarningManagerDetail);

        PreparedStatement stmCreateWarningManageStation = null;
        PreparedStatement stmCreateWarningManagerDetail = null;
        Connection connection = null;
        try {
            connection = ds.getConnection();
            connection.setAutoCommit(false);
             stmCreateWarningManageStation = connection.prepareStatement(sqlCreateWarningManageStation);
             stmCreateWarningManagerDetail = connection.prepareStatement(sqlCreateWarningManagerDetail);

            // insert stmCreateWarningManageStation
            stmCreateWarningManageStation.setString(1, warningManagerStationDTO.getCode());
            stmCreateWarningManageStation.setString(2, warningManagerStationDTO.getName());
            stmCreateWarningManageStation.setString(3, warningManagerStationDTO.getDescription());
            stmCreateWarningManageStation.setString(4, warningManagerStationDTO.getContent());
            stmCreateWarningManageStation.setString(5, warningManagerStationDTO.getColor());
            stmCreateWarningManageStation.setString(6, warningManagerStationDTO.getIcon());
            stmCreateWarningManageStation.setString(7, warningManagerStationDTO.getStationId());
            stmCreateWarningManageStation.setString(8, warningManagerStationDTO.getSuffixesTable());
            stmCreateWarningManageStation.setString(9, warningManagerStationDTO.getCreateBy());
            stmCreateWarningManageStation.executeUpdate();

            // insert stmCreateWarningManagerDetail
            List<WarningManagerDetailDTO> dataWarning = warningManagerStationDTO.getDataWarning();

            for (WarningManagerDetailDTO obj : dataWarning) {
                stmCreateWarningManagerDetail.setLong(1, obj.getWarningThresholdId());
                stmCreateWarningManagerDetail.setString(2, obj.getCreateBy());
                stmCreateWarningManagerDetail.addBatch();

            }
            stmCreateWarningManagerDetail.executeBatch();

            connection.commit();

            stmCreateWarningManageStation.close();
            stmCreateWarningManagerDetail.close();
        } catch (Exception e) {
            logger.info("WarningManagerStationDAOImpl Exception : {}", e.getMessage());
            if (e instanceof SQLIntegrityConstraintViolationException) {
                return DefaultResponseDTO.builder().status(0).message("M?? c???nh b??o ???? t???n t???i").build();
            }
            return DefaultResponseDTO.builder().status(0).message("Kh??ng th??nh c??ng").build();
        } finally {
            if(stmCreateWarningManagerDetail!= null){
                stmCreateWarningManagerDetail.close();
            }
            if(stmCreateWarningManageStation!= null){
                stmCreateWarningManageStation.close();
            }
            if(connection!= null){
                connection.close();
            }
        }
        return DefaultResponseDTO.builder().status(1).message("Th??nh c??ng").build();
    }

    @Override
    public List<WarningMangerDetailInfoDTO> getWarningMangerDetailInfoDTOs(Long WarningManageStationId) throws SQLException {
        String sql = "select wd.id, pt.parameter_type_id, wt.id as warning_threshold_id, pt.parameter_type_name, wt.code, wt.level_warning, wt.level_clean, wd.created_by, wd.created_at, wv.value_level1, wv.value_level2, wv.value_level3, wv.value_level4, wv.value_level5 from warning_manage_detail wd inner join warning_threshold wt on wt.id = wd.warning_threshold_id inner join warning_threshold_value wv on wv.id = wt.warning_threshold_value_id inner join parameter_type pt on pt.parameter_type_id = wv.parameter_type_id where wd.warning_manage_station_id = ?";
        logger.info("sql get WarningThresholdINF : {}", sql);
        try (Connection connection = ds.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
            statement.setLong(1, WarningManageStationId);
            ResultSet resultSet = statement.executeQuery();
            List<WarningMangerDetailInfoDTO> warningMangerDetailInfoDTOs = new ArrayList<>();
            while (resultSet.next()) {
                WarningMangerDetailInfoDTO warningMangerDetailInfoDTO = WarningMangerDetailInfoDTO.builder()
                        .id(resultSet.getLong("id"))
                        .idParameter(resultSet.getLong("parameter_type_id"))
                        .idWarningThreshold(resultSet.getLong("warning_threshold_id"))
                        .nameParameter(resultSet.getString("parameter_type_name"))
                        .warningThresholdCode(resultSet.getString("code"))
                        .warningThreshold(resultSet.getInt("level_warning"))
                        .warningThresholdCancel(resultSet.getInt("level_clean"))
                        .valueLevel1(resultSet.getFloat("value_level1"))
                        .valueLevel2(resultSet.getFloat("value_level2"))
                        .valueLevel3(resultSet.getFloat("value_level3"))
                        .valueLevel4(resultSet.getFloat("value_level4"))
                        .valueLevel5(resultSet.getFloat("value_level5"))
                        .createBy(resultSet.getString("created_by"))
                        .createAt(resultSet.getString("created_at"))
                        .build();
                warningMangerDetailInfoDTOs.add(warningMangerDetailInfoDTO);

            }
            return warningMangerDetailInfoDTOs;
        }
    }

    @Override
    public DefaultResponseDTO editWarningManagerStation(WarningManagerStationDTO warningManagerStationDTO, List<WarningManagerDetailDTO> deletes, List<WarningManagerDetailDTO> creates) throws SQLException {
        String sqlUpdate = "update warning_manage_stations set code = ?, name = ?, description = ? , content = ? , color = ? , icon = ?, SUFFIXES_TABLE = ? where id = ?";
        String sqlDelete = "delete from warning_manage_detail where id = ?";
        String sqlCreate = "insert into warning_manage_detail(id, warning_manage_station_id, warning_threshold_id, created_by, created_at) values (WARNING_MANAGER_DETAIL_SEQ.nextval,?,?,?,sysdate)";

        logger.info("WarningManagerStationDAOImpl sql : {}", sqlUpdate);

        logger.info("WarningManagerStationDAOImpl sql : {}", sqlDelete);

        logger.info("WarningManagerStationDAOImpl sql : {}", sqlCreate);

        PreparedStatement stmUpdate = null;
        PreparedStatement stmDelete = null;
        PreparedStatement stmCreate = null;

        Connection connection = ds.getConnection();

        try {
            connection.setAutoCommit(false);
             stmUpdate = connection.prepareStatement(sqlUpdate);
             stmDelete = connection.prepareStatement(sqlDelete);
             stmCreate = connection.prepareStatement(sqlCreate);

            // th???c hi???n update
            stmUpdate.setString(1, warningManagerStationDTO.getCode());
            stmUpdate.setString(2, warningManagerStationDTO.getName());
            stmUpdate.setString(3, warningManagerStationDTO.getDescription());
            stmUpdate.setString(4, warningManagerStationDTO.getContent());
            stmUpdate.setString(5, warningManagerStationDTO.getColor());
            stmUpdate.setString(6, warningManagerStationDTO.getIcon());
            stmUpdate.setString(7, warningManagerStationDTO.getSuffixesTable());
            stmUpdate.setLong(8, warningManagerStationDTO.getId());
            stmUpdate.executeUpdate();

            // th???c hi???n th??m m???i
            for (WarningManagerDetailDTO obj : creates) {
                stmCreate.setLong(1, warningManagerStationDTO.getId());
                stmCreate.setLong(2, obj.getWarningThresholdId());
                stmCreate.setString(3, obj.getCreateBy());
                stmCreate.addBatch();

            }
            stmCreate.executeBatch();
            // th???c hi???n x??a

            for (WarningManagerDetailDTO obj : deletes) {
                stmDelete.setLong(1, obj.getId());
                stmDelete.addBatch();

            }
            stmDelete.executeBatch();
            connection.commit();

            stmUpdate.close();
            stmDelete.close();
            stmCreate.close();

        } catch (Exception e) {
            logger.info("WarningManagerStationDAOImpl Exception : {}", e.getMessage());
            if (e instanceof SQLIntegrityConstraintViolationException) {
                return DefaultResponseDTO.builder().status(0).message("M?? c???nh b??o ???? t???n t???i").build();
            }
            logger.info("WarningManagerStationDAOImpl exception : {}", e.getMessage());
        } finally {
            if(stmUpdate != null){
                stmUpdate.close();
            }
            if(stmDelete != null){
                stmDelete.close();
            }
            if(stmCreate != null){
                stmCreate.close();
            }
            connection.close();
        }
        return DefaultResponseDTO.builder().status(1).message("Th??nh c??ng").build();
    }

    @Override
    public DefaultResponseDTO deleteWarningManagerStation(List<Long> id) throws SQLException {
        String sqlDeleteManager = "delete from warning_manage_stations where id =?";
        String sqlDeleteDetail = "delete from warning_manage_detail where warning_manage_station_id = ?";
        PreparedStatement stmDetateManager =null;
        PreparedStatement stmDeleteDetail =null;
        Connection connection = null;
        try {
            connection = ds.getConnection();
            connection.setAutoCommit(false);
            stmDetateManager = connection.prepareStatement(sqlDeleteManager);
            stmDeleteDetail = connection.prepareStatement(sqlDeleteDetail);

            for (Long tmp : id) {
                stmDeleteDetail.setLong(1, tmp);
                stmDeleteDetail.addBatch();
                stmDetateManager.setLong(1, tmp);
                stmDetateManager.addBatch();
            }
            stmDeleteDetail.executeBatch();
            stmDetateManager.executeBatch();
            connection.commit();

        } catch (Exception e) {
            connection.rollback();
            logger.error("WarningManagerStationDAOImpl exception : {}", e.getMessage());
            return DefaultResponseDTO.builder().status(0).message("Kh??ng th??nh c??ng").build();

        } finally {
            if(stmDetateManager!= null){
                stmDetateManager.close();
            }
            if(stmDeleteDetail!= null){
                connection.close();
            }
            connection.close();
        }

        return DefaultResponseDTO.builder().status(1).message("Th??nh c??ng").build();
    }

    @Override
    public List<ComboBoxStr> getWarningComboBox(SelectWarningManagerStrVM selectVM) throws SQLException {
        String sql = "select id,code, name from warning_manage_stations where 1 = 1 and station_id = ? ";
        if (selectVM.getTerm() != null && !selectVM.getTerm().equals("")) {
            sql = sql + " code like ? or name like ?";
        }
        sql = sql + " and rownum < 100";
        try (Connection connection = ds.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setString(1, selectVM.getId());
            if (selectVM.getTerm() != null && !selectVM.getTerm().equals("")) {
                statement.setString(2, "%" + selectVM.getTerm() + "%");
                statement.setString(3, "%" + selectVM.getTerm() + "%");
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
    public List<NotificationToDayDTO> getListWarningManagerStationByDate(String startDate, String endDate) throws SQLException {
        logger.debug("START_DATE: {}, END_DATE: {}", startDate, endDate);
        String sql = "SELECT DISTINCT wms.id, nh.id AS notification_history_id, wms.name, wms.description, wms.color, wms.icon, wms.created_at, nh.push_timestap FROM warning_manage_stations wms JOIN warning_recipents wr ON wms.id = wr.manage_warning_stations JOIN notification_history nh ON wr.id = nh.warning_recipents_id WHERE nh.push_timestap >= to_date(?, 'dd/mm/yyyy HH24:mi')";
        if (endDate != null)
            sql = sql + " AND nh.push_timestap <= to_date(?, 'dd/mm/yyyy HH24:mi')";
        sql = sql + " ORDER BY nh.push_timestap DESC";
        List<NotificationToDayDTO> notificationToDayDTOList = new ArrayList<>();
        try (
                Connection connection = ds.getConnection();PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {
            preparedStatement.setString(1, startDate);
            if (endDate != null)
                preparedStatement.setString(2, endDate);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                NotificationToDayDTO notificationToDayDTO = NotificationToDayDTO.builder()
                        .id(resultSet.getLong("notification_history_id"))
                        .name(resultSet.getString("name"))
                        .description(resultSet.getString("description"))
                        .color(resultSet.getString("color"))
                        .icon(resultSet.getString("icon"))
                        .createdAt(DateUtils.getStringFromDateFormat(resultSet.getDate("created_at"), "dd/MM/yyyy"))
                        .pushTimestamp(DateUtils.getStringFromDateFormat(resultSet.getDate("push_timestap"), "dd/MM/yyyy HH:mm"))
                        .build();
                notificationToDayDTOList.add(notificationToDayDTO);
            }
        }
        return notificationToDayDTOList;
    }

    @Override
    public NotificationToDayDTO getWarningManagerStationById(Long notificationHistoryId) throws SQLException {
        String sql1 = "SELECT wms.id, wms.code, wms.name, wms.description, wms.content, wms.color, wms.icon, wms.created_at, st.station_name, st.station_id, nh.push_timestap FROM warning_manage_stations wms JOIN warning_recipents wr ON wms.id = wr.manage_warning_stations JOIN notification_history nh ON nh.warning_recipents_id = wr.id JOIN stations st ON wms.station_id = st.station_id WHERE nh.id = ?";
        String sql2 = "SELECT id, notification_history_id, parameter_type_id, parameter_type_name, parameter_value FROM notification_history_detail WHERE notification_history_id = ?";
        NotificationToDayDTO notificationToDayDTO = null;
        try (
                Connection connection = ds.getConnection();
                PreparedStatement statementGetNotification = connection.prepareStatement(sql1);

        ) {
            statementGetNotification.setLong(1, notificationHistoryId);
            ResultSet resultSet1 = statementGetNotification.executeQuery();
            if (resultSet1.next()) {
                notificationToDayDTO = NotificationToDayDTO.builder()
                        .id(resultSet1.getLong("id"))
                        .code(resultSet1.getString("code"))
                        .name(resultSet1.getString("name"))
                        .description(resultSet1.getString("description"))
                        .content(resultSet1.getString("content"))
                        .color(resultSet1.getString("color"))
                        .icon(resultSet1.getString("icon"))
                        .createdAt(DateUtils.getStringFromDateFormat(resultSet1.getDate("created_at"), "dd/MM/yyyy"))
                        .stationName(resultSet1.getString("station_name"))
                        .stationId(resultSet1.getString("station_id"))
                        .pushTimestamp(DateUtils.getStringFromDateFormat(resultSet1.getDate("push_timestap"), "dd/MM/yyyy HH:mm"))
                        .build();

                PreparedStatement statementGetDetail = connection.prepareStatement(sql2);
                statementGetDetail.setLong(1, notificationHistoryId);
                ResultSet resultSet2 = statementGetDetail.executeQuery();
                List<NotificationHistoryDetail> listDetail = new ArrayList<>();
                while (resultSet2.next()) {
                    NotificationHistoryDetail detail = NotificationHistoryDetail
                            .builder()
                            .id(resultSet2.getLong("id"))
                            .notificationHistoryId(resultSet2.getLong("notification_history_id"))
                            .parameterTypeId(resultSet2.getLong("parameter_type_id"))
                            .parameterTypeName(resultSet2.getString("parameter_type_name"))
                            .parameterValue(resultSet2.getFloat("parameter_value"))
                            .build();
                    listDetail.add(detail);
                }
                notificationToDayDTO.setDetails(listDetail);
            }


        }
        return notificationToDayDTO;
    }

    @Override
    public List<WarningStationHistoryDTO> getWarningStationHistory(WarningStationHistoryVM warningStationHistoryVM) throws SQLException {
        String sql = "SELECT * ";
        NotificationToDayDTO notificationToDayDTO = null;
        try (
                Connection connection = ds.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ) {

        }
        return null;
    }
}
