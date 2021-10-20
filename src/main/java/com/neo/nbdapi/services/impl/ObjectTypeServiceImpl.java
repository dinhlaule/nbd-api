package com.neo.nbdapi.services.impl;

import com.neo.nbdapi.dao.ObjectTypeDAO;
import com.neo.nbdapi.entity.ComboBoxStr;
import com.neo.nbdapi.services.ObjectTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class ObjectTypeServiceImpl implements ObjectTypeService {

    @Autowired
    private ObjectTypeDAO objectTypeDAO;

    @Override
    public List<ComboBoxStr> getStationComboBox(String query) throws SQLException {
        return objectTypeDAO.getStationComboBox(query);
    }
}
