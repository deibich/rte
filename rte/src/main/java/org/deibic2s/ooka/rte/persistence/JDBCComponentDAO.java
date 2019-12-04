package org.deibic2s.ooka.rte.persistence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.deibic2s.ooka.rte.utils.ComponentState;

public class JDBCComponentDAO implements IComponentDAO {
    @Override
    public List<ComponentDTO> getAllDataComponents() {
        return doQuery("SELECT COMPONENTID, COMPONENTSTATE, PATHTOCOMPONENT, COMPONENTNAME FROM datacomponent");
    }

    @Override
    public ComponentDTO getDataComponent(Integer componentID) {
        if(componentID == null)
            return null;
        List<ComponentDTO> components = doQuery("SELECT COMPONENTID, COMPONENTSTATE, PATHTOCOMPONENT, COMPONENTNAME FROM datacomponent WHERE COMPONENTID="+componentID);
        if(components == null || components.size() == 0)
            return null;
        return components.get(0);
    }

    @Override
    public boolean createDataComponent(ComponentDTO componentDTO) {
        Connection connection = ConnectionFactory.getConnection();
        if(connection == null)
            return false;
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO datacomponent VALUES (NULL, ?, ?, ?, ?)");
            ps.setInt(1, componentDTO.getComponentID());
            ps.setString(2, componentDTO.getComponentState().toString());
            ps.setString(3, componentDTO.getPathToComponent());
            ps.setString(4, componentDTO.getName());
            if(ps.executeUpdate() == 1)
                return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removeDataComponent(ComponentDTO dataComponent) {
        Connection connection = ConnectionFactory.getConnection();
        if(connection == null)
            return false;
        try {
            Statement s = connection.createStatement();
            if(s.executeUpdate("DELETE FROM datacomponent WHERE componentid="+dataComponent.getComponentID()) == 1)
                return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateDataComponent(ComponentDTO componentDTO) {
        Connection connection = ConnectionFactory.getConnection();
        if(connection == null)
            return false;
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE datacomponent SET componentstate=?, pathtocomponent=?, componentname=? where componentid=?");
            ps.setString(1, componentDTO.getComponentState().toString());
            ps.setString(2, componentDTO.getPathToComponent());
            ps.setString(3, componentDTO.getName());
            ps.setInt(4, componentDTO.getComponentID());
            if(ps.executeUpdate() == 1)
                return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removeAllDataComponents() {
        Connection connection = ConnectionFactory.getConnection();
        if(connection == null)
            return false;
        try {
            Statement s = connection.createStatement();
            if(s.executeUpdate("DELETE FROM datacomponent") == 1)
                return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private List<ComponentDTO> doQuery(String queryString){
        if(queryString == null)
            return null;
        Connection c = ConnectionFactory.getConnection();
        if(c == null)
            return null;
        try {
            Statement s = c.createStatement();
            ResultSet rs = s.executeQuery(queryString);
            List<ComponentDTO> componentDTOS = new ArrayList<>();
            while(rs.next()){
                componentDTOS.add(extractDataComponent(rs));
            }
            return componentDTOS;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ComponentDTO extractDataComponent(ResultSet rs) throws SQLException{
        return new ComponentDTO(rs.getString("componentname"), rs.getInt("componentid"), ComponentState.valueOf(rs.getString("componentstate")), rs.getString("pathtocomponent"));
    }
}
