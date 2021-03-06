package com.neo.nbdapi.dao.impl;

import com.neo.nbdapi.dao.WarningThresholdValueDAO;
import com.neo.nbdapi.dto.DefaultResponseDTO;
import com.neo.nbdapi.entity.ComboBox;
import com.neo.nbdapi.entity.WarningThreshold;
import com.neo.nbdapi.rest.vm.WarningThresholdVM;
import com.neo.nbdapi.rest.vm.WarningThresholdValueVM;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class WarningThresholdValueDAOImpl implements WarningThresholdValueDAO {

    @Autowired
    private HikariDataSource ds;

    @Override
    public DefaultResponseDTO createWarningThreshold(WarningThresholdValueVM warningThresholdValueVM) throws SQLException {
        Connection connection = ds.getConnection();
        String sqlInsertThresholdValue = "insert into warning_threshold_value (id, parameter_type_id, value_level1, value_level2, value_level3, value_level4, value_level5,station_id) values (WARNING_THRESHOLD_VALUE_SEQ.nextval,?,?,?,?,?,?,?)";
        String sqlInsertThreshold = "insert into warning_threshold(id, code, warning_threshold_value_id, level_warning, LEVEL_CLEAN, status) values (WARNING_THRESHOLD_SEQ.nextval, ?, WARNING_THRESHOLD_VALUE_SEQ.CURRVAL, ?,?,?)";

        PreparedStatement stmInsertThresholdValue = null;
        PreparedStatement stmInsertThreshold = null;
        try{
            connection.setAutoCommit(false);
            stmInsertThresholdValue = connection.prepareStatement(sqlInsertThresholdValue);
            stmInsertThreshold = connection.prepareStatement(sqlInsertThreshold);

            //insert trong config threshol value
            stmInsertThresholdValue.setLong(1,warningThresholdValueVM.getParameterStation());
            stmInsertThresholdValue.setFloat(2,warningThresholdValueVM.getThreshold1());
            stmInsertThresholdValue.setFloat(3,warningThresholdValueVM.getThreshold2());
            stmInsertThresholdValue.setFloat(4,warningThresholdValueVM.getThreshold3());
            stmInsertThresholdValue.setFloat(5,warningThresholdValueVM.getThreshold4());
            stmInsertThresholdValue.setFloat(6,warningThresholdValueVM.getThreshold5());
            stmInsertThresholdValue.setString(7,warningThresholdValueVM.getStationId());
            stmInsertThresholdValue.executeUpdate();

            // insert c??c b???n ghi warning threshold

            List<WarningThresholdVM> warningThresholdVM = warningThresholdValueVM.getDataThreshold();
            for(WarningThresholdVM tmp : warningThresholdVM){
                stmInsertThreshold.setString(1,tmp.getWarningThresholdCode());
                if(tmp.getThresholdId() == null){
                    stmInsertThreshold.setNull(2,java.sql.Types.INTEGER);
                } else{
                    stmInsertThreshold.setLong(2,tmp.getThresholdId());
                }
                if(tmp.getThresholdCancelID()  == null){
                    stmInsertThreshold.setNull(3,java.sql.Types.INTEGER);
                } else{
                    stmInsertThreshold.setLong(3,tmp.getThresholdCancelID());
                }
                stmInsertThreshold.setInt(4,tmp.getStatus());
                stmInsertThreshold.addBatch();
            }
            stmInsertThreshold.executeBatch();
            connection.commit();
        } catch (Exception e){
            connection.rollback();
            throw e;
        } finally {
            if(stmInsertThresholdValue!= null){
                stmInsertThresholdValue.close();
            }
            if(stmInsertThreshold!= null){
                stmInsertThreshold.close();
            }
            if(connection!=null){
                connection.close();
            }
        }
        return DefaultResponseDTO.builder().status(1).message("Th??nh c??ng").build();
    }

    @Override
    public DefaultResponseDTO editWarningThreshold(WarningThresholdValueVM warningThresholdValueVM, List<WarningThreshold> deletes, List<WarningThreshold> updates, List<WarningThreshold> creates) throws SQLException {
        String sqlUpdateThresholdValue = "UPDATE warning_threshold_value set value_level1 = ?, value_level2=?, value_level3 = ?, value_level4 = ?, value_level5 = ? where id = ?";
        String sqlDeleteWarningThreshold = "delete from warning_threshold where code = ?";
        String sqlUpdateWarningThreshold = "update warning_threshold set level_clean = ? , level_warning = ?, status = ? where code = ?";
        String sqlCreateWarningThreshold = "insert into warning_threshold(id, code, warning_threshold_value_id, level_warning, LEVEL_CLEAN, status) values (WARNING_THRESHOLD_SEQ.nextval, ?, ?, ?, ?, ?)";
        //ch?? c??u truy v???n b??n tr??n l?? truy???n v??o ch??? kh??ng c?? ????? l???y
        Connection connection = ds.getConnection();
        PreparedStatement stmUpdateThresholdValue = null;
        PreparedStatement stmDeleteWarningThreshold = null;
        PreparedStatement stmUpdateWarningThreshold = null;
        PreparedStatement stmCreateWarningThreshold = null;
        try{
            connection.setAutoCommit(false);
             stmUpdateThresholdValue = connection.prepareStatement(sqlUpdateThresholdValue);
             stmDeleteWarningThreshold = connection.prepareStatement(sqlDeleteWarningThreshold);
             stmUpdateWarningThreshold = connection.prepareStatement(sqlUpdateWarningThreshold);
             stmCreateWarningThreshold = connection.prepareStatement(sqlCreateWarningThreshold);

            stmUpdateThresholdValue.setFloat(1, warningThresholdValueVM.getThreshold1());
            stmUpdateThresholdValue.setFloat(2, warningThresholdValueVM.getThreshold2());
            stmUpdateThresholdValue.setFloat(3, warningThresholdValueVM.getThreshold3());
            stmUpdateThresholdValue.setFloat(4, warningThresholdValueVM.getThreshold4());
            stmUpdateThresholdValue.setFloat(5, warningThresholdValueVM.getThreshold5());
            stmUpdateThresholdValue.setLong(6, warningThresholdValueVM.getId());
            stmUpdateThresholdValue.executeUpdate();

            // th???c hi???n delete tr?????c sau ???? t???o m???i, r???i sau ???? update

            for (WarningThreshold  delete : deletes) {
                stmDeleteWarningThreshold.setString(1,delete.getWarningThresholdCode());
                stmDeleteWarningThreshold.addBatch();
            }
            stmDeleteWarningThreshold.executeBatch();

            // th???c hi???n th??m m???i
            for (WarningThreshold create: creates) {
                stmCreateWarningThreshold.setString(1, create.getWarningThresholdCode());
                stmCreateWarningThreshold.setLong(2, warningThresholdValueVM.getId());
                stmCreateWarningThreshold.setLong(3,create.getThresholdId());
                stmCreateWarningThreshold.setLong(4,create.getThresholdCancelID());
                stmCreateWarningThreshold.setInt(5,create.getStatus());
                stmCreateWarningThreshold.addBatch();
            }
            stmCreateWarningThreshold.executeBatch();

            // th???c hi???n upadte warning threshold

            for(WarningThreshold update : updates){
                if(update.getThresholdCancelID() == null){
                    stmUpdateWarningThreshold.setNull(1,java.sql.Types.INTEGER);
                } else{
                    stmUpdateWarningThreshold.setLong(1,update.getThresholdCancelID());
                }
                if(update.getThresholdId() == null){
                    stmUpdateWarningThreshold.setNull(2,java.sql.Types.INTEGER);
                } else{
                    stmUpdateWarningThreshold.setLong(2,update.getThresholdId());
                }
                stmUpdateWarningThreshold.setInt(3,update.getStatus());
                stmUpdateWarningThreshold.setString(4, update.getWarningThresholdCode());
                stmUpdateWarningThreshold.addBatch();
            }
            stmUpdateWarningThreshold.executeBatch();
            connection.commit();
        } catch (Exception e){
            connection.rollback();
            throw e;
        } finally {
            if(stmUpdateThresholdValue!=null){
                stmUpdateThresholdValue.close();
            }
            if(stmDeleteWarningThreshold!=null){
                stmDeleteWarningThreshold.close();
            }
            if(stmUpdateWarningThreshold!=null){
                stmUpdateWarningThreshold.close();
            }
            if(stmCreateWarningThreshold!=null){
                stmCreateWarningThreshold.close();
            }
            if(connection != null){
                connection.close();
            }
        }

        return DefaultResponseDTO.builder().status(1).message("Th??nh c??ng").build();
    }

    @Override
    public ComboBox getValueType(Long id) throws SQLException {
        ComboBox comboBox = null;
        String sql = "select p.parameter_type_id, p.parameter_type_code, p.parameter_type_name from warning_threshold_value w inner join parameter_type p on w.parameter_type_id = p.parameter_type_id where w.id =?";
        try (Connection connection = ds.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
            statement.setLong(1, id);

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                comboBox = ComboBox.builder().id(resultSet.getLong("parameter_type_id"))
                        .text(resultSet.getString("parameter_type_code")+"-"+ resultSet.getString("parameter_type_name")).build();

            }
            return comboBox;
        }
    }

    @Override
    public DefaultResponseDTO deleteWarningThresholdValue(Long id) throws SQLException {
        Connection connection = ds.getConnection();
        String deleteValue = "delete from warning_threshold_value where id = ?";
        String delete = "delete from warning_threshold where warning_threshold_value_id = ?";
        PreparedStatement stmDeleteValue = connection.prepareStatement(deleteValue);
        PreparedStatement stmDelete = connection.prepareStatement(delete);
        try{
            connection.setAutoCommit(false);

            stmDelete.setLong(1,id);
            stmDelete.executeUpdate();
            stmDeleteValue.setLong(1,id);
            stmDeleteValue.executeUpdate();
            connection.commit();
        } catch (Exception e){
            connection.rollback();
            throw  e;
        } finally {
            stmDelete.close();
            stmDeleteValue.close();
            connection.close();
        }
        return  DefaultResponseDTO.builder().status(1).message("Th??nh c??ng").build();
    }

}
