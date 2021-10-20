package com.neo.nbdapi.dao;

import com.neo.nbdapi.entity.ComboBoxStr;

import java.sql.SQLException;
import java.util.List;

public interface ObjectTypeDAO {
    List<ComboBoxStr> getStationComboBox(String query) throws SQLException;
}
