package com.neo.nbdapi.services;

import com.neo.nbdapi.entity.ComboBoxStr;

import java.sql.SQLException;
import java.util.List;

public interface ObjectTypeService {
    public List<ComboBoxStr> getStationComboBox(String query) throws SQLException;
}
