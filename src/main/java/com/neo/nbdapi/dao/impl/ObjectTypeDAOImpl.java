package com.neo.nbdapi.dao.impl;

import com.neo.nbdapi.dao.ObjectTypeDAO;
import com.neo.nbdapi.entity.ComboBoxStr;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ObjectTypeDAOImpl implements ObjectTypeDAO {
    @Autowired
    private HikariDataSource ds;

    @Override
    public List<ComboBoxStr> getStationComboBox(String query) throws SQLException {
        String sql = "select object_type_id as id, object_type as code, object_type_shortname as name from object_type where 1 = 1";
        if (query != null && !query.equals("")) {
            sql = sql + " and (UPPER(object_type_shortname) like ? or UPPER(object_type) like ?)";
        }
        sql = sql + " and rownum < 100  order by object_type";
        try (Connection connection = ds.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            if (query != null && !query.equals("")) {
                statement.setString(1, "%" + query.toUpperCase() + "%");
                statement.setString(2, "%" + query.toUpperCase() + "%");
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
}
