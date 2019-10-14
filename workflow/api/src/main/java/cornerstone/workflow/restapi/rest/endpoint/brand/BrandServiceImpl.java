package cornerstone.workflow.restapi.rest.endpoint.brand;

import cornerstone.workflow.restapi.datasource.MainDB;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BrandServiceImpl implements BrandService {
    private static final Logger logger = LoggerFactory.getLogger(BrandServiceImpl.class);
    private final BasicDataSource dataSource;

    @Inject
    public BrandServiceImpl(MainDB MainDB) {
        this.dataSource = MainDB;
    }

    public static List<Map<String,String>> extractResultSet(final ResultSet rs) throws SQLException {
        if ( rs != null ) {
            List<Map<String, String>> list = new LinkedList<>();
            Map<String, String> map;

            try {
                while (rs.next()) {
                    map = new HashMap<>();
                    for (int i = 1; i <= rs.getMetaData().getColumnCount(); ++i) {
                        map.put(rs.getMetaData().getColumnName(i), rs.getString(i));
                    }
                    list.add(map);
                }
            }
            catch (SQLException e){
                logger.error("Failed to parse ResultSet into List<Map<String,String> : {}", e.getMessage());
                throw e;
            }

            return list;
        }

        return null;
    }

    @Override
    public List<Map<String, String>> getBrands() throws SQLException {
        try (Connection c = dataSource.getConnection()){
            try (PreparedStatement ps = c.prepareStatement("SELECT id, name FROM Brands")){
                return extractResultSet(ps.executeQuery());
            }
        } catch (SQLException e){
            logger.error(e.getMessage());
            throw e;
        }
    }
}
